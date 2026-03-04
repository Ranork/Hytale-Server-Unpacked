package com.hypixel.hytale.protocol.packets.assets;

import com.hypixel.hytale.protocol.ItemSoundSet;
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

public class UpdateItemSoundSets implements Packet, ToClientPacket {
   public static final int PACKET_ID = 43;
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
   public Map<Integer, ItemSoundSet> itemSoundSets;

   @Override
   public int getId() {
      return 43;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public UpdateItemSoundSets() {
   }

   public UpdateItemSoundSets(@Nonnull UpdateType type, int maxId, @Nullable Map<Integer, ItemSoundSet> itemSoundSets) {
      this.type = type;
      this.maxId = maxId;
      this.itemSoundSets = itemSoundSets;
   }

   public UpdateItemSoundSets(@Nonnull UpdateItemSoundSets other) {
      this.type = other.type;
      this.maxId = other.maxId;
      this.itemSoundSets = other.itemSoundSets;
   }

   @Nonnull
   public static UpdateItemSoundSets deserialize(@Nonnull ByteBuf buf, int offset) {
      UpdateItemSoundSets obj = new UpdateItemSoundSets();
      byte nullBits = buf.getByte(offset);
      obj.type = UpdateType.fromValue(buf.getByte(offset + 1));
      obj.maxId = buf.getIntLE(offset + 2);
      int pos = offset + 6;
      if ((nullBits & 1) != 0) {
         int itemSoundSetsCount = VarInt.peek(buf, pos);
         if (itemSoundSetsCount < 0) {
            throw ProtocolException.negativeLength("ItemSoundSets", itemSoundSetsCount);
         }

         if (itemSoundSetsCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("ItemSoundSets", itemSoundSetsCount, 4096000);
         }

         pos += VarInt.size(itemSoundSetsCount);
         obj.itemSoundSets = new HashMap<>(itemSoundSetsCount);

         for (int i = 0; i < itemSoundSetsCount; i++) {
            int key = buf.getIntLE(pos);
            pos += 4;
            ItemSoundSet val = ItemSoundSet.deserialize(buf, pos);
            pos += ItemSoundSet.computeBytesConsumed(buf, pos);
            if (obj.itemSoundSets.put(key, val) != null) {
               throw ProtocolException.duplicateKey("itemSoundSets", key);
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
            pos += ItemSoundSet.computeBytesConsumed(buf, pos);
         }
      }

      return pos - offset;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.itemSoundSets != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.type.getValue());
      buf.writeIntLE(this.maxId);
      if (this.itemSoundSets != null) {
         if (this.itemSoundSets.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("ItemSoundSets", this.itemSoundSets.size(), 4096000);
         }

         VarInt.write(buf, this.itemSoundSets.size());

         for (Entry<Integer, ItemSoundSet> e : this.itemSoundSets.entrySet()) {
            buf.writeIntLE(e.getKey());
            e.getValue().serialize(buf);
         }
      }
   }

   @Override
   public int computeSize() {
      int size = 6;
      if (this.itemSoundSets != null) {
         int itemSoundSetsSize = 0;

         for (Entry<Integer, ItemSoundSet> kvp : this.itemSoundSets.entrySet()) {
            itemSoundSetsSize += 4 + kvp.getValue().computeSize();
         }

         size += VarInt.size(this.itemSoundSets.size()) + itemSoundSetsSize;
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
            int itemSoundSetsCount = VarInt.peek(buffer, pos);
            if (itemSoundSetsCount < 0) {
               return ValidationResult.error("Invalid dictionary count for ItemSoundSets");
            }

            if (itemSoundSetsCount > 4096000) {
               return ValidationResult.error("ItemSoundSets exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);

            for (int i = 0; i < itemSoundSetsCount; i++) {
               pos += 4;
               if (pos > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading key");
               }

               pos += ItemSoundSet.computeBytesConsumed(buffer, pos);
            }
         }

         return ValidationResult.OK;
      }
   }

   public UpdateItemSoundSets clone() {
      UpdateItemSoundSets copy = new UpdateItemSoundSets();
      copy.type = this.type;
      copy.maxId = this.maxId;
      if (this.itemSoundSets != null) {
         Map<Integer, ItemSoundSet> m = new HashMap<>();

         for (Entry<Integer, ItemSoundSet> e : this.itemSoundSets.entrySet()) {
            m.put(e.getKey(), e.getValue().clone());
         }

         copy.itemSoundSets = m;
      }

      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof UpdateItemSoundSets other)
            ? false
            : Objects.equals(this.type, other.type) && this.maxId == other.maxId && Objects.equals(this.itemSoundSets, other.itemSoundSets);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.type, this.maxId, this.itemSoundSets);
   }
}
