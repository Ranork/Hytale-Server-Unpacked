package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
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
      if (buf.readableBytes() - offset < 13) {
         throw ProtocolException.bufferTooSmall("DamageEffects", 13, buf.readableBytes() - offset);
      } else {
         DamageEffects obj = new DamageEffects();
         byte nullBits = buf.getByte(offset);
         obj.soundEventIndex = buf.getIntLE(offset + 1);
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 5);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 13) {
               throw ProtocolException.invalidOffset("ModelParticles", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 13 + varPosBase0;
            int modelParticlesCount = VarInt.peek(buf, varPos0);
            if (modelParticlesCount < 0) {
               throw ProtocolException.invalidVarInt("ModelParticles");
            }

            int varIntLen = VarInt.size(modelParticlesCount);
            if (modelParticlesCount > 4096000) {
               throw ProtocolException.arrayTooLong("ModelParticles", modelParticlesCount, 4096000);
            }

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
            int varPosBase1 = buf.getIntLE(offset + 9);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 13) {
               throw ProtocolException.invalidOffset("WorldParticles", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 13 + varPosBase1;
            int worldParticlesCount = VarInt.peek(buf, varPos1);
            if (worldParticlesCount < 0) {
               throw ProtocolException.invalidVarInt("WorldParticles");
            }

            int varIntLenx = VarInt.size(worldParticlesCount);
            if (worldParticlesCount > 4096000) {
               throw ProtocolException.arrayTooLong("WorldParticles", worldParticlesCount, 4096000);
            }

            if (varPos1 + varIntLenx + worldParticlesCount * 32L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("WorldParticles", varPos1 + varIntLenx + worldParticlesCount * 32, buf.readableBytes());
            }

            obj.worldParticles = new WorldParticle[worldParticlesCount];
            int elemPos = varPos1 + varIntLenx;

            for (int i = 0; i < worldParticlesCount; i++) {
               obj.worldParticles[i] = WorldParticle.deserialize(buf, elemPos);
               elemPos += WorldParticle.computeBytesConsumed(buf, elemPos);
            }
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 13;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 5);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 13) {
            throw ProtocolException.invalidOffset("ModelParticles", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 13 + fieldOffset0;
         int arrLen = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            pos0 += ModelParticle.computeBytesConsumed(buf, pos0);
         }

         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 9);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 13) {
            throw ProtocolException.invalidOffset("WorldParticles", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 13 + fieldOffset1;
         int arrLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            pos1 += WorldParticle.computeBytesConsumed(buf, pos1);
         }

         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 13L;
   }

   @Nullable
   public static ModelParticle[] getModelParticles(MemorySegment mem) {
      return getModelParticles(mem, 0);
   }

   @Nullable
   public static ModelParticle[] getModelParticles(MemorySegment mem, int offset) {
      if (!hasModelParticles(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 5, 13, "ModelParticles");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("ModelParticles", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("ModelParticles", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("ModelParticles", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               ModelParticle[] data = new ModelParticle[len];

               for (int i = 0; i < len; i++) {
                  data[i] = ModelParticle.toObject(mem, off);
                  off += data[i].computeSize();
               }

               return data;
            }
         }
      }
   }

   @Nullable
   public static WorldParticle[] getWorldParticles(MemorySegment mem) {
      return getWorldParticles(mem, 0);
   }

   @Nullable
   public static WorldParticle[] getWorldParticles(MemorySegment mem, int offset) {
      if (!hasWorldParticles(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 9, 13, "WorldParticles");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("WorldParticles", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("WorldParticles", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("WorldParticles", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               WorldParticle[] data = new WorldParticle[len];

               for (int i = 0; i < len; i++) {
                  data[i] = WorldParticle.toObject(mem, off);
                  off += data[i].computeSize();
               }

               return data;
            }
         }
      }
   }

   public static int getSoundEventIndex(MemorySegment mem) {
      return getSoundEventIndex(mem, 0);
   }

   public static int getSoundEventIndex(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 1));
   }

   public static boolean hasModelParticles(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasWorldParticles(MemorySegment mem, int offset) {
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

   public static DamageEffects toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static DamageEffects toObject(MemorySegment mem, int offset) {
      if (offset + 13 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("DamageEffects", offset + 13, (int)mem.byteSize());
      } else {
         ModelParticle[] modelParticles = null;
         if (hasModelParticles(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 5, 13, "ModelParticles");
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("ModelParticles", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("ModelParticles", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("ModelParticles", off + lenOffset + len, (int)mem.byteSize());
            }

            off += lenOffset;
            modelParticles = new ModelParticle[len];

            for (int i = 0; i < len; i++) {
               modelParticles[i] = ModelParticle.toObject(mem, off);
               off += modelParticles[i].computeSize();
            }
         }

         WorldParticle[] worldParticles = null;
         if (hasWorldParticles(mem, offset)) {
            int offx = offset + getValidatedOffset(mem, offset, 9, 13, "WorldParticles");
            long packedx = VarInt.getWithLength(mem, offx);
            int lenx = (int)packedx;
            if (lenx < 0) {
               throw ProtocolException.negativeLength("WorldParticles", lenx);
            }

            if (lenx > 4096000) {
               throw ProtocolException.arrayTooLong("WorldParticles", lenx, 4096000);
            }

            int lenOffset = (int)(packedx >>> 32);
            if (offx + lenOffset + lenx > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("WorldParticles", offx + lenOffset + lenx, (int)mem.byteSize());
            }

            offx += lenOffset;
            worldParticles = new WorldParticle[lenx];

            for (int i = 0; i < lenx; i++) {
               worldParticles[i] = WorldParticle.toObject(mem, offx);
               offx += worldParticles[i].computeSize();
            }
         }

         return new DamageEffects(modelParticles, worldParticles, mem.get(PacketIO.PROTO_INT, (long)(offset + 1)));
      }
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

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.modelParticles != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.worldParticles != null) {
         nullBits = (byte)(nullBits | 2);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 1), this.soundEventIndex);
      int varOffset = offset + 13;
      if (this.modelParticles != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), varOffset - offset - 13);
         if (this.modelParticles.length > 4096000) {
            throw ProtocolException.arrayTooLong("ModelParticles", this.modelParticles.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.modelParticles.length);
         int modelParticlesValueOffset = 0;

         for (int i = 0; i < this.modelParticles.length; i++) {
            modelParticlesValueOffset += this.modelParticles[i].serialize(mem, varOffset + modelParticlesValueOffset);
         }

         varOffset += modelParticlesValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), -1);
      }

      if (this.worldParticles != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 9), varOffset - offset - 13);
         if (this.worldParticles.length > 4096000) {
            throw ProtocolException.arrayTooLong("WorldParticles", this.worldParticles.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.worldParticles.length);
         int worldParticlesValueOffset = 0;

         for (int i = 0; i < this.worldParticles.length; i++) {
            worldParticlesValueOffset += this.worldParticles[i].serialize(mem, varOffset + worldParticlesValueOffset);
         }

         varOffset += worldParticlesValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 9), -1);
      }

      return varOffset - offset;
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
            if (modelParticlesOffset < 0 || modelParticlesOffset > buffer.writerIndex() - offset - 13) {
               return ValidationResult.error("Invalid offset for ModelParticles");
            }

            int pos = offset + 13 + modelParticlesOffset;
            int modelParticlesCount = VarInt.peek(buffer, pos);
            if (modelParticlesCount < 0) {
               return ValidationResult.error("Invalid array count for ModelParticles");
            }

            if (modelParticlesCount > 4096000) {
               return ValidationResult.error("ModelParticles exceeds max length 4096000");
            }

            pos += VarInt.size(modelParticlesCount);

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
            if (worldParticlesOffset < 0 || worldParticlesOffset > buffer.writerIndex() - offset - 13) {
               return ValidationResult.error("Invalid offset for WorldParticles");
            }

            int posx = offset + 13 + worldParticlesOffset;
            int worldParticlesCount = VarInt.peek(buffer, posx);
            if (worldParticlesCount < 0) {
               return ValidationResult.error("Invalid array count for WorldParticles");
            }

            if (worldParticlesCount > 4096000) {
               return ValidationResult.error("WorldParticles exceeds max length 4096000");
            }

            posx += VarInt.size(worldParticlesCount);

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
