package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemWithAllMetadata {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 22;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 30;
   public static final int MAX_SIZE = 32768040;
   @Nonnull
   public String itemId = "";
   public int quantity;
   public double durability;
   public double maxDurability;
   public boolean overrideDroppedItemAnimation;
   @Nullable
   public String metadata;

   public ItemWithAllMetadata() {
   }

   public ItemWithAllMetadata(
      @Nonnull String itemId, int quantity, double durability, double maxDurability, boolean overrideDroppedItemAnimation, @Nullable String metadata
   ) {
      this.itemId = itemId;
      this.quantity = quantity;
      this.durability = durability;
      this.maxDurability = maxDurability;
      this.overrideDroppedItemAnimation = overrideDroppedItemAnimation;
      this.metadata = metadata;
   }

   public ItemWithAllMetadata(@Nonnull ItemWithAllMetadata other) {
      this.itemId = other.itemId;
      this.quantity = other.quantity;
      this.durability = other.durability;
      this.maxDurability = other.maxDurability;
      this.overrideDroppedItemAnimation = other.overrideDroppedItemAnimation;
      this.metadata = other.metadata;
   }

   @Nonnull
   public static ItemWithAllMetadata deserialize(@Nonnull ByteBuf buf, int offset) {
      ItemWithAllMetadata obj = new ItemWithAllMetadata();
      byte nullBits = buf.getByte(offset);
      obj.quantity = buf.getIntLE(offset + 1);
      obj.durability = buf.getDoubleLE(offset + 5);
      obj.maxDurability = buf.getDoubleLE(offset + 13);
      obj.overrideDroppedItemAnimation = buf.getByte(offset + 21) != 0;
      int varPos0 = offset + 30 + buf.getIntLE(offset + 22);
      int itemIdLen = VarInt.peek(buf, varPos0);
      if (itemIdLen < 0) {
         throw ProtocolException.negativeLength("ItemId", itemIdLen);
      } else if (itemIdLen > 4096000) {
         throw ProtocolException.stringTooLong("ItemId", itemIdLen, 4096000);
      } else {
         obj.itemId = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
         if ((nullBits & 1) != 0) {
            varPos0 = offset + 30 + buf.getIntLE(offset + 26);
            itemIdLen = VarInt.peek(buf, varPos0);
            if (itemIdLen < 0) {
               throw ProtocolException.negativeLength("Metadata", itemIdLen);
            }

            if (itemIdLen > 4096000) {
               throw ProtocolException.stringTooLong("Metadata", itemIdLen, 4096000);
            }

            obj.metadata = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 30;
      int fieldOffset0 = buf.getIntLE(offset + 22);
      int pos0 = offset + 30 + fieldOffset0;
      int sl = VarInt.peek(buf, pos0);
      pos0 += VarInt.length(buf, pos0) + sl;
      if (pos0 - offset > maxEnd) {
         maxEnd = pos0 - offset;
      }

      if ((nullBits & 1) != 0) {
         fieldOffset0 = buf.getIntLE(offset + 26);
         pos0 = offset + 30 + fieldOffset0;
         sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      return maxEnd;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.metadata != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.quantity);
      buf.writeDoubleLE(this.durability);
      buf.writeDoubleLE(this.maxDurability);
      buf.writeByte(this.overrideDroppedItemAnimation ? 1 : 0);
      int itemIdOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int metadataOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      buf.setIntLE(itemIdOffsetSlot, buf.writerIndex() - varBlockStart);
      PacketIO.writeVarString(buf, this.itemId, 4096000);
      if (this.metadata != null) {
         buf.setIntLE(metadataOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.metadata, 4096000);
      } else {
         buf.setIntLE(metadataOffsetSlot, -1);
      }
   }

   public int computeSize() {
      int size = 30;
      size += PacketIO.stringSize(this.itemId);
      if (this.metadata != null) {
         size += PacketIO.stringSize(this.metadata);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 30) {
         return ValidationResult.error("Buffer too small: expected at least 30 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int itemIdOffset = buffer.getIntLE(offset + 22);
         if (itemIdOffset < 0) {
            return ValidationResult.error("Invalid offset for ItemId");
         } else {
            int pos = offset + 30 + itemIdOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for ItemId");
            } else {
               int itemIdLen = VarInt.peek(buffer, pos);
               if (itemIdLen < 0) {
                  return ValidationResult.error("Invalid string length for ItemId");
               } else if (itemIdLen > 4096000) {
                  return ValidationResult.error("ItemId exceeds max length 4096000");
               } else {
                  pos += VarInt.length(buffer, pos);
                  pos += itemIdLen;
                  if (pos > buffer.writerIndex()) {
                     return ValidationResult.error("Buffer overflow reading ItemId");
                  } else {
                     if ((nullBits & 1) != 0) {
                        itemIdOffset = buffer.getIntLE(offset + 26);
                        if (itemIdOffset < 0) {
                           return ValidationResult.error("Invalid offset for Metadata");
                        }

                        pos = offset + 30 + itemIdOffset;
                        if (pos >= buffer.writerIndex()) {
                           return ValidationResult.error("Offset out of bounds for Metadata");
                        }

                        itemIdLen = VarInt.peek(buffer, pos);
                        if (itemIdLen < 0) {
                           return ValidationResult.error("Invalid string length for Metadata");
                        }

                        if (itemIdLen > 4096000) {
                           return ValidationResult.error("Metadata exceeds max length 4096000");
                        }

                        pos += VarInt.length(buffer, pos);
                        pos += itemIdLen;
                        if (pos > buffer.writerIndex()) {
                           return ValidationResult.error("Buffer overflow reading Metadata");
                        }
                     }

                     return ValidationResult.OK;
                  }
               }
            }
         }
      }
   }

   public ItemWithAllMetadata clone() {
      ItemWithAllMetadata copy = new ItemWithAllMetadata();
      copy.itemId = this.itemId;
      copy.quantity = this.quantity;
      copy.durability = this.durability;
      copy.maxDurability = this.maxDurability;
      copy.overrideDroppedItemAnimation = this.overrideDroppedItemAnimation;
      copy.metadata = this.metadata;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ItemWithAllMetadata other)
            ? false
            : Objects.equals(this.itemId, other.itemId)
               && this.quantity == other.quantity
               && this.durability == other.durability
               && this.maxDurability == other.maxDurability
               && this.overrideDroppedItemAnimation == other.overrideDroppedItemAnimation
               && Objects.equals(this.metadata, other.metadata);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.itemId, this.quantity, this.durability, this.maxDurability, this.overrideDroppedItemAnimation, this.metadata);
   }
}
