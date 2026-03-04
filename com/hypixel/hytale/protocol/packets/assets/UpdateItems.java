package com.hypixel.hytale.protocol.packets.assets;

import com.hypixel.hytale.protocol.ItemBase;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.UpdateType;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class UpdateItems implements Packet, ToClientPacket {
   public static final int PACKET_ID = 54;
   public static final boolean IS_COMPRESSED = true;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 4;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 12;
   public static final int MAX_SIZE = 1677721600;
   @Nonnull
   public UpdateType type = UpdateType.Init;
   @Nullable
   public Map<String, ItemBase> items;
   @Nullable
   public String[] removedItems;
   public boolean updateModels;
   public boolean updateIcons;

   @Override
   public int getId() {
      return 54;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public UpdateItems() {
   }

   public UpdateItems(
      @Nonnull UpdateType type, @Nullable Map<String, ItemBase> items, @Nullable String[] removedItems, boolean updateModels, boolean updateIcons
   ) {
      this.type = type;
      this.items = items;
      this.removedItems = removedItems;
      this.updateModels = updateModels;
      this.updateIcons = updateIcons;
   }

   public UpdateItems(@Nonnull UpdateItems other) {
      this.type = other.type;
      this.items = other.items;
      this.removedItems = other.removedItems;
      this.updateModels = other.updateModels;
      this.updateIcons = other.updateIcons;
   }

   @Nonnull
   public static UpdateItems deserialize(@Nonnull ByteBuf buf, int offset) {
      UpdateItems obj = new UpdateItems();
      byte nullBits = buf.getByte(offset);
      obj.type = UpdateType.fromValue(buf.getByte(offset + 1));
      obj.updateModels = buf.getByte(offset + 2) != 0;
      obj.updateIcons = buf.getByte(offset + 3) != 0;
      if ((nullBits & 1) != 0) {
         int varPos0 = offset + 12 + buf.getIntLE(offset + 4);
         int itemsCount = VarInt.peek(buf, varPos0);
         if (itemsCount < 0) {
            throw ProtocolException.negativeLength("Items", itemsCount);
         }

         if (itemsCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Items", itemsCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos0);
         obj.items = new HashMap<>(itemsCount);
         int dictPos = varPos0 + varIntLen;

         for (int i = 0; i < itemsCount; i++) {
            int keyLen = VarInt.peek(buf, dictPos);
            if (keyLen < 0) {
               throw ProtocolException.negativeLength("key", keyLen);
            }

            if (keyLen > 4096000) {
               throw ProtocolException.stringTooLong("key", keyLen, 4096000);
            }

            int keyVarLen = VarInt.length(buf, dictPos);
            String key = PacketIO.readVarString(buf, dictPos);
            dictPos += keyVarLen + keyLen;
            ItemBase val = ItemBase.deserialize(buf, dictPos);
            dictPos += ItemBase.computeBytesConsumed(buf, dictPos);
            if (obj.items.put(key, val) != null) {
               throw ProtocolException.duplicateKey("items", key);
            }
         }
      }

      if ((nullBits & 2) != 0) {
         int varPos1 = offset + 12 + buf.getIntLE(offset + 8);
         int removedItemsCount = VarInt.peek(buf, varPos1);
         if (removedItemsCount < 0) {
            throw ProtocolException.negativeLength("RemovedItems", removedItemsCount);
         }

         if (removedItemsCount > 4096000) {
            throw ProtocolException.arrayTooLong("RemovedItems", removedItemsCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos1);
         if (varPos1 + varIntLen + removedItemsCount * 1L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("RemovedItems", varPos1 + varIntLen + removedItemsCount * 1, buf.readableBytes());
         }

         obj.removedItems = new String[removedItemsCount];
         int elemPos = varPos1 + varIntLen;

         for (int i = 0; i < removedItemsCount; i++) {
            int strLen = VarInt.peek(buf, elemPos);
            if (strLen < 0) {
               throw ProtocolException.negativeLength("removedItems[" + i + "]", strLen);
            }

            if (strLen > 4096000) {
               throw ProtocolException.stringTooLong("removedItems[" + i + "]", strLen, 4096000);
            }

            int strVarLen = VarInt.length(buf, elemPos);
            obj.removedItems[i] = PacketIO.readVarString(buf, elemPos);
            elemPos += strVarLen + strLen;
         }
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 12;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 4);
         int pos0 = offset + 12 + fieldOffset0;
         int dictLen = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0);

         for (int i = 0; i < dictLen; i++) {
            int sl = VarInt.peek(buf, pos0);
            pos0 += VarInt.length(buf, pos0) + sl;
            pos0 += ItemBase.computeBytesConsumed(buf, pos0);
         }

         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 8);
         int pos1 = offset + 12 + fieldOffset1;
         int arrLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.length(buf, pos1);

         for (int i = 0; i < arrLen; i++) {
            int sl = VarInt.peek(buf, pos1);
            pos1 += VarInt.length(buf, pos1) + sl;
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
      if (this.items != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.removedItems != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.type.getValue());
      buf.writeByte(this.updateModels ? 1 : 0);
      buf.writeByte(this.updateIcons ? 1 : 0);
      int itemsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int removedItemsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.items != null) {
         buf.setIntLE(itemsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.items.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Items", this.items.size(), 4096000);
         }

         VarInt.write(buf, this.items.size());

         for (Entry<String, ItemBase> e : this.items.entrySet()) {
            PacketIO.writeVarString(buf, e.getKey(), 4096000);
            e.getValue().serialize(buf);
         }
      } else {
         buf.setIntLE(itemsOffsetSlot, -1);
      }

      if (this.removedItems != null) {
         buf.setIntLE(removedItemsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.removedItems.length > 4096000) {
            throw ProtocolException.arrayTooLong("RemovedItems", this.removedItems.length, 4096000);
         }

         VarInt.write(buf, this.removedItems.length);

         for (String item : this.removedItems) {
            PacketIO.writeVarString(buf, item, 4096000);
         }
      } else {
         buf.setIntLE(removedItemsOffsetSlot, -1);
      }
   }

   @Override
   public int computeSize() {
      int size = 12;
      if (this.items != null) {
         int itemsSize = 0;

         for (Entry<String, ItemBase> kvp : this.items.entrySet()) {
            itemsSize += PacketIO.stringSize(kvp.getKey()) + kvp.getValue().computeSize();
         }

         size += VarInt.size(this.items.size()) + itemsSize;
      }

      if (this.removedItems != null) {
         int removedItemsSize = 0;

         for (String elem : this.removedItems) {
            removedItemsSize += PacketIO.stringSize(elem);
         }

         size += VarInt.size(this.removedItems.length) + removedItemsSize;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 12) {
         return ValidationResult.error("Buffer too small: expected at least 12 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int itemsOffset = buffer.getIntLE(offset + 4);
            if (itemsOffset < 0) {
               return ValidationResult.error("Invalid offset for Items");
            }

            int pos = offset + 12 + itemsOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Items");
            }

            int itemsCount = VarInt.peek(buffer, pos);
            if (itemsCount < 0) {
               return ValidationResult.error("Invalid dictionary count for Items");
            }

            if (itemsCount > 4096000) {
               return ValidationResult.error("Items exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);

            for (int i = 0; i < itemsCount; i++) {
               int keyLen = VarInt.peek(buffer, pos);
               if (keyLen < 0) {
                  return ValidationResult.error("Invalid string length for key");
               }

               if (keyLen > 4096000) {
                  return ValidationResult.error("key exceeds max length 4096000");
               }

               pos += VarInt.length(buffer, pos);
               pos += keyLen;
               if (pos > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading key");
               }

               pos += ItemBase.computeBytesConsumed(buffer, pos);
            }
         }

         if ((nullBits & 2) != 0) {
            int removedItemsOffset = buffer.getIntLE(offset + 8);
            if (removedItemsOffset < 0) {
               return ValidationResult.error("Invalid offset for RemovedItems");
            }

            int posx = offset + 12 + removedItemsOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for RemovedItems");
            }

            int removedItemsCount = VarInt.peek(buffer, posx);
            if (removedItemsCount < 0) {
               return ValidationResult.error("Invalid array count for RemovedItems");
            }

            if (removedItemsCount > 4096000) {
               return ValidationResult.error("RemovedItems exceeds max length 4096000");
            }

            posx += VarInt.length(buffer, posx);

            for (int i = 0; i < removedItemsCount; i++) {
               int strLen = VarInt.peek(buffer, posx);
               if (strLen < 0) {
                  return ValidationResult.error("Invalid string length in RemovedItems");
               }

               posx += VarInt.length(buffer, posx);
               posx += strLen;
               if (posx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading string in RemovedItems");
               }
            }
         }

         return ValidationResult.OK;
      }
   }

   public UpdateItems clone() {
      UpdateItems copy = new UpdateItems();
      copy.type = this.type;
      if (this.items != null) {
         Map<String, ItemBase> m = new HashMap<>();

         for (Entry<String, ItemBase> e : this.items.entrySet()) {
            m.put(e.getKey(), e.getValue().clone());
         }

         copy.items = m;
      }

      copy.removedItems = this.removedItems != null ? Arrays.copyOf(this.removedItems, this.removedItems.length) : null;
      copy.updateModels = this.updateModels;
      copy.updateIcons = this.updateIcons;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof UpdateItems other)
            ? false
            : Objects.equals(this.type, other.type)
               && Objects.equals(this.items, other.items)
               && Arrays.equals((Object[])this.removedItems, (Object[])other.removedItems)
               && this.updateModels == other.updateModels
               && this.updateIcons == other.updateIcons;
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.type);
      result = 31 * result + Objects.hashCode(this.items);
      result = 31 * result + Arrays.hashCode((Object[])this.removedItems);
      result = 31 * result + Boolean.hashCode(this.updateModels);
      return 31 * result + Boolean.hashCode(this.updateIcons);
   }
}
