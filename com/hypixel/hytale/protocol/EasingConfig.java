package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;

public class EasingConfig {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 5;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 5;
   public static final int MAX_SIZE = 5;
   public float time;
   @Nonnull
   public EasingType type = EasingType.Linear;

   public EasingConfig() {
   }

   public EasingConfig(float time, @Nonnull EasingType type) {
      this.time = time;
      this.type = type;
   }

   public EasingConfig(@Nonnull EasingConfig other) {
      this.time = other.time;
      this.type = other.type;
   }

   @Nonnull
   public static EasingConfig deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 5) {
         throw ProtocolException.bufferTooSmall("EasingConfig", 5, buf.readableBytes() - offset);
      } else {
         EasingConfig obj = new EasingConfig();
         obj.time = buf.getFloatLE(offset + 0);
         obj.type = EasingType.fromValue(buf.getByte(offset + 4));
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 5;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 5L;
   }

   public static float getTime(MemorySegment mem) {
      return getTime(mem, 0);
   }

   public static float getTime(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 0));
   }

   public static EasingType getType(MemorySegment mem) {
      return getType(mem, 0);
   }

   public static EasingType getType(MemorySegment mem, int offset) {
      return EasingType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 4)));
   }

   public static EasingConfig toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static EasingConfig toObject(MemorySegment mem, int offset) {
      if (offset + 5 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("EasingConfig", offset + 5, (int)mem.byteSize());
      } else {
         return new EasingConfig(mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 0)), EasingType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 4))));
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeFloatLE(this.time);
      buf.writeByte(this.type.getValue());
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 0), this.time);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 4), (byte)this.type.getValue());
      return 5;
   }

   public int computeSize() {
      return 5;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 5) {
         return ValidationResult.error("Buffer too small: expected at least 5 bytes");
      } else {
         int v = buffer.getByte(offset + 4) & 255;
         return v >= 31 ? ValidationResult.error("Invalid EasingType value for Type") : ValidationResult.OK;
      }
   }

   public EasingConfig clone() {
      EasingConfig copy = new EasingConfig();
      copy.time = this.time;
      copy.type = this.type;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof EasingConfig other) ? false : this.time == other.time && Objects.equals(this.type, other.type);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.time, this.type);
   }
}
