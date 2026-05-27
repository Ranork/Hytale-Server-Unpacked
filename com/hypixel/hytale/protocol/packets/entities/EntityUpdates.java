package com.hypixel.hytale.protocol.packets.entities;

import com.hypixel.hytale.protocol.EntityUpdate;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EntityUpdates implements Packet, ToClientPacket {
   public static final int PACKET_ID = 161;
   public static final boolean IS_COMPRESSED = true;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 9;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public int[] removed;
   @Nullable
   public EntityUpdate[] updates;

   @Override
   public int getId() {
      return 161;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public EntityUpdates() {
   }

   public EntityUpdates(@Nullable int[] removed, @Nullable EntityUpdate[] updates) {
      this.removed = removed;
      this.updates = updates;
   }

   public EntityUpdates(@Nonnull EntityUpdates other) {
      this.removed = other.removed;
      this.updates = other.updates;
   }

   @Nonnull
   public static EntityUpdates deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 9) {
         throw ProtocolException.bufferTooSmall("EntityUpdates", 9, buf.readableBytes() - offset);
      } else {
         EntityUpdates obj = new EntityUpdates();
         byte nullBits = buf.getByte(offset);
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 1);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 9) {
               throw ProtocolException.invalidOffset("Removed", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 9 + varPosBase0;
            int removedCount = VarInt.peek(buf, varPos0);
            if (removedCount < 0) {
               throw ProtocolException.invalidVarInt("Removed");
            }

            int varIntLen = VarInt.size(removedCount);
            if (removedCount > 4096000) {
               throw ProtocolException.arrayTooLong("Removed", removedCount, 4096000);
            }

            if (varPos0 + varIntLen + removedCount * 4L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Removed", varPos0 + varIntLen + removedCount * 4, buf.readableBytes());
            }

            obj.removed = new int[removedCount];

            for (int i = 0; i < removedCount; i++) {
               obj.removed[i] = buf.getIntLE(varPos0 + varIntLen + i * 4);
            }
         }

         if ((nullBits & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 5);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 9) {
               throw ProtocolException.invalidOffset("Updates", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 9 + varPosBase1;
            int updatesCount = VarInt.peek(buf, varPos1);
            if (updatesCount < 0) {
               throw ProtocolException.invalidVarInt("Updates");
            }

            int varIntLenx = VarInt.size(updatesCount);
            if (updatesCount > 4096000) {
               throw ProtocolException.arrayTooLong("Updates", updatesCount, 4096000);
            }

            if (varPos1 + varIntLenx + updatesCount * 5L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Updates", varPos1 + varIntLenx + updatesCount * 5, buf.readableBytes());
            }

            obj.updates = new EntityUpdate[updatesCount];
            int elemPos = varPos1 + varIntLenx;

            for (int i = 0; i < updatesCount; i++) {
               obj.updates[i] = EntityUpdate.deserialize(buf, elemPos);
               elemPos += EntityUpdate.computeBytesConsumed(buf, elemPos);
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
            throw ProtocolException.invalidOffset("Removed", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 9 + fieldOffset0;
         int arrLen = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(arrLen) + arrLen * 4;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 5);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 9) {
            throw ProtocolException.invalidOffset("Updates", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 9 + fieldOffset1;
         int arrLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            pos1 += EntityUpdate.computeBytesConsumed(buf, pos1);
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
   public static int[] getRemoved(MemorySegment mem) {
      return getRemoved(mem, 0);
   }

   @Nullable
   public static int[] getRemoved(MemorySegment mem, int offset) {
      if (!hasRemoved(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 1, 9, "Removed");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Removed", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("Removed", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 4L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Removed", off + lenOffset + len * 4, (int)mem.byteSize());
            } else {
               off += lenOffset;
               int[] data = new int[len];
               MemorySegment.copy(mem, PacketIO.PROTO_INT, off, data, 0, len);
               return data;
            }
         }
      }
   }

   @Nullable
   public static EntityUpdate[] getUpdates(MemorySegment mem) {
      return getUpdates(mem, 0);
   }

   @Nullable
   public static EntityUpdate[] getUpdates(MemorySegment mem, int offset) {
      if (!hasUpdates(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 5, 9, "Updates");
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
               EntityUpdate[] data = new EntityUpdate[len];

               for (int i = 0; i < len; i++) {
                  data[i] = EntityUpdate.toObject(mem, off);
                  off += data[i].computeSize();
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

   public static EntityUpdates toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static EntityUpdates toObject(MemorySegment mem, int offset) {
      if (offset + 9 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("EntityUpdates", offset + 9, (int)mem.byteSize());
      } else {
         int[] removed = null;
         if (hasRemoved(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 1, 9, "Removed");
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Removed", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("Removed", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 4L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Removed", off + lenOffset + len * 4, (int)mem.byteSize());
            }

            off += lenOffset;
            removed = new int[len];
            MemorySegment.copy(mem, PacketIO.PROTO_INT, off, removed, 0, len);
         }

         EntityUpdate[] updates = null;
         if (hasUpdates(mem, offset)) {
            int offx = offset + getValidatedOffset(mem, offset, 5, 9, "Updates");
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
            updates = new EntityUpdate[lenx];

            for (int i = 0; i < lenx; i++) {
               updates[i] = EntityUpdate.toObject(mem, offx);
               offx += updates[i].computeSize();
            }
         }

         return new EntityUpdates(removed, updates);
      }
   }

   @Override
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

         for (int item : this.removed) {
            buf.writeIntLE(item);
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

         for (EntityUpdate item : this.updates) {
            item.serialize(buf);
         }
      } else {
         buf.setIntLE(updatesOffsetSlot, -1);
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.removed != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.updates != null) {
         nullBits = (byte)(nullBits | 2);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      int varOffset = offset + 9;
      if (this.removed != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 1), varOffset - offset - 9);
         if (this.removed.length > 4096000) {
            throw ProtocolException.arrayTooLong("Removed", this.removed.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.removed.length);
         MemorySegment.copy(this.removed, 0, mem, PacketIO.PROTO_INT, varOffset, this.removed.length);
         varOffset += this.removed.length * 4;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 1), -1);
      }

      if (this.updates != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), varOffset - offset - 9);
         if (this.updates.length > 4096000) {
            throw ProtocolException.arrayTooLong("Updates", this.updates.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.updates.length);
         int updatesValueOffset = 0;

         for (int i = 0; i < this.updates.length; i++) {
            updatesValueOffset += this.updates[i].serialize(mem, varOffset + updatesValueOffset);
         }

         varOffset += updatesValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), -1);
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 9;
      if (this.removed != null) {
         size += VarInt.size(this.removed.length) + this.removed.length * 4;
      }

      if (this.updates != null) {
         int updatesSize = 0;

         for (EntityUpdate elem : this.updates) {
            updatesSize += elem.computeSize();
         }

         size += VarInt.size(this.updates.length) + updatesSize;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 9) {
         return ValidationResult.error("Buffer too small: expected at least 9 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int removedOffset = buffer.getIntLE(offset + 1);
            if (removedOffset < 0 || removedOffset > buffer.writerIndex() - offset - 9) {
               return ValidationResult.error("Invalid offset for Removed");
            }

            int pos = offset + 9 + removedOffset;
            int removedCount = VarInt.peek(buffer, pos);
            if (removedCount < 0) {
               return ValidationResult.error("Invalid array count for Removed");
            }

            if (removedCount > 4096000) {
               return ValidationResult.error("Removed exceeds max length 4096000");
            }

            pos += VarInt.size(removedCount);
            pos += removedCount * 4;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Removed");
            }
         }

         if ((nullBits & 2) != 0) {
            int updatesOffset = buffer.getIntLE(offset + 5);
            if (updatesOffset < 0 || updatesOffset > buffer.writerIndex() - offset - 9) {
               return ValidationResult.error("Invalid offset for Updates");
            }

            int posx = offset + 9 + updatesOffset;
            int updatesCount = VarInt.peek(buffer, posx);
            if (updatesCount < 0) {
               return ValidationResult.error("Invalid array count for Updates");
            }

            if (updatesCount > 4096000) {
               return ValidationResult.error("Updates exceeds max length 4096000");
            }

            posx += VarInt.size(updatesCount);

            for (int i = 0; i < updatesCount; i++) {
               ValidationResult structResult = EntityUpdate.validateStructure(buffer, posx);
               if (!structResult.isValid()) {
                  return ValidationResult.error("Invalid EntityUpdate in Updates[" + i + "]: " + structResult.error());
               }

               posx += EntityUpdate.computeBytesConsumed(buffer, posx);
            }
         }

         return ValidationResult.OK;
      }
   }

   public EntityUpdates clone() {
      EntityUpdates copy = new EntityUpdates();
      copy.removed = this.removed != null ? Arrays.copyOf(this.removed, this.removed.length) : null;
      copy.updates = this.updates != null ? Arrays.stream(this.updates).map(e -> e.clone()).toArray(EntityUpdate[]::new) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof EntityUpdates other)
            ? false
            : Arrays.equals(this.removed, other.removed) && Arrays.equals((Object[])this.updates, (Object[])other.updates);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Arrays.hashCode(this.removed);
      return 31 * result + Arrays.hashCode((Object[])this.updates);
   }
}
