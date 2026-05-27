package com.hypixel.hytale.server.core.io.handlers.login;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.ChannelConnection;
import com.hypixel.hytale.protocol.io.ConnectionHandler;
import com.hypixel.hytale.protocol.packets.auth.PasswordAccepted;
import com.hypixel.hytale.protocol.packets.auth.PasswordRejected;
import com.hypixel.hytale.protocol.packets.auth.PasswordResponse;
import com.hypixel.hytale.protocol.packets.connection.ClientDisconnect;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.HytaleServerConfig;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.auth.PlayerAuthentication;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.io.ProtocolVersion;
import com.hypixel.hytale.server.core.io.handlers.GenericConnectionPacketHandler;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PasswordPacketHandler extends GenericConnectionPacketHandler {
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   private static final int MAX_PASSWORD_ATTEMPTS = 3;
   private static final int CHALLENGE_LENGTH = 32;
   @Nonnull
   private final PlayerAuthentication pendingAuth;
   private byte[] passwordChallenge;
   private final PasswordPacketHandler.SetupHandlerSupplier setupHandlerSupplier;
   private int attemptsRemaining = 3;

   public PasswordPacketHandler(
      @Nonnull ChannelConnection channel,
      @Nonnull ProtocolVersion protocolVersion,
      @Nonnull String language,
      @Nonnull PlayerAuthentication pendingAuth,
      @Nullable byte[] passwordChallenge,
      @Nonnull PasswordPacketHandler.SetupHandlerSupplier setupHandlerSupplier
   ) {
      super(channel, protocolVersion, language);
      this.pendingAuth = pendingAuth;
      this.passwordChallenge = passwordChallenge;
      this.setupHandlerSupplier = setupHandlerSupplier;
   }

   @Nonnull
   @Override
   public String getIdentifier() {
      return "{Password(" + this.getChannel().formatRemoteAddress() + "), " + this.pendingAuth.getUsername() + "}";
   }

   @Override
   public void registered0(ConnectionHandler oldHandler) {
      HytaleServerConfig.TimeoutProfile timeouts = HytaleServer.get().getConfig().getConnectionTimeouts();
      if (this.passwordChallenge != null && this.passwordChallenge.length != 0) {
         LOGGER.at(Level.FINE).log("Waiting for password response from %s", this.pendingAuth.getUsername());
         this.enterStage("password", timeouts.getPassword(), () -> !this.registered);
      } else {
         LOGGER.at(Level.FINE).log("No password required for %s, proceeding to setup", this.pendingAuth.getUsername());
         this.proceedToSetup();
      }
   }

   @Override
   public void accept(@Nonnull ToServerPacket packet) {
      switch (packet.getId()) {
         case 1:
            this.handle((ClientDisconnect)packet);
            break;
         case 15:
            this.handle((PasswordResponse)packet);
            break;
         default:
            this.disconnect(Message.translation("client.general.disconnect.protocol.unexpectedPacket").param("packetId", packet.getId()));
      }
   }

   public void handle(@Nonnull ClientDisconnect packet) {
      this.disconnectReason.setClientDisconnectType(packet.type);
      LOGGER.at(Level.INFO)
         .log(
            "%s (%s) at %s left with reason: %s - %s",
            this.pendingAuth.getUuid(),
            this.pendingAuth.getUsername(),
            this.getChannel().formatRemoteAddress(),
            packet.type.name(),
            packet.reason.name()
         );
      this.getChannel().closeApplicationConnection();
   }

   public void handle(@Nonnull PasswordResponse packet) {
      this.clearTimeout();
      if (this.passwordChallenge != null && this.passwordChallenge.length != 0) {
         byte[] clientHash = packet.hash;
         if (clientHash != null && clientHash.length != 0) {
            String password = HytaleServer.get().getConfig().getPassword();
            if (password != null && !password.isEmpty()) {
               byte[] expectedHash = computePasswordHash(this.passwordChallenge, password);
               if (expectedHash == null) {
                  LOGGER.at(Level.SEVERE).log("Failed to compute password hash");
                  this.disconnect(Message.translation("client.general.disconnect.serverError"));
               } else if (!MessageDigest.isEqual(expectedHash, clientHash)) {
                  this.attemptsRemaining--;
                  LOGGER.at(Level.WARNING)
                     .log(
                        "Invalid password from %s (%s), %d attempts remaining",
                        this.pendingAuth.getUsername(),
                        this.getChannel().formatRemoteAddress(),
                        this.attemptsRemaining
                     );
                  if (this.attemptsRemaining <= 0) {
                     this.disconnect(Message.translation("client.general.disconnect.tooManyPasswordAttempts"));
                  } else {
                     this.passwordChallenge = generateChallenge();
                     this.write(new PasswordRejected(this.passwordChallenge, this.attemptsRemaining));
                     HytaleServerConfig.TimeoutProfile timeouts = HytaleServer.get().getConfig().getConnectionTimeouts();
                     this.continueStage("password", timeouts.getPassword(), () -> !this.registered);
                  }
               } else {
                  LOGGER.at(Level.INFO).log("Password accepted for %s (%s)", this.pendingAuth.getUsername(), this.pendingAuth.getUuid());
                  this.write(new PasswordAccepted());
                  this.proceedToSetup();
               }
            } else {
               LOGGER.at(Level.SEVERE).log("Password validation failed - no password configured but challenge was sent");
               this.disconnect(Message.translation("client.general.disconnect.serverConfigError"));
            }
         } else {
            LOGGER.at(Level.WARNING).log("Received empty password hash from %s", this.getChannel().formatRemoteAddress());
            this.disconnect(Message.translation("client.general.disconnect.invalidPasswordResponse"));
         }
      } else {
         LOGGER.at(Level.WARNING).log("Received unexpected PasswordResponse from %s - no password required", this.getChannel().formatRemoteAddress());
         this.disconnect(Message.translation("client.general.disconnect.protocol.unexpectedPasswordResponse"));
      }
   }

   private static byte[] generateChallenge() {
      byte[] challenge = new byte[32];
      new SecureRandom().nextBytes(challenge);
      return challenge;
   }

   private void proceedToSetup() {
      this.auth = this.pendingAuth;
      LOGGER.at(Level.INFO)
         .log("Connection complete for %s (%s) (SNI: %s), transitioning to setup", this.auth.getUsername(), this.auth.getUuid(), this.getSniHostname());
      this.getChannel().setChannelHandler(this.setupHandlerSupplier.create(this.getChannel(), this.protocolVersion, this.language, this.auth));
   }

   @Nullable
   private static byte[] computePasswordHash(byte[] challenge, String password) {
      try {
         MessageDigest digest = MessageDigest.getInstance("SHA-256");
         digest.update(challenge);
         digest.update(password.getBytes(StandardCharsets.UTF_8));
         return digest.digest();
      } catch (NoSuchAlgorithmException var3) {
         return null;
      }
   }

   @FunctionalInterface
   public interface SetupHandlerSupplier {
      PacketHandler create(ChannelConnection var1, ProtocolVersion var2, String var3, PlayerAuthentication var4);
   }
}
