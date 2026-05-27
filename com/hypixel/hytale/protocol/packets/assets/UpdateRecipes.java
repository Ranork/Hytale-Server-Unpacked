package com.hypixel.hytale.protocol.packets.assets;

import com.hypixel.hytale.protocol.CraftingRecipe;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.UpdateType;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class UpdateRecipes implements Packet, ToClientPacket {
   public static final int PACKET_ID = 60;
   public static final boolean IS_COMPRESSED = true;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 2;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 10;
   public static final int MAX_SIZE = 1677721600;
   @Nonnull
   public UpdateType type = UpdateType.Init;
   @Nullable
   public Map<String, CraftingRecipe> recipes;
   @Nullable
   public String[] removedRecipes;

   @Override
   public int getId() {
      return 60;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public UpdateRecipes() {
   }

   public UpdateRecipes(@Nonnull UpdateType type, @Nullable Map<String, CraftingRecipe> recipes, @Nullable String[] removedRecipes) {
      this.type = type;
      this.recipes = recipes;
      this.removedRecipes = removedRecipes;
   }

   public UpdateRecipes(@Nonnull UpdateRecipes other) {
      this.type = other.type;
      this.recipes = other.recipes;
      this.removedRecipes = other.removedRecipes;
   }

   @Nonnull
   public static UpdateRecipes deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 10) {
         throw ProtocolException.bufferTooSmall("UpdateRecipes", 10, buf.readableBytes() - offset);
      } else {
         UpdateRecipes obj = new UpdateRecipes();
         byte nullBits = buf.getByte(offset);
         obj.type = UpdateType.fromValue(buf.getByte(offset + 1));
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 2);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 10) {
               throw ProtocolException.invalidOffset("Recipes", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 10 + varPosBase0;
            int recipesCount = VarInt.peek(buf, varPos0);
            if (recipesCount < 0) {
               throw ProtocolException.invalidVarInt("Recipes");
            }

            int varIntLen = VarInt.size(recipesCount);
            if (recipesCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("Recipes", recipesCount, 4096000);
            }

            obj.recipes = new HashMap<>(recipesCount);
            int dictPos = varPos0 + varIntLen;

            for (int i = 0; i < recipesCount; i++) {
               int keyLen = VarInt.peek(buf, dictPos);
               if (keyLen < 0) {
                  throw ProtocolException.invalidVarInt("key");
               }

               int keyVarLen = VarInt.size(keyLen);
               if (keyLen > 4096000) {
                  throw ProtocolException.stringTooLong("key", keyLen, 4096000);
               }

               if (dictPos + keyVarLen + keyLen > buf.readableBytes()) {
                  throw ProtocolException.bufferTooSmall("key", dictPos + keyVarLen + keyLen, buf.readableBytes());
               }

               String key = PacketIO.readVarString(buf, dictPos);
               dictPos += keyVarLen + keyLen;
               CraftingRecipe val = CraftingRecipe.deserialize(buf, dictPos);
               dictPos += CraftingRecipe.computeBytesConsumed(buf, dictPos);
               if (obj.recipes.put(key, val) != null) {
                  throw ProtocolException.duplicateKey("recipes", key);
               }
            }
         }

         if ((nullBits & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 6);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 10) {
               throw ProtocolException.invalidOffset("RemovedRecipes", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 10 + varPosBase1;
            int removedRecipesCount = VarInt.peek(buf, varPos1);
            if (removedRecipesCount < 0) {
               throw ProtocolException.invalidVarInt("RemovedRecipes");
            }

            int varIntLen = VarInt.size(removedRecipesCount);
            if (removedRecipesCount > 4096000) {
               throw ProtocolException.arrayTooLong("RemovedRecipes", removedRecipesCount, 4096000);
            }

            if (varPos1 + varIntLen + removedRecipesCount * 1L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("RemovedRecipes", varPos1 + varIntLen + removedRecipesCount * 1, buf.readableBytes());
            }

            obj.removedRecipes = new String[removedRecipesCount];
            int elemPos = varPos1 + varIntLen;

            for (int i = 0; i < removedRecipesCount; i++) {
               int strLen = VarInt.peek(buf, elemPos);
               if (strLen < 0) {
                  throw ProtocolException.invalidVarInt("removedRecipes[" + i + "]");
               }

               int strVarLen = VarInt.size(strLen);
               if (strLen > 4096000) {
                  throw ProtocolException.stringTooLong("removedRecipes[" + i + "]", strLen, 4096000);
               }

               if (elemPos + strVarLen + strLen > buf.readableBytes()) {
                  throw ProtocolException.bufferTooSmall("removedRecipes[" + i + "]", elemPos + strVarLen + strLen, buf.readableBytes());
               }

               obj.removedRecipes[i] = PacketIO.readVarString(buf, elemPos);
               elemPos += strVarLen + strLen;
            }
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 10;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 2);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 10) {
            throw ProtocolException.invalidOffset("Recipes", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 10 + fieldOffset0;
         int dictLen = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(dictLen);

         for (int i = 0; i < dictLen; i++) {
            int sl = VarInt.peek(buf, pos0);
            pos0 += VarInt.size(sl) + sl;
            pos0 += CraftingRecipe.computeBytesConsumed(buf, pos0);
         }

         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 6);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 10) {
            throw ProtocolException.invalidOffset("RemovedRecipes", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 10 + fieldOffset1;
         int arrLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            int sl = VarInt.peek(buf, pos1);
            pos1 += VarInt.size(sl) + sl;
         }

         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 10L;
   }

   public static UpdateType getType(MemorySegment mem) {
      return getType(mem, 0);
   }

   public static UpdateType getType(MemorySegment mem, int offset) {
      return UpdateType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1)));
   }

   @Nullable
   public static Map<String, CraftingRecipe> getRecipes(MemorySegment mem) {
      return getRecipes(mem, 0);
   }

   @Nullable
   public static Map<String, CraftingRecipe> getRecipes(MemorySegment mem, int offset) {
      if (!hasRecipes(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 2, 10, "Recipes");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Recipes", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Recipes", len, 4096000);
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
                  throw ProtocolException.duplicateKey("Recipes", key);
               }
            }

            return data;
         }
      }
   }

   @Nullable
   public static String[] getRemovedRecipes(MemorySegment mem) {
      return getRemovedRecipes(mem, 0);
   }

   @Nullable
   public static String[] getRemovedRecipes(MemorySegment mem, int offset) {
      if (!hasRemovedRecipes(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 6, 10, "RemovedRecipes");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("RemovedRecipes", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("RemovedRecipes", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("RemovedRecipes", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               String[] data = new String[len];

               for (int i = 0; i < len; i++) {
                  long sp = VarInt.getWithLength(mem, off);
                  int n = (int)sp + (int)(sp >>> 32);
                  data[i] = PacketIO.readVarString("RemovedRecipes", mem, off, 16384000, PacketIO.UTF8);
                  off += n;
               }

               return data;
            }
         }
      }
   }

   public static boolean hasRecipes(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasRemovedRecipes(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   private static int getValidatedOffset(MemorySegment buffer, int base, int slotPosition, int varBlockStart, String fieldName) {
      int offset = buffer.get(PacketIO.PROTO_INT, (long)(base + slotPosition));
      if (offset >= 0 && offset <= buffer.byteSize() - base - varBlockStart) {
         return varBlockStart + offset;
      } else {
         throw ProtocolException.invalidOffset(fieldName, offset, (int)buffer.byteSize());
      }
   }

   public static UpdateRecipes toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static UpdateRecipes toObject(MemorySegment mem, int offset) {
      if (offset + 10 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("UpdateRecipes", offset + 10, (int)mem.byteSize());
      } else {
         Map<String, CraftingRecipe> recipes = null;
         if (hasRecipes(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 2, 10, "Recipes");
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Recipes", len);
            }

            if (len > 4096000) {
               throw ProtocolException.dictionaryTooLarge("Recipes", len, 4096000);
            }

            recipes = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               long keyPacked = VarInt.getWithLength(mem, off);
               int nkey = (int)keyPacked + (int)(keyPacked >>> 32);
               String key = PacketIO.readVarString("key", mem, off, 16384000, PacketIO.UTF8);
               off += nkey;
               CraftingRecipe value = CraftingRecipe.toObject(mem, off);
               off += value.computeSize();
               if (recipes.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("Recipes", key);
               }
            }
         }

         String[] removedRecipes = null;
         if (hasRemovedRecipes(mem, offset)) {
            int offx = offset + getValidatedOffset(mem, offset, 6, 10, "RemovedRecipes");
            long packedx = VarInt.getWithLength(mem, offx);
            int lenx = (int)packedx;
            if (lenx < 0) {
               throw ProtocolException.negativeLength("RemovedRecipes", lenx);
            }

            if (lenx > 4096000) {
               throw ProtocolException.arrayTooLong("RemovedRecipes", lenx, 4096000);
            }

            int lenOffset = (int)(packedx >>> 32);
            if (offx + lenOffset + lenx > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("RemovedRecipes", offx + lenOffset + lenx, (int)mem.byteSize());
            }

            offx += lenOffset;
            removedRecipes = new String[lenx];

            for (int ix = 0; ix < lenx; ix++) {
               long sp = VarInt.getWithLength(mem, offx);
               int n = (int)sp + (int)(sp >>> 32);
               removedRecipes[ix] = PacketIO.readVarString("RemovedRecipes", mem, offx, 16384000, PacketIO.UTF8);
               offx += n;
            }
         }

         return new UpdateRecipes(UpdateType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1))), recipes, removedRecipes);
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.recipes != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.removedRecipes != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.type.getValue());
      int recipesOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int removedRecipesOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.recipes != null) {
         buf.setIntLE(recipesOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.recipes.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Recipes", this.recipes.size(), 4096000);
         }

         VarInt.write(buf, this.recipes.size());

         for (Entry<String, CraftingRecipe> e : this.recipes.entrySet()) {
            PacketIO.writeVarString(buf, e.getKey(), 4096000);
            e.getValue().serialize(buf);
         }
      } else {
         buf.setIntLE(recipesOffsetSlot, -1);
      }

      if (this.removedRecipes != null) {
         buf.setIntLE(removedRecipesOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.removedRecipes.length > 4096000) {
            throw ProtocolException.arrayTooLong("RemovedRecipes", this.removedRecipes.length, 4096000);
         }

         VarInt.write(buf, this.removedRecipes.length);

         for (String item : this.removedRecipes) {
            PacketIO.writeVarString(buf, item, 4096000);
         }
      } else {
         buf.setIntLE(removedRecipesOffsetSlot, -1);
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.recipes != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.removedRecipes != null) {
         nullBits = (byte)(nullBits | 2);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 1), (byte)this.type.getValue());
      int varOffset = offset + 10;
      if (this.recipes != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 2), varOffset - offset - 10);
         if (this.recipes.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Recipes", this.recipes.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.recipes.size());

         for (Entry<String, CraftingRecipe> e : this.recipes.entrySet()) {
            varOffset += PacketIO.writeVarString(mem, varOffset, e.getKey(), 16384000);
            varOffset += e.getValue().serialize(mem, varOffset);
         }
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 2), -1);
      }

      if (this.removedRecipes != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 6), varOffset - offset - 10);
         if (this.removedRecipes.length > 4096000) {
            throw ProtocolException.arrayTooLong("RemovedRecipes", this.removedRecipes.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.removedRecipes.length);
         int removedRecipesValueOffset = 0;

         for (int i = 0; i < this.removedRecipes.length; i++) {
            removedRecipesValueOffset += PacketIO.writeVarString(mem, varOffset + removedRecipesValueOffset, this.removedRecipes[i], 16384000);
         }

         varOffset += removedRecipesValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 6), -1);
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 10;
      if (this.recipes != null) {
         int recipesSize = 0;

         for (Entry<String, CraftingRecipe> kvp : this.recipes.entrySet()) {
            recipesSize += PacketIO.stringSize(kvp.getKey()) + kvp.getValue().computeSize();
         }

         size += VarInt.size(this.recipes.size()) + recipesSize;
      }

      if (this.removedRecipes != null) {
         int removedRecipesSize = 0;

         for (String elem : this.removedRecipes) {
            removedRecipesSize += PacketIO.stringSize(elem);
         }

         size += VarInt.size(this.removedRecipes.length) + removedRecipesSize;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 10) {
         return ValidationResult.error("Buffer too small: expected at least 10 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int v = buffer.getByte(offset + 1) & 255;
         if (v >= 3) {
            return ValidationResult.error("Invalid UpdateType value for Type");
         } else {
            if ((nullBits & 1) != 0) {
               v = buffer.getIntLE(offset + 2);
               if (v < 0 || v > buffer.writerIndex() - offset - 10) {
                  return ValidationResult.error("Invalid offset for Recipes");
               }

               int pos = offset + 10 + v;
               int recipesCount = VarInt.peek(buffer, pos);
               if (recipesCount < 0) {
                  return ValidationResult.error("Invalid dictionary count for Recipes");
               }

               if (recipesCount > 4096000) {
                  return ValidationResult.error("Recipes exceeds max length 4096000");
               }

               pos += VarInt.size(recipesCount);

               for (int i = 0; i < recipesCount; i++) {
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

            if ((nullBits & 2) != 0) {
               v = buffer.getIntLE(offset + 6);
               if (v < 0 || v > buffer.writerIndex() - offset - 10) {
                  return ValidationResult.error("Invalid offset for RemovedRecipes");
               }

               int posx = offset + 10 + v;
               int removedRecipesCount = VarInt.peek(buffer, posx);
               if (removedRecipesCount < 0) {
                  return ValidationResult.error("Invalid array count for RemovedRecipes");
               }

               if (removedRecipesCount > 4096000) {
                  return ValidationResult.error("RemovedRecipes exceeds max length 4096000");
               }

               posx += VarInt.size(removedRecipesCount);

               for (int i = 0; i < removedRecipesCount; i++) {
                  int strLen = VarInt.peek(buffer, posx);
                  if (strLen < 0) {
                     return ValidationResult.error("Invalid string length in RemovedRecipes");
                  }

                  posx += VarInt.size(strLen);
                  posx += strLen;
                  if (posx > buffer.writerIndex()) {
                     return ValidationResult.error("Buffer overflow reading string in RemovedRecipes");
                  }
               }
            }

            return ValidationResult.OK;
         }
      }
   }

   public UpdateRecipes clone() {
      UpdateRecipes copy = new UpdateRecipes();
      copy.type = this.type;
      if (this.recipes != null) {
         Map<String, CraftingRecipe> m = new HashMap<>();

         for (Entry<String, CraftingRecipe> e : this.recipes.entrySet()) {
            m.put(e.getKey(), e.getValue().clone());
         }

         copy.recipes = m;
      }

      copy.removedRecipes = this.removedRecipes != null ? Arrays.copyOf(this.removedRecipes, this.removedRecipes.length) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof UpdateRecipes other)
            ? false
            : Objects.equals(this.type, other.type)
               && Objects.equals(this.recipes, other.recipes)
               && Arrays.equals((Object[])this.removedRecipes, (Object[])other.removedRecipes);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.type);
      result = 31 * result + Objects.hashCode(this.recipes);
      return 31 * result + Arrays.hashCode((Object[])this.removedRecipes);
   }
}
