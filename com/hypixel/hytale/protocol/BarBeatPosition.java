package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;

public class BarBeatPosition {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 12;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 12;
   public static final int MAX_SIZE = 12;
   public int bar;
   public int beat;
   public float ms;

   public BarBeatPosition() {
   }

   public BarBeatPosition(int bar, int beat, float ms) {
      this.bar = bar;
      this.beat = beat;
      this.ms = ms;
   }

   public BarBeatPosition(@Nonnull BarBeatPosition other) {
      this.bar = other.bar;
      this.beat = other.beat;
      this.ms = other.ms;
   }

   @Nonnull
   public static BarBeatPosition deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 12) {
         throw ProtocolException.bufferTooSmall("BarBeatPosition", 12, buf.readableBytes() - offset);
      } else {
         BarBeatPosition obj = new BarBeatPosition();
         obj.bar = buf.getIntLE(offset + 0);
         obj.beat = buf.getIntLE(offset + 4);
         obj.ms = buf.getFloatLE(offset + 8);
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 12;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 12L;
   }

   public static int getBar(MemorySegment mem) {
      return getBar(mem, 0);
   }

   public static int getBar(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 0));
   }

   public static int getBeat(MemorySegment mem) {
      return getBeat(mem, 0);
   }

   public static int getBeat(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 4));
   }

   public static float getMs(MemorySegment mem) {
      return getMs(mem, 0);
   }

   public static float getMs(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 8));
   }

   public static BarBeatPosition toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static BarBeatPosition toObject(MemorySegment mem, int offset) {
      if (offset + 12 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("BarBeatPosition", offset + 12, (int)mem.byteSize());
      } else {
         return new BarBeatPosition(
            mem.get(PacketIO.PROTO_INT, (long)(offset + 0)), mem.get(PacketIO.PROTO_INT, (long)(offset + 4)), mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 8))
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeIntLE(this.bar);
      buf.writeIntLE(this.beat);
      buf.writeFloatLE(this.ms);
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_INT, (long)(offset + 0), this.bar);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 4), this.beat);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 8), this.ms);
      return 12;
   }

   public int computeSize() {
      return 12;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 12 ? ValidationResult.error("Buffer too small: expected at least 12 bytes") : ValidationResult.OK;
   }

   public BarBeatPosition clone() {
      BarBeatPosition copy = new BarBeatPosition();
      copy.bar = this.bar;
      copy.beat = this.beat;
      copy.ms = this.ms;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof BarBeatPosition other) ? false : this.bar == other.bar && this.beat == other.beat && this.ms == other.ms;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.bar, this.beat, this.ms);
   }
}
