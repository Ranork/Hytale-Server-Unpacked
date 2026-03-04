package com.hypixel.hytale.server.core.io.handlers.login;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.HostAddress;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.netty.ProtocolUtil;
import com.hypixel.hytale.protocol.packets.auth.AuthGrant;
import com.hypixel.hytale.protocol.packets.auth.AuthToken;
import com.hypixel.hytale.protocol.packets.auth.ServerAuthToken;
import com.hypixel.hytale.protocol.packets.connection.ClientType;
import com.hypixel.hytale.protocol.packets.connection.Disconnect;
import com.hypixel.hytale.server.core.Constants;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.HytaleServerConfig;
import com.hypixel.hytale.server.core.auth.AuthConfig;
import com.hypixel.hytale.server.core.auth.JWTValidator;
import com.hypixel.hytale.server.core.auth.PlayerAuthentication;
import com.hypixel.hytale.server.core.auth.ServerAuthManager;
import com.hypixel.hytale.server.core.auth.SessionServiceClient;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.io.ProtocolVersion;
import com.hypixel.hytale.server.core.io.handlers.GenericConnectionPacketHandler;
import com.hypixel.hytale.server.core.io.netty.NettyUtil;
import com.hypixel.hytale.server.core.io.transport.QUICTransport;
import com.hypixel.hytale.server.core.modules.singleplayer.SingleplayerModule;
import io.netty.channel.Channel;
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
   private final ClientType clientType;
   private final String identityToken;
   private final UUID playerUuid;
   private final String username;
   private final byte[] referralData;
   private final HostAddress referralSource;

   public HandshakeHandler(
      @Nonnull Channel channel,
      @Nonnull ProtocolVersion protocolVersion,
      @Nonnull String language,
      @Nonnull ClientType clientType,
      @Nonnull String identityToken,
      @Nonnull UUID playerUuid,
      @Nonnull String username,
      @Nullable byte[] referralData,
      @Nullable HostAddress referralSource
   ) {
      super(channel, protocolVersion, language);
      this.clientType = clientType;
      this.identityToken = identityToken;
      this.playerUuid = playerUuid;
      this.username = username;
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
            this.handle((Disconnect)packet);
            break;
         case 12:
            this.handle((AuthToken)packet);
            break;
         default:
            this.disconnect("Protocol error: unexpected packet " + packet.getId());
      }
   }

   @Override
   public void registered0(PacketHandler oldHandler) {
      HytaleServerConfig.TimeoutProfile timeouts = HytaleServer.get().getConfig().getConnectionTimeouts();
      this.enterStage("auth", timeouts.getAuth());
      JWTValidator.IdentityTokenClaims identityClaims = getJwtValidator().validateIdentityToken(this.identityToken);
      if (identityClaims == null) {
         LOGGER.at(Level.WARNING).log("Identity token validation failed for %s from %s", this.username, NettyUtil.formatRemoteAddress(this.getChannel()));
         this.disconnect("Invalid or expired identity token");
      } else {
         UUID tokenUuid = identityClaims.getSubjectAsUUID();
         if (tokenUuid != null && tokenUuid.equals(this.playerUuid)) {
            String requiredScope = this.clientType == ClientType.Editor ? "hytale:editor" : "hytale:client";
            if (!identityClaims.hasScope(requiredScope)) {
               LOGGER.at(Level.WARNING)
                  .log(
                     "Identity token missing required scope for %s from %s (clientType: %s, required: %s, actual: %s)",
                     this.username,
                     NettyUtil.formatRemoteAddress(this.getChannel()),
                     this.clientType,
                     requiredScope,
                     identityClaims.scope
                  );
               this.disconnect("Invalid identity token: missing " + requiredScope + " scope");
            } else {
               LOGGER.at(Level.INFO)
                  .log(
                     "Identity token validated for %s (UUID: %s, scope: %s) from %s, requesting auth grant",
                     this.username,
                     this.playerUuid,
                     identityClaims.scope,
                     NettyUtil.formatRemoteAddress(this.getChannel())
                  );
               this.continueStage("auth:grant", timeouts.getAuthGrant(), () -> this.authState != HandshakeHandler.AuthState.REQUESTING_AUTH_GRANT);
               this.requestAuthGrant();
            }
         } else {
            LOGGER.at(Level.WARNING)
               .log(
                  "Identity token UUID mismatch for %s from %s (expected: %s, got: %s)",
                  this.username,
                  NettyUtil.formatRemoteAddress(this.getChannel()),
                  this.playerUuid,
                  tokenUuid
               );
            this.disconnect("Invalid identity token: UUID mismatch");
         }
      }
   }

   private void requestAuthGrant() {
      String serverSessionToken = ServerAuthManager.getInstance().getSessionToken();
      if (serverSessionToken != null && !serverSessionToken.isEmpty()) {
         Channel channel = this.getChannel();
         getSessionServiceClient()
            .requestAuthorizationGrantAsync(this.identityToken, AuthConfig.getServerAudience(), serverSessionToken)
            .thenAccept(
               authGrant -> {
                  if (channel.isActive()) {
                     if (authGrant == null) {
                        channel.eventLoop().execute(() -> this.disconnect("Failed to obtain authorization grant from session service"));
                     } else {
                        String serverIdentityToken = ServerAuthManager.getInstance().getIdentityToken();
                        if (serverIdentityToken != null && !serverIdentityToken.isEmpty()) {
                           channel.eventLoop()
                              .execute(
                                 () -> {
                                    if (channel.isActive()) {
                                       if (this.authState != HandshakeHandler.AuthState.REQUESTING_AUTH_GRANT) {
                                          LOGGER.at(Level.WARNING).log("State changed during auth grant request, current state: %s", this.authState);
                                       } else {
                                          this.clearTimeout();
                                          LOGGER.at(Level.INFO)
                                             .log(
                                                "Sending AuthGrant to %s (with server identity: %s)",
                                                NettyUtil.formatRemoteAddress(channel),
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
                           channel.eventLoop().execute(() -> this.disconnect("Server authentication unavailable - please try again later"));
                        }
                     }
                  }
               }
            )
            .exceptionally(ex -> {
               ((HytaleLogger.Api)LOGGER.at(Level.WARNING).withCause(ex)).log("Error requesting auth grant");
               channel.eventLoop().execute(() -> this.disconnect("Authentication error: " + ex.getMessage()));
               return null;
            });
      } else {
         LOGGER.at(Level.SEVERE).log("Server session token not available - cannot request auth grant");
         this.disconnect("Server authentication unavailable - please try again later");
      }
   }

   public void handle(@Nonnull Disconnect packet) {
      this.disconnectReason.setClientDisconnectType(packet.type);
      LOGGER.at(Level.INFO)
         .log(
            "%s (%s) at %s left with reason: %s - %s",
            this.playerUuid,
            this.username,
            NettyUtil.formatRemoteAddress(this.getChannel()),
            packet.type.name(),
            packet.reason
         );
      ProtocolUtil.closeApplicationConnection(this.getChannel());
   }

   public void handle(@Nonnull AuthToken packet) {
      Channel channel = this.getChannel();
      if (this.authState != HandshakeHandler.AuthState.AWAITING_AUTH_TOKEN) {
         LOGGER.at(Level.WARNING).log("Received unexpected AuthToken packet in state %s from %s", this.authState, NettyUtil.formatRemoteAddress(channel));
         this.disconnect("Protocol error: unexpected AuthToken packet");
      } else if (this.authTokenPacketReceived) {
         LOGGER.at(Level.WARNING).log("Received duplicate AuthToken packet from %s", NettyUtil.formatRemoteAddress(channel));
         this.disconnect("Protocol error: duplicate AuthToken packet");
      } else {
         this.authTokenPacketReceived = true;
         this.authState = HandshakeHandler.AuthState.PROCESSING_AUTH_TOKEN;
         this.clearTimeout();
         String accessToken = packet.accessToken;
         if (accessToken != null && !accessToken.isEmpty()) {
            String serverAuthGrant = packet.serverAuthorizationGrant;
            X509Certificate clientCert = (X509Certificate)channel.attr(QUICTransport.CLIENT_CERTIFICATE_ATTR).get();
            LOGGER.at(Level.INFO)
               .log(
                  "Received AuthToken from %s, validating JWT (mTLS cert present: %s, server auth grant: %s)",
                  NettyUtil.formatRemoteAddress(channel),
                  clientCert != null,
                  serverAuthGrant != null && !serverAuthGrant.isEmpty()
               );
            JWTValidator.JWTClaims claims = getJwtValidator().validateToken(accessToken, clientCert);
            if (claims == null) {
               LOGGER.at(Level.WARNING).log("JWT validation failed for %s", NettyUtil.formatRemoteAddress(channel));
               this.disconnect("Invalid access token");
            } else {
               UUID tokenUuid = claims.getSubjectAsUUID();
               String tokenUsername = claims.username;
               if (tokenUuid == null || !tokenUuid.equals(this.playerUuid)) {
                  LOGGER.at(Level.WARNING)
                     .log("JWT UUID mismatch for %s (expected: %s, got: %s)", NettyUtil.formatRemoteAddress(channel), this.playerUuid, tokenUuid);
                  this.disconnect("Invalid token claims: UUID mismatch");
               } else if (tokenUsername == null || tokenUsername.isEmpty()) {
                  LOGGER.at(Level.WARNING).log("JWT missing username for %s", NettyUtil.formatRemoteAddress(channel));
                  this.disconnect("Invalid token claims: missing username");
               } else if (!tokenUsername.equals(this.username)) {
                  LOGGER.at(Level.WARNING)
                     .log("JWT username mismatch for %s (expected: %s, got: %s)", NettyUtil.formatRemoteAddress(channel), this.username, tokenUsername);
                  this.disconnect("Invalid token claims: username mismatch");
               } else {
                  this.authenticatedUsername = tokenUsername;
                  if (serverAuthGrant != null && !serverAuthGrant.isEmpty()) {
                     this.authState = HandshakeHandler.AuthState.EXCHANGING_SERVER_TOKEN;
                     HytaleServerConfig.TimeoutProfile timeouts = HytaleServer.get().getConfig().getConnectionTimeouts();
                     this.continueStage(
                        "auth:server-exchange", timeouts.getAuthServerExchange(), () -> this.authState != HandshakeHandler.AuthState.EXCHANGING_SERVER_TOKEN
                     );
                     this.exchangeServerAuthGrant(serverAuthGrant);
                  } else {
                     LOGGER.at(Level.WARNING).log("Client did not provide server auth grant for mutual authentication");
                     this.disconnect("Mutual authentication required - please update your client");
                  }
               }
            }
         } else {
            LOGGER.at(Level.WARNING).log("Received AuthToken packet with empty access token from %s", NettyUtil.formatRemoteAddress(channel));
            this.disconnect("Invalid access token");
         }
      }
   }

   private void exchangeServerAuthGrant(@Nonnull String serverAuthGrant) {
      ServerAuthManager serverAuthManager = ServerAuthManager.getInstance();
      String serverCertFingerprint = serverAuthManager.getServerCertificateFingerprint();
      if (serverCertFingerprint == null) {
         LOGGER.at(Level.SEVERE).log("Server certificate fingerprint not available for mutual auth");
         this.disconnect("Server authentication unavailable - please try again later");
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
            this.disconnect("Server authentication unavailable - please try again later");
         } else {
            LOGGER.at(Level.FINE)
               .log("Using session token (first 20 chars): %s...", serverSessionToken.length() > 20 ? serverSessionToken.substring(0, 20) : serverSessionToken);
            Channel channel = this.getChannel();
            getSessionServiceClient()
               .exchangeAuthGrantForTokenAsync(serverAuthGrant, serverCertFingerprint, serverSessionToken)
               .thenAccept(
                  serverAccessToken -> {
                     if (channel.isActive()) {
                        channel.eventLoop()
                           .execute(
                              () -> {
                                 if (channel.isActive()) {
                                    if (this.authState != HandshakeHandler.AuthState.EXCHANGING_SERVER_TOKEN) {
                                       LOGGER.at(Level.WARNING).log("State changed during server token exchange, current state: %s", this.authState);
                                    } else if (serverAccessToken == null) {
                                       LOGGER.at(Level.SEVERE).log("Failed to exchange server auth grant for access token");
                                       this.disconnect("Server authentication failed - please try again later");
                                    } else {
                                       byte[] passwordChallenge = this.generatePasswordChallengeIfNeeded();
                                       LOGGER.at(Level.INFO)
                                          .log(
                                             "Sending ServerAuthToken to %s (with password challenge: %s)",
                                             NettyUtil.formatRemoteAddress(channel),
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
                  channel.eventLoop().execute(() -> {
                     if (this.authState == HandshakeHandler.AuthState.EXCHANGING_SERVER_TOKEN) {
                        this.disconnect("Server authentication failed - please try again later");
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
            if (ownerUuid != null && ownerUuid.equals(this.playerUuid)) {
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
      this.auth = new PlayerAuthentication(this.playerUuid, this.authenticatedUsername);
      if (this.referralData != null) {
         this.auth.setReferralData(this.referralData);
      }

      if (this.referralSource != null) {
         this.auth.setReferralSource(this.referralSource);
      }

      this.authState = HandshakeHandler.AuthState.AUTHENTICATED;
      this.clearTimeout();
      LOGGER.at(Level.INFO)
         .log(
            "Mutual authentication complete for %s (%s) from %s", this.authenticatedUsername, this.playerUuid, NettyUtil.formatRemoteAddress(this.getChannel())
         );
      this.onAuthenticated(passwordChallenge);
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
