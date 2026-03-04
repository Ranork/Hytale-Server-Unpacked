package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AppliedForce {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 18;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 18;
   public static final int MAX_SIZE = 18;
   @Nullable
   public Vector3f direction;
   public boolean adjustVertical;
   public float force;

   public AppliedForce() {
   }

   public AppliedForce(@Nullable Vector3f direction, boolean adjustVertical, float force) {
      this.direction = direction;
      this.adjustVertical = adjustVertical;
      this.force = force;
   }

   public AppliedForce(@Nonnull AppliedForce other) {
      this.direction = other.direction;
      this.adjustVertical = other.adjustVertical;
      this.force = other.force;
   }

   @Nonnull
   public static AppliedForce deserialize(@Nonnull ByteBuf buf, int offset) {
      AppliedForce obj = new AppliedForce();
      byte nullBits = buf.getByte(offset);
      if ((nullBits & 1) != 0) {
         obj.direction = Vector3f.deserialize(buf, offset + 1);
      }

      obj.adjustVertical = buf.getByte(offset + 13) != 0;
      obj.force = buf.getFloatLE(offset + 14);
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 18;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.direction != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      if (this.direction != null) {
         this.direction.serialize(buf);
      } else {
         buf.writeZero(12);
      }

      buf.writeByte(this.adjustVertical ? 1 : 0);
      buf.writeFloatLE(this.force);
   }

   public int computeSize() {
      return 18;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 18 ? ValidationResult.error("Buffer too small: expected at least 18 bytes") : ValidationResult.OK;
   }

   public AppliedForce clone() {
      AppliedForce copy = new AppliedForce();
      copy.direction = this.direction != null ? this.direction.clone() : null;
      copy.adjustVertical = this.adjustVertical;
      copy.force = this.force;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof AppliedForce other)
            ? false
            : Objects.equals(this.direction, other.direction) && this.adjustVertical == other.adjustVertical && this.force == other.force;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.direction, this.adjustVertical, this.force);
   }
}
