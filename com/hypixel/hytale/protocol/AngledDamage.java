package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AngledDamage {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 21;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 21;
   public static final int MAX_SIZE = 1677721600;
   public double angle;
   public double angleDistance;
   @Nullable
   public DamageEffects damageEffects;
   public int next;

   public AngledDamage() {
   }

   public AngledDamage(double angle, double angleDistance, @Nullable DamageEffects damageEffects, int next) {
      this.angle = angle;
      this.angleDistance = angleDistance;
      this.damageEffects = damageEffects;
      this.next = next;
   }

   public AngledDamage(@Nonnull AngledDamage other) {
      this.angle = other.angle;
      this.angleDistance = other.angleDistance;
      this.damageEffects = other.damageEffects;
      this.next = other.next;
   }

   @Nonnull
   public static AngledDamage deserialize(@Nonnull ByteBuf buf, int offset) {
      AngledDamage obj = new AngledDamage();
      byte nullBits = buf.getByte(offset);
      obj.angle = buf.getDoubleLE(offset + 1);
      obj.angleDistance = buf.getDoubleLE(offset + 9);
      obj.next = buf.getIntLE(offset + 17);
      int pos = offset + 21;
      if ((nullBits & 1) != 0) {
         obj.damageEffects = DamageEffects.deserialize(buf, pos);
         pos += DamageEffects.computeBytesConsumed(buf, pos);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 21;
      if ((nullBits & 1) != 0) {
         pos += DamageEffects.computeBytesConsumed(buf, pos);
      }

      return pos - offset;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.damageEffects != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeDoubleLE(this.angle);
      buf.writeDoubleLE(this.angleDistance);
      buf.writeIntLE(this.next);
      if (this.damageEffects != null) {
         this.damageEffects.serialize(buf);
      }
   }

   public int computeSize() {
      int size = 21;
      if (this.damageEffects != null) {
         size += this.damageEffects.computeSize();
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 21) {
         return ValidationResult.error("Buffer too small: expected at least 21 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 21;
         if ((nullBits & 1) != 0) {
            ValidationResult damageEffectsResult = DamageEffects.validateStructure(buffer, pos);
            if (!damageEffectsResult.isValid()) {
               return ValidationResult.error("Invalid DamageEffects: " + damageEffectsResult.error());
            }

            pos += DamageEffects.computeBytesConsumed(buffer, pos);
         }

         return ValidationResult.OK;
      }
   }

   public AngledDamage clone() {
      AngledDamage copy = new AngledDamage();
      copy.angle = this.angle;
      copy.angleDistance = this.angleDistance;
      copy.damageEffects = this.damageEffects != null ? this.damageEffects.clone() : null;
      copy.next = this.next;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof AngledDamage other)
            ? false
            : this.angle == other.angle
               && this.angleDistance == other.angleDistance
               && Objects.equals(this.damageEffects, other.damageEffects)
               && this.next == other.next;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.angle, this.angleDistance, this.damageEffects, this.next);
   }
}
