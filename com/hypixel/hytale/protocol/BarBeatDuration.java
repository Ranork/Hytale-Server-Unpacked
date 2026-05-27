package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;

public class BarBeatDuration {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 12;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 12;
   public static final int MAX_SIZE = 12;
   public int bars;
   public int beats;
   public float ms;

   public BarBeatDuration() {
   }

   public BarBeatDuration(int bars, int beats, float ms) {
      this.bars = bars;
      this.beats = beats;
      this.ms = ms;
   }

   public BarBeatDuration(@Nonnull BarBeatDuration other) {
      this.bars = other.bars;
      this.beats = other.beats;
      this.ms = other.ms;
   }

   @Nonnull
   public static BarBeatDuration deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 12) {
         throw ProtocolException.bufferTooSmall("BarBeatDuration", 12, buf.readableBytes() - offset);
      } else {
         BarBeatDuration obj = new BarBeatDuration();
         obj.bars = buf.getIntLE(offset + 0);
         obj.beats = buf.getIntLE(offset + 4);
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

   public static int getBars(MemorySegment mem) {
      return getBars(mem, 0);
   }

   public static int getBars(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 0));
   }

   public static int getBeats(MemorySegment mem) {
      return getBeats(mem, 0);
   }

   public static int getBeats(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 4));
   }

   public static float getMs(MemorySegment mem) {
      return getMs(mem, 0);
   }

   public static float getMs(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 8));
   }

   public static BarBeatDuration toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static BarBeatDuration toObject(MemorySegment mem, int offset) {
      if (offset + 12 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("BarBeatDuration", offset + 12, (int)mem.byteSize());
      } else {
         return new BarBeatDuration(
            mem.get(PacketIO.PROTO_INT, (long)(offset + 0)), mem.get(PacketIO.PROTO_INT, (long)(offset + 4)), mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 8))
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeIntLE(this.bars);
      buf.writeIntLE(this.beats);
      buf.writeFloatLE(this.ms);
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_INT, (long)(offset + 0), this.bars);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 4), this.beats);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 8), this.ms);
      return 12;
   }

   public int computeSize() {
      return 12;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 12 ? ValidationResult.error("Buffer too small: expected at least 12 bytes") : ValidationResult.OK;
   }

   public BarBeatDuration clone() {
      BarBeatDuration copy = new BarBeatDuration();
      copy.bars = this.bars;
      copy.beats = this.beats;
      copy.ms = this.ms;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof BarBeatDuration other) ? false : this.bars == other.bars && this.beats == other.beats && this.ms == other.ms;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.bars, this.beats, this.ms);
   }
}
