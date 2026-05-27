package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
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
      if (buf.readableBytes() - offset < 9) {
         throw ProtocolException.bufferTooSmall("InteractionCameraSettings", 9, buf.readableBytes() - offset);
      } else {
         InteractionCameraSettings obj = new InteractionCameraSettings();
         byte nullBits = buf.getByte(offset);
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 1);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 9) {
               throw ProtocolException.invalidOffset("FirstPerson", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 9 + varPosBase0;
            int firstPersonCount = VarInt.peek(buf, varPos0);
            if (firstPersonCount < 0) {
               throw ProtocolException.invalidVarInt("FirstPerson");
            }

            int varIntLen = VarInt.size(firstPersonCount);
            if (firstPersonCount > 4096000) {
               throw ProtocolException.arrayTooLong("FirstPerson", firstPersonCount, 4096000);
            }

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
            int varPosBase1 = buf.getIntLE(offset + 5);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 9) {
               throw ProtocolException.invalidOffset("ThirdPerson", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 9 + varPosBase1;
            int thirdPersonCount = VarInt.peek(buf, varPos1);
            if (thirdPersonCount < 0) {
               throw ProtocolException.invalidVarInt("ThirdPerson");
            }

            int varIntLenx = VarInt.size(thirdPersonCount);
            if (thirdPersonCount > 4096000) {
               throw ProtocolException.arrayTooLong("ThirdPerson", thirdPersonCount, 4096000);
            }

            if (varPos1 + varIntLenx + thirdPersonCount * 29L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("ThirdPerson", varPos1 + varIntLenx + thirdPersonCount * 29, buf.readableBytes());
            }

            obj.thirdPerson = new InteractionCamera[thirdPersonCount];
            int elemPos = varPos1 + varIntLenx;

            for (int i = 0; i < thirdPersonCount; i++) {
               obj.thirdPerson[i] = InteractionCamera.deserialize(buf, elemPos);
               elemPos += InteractionCamera.computeBytesConsumed(buf, elemPos);
            }
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 9;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 1);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 9) {
            throw ProtocolException.invalidOffset("FirstPerson", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 9 + fieldOffset0;
         int arrLen = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            pos0 += InteractionCamera.computeBytesConsumed(buf, pos0);
         }

         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 5);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 9) {
            throw ProtocolException.invalidOffset("ThirdPerson", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 9 + fieldOffset1;
         int arrLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            pos1 += InteractionCamera.computeBytesConsumed(buf, pos1);
         }

         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 9L;
   }

   @Nullable
   public static InteractionCamera[] getFirstPerson(MemorySegment mem) {
      return getFirstPerson(mem, 0);
   }

   @Nullable
   public static InteractionCamera[] getFirstPerson(MemorySegment mem, int offset) {
      if (!hasFirstPerson(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 1, 9, "FirstPerson");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("FirstPerson", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("FirstPerson", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 29L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("FirstPerson", off + lenOffset + len * 29, (int)mem.byteSize());
            } else {
               off += lenOffset;
               InteractionCamera[] data = new InteractionCamera[len];

               for (int i = 0; i < len; i++) {
                  data[i] = InteractionCamera.toObject(mem, off + i * 29);
               }

               return data;
            }
         }
      }
   }

   @Nullable
   public static InteractionCamera[] getThirdPerson(MemorySegment mem) {
      return getThirdPerson(mem, 0);
   }

   @Nullable
   public static InteractionCamera[] getThirdPerson(MemorySegment mem, int offset) {
      if (!hasThirdPerson(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 5, 9, "ThirdPerson");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("ThirdPerson", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("ThirdPerson", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 29L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("ThirdPerson", off + lenOffset + len * 29, (int)mem.byteSize());
            } else {
               off += lenOffset;
               InteractionCamera[] data = new InteractionCamera[len];

               for (int i = 0; i < len; i++) {
                  data[i] = InteractionCamera.toObject(mem, off + i * 29);
               }

               return data;
            }
         }
      }
   }

   public static boolean hasFirstPerson(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasThirdPerson(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   private static int getValidatedOffset(MemorySegment buffer, int base, int slotPosition, int varBlockStart, String fieldName) {
      int offset = buffer.get(PacketIO.PROTO_INT, (long)(base + slotPosition));
      if (offset >= 0 && offset <= buffer.byteSize() - base - varBlockStart) {
         return varBlockStart + offset;
      } else {
         throw ProtocolException.invalidOffset(fieldName, offset, (int)buffer.byteSize());
      }
   }

   public static InteractionCameraSettings toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static InteractionCameraSettings toObject(MemorySegment mem, int offset) {
      if (offset + 9 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("InteractionCameraSettings", offset + 9, (int)mem.byteSize());
      } else {
         InteractionCamera[] firstPerson = null;
         if (hasFirstPerson(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 1, 9, "FirstPerson");
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("FirstPerson", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("FirstPerson", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 29L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("FirstPerson", off + lenOffset + len * 29, (int)mem.byteSize());
            }

            off += lenOffset;
            firstPerson = new InteractionCamera[len];

            for (int i = 0; i < len; i++) {
               firstPerson[i] = InteractionCamera.toObject(mem, off + i * 29);
            }
         }

         InteractionCamera[] thirdPerson = null;
         if (hasThirdPerson(mem, offset)) {
            int offx = offset + getValidatedOffset(mem, offset, 5, 9, "ThirdPerson");
            long packedx = VarInt.getWithLength(mem, offx);
            int lenx = (int)packedx;
            if (lenx < 0) {
               throw ProtocolException.negativeLength("ThirdPerson", lenx);
            }

            if (lenx > 4096000) {
               throw ProtocolException.arrayTooLong("ThirdPerson", lenx, 4096000);
            }

            int lenOffset = (int)(packedx >>> 32);
            if (offx + lenOffset + lenx * 29L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("ThirdPerson", offx + lenOffset + lenx * 29, (int)mem.byteSize());
            }

            offx += lenOffset;
            thirdPerson = new InteractionCamera[lenx];

            for (int i = 0; i < lenx; i++) {
               thirdPerson[i] = InteractionCamera.toObject(mem, offx + i * 29);
            }
         }

         return new InteractionCameraSettings(firstPerson, thirdPerson);
      }
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

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.firstPerson != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.thirdPerson != null) {
         nullBits = (byte)(nullBits | 2);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      int varOffset = offset + 9;
      if (this.firstPerson != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 1), varOffset - offset - 9);
         if (this.firstPerson.length > 4096000) {
            throw ProtocolException.arrayTooLong("FirstPerson", this.firstPerson.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.firstPerson.length);
         int firstPersonValueOffset = 0;

         for (int i = 0; i < this.firstPerson.length; i++) {
            firstPersonValueOffset += this.firstPerson[i].serialize(mem, varOffset + firstPersonValueOffset);
         }

         varOffset += firstPersonValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 1), -1);
      }

      if (this.thirdPerson != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), varOffset - offset - 9);
         if (this.thirdPerson.length > 4096000) {
            throw ProtocolException.arrayTooLong("ThirdPerson", this.thirdPerson.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.thirdPerson.length);
         int thirdPersonValueOffset = 0;

         for (int i = 0; i < this.thirdPerson.length; i++) {
            thirdPersonValueOffset += this.thirdPerson[i].serialize(mem, varOffset + thirdPersonValueOffset);
         }

         varOffset += thirdPersonValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), -1);
      }

      return varOffset - offset;
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
            if (firstPersonOffset < 0 || firstPersonOffset > buffer.writerIndex() - offset - 9) {
               return ValidationResult.error("Invalid offset for FirstPerson");
            }

            int pos = offset + 9 + firstPersonOffset;
            int firstPersonCount = VarInt.peek(buffer, pos);
            if (firstPersonCount < 0) {
               return ValidationResult.error("Invalid array count for FirstPerson");
            }

            if (firstPersonCount > 4096000) {
               return ValidationResult.error("FirstPerson exceeds max length 4096000");
            }

            pos += VarInt.size(firstPersonCount);
            pos += firstPersonCount * 29;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading FirstPerson");
            }
         }

         if ((nullBits & 2) != 0) {
            int thirdPersonOffset = buffer.getIntLE(offset + 5);
            if (thirdPersonOffset < 0 || thirdPersonOffset > buffer.writerIndex() - offset - 9) {
               return ValidationResult.error("Invalid offset for ThirdPerson");
            }

            int posx = offset + 9 + thirdPersonOffset;
            int thirdPersonCount = VarInt.peek(buffer, posx);
            if (thirdPersonCount < 0) {
               return ValidationResult.error("Invalid array count for ThirdPerson");
            }

            if (thirdPersonCount > 4096000) {
               return ValidationResult.error("ThirdPerson exceeds max length 4096000");
            }

            posx += VarInt.size(thirdPersonCount);
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
