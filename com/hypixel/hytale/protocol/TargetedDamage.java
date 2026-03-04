package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TargetedDamage {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 9;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 9;
   public static final int MAX_SIZE = 1677721600;
   public int index;
   @Nullable
   public DamageEffects damageEffects;
   public int next;

   public TargetedDamage() {
   }

   public TargetedDamage(int index, @Nullable DamageEffects damageEffects, int next) {
      this.index = index;
      this.damageEffects = damageEffects;
      this.next = next;
   }

   public TargetedDamage(@Nonnull TargetedDamage other) {
      this.index = other.index;
      this.damageEffects = other.damageEffects;
      this.next = other.next;
   }

   @Nonnull
   public static TargetedDamage deserialize(@Nonnull ByteBuf buf, int offset) {
      TargetedDamage obj = new TargetedDamage();
      byte nullBits = buf.getByte(offset);
      obj.index = buf.getIntLE(offset + 1);
      obj.next = buf.getIntLE(offset + 5);
      int pos = offset + 9;
      if ((nullBits & 1) != 0) {
         obj.damageEffects = DamageEffects.deserialize(buf, pos);
         pos += DamageEffects.computeBytesConsumed(buf, pos);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 9;
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
      buf.writeIntLE(this.index);
      buf.writeIntLE(this.next);
      if (this.damageEffects != null) {
         this.damageEffects.serialize(buf);
      }
   }

   public int computeSize() {
      int size = 9;
      if (this.damageEffects != null) {
         size += this.damageEffects.computeSize();
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 9) {
         return ValidationResult.error("Buffer too small: expected at least 9 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 9;
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

   public TargetedDamage clone() {
      TargetedDamage copy = new TargetedDamage();
      copy.index = this.index;
      copy.damageEffects = this.damageEffects != null ? this.damageEffects.clone() : null;
      copy.next = this.next;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof TargetedDamage other)
            ? false
            : this.index == other.index && Objects.equals(this.damageEffects, other.damageEffects) && this.next == other.next;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.index, this.damageEffects, this.next);
   }
}
