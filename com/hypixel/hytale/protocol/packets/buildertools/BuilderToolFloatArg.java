package com.hypixel.hytale.protocol.packets.buildertools;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;

public class BuilderToolFloatArg {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 12;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 12;
   public static final int MAX_SIZE = 12;
   public float defaultValue;
   public float min;
   public float max;

   public BuilderToolFloatArg() {
   }

   public BuilderToolFloatArg(float defaultValue, float min, float max) {
      this.defaultValue = defaultValue;
      this.min = min;
      this.max = max;
   }

   public BuilderToolFloatArg(@Nonnull BuilderToolFloatArg other) {
      this.defaultValue = other.defaultValue;
      this.min = other.min;
      this.max = other.max;
   }

   @Nonnull
   public static BuilderToolFloatArg deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 12) {
         throw ProtocolException.bufferTooSmall("BuilderToolFloatArg", 12, buf.readableBytes() - offset);
      } else {
         BuilderToolFloatArg obj = new BuilderToolFloatArg();
         obj.defaultValue = buf.getFloatLE(offset + 0);
         obj.min = buf.getFloatLE(offset + 4);
         obj.max = buf.getFloatLE(offset + 8);
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 12;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 12L;
   }

   public static float getDefault(MemorySegment mem) {
      return getDefault(mem, 0);
   }

   public static float getDefault(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 0));
   }

   public static float getMin(MemorySegment mem) {
      return getMin(mem, 0);
   }

   public static float getMin(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 4));
   }

   public static float getMax(MemorySegment mem) {
      return getMax(mem, 0);
   }

   public static float getMax(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 8));
   }

   public static BuilderToolFloatArg toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static BuilderToolFloatArg toObject(MemorySegment mem, int offset) {
      if (offset + 12 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("BuilderToolFloatArg", offset + 12, (int)mem.byteSize());
      } else {
         return new BuilderToolFloatArg(
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 0)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 4)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 8))
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeFloatLE(this.defaultValue);
      buf.writeFloatLE(this.min);
      buf.writeFloatLE(this.max);
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 0), this.defaultValue);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 4), this.min);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 8), this.max);
      return 12;
   }

   public int computeSize() {
      return 12;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 12 ? ValidationResult.error("Buffer too small: expected at least 12 bytes") : ValidationResult.OK;
   }

   public BuilderToolFloatArg clone() {
      BuilderToolFloatArg copy = new BuilderToolFloatArg();
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
         return !(obj instanceof BuilderToolFloatArg other) ? false : this.defaultValue == other.defaultValue && this.min == other.min && this.max == other.max;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.defaultValue, this.min, this.max);
   }
}
