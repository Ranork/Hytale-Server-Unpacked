package com.hypixel.hytale.server.core.io.handlers;

import com.hypixel.hytale.common.util.java.ManifestUtil;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.FormattedMessage;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.netty.ProtocolUtil;
import com.hypixel.hytale.protocol.packets.auth.ConnectAccept;
import com.hypixel.hytale.protocol.packets.connection.ClientDisconnect;
import com.hypixel.hytale.protocol.packets.connection.ClientType;
import com.hypixel.hytale.protocol.packets.connection.Connect;
import com.hypixel.hytale.protocol.packets.connection.QuicApplicationErrorCode;
import com.hypixel.hytale.server.core.Constants;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.HytaleServerConfig;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.Options;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.io.ProtocolVersion;
import com.hypixel.hytale.server.core.io.handlers.login.AuthenticationPacketHandler;
import com.hypixel.hytale.server.core.io.handlers.login.PasswordPacketHandler;
import com.hypixel.hytale.server.core.io.netty.NettyUtil;
import com.hypixel.hytale.server.core.modules.singleplayer.SingleplayerModule;
import io.netty.channel.Channel;
import io.netty.handler.codec.quic.QuicStreamChannel;
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

   public InitialPacketHandler(@Nonnull Channel channel) {
      super(channel, null);
   }

   @Nonnull
   @Override
   public String getIdentifier() {
      return "{Initial(" + NettyUtil.formatRemoteAddress(this.getChannel()) + ")}";
   }

   @Override
   public void registered0(PacketHandler oldHandler) {
      HytaleServerConfig.TimeoutProfile timeouts = HytaleServer.get().getConfig().getConnectionTimeouts();
      this.initStage("initial", timeouts.getInitial(), () -> !this.registered);
      PacketHandler.logConnectionTimings(this.getChannel(), "Registered", Level.FINE);
   }

   @Override
   public void accept(@Nonnull ToServerPacket packet) {
      if (packet.getId() == 0) {
         this.handle((Connect)packet);
      } else if (packet.getId() == 1) {
         this.handle((ClientDisconnect)packet);
      } else {
         this.disconnect(Message.translation("client.general.disconnect.protocol.unexpectedPacket").param("packetId", packet.getId()));
      }
   }

   @Override
   public void disconnect(@Nonnull FormattedMessage message) {
      if (this.receivedConnect) {
         super.disconnect(message);
      } else {
         HytaleLogger.getLogger().at(Level.INFO).log("Silently disconnecting %s because no Connect packet!", NettyUtil.formatRemoteAddress(this.getChannel()));
         ProtocolUtil.closeConnection(this.getChannel());
      }
   }

   public void handle(@Nonnull Connect packet) {
      this.receivedConnect = true;
      this.clearTimeout();
      PacketHandler.logConnectionTimings(this.getChannel(), "Connect", Level.FINE);
      if (packet.protocolCrc != 1080406952) {
         int clientBuild = packet.protocolBuildNumber;
         int serverBuild = 51;
         QuicApplicationErrorCode errorCode = clientBuild < serverBuild ? QuicApplicationErrorCode.ClientOutdated : QuicApplicationErrorCode.ServerOutdated;
         String serverVersion = ManifestUtil.getImplementationVersion();
         ProtocolUtil.closeApplicationConnection(this.getChannel(), errorCode, serverVersion != null ? serverVersion : "unknown");
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

         boolean isTcpConnection = !(this.getChannel() instanceof QuicStreamChannel);
         if (isTcpConnection) {
            HytaleLogger.getLogger()
               .at(Level.INFO)
               .log("TCP connection from %s - only insecure auth supported", NettyUtil.formatRemoteAddress(this.getChannel()));
         }

         if (packet.uuid == null) {
            this.disconnect(Message.translation("client.general.disconnect.missingUuid"));
         } else if (packet.username != null && !packet.username.isEmpty()) {
            if (packet.referralData != null && packet.referralData.length > 4096) {
               HytaleLogger.getLogger()
                  .at(Level.WARNING)
                  .log("Rejecting connection from %s - referral data too large: %d bytes (max: %d)", packet.username, packet.referralData.length, 4096);
               this.disconnect(Message.translation("client.general.disconnect.referralDataTooLarge").param("maxSize", 4096));
            } else {
               if (packet.referralData != null) {
                  if (packet.referralSource == null) {
                     HytaleLogger.getLogger()
                        .at(Level.WARNING)
                        .log("Rejecting connection from %s - referral data provided without source address", packet.username);
                     this.disconnect(Message.translation("client.general.disconnect.referralMissingSource"));
                     return;
                  }

                  if (packet.referralSource.host == null || packet.referralSource.host.isEmpty()) {
                     HytaleLogger.getLogger().at(Level.WARNING).log("Rejecting connection from %s - referral source has empty host", packet.username);
                     this.disconnect(Message.translation("client.general.disconnect.referralInvalidSource"));
                     return;
                  }
               }

               boolean hasIdentityToken = packet.identityToken != null && !packet.identityToken.isEmpty();
               boolean isEditorClient = packet.clientType == ClientType.Editor;
               Options.AuthMode authMode = (Options.AuthMode)Options.getOptionSet().valueOf(Options.AUTH_MODE);
               if (hasIdentityToken && authMode == Options.AuthMode.AUTHENTICATED) {
                  if (isTcpConnection) {
                     HytaleLogger.getLogger()
                        .at(Level.WARNING)
                        .log("Rejecting authenticated connection from %s - TCP only supports insecure auth", NettyUtil.formatRemoteAddress(this.getChannel()));
                     this.disconnect(Message.translation("client.general.disconnect.tcpAuthenticationNotSupported"));
                     return;
                  }

                  AuthenticationPacketHandler.AuthHandlerSupplier supplier = isEditorClient ? EDITOR_PACKET_HANDLER_SUPPLIER : SetupPacketHandler::new;
                  if (isEditorClient && supplier == null) {
                     this.disconnect(Message.translation("client.general.disconnect.editorNotSupported"));
                     return;
                  }

                  HytaleLogger.getLogger()
                     .at(Level.INFO)
                     .log("Starting authenticated flow for %s (%s) from %s", packet.username, packet.uuid, NettyUtil.formatRemoteAddress(this.getChannel()));
                  NettyUtil.setChannelHandler(
                     this.getChannel(),
                     new AuthenticationPacketHandler(
                        this.getChannel(),
                        protocolVersion,
                        language,
                        supplier,
                        packet.clientType,
                        packet.identityToken,
                        packet.uuid,
                        packet.username,
                        packet.referralData,
                        packet.referralSource
                     )
                  );
               } else {
                  if (authMode == Options.AuthMode.AUTHENTICATED) {
                     HytaleLogger.getLogger()
                        .at(Level.WARNING)
                        .log(
                           "Rejecting development connection from %s - server requires authentication (auth-mode=%s)",
                           NettyUtil.formatRemoteAddress(this.getChannel()),
                           authMode
                        );
                     this.disconnect(Message.translation("client.general.disconnect.serverRequiresAuthentication"));
                     return;
                  }

                  if (authMode == Options.AuthMode.OFFLINE) {
                     if (!Constants.SINGLEPLAYER) {
                        HytaleLogger.getLogger()
                           .at(Level.WARNING)
                           .log("Rejecting connection from %s - offline mode is only valid in singleplayer", NettyUtil.formatRemoteAddress(this.getChannel()));
                        this.disconnect(Message.translation("client.general.disconnect.offlineModeSingleplayerOnly"));
                        return;
                     }

                     if (!SingleplayerModule.isOwner(null, packet.uuid)) {
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
                     .log("Starting development flow for %s (%s) from %s", packet.username, packet.uuid, NettyUtil.formatRemoteAddress(this.getChannel()));
                  byte[] passwordChallenge = this.generatePasswordChallengeIfNeeded(packet.uuid);
                  this.write(new ConnectAccept(passwordChallenge));
                  PasswordPacketHandler.SetupHandlerSupplier setupSupplier = isEditorClient && EDITOR_PACKET_HANDLER_SUPPLIER != null
                     ? (ch, pv, lang, auth) -> EDITOR_PACKET_HANDLER_SUPPLIER.create(ch, pv, lang, auth)
                     : SetupPacketHandler::new;
                  NettyUtil.setChannelHandler(
                     this.getChannel(),
                     new PasswordPacketHandler(
                        this.getChannel(),
                        protocolVersion,
                        language,
                        packet.uuid,
                        packet.username,
                        packet.referralData,
                        packet.referralSource,
                        passwordChallenge,
                        setupSupplier
                     )
                  );
               }
            }
         } else {
            this.disconnect(Message.translation("client.general.disconnect.missingUsername"));
         }
      }
   }

   private byte[] generatePasswordChallengeIfNeeded(UUID playerUuid) {
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
      HytaleLogger.getLogger().at(Level.WARNING).log("Disconnecting %s - Sent disconnect packet???", NettyUtil.formatRemoteAddress(this.getChannel()));
      ProtocolUtil.closeApplicationConnection(this.getChannel());
   }
}
