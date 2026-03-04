package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
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
      FogOptions obj = new FogOptions();
      obj.ignoreFogLimits = buf.getByte(offset + 0) != 0;
      obj.effectiveViewDistanceMultiplier = buf.getFloatLE(offset + 1);
      obj.fogFarViewDistance = buf.getFloatLE(offset + 5);
      obj.fogHeightCameraOffset = buf.getFloatLE(offset + 9);
      obj.fogHeightCameraOverriden = buf.getByte(offset + 13) != 0;
      obj.fogHeightCameraFixed = buf.getFloatLE(offset + 14);
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 18;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeByte(this.ignoreFogLimits ? 1 : 0);
      buf.writeFloatLE(this.effectiveViewDistanceMultiplier);
      buf.writeFloatLE(this.fogFarViewDistance);
      buf.writeFloatLE(this.fogHeightCameraOffset);
      buf.writeByte(this.fogHeightCameraOverriden ? 1 : 0);
      buf.writeFloatLE(this.fogHeightCameraFixed);
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
