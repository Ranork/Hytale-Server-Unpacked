package com.hypixel.hytale.protocol.packets.assets;

import com.hypixel.hytale.protocol.ItemQuality;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.UpdateType;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class UpdateItemQualities implements Packet, ToClientPacket {
   public static final int PACKET_ID = 55;
   public static final boolean IS_COMPRESSED = true;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 6;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 6;
   public static final int MAX_SIZE = 1677721600;
   @Nonnull
   public UpdateType type = UpdateType.Init;
   public int maxId;
   @Nullable
   public Map<Integer, ItemQuality> itemQualities;

   @Override
   public int getId() {
      return 55;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public UpdateItemQualities() {
   }

   public UpdateItemQualities(@Nonnull UpdateType type, int maxId, @Nullable Map<Integer, ItemQuality> itemQualities) {
      this.type = type;
      this.maxId = maxId;
      this.itemQualities = itemQualities;
   }

   public UpdateItemQualities(@Nonnull UpdateItemQualities other) {
      this.type = other.type;
      this.maxId = other.maxId;
      this.itemQualities = other.itemQualities;
   }

   @Nonnull
   public static UpdateItemQualities deserialize(@Nonnull ByteBuf buf, int offset) {
      UpdateItemQualities obj = new UpdateItemQualities();
      byte nullBits = buf.getByte(offset);
      obj.type = UpdateType.fromValue(buf.getByte(offset + 1));
      obj.maxId = buf.getIntLE(offset + 2);
      int pos = offset + 6;
      if ((nullBits & 1) != 0) {
         int itemQualitiesCount = VarInt.peek(buf, pos);
         if (itemQualitiesCount < 0) {
            throw ProtocolException.negativeLength("ItemQualities", itemQualitiesCount);
         }

         if (itemQualitiesCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("ItemQualities", itemQualitiesCount, 4096000);
         }

         pos += VarInt.size(itemQualitiesCount);
         obj.itemQualities = new HashMap<>(itemQualitiesCount);

         for (int i = 0; i < itemQualitiesCount; i++) {
            int key = buf.getIntLE(pos);
            pos += 4;
            ItemQuality val = ItemQuality.deserialize(buf, pos);
            pos += ItemQuality.computeBytesConsumed(buf, pos);
            if (obj.itemQualities.put(key, val) != null) {
               throw ProtocolException.duplicateKey("itemQualities", key);
            }
         }
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 6;
      if ((nullBits & 1) != 0) {
         int dictLen = VarInt.peek(buf, pos);
         pos += VarInt.length(buf, pos);

         for (int i = 0; i < dictLen; i++) {
            pos += 4;
            pos += ItemQuality.computeBytesConsumed(buf, pos);
         }
      }

      return pos - offset;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.itemQualities != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.type.getValue());
      buf.writeIntLE(this.maxId);
      if (this.itemQualities != null) {
         if (this.itemQualities.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("ItemQualities", this.itemQualities.size(), 4096000);
         }

         VarInt.write(buf, this.itemQualities.size());

         for (Entry<Integer, ItemQuality> e : this.itemQualities.entrySet()) {
            buf.writeIntLE(e.getKey());
            e.getValue().serialize(buf);
         }
      }
   }

   @Override
   public int computeSize() {
      int size = 6;
      if (this.itemQualities != null) {
         int itemQualitiesSize = 0;

         for (Entry<Integer, ItemQuality> kvp : this.itemQualities.entrySet()) {
            itemQualitiesSize += 4 + kvp.getValue().computeSize();
         }

         size += VarInt.size(this.itemQualities.size()) + itemQualitiesSize;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 6) {
         return ValidationResult.error("Buffer too small: expected at least 6 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 6;
         if ((nullBits & 1) != 0) {
            int itemQualitiesCount = VarInt.peek(buffer, pos);
            if (itemQualitiesCount < 0) {
               return ValidationResult.error("Invalid dictionary count for ItemQualities");
            }

            if (itemQualitiesCount > 4096000) {
               return ValidationResult.error("ItemQualities exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);

            for (int i = 0; i < itemQualitiesCount; i++) {
               pos += 4;
               if (pos > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading key");
               }

               pos += ItemQuality.computeBytesConsumed(buffer, pos);
            }
         }

         return ValidationResult.OK;
      }
   }

   public UpdateItemQualities clone() {
      UpdateItemQualities copy = new UpdateItemQualities();
      copy.type = this.type;
      copy.maxId = this.maxId;
      if (this.itemQualities != null) {
         Map<Integer, ItemQuality> m = new HashMap<>();

         for (Entry<Integer, ItemQuality> e : this.itemQualities.entrySet()) {
            m.put(e.getKey(), e.getValue().clone());
         }

         copy.itemQualities = m;
      }

      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof UpdateItemQualities other)
            ? false
            : Objects.equals(this.type, other.type) && this.maxId == other.maxId && Objects.equals(this.itemQualities, other.itemQualities);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.type, this.maxId, this.itemQualities);
   }
}
