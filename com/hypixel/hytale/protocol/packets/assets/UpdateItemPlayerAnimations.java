package com.hypixel.hytale.protocol.packets.assets;

import com.hypixel.hytale.protocol.ItemPlayerAnimations;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.UpdateType;
import com.hypixel.hytale.protocol.io.PacketIO;
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

public class UpdateItemPlayerAnimations implements Packet, ToClientPacket {
   public static final int PACKET_ID = 52;
   public static final boolean IS_COMPRESSED = true;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 2;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 2;
   public static final int MAX_SIZE = 1677721600;
   @Nonnull
   public UpdateType type = UpdateType.Init;
   @Nullable
   public Map<String, ItemPlayerAnimations> itemPlayerAnimations;

   @Override
   public int getId() {
      return 52;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public UpdateItemPlayerAnimations() {
   }

   public UpdateItemPlayerAnimations(@Nonnull UpdateType type, @Nullable Map<String, ItemPlayerAnimations> itemPlayerAnimations) {
      this.type = type;
      this.itemPlayerAnimations = itemPlayerAnimations;
   }

   public UpdateItemPlayerAnimations(@Nonnull UpdateItemPlayerAnimations other) {
      this.type = other.type;
      this.itemPlayerAnimations = other.itemPlayerAnimations;
   }

   @Nonnull
   public static UpdateItemPlayerAnimations deserialize(@Nonnull ByteBuf buf, int offset) {
      UpdateItemPlayerAnimations obj = new UpdateItemPlayerAnimations();
      byte nullBits = buf.getByte(offset);
      obj.type = UpdateType.fromValue(buf.getByte(offset + 1));
      int pos = offset + 2;
      if ((nullBits & 1) != 0) {
         int itemPlayerAnimationsCount = VarInt.peek(buf, pos);
         if (itemPlayerAnimationsCount < 0) {
            throw ProtocolException.negativeLength("ItemPlayerAnimations", itemPlayerAnimationsCount);
         }

         if (itemPlayerAnimationsCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("ItemPlayerAnimations", itemPlayerAnimationsCount, 4096000);
         }

         pos += VarInt.size(itemPlayerAnimationsCount);
         obj.itemPlayerAnimations = new HashMap<>(itemPlayerAnimationsCount);

         for (int i = 0; i < itemPlayerAnimationsCount; i++) {
            int keyLen = VarInt.peek(buf, pos);
            if (keyLen < 0) {
               throw ProtocolException.negativeLength("key", keyLen);
            }

            if (keyLen > 4096000) {
               throw ProtocolException.stringTooLong("key", keyLen, 4096000);
            }

            int keyVarLen = VarInt.length(buf, pos);
            String key = PacketIO.readVarString(buf, pos);
            pos += keyVarLen + keyLen;
            ItemPlayerAnimations val = ItemPlayerAnimations.deserialize(buf, pos);
            pos += ItemPlayerAnimations.computeBytesConsumed(buf, pos);
            if (obj.itemPlayerAnimations.put(key, val) != null) {
               throw ProtocolException.duplicateKey("itemPlayerAnimations", key);
            }
         }
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 2;
      if ((nullBits & 1) != 0) {
         int dictLen = VarInt.peek(buf, pos);
         pos += VarInt.length(buf, pos);

         for (int i = 0; i < dictLen; i++) {
            int sl = VarInt.peek(buf, pos);
            pos += VarInt.length(buf, pos) + sl;
            pos += ItemPlayerAnimations.computeBytesConsumed(buf, pos);
         }
      }

      return pos - offset;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.itemPlayerAnimations != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.type.getValue());
      if (this.itemPlayerAnimations != null) {
         if (this.itemPlayerAnimations.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("ItemPlayerAnimations", this.itemPlayerAnimations.size(), 4096000);
         }

         VarInt.write(buf, this.itemPlayerAnimations.size());

         for (Entry<String, ItemPlayerAnimations> e : this.itemPlayerAnimations.entrySet()) {
            PacketIO.writeVarString(buf, e.getKey(), 4096000);
            e.getValue().serialize(buf);
         }
      }
   }

   @Override
   public int computeSize() {
      int size = 2;
      if (this.itemPlayerAnimations != null) {
         int itemPlayerAnimationsSize = 0;

         for (Entry<String, ItemPlayerAnimations> kvp : this.itemPlayerAnimations.entrySet()) {
            itemPlayerAnimationsSize += PacketIO.stringSize(kvp.getKey()) + kvp.getValue().computeSize();
         }

         size += VarInt.size(this.itemPlayerAnimations.size()) + itemPlayerAnimationsSize;
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
            int itemPlayerAnimationsCount = VarInt.peek(buffer, pos);
            if (itemPlayerAnimationsCount < 0) {
               return ValidationResult.error("Invalid dictionary count for ItemPlayerAnimations");
            }

            if (itemPlayerAnimationsCount > 4096000) {
               return ValidationResult.error("ItemPlayerAnimations exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);

            for (int i = 0; i < itemPlayerAnimationsCount; i++) {
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

               pos += ItemPlayerAnimations.computeBytesConsumed(buffer, pos);
            }
         }

         return ValidationResult.OK;
      }
   }

   public UpdateItemPlayerAnimations clone() {
      UpdateItemPlayerAnimations copy = new UpdateItemPlayerAnimations();
      copy.type = this.type;
      if (this.itemPlayerAnimations != null) {
         Map<String, ItemPlayerAnimations> m = new HashMap<>();

         for (Entry<String, ItemPlayerAnimations> e : this.itemPlayerAnimations.entrySet()) {
            m.put(e.getKey(), e.getValue().clone());
         }

         copy.itemPlayerAnimations = m;
      }

      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof UpdateItemPlayerAnimations other)
            ? false
            : Objects.equals(this.type, other.type) && Objects.equals(this.itemPlayerAnimations, other.itemPlayerAnimations);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.type, this.itemPlayerAnimations);
   }
}
