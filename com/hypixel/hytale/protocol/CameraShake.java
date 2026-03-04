package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CameraShake {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 9;
   public static final int MAX_SIZE = 1130496177;
   @Nullable
   public CameraShakeConfig firstPerson;
   @Nullable
   public CameraShakeConfig thirdPerson;

   public CameraShake() {
   }

   public CameraShake(@Nullable CameraShakeConfig firstPerson, @Nullable CameraShakeConfig thirdPerson) {
      this.firstPerson = firstPerson;
      this.thirdPerson = thirdPerson;
   }

   public CameraShake(@Nonnull CameraShake other) {
      this.firstPerson = other.firstPerson;
      this.thirdPerson = other.thirdPerson;
   }

   @Nonnull
   public static CameraShake deserialize(@Nonnull ByteBuf buf, int offset) {
      CameraShake obj = new CameraShake();
      byte nullBits = buf.getByte(offset);
      if ((nullBits & 1) != 0) {
         int varPos0 = offset + 9 + buf.getIntLE(offset + 1);
         obj.firstPerson = CameraShakeConfig.deserialize(buf, varPos0);
      }

      if ((nullBits & 2) != 0) {
         int varPos1 = offset + 9 + buf.getIntLE(offset + 5);
         obj.thirdPerson = CameraShakeConfig.deserialize(buf, varPos1);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 9;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 1);
         int pos0 = offset + 9 + fieldOffset0;
         pos0 += CameraShakeConfig.computeBytesConsumed(buf, pos0);
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 5);
         int pos1 = offset + 9 + fieldOffset1;
         pos1 += CameraShakeConfig.computeBytesConsumed(buf, pos1);
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.firstPerson != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.thirdPerson != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      int firstPersonOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int thirdPersonOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.firstPerson != null) {
         buf.setIntLE(firstPersonOffsetSlot, buf.writerIndex() - varBlockStart);
         this.firstPerson.serialize(buf);
      } else {
         buf.setIntLE(firstPersonOffsetSlot, -1);
      }

      if (this.thirdPerson != null) {
         buf.setIntLE(thirdPersonOffsetSlot, buf.writerIndex() - varBlockStart);
         this.thirdPerson.serialize(buf);
      } else {
         buf.setIntLE(thirdPersonOffsetSlot, -1);
      }
   }

   public int computeSize() {
      int size = 9;
      if (this.firstPerson != null) {
         size += this.firstPerson.computeSize();
      }

      if (this.thirdPerson != null) {
         size += this.thirdPerson.computeSize();
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 9) {
         return ValidationResult.error("Buffer too small: expected at least 9 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int firstPersonOffset = buffer.getIntLE(offset + 1);
            if (firstPersonOffset < 0) {
               return ValidationResult.error("Invalid offset for FirstPerson");
            }

            int pos = offset + 9 + firstPersonOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for FirstPerson");
            }

            ValidationResult firstPersonResult = CameraShakeConfig.validateStructure(buffer, pos);
            if (!firstPersonResult.isValid()) {
               return ValidationResult.error("Invalid FirstPerson: " + firstPersonResult.error());
            }

            pos += CameraShakeConfig.computeBytesConsumed(buffer, pos);
         }

         if ((nullBits & 2) != 0) {
            int thirdPersonOffset = buffer.getIntLE(offset + 5);
            if (thirdPersonOffset < 0) {
               return ValidationResult.error("Invalid offset for ThirdPerson");
            }

            int posx = offset + 9 + thirdPersonOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for ThirdPerson");
            }

            ValidationResult thirdPersonResult = CameraShakeConfig.validateStructure(buffer, posx);
            if (!thirdPersonResult.isValid()) {
               return ValidationResult.error("Invalid ThirdPerson: " + thirdPersonResult.error());
            }

            posx += CameraShakeConfig.computeBytesConsumed(buffer, posx);
         }

         return ValidationResult.OK;
      }
   }

   public CameraShake clone() {
      CameraShake copy = new CameraShake();
      copy.firstPerson = this.firstPerson != null ? this.firstPerson.clone() : null;
      copy.thirdPerson = this.thirdPerson != null ? this.thirdPerson.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof CameraShake other)
            ? false
            : Objects.equals(this.firstPerson, other.firstPerson) && Objects.equals(this.thirdPerson, other.thirdPerson);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.firstPerson, this.thirdPerson);
   }
}
