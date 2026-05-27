package com.hypixel.hytale.server.core.io.stream;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.ConnectionHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class StreamConnectionHandlerAdapter extends SimpleChannelInboundHandler<Packet> {
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   private final ConnectionHandler handler;

   public StreamConnectionHandlerAdapter(@Nonnull ConnectionHandler handler) {
      this.handler = handler;
   }

   public void handlerAdded(@Nonnull ChannelHandlerContext ctx) throws Exception {
      this.handler.registered(null);
      super.handlerAdded(ctx);
   }

   protected void channelRead0(@Nonnull ChannelHandlerContext ctx, @Nonnull Packet packet) {
      this.handler.handle((ToServerPacket)packet);
   }

   public void channelInactive(@Nonnull ChannelHandlerContext ctx) throws Exception {
      this.handler.closed(null);
      super.channelInactive(ctx);
   }

   public void exceptionCaught(@Nonnull ChannelHandlerContext ctx, @Nonnull Throwable cause) {
      ((HytaleLogger.Api)LOGGER.at(Level.WARNING).withCause(cause)).log("Exception in stream handler adapter");
      ctx.close();
   }
}
