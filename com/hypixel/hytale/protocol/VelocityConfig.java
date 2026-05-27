package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;

public class VelocityConfig {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 21;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 21;
   public static final int MAX_SIZE = 21;
   public float groundResistance;
   public float groundResistanceMax;
   public float airResistance;
   public float airResistanceMax;
   public float threshold;
   @Nonnull
   public VelocityThresholdStyle style = VelocityThresholdStyle.Linear;

   public VelocityConfig() {
   }

   public VelocityConfig(
      float groundResistance, float groundResistanceMax, float airResistance, float airResistanceMax, float threshold, @Nonnull VelocityThresholdStyle style
   ) {
      this.groundResistance = groundResistance;
      this.groundResistanceMax = groundResistanceMax;
      this.airResistance = airResistance;
      this.airResistanceMax = airResistanceMax;
      this.threshold = threshold;
      this.style = style;
   }

   public VelocityConfig(@Nonnull VelocityConfig other) {
      this.groundResistance = other.groundResistance;
      this.groundResistanceMax = other.groundResistanceMax;
      this.airResistance = other.airResistance;
      this.airResistanceMax = other.airResistanceMax;
      this.threshold = other.threshold;
      this.style = other.style;
   }

   @Nonnull
   public static VelocityConfig deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 21) {
         throw ProtocolException.bufferTooSmall("VelocityConfig", 21, buf.readableBytes() - offset);
      } else {
         VelocityConfig obj = new VelocityConfig();
         obj.groundResistance = buf.getFloatLE(offset + 0);
         obj.groundResistanceMax = buf.getFloatLE(offset + 4);
         obj.airResistance = buf.getFloatLE(offset + 8);
         obj.airResistanceMax = buf.getFloatLE(offset + 12);
         obj.threshold = buf.getFloatLE(offset + 16);
         obj.style = VelocityThresholdStyle.fromValue(buf.getByte(offset + 20));
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 21;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 21L;
   }

   public static float getGroundResistance(MemorySegment mem) {
      return getGroundResistance(mem, 0);
   }

   public static float getGroundResistance(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 0));
   }

   public static float getGroundResistanceMax(MemorySegment mem) {
      return getGroundResistanceMax(mem, 0);
   }

   public static float getGroundResistanceMax(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 4));
   }

   public static float getAirResistance(MemorySegment mem) {
      return getAirResistance(mem, 0);
   }

   public static float getAirResistance(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 8));
   }

   public static float getAirResistanceMax(MemorySegment mem) {
      return getAirResistanceMax(mem, 0);
   }

   public static float getAirResistanceMax(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 12));
   }

   public static float getThreshold(MemorySegment mem) {
      return getThreshold(mem, 0);
   }

   public static float getThreshold(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 16));
   }

   public static VelocityThresholdStyle getStyle(MemorySegment mem) {
      return getStyle(mem, 0);
   }

   public static VelocityThresholdStyle getStyle(MemorySegment mem, int offset) {
      return VelocityThresholdStyle.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 20)));
   }

   public static VelocityConfig toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static VelocityConfig toObject(MemorySegment mem, int offset) {
      if (offset + 21 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("VelocityConfig", offset + 21, (int)mem.byteSize());
      } else {
         return new VelocityConfig(
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 0)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 4)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 8)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 12)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 16)),
            VelocityThresholdStyle.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 20)))
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeFloatLE(this.groundResistance);
      buf.writeFloatLE(this.groundResistanceMax);
      buf.writeFloatLE(this.airResistance);
      buf.writeFloatLE(this.airResistanceMax);
      buf.writeFloatLE(this.threshold);
      buf.writeByte(this.style.getValue());
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 0), this.groundResistance);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 4), this.groundResistanceMax);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 8), this.airResistance);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 12), this.airResistanceMax);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 16), this.threshold);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 20), (byte)this.style.getValue());
      return 21;
   }

   public int computeSize() {
      return 21;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 21) {
         return ValidationResult.error("Buffer too small: expected at least 21 bytes");
      } else {
         int v = buffer.getByte(offset + 20) & 255;
         return v >= 2 ? ValidationResult.error("Invalid VelocityThresholdStyle value for Style") : ValidationResult.OK;
      }
   }

   public VelocityConfig clone() {
      VelocityConfig copy = new VelocityConfig();
      copy.groundResistance = this.groundResistance;
      copy.groundResistanceMax = this.groundResistanceMax;
      copy.airResistance = this.airResistance;
      copy.airResistanceMax = this.airResistanceMax;
      copy.threshold = this.threshold;
      copy.style = this.style;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof VelocityConfig other)
            ? false
            : this.groundResistance == other.groundResistance
               && this.groundResistanceMax == other.groundResistanceMax
               && this.airResistance == other.airResistance
               && this.airResistanceMax == other.airResistanceMax
               && this.threshold == other.threshold
               && Objects.equals(this.style, other.style);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.groundResistance, this.groundResistanceMax, this.airResistance, this.airResistanceMax, this.threshold, this.style);
   }
}
