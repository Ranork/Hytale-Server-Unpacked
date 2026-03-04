package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class FluidFXMovementSettings {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 24;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 24;
   public static final int MAX_SIZE = 24;
   public float swimUpSpeed;
   public float swimDownSpeed;
   public float sinkSpeed;
   public float horizontalSpeedMultiplier;
   public float fieldOfViewMultiplier;
   public float entryVelocityMultiplier;

   public FluidFXMovementSettings() {
   }

   public FluidFXMovementSettings(
      float swimUpSpeed, float swimDownSpeed, float sinkSpeed, float horizontalSpeedMultiplier, float fieldOfViewMultiplier, float entryVelocityMultiplier
   ) {
      this.swimUpSpeed = swimUpSpeed;
      this.swimDownSpeed = swimDownSpeed;
      this.sinkSpeed = sinkSpeed;
      this.horizontalSpeedMultiplier = horizontalSpeedMultiplier;
      this.fieldOfViewMultiplier = fieldOfViewMultiplier;
      this.entryVelocityMultiplier = entryVelocityMultiplier;
   }

   public FluidFXMovementSettings(@Nonnull FluidFXMovementSettings other) {
      this.swimUpSpeed = other.swimUpSpeed;
      this.swimDownSpeed = other.swimDownSpeed;
      this.sinkSpeed = other.sinkSpeed;
      this.horizontalSpeedMultiplier = other.horizontalSpeedMultiplier;
      this.fieldOfViewMultiplier = other.fieldOfViewMultiplier;
      this.entryVelocityMultiplier = other.entryVelocityMultiplier;
   }

   @Nonnull
   public static FluidFXMovementSettings deserialize(@Nonnull ByteBuf buf, int offset) {
      FluidFXMovementSettings obj = new FluidFXMovementSettings();
      obj.swimUpSpeed = buf.getFloatLE(offset + 0);
      obj.swimDownSpeed = buf.getFloatLE(offset + 4);
      obj.sinkSpeed = buf.getFloatLE(offset + 8);
      obj.horizontalSpeedMultiplier = buf.getFloatLE(offset + 12);
      obj.fieldOfViewMultiplier = buf.getFloatLE(offset + 16);
      obj.entryVelocityMultiplier = buf.getFloatLE(offset + 20);
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 24;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeFloatLE(this.swimUpSpeed);
      buf.writeFloatLE(this.swimDownSpeed);
      buf.writeFloatLE(this.sinkSpeed);
      buf.writeFloatLE(this.horizontalSpeedMultiplier);
      buf.writeFloatLE(this.fieldOfViewMultiplier);
      buf.writeFloatLE(this.entryVelocityMultiplier);
   }

   public int computeSize() {
      return 24;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 24 ? ValidationResult.error("Buffer too small: expected at least 24 bytes") : ValidationResult.OK;
   }

   public FluidFXMovementSettings clone() {
      FluidFXMovementSettings copy = new FluidFXMovementSettings();
      copy.swimUpSpeed = this.swimUpSpeed;
      copy.swimDownSpeed = this.swimDownSpeed;
      copy.sinkSpeed = this.sinkSpeed;
      copy.horizontalSpeedMultiplier = this.horizontalSpeedMultiplier;
      copy.fieldOfViewMultiplier = this.fieldOfViewMultiplier;
      copy.entryVelocityMultiplier = this.entryVelocityMultiplier;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof FluidFXMovementSettings other)
            ? false
            : this.swimUpSpeed == other.swimUpSpeed
               && this.swimDownSpeed == other.swimDownSpeed
               && this.sinkSpeed == other.sinkSpeed
               && this.horizontalSpeedMultiplier == other.horizontalSpeedMultiplier
               && this.fieldOfViewMultiplier == other.fieldOfViewMultiplier
               && this.entryVelocityMultiplier == other.entryVelocityMultiplier;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(
         this.swimUpSpeed, this.swimDownSpeed, this.sinkSpeed, this.horizontalSpeedMultiplier, this.fieldOfViewMultiplier, this.entryVelocityMultiplier
      );
   }
}
