package com.hypixel.hytale.protocol.packets.buildertools;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;

public class BuilderToolIntArg {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 12;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 12;
   public static final int MAX_SIZE = 12;
   public int defaultValue;
   public int min;
   public int max;

   public BuilderToolIntArg() {
   }

   public BuilderToolIntArg(int defaultValue, int min, int max) {
      this.defaultValue = defaultValue;
      this.min = min;
      this.max = max;
   }

   public BuilderToolIntArg(@Nonnull BuilderToolIntArg other) {
      this.defaultValue = other.defaultValue;
      this.min = other.min;
      this.max = other.max;
   }

   @Nonnull
   public static BuilderToolIntArg deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 12) {
         throw ProtocolException.bufferTooSmall("BuilderToolIntArg", 12, buf.readableBytes() - offset);
      } else {
         BuilderToolIntArg obj = new BuilderToolIntArg();
         obj.defaultValue = buf.getIntLE(offset + 0);
         obj.min = buf.getIntLE(offset + 4);
         obj.max = buf.getIntLE(offset + 8);
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 12;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 12L;
   }

   public static int getDefault(MemorySegment mem) {
      return getDefault(mem, 0);
   }

   public static int getDefault(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 0));
   }

   public static int getMin(MemorySegment mem) {
      return getMin(mem, 0);
   }

   public static int getMin(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 4));
   }

   public static int getMax(MemorySegment mem) {
      return getMax(mem, 0);
   }

   public static int getMax(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 8));
   }

   public static BuilderToolIntArg toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static BuilderToolIntArg toObject(MemorySegment mem, int offset) {
      if (offset + 12 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("BuilderToolIntArg", offset + 12, (int)mem.byteSize());
      } else {
         return new BuilderToolIntArg(
            mem.get(PacketIO.PROTO_INT, (long)(offset + 0)), mem.get(PacketIO.PROTO_INT, (long)(offset + 4)), mem.get(PacketIO.PROTO_INT, (long)(offset + 8))
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeIntLE(this.defaultValue);
      buf.writeIntLE(this.min);
      buf.writeIntLE(this.max);
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_INT, (long)(offset + 0), this.defaultValue);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 4), this.min);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 8), this.max);
      return 12;
   }

   public int computeSize() {
      return 12;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 12 ? ValidationResult.error("Buffer too small: expected at least 12 bytes") : ValidationResult.OK;
   }

   public BuilderToolIntArg clone() {
      BuilderToolIntArg copy = new BuilderToolIntArg();
      copy.defaultValue = this.defaultValue;
      copy.min = this.min;
      copy.max = this.max;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof BuilderToolIntArg other) ? false : this.defaultValue == other.defaultValue && this.min == other.min && this.max == other.max;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.defaultValue, this.min, this.max);
   }
}
