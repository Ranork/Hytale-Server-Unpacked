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
import java.lang.foreign.MemorySegment;
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
      if (buf.readableBytes() - offset < 1) {
         throw ProtocolException.bufferTooSmall("UpdateKnownRecipes", 1, buf.readableBytes() - offset);
      } else {
         UpdateKnownRecipes obj = new UpdateKnownRecipes();
         byte nullBits = buf.getByte(offset);
         int pos = offset + 1;
         if ((nullBits & 1) != 0) {
            int knownCount = VarInt.peek(buf, pos);
            if (knownCount < 0) {
               throw ProtocolException.invalidVarInt("Known");
            }

            int knownVarLen = VarInt.size(knownCount);
            if (knownCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("Known", knownCount, 4096000);
            }

            pos += knownVarLen;
            obj.known = new HashMap<>(knownCount);

            for (int i = 0; i < knownCount; i++) {
               int keyLen = VarInt.peek(buf, pos);
               if (keyLen < 0) {
                  throw ProtocolException.invalidVarInt("key");
               }

               int keyVarLen = VarInt.size(keyLen);
               if (keyLen > 4096000) {
                  throw ProtocolException.stringTooLong("key", keyLen, 4096000);
               }

               if (pos + keyVarLen + keyLen > buf.readableBytes()) {
                  throw ProtocolException.bufferTooSmall("key", pos + keyVarLen + keyLen, buf.readableBytes());
               }

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
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 1;
      if ((nullBits & 1) != 0) {
         int dictLen = VarInt.peek(buf, pos);
         pos += VarInt.size(dictLen);

         for (int i = 0; i < dictLen; i++) {
            int sl = VarInt.peek(buf, pos);
            pos += VarInt.size(sl) + sl;
            pos += CraftingRecipe.computeBytesConsumed(buf, pos);
         }
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 1L;
   }

   @Nullable
   public static Map<String, CraftingRecipe> getKnown(MemorySegment mem) {
      return getKnown(mem, 0);
   }

   @Nullable
   public static Map<String, CraftingRecipe> getKnown(MemorySegment mem, int offset) {
      if (!hasKnown(mem, offset)) {
         return null;
      } else {
         int off = offset + 1;
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Known", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Known", len, 4096000);
         } else {
            Map<String, CraftingRecipe> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               long keyPacked = VarInt.getWithLength(mem, off);
               int nkey = (int)keyPacked + (int)(keyPacked >>> 32);
               String key = PacketIO.readVarString("key", mem, off, 16384000, PacketIO.UTF8);
               off += nkey;
               CraftingRecipe value = CraftingRecipe.toObject(mem, off);
               off += value.computeSize();
               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("Known", key);
               }
            }

            return data;
         }
      }
   }

   public static boolean hasKnown(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static UpdateKnownRecipes toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static UpdateKnownRecipes toObject(MemorySegment mem, int offset) {
      if (offset + 1 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("UpdateKnownRecipes", offset + 1, (int)mem.byteSize());
      } else {
         Map<String, CraftingRecipe> known = null;
         if (hasKnown(mem, offset)) {
            int off = offset + 1;
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Known", len);
            }

            if (len > 4096000) {
               throw ProtocolException.dictionaryTooLarge("Known", len, 4096000);
            }

            known = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               long keyPacked = VarInt.getWithLength(mem, off);
               int nkey = (int)keyPacked + (int)(keyPacked >>> 32);
               String key = PacketIO.readVarString("key", mem, off, 16384000, PacketIO.UTF8);
               off += nkey;
               CraftingRecipe value = CraftingRecipe.toObject(mem, off);
               off += value.computeSize();
               if (known.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("Known", key);
               }
            }
         }

         return new UpdateKnownRecipes(known);
      }
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
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.known != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      int varOffset = offset + 1;
      if (this.known != null) {
         if (this.known.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Known", this.known.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.known.size());

         for (Entry<String, CraftingRecipe> e : this.known.entrySet()) {
            varOffset += PacketIO.writeVarString(mem, varOffset, e.getKey(), 16384000);
            varOffset += e.getValue().serialize(mem, varOffset);
         }
      }

      return varOffset - offset;
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

            pos += VarInt.size(knownCount);

            for (int i = 0; i < knownCount; i++) {
               int keyLen = VarInt.peek(buffer, pos);
               if (keyLen < 0) {
                  return ValidationResult.error("Invalid string length for key");
               }

               if (keyLen > 4096000) {
                  return ValidationResult.error("key exceeds max length 4096000");
               }

               pos += VarInt.size(keyLen);
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
