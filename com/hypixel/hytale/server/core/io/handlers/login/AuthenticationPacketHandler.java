package com.hypixel.hytale.server.core.io.handlers.login;

import com.hypixel.hytale.protocol.HostAddress;
import com.hypixel.hytale.protocol.io.ChannelConnection;
import com.hypixel.hytale.protocol.io.ConnectionHandler;
import com.hypixel.hytale.protocol.packets.connection.ClientType;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.auth.PlayerAuthentication;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.io.ProtocolVersion;
import com.hypixel.hytale.server.core.universe.Universe;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AuthenticationPacketHandler extends HandshakeHandler {
   private final AuthenticationPacketHandler.AuthHandlerSupplier authHandlerSupplier;

   public AuthenticationPacketHandler(
      @Nonnull ChannelConnection channel,
      @Nonnull ProtocolVersion protocolVersion,
      @Nonnull String language,
      @Nonnull AuthenticationPacketHandler.AuthHandlerSupplier authHandlerSupplier,
      @Nonnull ClientType clientType,
      @Nonnull String identityToken,
      @Nullable byte[] referralData,
      @Nullable HostAddress referralSource
   ) {
      super(channel, protocolVersion, language, clientType, identityToken, referralData, referralSource);
      this.authHandlerSupplier = authHandlerSupplier;
   }

   @Nonnull
   @Override
   public String getIdentifier() {
      return "{Authenticating(" + this.getChannel().formatRemoteAddress() + "), authHandlerSupplier=" + this.authHandlerSupplier + "}";
   }

   @Override
   public void registered0(ConnectionHandler oldHandler) {
      int maxPlayers = HytaleServer.get().getConfig().getMaxPlayers();
      if (maxPlayers > 0 && Universe.get().getPlayerCount() >= maxPlayers) {
         this.disconnect(Message.translation("client.general.disconnect.serverFull"));
      } else {
         super.registered0(oldHandler);
      }
   }

   @Override
   protected void onAuthenticated(byte[] passwordChallenge) {
      this.getChannel().logConnectionTimings("Authenticated", Level.FINE);
      this.getChannel()
         .setChannelHandler(
            new PasswordPacketHandler(
               this.getChannel(),
               this.protocolVersion,
               this.language,
               this.auth,
               passwordChallenge,
               (ch, pv, lang, a) -> this.authHandlerSupplier.create(ch, pv, lang, a)
            )
         );
   }

   @FunctionalInterface
   public interface AuthHandlerSupplier {
      PacketHandler create(ChannelConnection var1, ProtocolVersion var2, String var3, PlayerAuthentication var4);
   }
}
