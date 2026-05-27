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

public class Fluid {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 23;
   public static final int VARIABLE_FIELD_COUNT = 6;
   public static final int VARIABLE_BLOCK_START = 47;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public String id;
   public int maxFluidLevel;
   @Nullable
   public BlockTextures[] cubeTextures;
   public boolean requiresAlphaBlending;
   @Nonnull
   public Opacity opacity = Opacity.Solid;
   @Nullable
   public ShaderType[] shaderEffect;
   @Nullable
   public ColorLight light;
   @Nullable
   public ModelParticle[] particles;
   @Nonnull
   public FluidDrawType drawType = FluidDrawType.None;
   public int fluidFXIndex;
   public int blockSoundSetIndex;
   @Nullable
   public String blockParticleSetId;
   @Nullable
   public Color particleColor;
   @Nullable
   public int[] tagIndexes;

   public Fluid() {
   }

   public Fluid(
      @Nullable String id,
      int maxFluidLevel,
      @Nullable BlockTextures[] cubeTextures,
      boolean requiresAlphaBlending,
      @Nonnull Opacity opacity,
      @Nullable ShaderType[] shaderEffect,
      @Nullable ColorLight light,
      @Nullable ModelParticle[] particles,
      @Nonnull FluidDrawType drawType,
      int fluidFXIndex,
      int blockSoundSetIndex,
      @Nullable String blockParticleSetId,
      @Nullable Color particleColor,
      @Nullable int[] tagIndexes
   ) {
      this.id = id;
      this.maxFluidLevel = maxFluidLevel;
      this.cubeTextures = cubeTextures;
      this.requiresAlphaBlending = requiresAlphaBlending;
      this.opacity = opacity;
      this.shaderEffect = shaderEffect;
      this.light = light;
      this.particles = particles;
      this.drawType = drawType;
      this.fluidFXIndex = fluidFXIndex;
      this.blockSoundSetIndex = blockSoundSetIndex;
      this.blockParticleSetId = blockParticleSetId;
      this.particleColor = particleColor;
      this.tagIndexes = tagIndexes;
   }

   public Fluid(@Nonnull Fluid other) {
      this.id = other.id;
      this.maxFluidLevel = other.maxFluidLevel;
      this.cubeTextures = other.cubeTextures;
      this.requiresAlphaBlending = other.requiresAlphaBlending;
      this.opacity = other.opacity;
      this.shaderEffect = other.shaderEffect;
      this.light = other.light;
      this.particles = other.particles;
      this.drawType = other.drawType;
      this.fluidFXIndex = other.fluidFXIndex;
      this.blockSoundSetIndex = other.blockSoundSetIndex;
      this.blockParticleSetId = other.blockParticleSetId;
      this.particleColor = other.particleColor;
      this.tagIndexes = other.tagIndexes;
   }

   @Nonnull
   public static Fluid deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 47) {
         throw ProtocolException.bufferTooSmall("Fluid", 47, buf.readableBytes() - offset);
      } else {
         Fluid obj = new Fluid();
         byte nullBits = buf.getByte(offset);
         obj.maxFluidLevel = buf.getIntLE(offset + 1);
         obj.requiresAlphaBlending = buf.getByte(offset + 5) != 0;
         obj.opacity = Opacity.fromValue(buf.getByte(offset + 6));
         if ((nullBits & 1) != 0) {
            obj.light = ColorLight.deserialize(buf, offset + 7);
         }

         obj.drawType = FluidDrawType.fromValue(buf.getByte(offset + 11));
         obj.fluidFXIndex = buf.getIntLE(offset + 12);
         obj.blockSoundSetIndex = buf.getIntLE(offset + 16);
         if ((nullBits & 2) != 0) {
            obj.particleColor = Color.deserialize(buf, offset + 20);
         }

         if ((nullBits & 4) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 23);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 47) {
               throw ProtocolException.invalidOffset("Id", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 47 + varPosBase0;
            int idLen = VarInt.peek(buf, varPos0);
            if (idLen < 0) {
               throw ProtocolException.invalidVarInt("Id");
            }

            int idVarIntLen = VarInt.size(idLen);
            if (idLen > 4096000) {
               throw ProtocolException.stringTooLong("Id", idLen, 4096000);
            }

            if (varPos0 + idVarIntLen + idLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Id", varPos0 + idVarIntLen + idLen, buf.readableBytes());
            }

            obj.id = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
         }

         if ((nullBits & 8) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 27);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 47) {
               throw ProtocolException.invalidOffset("CubeTextures", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 47 + varPosBase1;
            int cubeTexturesCount = VarInt.peek(buf, varPos1);
            if (cubeTexturesCount < 0) {
               throw ProtocolException.invalidVarInt("CubeTextures");
            }

            int varIntLen = VarInt.size(cubeTexturesCount);
            if (cubeTexturesCount > 4096000) {
               throw ProtocolException.arrayTooLong("CubeTextures", cubeTexturesCount, 4096000);
            }

            if (varPos1 + varIntLen + cubeTexturesCount * 5L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("CubeTextures", varPos1 + varIntLen + cubeTexturesCount * 5, buf.readableBytes());
            }

            obj.cubeTextures = new BlockTextures[cubeTexturesCount];
            int elemPos = varPos1 + varIntLen;

            for (int i = 0; i < cubeTexturesCount; i++) {
               obj.cubeTextures[i] = BlockTextures.deserialize(buf, elemPos);
               elemPos += BlockTextures.computeBytesConsumed(buf, elemPos);
            }
         }

         if ((nullBits & 16) != 0) {
            int varPosBase2 = buf.getIntLE(offset + 31);
            if (varPosBase2 < 0 || varPosBase2 > buf.writerIndex() - offset - 47) {
               throw ProtocolException.invalidOffset("ShaderEffect", varPosBase2, buf.readableBytes());
            }

            int varPos2 = offset + 47 + varPosBase2;
            int shaderEffectCount = VarInt.peek(buf, varPos2);
            if (shaderEffectCount < 0) {
               throw ProtocolException.invalidVarInt("ShaderEffect");
            }

            int varIntLenx = VarInt.size(shaderEffectCount);
            if (shaderEffectCount > 4096000) {
               throw ProtocolException.arrayTooLong("ShaderEffect", shaderEffectCount, 4096000);
            }

            if (varPos2 + varIntLenx + shaderEffectCount * 1L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("ShaderEffect", varPos2 + varIntLenx + shaderEffectCount * 1, buf.readableBytes());
            }

            obj.shaderEffect = new ShaderType[shaderEffectCount];
            int elemPos = varPos2 + varIntLenx;

            for (int i = 0; i < shaderEffectCount; i++) {
               obj.shaderEffect[i] = ShaderType.fromValue(buf.getByte(elemPos));
               elemPos++;
            }
         }

         if ((nullBits & 32) != 0) {
            int varPosBase3 = buf.getIntLE(offset + 35);
            if (varPosBase3 < 0 || varPosBase3 > buf.writerIndex() - offset - 47) {
               throw ProtocolException.invalidOffset("Particles", varPosBase3, buf.readableBytes());
            }

            int varPos3 = offset + 47 + varPosBase3;
            int particlesCount = VarInt.peek(buf, varPos3);
            if (particlesCount < 0) {
               throw ProtocolException.invalidVarInt("Particles");
            }

            int varIntLenxx = VarInt.size(particlesCount);
            if (particlesCount > 4096000) {
               throw ProtocolException.arrayTooLong("Particles", particlesCount, 4096000);
            }

            if (varPos3 + varIntLenxx + particlesCount * 34L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Particles", varPos3 + varIntLenxx + particlesCount * 34, buf.readableBytes());
            }

            obj.particles = new ModelParticle[particlesCount];
            int elemPos = varPos3 + varIntLenxx;

            for (int i = 0; i < particlesCount; i++) {
               obj.particles[i] = ModelParticle.deserialize(buf, elemPos);
               elemPos += ModelParticle.computeBytesConsumed(buf, elemPos);
            }
         }

         if ((nullBits & 64) != 0) {
            int varPosBase4 = buf.getIntLE(offset + 39);
            if (varPosBase4 < 0 || varPosBase4 > buf.writerIndex() - offset - 47) {
               throw ProtocolException.invalidOffset("BlockParticleSetId", varPosBase4, buf.readableBytes());
            }

            int varPos4 = offset + 47 + varPosBase4;
            int blockParticleSetIdLen = VarInt.peek(buf, varPos4);
            if (blockParticleSetIdLen < 0) {
               throw ProtocolException.invalidVarInt("BlockParticleSetId");
            }

            int blockParticleSetIdVarIntLen = VarInt.size(blockParticleSetIdLen);
            if (blockParticleSetIdLen > 4096000) {
               throw ProtocolException.stringTooLong("BlockParticleSetId", blockParticleSetIdLen, 4096000);
            }

            if (varPos4 + blockParticleSetIdVarIntLen + blockParticleSetIdLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("BlockParticleSetId", varPos4 + blockParticleSetIdVarIntLen + blockParticleSetIdLen, buf.readableBytes());
            }

            obj.blockParticleSetId = PacketIO.readVarString(buf, varPos4, PacketIO.UTF8);
         }

         if ((nullBits & 128) != 0) {
            int varPosBase5 = buf.getIntLE(offset + 43);
            if (varPosBase5 < 0 || varPosBase5 > buf.writerIndex() - offset - 47) {
               throw ProtocolException.invalidOffset("TagIndexes", varPosBase5, buf.readableBytes());
            }

            int varPos5 = offset + 47 + varPosBase5;
            int tagIndexesCount = VarInt.peek(buf, varPos5);
            if (tagIndexesCount < 0) {
               throw ProtocolException.invalidVarInt("TagIndexes");
            }

            int varIntLenxxx = VarInt.size(tagIndexesCount);
            if (tagIndexesCount > 4096000) {
               throw ProtocolException.arrayTooLong("TagIndexes", tagIndexesCount, 4096000);
            }

            if (varPos5 + varIntLenxxx + tagIndexesCount * 4L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("TagIndexes", varPos5 + varIntLenxxx + tagIndexesCount * 4, buf.readableBytes());
            }

            obj.tagIndexes = new int[tagIndexesCount];

            for (int i = 0; i < tagIndexesCount; i++) {
               obj.tagIndexes[i] = buf.getIntLE(varPos5 + varIntLenxxx + i * 4);
            }
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 47;
      if ((nullBits & 4) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 23);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 47) {
            throw ProtocolException.invalidOffset("Id", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 47 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 8) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 27);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 47) {
            throw ProtocolException.invalidOffset("CubeTextures", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 47 + fieldOffset1;
         int arrLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            pos1 += BlockTextures.computeBytesConsumed(buf, pos1);
         }

         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 16) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 31);
         if (fieldOffset2 < 0 || fieldOffset2 > buf.writerIndex() - offset - 47) {
            throw ProtocolException.invalidOffset("ShaderEffect", fieldOffset2, maxEnd);
         }

         int pos2 = offset + 47 + fieldOffset2;
         int arrLen = VarInt.peek(buf, pos2);
         pos2 += VarInt.size(arrLen) + arrLen * 1;
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      if ((nullBits & 32) != 0) {
         int fieldOffset3 = buf.getIntLE(offset + 35);
         if (fieldOffset3 < 0 || fieldOffset3 > buf.writerIndex() - offset - 47) {
            throw ProtocolException.invalidOffset("Particles", fieldOffset3, maxEnd);
         }

         int pos3 = offset + 47 + fieldOffset3;
         int arrLen = VarInt.peek(buf, pos3);
         pos3 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            pos3 += ModelParticle.computeBytesConsumed(buf, pos3);
         }

         if (pos3 - offset > maxEnd) {
            maxEnd = pos3 - offset;
         }
      }

      if ((nullBits & 64) != 0) {
         int fieldOffset4 = buf.getIntLE(offset + 39);
         if (fieldOffset4 < 0 || fieldOffset4 > buf.writerIndex() - offset - 47) {
            throw ProtocolException.invalidOffset("BlockParticleSetId", fieldOffset4, maxEnd);
         }

         int pos4 = offset + 47 + fieldOffset4;
         int sl = VarInt.peek(buf, pos4);
         pos4 += VarInt.size(sl) + sl;
         if (pos4 - offset > maxEnd) {
            maxEnd = pos4 - offset;
         }
      }

      if ((nullBits & 128) != 0) {
         int fieldOffset5 = buf.getIntLE(offset + 43);
         if (fieldOffset5 < 0 || fieldOffset5 > buf.writerIndex() - offset - 47) {
            throw ProtocolException.invalidOffset("TagIndexes", fieldOffset5, maxEnd);
         }

         int pos5 = offset + 47 + fieldOffset5;
         int arrLen = VarInt.peek(buf, pos5);
         pos5 += VarInt.size(arrLen) + arrLen * 4;
         if (pos5 - offset > maxEnd) {
            maxEnd = pos5 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 47L;
   }

   @Nullable
   public static String getId(MemorySegment mem) {
      return getId(mem, 0);
   }

   @Nullable
   public static String getId(MemorySegment mem, int offset) {
      return hasId(mem, offset) ? PacketIO.readVarString("Id", mem, offset + getValidatedOffset(mem, offset, 23, 47, "Id"), 4096000, PacketIO.UTF8) : null;
   }

   public static int getMaxFluidLevel(MemorySegment mem) {
      return getMaxFluidLevel(mem, 0);
   }

   public static int getMaxFluidLevel(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 1));
   }

   @Nullable
   public static BlockTextures[] getCubeTextures(MemorySegment mem) {
      return getCubeTextures(mem, 0);
   }

   @Nullable
   public static BlockTextures[] getCubeTextures(MemorySegment mem, int offset) {
      if (!hasCubeTextures(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 27, 47, "CubeTextures");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("CubeTextures", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("CubeTextures", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("CubeTextures", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               BlockTextures[] data = new BlockTextures[len];

               for (int i = 0; i < len; i++) {
                  data[i] = BlockTextures.toObject(mem, off);
                  off += data[i].computeSize();
               }

               return data;
            }
         }
      }
   }

   public static boolean getRequiresAlphaBlending(MemorySegment mem) {
      return getRequiresAlphaBlending(mem, 0);
   }

   public static boolean getRequiresAlphaBlending(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 5));
   }

   public static Opacity getOpacity(MemorySegment mem) {
      return getOpacity(mem, 0);
   }

   public static Opacity getOpacity(MemorySegment mem, int offset) {
      return Opacity.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 6)));
   }

   @Nullable
   public static ShaderType[] getShaderEffect(MemorySegment mem) {
      return getShaderEffect(mem, 0);
   }

   @Nullable
   public static ShaderType[] getShaderEffect(MemorySegment mem, int offset) {
      if (!hasShaderEffect(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 31, 47, "ShaderEffect");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("ShaderEffect", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("ShaderEffect", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 1L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("ShaderEffect", off + lenOffset + len * 1, (int)mem.byteSize());
            } else {
               off += lenOffset;
               ShaderType[] data = new ShaderType[len];

               for (int i = 0; i < len; i++) {
                  data[i] = ShaderType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(off + i * 1)));
               }

               return data;
            }
         }
      }
   }

   @Nullable
   public static ColorLight getLight(MemorySegment mem) {
      return getLight(mem, 0);
   }

   @Nullable
   public static ColorLight getLight(MemorySegment mem, int offset) {
      return hasLight(mem, offset) ? ColorLight.toObject(mem, offset + 7) : null;
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
         int off = offset + getValidatedOffset(mem, offset, 35, 47, "Particles");
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

   public static FluidDrawType getDrawType(MemorySegment mem) {
      return getDrawType(mem, 0);
   }

   public static FluidDrawType getDrawType(MemorySegment mem, int offset) {
      return FluidDrawType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 11)));
   }

   public static int getFluidFXIndex(MemorySegment mem) {
      return getFluidFXIndex(mem, 0);
   }

   public static int getFluidFXIndex(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 12));
   }

   public static int getBlockSoundSetIndex(MemorySegment mem) {
      return getBlockSoundSetIndex(mem, 0);
   }

   public static int getBlockSoundSetIndex(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 16));
   }

   @Nullable
   public static String getBlockParticleSetId(MemorySegment mem) {
      return getBlockParticleSetId(mem, 0);
   }

   @Nullable
   public static String getBlockParticleSetId(MemorySegment mem, int offset) {
      return hasBlockParticleSetId(mem, offset)
         ? PacketIO.readVarString("BlockParticleSetId", mem, offset + getValidatedOffset(mem, offset, 39, 47, "BlockParticleSetId"), 4096000, PacketIO.UTF8)
         : null;
   }

   @Nullable
   public static Color getParticleColor(MemorySegment mem) {
      return getParticleColor(mem, 0);
   }

   @Nullable
   public static Color getParticleColor(MemorySegment mem, int offset) {
      return hasParticleColor(mem, offset) ? Color.toObject(mem, offset + 20) : null;
   }

   @Nullable
   public static int[] getTagIndexes(MemorySegment mem) {
      return getTagIndexes(mem, 0);
   }

   @Nullable
   public static int[] getTagIndexes(MemorySegment mem, int offset) {
      if (!hasTagIndexes(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 43, 47, "TagIndexes");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("TagIndexes", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("TagIndexes", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 4L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("TagIndexes", off + lenOffset + len * 4, (int)mem.byteSize());
            } else {
               off += lenOffset;
               int[] data = new int[len];
               MemorySegment.copy(mem, PacketIO.PROTO_INT, off, data, 0, len);
               return data;
            }
         }
      }
   }

   public static boolean hasLight(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasParticleColor(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasId(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 4) != 0;
   }

   public static boolean hasCubeTextures(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 8) != 0;
   }

   public static boolean hasShaderEffect(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 16) != 0;
   }

   public static boolean hasParticles(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 32) != 0;
   }

   public static boolean hasBlockParticleSetId(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 64) != 0;
   }

   public static boolean hasTagIndexes(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 128) != 0;
   }

   private static int getValidatedOffset(MemorySegment buffer, int base, int slotPosition, int varBlockStart, String fieldName) {
      int offset = buffer.get(PacketIO.PROTO_INT, (long)(base + slotPosition));
      if (offset >= 0 && offset <= buffer.byteSize() - base - varBlockStart) {
         return varBlockStart + offset;
      } else {
         throw ProtocolException.invalidOffset(fieldName, offset, (int)buffer.byteSize());
      }
   }

   public static Fluid toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static Fluid toObject(MemorySegment mem, int offset) {
      if (offset + 47 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("Fluid", offset + 47, (int)mem.byteSize());
      } else {
         BlockTextures[] cubeTextures = null;
         if (hasCubeTextures(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 27, 47, "CubeTextures");
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("CubeTextures", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("CubeTextures", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("CubeTextures", off + lenOffset + len, (int)mem.byteSize());
            }

            off += lenOffset;
            cubeTextures = new BlockTextures[len];

            for (int i = 0; i < len; i++) {
               cubeTextures[i] = BlockTextures.toObject(mem, off);
               off += cubeTextures[i].computeSize();
            }
         }

         ShaderType[] shaderEffect = null;
         if (hasShaderEffect(mem, offset)) {
            int offx = offset + getValidatedOffset(mem, offset, 31, 47, "ShaderEffect");
            long packedx = VarInt.getWithLength(mem, offx);
            int lenx = (int)packedx;
            if (lenx < 0) {
               throw ProtocolException.negativeLength("ShaderEffect", lenx);
            }

            if (lenx > 4096000) {
               throw ProtocolException.arrayTooLong("ShaderEffect", lenx, 4096000);
            }

            int lenOffset = (int)(packedx >>> 32);
            if (offx + lenOffset + lenx * 1L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("ShaderEffect", offx + lenOffset + lenx * 1, (int)mem.byteSize());
            }

            offx += lenOffset;
            shaderEffect = new ShaderType[lenx];

            for (int i = 0; i < lenx; i++) {
               shaderEffect[i] = ShaderType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offx + i * 1)));
            }
         }

         ModelParticle[] particles = null;
         if (hasParticles(mem, offset)) {
            int offxx = offset + getValidatedOffset(mem, offset, 35, 47, "Particles");
            long packedxx = VarInt.getWithLength(mem, offxx);
            int lenxx = (int)packedxx;
            if (lenxx < 0) {
               throw ProtocolException.negativeLength("Particles", lenxx);
            }

            if (lenxx > 4096000) {
               throw ProtocolException.arrayTooLong("Particles", lenxx, 4096000);
            }

            int lenOffset = (int)(packedxx >>> 32);
            if (offxx + lenOffset + lenxx > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Particles", offxx + lenOffset + lenxx, (int)mem.byteSize());
            }

            offxx += lenOffset;
            particles = new ModelParticle[lenxx];

            for (int i = 0; i < lenxx; i++) {
               particles[i] = ModelParticle.toObject(mem, offxx);
               offxx += particles[i].computeSize();
            }
         }

         int[] tagIndexes = null;
         if (hasTagIndexes(mem, offset)) {
            int offxxx = offset + getValidatedOffset(mem, offset, 43, 47, "TagIndexes");
            long packedxxx = VarInt.getWithLength(mem, offxxx);
            int lenxxx = (int)packedxxx;
            if (lenxxx < 0) {
               throw ProtocolException.negativeLength("TagIndexes", lenxxx);
            }

            if (lenxxx > 4096000) {
               throw ProtocolException.arrayTooLong("TagIndexes", lenxxx, 4096000);
            }

            int lenOffset = (int)(packedxxx >>> 32);
            if (offxxx + lenOffset + lenxxx * 4L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("TagIndexes", offxxx + lenOffset + lenxxx * 4, (int)mem.byteSize());
            }

            offxxx += lenOffset;
            tagIndexes = new int[lenxxx];
            MemorySegment.copy(mem, PacketIO.PROTO_INT, offxxx, tagIndexes, 0, lenxxx);
         }

         return new Fluid(
            hasId(mem, offset) ? PacketIO.readVarString("Id", mem, offset + getValidatedOffset(mem, offset, 23, 47, "Id"), 4096000, PacketIO.UTF8) : null,
            mem.get(PacketIO.PROTO_INT, (long)(offset + 1)),
            cubeTextures,
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 5)),
            Opacity.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 6))),
            shaderEffect,
            hasLight(mem, offset) ? ColorLight.toObject(mem, offset + 7) : null,
            particles,
            FluidDrawType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 11))),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 12)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 16)),
            hasBlockParticleSetId(mem, offset)
               ? PacketIO.readVarString(
                  "BlockParticleSetId", mem, offset + getValidatedOffset(mem, offset, 39, 47, "BlockParticleSetId"), 4096000, PacketIO.UTF8
               )
               : null,
            hasParticleColor(mem, offset) ? Color.toObject(mem, offset + 20) : null,
            tagIndexes
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.light != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.particleColor != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.id != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.cubeTextures != null) {
         nullBits = (byte)(nullBits | 8);
      }

      if (this.shaderEffect != null) {
         nullBits = (byte)(nullBits | 16);
      }

      if (this.particles != null) {
         nullBits = (byte)(nullBits | 32);
      }

      if (this.blockParticleSetId != null) {
         nullBits = (byte)(nullBits | 64);
      }

      if (this.tagIndexes != null) {
         nullBits = (byte)(nullBits | 128);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.maxFluidLevel);
      buf.writeByte(this.requiresAlphaBlending ? 1 : 0);
      buf.writeByte(this.opacity.getValue());
      if (this.light != null) {
         this.light.serialize(buf);
      } else {
         buf.writeZero(4);
      }

      buf.writeByte(this.drawType.getValue());
      buf.writeIntLE(this.fluidFXIndex);
      buf.writeIntLE(this.blockSoundSetIndex);
      if (this.particleColor != null) {
         this.particleColor.serialize(buf);
      } else {
         buf.writeZero(3);
      }

      int idOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int cubeTexturesOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int shaderEffectOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int particlesOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int blockParticleSetIdOffsetSlot = buf.writerIndex();
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

      if (this.cubeTextures != null) {
         buf.setIntLE(cubeTexturesOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.cubeTextures.length > 4096000) {
            throw ProtocolException.arrayTooLong("CubeTextures", this.cubeTextures.length, 4096000);
         }

         VarInt.write(buf, this.cubeTextures.length);

         for (BlockTextures item : this.cubeTextures) {
            item.serialize(buf);
         }
      } else {
         buf.setIntLE(cubeTexturesOffsetSlot, -1);
      }

      if (this.shaderEffect != null) {
         buf.setIntLE(shaderEffectOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.shaderEffect.length > 4096000) {
            throw ProtocolException.arrayTooLong("ShaderEffect", this.shaderEffect.length, 4096000);
         }

         VarInt.write(buf, this.shaderEffect.length);

         for (ShaderType item : this.shaderEffect) {
            buf.writeByte(item.getValue());
         }
      } else {
         buf.setIntLE(shaderEffectOffsetSlot, -1);
      }

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

      if (this.blockParticleSetId != null) {
         buf.setIntLE(blockParticleSetIdOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.blockParticleSetId, 4096000);
      } else {
         buf.setIntLE(blockParticleSetIdOffsetSlot, -1);
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

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.light != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.particleColor != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.id != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.cubeTextures != null) {
         nullBits = (byte)(nullBits | 8);
      }

      if (this.shaderEffect != null) {
         nullBits = (byte)(nullBits | 16);
      }

      if (this.particles != null) {
         nullBits = (byte)(nullBits | 32);
      }

      if (this.blockParticleSetId != null) {
         nullBits = (byte)(nullBits | 64);
      }

      if (this.tagIndexes != null) {
         nullBits = (byte)(nullBits | 128);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 1), this.maxFluidLevel);
      mem.set(PacketIO.PROTO_BOOL, offset + 5, this.requiresAlphaBlending);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 6), (byte)this.opacity.getValue());
      if (this.light != null) {
         this.light.serialize(mem, offset + 7);
      } else {
         mem.asSlice(offset + 7, 4L).fill((byte)0);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 11), (byte)this.drawType.getValue());
      mem.set(PacketIO.PROTO_INT, (long)(offset + 12), this.fluidFXIndex);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 16), this.blockSoundSetIndex);
      if (this.particleColor != null) {
         this.particleColor.serialize(mem, offset + 20);
      } else {
         mem.asSlice(offset + 20, 3L).fill((byte)0);
      }

      int varOffset = offset + 47;
      if (this.id != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 23), varOffset - offset - 47);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.id, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 23), -1);
      }

      if (this.cubeTextures != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 27), varOffset - offset - 47);
         if (this.cubeTextures.length > 4096000) {
            throw ProtocolException.arrayTooLong("CubeTextures", this.cubeTextures.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.cubeTextures.length);
         int cubeTexturesValueOffset = 0;

         for (int i = 0; i < this.cubeTextures.length; i++) {
            cubeTexturesValueOffset += this.cubeTextures[i].serialize(mem, varOffset + cubeTexturesValueOffset);
         }

         varOffset += cubeTexturesValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 27), -1);
      }

      if (this.shaderEffect != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 31), varOffset - offset - 47);
         if (this.shaderEffect.length > 4096000) {
            throw ProtocolException.arrayTooLong("ShaderEffect", this.shaderEffect.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.shaderEffect.length);

         for (int i = 0; i < this.shaderEffect.length; i++) {
            mem.set(PacketIO.PROTO_BYTE, (long)(varOffset + i * 1), (byte)this.shaderEffect[i].getValue());
         }

         varOffset += this.shaderEffect.length * 1;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 31), -1);
      }

      if (this.particles != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 35), varOffset - offset - 47);
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
         mem.set(PacketIO.PROTO_INT, (long)(offset + 35), -1);
      }

      if (this.blockParticleSetId != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 39), varOffset - offset - 47);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.blockParticleSetId, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 39), -1);
      }

      if (this.tagIndexes != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 43), varOffset - offset - 47);
         if (this.tagIndexes.length > 4096000) {
            throw ProtocolException.arrayTooLong("TagIndexes", this.tagIndexes.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.tagIndexes.length);
         MemorySegment.copy(this.tagIndexes, 0, mem, PacketIO.PROTO_INT, varOffset, this.tagIndexes.length);
         varOffset += this.tagIndexes.length * 4;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 43), -1);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 47;
      if (this.id != null) {
         size += PacketIO.stringSize(this.id);
      }

      if (this.cubeTextures != null) {
         int cubeTexturesSize = 0;

         for (BlockTextures elem : this.cubeTextures) {
            cubeTexturesSize += elem.computeSize();
         }

         size += VarInt.size(this.cubeTextures.length) + cubeTexturesSize;
      }

      if (this.shaderEffect != null) {
         size += VarInt.size(this.shaderEffect.length) + this.shaderEffect.length * 1;
      }

      if (this.particles != null) {
         int particlesSize = 0;

         for (ModelParticle elem : this.particles) {
            particlesSize += elem.computeSize();
         }

         size += VarInt.size(this.particles.length) + particlesSize;
      }

      if (this.blockParticleSetId != null) {
         size += PacketIO.stringSize(this.blockParticleSetId);
      }

      if (this.tagIndexes != null) {
         size += VarInt.size(this.tagIndexes.length) + this.tagIndexes.length * 4;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 47) {
         return ValidationResult.error("Buffer too small: expected at least 47 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int v = buffer.getByte(offset + 6) & 255;
         if (v >= 4) {
            return ValidationResult.error("Invalid Opacity value for Opacity");
         } else {
            v = buffer.getByte(offset + 11) & 255;
            if (v >= 2) {
               return ValidationResult.error("Invalid FluidDrawType value for DrawType");
            } else {
               if ((nullBits & 4) != 0) {
                  v = buffer.getIntLE(offset + 23);
                  if (v < 0 || v > buffer.writerIndex() - offset - 47) {
                     return ValidationResult.error("Invalid offset for Id");
                  }

                  int pos = offset + 47 + v;
                  int idLen = VarInt.peek(buffer, pos);
                  if (idLen < 0) {
                     return ValidationResult.error("Invalid string length for Id");
                  }

                  if (idLen > 4096000) {
                     return ValidationResult.error("Id exceeds max length 4096000");
                  }

                  pos += VarInt.size(idLen);
                  pos += idLen;
                  if (pos > buffer.writerIndex()) {
                     return ValidationResult.error("Buffer overflow reading Id");
                  }
               }

               if ((nullBits & 8) != 0) {
                  v = buffer.getIntLE(offset + 27);
                  if (v < 0 || v > buffer.writerIndex() - offset - 47) {
                     return ValidationResult.error("Invalid offset for CubeTextures");
                  }

                  int posx = offset + 47 + v;
                  int cubeTexturesCount = VarInt.peek(buffer, posx);
                  if (cubeTexturesCount < 0) {
                     return ValidationResult.error("Invalid array count for CubeTextures");
                  }

                  if (cubeTexturesCount > 4096000) {
                     return ValidationResult.error("CubeTextures exceeds max length 4096000");
                  }

                  posx += VarInt.size(cubeTexturesCount);

                  for (int i = 0; i < cubeTexturesCount; i++) {
                     ValidationResult structResult = BlockTextures.validateStructure(buffer, posx);
                     if (!structResult.isValid()) {
                        return ValidationResult.error("Invalid BlockTextures in CubeTextures[" + i + "]: " + structResult.error());
                     }

                     posx += BlockTextures.computeBytesConsumed(buffer, posx);
                  }
               }

               if ((nullBits & 16) != 0) {
                  v = buffer.getIntLE(offset + 31);
                  if (v < 0 || v > buffer.writerIndex() - offset - 47) {
                     return ValidationResult.error("Invalid offset for ShaderEffect");
                  }

                  int posxx = offset + 47 + v;
                  int shaderEffectCount = VarInt.peek(buffer, posxx);
                  if (shaderEffectCount < 0) {
                     return ValidationResult.error("Invalid array count for ShaderEffect");
                  }

                  if (shaderEffectCount > 4096000) {
                     return ValidationResult.error("ShaderEffect exceeds max length 4096000");
                  }

                  posxx += VarInt.size(shaderEffectCount);
                  if (posxx + shaderEffectCount * 1L > buffer.writerIndex()) {
                     return ValidationResult.error("Buffer overflow reading ShaderEffect");
                  }

                  for (int i = 0; i < shaderEffectCount; i++) {
                     int vx = buffer.getByte(posxx) & 255;
                     if (vx >= 10) {
                        return ValidationResult.error("Invalid ShaderType value for ShaderEffect[i]");
                     }

                     posxx++;
                  }
               }

               if ((nullBits & 32) != 0) {
                  v = buffer.getIntLE(offset + 35);
                  if (v < 0 || v > buffer.writerIndex() - offset - 47) {
                     return ValidationResult.error("Invalid offset for Particles");
                  }

                  int posxxx = offset + 47 + v;
                  int particlesCount = VarInt.peek(buffer, posxxx);
                  if (particlesCount < 0) {
                     return ValidationResult.error("Invalid array count for Particles");
                  }

                  if (particlesCount > 4096000) {
                     return ValidationResult.error("Particles exceeds max length 4096000");
                  }

                  posxxx += VarInt.size(particlesCount);

                  for (int i = 0; i < particlesCount; i++) {
                     ValidationResult structResult = ModelParticle.validateStructure(buffer, posxxx);
                     if (!structResult.isValid()) {
                        return ValidationResult.error("Invalid ModelParticle in Particles[" + i + "]: " + structResult.error());
                     }

                     posxxx += ModelParticle.computeBytesConsumed(buffer, posxxx);
                  }
               }

               if ((nullBits & 64) != 0) {
                  v = buffer.getIntLE(offset + 39);
                  if (v < 0 || v > buffer.writerIndex() - offset - 47) {
                     return ValidationResult.error("Invalid offset for BlockParticleSetId");
                  }

                  int posxxxx = offset + 47 + v;
                  int blockParticleSetIdLen = VarInt.peek(buffer, posxxxx);
                  if (blockParticleSetIdLen < 0) {
                     return ValidationResult.error("Invalid string length for BlockParticleSetId");
                  }

                  if (blockParticleSetIdLen > 4096000) {
                     return ValidationResult.error("BlockParticleSetId exceeds max length 4096000");
                  }

                  posxxxx += VarInt.size(blockParticleSetIdLen);
                  posxxxx += blockParticleSetIdLen;
                  if (posxxxx > buffer.writerIndex()) {
                     return ValidationResult.error("Buffer overflow reading BlockParticleSetId");
                  }
               }

               if ((nullBits & 128) != 0) {
                  v = buffer.getIntLE(offset + 43);
                  if (v < 0 || v > buffer.writerIndex() - offset - 47) {
                     return ValidationResult.error("Invalid offset for TagIndexes");
                  }

                  int posxxxxx = offset + 47 + v;
                  int tagIndexesCount = VarInt.peek(buffer, posxxxxx);
                  if (tagIndexesCount < 0) {
                     return ValidationResult.error("Invalid array count for TagIndexes");
                  }

                  if (tagIndexesCount > 4096000) {
                     return ValidationResult.error("TagIndexes exceeds max length 4096000");
                  }

                  posxxxxx += VarInt.size(tagIndexesCount);
                  posxxxxx += tagIndexesCount * 4;
                  if (posxxxxx > buffer.writerIndex()) {
                     return ValidationResult.error("Buffer overflow reading TagIndexes");
                  }
               }

               return ValidationResult.OK;
            }
         }
      }
   }

   public Fluid clone() {
      Fluid copy = new Fluid();
      copy.id = this.id;
      copy.maxFluidLevel = this.maxFluidLevel;
      copy.cubeTextures = this.cubeTextures != null ? Arrays.stream(this.cubeTextures).map(e -> e.clone()).toArray(BlockTextures[]::new) : null;
      copy.requiresAlphaBlending = this.requiresAlphaBlending;
      copy.opacity = this.opacity;
      copy.shaderEffect = this.shaderEffect != null ? Arrays.copyOf(this.shaderEffect, this.shaderEffect.length) : null;
      copy.light = this.light != null ? this.light.clone() : null;
      copy.particles = this.particles != null ? Arrays.stream(this.particles).map(e -> e.clone()).toArray(ModelParticle[]::new) : null;
      copy.drawType = this.drawType;
      copy.fluidFXIndex = this.fluidFXIndex;
      copy.blockSoundSetIndex = this.blockSoundSetIndex;
      copy.blockParticleSetId = this.blockParticleSetId;
      copy.particleColor = this.particleColor != null ? this.particleColor.clone() : null;
      copy.tagIndexes = this.tagIndexes != null ? Arrays.copyOf(this.tagIndexes, this.tagIndexes.length) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof Fluid other)
            ? false
            : Objects.equals(this.id, other.id)
               && this.maxFluidLevel == other.maxFluidLevel
               && Arrays.equals((Object[])this.cubeTextures, (Object[])other.cubeTextures)
               && this.requiresAlphaBlending == other.requiresAlphaBlending
               && Objects.equals(this.opacity, other.opacity)
               && Arrays.equals((Object[])this.shaderEffect, (Object[])other.shaderEffect)
               && Objects.equals(this.light, other.light)
               && Arrays.equals((Object[])this.particles, (Object[])other.particles)
               && Objects.equals(this.drawType, other.drawType)
               && this.fluidFXIndex == other.fluidFXIndex
               && this.blockSoundSetIndex == other.blockSoundSetIndex
               && Objects.equals(this.blockParticleSetId, other.blockParticleSetId)
               && Objects.equals(this.particleColor, other.particleColor)
               && Arrays.equals(this.tagIndexes, other.tagIndexes);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.id);
      result = 31 * result + Integer.hashCode(this.maxFluidLevel);
      result = 31 * result + Arrays.hashCode((Object[])this.cubeTextures);
      result = 31 * result + Boolean.hashCode(this.requiresAlphaBlending);
      result = 31 * result + Objects.hashCode(this.opacity);
      result = 31 * result + Arrays.hashCode((Object[])this.shaderEffect);
      result = 31 * result + Objects.hashCode(this.light);
      result = 31 * result + Arrays.hashCode((Object[])this.particles);
      result = 31 * result + Objects.hashCode(this.drawType);
      result = 31 * result + Integer.hashCode(this.fluidFXIndex);
      result = 31 * result + Integer.hashCode(this.blockSoundSetIndex);
      result = 31 * result + Objects.hashCode(this.blockParticleSetId);
      result = 31 * result + Objects.hashCode(this.particleColor);
      return 31 * result + Arrays.hashCode(this.tagIndexes);
   }
}
