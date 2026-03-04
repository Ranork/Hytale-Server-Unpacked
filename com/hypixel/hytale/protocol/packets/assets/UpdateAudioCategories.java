package com.hypixel.hytale.protocol.packets.assets;

import com.hypixel.hytale.protocol.AudioCategory;
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

public class UpdateAudioCategories implements Packet, ToClientPacket {
   public static final int PACKET_ID = 80;
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
   public Map<Integer, AudioCategory> categories;

   @Override
   public int getId() {
      return 80;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public UpdateAudioCategories() {
   }

   public UpdateAudioCategories(@Nonnull UpdateType type, int maxId, @Nullable Map<Integer, AudioCategory> categories) {
      this.type = type;
      this.maxId = maxId;
      this.categories = categories;
   }

   public UpdateAudioCategories(@Nonnull UpdateAudioCategories other) {
      this.type = other.type;
      this.maxId = other.maxId;
      this.categories = other.categories;
   }

   @Nonnull
   public static UpdateAudioCategories deserialize(@Nonnull ByteBuf buf, int offset) {
      UpdateAudioCategories obj = new UpdateAudioCategories();
      byte nullBits = buf.getByte(offset);
      obj.type = UpdateType.fromValue(buf.getByte(offset + 1));
      obj.maxId = buf.getIntLE(offset + 2);
      int pos = offset + 6;
      if ((nullBits & 1) != 0) {
         int categoriesCount = VarInt.peek(buf, pos);
         if (categoriesCount < 0) {
            throw ProtocolException.negativeLength("Categories", categoriesCount);
         }

         if (categoriesCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Categories", categoriesCount, 4096000);
         }

         pos += VarInt.size(categoriesCount);
         obj.categories = new HashMap<>(categoriesCount);

         for (int i = 0; i < categoriesCount; i++) {
            int key = buf.getIntLE(pos);
            pos += 4;
            AudioCategory val = AudioCategory.deserialize(buf, pos);
            pos += AudioCategory.computeBytesConsumed(buf, pos);
            if (obj.categories.put(key, val) != null) {
               throw ProtocolException.duplicateKey("categories", key);
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
            pos += AudioCategory.computeBytesConsumed(buf, pos);
         }
      }

      return pos - offset;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.categories != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.type.getValue());
      buf.writeIntLE(this.maxId);
      if (this.categories != null) {
         if (this.categories.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Categories", this.categories.size(), 4096000);
         }

         VarInt.write(buf, this.categories.size());

         for (Entry<Integer, AudioCategory> e : this.categories.entrySet()) {
            buf.writeIntLE(e.getKey());
            e.getValue().serialize(buf);
         }
      }
   }

   @Override
   public int computeSize() {
      int size = 6;
      if (this.categories != null) {
         int categoriesSize = 0;

         for (Entry<Integer, AudioCategory> kvp : this.categories.entrySet()) {
            categoriesSize += 4 + kvp.getValue().computeSize();
         }

         size += VarInt.size(this.categories.size()) + categoriesSize;
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
            int categoriesCount = VarInt.peek(buffer, pos);
            if (categoriesCount < 0) {
               return ValidationResult.error("Invalid dictionary count for Categories");
            }

            if (categoriesCount > 4096000) {
               return ValidationResult.error("Categories exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);

            for (int i = 0; i < categoriesCount; i++) {
               pos += 4;
               if (pos > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading key");
               }

               pos += AudioCategory.computeBytesConsumed(buffer, pos);
            }
         }

         return ValidationResult.OK;
      }
   }

   public UpdateAudioCategories clone() {
      UpdateAudioCategories copy = new UpdateAudioCategories();
      copy.type = this.type;
      copy.maxId = this.maxId;
      if (this.categories != null) {
         Map<Integer, AudioCategory> m = new HashMap<>();

         for (Entry<Integer, AudioCategory> e : this.categories.entrySet()) {
            m.put(e.getKey(), e.getValue().clone());
         }

         copy.categories = m;
      }

      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof UpdateAudioCategories other)
            ? false
            : Objects.equals(this.type, other.type) && this.maxId == other.maxId && Objects.equals(this.categories, other.categories);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.type, this.maxId, this.categories);
   }
}
