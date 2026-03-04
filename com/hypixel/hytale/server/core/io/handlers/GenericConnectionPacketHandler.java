package com.hypixel.hytale.server.core.io.handlers;

import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.io.ProtocolVersion;
import io.netty.channel.Channel;
import javax.annotation.Nonnull;

public abstract class GenericConnectionPacketHandler extends PacketHandler {
   protected final String language;

   public GenericConnectionPacketHandler(@Nonnull Channel channel, @Nonnull ProtocolVersion protocolVersion, String language) {
      super(channel, protocolVersion);
      this.language = language;
   }
}
