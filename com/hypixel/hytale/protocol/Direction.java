package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class Direction {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 12;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 12;
   public static final int MAX_SIZE = 12;
   public float yaw;
   public float pitch;
   public float roll;

   public Direction() {
   }

   public Direction(float yaw, float pitch, float roll) {
      this.yaw = yaw;
      this.pitch = pitch;
      this.roll = roll;
   }

   public Direction(@Nonnull Direction other) {
      this.yaw = other.yaw;
      this.pitch = other.pitch;
      this.roll = other.roll;
   }

   @Nonnull
   public static Direction deserialize(@Nonnull ByteBuf buf, int offset) {
      Direction obj = new Direction();
      obj.yaw = buf.getFloatLE(offset + 0);
      obj.pitch = buf.getFloatLE(offset + 4);
      obj.roll = buf.getFloatLE(offset + 8);
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 12;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeFloatLE(this.yaw);
      buf.writeFloatLE(this.pitch);
      buf.writeFloatLE(this.roll);
   }

   public int computeSize() {
      return 12;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 12 ? ValidationResult.error("Buffer too small: expected at least 12 bytes") : ValidationResult.OK;
   }

   public Direction clone() {
      Direction copy = new Direction();
      copy.yaw = this.yaw;
      copy.pitch = this.pitch;
      copy.roll = this.roll;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof Direction other) ? false : this.yaw == other.yaw && this.pitch == other.pitch && this.roll == other.roll;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.yaw, this.pitch, this.roll);
   }
}
