package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
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
      MaterialQuantity obj = new MaterialQuantity();
      byte nullBits = buf.getByte(offset);
      obj.itemTag = buf.getIntLE(offset + 1);
      obj.quantity = buf.getIntLE(offset + 5);
      if ((nullBits & 1) != 0) {
         int varPos0 = offset + 17 + buf.getIntLE(offset + 9);
         int itemIdLen = VarInt.peek(buf, varPos0);
         if (itemIdLen < 0) {
            throw ProtocolException.negativeLength("ItemId", itemIdLen);
         }

         if (itemIdLen > 4096000) {
            throw ProtocolException.stringTooLong("ItemId", itemIdLen, 4096000);
         }

         obj.itemId = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
      }

      if ((nullBits & 2) != 0) {
         int varPos1 = offset + 17 + buf.getIntLE(offset + 13);
         int resourceTypeIdLen = VarInt.peek(buf, varPos1);
         if (resourceTypeIdLen < 0) {
            throw ProtocolException.negativeLength("ResourceTypeId", resourceTypeIdLen);
         }

         if (resourceTypeIdLen > 4096000) {
            throw ProtocolException.stringTooLong("ResourceTypeId", resourceTypeIdLen, 4096000);
         }

         obj.resourceTypeId = PacketIO.readVarString(buf, varPos1, PacketIO.UTF8);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 17;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 9);
         int pos0 = offset + 17 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 13);
         int pos1 = offset + 17 + fieldOffset1;
         int sl = VarInt.peek(buf, pos1);
         pos1 += VarInt.length(buf, pos1) + sl;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
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
            if (itemIdOffset < 0) {
               return ValidationResult.error("Invalid offset for ItemId");
            }

            int pos = offset + 17 + itemIdOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for ItemId");
            }

            int itemIdLen = VarInt.peek(buffer, pos);
            if (itemIdLen < 0) {
               return ValidationResult.error("Invalid string length for ItemId");
            }

            if (itemIdLen > 4096000) {
               return ValidationResult.error("ItemId exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += itemIdLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading ItemId");
            }
         }

         if ((nullBits & 2) != 0) {
            int resourceTypeIdOffset = buffer.getIntLE(offset + 13);
            if (resourceTypeIdOffset < 0) {
               return ValidationResult.error("Invalid offset for ResourceTypeId");
            }

            int posx = offset + 17 + resourceTypeIdOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for ResourceTypeId");
            }

            int resourceTypeIdLen = VarInt.peek(buffer, posx);
            if (resourceTypeIdLen < 0) {
               return ValidationResult.error("Invalid string length for ResourceTypeId");
            }

            if (resourceTypeIdLen > 4096000) {
               return ValidationResult.error("ResourceTypeId exceeds max length 4096000");
            }

            posx += VarInt.length(buffer, posx);
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
