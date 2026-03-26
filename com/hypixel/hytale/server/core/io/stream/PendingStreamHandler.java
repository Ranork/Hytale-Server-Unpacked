package com.hypixel.hytale.server.core.io.stream;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.packets.stream.StreamOpen;
import com.hypixel.hytale.protocol.packets.stream.StreamOpenResponse;
import com.hypixel.hytale.protocol.packets.stream.StreamType;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.io.netty.NettyUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.quic.QuicStreamChannel;
import io.netty.util.ReferenceCountUtil;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class PendingStreamHandler extends ChannelInboundHandlerAdapter {
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   private static final int MAX_AUXILIARY_STREAMS = 4;
   private final PacketHandler packetHandler;
   private final StreamManager streamManager;

   public PendingStreamHandler(@Nonnull PacketHandler packetHandler) {
      this(packetHandler, StreamManager.getInstance());
   }

   public PendingStreamHandler(@Nonnull PacketHandler packetHandler, @Nonnull StreamManager streamManager) {
      this.packetHandler = packetHandler;
      this.streamManager = streamManager;
   }

   public void channelRead(@Nonnull ChannelHandlerContext ctx, @Nonnull Object msg) {
      if (msg instanceof Packet packet) {
         if (packet instanceof StreamOpen open) {
            StreamType type = open.type;
            if (this.packetHandler.checkStreamOpenRateLimit()) {
               LOGGER.at(Level.WARNING).log("Stream open rate limited for %s requesting %s", this.packetHandler.getIdentifier(), type.name());
               ctx.writeAndFlush(new StreamOpenResponse(type, false, "Rate limited - try again later")).addListener(future -> ctx.close());
            } else if (type == StreamType.Game) {
               LOGGER.at(Level.WARNING).log("Cannot open Game stream - stream 0 is already the game stream, from %s", this.packetHandler.getIdentifier());
               ctx.writeAndFlush(new StreamOpenResponse(type, false, "Game stream cannot be opened explicitly")).addListener(future -> ctx.close());
            } else if (!this.streamManager.isSupported(type)) {
               LOGGER.at(Level.INFO).log("Unsupported stream type %s from %s", type.name(), this.packetHandler.getIdentifier());
               ctx.writeAndFlush(new StreamOpenResponse(type, false, "Stream type not supported")).addListener(future -> ctx.close());
            } else {
               Channel existingChannel = this.packetHandler.getChannel(type);
               if (existingChannel != null) {
                  LOGGER.at(Level.INFO)
                     .log(
                        "Replacing stale %s stream for %s (old channel active=%s)", type.name(), this.packetHandler.getIdentifier(), existingChannel.isActive()
                     );
                  this.packetHandler.setChannel(type, null);
                  existingChannel.close();
               }

               if (this.packetHandler.getAuxiliaryChannelCount() >= 4) {
                  LOGGER.at(Level.WARNING).log("Maximum auxiliary streams exceeded for %s requesting %s", this.packetHandler.getIdentifier(), type.name());
                  ctx.writeAndFlush(new StreamOpenResponse(type, false, "Maximum auxiliary streams exceeded")).addListener(future -> ctx.close());
               } else {
                  ChannelHandler handler = this.streamManager.createHandler(type, this.packetHandler);
                  if (handler == null) {
                     LOGGER.at(Level.SEVERE).log("Failed to create handler for stream type %s from %s", type.name(), this.packetHandler.getIdentifier());
                     ctx.writeAndFlush(new StreamOpenResponse(type, false, "Internal error")).addListener(future -> ctx.close());
                  } else {
                     LOGGER.at(Level.INFO).log("Opening %s stream for %s", type.name(), this.packetHandler.getIdentifier());
                     ctx.pipeline().replace(this, type.name() + "Handler", handler);
                     ctx.pipeline().remove("aux_read_timeout");
                     this.packetHandler.setChannel(type, ctx.channel());
                     if (ctx.channel() instanceof QuicStreamChannel quicStreamChannel) {
                        quicStreamChannel.updatePriority(this.streamManager.getStreamPriority(type));
                     }

                     ctx.writeAndFlush(new StreamOpenResponse(type, true, null));
                  }
               }
            }
         } else {
            LOGGER.at(Level.WARNING).log("Auxiliary stream first packet was not StreamOpen, closing: %s", packet.getClass().getSimpleName());
            ctx.close();
         }
      } else {
         LOGGER.at(Level.WARNING)
            .log("Expected Packet but got %s on pending stream from %s", msg.getClass().getSimpleName(), NettyUtil.formatRemoteAddress(ctx.channel()));
         ReferenceCountUtil.release(msg);
         ctx.close();
      }
   }

   public void exceptionCaught(@Nonnull ChannelHandlerContext ctx, @Nonnull Throwable cause) {
      if (cause instanceof ProtocolException) {
         LOGGER.at(Level.WARNING).log("Protocol error on pending stream for %s: %s", this.packetHandler.getIdentifier(), cause.getMessage());
      } else {
         ((HytaleLogger.Api)LOGGER.at(Level.WARNING).withCause(cause)).log("Exception in pending stream handler for %s", this.packetHandler.getIdentifier());
      }

      ctx.close();
   }

   public void channelInactive(@Nonnull ChannelHandlerContext ctx) throws Exception {
      LOGGER.at(Level.FINE).log("Pending stream closed for %s", this.packetHandler.getIdentifier());
      super.channelInactive(ctx);
   }
}
