package com.hypixel.hytale.protocol.packets.assets;

import com.hypixel.hytale.protocol.ItemCategory;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.UpdateType;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class UpdateFieldcraftCategories implements Packet, ToClientPacket {
   public static final int PACKET_ID = 58;
   public static final boolean IS_COMPRESSED = true;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 2;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 2;
   public static final int MAX_SIZE = 1677721600;
   @Nonnull
   public UpdateType type = UpdateType.Init;
   @Nullable
   public ItemCategory[] itemCategories;

   @Override
   public int getId() {
      return 58;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public UpdateFieldcraftCategories() {
   }

   public UpdateFieldcraftCategories(@Nonnull UpdateType type, @Nullable ItemCategory[] itemCategories) {
      this.type = type;
      this.itemCategories = itemCategories;
   }

   public UpdateFieldcraftCategories(@Nonnull UpdateFieldcraftCategories other) {
      this.type = other.type;
      this.itemCategories = other.itemCategories;
   }

   @Nonnull
   public static UpdateFieldcraftCategories deserialize(@Nonnull ByteBuf buf, int offset) {
      UpdateFieldcraftCategories obj = new UpdateFieldcraftCategories();
      byte nullBits = buf.getByte(offset);
      obj.type = UpdateType.fromValue(buf.getByte(offset + 1));
      int pos = offset + 2;
      if ((nullBits & 1) != 0) {
         int itemCategoriesCount = VarInt.peek(buf, pos);
         if (itemCategoriesCount < 0) {
            throw ProtocolException.negativeLength("ItemCategories", itemCategoriesCount);
         }

         if (itemCategoriesCount > 4096000) {
            throw ProtocolException.arrayTooLong("ItemCategories", itemCategoriesCount, 4096000);
         }

         int itemCategoriesVarLen = VarInt.size(itemCategoriesCount);
         if (pos + itemCategoriesVarLen + itemCategoriesCount * 6L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("ItemCategories", pos + itemCategoriesVarLen + itemCategoriesCount * 6, buf.readableBytes());
         }

         pos += itemCategoriesVarLen;
         obj.itemCategories = new ItemCategory[itemCategoriesCount];

         for (int i = 0; i < itemCategoriesCount; i++) {
            obj.itemCategories[i] = ItemCategory.deserialize(buf, pos);
            pos += ItemCategory.computeBytesConsumed(buf, pos);
         }
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 2;
      if ((nullBits & 1) != 0) {
         int arrLen = VarInt.peek(buf, pos);
         pos += VarInt.length(buf, pos);

         for (int i = 0; i < arrLen; i++) {
            pos += ItemCategory.computeBytesConsumed(buf, pos);
         }
      }

      return pos - offset;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.itemCategories != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.type.getValue());
      if (this.itemCategories != null) {
         if (this.itemCategories.length > 4096000) {
            throw ProtocolException.arrayTooLong("ItemCategories", this.itemCategories.length, 4096000);
         }

         VarInt.write(buf, this.itemCategories.length);

         for (ItemCategory item : this.itemCategories) {
            item.serialize(buf);
         }
      }
   }

   @Override
   public int computeSize() {
      int size = 2;
      if (this.itemCategories != null) {
         int itemCategoriesSize = 0;

         for (ItemCategory elem : this.itemCategories) {
            itemCategoriesSize += elem.computeSize();
         }

         size += VarInt.size(this.itemCategories.length) + itemCategoriesSize;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 2) {
         return ValidationResult.error("Buffer too small: expected at least 2 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 2;
         if ((nullBits & 1) != 0) {
            int itemCategoriesCount = VarInt.peek(buffer, pos);
            if (itemCategoriesCount < 0) {
               return ValidationResult.error("Invalid array count for ItemCategories");
            }

            if (itemCategoriesCount > 4096000) {
               return ValidationResult.error("ItemCategories exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);

            for (int i = 0; i < itemCategoriesCount; i++) {
               ValidationResult structResult = ItemCategory.validateStructure(buffer, pos);
               if (!structResult.isValid()) {
                  return ValidationResult.error("Invalid ItemCategory in ItemCategories[" + i + "]: " + structResult.error());
               }

               pos += ItemCategory.computeBytesConsumed(buffer, pos);
            }
         }

         return ValidationResult.OK;
      }
   }

   public UpdateFieldcraftCategories clone() {
      UpdateFieldcraftCategories copy = new UpdateFieldcraftCategories();
      copy.type = this.type;
      copy.itemCategories = this.itemCategories != null ? Arrays.stream(this.itemCategories).map(e -> e.clone()).toArray(ItemCategory[]::new) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof UpdateFieldcraftCategories other)
            ? false
            : Objects.equals(this.type, other.type) && Arrays.equals((Object[])this.itemCategories, (Object[])other.itemCategories);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.type);
      return 31 * result + Arrays.hashCode((Object[])this.itemCategories);
   }
}
