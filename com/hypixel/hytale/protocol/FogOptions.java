package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;

public class FogOptions {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 18;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 18;
   public static final int MAX_SIZE = 18;
   public boolean ignoreFogLimits;
   public float effectiveViewDistanceMultiplier;
   public float fogFarViewDistance;
   public float fogHeightCameraOffset;
   public boolean fogHeightCameraOverriden;
   public float fogHeightCameraFixed;

   public FogOptions() {
   }

   public FogOptions(
      boolean ignoreFogLimits,
      float effectiveViewDistanceMultiplier,
      float fogFarViewDistance,
      float fogHeightCameraOffset,
      boolean fogHeightCameraOverriden,
      float fogHeightCameraFixed
   ) {
      this.ignoreFogLimits = ignoreFogLimits;
      this.effectiveViewDistanceMultiplier = effectiveViewDistanceMultiplier;
      this.fogFarViewDistance = fogFarViewDistance;
      this.fogHeightCameraOffset = fogHeightCameraOffset;
      this.fogHeightCameraOverriden = fogHeightCameraOverriden;
      this.fogHeightCameraFixed = fogHeightCameraFixed;
   }

   public FogOptions(@Nonnull FogOptions other) {
      this.ignoreFogLimits = other.ignoreFogLimits;
      this.effectiveViewDistanceMultiplier = other.effectiveViewDistanceMultiplier;
      this.fogFarViewDistance = other.fogFarViewDistance;
      this.fogHeightCameraOffset = other.fogHeightCameraOffset;
      this.fogHeightCameraOverriden = other.fogHeightCameraOverriden;
      this.fogHeightCameraFixed = other.fogHeightCameraFixed;
   }

   @Nonnull
   public static FogOptions deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 18) {
         throw ProtocolException.bufferTooSmall("FogOptions", 18, buf.readableBytes() - offset);
      } else {
         FogOptions obj = new FogOptions();
         obj.ignoreFogLimits = buf.getByte(offset + 0) != 0;
         obj.effectiveViewDistanceMultiplier = buf.getFloatLE(offset + 1);
         obj.fogFarViewDistance = buf.getFloatLE(offset + 5);
         obj.fogHeightCameraOffset = buf.getFloatLE(offset + 9);
         obj.fogHeightCameraOverriden = buf.getByte(offset + 13) != 0;
         obj.fogHeightCameraFixed = buf.getFloatLE(offset + 14);
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 18;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 18L;
   }

   public static boolean getIgnoreFogLimits(MemorySegment mem) {
      return getIgnoreFogLimits(mem, 0);
   }

   public static boolean getIgnoreFogLimits(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 0));
   }

   public static float getEffectiveViewDistanceMultiplier(MemorySegment mem) {
      return getEffectiveViewDistanceMultiplier(mem, 0);
   }

   public static float getEffectiveViewDistanceMultiplier(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 1));
   }

   public static float getFogFarViewDistance(MemorySegment mem) {
      return getFogFarViewDistance(mem, 0);
   }

   public static float getFogFarViewDistance(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 5));
   }

   public static float getFogHeightCameraOffset(MemorySegment mem) {
      return getFogHeightCameraOffset(mem, 0);
   }

   public static float getFogHeightCameraOffset(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 9));
   }

   public static boolean getFogHeightCameraOverriden(MemorySegment mem) {
      return getFogHeightCameraOverriden(mem, 0);
   }

   public static boolean getFogHeightCameraOverriden(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 13));
   }

   public static float getFogHeightCameraFixed(MemorySegment mem) {
      return getFogHeightCameraFixed(mem, 0);
   }

   public static float getFogHeightCameraFixed(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 14));
   }

   public static FogOptions toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static FogOptions toObject(MemorySegment mem, int offset) {
      if (offset + 18 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("FogOptions", offset + 18, (int)mem.byteSize());
      } else {
         return new FogOptions(
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 0)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 1)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 5)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 9)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 13)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 14))
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeByte(this.ignoreFogLimits ? 1 : 0);
      buf.writeFloatLE(this.effectiveViewDistanceMultiplier);
      buf.writeFloatLE(this.fogFarViewDistance);
      buf.writeFloatLE(this.fogHeightCameraOffset);
      buf.writeByte(this.fogHeightCameraOverriden ? 1 : 0);
      buf.writeFloatLE(this.fogHeightCameraFixed);
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_BOOL, offset + 0, this.ignoreFogLimits);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 1), this.effectiveViewDistanceMultiplier);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 5), this.fogFarViewDistance);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 9), this.fogHeightCameraOffset);
      mem.set(PacketIO.PROTO_BOOL, offset + 13, this.fogHeightCameraOverriden);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 14), this.fogHeightCameraFixed);
      return 18;
   }

   public int computeSize() {
      return 18;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 18 ? ValidationResult.error("Buffer too small: expected at least 18 bytes") : ValidationResult.OK;
   }

   public FogOptions clone() {
      FogOptions copy = new FogOptions();
      copy.ignoreFogLimits = this.ignoreFogLimits;
      copy.effectiveViewDistanceMultiplier = this.effectiveViewDistanceMultiplier;
      copy.fogFarViewDistance = this.fogFarViewDistance;
      copy.fogHeightCameraOffset = this.fogHeightCameraOffset;
      copy.fogHeightCameraOverriden = this.fogHeightCameraOverriden;
      copy.fogHeightCameraFixed = this.fogHeightCameraFixed;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof FogOptions other)
            ? false
            : this.ignoreFogLimits == other.ignoreFogLimits
               && this.effectiveViewDistanceMultiplier == other.effectiveViewDistanceMultiplier
               && this.fogFarViewDistance == other.fogFarViewDistance
               && this.fogHeightCameraOffset == other.fogHeightCameraOffset
               && this.fogHeightCameraOverriden == other.fogHeightCameraOverriden
               && this.fogHeightCameraFixed == other.fogHeightCameraFixed;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(
         this.ignoreFogLimits,
         this.effectiveViewDistanceMultiplier,
         this.fogFarViewDistance,
         this.fogHeightCameraOffset,
         this.fogHeightCameraOverriden,
         this.fogHeightCameraFixed
      );
   }
}
