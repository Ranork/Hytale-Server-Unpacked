package com.hypixel.hytale.protocol.packets.world;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;

public class ServerSetPaused implements Packet, ToClientPacket {
   public static final int PACKET_ID = 159;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 1;
   public static final int MAX_SIZE = 1;
   public boolean paused;

   @Override
   public int getId() {
      return 159;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public ServerSetPaused() {
   }

   public ServerSetPaused(boolean paused) {
      this.paused = paused;
   }

   public ServerSetPaused(@Nonnull ServerSetPaused other) {
      this.paused = other.paused;
   }

   @Nonnull
   public static ServerSetPaused deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 1) {
         throw ProtocolException.bufferTooSmall("ServerSetPaused", 1, buf.readableBytes() - offset);
      } else {
         ServerSetPaused obj = new ServerSetPaused();
         obj.paused = buf.getByte(offset + 0) != 0;
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 1;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 1L;
   }

   public static boolean getPaused(MemorySegment mem) {
      return getPaused(mem, 0);
   }

   public static boolean getPaused(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 0));
   }

   public static ServerSetPaused toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static ServerSetPaused toObject(MemorySegment mem, int offset) {
      if (offset + 1 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("ServerSetPaused", offset + 1, (int)mem.byteSize());
      } else {
         return new ServerSetPaused(mem.get(PacketIO.PROTO_BOOL, (long)(offset + 0)));
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeByte(this.paused ? 1 : 0);
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_BOOL, offset + 0, this.paused);
      return 1;
   }

   @Override
   public int computeSize() {
      return 1;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 1 ? ValidationResult.error("Buffer too small: expected at least 1 bytes") : ValidationResult.OK;
   }

   public ServerSetPaused clone() {
      ServerSetPaused copy = new ServerSetPaused();
      copy.paused = this.paused;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof ServerSetPaused other ? this.paused == other.paused : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.paused);
   }
}
