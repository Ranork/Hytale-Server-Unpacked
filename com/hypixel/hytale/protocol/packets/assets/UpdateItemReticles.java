package com.hypixel.hytale.protocol.packets.assets;

import com.hypixel.hytale.protocol.ItemReticleConfig;
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

public class UpdateItemReticles implements Packet, ToClientPacket {
   public static final int PACKET_ID = 57;
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
   public Map<Integer, ItemReticleConfig> itemReticleConfigs;

   @Override
   public int getId() {
      return 57;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public UpdateItemReticles() {
   }

   public UpdateItemReticles(@Nonnull UpdateType type, int maxId, @Nullable Map<Integer, ItemReticleConfig> itemReticleConfigs) {
      this.type = type;
      this.maxId = maxId;
      this.itemReticleConfigs = itemReticleConfigs;
   }

   public UpdateItemReticles(@Nonnull UpdateItemReticles other) {
      this.type = other.type;
      this.maxId = other.maxId;
      this.itemReticleConfigs = other.itemReticleConfigs;
   }

   @Nonnull
   public static UpdateItemReticles deserialize(@Nonnull ByteBuf buf, int offset) {
      UpdateItemReticles obj = new UpdateItemReticles();
      byte nullBits = buf.getByte(offset);
      obj.type = UpdateType.fromValue(buf.getByte(offset + 1));
      obj.maxId = buf.getIntLE(offset + 2);
      int pos = offset + 6;
      if ((nullBits & 1) != 0) {
         int itemReticleConfigsCount = VarInt.peek(buf, pos);
         if (itemReticleConfigsCount < 0) {
            throw ProtocolException.negativeLength("ItemReticleConfigs", itemReticleConfigsCount);
         }

         if (itemReticleConfigsCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("ItemReticleConfigs", itemReticleConfigsCount, 4096000);
         }

         pos += VarInt.size(itemReticleConfigsCount);
         obj.itemReticleConfigs = new HashMap<>(itemReticleConfigsCount);

         for (int i = 0; i < itemReticleConfigsCount; i++) {
            int key = buf.getIntLE(pos);
            pos += 4;
            ItemReticleConfig val = ItemReticleConfig.deserialize(buf, pos);
            pos += ItemReticleConfig.computeBytesConsumed(buf, pos);
            if (obj.itemReticleConfigs.put(key, val) != null) {
               throw ProtocolException.duplicateKey("itemReticleConfigs", key);
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
            pos += ItemReticleConfig.computeBytesConsumed(buf, pos);
         }
      }

      return pos - offset;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.itemReticleConfigs != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.type.getValue());
      buf.writeIntLE(this.maxId);
      if (this.itemReticleConfigs != null) {
         if (this.itemReticleConfigs.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("ItemReticleConfigs", this.itemReticleConfigs.size(), 4096000);
         }

         VarInt.write(buf, this.itemReticleConfigs.size());

         for (Entry<Integer, ItemReticleConfig> e : this.itemReticleConfigs.entrySet()) {
            buf.writeIntLE(e.getKey());
            e.getValue().serialize(buf);
         }
      }
   }

   @Override
   public int computeSize() {
      int size = 6;
      if (this.itemReticleConfigs != null) {
         int itemReticleConfigsSize = 0;

         for (Entry<Integer, ItemReticleConfig> kvp : this.itemReticleConfigs.entrySet()) {
            itemReticleConfigsSize += 4 + kvp.getValue().computeSize();
         }

         size += VarInt.size(this.itemReticleConfigs.size()) + itemReticleConfigsSize;
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
            int itemReticleConfigsCount = VarInt.peek(buffer, pos);
            if (itemReticleConfigsCount < 0) {
               return ValidationResult.error("Invalid dictionary count for ItemReticleConfigs");
            }

            if (itemReticleConfigsCount > 4096000) {
               return ValidationResult.error("ItemReticleConfigs exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);

            for (int i = 0; i < itemReticleConfigsCount; i++) {
               pos += 4;
               if (pos > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading key");
               }

               pos += ItemReticleConfig.computeBytesConsumed(buffer, pos);
            }
         }

         return ValidationResult.OK;
      }
   }

   public UpdateItemReticles clone() {
      UpdateItemReticles copy = new UpdateItemReticles();
      copy.type = this.type;
      copy.maxId = this.maxId;
      if (this.itemReticleConfigs != null) {
         Map<Integer, ItemReticleConfig> m = new HashMap<>();

         for (Entry<Integer, ItemReticleConfig> e : this.itemReticleConfigs.entrySet()) {
            m.put(e.getKey(), e.getValue().clone());
         }

         copy.itemReticleConfigs = m;
      }

      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof UpdateItemReticles other)
            ? false
            : Objects.equals(this.type, other.type) && this.maxId == other.maxId && Objects.equals(this.itemReticleConfigs, other.itemReticleConfigs);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.type, this.maxId, this.itemReticleConfigs);
   }
}
