package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Arrays;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EquipmentUpdate extends ComponentUpdate {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 3;
   public static final int VARIABLE_BLOCK_START = 13;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public String[] armorIds;
   @Nullable
   public String rightHandItemId;
   @Nullable
   public String leftHandItemId;

   public EquipmentUpdate() {
   }

   public EquipmentUpdate(@Nullable String[] armorIds, @Nullable String rightHandItemId, @Nullable String leftHandItemId) {
      this.armorIds = armorIds;
      this.rightHandItemId = rightHandItemId;
      this.leftHandItemId = leftHandItemId;
   }

   public EquipmentUpdate(@Nonnull EquipmentUpdate other) {
      this.armorIds = other.armorIds;
      this.rightHandItemId = other.rightHandItemId;
      this.leftHandItemId = other.leftHandItemId;
   }

   @Nonnull
   public static EquipmentUpdate deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 13) {
         throw ProtocolException.bufferTooSmall("EquipmentUpdate", 13, buf.readableBytes() - offset);
      } else {
         EquipmentUpdate obj = new EquipmentUpdate();
         byte nullBits = buf.getByte(offset);
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 1);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 13) {
               throw ProtocolException.invalidOffset("ArmorIds", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 13 + varPosBase0;
            int armorIdsCount = VarInt.peek(buf, varPos0);
            if (armorIdsCount < 0) {
               throw ProtocolException.invalidVarInt("ArmorIds");
            }

            int varIntLen = VarInt.size(armorIdsCount);
            if (armorIdsCount > 4096000) {
               throw ProtocolException.arrayTooLong("ArmorIds", armorIdsCount, 4096000);
            }

            if (varPos0 + varIntLen + armorIdsCount * 1L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("ArmorIds", varPos0 + varIntLen + armorIdsCount * 1, buf.readableBytes());
            }

            obj.armorIds = new String[armorIdsCount];
            int elemPos = varPos0 + varIntLen;

            for (int i = 0; i < armorIdsCount; i++) {
               int strLen = VarInt.peek(buf, elemPos);
               if (strLen < 0) {
                  throw ProtocolException.invalidVarInt("armorIds[" + i + "]");
               }

               int strVarLen = VarInt.size(strLen);
               if (strLen > 4096000) {
                  throw ProtocolException.stringTooLong("armorIds[" + i + "]", strLen, 4096000);
               }

               if (elemPos + strVarLen + strLen > buf.readableBytes()) {
                  throw ProtocolException.bufferTooSmall("armorIds[" + i + "]", elemPos + strVarLen + strLen, buf.readableBytes());
               }

               obj.armorIds[i] = PacketIO.readVarString(buf, elemPos);
               elemPos += strVarLen + strLen;
            }
         }

         if ((nullBits & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 5);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 13) {
               throw ProtocolException.invalidOffset("RightHandItemId", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 13 + varPosBase1;
            int rightHandItemIdLen = VarInt.peek(buf, varPos1);
            if (rightHandItemIdLen < 0) {
               throw ProtocolException.invalidVarInt("RightHandItemId");
            }

            int rightHandItemIdVarIntLen = VarInt.size(rightHandItemIdLen);
            if (rightHandItemIdLen > 4096000) {
               throw ProtocolException.stringTooLong("RightHandItemId", rightHandItemIdLen, 4096000);
            }

            if (varPos1 + rightHandItemIdVarIntLen + rightHandItemIdLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("RightHandItemId", varPos1 + rightHandItemIdVarIntLen + rightHandItemIdLen, buf.readableBytes());
            }

            obj.rightHandItemId = PacketIO.readVarString(buf, varPos1, PacketIO.UTF8);
         }

         if ((nullBits & 4) != 0) {
            int varPosBase2 = buf.getIntLE(offset + 9);
            if (varPosBase2 < 0 || varPosBase2 > buf.writerIndex() - offset - 13) {
               throw ProtocolException.invalidOffset("LeftHandItemId", varPosBase2, buf.readableBytes());
            }

            int varPos2 = offset + 13 + varPosBase2;
            int leftHandItemIdLen = VarInt.peek(buf, varPos2);
            if (leftHandItemIdLen < 0) {
               throw ProtocolException.invalidVarInt("LeftHandItemId");
            }

            int leftHandItemIdVarIntLen = VarInt.size(leftHandItemIdLen);
            if (leftHandItemIdLen > 4096000) {
               throw ProtocolException.stringTooLong("LeftHandItemId", leftHandItemIdLen, 4096000);
            }

            if (varPos2 + leftHandItemIdVarIntLen + leftHandItemIdLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("LeftHandItemId", varPos2 + leftHandItemIdVarIntLen + leftHandItemIdLen, buf.readableBytes());
            }

            obj.leftHandItemId = PacketIO.readVarString(buf, varPos2, PacketIO.UTF8);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 13;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 1);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 13) {
            throw ProtocolException.invalidOffset("ArmorIds", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 13 + fieldOffset0;
         int arrLen = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            int sl = VarInt.peek(buf, pos0);
            pos0 += VarInt.size(sl) + sl;
         }

         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 5);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 13) {
            throw ProtocolException.invalidOffset("RightHandItemId", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 13 + fieldOffset1;
         int sl = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(sl) + sl;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 9);
         if (fieldOffset2 < 0 || fieldOffset2 > buf.writerIndex() - offset - 13) {
            throw ProtocolException.invalidOffset("LeftHandItemId", fieldOffset2, maxEnd);
         }

         int pos2 = offset + 13 + fieldOffset2;
         int sl = VarInt.peek(buf, pos2);
         pos2 += VarInt.size(sl) + sl;
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 13L;
   }

   @Nullable
   public static String[] getArmorIds(MemorySegment mem) {
      return getArmorIds(mem, 0);
   }

   @Nullable
   public static String[] getArmorIds(MemorySegment mem, int offset) {
      if (!hasArmorIds(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 1, 13, "ArmorIds");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("ArmorIds", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("ArmorIds", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("ArmorIds", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               String[] data = new String[len];

               for (int i = 0; i < len; i++) {
                  long sp = VarInt.getWithLength(mem, off);
                  int n = (int)sp + (int)(sp >>> 32);
                  data[i] = PacketIO.readVarString("ArmorIds", mem, off, 16384000, PacketIO.UTF8);
                  off += n;
               }

               return data;
            }
         }
      }
   }

   @Nullable
   public static String getRightHandItemId(MemorySegment mem) {
      return getRightHandItemId(mem, 0);
   }

   @Nullable
   public static String getRightHandItemId(MemorySegment mem, int offset) {
      return hasRightHandItemId(mem, offset)
         ? PacketIO.readVarString("RightHandItemId", mem, offset + getValidatedOffset(mem, offset, 5, 13, "RightHandItemId"), 4096000, PacketIO.UTF8)
         : null;
   }

   @Nullable
   public static String getLeftHandItemId(MemorySegment mem) {
      return getLeftHandItemId(mem, 0);
   }

   @Nullable
   public static String getLeftHandItemId(MemorySegment mem, int offset) {
      return hasLeftHandItemId(mem, offset)
         ? PacketIO.readVarString("LeftHandItemId", mem, offset + getValidatedOffset(mem, offset, 9, 13, "LeftHandItemId"), 4096000, PacketIO.UTF8)
         : null;
   }

   public static boolean hasArmorIds(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasRightHandItemId(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasLeftHandItemId(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 4) != 0;
   }

   private static int getValidatedOffset(MemorySegment buffer, int base, int slotPosition, int varBlockStart, String fieldName) {
      int offset = buffer.get(PacketIO.PROTO_INT, (long)(base + slotPosition));
      if (offset >= 0 && offset <= buffer.byteSize() - base - varBlockStart) {
         return varBlockStart + offset;
      } else {
         throw ProtocolException.invalidOffset(fieldName, offset, (int)buffer.byteSize());
      }
   }

   public static EquipmentUpdate toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static EquipmentUpdate toObject(MemorySegment mem, int offset) {
      if (offset + 13 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("EquipmentUpdate", offset + 13, (int)mem.byteSize());
      } else {
         String[] armorIds = null;
         if (hasArmorIds(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 1, 13, "ArmorIds");
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("ArmorIds", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("ArmorIds", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("ArmorIds", off + lenOffset + len, (int)mem.byteSize());
            }

            off += lenOffset;
            armorIds = new String[len];

            for (int i = 0; i < len; i++) {
               long sp = VarInt.getWithLength(mem, off);
               int n = (int)sp + (int)(sp >>> 32);
               armorIds[i] = PacketIO.readVarString("ArmorIds", mem, off, 16384000, PacketIO.UTF8);
               off += n;
            }
         }

         return new EquipmentUpdate(
            armorIds,
            hasRightHandItemId(mem, offset)
               ? PacketIO.readVarString("RightHandItemId", mem, offset + getValidatedOffset(mem, offset, 5, 13, "RightHandItemId"), 4096000, PacketIO.UTF8)
               : null,
            hasLeftHandItemId(mem, offset)
               ? PacketIO.readVarString("LeftHandItemId", mem, offset + getValidatedOffset(mem, offset, 9, 13, "LeftHandItemId"), 4096000, PacketIO.UTF8)
               : null
         );
      }
   }

   @Override
   public int serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.armorIds != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.rightHandItemId != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.leftHandItemId != null) {
         nullBits = (byte)(nullBits | 4);
      }

      buf.writeByte(nullBits);
      int armorIdsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int rightHandItemIdOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int leftHandItemIdOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.armorIds != null) {
         buf.setIntLE(armorIdsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.armorIds.length > 4096000) {
            throw ProtocolException.arrayTooLong("ArmorIds", this.armorIds.length, 4096000);
         }

         VarInt.write(buf, this.armorIds.length);

         for (String item : this.armorIds) {
            PacketIO.writeVarString(buf, item, 4096000);
         }
      } else {
         buf.setIntLE(armorIdsOffsetSlot, -1);
      }

      if (this.rightHandItemId != null) {
         buf.setIntLE(rightHandItemIdOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.rightHandItemId, 4096000);
      } else {
         buf.setIntLE(rightHandItemIdOffsetSlot, -1);
      }

      if (this.leftHandItemId != null) {
         buf.setIntLE(leftHandItemIdOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.leftHandItemId, 4096000);
      } else {
         buf.setIntLE(leftHandItemIdOffsetSlot, -1);
      }

      return buf.writerIndex() - startPos;
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.armorIds != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.rightHandItemId != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.leftHandItemId != null) {
         nullBits = (byte)(nullBits | 4);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      int varOffset = offset + 13;
      if (this.armorIds != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 1), varOffset - offset - 13);
         if (this.armorIds.length > 4096000) {
            throw ProtocolException.arrayTooLong("ArmorIds", this.armorIds.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.armorIds.length);
         int armorIdsValueOffset = 0;

         for (int i = 0; i < this.armorIds.length; i++) {
            armorIdsValueOffset += PacketIO.writeVarString(mem, varOffset + armorIdsValueOffset, this.armorIds[i], 16384000);
         }

         varOffset += armorIdsValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 1), -1);
      }

      if (this.rightHandItemId != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), varOffset - offset - 13);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.rightHandItemId, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), -1);
      }

      if (this.leftHandItemId != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 9), varOffset - offset - 13);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.leftHandItemId, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 9), -1);
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 13;
      if (this.armorIds != null) {
         int armorIdsSize = 0;

         for (String elem : this.armorIds) {
            armorIdsSize += PacketIO.stringSize(elem);
         }

         size += VarInt.size(this.armorIds.length) + armorIdsSize;
      }

      if (this.rightHandItemId != null) {
         size += PacketIO.stringSize(this.rightHandItemId);
      }

      if (this.leftHandItemId != null) {
         size += PacketIO.stringSize(this.leftHandItemId);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 13) {
         return ValidationResult.error("Buffer too small: expected at least 13 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int armorIdsOffset = buffer.getIntLE(offset + 1);
            if (armorIdsOffset < 0 || armorIdsOffset > buffer.writerIndex() - offset - 13) {
               return ValidationResult.error("Invalid offset for ArmorIds");
            }

            int pos = offset + 13 + armorIdsOffset;
            int armorIdsCount = VarInt.peek(buffer, pos);
            if (armorIdsCount < 0) {
               return ValidationResult.error("Invalid array count for ArmorIds");
            }

            if (armorIdsCount > 4096000) {
               return ValidationResult.error("ArmorIds exceeds max length 4096000");
            }

            pos += VarInt.size(armorIdsCount);

            for (int i = 0; i < armorIdsCount; i++) {
               int strLen = VarInt.peek(buffer, pos);
               if (strLen < 0) {
                  return ValidationResult.error("Invalid string length in ArmorIds");
               }

               pos += VarInt.size(strLen);
               pos += strLen;
               if (pos > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading string in ArmorIds");
               }
            }
         }

         if ((nullBits & 2) != 0) {
            int rightHandItemIdOffset = buffer.getIntLE(offset + 5);
            if (rightHandItemIdOffset < 0 || rightHandItemIdOffset > buffer.writerIndex() - offset - 13) {
               return ValidationResult.error("Invalid offset for RightHandItemId");
            }

            int posx = offset + 13 + rightHandItemIdOffset;
            int rightHandItemIdLen = VarInt.peek(buffer, posx);
            if (rightHandItemIdLen < 0) {
               return ValidationResult.error("Invalid string length for RightHandItemId");
            }

            if (rightHandItemIdLen > 4096000) {
               return ValidationResult.error("RightHandItemId exceeds max length 4096000");
            }

            posx += VarInt.size(rightHandItemIdLen);
            posx += rightHandItemIdLen;
            if (posx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading RightHandItemId");
            }
         }

         if ((nullBits & 4) != 0) {
            int leftHandItemIdOffset = buffer.getIntLE(offset + 9);
            if (leftHandItemIdOffset < 0 || leftHandItemIdOffset > buffer.writerIndex() - offset - 13) {
               return ValidationResult.error("Invalid offset for LeftHandItemId");
            }

            int posxx = offset + 13 + leftHandItemIdOffset;
            int leftHandItemIdLen = VarInt.peek(buffer, posxx);
            if (leftHandItemIdLen < 0) {
               return ValidationResult.error("Invalid string length for LeftHandItemId");
            }

            if (leftHandItemIdLen > 4096000) {
               return ValidationResult.error("LeftHandItemId exceeds max length 4096000");
            }

            posxx += VarInt.size(leftHandItemIdLen);
            posxx += leftHandItemIdLen;
            if (posxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading LeftHandItemId");
            }
         }

         return ValidationResult.OK;
      }
   }

   public EquipmentUpdate clone() {
      EquipmentUpdate copy = new EquipmentUpdate();
      copy.armorIds = this.armorIds != null ? Arrays.copyOf(this.armorIds, this.armorIds.length) : null;
      copy.rightHandItemId = this.rightHandItemId;
      copy.leftHandItemId = this.leftHandItemId;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof EquipmentUpdate other)
            ? false
            : Arrays.equals((Object[])this.armorIds, (Object[])other.armorIds)
               && Objects.equals(this.rightHandItemId, other.rightHandItemId)
               && Objects.equals(this.leftHandItemId, other.leftHandItemId);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Arrays.hashCode((Object[])this.armorIds);
      result = 31 * result + Objects.hashCode(this.rightHandItemId);
      return 31 * result + Objects.hashCode(this.leftHandItemId);
   }
}
