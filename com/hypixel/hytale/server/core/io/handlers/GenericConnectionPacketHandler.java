package com.hypixel.hytale.server.core.io.handlers;

import com.hypixel.hytale.protocol.io.ChannelConnection;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.io.ProtocolVersion;
import javax.annotation.Nonnull;

public abstract class GenericConnectionPacketHandler extends PacketHandler {
   protected final String language;

   public GenericConnectionPacketHandler(@Nonnull ChannelConnection channel, @Nonnull ProtocolVersion protocolVersion, String language) {
      super(channel, protocolVersion);
      this.language = language;
   }
}
