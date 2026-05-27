package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;

public class ClampConfig {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 9;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 9;
   public static final int MAX_SIZE = 9;
   public float min;
   public float max;
   public boolean normalize;

   public ClampConfig() {
   }

   public ClampConfig(float min, float max, boolean normalize) {
      this.min = min;
      this.max = max;
      this.normalize = normalize;
   }

   public ClampConfig(@Nonnull ClampConfig other) {
      this.min = other.min;
      this.max = other.max;
      this.normalize = other.normalize;
   }

   @Nonnull
   public static ClampConfig deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 9) {
         throw ProtocolException.bufferTooSmall("ClampConfig", 9, buf.readableBytes() - offset);
      } else {
         ClampConfig obj = new ClampConfig();
         obj.min = buf.getFloatLE(offset + 0);
         obj.max = buf.getFloatLE(offset + 4);
         obj.normalize = buf.getByte(offset + 8) != 0;
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 9;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 9L;
   }

   public static float getMin(MemorySegment mem) {
      return getMin(mem, 0);
   }

   public static float getMin(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 0));
   }

   public static float getMax(MemorySegment mem) {
      return getMax(mem, 0);
   }

   public static float getMax(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 4));
   }

   public static boolean getNormalize(MemorySegment mem) {
      return getNormalize(mem, 0);
   }

   public static boolean getNormalize(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 8));
   }

   public static ClampConfig toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static ClampConfig toObject(MemorySegment mem, int offset) {
      if (offset + 9 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("ClampConfig", offset + 9, (int)mem.byteSize());
      } else {
         return new ClampConfig(
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 0)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 4)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 8))
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeFloatLE(this.min);
      buf.writeFloatLE(this.max);
      buf.writeByte(this.normalize ? 1 : 0);
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 0), this.min);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 4), this.max);
      mem.set(PacketIO.PROTO_BOOL, offset + 8, this.normalize);
      return 9;
   }

   public int computeSize() {
      return 9;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 9 ? ValidationResult.error("Buffer too small: expected at least 9 bytes") : ValidationResult.OK;
   }

   public ClampConfig clone() {
      ClampConfig copy = new ClampConfig();
      copy.min = this.min;
      copy.max = this.max;
      copy.normalize = this.normalize;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ClampConfig other) ? false : this.min == other.min && this.max == other.max && this.normalize == other.normalize;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.min, this.max, this.normalize);
   }
}
