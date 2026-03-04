package com.hypixel.hytale.protocol.io.netty;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.PacketRegistry;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.PacketStatsRecorder;
import com.hypixel.hytale.protocol.io.ProtocolException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.timeout.ReadTimeoutException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;

public class PacketDecoder extends ByteToMessageDecoder {
   private static final int LENGTH_PREFIX_SIZE = 4;
   private static final int PACKET_ID_SIZE = 4;
   private static final int MIN_FRAME_SIZE = 8;
   private static final long CHECK_INTERVAL_MS = 1000L;
   private volatile long lastPacketTimeNanos;
   private ScheduledFuture<?> timeoutCheckFuture;

   public void handlerAdded(@Nonnull ChannelHandlerContext ctx) throws Exception {
      if (ctx.channel().isActive()) {
         this.initialize(ctx);
      }

      super.handlerAdded(ctx);
   }

   public void channelActive(@Nonnull ChannelHandlerContext ctx) throws Exception {
      this.initialize(ctx);
      super.channelActive(ctx);
   }

   public void channelInactive(@Nonnull ChannelHandlerContext ctx) throws Exception {
      this.cancelTimeoutCheck();
      super.channelInactive(ctx);
   }

   private void initialize(@Nonnull ChannelHandlerContext ctx) {
      if (this.timeoutCheckFuture == null) {
         this.lastPacketTimeNanos = System.nanoTime();
         this.timeoutCheckFuture = ctx.executor().scheduleAtFixedRate(() -> this.checkTimeout(ctx), 1000L, 1000L, TimeUnit.MILLISECONDS);
      }
   }

   private void cancelTimeoutCheck() {
      if (this.timeoutCheckFuture != null) {
         this.timeoutCheckFuture.cancel(false);
         this.timeoutCheckFuture = null;
      }
   }

   private void checkTimeout(@Nonnull ChannelHandlerContext ctx) {
      if (!ctx.channel().isActive()) {
         this.cancelTimeoutCheck();
      } else {
         Duration timeout = (Duration)ctx.channel().attr(ProtocolUtil.PACKET_TIMEOUT_KEY).get();
         if (timeout != null) {
            long elapsedNanos = System.nanoTime() - this.lastPacketTimeNanos;
            if (elapsedNanos >= timeout.toNanos()) {
               this.cancelTimeoutCheck();
               ctx.fireExceptionCaught(ReadTimeoutException.INSTANCE);
               ctx.close();
            }
         }
      }
   }

   protected void decode(@Nonnull ChannelHandlerContext ctx, @Nonnull ByteBuf in, @Nonnull List<Object> out) {
      if (in.readableBytes() >= 8) {
         in.markReaderIndex();
         int payloadLength = in.readIntLE();
         if (payloadLength >= 0 && payloadLength <= 1677721600) {
            int packetId = in.readIntLE();
            PacketRegistry.PacketInfo packetInfo = PacketRegistry.getToServerPacketById(packetId);
            if (packetInfo == null) {
               in.skipBytes(in.readableBytes());
               ProtocolUtil.closeConnection(ctx.channel());
            } else if (payloadLength > packetInfo.maxSize()) {
               in.skipBytes(in.readableBytes());
               ProtocolUtil.closeConnection(ctx.channel());
            } else {
               NetworkChannel channelVal = (NetworkChannel)ctx.channel().attr(ProtocolUtil.STREAM_CHANNEL_KEY).get();
               if (channelVal != null && channelVal != packetInfo.channel()) {
                  in.skipBytes(in.readableBytes());
                  ProtocolUtil.closeConnection(ctx.channel());
               } else if (in.readableBytes() < payloadLength) {
                  in.resetReaderIndex();
               } else {
                  PacketStatsRecorder statsRecorder = (PacketStatsRecorder)ctx.channel().attr(PacketStatsRecorder.CHANNEL_KEY).get();
                  if (statsRecorder == null) {
                     statsRecorder = PacketStatsRecorder.NOOP;
                  }

                  try {
                     out.add(PacketIO.readFramedPacketWithInfo(in, payloadLength, packetInfo, statsRecorder));
                     this.lastPacketTimeNanos = System.nanoTime();
                  } catch (ProtocolException var10) {
                     in.skipBytes(in.readableBytes());
                     ProtocolUtil.closeConnection(ctx.channel());
                  } catch (IndexOutOfBoundsException var11) {
                     in.skipBytes(in.readableBytes());
                     ProtocolUtil.closeConnection(ctx.channel());
                  }
               }
            }
         } else {
            in.skipBytes(in.readableBytes());
            ProtocolUtil.closeConnection(ctx.channel());
         }
      }
   }
}
