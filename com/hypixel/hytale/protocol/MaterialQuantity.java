package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MaterialQuantity {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 9;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 17;
   public static final int MAX_SIZE = 32768027;
   @Nullable
   public String itemId;
   public int itemTag;
   @Nullable
   public String resourceTypeId;
   public int quantity;

   public MaterialQuantity() {
   }

   public MaterialQuantity(@Nullable String itemId, int itemTag, @Nullable String resourceTypeId, int quantity) {
      this.itemId = itemId;
      this.itemTag = itemTag;
      this.resourceTypeId = resourceTypeId;
      this.quantity = quantity;
   }

   public MaterialQuantity(@Nonnull MaterialQuantity other) {
      this.itemId = other.itemId;
      this.itemTag = other.itemTag;
      this.resourceTypeId = other.resourceTypeId;
      this.quantity = other.quantity;
   }

   @Nonnull
   public static MaterialQuantity deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 17) {
         throw ProtocolException.bufferTooSmall("MaterialQuantity", 17, buf.readableBytes() - offset);
      } else {
         MaterialQuantity obj = new MaterialQuantity();
         byte nullBits = buf.getByte(offset);
         obj.itemTag = buf.getIntLE(offset + 1);
         obj.quantity = buf.getIntLE(offset + 5);
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 9);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 17) {
               throw ProtocolException.invalidOffset("ItemId", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 17 + varPosBase0;
            int itemIdLen = VarInt.peek(buf, varPos0);
            if (itemIdLen < 0) {
               throw ProtocolException.invalidVarInt("ItemId");
            }

            int itemIdVarIntLen = VarInt.size(itemIdLen);
            if (itemIdLen > 4096000) {
               throw ProtocolException.stringTooLong("ItemId", itemIdLen, 4096000);
            }

            if (varPos0 + itemIdVarIntLen + itemIdLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("ItemId", varPos0 + itemIdVarIntLen + itemIdLen, buf.readableBytes());
            }

            obj.itemId = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
         }

         if ((nullBits & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 13);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 17) {
               throw ProtocolException.invalidOffset("ResourceTypeId", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 17 + varPosBase1;
            int resourceTypeIdLen = VarInt.peek(buf, varPos1);
            if (resourceTypeIdLen < 0) {
               throw ProtocolException.invalidVarInt("ResourceTypeId");
            }

            int resourceTypeIdVarIntLen = VarInt.size(resourceTypeIdLen);
            if (resourceTypeIdLen > 4096000) {
               throw ProtocolException.stringTooLong("ResourceTypeId", resourceTypeIdLen, 4096000);
            }

            if (varPos1 + resourceTypeIdVarIntLen + resourceTypeIdLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("ResourceTypeId", varPos1 + resourceTypeIdVarIntLen + resourceTypeIdLen, buf.readableBytes());
            }

            obj.resourceTypeId = PacketIO.readVarString(buf, varPos1, PacketIO.UTF8);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 17;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 9);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 17) {
            throw ProtocolException.invalidOffset("ItemId", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 17 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 13);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 17) {
            throw ProtocolException.invalidOffset("ResourceTypeId", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 17 + fieldOffset1;
         int sl = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(sl) + sl;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 17L;
   }

   @Nullable
   public static String getItemId(MemorySegment mem) {
      return getItemId(mem, 0);
   }

   @Nullable
   public static String getItemId(MemorySegment mem, int offset) {
      return hasItemId(mem, offset)
         ? PacketIO.readVarString("ItemId", mem, offset + getValidatedOffset(mem, offset, 9, 17, "ItemId"), 4096000, PacketIO.UTF8)
         : null;
   }

   public static int getItemTag(MemorySegment mem) {
      return getItemTag(mem, 0);
   }

   public static int getItemTag(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 1));
   }

   @Nullable
   public static String getResourceTypeId(MemorySegment mem) {
      return getResourceTypeId(mem, 0);
   }

   @Nullable
   public static String getResourceTypeId(MemorySegment mem, int offset) {
      return hasResourceTypeId(mem, offset)
         ? PacketIO.readVarString("ResourceTypeId", mem, offset + getValidatedOffset(mem, offset, 13, 17, "ResourceTypeId"), 4096000, PacketIO.UTF8)
         : null;
   }

   public static int getQuantity(MemorySegment mem) {
      return getQuantity(mem, 0);
   }

   public static int getQuantity(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 5));
   }

   public static boolean hasItemId(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasResourceTypeId(MemorySegment mem, int offset) {
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

   public static MaterialQuantity toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static MaterialQuantity toObject(MemorySegment mem, int offset) {
      if (offset + 17 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("MaterialQuantity", offset + 17, (int)mem.byteSize());
      } else {
         return new MaterialQuantity(
            hasItemId(mem, offset)
               ? PacketIO.readVarString("ItemId", mem, offset + getValidatedOffset(mem, offset, 9, 17, "ItemId"), 4096000, PacketIO.UTF8)
               : null,
            mem.get(PacketIO.PROTO_INT, (long)(offset + 1)),
            hasResourceTypeId(mem, offset)
               ? PacketIO.readVarString("ResourceTypeId", mem, offset + getValidatedOffset(mem, offset, 13, 17, "ResourceTypeId"), 4096000, PacketIO.UTF8)
               : null,
            mem.get(PacketIO.PROTO_INT, (long)(offset + 5))
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.itemId != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.resourceTypeId != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.itemTag);
      buf.writeIntLE(this.quantity);
      int itemIdOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int resourceTypeIdOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.itemId != null) {
         buf.setIntLE(itemIdOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.itemId, 4096000);
      } else {
         buf.setIntLE(itemIdOffsetSlot, -1);
      }

      if (this.resourceTypeId != null) {
         buf.setIntLE(resourceTypeIdOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.resourceTypeId, 4096000);
      } else {
         buf.setIntLE(resourceTypeIdOffsetSlot, -1);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.itemId != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.resourceTypeId != null) {
         nullBits = (byte)(nullBits | 2);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 1), this.itemTag);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 5), this.quantity);
      int varOffset = offset + 17;
      if (this.itemId != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 9), varOffset - offset - 17);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.itemId, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 9), -1);
      }

      if (this.resourceTypeId != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 13), varOffset - offset - 17);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.resourceTypeId, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 13), -1);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 17;
      if (this.itemId != null) {
         size += PacketIO.stringSize(this.itemId);
      }

      if (this.resourceTypeId != null) {
         size += PacketIO.stringSize(this.resourceTypeId);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 17) {
         return ValidationResult.error("Buffer too small: expected at least 17 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int itemIdOffset = buffer.getIntLE(offset + 9);
            if (itemIdOffset < 0 || itemIdOffset > buffer.writerIndex() - offset - 17) {
               return ValidationResult.error("Invalid offset for ItemId");
            }

            int pos = offset + 17 + itemIdOffset;
            int itemIdLen = VarInt.peek(buffer, pos);
            if (itemIdLen < 0) {
               return ValidationResult.error("Invalid string length for ItemId");
            }

            if (itemIdLen > 4096000) {
               return ValidationResult.error("ItemId exceeds max length 4096000");
            }

            pos += VarInt.size(itemIdLen);
            pos += itemIdLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading ItemId");
            }
         }

         if ((nullBits & 2) != 0) {
            int resourceTypeIdOffset = buffer.getIntLE(offset + 13);
            if (resourceTypeIdOffset < 0 || resourceTypeIdOffset > buffer.writerIndex() - offset - 17) {
               return ValidationResult.error("Invalid offset for ResourceTypeId");
            }

            int posx = offset + 17 + resourceTypeIdOffset;
            int resourceTypeIdLen = VarInt.peek(buffer, posx);
            if (resourceTypeIdLen < 0) {
               return ValidationResult.error("Invalid string length for ResourceTypeId");
            }

            if (resourceTypeIdLen > 4096000) {
               return ValidationResult.error("ResourceTypeId exceeds max length 4096000");
            }

            posx += VarInt.size(resourceTypeIdLen);
            posx += resourceTypeIdLen;
            if (posx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading ResourceTypeId");
            }
         }

         return ValidationResult.OK;
      }
   }

   public MaterialQuantity clone() {
      MaterialQuantity copy = new MaterialQuantity();
      copy.itemId = this.itemId;
      copy.itemTag = this.itemTag;
      copy.resourceTypeId = this.resourceTypeId;
      copy.quantity = this.quantity;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof MaterialQuantity other)
            ? false
            : Objects.equals(this.itemId, other.itemId)
               && this.itemTag == other.itemTag
               && Objects.equals(this.resourceTypeId, other.resourceTypeId)
               && this.quantity == other.quantity;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.itemId, this.itemTag, this.resourceTypeId, this.quantity);
   }
}
