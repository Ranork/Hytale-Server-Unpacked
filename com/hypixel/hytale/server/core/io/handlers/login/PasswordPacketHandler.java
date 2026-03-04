package com.hypixel.hytale.server.core.io.handlers.login;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.HostAddress;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.netty.ProtocolUtil;
import com.hypixel.hytale.protocol.packets.auth.PasswordAccepted;
import com.hypixel.hytale.protocol.packets.auth.PasswordRejected;
import com.hypixel.hytale.protocol.packets.auth.PasswordResponse;
import com.hypixel.hytale.protocol.packets.connection.Disconnect;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.HytaleServerConfig;
import com.hypixel.hytale.server.core.auth.PlayerAuthentication;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.io.ProtocolVersion;
import com.hypixel.hytale.server.core.io.handlers.GenericConnectionPacketHandler;
import com.hypixel.hytale.server.core.io.netty.NettyUtil;
import io.netty.channel.Channel;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.UUID;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PasswordPacketHandler extends GenericConnectionPacketHandler {
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   private static final int MAX_PASSWORD_ATTEMPTS = 3;
   private static final int CHALLENGE_LENGTH = 32;
   private final UUID playerUuid;
   private final String username;
   private final byte[] referralData;
   private final HostAddress referralSource;
   private byte[] passwordChallenge;
   private final PasswordPacketHandler.SetupHandlerSupplier setupHandlerSupplier;
   private int attemptsRemaining = 3;

   public PasswordPacketHandler(
      @Nonnull Channel channel,
      @Nonnull ProtocolVersion protocolVersion,
      @Nonnull String language,
      @Nonnull UUID playerUuid,
      @Nonnull String username,
      @Nullable byte[] referralData,
      @Nullable HostAddress referralSource,
      @Nullable byte[] passwordChallenge,
      @Nonnull PasswordPacketHandler.SetupHandlerSupplier setupHandlerSupplier
   ) {
      super(channel, protocolVersion, language);
      this.playerUuid = playerUuid;
      this.username = username;
      this.referralData = referralData;
      this.referralSource = referralSource;
      this.passwordChallenge = passwordChallenge;
      this.setupHandlerSupplier = setupHandlerSupplier;
   }

   @Nonnull
   @Override
   public String getIdentifier() {
      return "{Password(" + NettyUtil.formatRemoteAddress(this.getChannel()) + "), " + this.username + "}";
   }

   @Override
   public void registered0(PacketHandler oldHandler) {
      HytaleServerConfig.TimeoutProfile timeouts = HytaleServer.get().getConfig().getConnectionTimeouts();
      if (this.passwordChallenge != null && this.passwordChallenge.length != 0) {
         LOGGER.at(Level.FINE).log("Waiting for password response from %s", this.username);
         this.enterStage("password", timeouts.getPassword(), () -> !this.registered);
      } else {
         LOGGER.at(Level.FINE).log("No password required for %s, proceeding to setup", this.username);
         this.proceedToSetup();
      }
   }

   @Override
   public void accept(@Nonnull ToServerPacket packet) {
      switch (packet.getId()) {
         case 1:
            this.handle((Disconnect)packet);
            break;
         case 15:
            this.handle((PasswordResponse)packet);
            break;
         default:
            this.disconnect("Protocol error: unexpected packet " + packet.getId());
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
                  this.disconnect("Server error");
               } else if (!MessageDigest.isEqual(expectedHash, clientHash)) {
                  this.attemptsRemaining--;
                  LOGGER.at(Level.WARNING)
                     .log(
                        "Invalid password from %s (%s), %d attempts remaining",
                        this.username,
                        NettyUtil.formatRemoteAddress(this.getChannel()),
                        this.attemptsRemaining
                     );
                  if (this.attemptsRemaining <= 0) {
                     this.disconnect("Too many failed password attempts");
                  } else {
                     this.passwordChallenge = generateChallenge();
                     this.write(new PasswordRejected(this.passwordChallenge, this.attemptsRemaining));
                     HytaleServerConfig.TimeoutProfile timeouts = HytaleServer.get().getConfig().getConnectionTimeouts();
                     this.continueStage("password", timeouts.getPassword(), () -> !this.registered);
                  }
               } else {
                  LOGGER.at(Level.INFO).log("Password accepted for %s (%s)", this.username, this.playerUuid);
                  this.write(new PasswordAccepted());
                  this.proceedToSetup();
               }
            } else {
               LOGGER.at(Level.SEVERE).log("Password validation failed - no password configured but challenge was sent");
               this.disconnect("Server configuration error");
            }
         } else {
            LOGGER.at(Level.WARNING).log("Received empty password hash from %s", NettyUtil.formatRemoteAddress(this.getChannel()));
            this.disconnect("Invalid password response");
         }
      } else {
         LOGGER.at(Level.WARNING).log("Received unexpected PasswordResponse from %s - no password required", NettyUtil.formatRemoteAddress(this.getChannel()));
         this.disconnect("Protocol error: unexpected PasswordResponse");
      }
   }

   private static byte[] generateChallenge() {
      byte[] challenge = new byte[32];
      new SecureRandom().nextBytes(challenge);
      return challenge;
   }

   private void proceedToSetup() {
      this.auth = new PlayerAuthentication(this.playerUuid, this.username);
      if (this.referralData != null) {
         this.auth.setReferralData(this.referralData);
      }

      if (this.referralSource != null) {
         this.auth.setReferralSource(this.referralSource);
      }

      LOGGER.at(Level.INFO).log("Connection complete for %s (%s) (SNI: %s), transitioning to setup", this.username, this.playerUuid, this.getSniHostname());
      NettyUtil.setChannelHandler(this.getChannel(), this.setupHandlerSupplier.create(this.getChannel(), this.protocolVersion, this.language, this.auth));
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
      PacketHandler create(Channel var1, ProtocolVersion var2, String var3, PlayerAuthentication var4);
   }
}
