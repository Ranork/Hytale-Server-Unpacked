package com.hypixel.hytale.server.core.io.netty;

import com.hypixel.hytale.protocol.Packet;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.handler.codec.MessageToMessageEncoder;
import java.util.List;
import javax.annotation.Nonnull;

@Sharable
public class PacketArrayEncoder extends MessageToMessageEncoder<Packet[]> {
   protected void encode(ChannelHandlerContext ctx, @Nonnull Packet[] packets, @Nonnull List<Object> out) {
      for (Packet packet : packets) {
         if (packet != null) {
            out.add(packet);
         }
      }
   }
}
