package com.hypixel.hytale.server.core.io.handlers.login;

import com.hypixel.hytale.protocol.HostAddress;
import com.hypixel.hytale.protocol.packets.connection.ClientType;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.auth.PlayerAuthentication;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.io.ProtocolVersion;
import com.hypixel.hytale.server.core.io.netty.NettyUtil;
import com.hypixel.hytale.server.core.universe.Universe;
import io.netty.channel.Channel;
import java.util.UUID;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AuthenticationPacketHandler extends HandshakeHandler {
   private final AuthenticationPacketHandler.AuthHandlerSupplier authHandlerSupplier;

   public AuthenticationPacketHandler(
      @Nonnull Channel channel,
      @Nonnull ProtocolVersion protocolVersion,
      @Nonnull String language,
      @Nonnull AuthenticationPacketHandler.AuthHandlerSupplier authHandlerSupplier,
      @Nonnull ClientType clientType,
      @Nonnull String identityToken,
      @Nonnull UUID uuid,
      @Nonnull String username,
      @Nullable byte[] referralData,
      @Nullable HostAddress referralSource
   ) {
      super(channel, protocolVersion, language, clientType, identityToken, uuid, username, referralData, referralSource);
      this.authHandlerSupplier = authHandlerSupplier;
   }

   @Nonnull
   @Override
   public String getIdentifier() {
      return "{Authenticating(" + NettyUtil.formatRemoteAddress(this.getChannel()) + "), authHandlerSupplier=" + this.authHandlerSupplier + "}";
   }

   @Override
   public void registered0(PacketHandler oldHandler) {
      int maxPlayers = HytaleServer.get().getConfig().getMaxPlayers();
      if (maxPlayers > 0 && Universe.get().getPlayerCount() >= maxPlayers) {
         this.disconnect("Too many players!");
      } else {
         super.registered0(oldHandler);
      }
   }

   @Override
   protected void onAuthenticated(byte[] passwordChallenge) {
      PacketHandler.logConnectionTimings(this.getChannel(), "Authenticated", Level.FINE);
      NettyUtil.setChannelHandler(
         this.getChannel(),
         new PasswordPacketHandler(
            this.getChannel(),
            this.protocolVersion,
            this.language,
            this.auth.getUuid(),
            this.auth.getUsername(),
            this.auth.getReferralData(),
            this.auth.getReferralSource(),
            passwordChallenge,
            (ch, pv, lang, a) -> this.authHandlerSupplier.create(ch, pv, lang, a)
         )
      );
   }

   @FunctionalInterface
   public interface AuthHandlerSupplier {
      PacketHandler create(Channel var1, ProtocolVersion var2, String var3, PlayerAuthentication var4);
   }
}
