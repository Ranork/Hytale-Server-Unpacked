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

public class EntityUpdate {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 5;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 13;
   public static final int MAX_SIZE = 1677721600;
   public int networkId;
   @Nullable
   public ComponentUpdateType[] removed;
   @Nullable
   public ComponentUpdate[] updates;

   public EntityUpdate() {
   }

   public EntityUpdate(int networkId, @Nullable ComponentUpdateType[] removed, @Nullable ComponentUpdate[] updates) {
      this.networkId = networkId;
      this.removed = removed;
      this.updates = updates;
   }

   public EntityUpdate(@Nonnull EntityUpdate other) {
      this.networkId = other.networkId;
      this.removed = other.removed;
      this.updates = other.updates;
   }

   @Nonnull
   public static EntityUpdate deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 13) {
         throw ProtocolException.bufferTooSmall("EntityUpdate", 13, buf.readableBytes() - offset);
      } else {
         EntityUpdate obj = new EntityUpdate();
         byte nullBits = buf.getByte(offset);
         obj.networkId = buf.getIntLE(offset + 1);
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 5);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 13) {
               throw ProtocolException.invalidOffset("Removed", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 13 + varPosBase0;
            int removedCount = VarInt.peek(buf, varPos0);
            if (removedCount < 0) {
               throw ProtocolException.invalidVarInt("Removed");
            }

            int varIntLen = VarInt.size(removedCount);
            if (removedCount > 4096000) {
               throw ProtocolException.arrayTooLong("Removed", removedCount, 4096000);
            }

            if (varPos0 + varIntLen + removedCount * 1L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Removed", varPos0 + varIntLen + removedCount * 1, buf.readableBytes());
            }

            obj.removed = new ComponentUpdateType[removedCount];
            int elemPos = varPos0 + varIntLen;

            for (int i = 0; i < removedCount; i++) {
               obj.removed[i] = ComponentUpdateType.fromValue(buf.getByte(elemPos));
               elemPos++;
            }
         }

         if ((nullBits & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 9);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 13) {
               throw ProtocolException.invalidOffset("Updates", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 13 + varPosBase1;
            int updatesCount = VarInt.peek(buf, varPos1);
            if (updatesCount < 0) {
               throw ProtocolException.invalidVarInt("Updates");
            }

            int varIntLenx = VarInt.size(updatesCount);
            if (updatesCount > 4096000) {
               throw ProtocolException.arrayTooLong("Updates", updatesCount, 4096000);
            }

            if (varPos1 + varIntLenx + updatesCount * 1L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Updates", varPos1 + varIntLenx + updatesCount * 1, buf.readableBytes());
            }

            obj.updates = new ComponentUpdate[updatesCount];
            int elemPos = varPos1 + varIntLenx;

            for (int i = 0; i < updatesCount; i++) {
               obj.updates[i] = ComponentUpdate.deserialize(buf, elemPos);
               elemPos += ComponentUpdate.computeBytesConsumed(buf, elemPos);
            }
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 13;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 5);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 13) {
            throw ProtocolException.invalidOffset("Removed", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 13 + fieldOffset0;
         int arrLen = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(arrLen) + arrLen * 1;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 9);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 13) {
            throw ProtocolException.invalidOffset("Updates", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 13 + fieldOffset1;
         int arrLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            pos1 += ComponentUpdate.computeBytesConsumed(buf, pos1);
         }

         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 13L;
   }

   public static int getNetworkId(MemorySegment mem) {
      return getNetworkId(mem, 0);
   }

   public static int getNetworkId(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 1));
   }

   @Nullable
   public static ComponentUpdateType[] getRemoved(MemorySegment mem) {
      return getRemoved(mem, 0);
   }

   @Nullable
   public static ComponentUpdateType[] getRemoved(MemorySegment mem, int offset) {
      if (!hasRemoved(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 5, 13, "Removed");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Removed", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("Removed", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 1L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Removed", off + lenOffset + len * 1, (int)mem.byteSize());
            } else {
               off += lenOffset;
               ComponentUpdateType[] data = new ComponentUpdateType[len];

               for (int i = 0; i < len; i++) {
                  data[i] = ComponentUpdateType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(off + i * 1)));
               }

               return data;
            }
         }
      }
   }

   @Nullable
   public static ComponentUpdate[] getUpdates(MemorySegment mem) {
      return getUpdates(mem, 0);
   }

   @Nullable
   public static ComponentUpdate[] getUpdates(MemorySegment mem, int offset) {
      if (!hasUpdates(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 9, 13, "Updates");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Updates", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("Updates", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Updates", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               ComponentUpdate[] data = new ComponentUpdate[len];

               for (int i = 0; i < len; i++) {
                  data[i] = ComponentUpdate.toObject(mem, off);
                  off += data[i].computeSizeWithTypeId();
               }

               return data;
            }
         }
      }
   }

   public static boolean hasRemoved(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasUpdates(MemorySegment mem, int offset) {
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

   public static EntityUpdate toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static EntityUpdate toObject(MemorySegment mem, int offset) {
      if (offset + 13 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("EntityUpdate", offset + 13, (int)mem.byteSize());
      } else {
         ComponentUpdateType[] removed = null;
         if (hasRemoved(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 5, 13, "Removed");
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Removed", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("Removed", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 1L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Removed", off + lenOffset + len * 1, (int)mem.byteSize());
            }

            off += lenOffset;
            removed = new ComponentUpdateType[len];

            for (int i = 0; i < len; i++) {
               removed[i] = ComponentUpdateType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(off + i * 1)));
            }
         }

         ComponentUpdate[] updates = null;
         if (hasUpdates(mem, offset)) {
            int offx = offset + getValidatedOffset(mem, offset, 9, 13, "Updates");
            long packedx = VarInt.getWithLength(mem, offx);
            int lenx = (int)packedx;
            if (lenx < 0) {
               throw ProtocolException.negativeLength("Updates", lenx);
            }

            if (lenx > 4096000) {
               throw ProtocolException.arrayTooLong("Updates", lenx, 4096000);
            }

            int lenOffset = (int)(packedx >>> 32);
            if (offx + lenOffset + lenx > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Updates", offx + lenOffset + lenx, (int)mem.byteSize());
            }

            offx += lenOffset;
            updates = new ComponentUpdate[lenx];

            for (int i = 0; i < lenx; i++) {
               updates[i] = ComponentUpdate.toObject(mem, offx);
               offx += updates[i].computeSizeWithTypeId();
            }
         }

         return new EntityUpdate(mem.get(PacketIO.PROTO_INT, (long)(offset + 1)), removed, updates);
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.removed != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.updates != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.networkId);
      int removedOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int updatesOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.removed != null) {
         buf.setIntLE(removedOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.removed.length > 4096000) {
            throw ProtocolException.arrayTooLong("Removed", this.removed.length, 4096000);
         }

         VarInt.write(buf, this.removed.length);

         for (ComponentUpdateType item : this.removed) {
            buf.writeByte(item.getValue());
         }
      } else {
         buf.setIntLE(removedOffsetSlot, -1);
      }

      if (this.updates != null) {
         buf.setIntLE(updatesOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.updates.length > 4096000) {
            throw ProtocolException.arrayTooLong("Updates", this.updates.length, 4096000);
         }

         VarInt.write(buf, this.updates.length);

         for (ComponentUpdate item : this.updates) {
            item.serializeWithTypeId(buf);
         }
      } else {
         buf.setIntLE(updatesOffsetSlot, -1);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.removed != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.updates != null) {
         nullBits = (byte)(nullBits | 2);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 1), this.networkId);
      int varOffset = offset + 13;
      if (this.removed != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), varOffset - offset - 13);
         if (this.removed.length > 4096000) {
            throw ProtocolException.arrayTooLong("Removed", this.removed.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.removed.length);

         for (int i = 0; i < this.removed.length; i++) {
            mem.set(PacketIO.PROTO_BYTE, (long)(varOffset + i * 1), (byte)this.removed[i].getValue());
         }

         varOffset += this.removed.length * 1;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), -1);
      }

      if (this.updates != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 9), varOffset - offset - 13);
         if (this.updates.length > 4096000) {
            throw ProtocolException.arrayTooLong("Updates", this.updates.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.updates.length);
         int updatesValueOffset = 0;

         for (int i = 0; i < this.updates.length; i++) {
            updatesValueOffset += this.updates[i].serializeWithTypeId(mem, varOffset + updatesValueOffset);
         }

         varOffset += updatesValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 9), -1);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 13;
      if (this.removed != null) {
         size += VarInt.size(this.removed.length) + this.removed.length * 1;
      }

      if (this.updates != null) {
         int updatesSize = 0;

         for (ComponentUpdate elem : this.updates) {
            updatesSize += elem.computeSizeWithTypeId();
         }

         size += VarInt.size(this.updates.length) + updatesSize;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 13) {
         return ValidationResult.error("Buffer too small: expected at least 13 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int removedOffset = buffer.getIntLE(offset + 5);
            if (removedOffset < 0 || removedOffset > buffer.writerIndex() - offset - 13) {
               return ValidationResult.error("Invalid offset for Removed");
            }

            int pos = offset + 13 + removedOffset;
            int removedCount = VarInt.peek(buffer, pos);
            if (removedCount < 0) {
               return ValidationResult.error("Invalid array count for Removed");
            }

            if (removedCount > 4096000) {
               return ValidationResult.error("Removed exceeds max length 4096000");
            }

            pos += VarInt.size(removedCount);
            if (pos + removedCount * 1L > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Removed");
            }

            for (int i = 0; i < removedCount; i++) {
               int v = buffer.getByte(pos) & 255;
               if (v >= 26) {
                  return ValidationResult.error("Invalid ComponentUpdateType value for Removed[i]");
               }

               pos++;
            }
         }

         if ((nullBits & 2) != 0) {
            int updatesOffset = buffer.getIntLE(offset + 9);
            if (updatesOffset < 0 || updatesOffset > buffer.writerIndex() - offset - 13) {
               return ValidationResult.error("Invalid offset for Updates");
            }

            int posx = offset + 13 + updatesOffset;
            int updatesCount = VarInt.peek(buffer, posx);
            if (updatesCount < 0) {
               return ValidationResult.error("Invalid array count for Updates");
            }

            if (updatesCount > 4096000) {
               return ValidationResult.error("Updates exceeds max length 4096000");
            }

            posx += VarInt.size(updatesCount);

            for (int i = 0; i < updatesCount; i++) {
               ValidationResult structResult = ComponentUpdate.validateStructure(buffer, posx);
               if (!structResult.isValid()) {
                  return ValidationResult.error("Invalid ComponentUpdate in Updates[" + i + "]: " + structResult.error());
               }

               posx += ComponentUpdate.computeBytesConsumed(buffer, posx);
            }
         }

         return ValidationResult.OK;
      }
   }

   public EntityUpdate clone() {
      EntityUpdate copy = new EntityUpdate();
      copy.networkId = this.networkId;
      copy.removed = this.removed != null ? Arrays.copyOf(this.removed, this.removed.length) : null;
      copy.updates = this.updates != null ? Arrays.copyOf(this.updates, this.updates.length) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof EntityUpdate other)
            ? false
            : this.networkId == other.networkId
               && Arrays.equals((Object[])this.removed, (Object[])other.removed)
               && Arrays.equals((Object[])this.updates, (Object[])other.updates);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Integer.hashCode(this.networkId);
      result = 31 * result + Arrays.hashCode((Object[])this.removed);
      return 31 * result + Arrays.hashCode((Object[])this.updates);
   }
}
