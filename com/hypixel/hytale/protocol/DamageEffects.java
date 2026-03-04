package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DamageEffects {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 5;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 13;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public ModelParticle[] modelParticles;
   @Nullable
   public WorldParticle[] worldParticles;
   public int soundEventIndex;

   public DamageEffects() {
   }

   public DamageEffects(@Nullable ModelParticle[] modelParticles, @Nullable WorldParticle[] worldParticles, int soundEventIndex) {
      this.modelParticles = modelParticles;
      this.worldParticles = worldParticles;
      this.soundEventIndex = soundEventIndex;
   }

   public DamageEffects(@Nonnull DamageEffects other) {
      this.modelParticles = other.modelParticles;
      this.worldParticles = other.worldParticles;
      this.soundEventIndex = other.soundEventIndex;
   }

   @Nonnull
   public static DamageEffects deserialize(@Nonnull ByteBuf buf, int offset) {
      DamageEffects obj = new DamageEffects();
      byte nullBits = buf.getByte(offset);
      obj.soundEventIndex = buf.getIntLE(offset + 1);
      if ((nullBits & 1) != 0) {
         int varPos0 = offset + 13 + buf.getIntLE(offset + 5);
         int modelParticlesCount = VarInt.peek(buf, varPos0);
         if (modelParticlesCount < 0) {
            throw ProtocolException.negativeLength("ModelParticles", modelParticlesCount);
         }

         if (modelParticlesCount > 4096000) {
            throw ProtocolException.arrayTooLong("ModelParticles", modelParticlesCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos0);
         if (varPos0 + varIntLen + modelParticlesCount * 34L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("ModelParticles", varPos0 + varIntLen + modelParticlesCount * 34, buf.readableBytes());
         }

         obj.modelParticles = new ModelParticle[modelParticlesCount];
         int elemPos = varPos0 + varIntLen;

         for (int i = 0; i < modelParticlesCount; i++) {
            obj.modelParticles[i] = ModelParticle.deserialize(buf, elemPos);
            elemPos += ModelParticle.computeBytesConsumed(buf, elemPos);
         }
      }

      if ((nullBits & 2) != 0) {
         int varPos1 = offset + 13 + buf.getIntLE(offset + 9);
         int worldParticlesCount = VarInt.peek(buf, varPos1);
         if (worldParticlesCount < 0) {
            throw ProtocolException.negativeLength("WorldParticles", worldParticlesCount);
         }

         if (worldParticlesCount > 4096000) {
            throw ProtocolException.arrayTooLong("WorldParticles", worldParticlesCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos1);
         if (varPos1 + varIntLen + worldParticlesCount * 32L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("WorldParticles", varPos1 + varIntLen + worldParticlesCount * 32, buf.readableBytes());
         }

         obj.worldParticles = new WorldParticle[worldParticlesCount];
         int elemPos = varPos1 + varIntLen;

         for (int i = 0; i < worldParticlesCount; i++) {
            obj.worldParticles[i] = WorldParticle.deserialize(buf, elemPos);
            elemPos += WorldParticle.computeBytesConsumed(buf, elemPos);
         }
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 13;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 5);
         int pos0 = offset + 13 + fieldOffset0;
         int arrLen = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0);

         for (int i = 0; i < arrLen; i++) {
            pos0 += ModelParticle.computeBytesConsumed(buf, pos0);
         }

         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 9);
         int pos1 = offset + 13 + fieldOffset1;
         int arrLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.length(buf, pos1);

         for (int i = 0; i < arrLen; i++) {
            pos1 += WorldParticle.computeBytesConsumed(buf, pos1);
         }

         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.modelParticles != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.worldParticles != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.soundEventIndex);
      int modelParticlesOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int worldParticlesOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.modelParticles != null) {
         buf.setIntLE(modelParticlesOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.modelParticles.length > 4096000) {
            throw ProtocolException.arrayTooLong("ModelParticles", this.modelParticles.length, 4096000);
         }

         VarInt.write(buf, this.modelParticles.length);

         for (ModelParticle item : this.modelParticles) {
            item.serialize(buf);
         }
      } else {
         buf.setIntLE(modelParticlesOffsetSlot, -1);
      }

      if (this.worldParticles != null) {
         buf.setIntLE(worldParticlesOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.worldParticles.length > 4096000) {
            throw ProtocolException.arrayTooLong("WorldParticles", this.worldParticles.length, 4096000);
         }

         VarInt.write(buf, this.worldParticles.length);

         for (WorldParticle item : this.worldParticles) {
            item.serialize(buf);
         }
      } else {
         buf.setIntLE(worldParticlesOffsetSlot, -1);
      }
   }

   public int computeSize() {
      int size = 13;
      if (this.modelParticles != null) {
         int modelParticlesSize = 0;

         for (ModelParticle elem : this.modelParticles) {
            modelParticlesSize += elem.computeSize();
         }

         size += VarInt.size(this.modelParticles.length) + modelParticlesSize;
      }

      if (this.worldParticles != null) {
         int worldParticlesSize = 0;

         for (WorldParticle elem : this.worldParticles) {
            worldParticlesSize += elem.computeSize();
         }

         size += VarInt.size(this.worldParticles.length) + worldParticlesSize;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 13) {
         return ValidationResult.error("Buffer too small: expected at least 13 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int modelParticlesOffset = buffer.getIntLE(offset + 5);
            if (modelParticlesOffset < 0) {
               return ValidationResult.error("Invalid offset for ModelParticles");
            }

            int pos = offset + 13 + modelParticlesOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for ModelParticles");
            }

            int modelParticlesCount = VarInt.peek(buffer, pos);
            if (modelParticlesCount < 0) {
               return ValidationResult.error("Invalid array count for ModelParticles");
            }

            if (modelParticlesCount > 4096000) {
               return ValidationResult.error("ModelParticles exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);

            for (int i = 0; i < modelParticlesCount; i++) {
               ValidationResult structResult = ModelParticle.validateStructure(buffer, pos);
               if (!structResult.isValid()) {
                  return ValidationResult.error("Invalid ModelParticle in ModelParticles[" + i + "]: " + structResult.error());
               }

               pos += ModelParticle.computeBytesConsumed(buffer, pos);
            }
         }

         if ((nullBits & 2) != 0) {
            int worldParticlesOffset = buffer.getIntLE(offset + 9);
            if (worldParticlesOffset < 0) {
               return ValidationResult.error("Invalid offset for WorldParticles");
            }

            int posx = offset + 13 + worldParticlesOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for WorldParticles");
            }

            int worldParticlesCount = VarInt.peek(buffer, posx);
            if (worldParticlesCount < 0) {
               return ValidationResult.error("Invalid array count for WorldParticles");
            }

            if (worldParticlesCount > 4096000) {
               return ValidationResult.error("WorldParticles exceeds max length 4096000");
            }

            posx += VarInt.length(buffer, posx);

            for (int i = 0; i < worldParticlesCount; i++) {
               ValidationResult structResult = WorldParticle.validateStructure(buffer, posx);
               if (!structResult.isValid()) {
                  return ValidationResult.error("Invalid WorldParticle in WorldParticles[" + i + "]: " + structResult.error());
               }

               posx += WorldParticle.computeBytesConsumed(buffer, posx);
            }
         }

         return ValidationResult.OK;
      }
   }

   public DamageEffects clone() {
      DamageEffects copy = new DamageEffects();
      copy.modelParticles = this.modelParticles != null ? Arrays.stream(this.modelParticles).map(e -> e.clone()).toArray(ModelParticle[]::new) : null;
      copy.worldParticles = this.worldParticles != null ? Arrays.stream(this.worldParticles).map(e -> e.clone()).toArray(WorldParticle[]::new) : null;
      copy.soundEventIndex = this.soundEventIndex;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof DamageEffects other)
            ? false
            : Arrays.equals((Object[])this.modelParticles, (Object[])other.modelParticles)
               && Arrays.equals((Object[])this.worldParticles, (Object[])other.worldParticles)
               && this.soundEventIndex == other.soundEventIndex;
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Arrays.hashCode((Object[])this.modelParticles);
      result = 31 * result + Arrays.hashCode((Object[])this.worldParticles);
      return 31 * result + Integer.hashCode(this.soundEventIndex);
   }
}
