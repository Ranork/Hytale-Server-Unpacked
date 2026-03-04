package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class InteractionCameraSettings {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 9;
   public static final int MAX_SIZE = 237568019;
   @Nullable
   public InteractionCamera[] firstPerson;
   @Nullable
   public InteractionCamera[] thirdPerson;

   public InteractionCameraSettings() {
   }

   public InteractionCameraSettings(@Nullable InteractionCamera[] firstPerson, @Nullable InteractionCamera[] thirdPerson) {
      this.firstPerson = firstPerson;
      this.thirdPerson = thirdPerson;
   }

   public InteractionCameraSettings(@Nonnull InteractionCameraSettings other) {
      this.firstPerson = other.firstPerson;
      this.thirdPerson = other.thirdPerson;
   }

   @Nonnull
   public static InteractionCameraSettings deserialize(@Nonnull ByteBuf buf, int offset) {
      InteractionCameraSettings obj = new InteractionCameraSettings();
      byte nullBits = buf.getByte(offset);
      if ((nullBits & 1) != 0) {
         int varPos0 = offset + 9 + buf.getIntLE(offset + 1);
         int firstPersonCount = VarInt.peek(buf, varPos0);
         if (firstPersonCount < 0) {
            throw ProtocolException.negativeLength("FirstPerson", firstPersonCount);
         }

         if (firstPersonCount > 4096000) {
            throw ProtocolException.arrayTooLong("FirstPerson", firstPersonCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos0);
         if (varPos0 + varIntLen + firstPersonCount * 29L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("FirstPerson", varPos0 + varIntLen + firstPersonCount * 29, buf.readableBytes());
         }

         obj.firstPerson = new InteractionCamera[firstPersonCount];
         int elemPos = varPos0 + varIntLen;

         for (int i = 0; i < firstPersonCount; i++) {
            obj.firstPerson[i] = InteractionCamera.deserialize(buf, elemPos);
            elemPos += InteractionCamera.computeBytesConsumed(buf, elemPos);
         }
      }

      if ((nullBits & 2) != 0) {
         int varPos1 = offset + 9 + buf.getIntLE(offset + 5);
         int thirdPersonCount = VarInt.peek(buf, varPos1);
         if (thirdPersonCount < 0) {
            throw ProtocolException.negativeLength("ThirdPerson", thirdPersonCount);
         }

         if (thirdPersonCount > 4096000) {
            throw ProtocolException.arrayTooLong("ThirdPerson", thirdPersonCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos1);
         if (varPos1 + varIntLen + thirdPersonCount * 29L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("ThirdPerson", varPos1 + varIntLen + thirdPersonCount * 29, buf.readableBytes());
         }

         obj.thirdPerson = new InteractionCamera[thirdPersonCount];
         int elemPos = varPos1 + varIntLen;

         for (int i = 0; i < thirdPersonCount; i++) {
            obj.thirdPerson[i] = InteractionCamera.deserialize(buf, elemPos);
            elemPos += InteractionCamera.computeBytesConsumed(buf, elemPos);
         }
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 9;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 1);
         int pos0 = offset + 9 + fieldOffset0;
         int arrLen = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0);

         for (int i = 0; i < arrLen; i++) {
            pos0 += InteractionCamera.computeBytesConsumed(buf, pos0);
         }

         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 5);
         int pos1 = offset + 9 + fieldOffset1;
         int arrLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.length(buf, pos1);

         for (int i = 0; i < arrLen; i++) {
            pos1 += InteractionCamera.computeBytesConsumed(buf, pos1);
         }

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
         if (this.firstPerson.length > 4096000) {
            throw ProtocolException.arrayTooLong("FirstPerson", this.firstPerson.length, 4096000);
         }

         VarInt.write(buf, this.firstPerson.length);

         for (InteractionCamera item : this.firstPerson) {
            item.serialize(buf);
         }
      } else {
         buf.setIntLE(firstPersonOffsetSlot, -1);
      }

      if (this.thirdPerson != null) {
         buf.setIntLE(thirdPersonOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.thirdPerson.length > 4096000) {
            throw ProtocolException.arrayTooLong("ThirdPerson", this.thirdPerson.length, 4096000);
         }

         VarInt.write(buf, this.thirdPerson.length);

         for (InteractionCamera item : this.thirdPerson) {
            item.serialize(buf);
         }
      } else {
         buf.setIntLE(thirdPersonOffsetSlot, -1);
      }
   }

   public int computeSize() {
      int size = 9;
      if (this.firstPerson != null) {
         size += VarInt.size(this.firstPerson.length) + this.firstPerson.length * 29;
      }

      if (this.thirdPerson != null) {
         size += VarInt.size(this.thirdPerson.length) + this.thirdPerson.length * 29;
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

            int firstPersonCount = VarInt.peek(buffer, pos);
            if (firstPersonCount < 0) {
               return ValidationResult.error("Invalid array count for FirstPerson");
            }

            if (firstPersonCount > 4096000) {
               return ValidationResult.error("FirstPerson exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += firstPersonCount * 29;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading FirstPerson");
            }
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

            int thirdPersonCount = VarInt.peek(buffer, posx);
            if (thirdPersonCount < 0) {
               return ValidationResult.error("Invalid array count for ThirdPerson");
            }

            if (thirdPersonCount > 4096000) {
               return ValidationResult.error("ThirdPerson exceeds max length 4096000");
            }

            posx += VarInt.length(buffer, posx);
            posx += thirdPersonCount * 29;
            if (posx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading ThirdPerson");
            }
         }

         return ValidationResult.OK;
      }
   }

   public InteractionCameraSettings clone() {
      InteractionCameraSettings copy = new InteractionCameraSettings();
      copy.firstPerson = this.firstPerson != null ? Arrays.stream(this.firstPerson).map(e -> e.clone()).toArray(InteractionCamera[]::new) : null;
      copy.thirdPerson = this.thirdPerson != null ? Arrays.stream(this.thirdPerson).map(e -> e.clone()).toArray(InteractionCamera[]::new) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof InteractionCameraSettings other)
            ? false
            : Arrays.equals((Object[])this.firstPerson, (Object[])other.firstPerson) && Arrays.equals((Object[])this.thirdPerson, (Object[])other.thirdPerson);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Arrays.hashCode((Object[])this.firstPerson);
      return 31 * result + Arrays.hashCode((Object[])this.thirdPerson);
   }
}
