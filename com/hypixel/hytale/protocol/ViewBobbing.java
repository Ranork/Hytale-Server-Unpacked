package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ViewBobbing {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 1;
   public static final int MAX_SIZE = 565248085;
   @Nullable
   public CameraShakeConfig firstPerson;

   public ViewBobbing() {
   }

   public ViewBobbing(@Nullable CameraShakeConfig firstPerson) {
      this.firstPerson = firstPerson;
   }

   public ViewBobbing(@Nonnull ViewBobbing other) {
      this.firstPerson = other.firstPerson;
   }

   @Nonnull
   public static ViewBobbing deserialize(@Nonnull ByteBuf buf, int offset) {
      ViewBobbing obj = new ViewBobbing();
      byte nullBits = buf.getByte(offset);
      int pos = offset + 1;
      if ((nullBits & 1) != 0) {
         obj.firstPerson = CameraShakeConfig.deserialize(buf, pos);
         pos += CameraShakeConfig.computeBytesConsumed(buf, pos);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 1;
      if ((nullBits & 1) != 0) {
         pos += CameraShakeConfig.computeBytesConsumed(buf, pos);
      }

      return pos - offset;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.firstPerson != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      if (this.firstPerson != null) {
         this.firstPerson.serialize(buf);
      }
   }

   public int computeSize() {
      int size = 1;
      if (this.firstPerson != null) {
         size += this.firstPerson.computeSize();
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 1) {
         return ValidationResult.error("Buffer too small: expected at least 1 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 1;
         if ((nullBits & 1) != 0) {
            ValidationResult firstPersonResult = CameraShakeConfig.validateStructure(buffer, pos);
            if (!firstPersonResult.isValid()) {
               return ValidationResult.error("Invalid FirstPerson: " + firstPersonResult.error());
            }

            pos += CameraShakeConfig.computeBytesConsumed(buffer, pos);
         }

         return ValidationResult.OK;
      }
   }

   public ViewBobbing clone() {
      ViewBobbing copy = new ViewBobbing();
      copy.firstPerson = this.firstPerson != null ? this.firstPerson.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof ViewBobbing other ? Objects.equals(this.firstPerson, other.firstPerson) : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.firstPerson);
   }
}
