package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Arrays;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemAppearanceCondition {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 18;
   public static final int VARIABLE_FIELD_COUNT = 5;
   public static final int VARIABLE_BLOCK_START = 38;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public ModelParticle[] particles;
   @Nullable
   public ModelParticle[] firstPersonParticles;
   @Nullable
   public String model;
   @Nullable
   public String texture;
   @Nullable
   public String modelVFXId;
   @Nullable
   public FloatRange condition;
   @Nonnull
   public ValueType conditionValueType = ValueType.Percent;
   public int localSoundEventId;
   public int worldSoundEventId;

   public ItemAppearanceCondition() {
   }

   public ItemAppearanceCondition(
      @Nullable ModelParticle[] particles,
      @Nullable ModelParticle[] firstPersonParticles,
      @Nullable String model,
      @Nullable String texture,
      @Nullable String modelVFXId,
      @Nullable FloatRange condition,
      @Nonnull ValueType conditionValueType,
      int localSoundEventId,
      int worldSoundEventId
   ) {
      this.particles = particles;
      this.firstPersonParticles = firstPersonParticles;
      this.model = model;
      this.texture = texture;
      this.modelVFXId = modelVFXId;
      this.condition = condition;
      this.conditionValueType = conditionValueType;
      this.localSoundEventId = localSoundEventId;
      this.worldSoundEventId = worldSoundEventId;
   }

   public ItemAppearanceCondition(@Nonnull ItemAppearanceCondition other) {
      this.particles = other.particles;
      this.firstPersonParticles = other.firstPersonParticles;
      this.model = other.model;
      this.texture = other.texture;
      this.modelVFXId = other.modelVFXId;
      this.condition = other.condition;
      this.conditionValueType = other.conditionValueType;
      this.localSoundEventId = other.localSoundEventId;
      this.worldSoundEventId = other.worldSoundEventId;
   }

   @Nonnull
   public static ItemAppearanceCondition deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 38) {
         throw ProtocolException.bufferTooSmall("ItemAppearanceCondition", 38, buf.readableBytes() - offset);
      } else {
         ItemAppearanceCondition obj = new ItemAppearanceCondition();
         byte nullBits = buf.getByte(offset);
         if ((nullBits & 1) != 0) {
            obj.condition = FloatRange.deserialize(buf, offset + 1);
         }

         obj.conditionValueType = ValueType.fromValue(buf.getByte(offset + 9));
         obj.localSoundEventId = buf.getIntLE(offset + 10);
         obj.worldSoundEventId = buf.getIntLE(offset + 14);
         if ((nullBits & 2) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 18);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 38) {
               throw ProtocolException.invalidOffset("Particles", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 38 + varPosBase0;
            int particlesCount = VarInt.peek(buf, varPos0);
            if (particlesCount < 0) {
               throw ProtocolException.invalidVarInt("Particles");
            }

            int varIntLen = VarInt.size(particlesCount);
            if (particlesCount > 4096000) {
               throw ProtocolException.arrayTooLong("Particles", particlesCount, 4096000);
            }

            if (varPos0 + varIntLen + particlesCount * 34L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Particles", varPos0 + varIntLen + particlesCount * 34, buf.readableBytes());
            }

            obj.particles = new ModelParticle[particlesCount];
            int elemPos = varPos0 + varIntLen;

            for (int i = 0; i < particlesCount; i++) {
               obj.particles[i] = ModelParticle.deserialize(buf, elemPos);
               elemPos += ModelParticle.computeBytesConsumed(buf, elemPos);
            }
         }

         if ((nullBits & 4) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 22);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 38) {
               throw ProtocolException.invalidOffset("FirstPersonParticles", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 38 + varPosBase1;
            int firstPersonParticlesCount = VarInt.peek(buf, varPos1);
            if (firstPersonParticlesCount < 0) {
               throw ProtocolException.invalidVarInt("FirstPersonParticles");
            }

            int varIntLenx = VarInt.size(firstPersonParticlesCount);
            if (firstPersonParticlesCount > 4096000) {
               throw ProtocolException.arrayTooLong("FirstPersonParticles", firstPersonParticlesCount, 4096000);
            }

            if (varPos1 + varIntLenx + firstPersonParticlesCount * 34L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("FirstPersonParticles", varPos1 + varIntLenx + firstPersonParticlesCount * 34, buf.readableBytes());
            }

            obj.firstPersonParticles = new ModelParticle[firstPersonParticlesCount];
            int elemPos = varPos1 + varIntLenx;

            for (int i = 0; i < firstPersonParticlesCount; i++) {
               obj.firstPersonParticles[i] = ModelParticle.deserialize(buf, elemPos);
               elemPos += ModelParticle.computeBytesConsumed(buf, elemPos);
            }
         }

         if ((nullBits & 8) != 0) {
            int varPosBase2 = buf.getIntLE(offset + 26);
            if (varPosBase2 < 0 || varPosBase2 > buf.writerIndex() - offset - 38) {
               throw ProtocolException.invalidOffset("Model", varPosBase2, buf.readableBytes());
            }

            int varPos2 = offset + 38 + varPosBase2;
            int modelLen = VarInt.peek(buf, varPos2);
            if (modelLen < 0) {
               throw ProtocolException.invalidVarInt("Model");
            }

            int modelVarIntLen = VarInt.size(modelLen);
            if (modelLen > 4096000) {
               throw ProtocolException.stringTooLong("Model", modelLen, 4096000);
            }

            if (varPos2 + modelVarIntLen + modelLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Model", varPos2 + modelVarIntLen + modelLen, buf.readableBytes());
            }

            obj.model = PacketIO.readVarString(buf, varPos2, PacketIO.UTF8);
         }

         if ((nullBits & 16) != 0) {
            int varPosBase3 = buf.getIntLE(offset + 30);
            if (varPosBase3 < 0 || varPosBase3 > buf.writerIndex() - offset - 38) {
               throw ProtocolException.invalidOffset("Texture", varPosBase3, buf.readableBytes());
            }

            int varPos3 = offset + 38 + varPosBase3;
            int textureLen = VarInt.peek(buf, varPos3);
            if (textureLen < 0) {
               throw ProtocolException.invalidVarInt("Texture");
            }

            int textureVarIntLen = VarInt.size(textureLen);
            if (textureLen > 4096000) {
               throw ProtocolException.stringTooLong("Texture", textureLen, 4096000);
            }

            if (varPos3 + textureVarIntLen + textureLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Texture", varPos3 + textureVarIntLen + textureLen, buf.readableBytes());
            }

            obj.texture = PacketIO.readVarString(buf, varPos3, PacketIO.UTF8);
         }

         if ((nullBits & 32) != 0) {
            int varPosBase4 = buf.getIntLE(offset + 34);
            if (varPosBase4 < 0 || varPosBase4 > buf.writerIndex() - offset - 38) {
               throw ProtocolException.invalidOffset("ModelVFXId", varPosBase4, buf.readableBytes());
            }

            int varPos4 = offset + 38 + varPosBase4;
            int modelVFXIdLen = VarInt.peek(buf, varPos4);
            if (modelVFXIdLen < 0) {
               throw ProtocolException.invalidVarInt("ModelVFXId");
            }

            int modelVFXIdVarIntLen = VarInt.size(modelVFXIdLen);
            if (modelVFXIdLen > 4096000) {
               throw ProtocolException.stringTooLong("ModelVFXId", modelVFXIdLen, 4096000);
            }

            if (varPos4 + modelVFXIdVarIntLen + modelVFXIdLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("ModelVFXId", varPos4 + modelVFXIdVarIntLen + modelVFXIdLen, buf.readableBytes());
            }

            obj.modelVFXId = PacketIO.readVarString(buf, varPos4, PacketIO.UTF8);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 38;
      if ((nullBits & 2) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 18);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 38) {
            throw ProtocolException.invalidOffset("Particles", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 38 + fieldOffset0;
         int arrLen = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            pos0 += ModelParticle.computeBytesConsumed(buf, pos0);
         }

         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 22);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 38) {
            throw ProtocolException.invalidOffset("FirstPersonParticles", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 38 + fieldOffset1;
         int arrLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            pos1 += ModelParticle.computeBytesConsumed(buf, pos1);
         }

         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 8) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 26);
         if (fieldOffset2 < 0 || fieldOffset2 > buf.writerIndex() - offset - 38) {
            throw ProtocolException.invalidOffset("Model", fieldOffset2, maxEnd);
         }

         int pos2 = offset + 38 + fieldOffset2;
         int sl = VarInt.peek(buf, pos2);
         pos2 += VarInt.size(sl) + sl;
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      if ((nullBits & 16) != 0) {
         int fieldOffset3 = buf.getIntLE(offset + 30);
         if (fieldOffset3 < 0 || fieldOffset3 > buf.writerIndex() - offset - 38) {
            throw ProtocolException.invalidOffset("Texture", fieldOffset3, maxEnd);
         }

         int pos3 = offset + 38 + fieldOffset3;
         int sl = VarInt.peek(buf, pos3);
         pos3 += VarInt.size(sl) + sl;
         if (pos3 - offset > maxEnd) {
            maxEnd = pos3 - offset;
         }
      }

      if ((nullBits & 32) != 0) {
         int fieldOffset4 = buf.getIntLE(offset + 34);
         if (fieldOffset4 < 0 || fieldOffset4 > buf.writerIndex() - offset - 38) {
            throw ProtocolException.invalidOffset("ModelVFXId", fieldOffset4, maxEnd);
         }

         int pos4 = offset + 38 + fieldOffset4;
         int sl = VarInt.peek(buf, pos4);
         pos4 += VarInt.size(sl) + sl;
         if (pos4 - offset > maxEnd) {
            maxEnd = pos4 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 38L;
   }

   @Nullable
   public static ModelParticle[] getParticles(MemorySegment mem) {
      return getParticles(mem, 0);
   }

   @Nullable
   public static ModelParticle[] getParticles(MemorySegment mem, int offset) {
      if (!hasParticles(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 18, 38, "Particles");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Particles", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("Particles", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Particles", off + lenOffset + len, (int)mem.byteSize());
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
   public static ModelParticle[] getFirstPersonParticles(MemorySegment mem) {
      return getFirstPersonParticles(mem, 0);
   }

   @Nullable
   public static ModelParticle[] getFirstPersonParticles(MemorySegment mem, int offset) {
      if (!hasFirstPersonParticles(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 22, 38, "FirstPersonParticles");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("FirstPersonParticles", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("FirstPersonParticles", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("FirstPersonParticles", off + lenOffset + len, (int)mem.byteSize());
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
   public static String getModel(MemorySegment mem) {
      return getModel(mem, 0);
   }

   @Nullable
   public static String getModel(MemorySegment mem, int offset) {
      return hasModel(mem, offset)
         ? PacketIO.readVarString("Model", mem, offset + getValidatedOffset(mem, offset, 26, 38, "Model"), 4096000, PacketIO.UTF8)
         : null;
   }

   @Nullable
   public static String getTexture(MemorySegment mem) {
      return getTexture(mem, 0);
   }

   @Nullable
   public static String getTexture(MemorySegment mem, int offset) {
      return hasTexture(mem, offset)
         ? PacketIO.readVarString("Texture", mem, offset + getValidatedOffset(mem, offset, 30, 38, "Texture"), 4096000, PacketIO.UTF8)
         : null;
   }

   @Nullable
   public static String getModelVFXId(MemorySegment mem) {
      return getModelVFXId(mem, 0);
   }

   @Nullable
   public static String getModelVFXId(MemorySegment mem, int offset) {
      return hasModelVFXId(mem, offset)
         ? PacketIO.readVarString("ModelVFXId", mem, offset + getValidatedOffset(mem, offset, 34, 38, "ModelVFXId"), 4096000, PacketIO.UTF8)
         : null;
   }

   @Nullable
   public static FloatRange getCondition(MemorySegment mem) {
      return getCondition(mem, 0);
   }

   @Nullable
   public static FloatRange getCondition(MemorySegment mem, int offset) {
      return hasCondition(mem, offset) ? FloatRange.toObject(mem, offset + 1) : null;
   }

   public static ValueType getConditionValueType(MemorySegment mem) {
      return getConditionValueType(mem, 0);
   }

   public static ValueType getConditionValueType(MemorySegment mem, int offset) {
      return ValueType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 9)));
   }

   public static int getLocalSoundEventId(MemorySegment mem) {
      return getLocalSoundEventId(mem, 0);
   }

   public static int getLocalSoundEventId(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 10));
   }

   public static int getWorldSoundEventId(MemorySegment mem) {
      return getWorldSoundEventId(mem, 0);
   }

   public static int getWorldSoundEventId(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 14));
   }

   public static boolean hasCondition(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasParticles(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasFirstPersonParticles(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 4) != 0;
   }

   public static boolean hasModel(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 8) != 0;
   }

   public static boolean hasTexture(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 16) != 0;
   }

   public static boolean hasModelVFXId(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 32) != 0;
   }

   private static int getValidatedOffset(MemorySegment buffer, int base, int slotPosition, int varBlockStart, String fieldName) {
      int offset = buffer.get(PacketIO.PROTO_INT, (long)(base + slotPosition));
      if (offset >= 0 && offset <= buffer.byteSize() - base - varBlockStart) {
         return varBlockStart + offset;
      } else {
         throw ProtocolException.invalidOffset(fieldName, offset, (int)buffer.byteSize());
      }
   }

   public static ItemAppearanceCondition toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static ItemAppearanceCondition toObject(MemorySegment mem, int offset) {
      if (offset + 38 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("ItemAppearanceCondition", offset + 38, (int)mem.byteSize());
      } else {
         ModelParticle[] particles = null;
         if (hasParticles(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 18, 38, "Particles");
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Particles", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("Particles", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Particles", off + lenOffset + len, (int)mem.byteSize());
            }

            off += lenOffset;
            particles = new ModelParticle[len];

            for (int i = 0; i < len; i++) {
               particles[i] = ModelParticle.toObject(mem, off);
               off += particles[i].computeSize();
            }
         }

         ModelParticle[] firstPersonParticles = null;
         if (hasFirstPersonParticles(mem, offset)) {
            int offx = offset + getValidatedOffset(mem, offset, 22, 38, "FirstPersonParticles");
            long packedx = VarInt.getWithLength(mem, offx);
            int lenx = (int)packedx;
            if (lenx < 0) {
               throw ProtocolException.negativeLength("FirstPersonParticles", lenx);
            }

            if (lenx > 4096000) {
               throw ProtocolException.arrayTooLong("FirstPersonParticles", lenx, 4096000);
            }

            int lenOffset = (int)(packedx >>> 32);
            if (offx + lenOffset + lenx > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("FirstPersonParticles", offx + lenOffset + lenx, (int)mem.byteSize());
            }

            offx += lenOffset;
            firstPersonParticles = new ModelParticle[lenx];

            for (int i = 0; i < lenx; i++) {
               firstPersonParticles[i] = ModelParticle.toObject(mem, offx);
               offx += firstPersonParticles[i].computeSize();
            }
         }

         return new ItemAppearanceCondition(
            particles,
            firstPersonParticles,
            hasModel(mem, offset)
               ? PacketIO.readVarString("Model", mem, offset + getValidatedOffset(mem, offset, 26, 38, "Model"), 4096000, PacketIO.UTF8)
               : null,
            hasTexture(mem, offset)
               ? PacketIO.readVarString("Texture", mem, offset + getValidatedOffset(mem, offset, 30, 38, "Texture"), 4096000, PacketIO.UTF8)
               : null,
            hasModelVFXId(mem, offset)
               ? PacketIO.readVarString("ModelVFXId", mem, offset + getValidatedOffset(mem, offset, 34, 38, "ModelVFXId"), 4096000, PacketIO.UTF8)
               : null,
            hasCondition(mem, offset) ? FloatRange.toObject(mem, offset + 1) : null,
            ValueType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 9))),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 10)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 14))
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.condition != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.particles != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.firstPersonParticles != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.model != null) {
         nullBits = (byte)(nullBits | 8);
      }

      if (this.texture != null) {
         nullBits = (byte)(nullBits | 16);
      }

      if (this.modelVFXId != null) {
         nullBits = (byte)(nullBits | 32);
      }

      buf.writeByte(nullBits);
      if (this.condition != null) {
         this.condition.serialize(buf);
      } else {
         buf.writeZero(8);
      }

      buf.writeByte(this.conditionValueType.getValue());
      buf.writeIntLE(this.localSoundEventId);
      buf.writeIntLE(this.worldSoundEventId);
      int particlesOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int firstPersonParticlesOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int modelOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int textureOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int modelVFXIdOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.particles != null) {
         buf.setIntLE(particlesOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.particles.length > 4096000) {
            throw ProtocolException.arrayTooLong("Particles", this.particles.length, 4096000);
         }

         VarInt.write(buf, this.particles.length);

         for (ModelParticle item : this.particles) {
            item.serialize(buf);
         }
      } else {
         buf.setIntLE(particlesOffsetSlot, -1);
      }

      if (this.firstPersonParticles != null) {
         buf.setIntLE(firstPersonParticlesOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.firstPersonParticles.length > 4096000) {
            throw ProtocolException.arrayTooLong("FirstPersonParticles", this.firstPersonParticles.length, 4096000);
         }

         VarInt.write(buf, this.firstPersonParticles.length);

         for (ModelParticle item : this.firstPersonParticles) {
            item.serialize(buf);
         }
      } else {
         buf.setIntLE(firstPersonParticlesOffsetSlot, -1);
      }

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

      if (this.modelVFXId != null) {
         buf.setIntLE(modelVFXIdOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.modelVFXId, 4096000);
      } else {
         buf.setIntLE(modelVFXIdOffsetSlot, -1);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.condition != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.particles != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.firstPersonParticles != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.model != null) {
         nullBits = (byte)(nullBits | 8);
      }

      if (this.texture != null) {
         nullBits = (byte)(nullBits | 16);
      }

      if (this.modelVFXId != null) {
         nullBits = (byte)(nullBits | 32);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      if (this.condition != null) {
         this.condition.serialize(mem, offset + 1);
      } else {
         mem.asSlice(offset + 1, 8L).fill((byte)0);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 9), (byte)this.conditionValueType.getValue());
      mem.set(PacketIO.PROTO_INT, (long)(offset + 10), this.localSoundEventId);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 14), this.worldSoundEventId);
      int varOffset = offset + 38;
      if (this.particles != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 18), varOffset - offset - 38);
         if (this.particles.length > 4096000) {
            throw ProtocolException.arrayTooLong("Particles", this.particles.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.particles.length);
         int particlesValueOffset = 0;

         for (int i = 0; i < this.particles.length; i++) {
            particlesValueOffset += this.particles[i].serialize(mem, varOffset + particlesValueOffset);
         }

         varOffset += particlesValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 18), -1);
      }

      if (this.firstPersonParticles != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 22), varOffset - offset - 38);
         if (this.firstPersonParticles.length > 4096000) {
            throw ProtocolException.arrayTooLong("FirstPersonParticles", this.firstPersonParticles.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.firstPersonParticles.length);
         int firstPersonParticlesValueOffset = 0;

         for (int i = 0; i < this.firstPersonParticles.length; i++) {
            firstPersonParticlesValueOffset += this.firstPersonParticles[i].serialize(mem, varOffset + firstPersonParticlesValueOffset);
         }

         varOffset += firstPersonParticlesValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 22), -1);
      }

      if (this.model != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 26), varOffset - offset - 38);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.model, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 26), -1);
      }

      if (this.texture != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 30), varOffset - offset - 38);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.texture, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 30), -1);
      }

      if (this.modelVFXId != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 34), varOffset - offset - 38);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.modelVFXId, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 34), -1);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 38;
      if (this.particles != null) {
         int particlesSize = 0;

         for (ModelParticle elem : this.particles) {
            particlesSize += elem.computeSize();
         }

         size += VarInt.size(this.particles.length) + particlesSize;
      }

      if (this.firstPersonParticles != null) {
         int firstPersonParticlesSize = 0;

         for (ModelParticle elem : this.firstPersonParticles) {
            firstPersonParticlesSize += elem.computeSize();
         }

         size += VarInt.size(this.firstPersonParticles.length) + firstPersonParticlesSize;
      }

      if (this.model != null) {
         size += PacketIO.stringSize(this.model);
      }

      if (this.texture != null) {
         size += PacketIO.stringSize(this.texture);
      }

      if (this.modelVFXId != null) {
         size += PacketIO.stringSize(this.modelVFXId);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 38) {
         return ValidationResult.error("Buffer too small: expected at least 38 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int v = buffer.getByte(offset + 9) & 255;
         if (v >= 2) {
            return ValidationResult.error("Invalid ValueType value for ConditionValueType");
         } else {
            if ((nullBits & 2) != 0) {
               v = buffer.getIntLE(offset + 18);
               if (v < 0 || v > buffer.writerIndex() - offset - 38) {
                  return ValidationResult.error("Invalid offset for Particles");
               }

               int pos = offset + 38 + v;
               int particlesCount = VarInt.peek(buffer, pos);
               if (particlesCount < 0) {
                  return ValidationResult.error("Invalid array count for Particles");
               }

               if (particlesCount > 4096000) {
                  return ValidationResult.error("Particles exceeds max length 4096000");
               }

               pos += VarInt.size(particlesCount);

               for (int i = 0; i < particlesCount; i++) {
                  ValidationResult structResult = ModelParticle.validateStructure(buffer, pos);
                  if (!structResult.isValid()) {
                     return ValidationResult.error("Invalid ModelParticle in Particles[" + i + "]: " + structResult.error());
                  }

                  pos += ModelParticle.computeBytesConsumed(buffer, pos);
               }
            }

            if ((nullBits & 4) != 0) {
               v = buffer.getIntLE(offset + 22);
               if (v < 0 || v > buffer.writerIndex() - offset - 38) {
                  return ValidationResult.error("Invalid offset for FirstPersonParticles");
               }

               int posx = offset + 38 + v;
               int firstPersonParticlesCount = VarInt.peek(buffer, posx);
               if (firstPersonParticlesCount < 0) {
                  return ValidationResult.error("Invalid array count for FirstPersonParticles");
               }

               if (firstPersonParticlesCount > 4096000) {
                  return ValidationResult.error("FirstPersonParticles exceeds max length 4096000");
               }

               posx += VarInt.size(firstPersonParticlesCount);

               for (int i = 0; i < firstPersonParticlesCount; i++) {
                  ValidationResult structResult = ModelParticle.validateStructure(buffer, posx);
                  if (!structResult.isValid()) {
                     return ValidationResult.error("Invalid ModelParticle in FirstPersonParticles[" + i + "]: " + structResult.error());
                  }

                  posx += ModelParticle.computeBytesConsumed(buffer, posx);
               }
            }

            if ((nullBits & 8) != 0) {
               v = buffer.getIntLE(offset + 26);
               if (v < 0 || v > buffer.writerIndex() - offset - 38) {
                  return ValidationResult.error("Invalid offset for Model");
               }

               int posxx = offset + 38 + v;
               int modelLen = VarInt.peek(buffer, posxx);
               if (modelLen < 0) {
                  return ValidationResult.error("Invalid string length for Model");
               }

               if (modelLen > 4096000) {
                  return ValidationResult.error("Model exceeds max length 4096000");
               }

               posxx += VarInt.size(modelLen);
               posxx += modelLen;
               if (posxx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading Model");
               }
            }

            if ((nullBits & 16) != 0) {
               v = buffer.getIntLE(offset + 30);
               if (v < 0 || v > buffer.writerIndex() - offset - 38) {
                  return ValidationResult.error("Invalid offset for Texture");
               }

               int posxxx = offset + 38 + v;
               int textureLen = VarInt.peek(buffer, posxxx);
               if (textureLen < 0) {
                  return ValidationResult.error("Invalid string length for Texture");
               }

               if (textureLen > 4096000) {
                  return ValidationResult.error("Texture exceeds max length 4096000");
               }

               posxxx += VarInt.size(textureLen);
               posxxx += textureLen;
               if (posxxx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading Texture");
               }
            }

            if ((nullBits & 32) != 0) {
               v = buffer.getIntLE(offset + 34);
               if (v < 0 || v > buffer.writerIndex() - offset - 38) {
                  return ValidationResult.error("Invalid offset for ModelVFXId");
               }

               int posxxxx = offset + 38 + v;
               int modelVFXIdLen = VarInt.peek(buffer, posxxxx);
               if (modelVFXIdLen < 0) {
                  return ValidationResult.error("Invalid string length for ModelVFXId");
               }

               if (modelVFXIdLen > 4096000) {
                  return ValidationResult.error("ModelVFXId exceeds max length 4096000");
               }

               posxxxx += VarInt.size(modelVFXIdLen);
               posxxxx += modelVFXIdLen;
               if (posxxxx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading ModelVFXId");
               }
            }

            return ValidationResult.OK;
         }
      }
   }

   public ItemAppearanceCondition clone() {
      ItemAppearanceCondition copy = new ItemAppearanceCondition();
      copy.particles = this.particles != null ? Arrays.stream(this.particles).map(e -> e.clone()).toArray(ModelParticle[]::new) : null;
      copy.firstPersonParticles = this.firstPersonParticles != null
         ? Arrays.stream(this.firstPersonParticles).map(e -> e.clone()).toArray(ModelParticle[]::new)
         : null;
      copy.model = this.model;
      copy.texture = this.texture;
      copy.modelVFXId = this.modelVFXId;
      copy.condition = this.condition != null ? this.condition.clone() : null;
      copy.conditionValueType = this.conditionValueType;
      copy.localSoundEventId = this.localSoundEventId;
      copy.worldSoundEventId = this.worldSoundEventId;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ItemAppearanceCondition other)
            ? false
            : Arrays.equals((Object[])this.particles, (Object[])other.particles)
               && Arrays.equals((Object[])this.firstPersonParticles, (Object[])other.firstPersonParticles)
               && Objects.equals(this.model, other.model)
               && Objects.equals(this.texture, other.texture)
               && Objects.equals(this.modelVFXId, other.modelVFXId)
               && Objects.equals(this.condition, other.condition)
               && Objects.equals(this.conditionValueType, other.conditionValueType)
               && this.localSoundEventId == other.localSoundEventId
               && this.worldSoundEventId == other.worldSoundEventId;
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Arrays.hashCode((Object[])this.particles);
      result = 31 * result + Arrays.hashCode((Object[])this.firstPersonParticles);
      result = 31 * result + Objects.hashCode(this.model);
      result = 31 * result + Objects.hashCode(this.texture);
      result = 31 * result + Objects.hashCode(this.modelVFXId);
      result = 31 * result + Objects.hashCode(this.condition);
      result = 31 * result + Objects.hashCode(this.conditionValueType);
      result = 31 * result + Integer.hashCode(this.localSoundEventId);
      return 31 * result + Integer.hashCode(this.worldSoundEventId);
   }
}
