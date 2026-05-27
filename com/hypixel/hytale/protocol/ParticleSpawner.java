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

public class ParticleSpawner {
   public static final int NULLABLE_BIT_FIELD_SIZE = 2;
   public static final int FIXED_BLOCK_SIZE = 131;
   public static final int VARIABLE_FIELD_COUNT = 4;
   public static final int VARIABLE_BLOCK_START = 147;
   public static final int MAX_SIZE = 651264332;
   @Nullable
   public String id;
   @Nullable
   public Particle particle;
   @Nonnull
   public EmitShape shape = EmitShape.Sphere;
   @Nullable
   public RangeVector3f emitOffset;
   public float cameraOffset;
   public boolean useEmitDirection;
   public float lifeSpan;
   @Nullable
   public Rangef spawnRate;
   public boolean spawnBurst;
   @Nullable
   public Rangef waveDelay;
   @Nullable
   public Range totalParticles;
   public int maxConcurrentParticles;
   @Nullable
   public InitialVelocity initialVelocity;
   public float velocityStretchMultiplier;
   @Nonnull
   public ParticleRotationInfluence particleRotationInfluence = ParticleRotationInfluence.None;
   public boolean particleRotateWithSpawner;
   public boolean isLowRes;
   public float trailSpawnerPositionMultiplier;
   public float trailSpawnerRotationMultiplier;
   @Nullable
   public ParticleCollision particleCollision;
   @Nonnull
   public FXRenderMode renderMode = FXRenderMode.BlendLinear;
   public float lightInfluence;
   public boolean linearFiltering;
   @Nullable
   public Rangef particleLifeSpan;
   @Nullable
   public UVMotion uvMotion;
   @Nullable
   public ParticleAttractor[] attractors;
   @Nullable
   public IntersectionHighlight intersectionHighlight;

   public ParticleSpawner() {
   }

   public ParticleSpawner(
      @Nullable String id,
      @Nullable Particle particle,
      @Nonnull EmitShape shape,
      @Nullable RangeVector3f emitOffset,
      float cameraOffset,
      boolean useEmitDirection,
      float lifeSpan,
      @Nullable Rangef spawnRate,
      boolean spawnBurst,
      @Nullable Rangef waveDelay,
      @Nullable Range totalParticles,
      int maxConcurrentParticles,
      @Nullable InitialVelocity initialVelocity,
      float velocityStretchMultiplier,
      @Nonnull ParticleRotationInfluence particleRotationInfluence,
      boolean particleRotateWithSpawner,
      boolean isLowRes,
      float trailSpawnerPositionMultiplier,
      float trailSpawnerRotationMultiplier,
      @Nullable ParticleCollision particleCollision,
      @Nonnull FXRenderMode renderMode,
      float lightInfluence,
      boolean linearFiltering,
      @Nullable Rangef particleLifeSpan,
      @Nullable UVMotion uvMotion,
      @Nullable ParticleAttractor[] attractors,
      @Nullable IntersectionHighlight intersectionHighlight
   ) {
      this.id = id;
      this.particle = particle;
      this.shape = shape;
      this.emitOffset = emitOffset;
      this.cameraOffset = cameraOffset;
      this.useEmitDirection = useEmitDirection;
      this.lifeSpan = lifeSpan;
      this.spawnRate = spawnRate;
      this.spawnBurst = spawnBurst;
      this.waveDelay = waveDelay;
      this.totalParticles = totalParticles;
      this.maxConcurrentParticles = maxConcurrentParticles;
      this.initialVelocity = initialVelocity;
      this.velocityStretchMultiplier = velocityStretchMultiplier;
      this.particleRotationInfluence = particleRotationInfluence;
      this.particleRotateWithSpawner = particleRotateWithSpawner;
      this.isLowRes = isLowRes;
      this.trailSpawnerPositionMultiplier = trailSpawnerPositionMultiplier;
      this.trailSpawnerRotationMultiplier = trailSpawnerRotationMultiplier;
      this.particleCollision = particleCollision;
      this.renderMode = renderMode;
      this.lightInfluence = lightInfluence;
      this.linearFiltering = linearFiltering;
      this.particleLifeSpan = particleLifeSpan;
      this.uvMotion = uvMotion;
      this.attractors = attractors;
      this.intersectionHighlight = intersectionHighlight;
   }

   public ParticleSpawner(@Nonnull ParticleSpawner other) {
      this.id = other.id;
      this.particle = other.particle;
      this.shape = other.shape;
      this.emitOffset = other.emitOffset;
      this.cameraOffset = other.cameraOffset;
      this.useEmitDirection = other.useEmitDirection;
      this.lifeSpan = other.lifeSpan;
      this.spawnRate = other.spawnRate;
      this.spawnBurst = other.spawnBurst;
      this.waveDelay = other.waveDelay;
      this.totalParticles = other.totalParticles;
      this.maxConcurrentParticles = other.maxConcurrentParticles;
      this.initialVelocity = other.initialVelocity;
      this.velocityStretchMultiplier = other.velocityStretchMultiplier;
      this.particleRotationInfluence = other.particleRotationInfluence;
      this.particleRotateWithSpawner = other.particleRotateWithSpawner;
      this.isLowRes = other.isLowRes;
      this.trailSpawnerPositionMultiplier = other.trailSpawnerPositionMultiplier;
      this.trailSpawnerRotationMultiplier = other.trailSpawnerRotationMultiplier;
      this.particleCollision = other.particleCollision;
      this.renderMode = other.renderMode;
      this.lightInfluence = other.lightInfluence;
      this.linearFiltering = other.linearFiltering;
      this.particleLifeSpan = other.particleLifeSpan;
      this.uvMotion = other.uvMotion;
      this.attractors = other.attractors;
      this.intersectionHighlight = other.intersectionHighlight;
   }

   @Nonnull
   public static ParticleSpawner deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 147) {
         throw ProtocolException.bufferTooSmall("ParticleSpawner", 147, buf.readableBytes() - offset);
      } else {
         ParticleSpawner obj = new ParticleSpawner();
         byte[] nullBits = PacketIO.readBytes(buf, offset, 2);
         obj.shape = EmitShape.fromValue(buf.getByte(offset + 2));
         if ((nullBits[0] & 1) != 0) {
            obj.emitOffset = RangeVector3f.deserialize(buf, offset + 3);
         }

         obj.cameraOffset = buf.getFloatLE(offset + 28);
         obj.useEmitDirection = buf.getByte(offset + 32) != 0;
         obj.lifeSpan = buf.getFloatLE(offset + 33);
         if ((nullBits[0] & 2) != 0) {
            obj.spawnRate = Rangef.deserialize(buf, offset + 37);
         }

         obj.spawnBurst = buf.getByte(offset + 45) != 0;
         if ((nullBits[0] & 4) != 0) {
            obj.waveDelay = Rangef.deserialize(buf, offset + 46);
         }

         if ((nullBits[0] & 8) != 0) {
            obj.totalParticles = Range.deserialize(buf, offset + 54);
         }

         obj.maxConcurrentParticles = buf.getIntLE(offset + 62);
         if ((nullBits[0] & 16) != 0) {
            obj.initialVelocity = InitialVelocity.deserialize(buf, offset + 66);
         }

         obj.velocityStretchMultiplier = buf.getFloatLE(offset + 91);
         obj.particleRotationInfluence = ParticleRotationInfluence.fromValue(buf.getByte(offset + 95));
         obj.particleRotateWithSpawner = buf.getByte(offset + 96) != 0;
         obj.isLowRes = buf.getByte(offset + 97) != 0;
         obj.trailSpawnerPositionMultiplier = buf.getFloatLE(offset + 98);
         obj.trailSpawnerRotationMultiplier = buf.getFloatLE(offset + 102);
         if ((nullBits[0] & 32) != 0) {
            obj.particleCollision = ParticleCollision.deserialize(buf, offset + 106);
         }

         obj.renderMode = FXRenderMode.fromValue(buf.getByte(offset + 109));
         obj.lightInfluence = buf.getFloatLE(offset + 110);
         obj.linearFiltering = buf.getByte(offset + 114) != 0;
         if ((nullBits[0] & 64) != 0) {
            obj.particleLifeSpan = Rangef.deserialize(buf, offset + 115);
         }

         if ((nullBits[0] & 128) != 0) {
            obj.intersectionHighlight = IntersectionHighlight.deserialize(buf, offset + 123);
         }

         if ((nullBits[1] & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 131);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 147) {
               throw ProtocolException.invalidOffset("Id", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 147 + varPosBase0;
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

         if ((nullBits[1] & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 135);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 147) {
               throw ProtocolException.invalidOffset("Particle", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 147 + varPosBase1;
            obj.particle = Particle.deserialize(buf, varPos1);
         }

         if ((nullBits[1] & 4) != 0) {
            int varPosBase2 = buf.getIntLE(offset + 139);
            if (varPosBase2 < 0 || varPosBase2 > buf.writerIndex() - offset - 147) {
               throw ProtocolException.invalidOffset("UvMotion", varPosBase2, buf.readableBytes());
            }

            int varPos2 = offset + 147 + varPosBase2;
            obj.uvMotion = UVMotion.deserialize(buf, varPos2);
         }

         if ((nullBits[1] & 8) != 0) {
            int varPosBase3 = buf.getIntLE(offset + 143);
            if (varPosBase3 < 0 || varPosBase3 > buf.writerIndex() - offset - 147) {
               throw ProtocolException.invalidOffset("Attractors", varPosBase3, buf.readableBytes());
            }

            int varPos3 = offset + 147 + varPosBase3;
            int attractorsCount = VarInt.peek(buf, varPos3);
            if (attractorsCount < 0) {
               throw ProtocolException.invalidVarInt("Attractors");
            }

            int varIntLen = VarInt.size(attractorsCount);
            if (attractorsCount > 4096000) {
               throw ProtocolException.arrayTooLong("Attractors", attractorsCount, 4096000);
            }

            if (varPos3 + varIntLen + attractorsCount * 85L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Attractors", varPos3 + varIntLen + attractorsCount * 85, buf.readableBytes());
            }

            obj.attractors = new ParticleAttractor[attractorsCount];
            int elemPos = varPos3 + varIntLen;

            for (int i = 0; i < attractorsCount; i++) {
               obj.attractors[i] = ParticleAttractor.deserialize(buf, elemPos);
               elemPos += ParticleAttractor.computeBytesConsumed(buf, elemPos);
            }
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte[] nullBits = PacketIO.readBytes(buf, offset, 2);
      int maxEnd = 147;
      if ((nullBits[1] & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 131);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 147) {
            throw ProtocolException.invalidOffset("Id", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 147 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits[1] & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 135);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 147) {
            throw ProtocolException.invalidOffset("Particle", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 147 + fieldOffset1;
         pos1 += Particle.computeBytesConsumed(buf, pos1);
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits[1] & 4) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 139);
         if (fieldOffset2 < 0 || fieldOffset2 > buf.writerIndex() - offset - 147) {
            throw ProtocolException.invalidOffset("UvMotion", fieldOffset2, maxEnd);
         }

         int pos2 = offset + 147 + fieldOffset2;
         pos2 += UVMotion.computeBytesConsumed(buf, pos2);
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      if ((nullBits[1] & 8) != 0) {
         int fieldOffset3 = buf.getIntLE(offset + 143);
         if (fieldOffset3 < 0 || fieldOffset3 > buf.writerIndex() - offset - 147) {
            throw ProtocolException.invalidOffset("Attractors", fieldOffset3, maxEnd);
         }

         int pos3 = offset + 147 + fieldOffset3;
         int arrLen = VarInt.peek(buf, pos3);
         pos3 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            pos3 += ParticleAttractor.computeBytesConsumed(buf, pos3);
         }

         if (pos3 - offset > maxEnd) {
            maxEnd = pos3 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 147L;
   }

   @Nullable
   public static String getId(MemorySegment mem) {
      return getId(mem, 0);
   }

   @Nullable
   public static String getId(MemorySegment mem, int offset) {
      return hasId(mem, offset) ? PacketIO.readVarString("Id", mem, offset + getValidatedOffset(mem, offset, 131, 147, "Id"), 4096000, PacketIO.UTF8) : null;
   }

   @Nullable
   public static Particle getParticle(MemorySegment mem) {
      return getParticle(mem, 0);
   }

   @Nullable
   public static Particle getParticle(MemorySegment mem, int offset) {
      return hasParticle(mem, offset) ? Particle.toObject(mem, offset + getValidatedOffset(mem, offset, 135, 147, "Particle")) : null;
   }

   public static EmitShape getShape(MemorySegment mem) {
      return getShape(mem, 0);
   }

   public static EmitShape getShape(MemorySegment mem, int offset) {
      return EmitShape.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 2)));
   }

   @Nullable
   public static RangeVector3f getEmitOffset(MemorySegment mem) {
      return getEmitOffset(mem, 0);
   }

   @Nullable
   public static RangeVector3f getEmitOffset(MemorySegment mem, int offset) {
      return hasEmitOffset(mem, offset) ? RangeVector3f.toObject(mem, offset + 3) : null;
   }

   public static float getCameraOffset(MemorySegment mem) {
      return getCameraOffset(mem, 0);
   }

   public static float getCameraOffset(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 28));
   }

   public static boolean getUseEmitDirection(MemorySegment mem) {
      return getUseEmitDirection(mem, 0);
   }

   public static boolean getUseEmitDirection(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 32));
   }

   public static float getLifeSpan(MemorySegment mem) {
      return getLifeSpan(mem, 0);
   }

   public static float getLifeSpan(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 33));
   }

   @Nullable
   public static Rangef getSpawnRate(MemorySegment mem) {
      return getSpawnRate(mem, 0);
   }

   @Nullable
   public static Rangef getSpawnRate(MemorySegment mem, int offset) {
      return hasSpawnRate(mem, offset) ? Rangef.toObject(mem, offset + 37) : null;
   }

   public static boolean getSpawnBurst(MemorySegment mem) {
      return getSpawnBurst(mem, 0);
   }

   public static boolean getSpawnBurst(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 45));
   }

   @Nullable
   public static Rangef getWaveDelay(MemorySegment mem) {
      return getWaveDelay(mem, 0);
   }

   @Nullable
   public static Rangef getWaveDelay(MemorySegment mem, int offset) {
      return hasWaveDelay(mem, offset) ? Rangef.toObject(mem, offset + 46) : null;
   }

   @Nullable
   public static Range getTotalParticles(MemorySegment mem) {
      return getTotalParticles(mem, 0);
   }

   @Nullable
   public static Range getTotalParticles(MemorySegment mem, int offset) {
      return hasTotalParticles(mem, offset) ? Range.toObject(mem, offset + 54) : null;
   }

   public static int getMaxConcurrentParticles(MemorySegment mem) {
      return getMaxConcurrentParticles(mem, 0);
   }

   public static int getMaxConcurrentParticles(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 62));
   }

   @Nullable
   public static InitialVelocity getInitialVelocity(MemorySegment mem) {
      return getInitialVelocity(mem, 0);
   }

   @Nullable
   public static InitialVelocity getInitialVelocity(MemorySegment mem, int offset) {
      return hasInitialVelocity(mem, offset) ? InitialVelocity.toObject(mem, offset + 66) : null;
   }

   public static float getVelocityStretchMultiplier(MemorySegment mem) {
      return getVelocityStretchMultiplier(mem, 0);
   }

   public static float getVelocityStretchMultiplier(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 91));
   }

   public static ParticleRotationInfluence getParticleRotationInfluence(MemorySegment mem) {
      return getParticleRotationInfluence(mem, 0);
   }

   public static ParticleRotationInfluence getParticleRotationInfluence(MemorySegment mem, int offset) {
      return ParticleRotationInfluence.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 95)));
   }

   public static boolean getParticleRotateWithSpawner(MemorySegment mem) {
      return getParticleRotateWithSpawner(mem, 0);
   }

   public static boolean getParticleRotateWithSpawner(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 96));
   }

   public static boolean getIsLowRes(MemorySegment mem) {
      return getIsLowRes(mem, 0);
   }

   public static boolean getIsLowRes(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 97));
   }

   public static float getTrailSpawnerPositionMultiplier(MemorySegment mem) {
      return getTrailSpawnerPositionMultiplier(mem, 0);
   }

   public static float getTrailSpawnerPositionMultiplier(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 98));
   }

   public static float getTrailSpawnerRotationMultiplier(MemorySegment mem) {
      return getTrailSpawnerRotationMultiplier(mem, 0);
   }

   public static float getTrailSpawnerRotationMultiplier(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 102));
   }

   @Nullable
   public static ParticleCollision getParticleCollision(MemorySegment mem) {
      return getParticleCollision(mem, 0);
   }

   @Nullable
   public static ParticleCollision getParticleCollision(MemorySegment mem, int offset) {
      return hasParticleCollision(mem, offset) ? ParticleCollision.toObject(mem, offset + 106) : null;
   }

   public static FXRenderMode getRenderMode(MemorySegment mem) {
      return getRenderMode(mem, 0);
   }

   public static FXRenderMode getRenderMode(MemorySegment mem, int offset) {
      return FXRenderMode.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 109)));
   }

   public static float getLightInfluence(MemorySegment mem) {
      return getLightInfluence(mem, 0);
   }

   public static float getLightInfluence(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 110));
   }

   public static boolean getLinearFiltering(MemorySegment mem) {
      return getLinearFiltering(mem, 0);
   }

   public static boolean getLinearFiltering(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 114));
   }

   @Nullable
   public static Rangef getParticleLifeSpan(MemorySegment mem) {
      return getParticleLifeSpan(mem, 0);
   }

   @Nullable
   public static Rangef getParticleLifeSpan(MemorySegment mem, int offset) {
      return hasParticleLifeSpan(mem, offset) ? Rangef.toObject(mem, offset + 115) : null;
   }

   @Nullable
   public static UVMotion getUvMotion(MemorySegment mem) {
      return getUvMotion(mem, 0);
   }

   @Nullable
   public static UVMotion getUvMotion(MemorySegment mem, int offset) {
      return hasUvMotion(mem, offset) ? UVMotion.toObject(mem, offset + getValidatedOffset(mem, offset, 139, 147, "UvMotion")) : null;
   }

   @Nullable
   public static ParticleAttractor[] getAttractors(MemorySegment mem) {
      return getAttractors(mem, 0);
   }

   @Nullable
   public static ParticleAttractor[] getAttractors(MemorySegment mem, int offset) {
      if (!hasAttractors(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 143, 147, "Attractors");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Attractors", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("Attractors", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 85L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Attractors", off + lenOffset + len * 85, (int)mem.byteSize());
            } else {
               off += lenOffset;
               ParticleAttractor[] data = new ParticleAttractor[len];

               for (int i = 0; i < len; i++) {
                  data[i] = ParticleAttractor.toObject(mem, off + i * 85);
               }

               return data;
            }
         }
      }
   }

   @Nullable
   public static IntersectionHighlight getIntersectionHighlight(MemorySegment mem) {
      return getIntersectionHighlight(mem, 0);
   }

   @Nullable
   public static IntersectionHighlight getIntersectionHighlight(MemorySegment mem, int offset) {
      return hasIntersectionHighlight(mem, offset) ? IntersectionHighlight.toObject(mem, offset + 123) : null;
   }

   public static boolean hasEmitOffset(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasSpawnRate(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasWaveDelay(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 4) != 0;
   }

   public static boolean hasTotalParticles(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 8) != 0;
   }

   public static boolean hasInitialVelocity(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 16) != 0;
   }

   public static boolean hasParticleCollision(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 32) != 0;
   }

   public static boolean hasParticleLifeSpan(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 64) != 0;
   }

   public static boolean hasIntersectionHighlight(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 128) != 0;
   }

   public static boolean hasId(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1));
      return (b & 1) != 0;
   }

   public static boolean hasParticle(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1));
      return (b & 2) != 0;
   }

   public static boolean hasUvMotion(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1));
      return (b & 4) != 0;
   }

   public static boolean hasAttractors(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1));
      return (b & 8) != 0;
   }

   private static int getValidatedOffset(MemorySegment buffer, int base, int slotPosition, int varBlockStart, String fieldName) {
      int offset = buffer.get(PacketIO.PROTO_INT, (long)(base + slotPosition));
      if (offset >= 0 && offset <= buffer.byteSize() - base - varBlockStart) {
         return varBlockStart + offset;
      } else {
         throw ProtocolException.invalidOffset(fieldName, offset, (int)buffer.byteSize());
      }
   }

   public static ParticleSpawner toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static ParticleSpawner toObject(MemorySegment mem, int offset) {
      if (offset + 147 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("ParticleSpawner", offset + 147, (int)mem.byteSize());
      } else {
         ParticleAttractor[] attractors = null;
         if (hasAttractors(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 143, 147, "Attractors");
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Attractors", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("Attractors", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 85L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Attractors", off + lenOffset + len * 85, (int)mem.byteSize());
            }

            off += lenOffset;
            attractors = new ParticleAttractor[len];

            for (int i = 0; i < len; i++) {
               attractors[i] = ParticleAttractor.toObject(mem, off + i * 85);
            }
         }

         return new ParticleSpawner(
            hasId(mem, offset) ? PacketIO.readVarString("Id", mem, offset + getValidatedOffset(mem, offset, 131, 147, "Id"), 4096000, PacketIO.UTF8) : null,
            hasParticle(mem, offset) ? Particle.toObject(mem, offset + getValidatedOffset(mem, offset, 135, 147, "Particle")) : null,
            EmitShape.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 2))),
            hasEmitOffset(mem, offset) ? RangeVector3f.toObject(mem, offset + 3) : null,
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 28)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 32)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 33)),
            hasSpawnRate(mem, offset) ? Rangef.toObject(mem, offset + 37) : null,
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 45)),
            hasWaveDelay(mem, offset) ? Rangef.toObject(mem, offset + 46) : null,
            hasTotalParticles(mem, offset) ? Range.toObject(mem, offset + 54) : null,
            mem.get(PacketIO.PROTO_INT, (long)(offset + 62)),
            hasInitialVelocity(mem, offset) ? InitialVelocity.toObject(mem, offset + 66) : null,
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 91)),
            ParticleRotationInfluence.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 95))),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 96)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 97)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 98)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 102)),
            hasParticleCollision(mem, offset) ? ParticleCollision.toObject(mem, offset + 106) : null,
            FXRenderMode.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 109))),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 110)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 114)),
            hasParticleLifeSpan(mem, offset) ? Rangef.toObject(mem, offset + 115) : null,
            hasUvMotion(mem, offset) ? UVMotion.toObject(mem, offset + getValidatedOffset(mem, offset, 139, 147, "UvMotion")) : null,
            attractors,
            hasIntersectionHighlight(mem, offset) ? IntersectionHighlight.toObject(mem, offset + 123) : null
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte[] nullBits = new byte[2];
      if (this.emitOffset != null) {
         nullBits[0] = (byte)(nullBits[0] | 1);
      }

      if (this.spawnRate != null) {
         nullBits[0] = (byte)(nullBits[0] | 2);
      }

      if (this.waveDelay != null) {
         nullBits[0] = (byte)(nullBits[0] | 4);
      }

      if (this.totalParticles != null) {
         nullBits[0] = (byte)(nullBits[0] | 8);
      }

      if (this.initialVelocity != null) {
         nullBits[0] = (byte)(nullBits[0] | 16);
      }

      if (this.particleCollision != null) {
         nullBits[0] = (byte)(nullBits[0] | 32);
      }

      if (this.particleLifeSpan != null) {
         nullBits[0] = (byte)(nullBits[0] | 64);
      }

      if (this.intersectionHighlight != null) {
         nullBits[0] = (byte)(nullBits[0] | 128);
      }

      if (this.id != null) {
         nullBits[1] = (byte)(nullBits[1] | 1);
      }

      if (this.particle != null) {
         nullBits[1] = (byte)(nullBits[1] | 2);
      }

      if (this.uvMotion != null) {
         nullBits[1] = (byte)(nullBits[1] | 4);
      }

      if (this.attractors != null) {
         nullBits[1] = (byte)(nullBits[1] | 8);
      }

      buf.writeBytes(nullBits);
      buf.writeByte(this.shape.getValue());
      if (this.emitOffset != null) {
         this.emitOffset.serialize(buf);
      } else {
         buf.writeZero(25);
      }

      buf.writeFloatLE(this.cameraOffset);
      buf.writeByte(this.useEmitDirection ? 1 : 0);
      buf.writeFloatLE(this.lifeSpan);
      if (this.spawnRate != null) {
         this.spawnRate.serialize(buf);
      } else {
         buf.writeZero(8);
      }

      buf.writeByte(this.spawnBurst ? 1 : 0);
      if (this.waveDelay != null) {
         this.waveDelay.serialize(buf);
      } else {
         buf.writeZero(8);
      }

      if (this.totalParticles != null) {
         this.totalParticles.serialize(buf);
      } else {
         buf.writeZero(8);
      }

      buf.writeIntLE(this.maxConcurrentParticles);
      if (this.initialVelocity != null) {
         this.initialVelocity.serialize(buf);
      } else {
         buf.writeZero(25);
      }

      buf.writeFloatLE(this.velocityStretchMultiplier);
      buf.writeByte(this.particleRotationInfluence.getValue());
      buf.writeByte(this.particleRotateWithSpawner ? 1 : 0);
      buf.writeByte(this.isLowRes ? 1 : 0);
      buf.writeFloatLE(this.trailSpawnerPositionMultiplier);
      buf.writeFloatLE(this.trailSpawnerRotationMultiplier);
      if (this.particleCollision != null) {
         this.particleCollision.serialize(buf);
      } else {
         buf.writeZero(3);
      }

      buf.writeByte(this.renderMode.getValue());
      buf.writeFloatLE(this.lightInfluence);
      buf.writeByte(this.linearFiltering ? 1 : 0);
      if (this.particleLifeSpan != null) {
         this.particleLifeSpan.serialize(buf);
      } else {
         buf.writeZero(8);
      }

      if (this.intersectionHighlight != null) {
         this.intersectionHighlight.serialize(buf);
      } else {
         buf.writeZero(8);
      }

      int idOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int particleOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int uvMotionOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int attractorsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.id != null) {
         buf.setIntLE(idOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.id, 4096000);
      } else {
         buf.setIntLE(idOffsetSlot, -1);
      }

      if (this.particle != null) {
         buf.setIntLE(particleOffsetSlot, buf.writerIndex() - varBlockStart);
         this.particle.serialize(buf);
      } else {
         buf.setIntLE(particleOffsetSlot, -1);
      }

      if (this.uvMotion != null) {
         buf.setIntLE(uvMotionOffsetSlot, buf.writerIndex() - varBlockStart);
         this.uvMotion.serialize(buf);
      } else {
         buf.setIntLE(uvMotionOffsetSlot, -1);
      }

      if (this.attractors != null) {
         buf.setIntLE(attractorsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.attractors.length > 4096000) {
            throw ProtocolException.arrayTooLong("Attractors", this.attractors.length, 4096000);
         }

         VarInt.write(buf, this.attractors.length);

         for (ParticleAttractor item : this.attractors) {
            item.serialize(buf);
         }
      } else {
         buf.setIntLE(attractorsOffsetSlot, -1);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.emitOffset != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.spawnRate != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.waveDelay != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.totalParticles != null) {
         nullBits = (byte)(nullBits | 8);
      }

      if (this.initialVelocity != null) {
         nullBits = (byte)(nullBits | 16);
      }

      if (this.particleCollision != null) {
         nullBits = (byte)(nullBits | 32);
      }

      if (this.particleLifeSpan != null) {
         nullBits = (byte)(nullBits | 64);
      }

      if (this.intersectionHighlight != null) {
         nullBits = (byte)(nullBits | 128);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      nullBits = 0;
      if (this.id != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.particle != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.uvMotion != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.attractors != null) {
         nullBits = (byte)(nullBits | 8);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 1), nullBits);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 2), (byte)this.shape.getValue());
      if (this.emitOffset != null) {
         this.emitOffset.serialize(mem, offset + 3);
      } else {
         mem.asSlice(offset + 3, 25L).fill((byte)0);
      }

      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 28), this.cameraOffset);
      mem.set(PacketIO.PROTO_BOOL, offset + 32, this.useEmitDirection);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 33), this.lifeSpan);
      if (this.spawnRate != null) {
         this.spawnRate.serialize(mem, offset + 37);
      } else {
         mem.asSlice(offset + 37, 8L).fill((byte)0);
      }

      mem.set(PacketIO.PROTO_BOOL, offset + 45, this.spawnBurst);
      if (this.waveDelay != null) {
         this.waveDelay.serialize(mem, offset + 46);
      } else {
         mem.asSlice(offset + 46, 8L).fill((byte)0);
      }

      if (this.totalParticles != null) {
         this.totalParticles.serialize(mem, offset + 54);
      } else {
         mem.asSlice(offset + 54, 8L).fill((byte)0);
      }

      mem.set(PacketIO.PROTO_INT, (long)(offset + 62), this.maxConcurrentParticles);
      if (this.initialVelocity != null) {
         this.initialVelocity.serialize(mem, offset + 66);
      } else {
         mem.asSlice(offset + 66, 25L).fill((byte)0);
      }

      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 91), this.velocityStretchMultiplier);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 95), (byte)this.particleRotationInfluence.getValue());
      mem.set(PacketIO.PROTO_BOOL, offset + 96, this.particleRotateWithSpawner);
      mem.set(PacketIO.PROTO_BOOL, offset + 97, this.isLowRes);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 98), this.trailSpawnerPositionMultiplier);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 102), this.trailSpawnerRotationMultiplier);
      if (this.particleCollision != null) {
         this.particleCollision.serialize(mem, offset + 106);
      } else {
         mem.asSlice(offset + 106, 3L).fill((byte)0);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 109), (byte)this.renderMode.getValue());
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 110), this.lightInfluence);
      mem.set(PacketIO.PROTO_BOOL, offset + 114, this.linearFiltering);
      if (this.particleLifeSpan != null) {
         this.particleLifeSpan.serialize(mem, offset + 115);
      } else {
         mem.asSlice(offset + 115, 8L).fill((byte)0);
      }

      if (this.intersectionHighlight != null) {
         this.intersectionHighlight.serialize(mem, offset + 123);
      } else {
         mem.asSlice(offset + 123, 8L).fill((byte)0);
      }

      int varOffset = offset + 147;
      if (this.id != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 131), varOffset - offset - 147);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.id, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 131), -1);
      }

      if (this.particle != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 135), varOffset - offset - 147);
         varOffset += this.particle.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 135), -1);
      }

      if (this.uvMotion != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 139), varOffset - offset - 147);
         varOffset += this.uvMotion.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 139), -1);
      }

      if (this.attractors != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 143), varOffset - offset - 147);
         if (this.attractors.length > 4096000) {
            throw ProtocolException.arrayTooLong("Attractors", this.attractors.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.attractors.length);
         int attractorsValueOffset = 0;

         for (int i = 0; i < this.attractors.length; i++) {
            attractorsValueOffset += this.attractors[i].serialize(mem, varOffset + attractorsValueOffset);
         }

         varOffset += attractorsValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 143), -1);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 147;
      if (this.id != null) {
         size += PacketIO.stringSize(this.id);
      }

      if (this.particle != null) {
         size += this.particle.computeSize();
      }

      if (this.uvMotion != null) {
         size += this.uvMotion.computeSize();
      }

      if (this.attractors != null) {
         size += VarInt.size(this.attractors.length) + this.attractors.length * 85;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 147) {
         return ValidationResult.error("Buffer too small: expected at least 147 bytes");
      } else {
         byte[] nullBits = PacketIO.readBytes(buffer, offset, 2);
         int v = buffer.getByte(offset + 2) & 255;
         if (v >= 2) {
            return ValidationResult.error("Invalid EmitShape value for Shape");
         } else {
            v = buffer.getByte(offset + 95) & 255;
            if (v >= 5) {
               return ValidationResult.error("Invalid ParticleRotationInfluence value for ParticleRotationInfluence");
            } else {
               v = buffer.getByte(offset + 109) & 255;
               if (v >= 4) {
                  return ValidationResult.error("Invalid FXRenderMode value for RenderMode");
               } else {
                  if ((nullBits[1] & 1) != 0) {
                     v = buffer.getIntLE(offset + 131);
                     if (v < 0 || v > buffer.writerIndex() - offset - 147) {
                        return ValidationResult.error("Invalid offset for Id");
                     }

                     int pos = offset + 147 + v;
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

                  if ((nullBits[1] & 2) != 0) {
                     v = buffer.getIntLE(offset + 135);
                     if (v < 0 || v > buffer.writerIndex() - offset - 147) {
                        return ValidationResult.error("Invalid offset for Particle");
                     }

                     int posx = offset + 147 + v;
                     ValidationResult particleResult = Particle.validateStructure(buffer, posx);
                     if (!particleResult.isValid()) {
                        return ValidationResult.error("Invalid Particle: " + particleResult.error());
                     }

                     posx += Particle.computeBytesConsumed(buffer, posx);
                  }

                  if ((nullBits[1] & 4) != 0) {
                     v = buffer.getIntLE(offset + 139);
                     if (v < 0 || v > buffer.writerIndex() - offset - 147) {
                        return ValidationResult.error("Invalid offset for UvMotion");
                     }

                     int posx = offset + 147 + v;
                     ValidationResult uvMotionResult = UVMotion.validateStructure(buffer, posx);
                     if (!uvMotionResult.isValid()) {
                        return ValidationResult.error("Invalid UvMotion: " + uvMotionResult.error());
                     }

                     posx += UVMotion.computeBytesConsumed(buffer, posx);
                  }

                  if ((nullBits[1] & 8) != 0) {
                     v = buffer.getIntLE(offset + 143);
                     if (v < 0 || v > buffer.writerIndex() - offset - 147) {
                        return ValidationResult.error("Invalid offset for Attractors");
                     }

                     int posx = offset + 147 + v;
                     int attractorsCount = VarInt.peek(buffer, posx);
                     if (attractorsCount < 0) {
                        return ValidationResult.error("Invalid array count for Attractors");
                     }

                     if (attractorsCount > 4096000) {
                        return ValidationResult.error("Attractors exceeds max length 4096000");
                     }

                     posx += VarInt.size(attractorsCount);
                     posx += attractorsCount * 85;
                     if (posx > buffer.writerIndex()) {
                        return ValidationResult.error("Buffer overflow reading Attractors");
                     }
                  }

                  return ValidationResult.OK;
               }
            }
         }
      }
   }

   public ParticleSpawner clone() {
      ParticleSpawner copy = new ParticleSpawner();
      copy.id = this.id;
      copy.particle = this.particle != null ? this.particle.clone() : null;
      copy.shape = this.shape;
      copy.emitOffset = this.emitOffset != null ? this.emitOffset.clone() : null;
      copy.cameraOffset = this.cameraOffset;
      copy.useEmitDirection = this.useEmitDirection;
      copy.lifeSpan = this.lifeSpan;
      copy.spawnRate = this.spawnRate != null ? this.spawnRate.clone() : null;
      copy.spawnBurst = this.spawnBurst;
      copy.waveDelay = this.waveDelay != null ? this.waveDelay.clone() : null;
      copy.totalParticles = this.totalParticles != null ? this.totalParticles.clone() : null;
      copy.maxConcurrentParticles = this.maxConcurrentParticles;
      copy.initialVelocity = this.initialVelocity != null ? this.initialVelocity.clone() : null;
      copy.velocityStretchMultiplier = this.velocityStretchMultiplier;
      copy.particleRotationInfluence = this.particleRotationInfluence;
      copy.particleRotateWithSpawner = this.particleRotateWithSpawner;
      copy.isLowRes = this.isLowRes;
      copy.trailSpawnerPositionMultiplier = this.trailSpawnerPositionMultiplier;
      copy.trailSpawnerRotationMultiplier = this.trailSpawnerRotationMultiplier;
      copy.particleCollision = this.particleCollision != null ? this.particleCollision.clone() : null;
      copy.renderMode = this.renderMode;
      copy.lightInfluence = this.lightInfluence;
      copy.linearFiltering = this.linearFiltering;
      copy.particleLifeSpan = this.particleLifeSpan != null ? this.particleLifeSpan.clone() : null;
      copy.uvMotion = this.uvMotion != null ? this.uvMotion.clone() : null;
      copy.attractors = this.attractors != null ? Arrays.stream(this.attractors).map(e -> e.clone()).toArray(ParticleAttractor[]::new) : null;
      copy.intersectionHighlight = this.intersectionHighlight != null ? this.intersectionHighlight.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ParticleSpawner other)
            ? false
            : Objects.equals(this.id, other.id)
               && Objects.equals(this.particle, other.particle)
               && Objects.equals(this.shape, other.shape)
               && Objects.equals(this.emitOffset, other.emitOffset)
               && this.cameraOffset == other.cameraOffset
               && this.useEmitDirection == other.useEmitDirection
               && this.lifeSpan == other.lifeSpan
               && Objects.equals(this.spawnRate, other.spawnRate)
               && this.spawnBurst == other.spawnBurst
               && Objects.equals(this.waveDelay, other.waveDelay)
               && Objects.equals(this.totalParticles, other.totalParticles)
               && this.maxConcurrentParticles == other.maxConcurrentParticles
               && Objects.equals(this.initialVelocity, other.initialVelocity)
               && this.velocityStretchMultiplier == other.velocityStretchMultiplier
               && Objects.equals(this.particleRotationInfluence, other.particleRotationInfluence)
               && this.particleRotateWithSpawner == other.particleRotateWithSpawner
               && this.isLowRes == other.isLowRes
               && this.trailSpawnerPositionMultiplier == other.trailSpawnerPositionMultiplier
               && this.trailSpawnerRotationMultiplier == other.trailSpawnerRotationMultiplier
               && Objects.equals(this.particleCollision, other.particleCollision)
               && Objects.equals(this.renderMode, other.renderMode)
               && this.lightInfluence == other.lightInfluence
               && this.linearFiltering == other.linearFiltering
               && Objects.equals(this.particleLifeSpan, other.particleLifeSpan)
               && Objects.equals(this.uvMotion, other.uvMotion)
               && Arrays.equals((Object[])this.attractors, (Object[])other.attractors)
               && Objects.equals(this.intersectionHighlight, other.intersectionHighlight);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.id);
      result = 31 * result + Objects.hashCode(this.particle);
      result = 31 * result + Objects.hashCode(this.shape);
      result = 31 * result + Objects.hashCode(this.emitOffset);
      result = 31 * result + Float.hashCode(this.cameraOffset);
      result = 31 * result + Boolean.hashCode(this.useEmitDirection);
      result = 31 * result + Float.hashCode(this.lifeSpan);
      result = 31 * result + Objects.hashCode(this.spawnRate);
      result = 31 * result + Boolean.hashCode(this.spawnBurst);
      result = 31 * result + Objects.hashCode(this.waveDelay);
      result = 31 * result + Objects.hashCode(this.totalParticles);
      result = 31 * result + Integer.hashCode(this.maxConcurrentParticles);
      result = 31 * result + Objects.hashCode(this.initialVelocity);
      result = 31 * result + Float.hashCode(this.velocityStretchMultiplier);
      result = 31 * result + Objects.hashCode(this.particleRotationInfluence);
      result = 31 * result + Boolean.hashCode(this.particleRotateWithSpawner);
      result = 31 * result + Boolean.hashCode(this.isLowRes);
      result = 31 * result + Float.hashCode(this.trailSpawnerPositionMultiplier);
      result = 31 * result + Float.hashCode(this.trailSpawnerRotationMultiplier);
      result = 31 * result + Objects.hashCode(this.particleCollision);
      result = 31 * result + Objects.hashCode(this.renderMode);
      result = 31 * result + Float.hashCode(this.lightInfluence);
      result = 31 * result + Boolean.hashCode(this.linearFiltering);
      result = 31 * result + Objects.hashCode(this.particleLifeSpan);
      result = 31 * result + Objects.hashCode(this.uvMotion);
      result = 31 * result + Arrays.hashCode((Object[])this.attractors);
      return 31 * result + Objects.hashCode(this.intersectionHighlight);
   }
}
