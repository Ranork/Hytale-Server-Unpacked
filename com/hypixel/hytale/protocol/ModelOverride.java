package com.hypixel.hytale.protocol;

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

public class ModelOverride {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 3;
   public static final int VARIABLE_BLOCK_START = 13;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public String model;
   @Nullable
   public String texture;
   @Nullable
   public Map<String, AnimationSet> animationSets;

   public ModelOverride() {
   }

   public ModelOverride(@Nullable String model, @Nullable String texture, @Nullable Map<String, AnimationSet> animationSets) {
      this.model = model;
      this.texture = texture;
      this.animationSets = animationSets;
   }

   public ModelOverride(@Nonnull ModelOverride other) {
      this.model = other.model;
      this.texture = other.texture;
      this.animationSets = other.animationSets;
   }

   @Nonnull
   public static ModelOverride deserialize(@Nonnull ByteBuf buf, int offset) {
      ModelOverride obj = new ModelOverride();
      byte nullBits = buf.getByte(offset);
      if ((nullBits & 1) != 0) {
         int varPos0 = offset + 13 + buf.getIntLE(offset + 1);
         int modelLen = VarInt.peek(buf, varPos0);
         if (modelLen < 0) {
            throw ProtocolException.negativeLength("Model", modelLen);
         }

         if (modelLen > 4096000) {
            throw ProtocolException.stringTooLong("Model", modelLen, 4096000);
         }

         obj.model = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
      }

      if ((nullBits & 2) != 0) {
         int varPos1 = offset + 13 + buf.getIntLE(offset + 5);
         int textureLen = VarInt.peek(buf, varPos1);
         if (textureLen < 0) {
            throw ProtocolException.negativeLength("Texture", textureLen);
         }

         if (textureLen > 4096000) {
            throw ProtocolException.stringTooLong("Texture", textureLen, 4096000);
         }

         obj.texture = PacketIO.readVarString(buf, varPos1, PacketIO.UTF8);
      }

      if ((nullBits & 4) != 0) {
         int varPos2 = offset + 13 + buf.getIntLE(offset + 9);
         int animationSetsCount = VarInt.peek(buf, varPos2);
         if (animationSetsCount < 0) {
            throw ProtocolException.negativeLength("AnimationSets", animationSetsCount);
         }

         if (animationSetsCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("AnimationSets", animationSetsCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos2);
         obj.animationSets = new HashMap<>(animationSetsCount);
         int dictPos = varPos2 + varIntLen;

         for (int i = 0; i < animationSetsCount; i++) {
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
            AnimationSet val = AnimationSet.deserialize(buf, dictPos);
            dictPos += AnimationSet.computeBytesConsumed(buf, dictPos);
            if (obj.animationSets.put(key, val) != null) {
               throw ProtocolException.duplicateKey("animationSets", key);
            }
         }
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 13;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 1);
         int pos0 = offset + 13 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 5);
         int pos1 = offset + 13 + fieldOffset1;
         int sl = VarInt.peek(buf, pos1);
         pos1 += VarInt.length(buf, pos1) + sl;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 9);
         int pos2 = offset + 13 + fieldOffset2;
         int dictLen = VarInt.peek(buf, pos2);
         pos2 += VarInt.length(buf, pos2);

         for (int i = 0; i < dictLen; i++) {
            int sl = VarInt.peek(buf, pos2);
            pos2 += VarInt.length(buf, pos2) + sl;
            pos2 += AnimationSet.computeBytesConsumed(buf, pos2);
         }

         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      return maxEnd;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.model != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.texture != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.animationSets != null) {
         nullBits = (byte)(nullBits | 4);
      }

      buf.writeByte(nullBits);
      int modelOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int textureOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int animationSetsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.model != null) {
         buf.setIntLE(modelOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.model, 4096000);
      } else {
         buf.setIntLE(modelOffsetSlot, -1);
      }

      if (this.texture != null) {
         buf.setIntLE(textureOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.texture, 4096000);
      } else {
         buf.setIntLE(textureOffsetSlot, -1);
      }

      if (this.animationSets != null) {
         buf.setIntLE(animationSetsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.animationSets.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("AnimationSets", this.animationSets.size(), 4096000);
         }

         VarInt.write(buf, this.animationSets.size());

         for (Entry<String, AnimationSet> e : this.animationSets.entrySet()) {
            PacketIO.writeVarString(buf, e.getKey(), 4096000);
            e.getValue().serialize(buf);
         }
      } else {
         buf.setIntLE(animationSetsOffsetSlot, -1);
      }
   }

   public int computeSize() {
      int size = 13;
      if (this.model != null) {
         size += PacketIO.stringSize(this.model);
      }

      if (this.texture != null) {
         size += PacketIO.stringSize(this.texture);
      }

      if (this.animationSets != null) {
         int animationSetsSize = 0;

         for (Entry<String, AnimationSet> kvp : this.animationSets.entrySet()) {
            animationSetsSize += PacketIO.stringSize(kvp.getKey()) + kvp.getValue().computeSize();
         }

         size += VarInt.size(this.animationSets.size()) + animationSetsSize;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 13) {
         return ValidationResult.error("Buffer too small: expected at least 13 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int modelOffset = buffer.getIntLE(offset + 1);
            if (modelOffset < 0) {
               return ValidationResult.error("Invalid offset for Model");
            }

            int pos = offset + 13 + modelOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Model");
            }

            int modelLen = VarInt.peek(buffer, pos);
            if (modelLen < 0) {
               return ValidationResult.error("Invalid string length for Model");
            }

            if (modelLen > 4096000) {
               return ValidationResult.error("Model exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += modelLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Model");
            }
         }

         if ((nullBits & 2) != 0) {
            int textureOffset = buffer.getIntLE(offset + 5);
            if (textureOffset < 0) {
               return ValidationResult.error("Invalid offset for Texture");
            }

            int posx = offset + 13 + textureOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Texture");
            }

            int textureLen = VarInt.peek(buffer, posx);
            if (textureLen < 0) {
               return ValidationResult.error("Invalid string length for Texture");
            }

            if (textureLen > 4096000) {
               return ValidationResult.error("Texture exceeds max length 4096000");
            }

            posx += VarInt.length(buffer, posx);
            posx += textureLen;
            if (posx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Texture");
            }
         }

         if ((nullBits & 4) != 0) {
            int animationSetsOffset = buffer.getIntLE(offset + 9);
            if (animationSetsOffset < 0) {
               return ValidationResult.error("Invalid offset for AnimationSets");
            }

            int posxx = offset + 13 + animationSetsOffset;
            if (posxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for AnimationSets");
            }

            int animationSetsCount = VarInt.peek(buffer, posxx);
            if (animationSetsCount < 0) {
               return ValidationResult.error("Invalid dictionary count for AnimationSets");
            }

            if (animationSetsCount > 4096000) {
               return ValidationResult.error("AnimationSets exceeds max length 4096000");
            }

            posxx += VarInt.length(buffer, posxx);

            for (int i = 0; i < animationSetsCount; i++) {
               int keyLen = VarInt.peek(buffer, posxx);
               if (keyLen < 0) {
                  return ValidationResult.error("Invalid string length for key");
               }

               if (keyLen > 4096000) {
                  return ValidationResult.error("key exceeds max length 4096000");
               }

               posxx += VarInt.length(buffer, posxx);
               posxx += keyLen;
               if (posxx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading key");
               }

               posxx += AnimationSet.computeBytesConsumed(buffer, posxx);
            }
         }

         return ValidationResult.OK;
      }
   }

   public ModelOverride clone() {
      ModelOverride copy = new ModelOverride();
      copy.model = this.model;
      copy.texture = this.texture;
      if (this.animationSets != null) {
         Map<String, AnimationSet> m = new HashMap<>();

         for (Entry<String, AnimationSet> e : this.animationSets.entrySet()) {
            m.put(e.getKey(), e.getValue().clone());
         }

         copy.animationSets = m;
      }

      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ModelOverride other)
            ? false
            : Objects.equals(this.model, other.model) && Objects.equals(this.texture, other.texture) && Objects.equals(this.animationSets, other.animationSets);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.model, this.texture, this.animationSets);
   }
}
