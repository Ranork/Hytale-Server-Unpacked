package com.hypixel.hytale.protocol.packets.entities;

import com.hypixel.hytale.protocol.EntityUpdate;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
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
      EntityUpdates obj = new EntityUpdates();
      byte nullBits = buf.getByte(offset);
      if ((nullBits & 1) != 0) {
         int varPos0 = offset + 9 + buf.getIntLE(offset + 1);
         int removedCount = VarInt.peek(buf, varPos0);
         if (removedCount < 0) {
            throw ProtocolException.negativeLength("Removed", removedCount);
         }

         if (removedCount > 4096000) {
            throw ProtocolException.arrayTooLong("Removed", removedCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos0);
         if (varPos0 + varIntLen + removedCount * 4L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("Removed", varPos0 + varIntLen + removedCount * 4, buf.readableBytes());
         }

         obj.removed = new int[removedCount];

         for (int i = 0; i < removedCount; i++) {
            obj.removed[i] = buf.getIntLE(varPos0 + varIntLen + i * 4);
         }
      }

      if ((nullBits & 2) != 0) {
         int varPos1 = offset + 9 + buf.getIntLE(offset + 5);
         int updatesCount = VarInt.peek(buf, varPos1);
         if (updatesCount < 0) {
            throw ProtocolException.negativeLength("Updates", updatesCount);
         }

         if (updatesCount > 4096000) {
            throw ProtocolException.arrayTooLong("Updates", updatesCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos1);
         if (varPos1 + varIntLen + updatesCount * 5L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("Updates", varPos1 + varIntLen + updatesCount * 5, buf.readableBytes());
         }

         obj.updates = new EntityUpdate[updatesCount];
         int elemPos = varPos1 + varIntLen;

         for (int i = 0; i < updatesCount; i++) {
            obj.updates[i] = EntityUpdate.deserialize(buf, elemPos);
            elemPos += EntityUpdate.computeBytesConsumed(buf, elemPos);
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
         pos0 += VarInt.length(buf, pos0) + arrLen * 4;
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
            pos1 += EntityUpdate.computeBytesConsumed(buf, pos1);
         }

         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
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
            if (removedOffset < 0) {
               return ValidationResult.error("Invalid offset for Removed");
            }

            int pos = offset + 9 + removedOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Removed");
            }

            int removedCount = VarInt.peek(buffer, pos);
            if (removedCount < 0) {
               return ValidationResult.error("Invalid array count for Removed");
            }

            if (removedCount > 4096000) {
               return ValidationResult.error("Removed exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += removedCount * 4;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Removed");
            }
         }

         if ((nullBits & 2) != 0) {
            int updatesOffset = buffer.getIntLE(offset + 5);
            if (updatesOffset < 0) {
               return ValidationResult.error("Invalid offset for Updates");
            }

            int posx = offset + 9 + updatesOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Updates");
            }

            int updatesCount = VarInt.peek(buffer, posx);
            if (updatesCount < 0) {
               return ValidationResult.error("Invalid array count for Updates");
            }

            if (updatesCount > 4096000) {
               return ValidationResult.error("Updates exceeds max length 4096000");
            }

            posx += VarInt.length(buffer, posx);

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
