package com.hypixel.hytale.lib.quiche;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.CachedPacket;
import com.hypixel.hytale.protocol.FormattedMessage;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.PacketRegistry;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.ChannelConnection;
import com.hypixel.hytale.protocol.io.ConnectionHandler;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.PacketStatsRecorder;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ZstdNative;
import com.hypixel.hytale.protocol.packets.connection.DisconnectType;
import com.hypixel.hytale.protocol.packets.connection.QuicApplicationErrorCode;
import com.hypixel.hytale.protocol.packets.connection.ServerDisconnect;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.foreign.ValueLayout.OfInt;
import java.net.SocketAddress;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class QuicheChannel implements ChannelConnection {
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   private static final OfInt PACKET_SIZE_LAYOUT = ValueLayout.JAVA_INT_UNALIGNED.withOrder(ByteOrder.LITTLE_ENDIAN);
   private final QuicheConnection connection;
   private final long streamId;
   private final NetworkChannel networkChannel;
   boolean isNew = true;
   ConnectionHandler handler;
   private final PacketStatsRecorder packetStatsRecorder = PacketStatsRecorder.NOOP;
   private final ConcurrentLinkedQueue<ToClientPacket> packetQueue = new ConcurrentLinkedQueue<>();
   private final Arena arena = Arena.ofConfined();
   private final MemorySegment packetBuffer = this.arena.allocate(102400L);
   private boolean processingPacket = false;
   private int currentPacketOffset = 0;
   private int currentPacketSize = 0;
   @Nullable
   private Arena tempPacketArena = null;
   @Nullable
   private MemorySegment tempPacketBuffer = null;
   private final MemorySegment sendBuffer = this.arena.allocate(102400L);
   private final MemorySegment sendErrorCode = this.arena.allocate(QuicheNative.C_LONG);
   private int sendOffset = 0;
   private int sendLimit = 0;
   @Nullable
   private Arena tempSendArena = null;
   @Nullable
   private MemorySegment tempSendBuffer = null;
   private final AtomicBoolean writable = new AtomicBoolean(true);
   private final AtomicBoolean closed = new AtomicBoolean();

   QuicheChannel(QuicheConnection connection, long streamId, NetworkChannel networkChannel) {
      this.connection = connection;
      this.streamId = streamId;
      this.networkChannel = networkChannel;
   }

   public long getStreamId() {
      return this.streamId;
   }

   boolean tryWrite() {
      boolean didWork = false;

      while (true) {
         if (this.sendOffset < this.sendLimit) {
            MemorySegment buffer = this.tempSendBuffer != null ? this.tempSendBuffer : this.sendBuffer;
            int remaining = this.sendLimit - this.sendOffset;
            long sent = QuicheNative.connStreamSend(
               this.connection.connHandle, this.streamId, buffer.asSlice(this.sendOffset, remaining), remaining, false, this.sendErrorCode
            );
            if (sent == QuicheNative.QuicheError.DONE.code()) {
               this.writable.set(false);
               return didWork;
            }

            if (sent < 0L) {
               long appErr = this.sendErrorCode.get(ValueLayout.JAVA_LONG, 0L);
               ((HytaleLogger.Api)LOGGER.atSevere())
                  .log("Error writing to stream %d: %s (%d) appErr=%d", this.streamId, QuicheNative.QuicheError.fromCode((int)sent), sent, appErr);
               return didWork;
            }

            didWork = true;
            this.sendOffset += (int)sent;
            if (this.sendOffset < this.sendLimit) {
               this.writable.set(false);
               return true;
            }

            this.sendOffset = 0;
            this.sendLimit = 0;
            this.freeTempSendBuffer();
         }

         ToClientPacket packet = this.packetQueue.poll();
         this.writable.set(true);
         if (packet == null) {
            return didWork;
         }

         Class<? extends ToClientPacket> packetClass = (Class<? extends ToClientPacket>)(packet instanceof CachedPacket<?> cachedPacket
            ? cachedPacket.getPacketType()
            : packet.getClass());
         Integer id = PacketRegistry.getId(packetClass);
         if (id == null) {
            throw new ProtocolException("Unknown packet type: " + packetClass.getName());
         }

         PacketRegistry.PacketInfo info = PacketRegistry.getToClientPacketById(id);
         int packetSize = packet.computeSize();
         long requiredSize = (info.compressed() ? ZstdNative.compressBound(packetSize) : packetSize) + 8L;
         MemorySegment target;
         if (requiredSize > this.sendBuffer.byteSize()) {
            this.tempSendArena = Arena.ofConfined();
            this.tempSendBuffer = this.tempSendArena.allocate(requiredSize);
            target = this.tempSendBuffer;
         } else {
            target = this.sendBuffer;
         }

         this.sendLimit = PacketIO.writeFramedPacket(this.connection.compressionContext, packet, info, target, 0, packetSize, this.packetStatsRecorder);
         this.sendOffset = 0;
      }
   }

   private void freeTempSendBuffer() {
      if (this.tempSendArena != null) {
         this.tempSendArena.close();
         this.tempSendArena = null;
         this.tempSendBuffer = null;
      }
   }

   void consumePacketData(MemorySegment packetData, int dataSize) {
      int offset = 0;

      while (offset < dataSize) {
         if (!this.processingPacket) {
            int size;
            if (this.currentPacketSize != 0) {
               int sizeRead = Math.min(dataSize - offset, 4 - this.currentPacketSize);
               MemorySegment.copy(packetData, offset, this.packetBuffer, this.currentPacketOffset, sizeRead);
               this.currentPacketSize += sizeRead;
               this.currentPacketOffset += sizeRead;
               if (this.currentPacketSize < 4) {
                  return;
               }

               offset += sizeRead;
               size = this.packetBuffer.get(PACKET_SIZE_LAYOUT, 0L) + 4;
            } else {
               if (dataSize - offset < 4) {
                  int remaining = dataSize - offset;
                  MemorySegment.copy(packetData, offset, this.packetBuffer, 0L, remaining);
                  this.currentPacketSize += remaining;
                  this.currentPacketOffset += remaining;
                  return;
               }

               size = packetData.get(PACKET_SIZE_LAYOUT, (long)offset) + 4;
               offset += 4;
            }

            if (size <= 0 || size > 1677721600) {
               throw new IllegalArgumentException("Invalid packet size: " + size);
            }

            if (size > this.packetBuffer.byteSize()) {
               ((HytaleLogger.Api)LOGGER.atWarning()).log("Packet size exceeds buffer capacity, allocating new buffer: %d", size);
               this.tempPacketArena = Arena.ofConfined();
               this.tempPacketBuffer = this.tempPacketArena.allocate(size);
            }

            this.currentPacketOffset = 0;
            this.currentPacketSize = size;
         }

         this.processingPacket = true;
         MemorySegment buffer = this.tempPacketBuffer != null ? this.tempPacketBuffer : this.packetBuffer;
         int toConsume = Math.min(dataSize - offset, this.currentPacketSize - this.currentPacketOffset);
         MemorySegment.copy(packetData, offset, buffer, this.currentPacketOffset, toConsume);
         offset += toConsume;
         this.currentPacketOffset += toConsume;
         if (this.currentPacketOffset != this.currentPacketSize) {
            return;
         }

         Packet packet = PacketIO.readFramedPacket(this.connection.decompressionContext, buffer, this.currentPacketSize - 4, this.packetStatsRecorder);
         this.handler.handle((ToServerPacket)packet);
         this.processingPacket = false;
         this.currentPacketSize = 0;
         this.currentPacketOffset = 0;
         if (this.tempPacketArena != null) {
            this.tempPacketArena.close();
            this.tempPacketArena = null;
            this.tempPacketBuffer = null;
         }
      }
   }

   void writeForClose(ToClientPacket packet) {
      this.packetQueue.add(packet);
      this.tryWrite();
   }

   void close() {
      if (this.closed.compareAndSet(false, true)) {
         if (this.handler != null) {
            this.handler.closed(this.networkChannel);
         }

         this.freeTempSendBuffer();
         if (this.tempPacketArena != null) {
            this.tempPacketArena.close();
         }

         this.arena.close();
      }
   }

   @Override
   public void updateStreamPriority(int urgency, boolean incremental) {
      this.connection.execute(() -> {
         int result = QuicheNative.connStreamPriority(this.connection.connHandle, this.streamId, (byte)urgency, incremental);
         if (result < 0) {
            ((HytaleLogger.Api)LOGGER.atWarning()).log("Failed to set priority for stream %d: %d", this.streamId, result);
         }
      });
   }

   @Override
   public void flush() {
      this.connection.wake();
   }

   @Override
   public void write(ToClientPacket packet) {
      this.packetQueue.add(packet);
      this.connection.wake();
   }

   @Override
   public void writeAndFlush(ToClientPacket packet) {
      this.packetQueue.add(packet);
      this.connection.wake();
   }

   @Override
   public void write(ToClientPacket[] packets) {
      this.packetQueue.addAll(Arrays.asList(packets));
      this.connection.wake();
   }

   @Override
   public void writeAndFlush(ToClientPacket[] packets) {
      this.packetQueue.addAll(Arrays.asList(packets));
      this.connection.wake();
   }

   @Override
   public boolean isActive() {
      return !this.closed.get() && this.connection.isActive();
   }

   @Override
   public boolean isWritable() {
      return this.writable.get();
   }

   @Override
   public SocketAddress remoteAddress() {
      return this.connection.address;
   }

   @Override
   public String formatRemoteAddress() {
      return this.connection.address + "(" + formatDcid(this.connection.dcid) + ")";
   }

   static String formatDcid(byte[] dcid) {
      return HexFormat.of().formatHex(dcid);
   }

   @Override
   public void disconnect(@Nonnull FormattedMessage message) {
      this.connection.execute(() -> {
         if (!QuicheNative.connIsClosed(this.connection.connHandle)) {
            this.packetQueue.add(new ServerDisconnect(message, DisconnectType.Disconnect));
            this.tryWrite();
            QuicheNative.connClose(this.connection.connHandle, true, QuicApplicationErrorCode.NoError.getValue(), MemorySegment.NULL, 0L);
         }
      });
   }

   @Nullable
   @Override
   public PacketStatsRecorder getPacketStatsRecorder() {
      return this.packetStatsRecorder;
   }

   @Nullable
   @Override
   public String getSniHostname() {
      return this.connection.serverName;
   }

   @Override
   public boolean isFromSameOrigin(ChannelConnection other) {
      return other instanceof QuicheChannel otherQuiche && this.connection == otherQuiche.connection;
   }

   @Override
   public void execute(Runnable runnable) {
      this.connection.execute(runnable);
   }

   @Override
   public X509Certificate getClientCertificate() {
      return this.connection.certificate;
   }

   @Override
   public void initTimeoutContext(@Nonnull String stage, @Nonnull String identifier) {
   }

   @Override
   public void updateTimeoutContext(@Nonnull String stage, @Nonnull String identifier) {
   }

   @Override
   public void updateTimeoutContext(@Nonnull String stage) {
   }

   @Override
   public void setPacketTimeout(@Nonnull Duration timeout) {
   }

   @Override
   public void setStageTimeout(@Nonnull String stage, @Nonnull Duration timeout, @Nonnull BooleanSupplier condition, @Nonnull Runnable onTimeout) {
   }

   @Override
   public void clearStageTimeout() {
   }

   @Override
   public void logConnectionTimings(@Nonnull String message, @Nonnull Level level) {
   }

   @Nonnull
   @Override
   public CompletableFuture<Void> setupAuxiliaryChannels(
      @Nonnull ConnectionHandler handler, @Nonnull BiConsumer<NetworkChannel, ChannelConnection> onChannelReady
   ) {
      CompletableFuture<Void> fut = new CompletableFuture<>();
      this.execute(() -> {
         QuicheNative.connStreamPriority(this.connection.connHandle, this.streamId, (byte)0, true);
         QuicheChannel chunks = this.connection.createChannel(NetworkChannel.Chunks);
         chunks.handler = handler;
         QuicheNative.connStreamPriority(this.connection.connHandle, chunks.getStreamId(), (byte)0, true);
         onChannelReady.accept(NetworkChannel.Chunks, chunks);
         QuicheChannel worldMap = this.connection.createChannel(NetworkChannel.WorldMap);
         worldMap.handler = handler;
         QuicheNative.connStreamPriority(this.connection.connHandle, worldMap.getStreamId(), (byte)1, true);
         onChannelReady.accept(NetworkChannel.WorldMap, worldMap);
         fut.complete(null);
      });
      return fut;
   }

   @Override
   public void setChannelHandler(@Nonnull ConnectionHandler handler) {
      Runnable swap = () -> {
         ConnectionHandler old = this.handler;
         if (old != null) {
            old.unregistered(handler);
         }

         this.handler = handler;
         handler.registered(old);
      };
      if (Thread.currentThread() == this.connection.processingThread) {
         swap.run();
      } else {
         this.execute(swap);
      }
   }

   @Override
   public void closeConnection() {
      this.execute(() -> {
         QuicheNative.connStreamShutdown(this.connection.connHandle, this.streamId, 0, 0L);
         QuicheNative.connStreamShutdown(this.connection.connHandle, this.streamId, 1, 0L);
      });
   }

   @Override
   public void closeApplicationConnection() {
      this.closeApplicationConnection(QuicApplicationErrorCode.NoError);
   }

   @Override
   public void closeApplicationConnection(@Nonnull QuicApplicationErrorCode errorCode) {
      this.connection.execute(() -> {
         if (!QuicheNative.connIsClosed(this.connection.connHandle)) {
            QuicheNative.connClose(this.connection.connHandle, true, errorCode.getValue(), MemorySegment.NULL, 0L);
         }
      });
   }

   @Override
   public void closeApplicationConnection(@Nonnull QuicApplicationErrorCode errorCode, @Nonnull String reason) {
      this.connection.execute(() -> {
         if (!QuicheNative.connIsClosed(this.connection.connHandle)) {
            try (Arena tempArena = Arena.ofConfined()) {
               MemorySegment reasonMem = tempArena.allocateFrom(reason, StandardCharsets.UTF_8);
               QuicheNative.connClose(this.connection.connHandle, true, errorCode.getValue(), reasonMem, reasonMem.byteSize() - 1L);
            }
         }
      });
   }
}
