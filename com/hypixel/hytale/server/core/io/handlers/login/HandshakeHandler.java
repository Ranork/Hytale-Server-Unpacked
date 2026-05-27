package com.hypixel.hytale.server.core.io.handlers.login;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.HostAddress;
import com.hypixel.hytale.protocol.PlayerSkin;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.ChannelConnection;
import com.hypixel.hytale.protocol.io.ConnectionHandler;
import com.hypixel.hytale.protocol.packets.auth.AuthGrant;
import com.hypixel.hytale.protocol.packets.auth.AuthToken;
import com.hypixel.hytale.protocol.packets.auth.ServerAuthToken;
import com.hypixel.hytale.protocol.packets.connection.ClientDisconnect;
import com.hypixel.hytale.protocol.packets.connection.ClientType;
import com.hypixel.hytale.server.core.Constants;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.HytaleServerConfig;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.auth.AuthConfig;
import com.hypixel.hytale.server.core.auth.JWTValidator;
import com.hypixel.hytale.server.core.auth.PlayerAuthentication;
import com.hypixel.hytale.server.core.auth.ServerAuthManager;
import com.hypixel.hytale.server.core.auth.SessionServiceClient;
import com.hypixel.hytale.server.core.cosmetics.CosmeticsModule;
import com.hypixel.hytale.server.core.io.ProtocolVersion;
import com.hypixel.hytale.server.core.io.handlers.GenericConnectionPacketHandler;
import com.hypixel.hytale.server.core.modules.singleplayer.SingleplayerModule;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.UUID;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class HandshakeHandler extends GenericConnectionPacketHandler {
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   private static volatile SessionServiceClient sessionServiceClient;
   private static volatile JWTValidator jwtValidator;
   private volatile HandshakeHandler.AuthState authState = HandshakeHandler.AuthState.REQUESTING_AUTH_GRANT;
   private volatile boolean authTokenPacketReceived = false;
   private volatile String authenticatedUsername;
   private volatile PlayerSkin authenticatedSkin;
   private final ClientType clientType;
   private final String identityToken;
   private final byte[] referralData;
   private final HostAddress referralSource;
   @Nullable
   private volatile JWTValidator.IdentityTokenClaims identityClaims;

   public HandshakeHandler(
      @Nonnull ChannelConnection channel,
      @Nonnull ProtocolVersion protocolVersion,
      @Nonnull String language,
      @Nonnull ClientType clientType,
      @Nonnull String identityToken,
      @Nullable byte[] referralData,
      @Nullable HostAddress referralSource
   ) {
      super(channel, protocolVersion, language);
      this.clientType = clientType;
      this.identityToken = identityToken;
      this.referralData = referralData;
      this.referralSource = referralSource;
   }

   private static SessionServiceClient getSessionServiceClient() {
      if (sessionServiceClient == null) {
         synchronized (HandshakeHandler.class) {
            if (sessionServiceClient == null) {
               sessionServiceClient = new SessionServiceClient("https://sessions.hytale.com");
            }
         }
      }

      return sessionServiceClient;
   }

   private static JWTValidator getJwtValidator() {
      if (jwtValidator == null) {
         synchronized (HandshakeHandler.class) {
            if (jwtValidator == null) {
               jwtValidator = new JWTValidator(getSessionServiceClient(), "https://sessions.hytale.com", AuthConfig.getServerAudience());
            }
         }
      }

      return jwtValidator;
   }

   @Override
   public void accept(@Nonnull ToServerPacket packet) {
      switch (packet.getId()) {
         case 1:
            this.handle((ClientDisconnect)packet);
            break;
         case 12:
            this.handle((AuthToken)packet);
            break;
         default:
            this.disconnect(Message.translation("client.general.disconnect.protocol.unexpectedPacket").param("packetId", packet.getId()));
      }
   }

   @Override
   public void registered0(ConnectionHandler oldHandler) {
      HytaleServerConfig.TimeoutProfile timeouts = HytaleServer.get().getConfig().getConnectionTimeouts();
      this.enterStage("auth", timeouts.getAuth());
      this.identityClaims = getJwtValidator().validateIdentityToken(this.identityToken);
      if (this.identityClaims == null) {
         LOGGER.at(Level.WARNING).log("Identity token validation failed for %s", this.getChannel().formatRemoteAddress());
         this.disconnect(Message.translation("client.general.disconnect.invalidIdentityToken"));
      } else if (this.identityClaims.subject == null || this.identityClaims.subject.isEmpty()) {
         LOGGER.at(Level.WARNING).log("Identity token UUID missing for %s", this.getChannel().formatRemoteAddress());
         this.disconnect(Message.translation("client.general.disconnect.invalidIdentityToken"));
      } else if (this.identityClaims.username != null && !this.identityClaims.username.isEmpty()) {
         String requiredScope = this.clientType == ClientType.Editor ? "hytale:editor" : "hytale:client";
         if (!this.identityClaims.hasScope(requiredScope)) {
            LOGGER.at(Level.WARNING)
               .log(
                  "Identity token missing required scope for %s from %s (clientType: %s, required: %s, actual: %s)",
                  this.identityClaims.username,
                  this.getChannel().formatRemoteAddress(),
                  this.clientType,
                  requiredScope,
                  this.identityClaims.scope
               );
            this.disconnect(Message.translation("client.general.disconnect.identityTokenMissingScope").param("scope", requiredScope));
         } else {
            LOGGER.at(Level.INFO)
               .log(
                  "Identity token validated for %s (UUID: %s, scope: %s) from %s, requesting auth grant",
                  this.identityClaims.username,
                  this.identityClaims.subject,
                  this.identityClaims.scope,
                  this.getChannel().formatRemoteAddress()
               );
            this.continueStage("auth:grant", timeouts.getAuthGrant(), () -> this.authState != HandshakeHandler.AuthState.REQUESTING_AUTH_GRANT);
            this.requestAuthGrant();
         }
      } else {
         LOGGER.at(Level.WARNING).log("Identity token username missing for %s", this.getChannel().formatRemoteAddress());
         this.disconnect(Message.translation("client.general.disconnect.invalidIdentityToken"));
      }
   }

   private void requestAuthGrant() {
      String serverSessionToken = ServerAuthManager.getInstance().getSessionToken();
      if (serverSessionToken != null && !serverSessionToken.isEmpty()) {
         ChannelConnection channel = this.getChannel();
         getSessionServiceClient()
            .requestAuthorizationGrantAsync(this.identityToken, AuthConfig.getServerAudience(), serverSessionToken)
            .thenAccept(
               authGrant -> {
                  if (channel.isActive()) {
                     if (authGrant == null) {
                        channel.execute(() -> this.disconnect(Message.translation("client.general.disconnect.authGrantFailed")));
                     } else {
                        String serverIdentityToken = ServerAuthManager.getInstance().getIdentityToken();
                        if (serverIdentityToken != null && !serverIdentityToken.isEmpty()) {
                           channel.execute(
                              () -> {
                                 if (channel.isActive()) {
                                    if (this.authState != HandshakeHandler.AuthState.REQUESTING_AUTH_GRANT) {
                                       LOGGER.at(Level.WARNING).log("State changed during auth grant request, current state: %s", this.authState);
                                    } else {
                                       this.clearTimeout();
                                       LOGGER.at(Level.INFO)
                                          .log(
                                             "Sending AuthGrant to %s (with server identity: %s)",
                                             channel.formatRemoteAddress(),
                                             !serverIdentityToken.isEmpty()
                                          );
                                       this.write(new AuthGrant(authGrant, serverIdentityToken));
                                       this.authState = HandshakeHandler.AuthState.AWAITING_AUTH_TOKEN;
                                       HytaleServerConfig.TimeoutProfile timeouts = HytaleServer.get().getConfig().getConnectionTimeouts();
                                       this.continueStage(
                                          "auth:token", timeouts.getAuthToken(), () -> this.authState != HandshakeHandler.AuthState.AWAITING_AUTH_TOKEN
                                       );
                                    }
                                 }
                              }
                           );
                        } else {
                           LOGGER.at(Level.SEVERE).log("Server identity token not available - cannot complete mutual authentication");
                           channel.execute(() -> this.disconnect(Message.translation("client.general.disconnect.serverAuthUnavailable")));
                        }
                     }
                  }
               }
            )
            .exceptionally(ex -> {
               ((HytaleLogger.Api)LOGGER.at(Level.WARNING).withCause(ex)).log("Error requesting auth grant");
               channel.execute(() -> this.disconnect(Message.translation("client.general.disconnect.authError")));
               return null;
            });
      } else {
         LOGGER.at(Level.SEVERE).log("Server session token not available - cannot request auth grant");
         this.disconnect(Message.translation("client.general.disconnect.serverAuthUnavailable"));
      }
   }

   public void handle(@Nonnull ClientDisconnect packet) {
      this.disconnectReason.setClientDisconnectType(packet.type);
      if (this.identityClaims != null) {
         LOGGER.at(Level.INFO)
            .log(
               "%s (%s) at %s left with reason: %s - %s",
               this.identityClaims.subject,
               this.identityClaims.username,
               this.getChannel().formatRemoteAddress(),
               packet.type.name(),
               packet.reason.name()
            );
      }

      this.getChannel().closeApplicationConnection();
   }

   public void handle(@Nonnull AuthToken packet) {
      ChannelConnection channel = this.getChannel();
      if (this.authState != HandshakeHandler.AuthState.AWAITING_AUTH_TOKEN) {
         LOGGER.at(Level.WARNING).log("Received unexpected AuthToken packet in state %s from %s", this.authState, channel.formatRemoteAddress());
         this.disconnect(Message.translation("client.general.disconnect.protocol.unexpectedAuthToken"));
      } else if (this.authTokenPacketReceived) {
         LOGGER.at(Level.WARNING).log("Received duplicate AuthToken packet from %s", channel.formatRemoteAddress());
         this.disconnect(Message.translation("client.general.disconnect.protocol.duplicateAuthToken"));
      } else {
         this.authTokenPacketReceived = true;
         this.authState = HandshakeHandler.AuthState.PROCESSING_AUTH_TOKEN;
         this.clearTimeout();
         String accessToken = packet.accessToken;
         if (accessToken != null && !accessToken.isEmpty()) {
            String serverAuthGrant = packet.serverAuthorizationGrant;
            X509Certificate clientCert = channel.getClientCertificate();
            LOGGER.at(Level.INFO)
               .log(
                  "Received AuthToken from %s, validating JWT (mTLS cert present: %s, server auth grant: %s)",
                  channel.formatRemoteAddress(),
                  clientCert != null,
                  serverAuthGrant != null && !serverAuthGrant.isEmpty()
               );
            JWTValidator.JWTClaims claims = getJwtValidator().validateToken(accessToken, clientCert);
            if (claims == null) {
               LOGGER.at(Level.WARNING).log("JWT validation failed for %s", channel.formatRemoteAddress());
               this.disconnect(Message.translation("client.general.disconnect.invalidAccessToken"));
            } else if (this.identityClaims == null) {
               LOGGER.at(Level.WARNING).log("Identity token is null prior to validating authentication token for %s", this.getChannel().formatRemoteAddress());
               this.disconnect(Message.translation("client.general.disconnect.invalidIdentityToken"));
            } else {
               String tokenUuid = claims.subject;
               String tokenUsername = claims.username;
               if (tokenUuid == null || !tokenUuid.equals(this.identityClaims.subject)) {
                  LOGGER.at(Level.WARNING)
                     .log("JWT UUID mismatch for %s (expected: %s, got: %s)", this.getChannel().formatRemoteAddress(), this.identityClaims.subject, tokenUuid);
                  this.disconnect(Message.translation("client.general.disconnect.tokenUuidMismatch"));
               } else if (tokenUsername == null || tokenUsername.isEmpty()) {
                  LOGGER.at(Level.WARNING).log("JWT missing username for %s", channel.formatRemoteAddress());
                  this.disconnect(Message.translation("client.general.disconnect.tokenMissingUsername"));
               } else if (!tokenUsername.equals(this.identityClaims.username)) {
                  LOGGER.at(Level.WARNING)
                     .log(
                        "JWT username mismatch for %s (expected: %s, got: %s)",
                        this.getChannel().formatRemoteAddress(),
                        this.identityClaims.username,
                        tokenUsername
                     );
                  this.disconnect(Message.translation("client.general.disconnect.tokenUsernameMismatch"));
               } else {
                  this.authenticatedUsername = tokenUsername;
                  if (this.identityClaims.skin != null && !this.identityClaims.skin.isEmpty()) {
                     try {
                        PlayerSkin playerSkin = CosmeticsModule.get().parseSkinFromJson(this.identityClaims.skin);
                        if (playerSkin == null) {
                           this.disconnect(Message.translation("client.general.disconnect.invalidSkin").param("details", "failed to parse skin data"));
                           return;
                        }

                        CosmeticsModule.get().validateSkin(playerSkin);
                        this.authenticatedSkin = playerSkin;
                     } catch (CosmeticsModule.InvalidSkinException var10) {
                        this.disconnect(Message.translation("client.general.disconnect.invalidSkin").param("details", var10.getMessage()));
                        return;
                     }
                  }

                  if (serverAuthGrant != null && !serverAuthGrant.isEmpty()) {
                     this.authState = HandshakeHandler.AuthState.EXCHANGING_SERVER_TOKEN;
                     HytaleServerConfig.TimeoutProfile timeouts = HytaleServer.get().getConfig().getConnectionTimeouts();
                     this.continueStage(
                        "auth:server-exchange", timeouts.getAuthServerExchange(), () -> this.authState != HandshakeHandler.AuthState.EXCHANGING_SERVER_TOKEN
                     );
                     this.exchangeServerAuthGrant(serverAuthGrant);
                  } else {
                     LOGGER.at(Level.WARNING).log("Client did not provide server auth grant for mutual authentication");
                     this.disconnect(Message.translation("client.general.disconnect.mutualAuthRequired"));
                  }
               }
            }
         } else {
            LOGGER.at(Level.WARNING).log("Received AuthToken packet with empty access token from %s", channel.formatRemoteAddress());
            this.disconnect(Message.translation("client.general.disconnect.invalidAccessToken"));
         }
      }
   }

   private void exchangeServerAuthGrant(@Nonnull String serverAuthGrant) {
      ServerAuthManager serverAuthManager = ServerAuthManager.getInstance();
      String serverCertFingerprint = serverAuthManager.getServerCertificateFingerprint();
      if (serverCertFingerprint == null) {
         LOGGER.at(Level.SEVERE).log("Server certificate fingerprint not available for mutual auth");
         this.disconnect(Message.translation("client.general.disconnect.serverAuthUnavailable"));
      } else {
         String serverSessionToken = serverAuthManager.getSessionToken();
         LOGGER.at(Level.FINE)
            .log("Server session token available: %s, identity token available: %s", serverSessionToken != null, serverAuthManager.getIdentityToken() != null);
         if (serverSessionToken == null) {
            LOGGER.at(Level.SEVERE).log("Server session token not available for auth grant exchange");
            LOGGER.at(Level.FINE)
               .log(
                  "Auth mode: %s, has session token: %s, has identity token: %s",
                  serverAuthManager.getAuthStatus(),
                  serverAuthManager.hasSessionToken(),
                  serverAuthManager.hasIdentityToken()
               );
            this.disconnect(Message.translation("client.general.disconnect.serverAuthUnavailable"));
         } else {
            LOGGER.at(Level.FINE)
               .log("Using session token (first 20 chars): %s...", serverSessionToken.length() > 20 ? serverSessionToken.substring(0, 20) : serverSessionToken);
            ChannelConnection channel = this.getChannel();
            getSessionServiceClient()
               .exchangeAuthGrantForTokenAsync(serverAuthGrant, serverCertFingerprint, serverSessionToken)
               .thenAccept(
                  serverAccessToken -> {
                     if (channel.isActive()) {
                        channel.execute(
                           () -> {
                              if (channel.isActive()) {
                                 if (this.authState != HandshakeHandler.AuthState.EXCHANGING_SERVER_TOKEN) {
                                    LOGGER.at(Level.WARNING).log("State changed during server token exchange, current state: %s", this.authState);
                                 } else if (serverAccessToken == null) {
                                    LOGGER.at(Level.SEVERE).log("Failed to exchange server auth grant for access token");
                                    this.disconnect(Message.translation("client.general.disconnect.serverAuthFailed"));
                                 } else {
                                    byte[] passwordChallenge = this.generatePasswordChallengeIfNeeded();
                                    LOGGER.at(Level.INFO)
                                       .log(
                                          "Sending ServerAuthToken to %s (with password challenge: %s)",
                                          channel.formatRemoteAddress(),
                                          passwordChallenge != null
                                       );
                                    this.write(new ServerAuthToken(serverAccessToken, passwordChallenge));
                                    this.completeAuthentication(passwordChallenge);
                                 }
                              }
                           }
                        );
                     }
                  }
               )
               .exceptionally(ex -> {
                  ((HytaleLogger.Api)LOGGER.at(Level.WARNING).withCause(ex)).log("Error exchanging server auth grant");
                  channel.execute(() -> {
                     if (this.authState == HandshakeHandler.AuthState.EXCHANGING_SERVER_TOKEN) {
                        this.disconnect(Message.translation("client.general.disconnect.serverAuthFailed"));
                     }
                  });
                  return null;
               });
         }
      }
   }

   private byte[] generatePasswordChallengeIfNeeded() {
      String password = HytaleServer.get().getConfig().getPassword();
      if (password != null && !password.isEmpty()) {
         if (Constants.SINGLEPLAYER) {
            UUID ownerUuid = SingleplayerModule.getUuid();
            if (ownerUuid != null && ownerUuid.equals(this.identityClaims.getSubjectAsUUID())) {
               return null;
            }
         }

         byte[] challenge = new byte[32];
         new SecureRandom().nextBytes(challenge);
         return challenge;
      } else {
         return null;
      }
   }

   private void completeAuthentication(byte[] passwordChallenge) {
      UUID uuid = this.identityClaims.getSubjectAsUUID();
      if (uuid == null) {
         LOGGER.at(Level.SEVERE).log("Identity token subject is not a valid UUID: %s", this.identityClaims.subject);
         this.disconnect(Message.translation("client.general.disconnect.invalidIdentityToken"));
      } else {
         this.auth = new PlayerAuthentication(uuid, this.authenticatedUsername);
         if (this.referralData != null) {
            this.auth.setReferralData(this.referralData);
         }

         if (this.referralSource != null) {
            this.auth.setReferralSource(this.referralSource);
         }

         if (this.authenticatedSkin != null) {
            this.auth.setSkin(this.authenticatedSkin);
         }

         this.authState = HandshakeHandler.AuthState.AUTHENTICATED;
         this.clearTimeout();
         LOGGER.at(Level.INFO)
            .log(
               "Mutual authentication complete for %s (%s) from %s",
               this.authenticatedUsername,
               this.identityClaims.subject,
               this.getChannel().formatRemoteAddress()
            );
         this.onAuthenticated(passwordChallenge);
      }
   }

   protected abstract void onAuthenticated(byte[] var1);

   private static enum AuthState {
      REQUESTING_AUTH_GRANT,
      AWAITING_AUTH_TOKEN,
      PROCESSING_AUTH_TOKEN,
      EXCHANGING_SERVER_TOKEN,
      AUTHENTICATED;
   }
}
