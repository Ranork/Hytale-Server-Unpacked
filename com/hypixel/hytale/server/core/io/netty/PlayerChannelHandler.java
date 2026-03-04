package com.hypixel.hytale.server.core.io.netty;

import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.io.adapter.PacketAdapters;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class PlayerChannelHandler extends ChannelInboundHandlerAdapter {
   private final PacketHandler handler;

   public PlayerChannelHandler(PacketHandler handler) {
      this.handler = handler;
   }

   public PacketHandler getHandler() {
      return this.handler;
   }

   public void channelInactive(ChannelHandlerContext ctx) {
      this.handler.logCloseMessage();
      this.handler.closed(ctx);
   }

   public void channelRead(ChannelHandlerContext ctx, Object msg) {
      if (ctx.channel().isActive()) {
         ToServerPacket packet = (ToServerPacket)msg;
         if (!PacketAdapters.__handleInbound(this.handler, packet)) {
            this.handler.handle(packet);
         }
      }
   }
}
