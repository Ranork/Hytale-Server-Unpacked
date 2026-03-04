package com.hypixel.hytale.protocol.packets.interface_;

import com.hypixel.hytale.protocol.CraftingRecipe;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
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

public class UpdateKnownRecipes implements Packet, ToClientPacket {
   public static final int PACKET_ID = 228;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 1;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public Map<String, CraftingRecipe> known;

   @Override
   public int getId() {
      return 228;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public UpdateKnownRecipes() {
   }

   public UpdateKnownRecipes(@Nullable Map<String, CraftingRecipe> known) {
      this.known = known;
   }

   public UpdateKnownRecipes(@Nonnull UpdateKnownRecipes other) {
      this.known = other.known;
   }

   @Nonnull
   public static UpdateKnownRecipes deserialize(@Nonnull ByteBuf buf, int offset) {
      UpdateKnownRecipes obj = new UpdateKnownRecipes();
      byte nullBits = buf.getByte(offset);
      int pos = offset + 1;
      if ((nullBits & 1) != 0) {
         int knownCount = VarInt.peek(buf, pos);
         if (knownCount < 0) {
            throw ProtocolException.negativeLength("Known", knownCount);
         }

         if (knownCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Known", knownCount, 4096000);
         }

         pos += VarInt.size(knownCount);
         obj.known = new HashMap<>(knownCount);

         for (int i = 0; i < knownCount; i++) {
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
            CraftingRecipe val = CraftingRecipe.deserialize(buf, pos);
            pos += CraftingRecipe.computeBytesConsumed(buf, pos);
            if (obj.known.put(key, val) != null) {
               throw ProtocolException.duplicateKey("known", key);
            }
         }
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 1;
      if ((nullBits & 1) != 0) {
         int dictLen = VarInt.peek(buf, pos);
         pos += VarInt.length(buf, pos);

         for (int i = 0; i < dictLen; i++) {
            int sl = VarInt.peek(buf, pos);
            pos += VarInt.length(buf, pos) + sl;
            pos += CraftingRecipe.computeBytesConsumed(buf, pos);
         }
      }

      return pos - offset;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.known != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      if (this.known != null) {
         if (this.known.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Known", this.known.size(), 4096000);
         }

         VarInt.write(buf, this.known.size());

         for (Entry<String, CraftingRecipe> e : this.known.entrySet()) {
            PacketIO.writeVarString(buf, e.getKey(), 4096000);
            e.getValue().serialize(buf);
         }
      }
   }

   @Override
   public int computeSize() {
      int size = 1;
      if (this.known != null) {
         int knownSize = 0;

         for (Entry<String, CraftingRecipe> kvp : this.known.entrySet()) {
            knownSize += PacketIO.stringSize(kvp.getKey()) + kvp.getValue().computeSize();
         }

         size += VarInt.size(this.known.size()) + knownSize;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 1) {
         return ValidationResult.error("Buffer too small: expected at least 1 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 1;
         if ((nullBits & 1) != 0) {
            int knownCount = VarInt.peek(buffer, pos);
            if (knownCount < 0) {
               return ValidationResult.error("Invalid dictionary count for Known");
            }

            if (knownCount > 4096000) {
               return ValidationResult.error("Known exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);

            for (int i = 0; i < knownCount; i++) {
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

               pos += CraftingRecipe.computeBytesConsumed(buffer, pos);
            }
         }

         return ValidationResult.OK;
      }
   }

   public UpdateKnownRecipes clone() {
      UpdateKnownRecipes copy = new UpdateKnownRecipes();
      if (this.known != null) {
         Map<String, CraftingRecipe> m = new HashMap<>();

         for (Entry<String, CraftingRecipe> e : this.known.entrySet()) {
            m.put(e.getKey(), e.getValue().clone());
         }

         copy.known = m;
      }

      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof UpdateKnownRecipes other ? Objects.equals(this.known, other.known) : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.known);
   }
}
