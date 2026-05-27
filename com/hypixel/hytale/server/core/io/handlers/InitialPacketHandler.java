package com.hypixel.hytale.server.core.io.handlers;

import com.hypixel.hytale.common.util.java.ManifestUtil;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.FormattedMessage;
import com.hypixel.hytale.protocol.HostAddress;
import com.hypixel.hytale.protocol.PlayerSkin;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.ChannelConnection;
import com.hypixel.hytale.protocol.io.ConnectionHandler;
import com.hypixel.hytale.protocol.packets.auth.ConnectAccept;
import com.hypixel.hytale.protocol.packets.connection.ClientDisconnect;
import com.hypixel.hytale.protocol.packets.connection.ClientType;
import com.hypixel.hytale.protocol.packets.connection.Connect;
import com.hypixel.hytale.protocol.packets.connection.InsecurePlayerOptions;
import com.hypixel.hytale.protocol.packets.connection.QuicApplicationErrorCode;
import com.hypixel.hytale.protocol.packets.connection.RequestInsecurePlayerOptions;
import com.hypixel.hytale.server.core.Constants;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.HytaleServerConfig;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.Options;
import com.hypixel.hytale.server.core.auth.PlayerAuthentication;
import com.hypixel.hytale.server.core.cosmetics.CosmeticsModule;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.io.ProtocolVersion;
import com.hypixel.hytale.server.core.io.handlers.login.AuthenticationPacketHandler;
import com.hypixel.hytale.server.core.io.handlers.login.PasswordPacketHandler;
import com.hypixel.hytale.server.core.modules.singleplayer.SingleplayerModule;
import java.security.SecureRandom;
import java.util.UUID;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class InitialPacketHandler extends PacketHandler {
   private static final int MAX_REFERRAL_DATA_SIZE = 4096;
   @Nullable
   public static AuthenticationPacketHandler.AuthHandlerSupplier EDITOR_PACKET_HANDLER_SUPPLIER;
   private boolean receivedConnect;
   private boolean receivedInsecureOptions;
   @Nullable
   private InitialPacketHandler.PendingConnectData connectData;

   public InitialPacketHandler(@Nonnull ChannelConnection channel) {
      super(channel, null);
   }

   @Nonnull
   @Override
   public String getIdentifier() {
      return "{Initial(" + this.getChannel().formatRemoteAddress() + ")}";
   }

   @Override
   public void registered0(ConnectionHandler oldHandler) {
      HytaleServerConfig.TimeoutProfile timeouts = HytaleServer.get().getConfig().getConnectionTimeouts();
      this.initStage("initial", timeouts.getInitial(), () -> !this.registered);
      this.getChannel().logConnectionTimings("Registered", Level.FINE);
   }

   @Override
   public void accept(@Nonnull ToServerPacket packet) {
      switch (packet.getId()) {
         case 0:
            this.handle((Connect)packet);
            break;
         case 1:
            this.handle((ClientDisconnect)packet);
            break;
         case 363:
            this.handle((InsecurePlayerOptions)packet);
            break;
         default:
            this.disconnect(Message.translation("client.general.disconnect.protocol.unexpectedPacket").param("packetId", packet.getId()));
      }
   }

   @Override
   public void disconnect(@Nonnull FormattedMessage message) {
      if (this.receivedConnect) {
         super.disconnect(message);
      } else {
         HytaleLogger.getLogger().at(Level.INFO).log("Silently disconnecting %s because no Connect packet!", this.getChannel().formatRemoteAddress());
         this.getChannel().closeConnection();
      }
   }

   public void handle(@Nonnull Connect packet) {
      if (this.receivedConnect) {
         this.disconnect(Message.translation("client.general.disconnect.protocol.unexpectedPacket"));
      } else {
         this.receivedConnect = true;
         this.clearTimeout();
         this.getChannel().logConnectionTimings("Connect", Level.FINE);
         if (packet.protocolCrc != 1316766548) {
            int clientBuild = packet.protocolBuildNumber;
            int serverBuild = 100;
            QuicApplicationErrorCode errorCode = clientBuild < serverBuild ? QuicApplicationErrorCode.ClientOutdated : QuicApplicationErrorCode.ServerOutdated;
            String serverVersion = ManifestUtil.getImplementationVersion();
            this.getChannel().closeApplicationConnection(errorCode, serverVersion != null ? serverVersion : "unknown");
         } else if (HytaleServer.get().isShuttingDown()) {
            this.disconnect(Message.translation("client.general.disconnect.serverShuttingDown"));
         } else if (!HytaleServer.get().isBooted()) {
            this.disconnect(Message.translation("client.general.disconnect.serverBooting"));
         } else {
            ProtocolVersion protocolVersion = new ProtocolVersion(packet.protocolCrc);
            String language = packet.language;
            if (language == null) {
               language = "en-US";
            }

            if (packet.referralData != null && packet.referralData.length > 4096) {
               HytaleLogger.getLogger()
                  .at(Level.WARNING)
                  .log(
                     "Rejecting connection from %s - referral data too large: %d bytes (max: %d)",
                     this.getChannel().formatRemoteAddress(),
                     packet.referralData.length,
                     4096
                  );
               this.disconnect(Message.translation("client.general.disconnect.referralDataTooLarge").param("maxSize", 4096));
            } else {
               if (packet.referralData != null) {
                  if (packet.referralSource == null) {
                     HytaleLogger.getLogger()
                        .at(Level.WARNING)
                        .log("Rejecting connection from %s - referral data provided without source address", this.getChannel().formatRemoteAddress());
                     this.disconnect(Message.translation("client.general.disconnect.referralMissingSource"));
                     return;
                  }

                  if (packet.referralSource.host == null || packet.referralSource.host.isEmpty()) {
                     HytaleLogger.getLogger()
                        .at(Level.WARNING)
                        .log("Rejecting connection from %s - referral source has empty host", this.getChannel().formatRemoteAddress());
                     this.disconnect(Message.translation("client.general.disconnect.referralInvalidSource"));
                     return;
                  }
               }

               boolean hasIdentityToken = packet.identityToken != null && !packet.identityToken.isEmpty();
               boolean isEditorClient = packet.clientType == ClientType.Editor;
               Options.AuthMode authMode = (Options.AuthMode)Options.getOptionSet().valueOf(Options.AUTH_MODE);
               if (authMode == Options.AuthMode.AUTHENTICATED && !hasIdentityToken) {
                  HytaleLogger.getLogger()
                     .at(Level.WARNING)
                     .log(
                        "Rejecting development connection from %s - server requires authentication (auth-mode=%s)",
                        this.getChannel().formatRemoteAddress(),
                        authMode
                     );
                  this.disconnect(Message.translation("client.general.disconnect.serverRequiresAuthentication"));
               } else {
                  if (authMode == Options.AuthMode.AUTHENTICATED) {
                     AuthenticationPacketHandler.AuthHandlerSupplier supplier = isEditorClient ? EDITOR_PACKET_HANDLER_SUPPLIER : SetupPacketHandler::new;
                     if (isEditorClient && supplier == null) {
                        this.disconnect(Message.translation("client.general.disconnect.editorNotSupported"));
                        return;
                     }

                     HytaleLogger.getLogger().at(Level.INFO).log("Starting authenticated flow from %s", this.getChannel().formatRemoteAddress());
                     this.getChannel()
                        .setChannelHandler(
                           new AuthenticationPacketHandler(
                              this.getChannel(),
                              protocolVersion,
                              language,
                              supplier,
                              packet.clientType,
                              packet.identityToken,
                              packet.referralData,
                              packet.referralSource
                           )
                        );
                  } else {
                     this.connectData = new InitialPacketHandler.PendingConnectData(
                        packet.clientType, language, packet.referralData, packet.referralSource, protocolVersion
                     );
                     this.write(new RequestInsecurePlayerOptions());
                     HytaleServerConfig.TimeoutProfile timeouts = HytaleServer.get().getConfig().getConnectionTimeouts();
                     this.continueStage("initial:insecure-options", timeouts.getInitial(), () -> this.receivedInsecureOptions);
                  }
               }
            }
         }
      }
   }

   public void handle(@Nonnull InsecurePlayerOptions packet) {
      if (this.receivedConnect && !this.receivedInsecureOptions && this.connectData != null) {
         this.receivedInsecureOptions = true;
         Options.AuthMode authMode = (Options.AuthMode)Options.getOptionSet().valueOf(Options.AUTH_MODE);
         if (authMode == Options.AuthMode.AUTHENTICATED) {
            this.disconnect(Message.translation("client.general.disconnect.protocol.unexpectedPacket"));
         } else if (packet.uuid == null) {
            this.disconnect(Message.translation("client.general.disconnect.missingUuid"));
         } else if (packet.username != null && !packet.username.isEmpty()) {
            PlayerSkin skin = packet.skin;
            if (skin != null) {
               try {
                  CosmeticsModule.get().validateSkin(skin);
               } catch (CosmeticsModule.InvalidSkinException var8) {
                  this.disconnect(Message.translation("client.general.disconnect.invalidSkin").param("details", var8.getMessage()));
                  return;
               }
            }

            if (authMode == Options.AuthMode.OFFLINE) {
               if (!Constants.SINGLEPLAYER) {
                  HytaleLogger.getLogger()
                     .at(Level.WARNING)
                     .log("Rejecting connection from %s - offline mode is only valid in singleplayer", this.getChannel().formatRemoteAddress());
                  this.disconnect(Message.translation("client.general.disconnect.offlineModeSingleplayerOnly"));
                  return;
               }

               if (!SingleplayerModule.isOwner(packet.uuid)) {
                  HytaleLogger.getLogger()
                     .at(Level.WARNING)
                     .log(
                        "Rejecting connection from %s (%s) - offline mode only allows the world owner (%s)",
                        packet.username,
                        packet.uuid,
                        SingleplayerModule.getUuid()
                     );
                  this.disconnect(Message.translation("client.general.disconnect.offlineModeOwnerOnly"));
                  return;
               }
            }

            HytaleLogger.getLogger()
               .at(Level.INFO)
               .log("Starting development flow for %s (%s) from %s", packet.username, packet.uuid, this.getChannel().formatRemoteAddress());
            byte[] passwordChallenge = generatePasswordChallengeIfNeeded(packet.uuid);
            this.write(new ConnectAccept(passwordChallenge));
            boolean isEditorClient = this.connectData.clientType == ClientType.Editor;
            PasswordPacketHandler.SetupHandlerSupplier setupSupplier = isEditorClient && EDITOR_PACKET_HANDLER_SUPPLIER != null
               ? (ch, pv, lang, auth) -> EDITOR_PACKET_HANDLER_SUPPLIER.create(ch, pv, lang, auth)
               : SetupPacketHandler::new;
            PlayerAuthentication pendingAuth = new PlayerAuthentication(packet.uuid, packet.username);
            if (this.connectData.referralData != null) {
               pendingAuth.setReferralData(this.connectData.referralData);
            }

            if (this.connectData.referralSource != null) {
               pendingAuth.setReferralSource(this.connectData.referralSource);
            }

            if (skin != null) {
               pendingAuth.setSkin(skin);
            }

            this.getChannel()
               .setChannelHandler(
                  new PasswordPacketHandler(
                     this.getChannel(), this.connectData.protocolVersion, this.connectData.language, pendingAuth, passwordChallenge, setupSupplier
                  )
               );
            this.connectData = null;
         } else {
            this.disconnect(Message.translation("client.general.disconnect.missingUsername"));
         }
      } else {
         this.disconnect(Message.translation("client.general.disconnect.protocol.unexpectedPacket"));
      }
   }

   private static byte[] generatePasswordChallengeIfNeeded(UUID playerUuid) {
      String password = HytaleServer.get().getConfig().getPassword();
      if (password != null && !password.isEmpty()) {
         if (Constants.SINGLEPLAYER) {
            UUID ownerUuid = SingleplayerModule.getUuid();
            if (ownerUuid != null && ownerUuid.equals(playerUuid)) {
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

   public void handle(@Nonnull ClientDisconnect packet) {
      this.disconnectReason.setClientDisconnectType(packet.type);
      HytaleLogger.getLogger().at(Level.WARNING).log("Disconnecting %s - Sent disconnect packet???", this.getChannel().formatRemoteAddress());
      this.getChannel().closeApplicationConnection();
   }

   private record PendingConnectData(
      @Nonnull ClientType clientType,
      @Nonnull String language,
      @Nullable byte[] referralData,
      @Nullable HostAddress referralSource,
      @Nonnull ProtocolVersion protocolVersion
   ) {
   }
}
