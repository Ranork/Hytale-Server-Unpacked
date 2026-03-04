package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WorldEnvironment {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 4;
   public static final int VARIABLE_FIELD_COUNT = 3;
   public static final int VARIABLE_BLOCK_START = 16;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public String id;
   @Nullable
   public Color waterTint;
   @Nullable
   public Map<Integer, FluidParticle> fluidParticles;
   @Nullable
   public int[] tagIndexes;

   public WorldEnvironment() {
   }

   public WorldEnvironment(@Nullable String id, @Nullable Color waterTint, @Nullable Map<Integer, FluidParticle> fluidParticles, @Nullable int[] tagIndexes) {
      this.id = id;
      this.waterTint = waterTint;
      this.fluidParticles = fluidParticles;
      this.tagIndexes = tagIndexes;
   }

   public WorldEnvironment(@Nonnull WorldEnvironment other) {
      this.id = other.id;
      this.waterTint = other.waterTint;
      this.fluidParticles = other.fluidParticles;
      this.tagIndexes = other.tagIndexes;
   }

   @Nonnull
   public static WorldEnvironment deserialize(@Nonnull ByteBuf buf, int offset) {
      WorldEnvironment obj = new WorldEnvironment();
      byte nullBits = buf.getByte(offset);
      if ((nullBits & 1) != 0) {
         obj.waterTint = Color.deserialize(buf, offset + 1);
      }

      if ((nullBits & 2) != 0) {
         int varPos0 = offset + 16 + buf.getIntLE(offset + 4);
         int idLen = VarInt.peek(buf, varPos0);
         if (idLen < 0) {
            throw ProtocolException.negativeLength("Id", idLen);
         }

         if (idLen > 4096000) {
            throw ProtocolException.stringTooLong("Id", idLen, 4096000);
         }

         obj.id = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
      }

      if ((nullBits & 4) != 0) {
         int varPos1 = offset + 16 + buf.getIntLE(offset + 8);
         int fluidParticlesCount = VarInt.peek(buf, varPos1);
         if (fluidParticlesCount < 0) {
            throw ProtocolException.negativeLength("FluidParticles", fluidParticlesCount);
         }

         if (fluidParticlesCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("FluidParticles", fluidParticlesCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos1);
         obj.fluidParticles = new HashMap<>(fluidParticlesCount);
         int dictPos = varPos1 + varIntLen;

         for (int i = 0; i < fluidParticlesCount; i++) {
            int key = buf.getIntLE(dictPos);
            dictPos += 4;
            FluidParticle val = FluidParticle.deserialize(buf, dictPos);
            dictPos += FluidParticle.computeBytesConsumed(buf, dictPos);
            if (obj.fluidParticles.put(key, val) != null) {
               throw ProtocolException.duplicateKey("fluidParticles", key);
            }
         }
      }

      if ((nullBits & 8) != 0) {
         int varPos2 = offset + 16 + buf.getIntLE(offset + 12);
         int tagIndexesCount = VarInt.peek(buf, varPos2);
         if (tagIndexesCount < 0) {
            throw ProtocolException.negativeLength("TagIndexes", tagIndexesCount);
         }

         if (tagIndexesCount > 4096000) {
            throw ProtocolException.arrayTooLong("TagIndexes", tagIndexesCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos2);
         if (varPos2 + varIntLen + tagIndexesCount * 4L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("TagIndexes", varPos2 + varIntLen + tagIndexesCount * 4, buf.readableBytes());
         }

         obj.tagIndexes = new int[tagIndexesCount];

         for (int ix = 0; ix < tagIndexesCount; ix++) {
            obj.tagIndexes[ix] = buf.getIntLE(varPos2 + varIntLen + ix * 4);
         }
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 16;
      if ((nullBits & 2) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 4);
         int pos0 = offset + 16 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 8);
         int pos1 = offset + 16 + fieldOffset1;
         int dictLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.length(buf, pos1);

         for (int i = 0; i < dictLen; i++) {
            pos1 += 4;
            pos1 += FluidParticle.computeBytesConsumed(buf, pos1);
         }

         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 8) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 12);
         int pos2 = offset + 16 + fieldOffset2;
         int arrLen = VarInt.peek(buf, pos2);
         pos2 += VarInt.length(buf, pos2) + arrLen * 4;
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      return maxEnd;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.waterTint != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.id != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.fluidParticles != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.tagIndexes != null) {
         nullBits = (byte)(nullBits | 8);
      }

      buf.writeByte(nullBits);
      if (this.waterTint != null) {
         this.waterTint.serialize(buf);
      } else {
         buf.writeZero(3);
      }

      int idOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int fluidParticlesOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int tagIndexesOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.id != null) {
         buf.setIntLE(idOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.id, 4096000);
      } else {
         buf.setIntLE(idOffsetSlot, -1);
      }

      if (this.fluidParticles != null) {
         buf.setIntLE(fluidParticlesOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.fluidParticles.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("FluidParticles", this.fluidParticles.size(), 4096000);
         }

         VarInt.write(buf, this.fluidParticles.size());

         for (Entry<Integer, FluidParticle> e : this.fluidParticles.entrySet()) {
            buf.writeIntLE(e.getKey());
            e.getValue().serialize(buf);
         }
      } else {
         buf.setIntLE(fluidParticlesOffsetSlot, -1);
      }

      if (this.tagIndexes != null) {
         buf.setIntLE(tagIndexesOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.tagIndexes.length > 4096000) {
            throw ProtocolException.arrayTooLong("TagIndexes", this.tagIndexes.length, 4096000);
         }

         VarInt.write(buf, this.tagIndexes.length);

         for (int item : this.tagIndexes) {
            buf.writeIntLE(item);
         }
      } else {
         buf.setIntLE(tagIndexesOffsetSlot, -1);
      }
   }

   public int computeSize() {
      int size = 16;
      if (this.id != null) {
         size += PacketIO.stringSize(this.id);
      }

      if (this.fluidParticles != null) {
         int fluidParticlesSize = 0;

         for (Entry<Integer, FluidParticle> kvp : this.fluidParticles.entrySet()) {
            fluidParticlesSize += 4 + kvp.getValue().computeSize();
         }

         size += VarInt.size(this.fluidParticles.size()) + fluidParticlesSize;
      }

      if (this.tagIndexes != null) {
         size += VarInt.size(this.tagIndexes.length) + this.tagIndexes.length * 4;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 16) {
         return ValidationResult.error("Buffer too small: expected at least 16 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 2) != 0) {
            int idOffset = buffer.getIntLE(offset + 4);
            if (idOffset < 0) {
               return ValidationResult.error("Invalid offset for Id");
            }

            int pos = offset + 16 + idOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Id");
            }

            int idLen = VarInt.peek(buffer, pos);
            if (idLen < 0) {
               return ValidationResult.error("Invalid string length for Id");
            }

            if (idLen > 4096000) {
               return ValidationResult.error("Id exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += idLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Id");
            }
         }

         if ((nullBits & 4) != 0) {
            int fluidParticlesOffset = buffer.getIntLE(offset + 8);
            if (fluidParticlesOffset < 0) {
               return ValidationResult.error("Invalid offset for FluidParticles");
            }

            int posx = offset + 16 + fluidParticlesOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for FluidParticles");
            }

            int fluidParticlesCount = VarInt.peek(buffer, posx);
            if (fluidParticlesCount < 0) {
               return ValidationResult.error("Invalid dictionary count for FluidParticles");
            }

            if (fluidParticlesCount > 4096000) {
               return ValidationResult.error("FluidParticles exceeds max length 4096000");
            }

            posx += VarInt.length(buffer, posx);

            for (int i = 0; i < fluidParticlesCount; i++) {
               posx += 4;
               if (posx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading key");
               }

               posx += FluidParticle.computeBytesConsumed(buffer, posx);
            }
         }

         if ((nullBits & 8) != 0) {
            int tagIndexesOffset = buffer.getIntLE(offset + 12);
            if (tagIndexesOffset < 0) {
               return ValidationResult.error("Invalid offset for TagIndexes");
            }

            int posxx = offset + 16 + tagIndexesOffset;
            if (posxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for TagIndexes");
            }

            int tagIndexesCount = VarInt.peek(buffer, posxx);
            if (tagIndexesCount < 0) {
               return ValidationResult.error("Invalid array count for TagIndexes");
            }

            if (tagIndexesCount > 4096000) {
               return ValidationResult.error("TagIndexes exceeds max length 4096000");
            }

            posxx += VarInt.length(buffer, posxx);
            posxx += tagIndexesCount * 4;
            if (posxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading TagIndexes");
            }
         }

         return ValidationResult.OK;
      }
   }

   public WorldEnvironment clone() {
      WorldEnvironment copy = new WorldEnvironment();
      copy.id = this.id;
      copy.waterTint = this.waterTint != null ? this.waterTint.clone() : null;
      if (this.fluidParticles != null) {
         Map<Integer, FluidParticle> m = new HashMap<>();

         for (Entry<Integer, FluidParticle> e : this.fluidParticles.entrySet()) {
            m.put(e.getKey(), e.getValue().clone());
         }

         copy.fluidParticles = m;
      }

      copy.tagIndexes = this.tagIndexes != null ? Arrays.copyOf(this.tagIndexes, this.tagIndexes.length) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof WorldEnvironment other)
            ? false
            : Objects.equals(this.id, other.id)
               && Objects.equals(this.waterTint, other.waterTint)
               && Objects.equals(this.fluidParticles, other.fluidParticles)
               && Arrays.equals(this.tagIndexes, other.tagIndexes);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.id);
      result = 31 * result + Objects.hashCode(this.waterTint);
      result = 31 * result + Objects.hashCode(this.fluidParticles);
      return 31 * result + Arrays.hashCode(this.tagIndexes);
   }
}
