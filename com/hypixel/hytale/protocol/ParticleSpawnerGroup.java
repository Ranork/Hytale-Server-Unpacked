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
import org.joml.Vector3fc;

public class ParticleSpawnerGroup {
   public static final int NULLABLE_BIT_FIELD_SIZE = 2;
   public static final int FIXED_BLOCK_SIZE = 113;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 121;
   public static final int MAX_SIZE = 364544131;
   @Nullable
   public String spawnerId;
   @Nullable
   public Vector3fc positionOffset;
   @Nullable
   public Direction rotationOffset;
   public boolean fixedRotation;
   public float startDelay;
   @Nullable
   public Rangef spawnRate;
   @Nullable
   public Rangef waveDelay;
   public int totalSpawners;
   public int maxConcurrent;
   @Nullable
   public InitialVelocity initialVelocity;
   @Nullable
   public RangeVector3f emitOffset;
   @Nullable
   public Rangef lifeSpan;
   @Nullable
   public ParticleAttractor[] attractors;

   public ParticleSpawnerGroup() {
   }

   public ParticleSpawnerGroup(
      @Nullable String spawnerId,
      @Nullable Vector3fc positionOffset,
      @Nullable Direction rotationOffset,
      boolean fixedRotation,
      float startDelay,
      @Nullable Rangef spawnRate,
      @Nullable Rangef waveDelay,
      int totalSpawners,
      int maxConcurrent,
      @Nullable InitialVelocity initialVelocity,
      @Nullable RangeVector3f emitOffset,
      @Nullable Rangef lifeSpan,
      @Nullable ParticleAttractor[] attractors
   ) {
      this.spawnerId = spawnerId;
      this.positionOffset = positionOffset;
      this.rotationOffset = rotationOffset;
      this.fixedRotation = fixedRotation;
      this.startDelay = startDelay;
      this.spawnRate = spawnRate;
      this.waveDelay = waveDelay;
      this.totalSpawners = totalSpawners;
      this.maxConcurrent = maxConcurrent;
      this.initialVelocity = initialVelocity;
      this.emitOffset = emitOffset;
      this.lifeSpan = lifeSpan;
      this.attractors = attractors;
   }

   public ParticleSpawnerGroup(@Nonnull ParticleSpawnerGroup other) {
      this.spawnerId = other.spawnerId;
      this.positionOffset = other.positionOffset;
      this.rotationOffset = other.rotationOffset;
      this.fixedRotation = other.fixedRotation;
      this.startDelay = other.startDelay;
      this.spawnRate = other.spawnRate;
      this.waveDelay = other.waveDelay;
      this.totalSpawners = other.totalSpawners;
      this.maxConcurrent = other.maxConcurrent;
      this.initialVelocity = other.initialVelocity;
      this.emitOffset = other.emitOffset;
      this.lifeSpan = other.lifeSpan;
      this.attractors = other.attractors;
   }

   @Nonnull
   public static ParticleSpawnerGroup deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 121) {
         throw ProtocolException.bufferTooSmall("ParticleSpawnerGroup", 121, buf.readableBytes() - offset);
      } else {
         ParticleSpawnerGroup obj = new ParticleSpawnerGroup();
         byte[] nullBits = PacketIO.readBytes(buf, offset, 2);
         if ((nullBits[0] & 1) != 0) {
            obj.positionOffset = PacketIO.readVector3f(buf, offset + 2);
         }

         if ((nullBits[0] & 2) != 0) {
            obj.rotationOffset = Direction.deserialize(buf, offset + 14);
         }

         obj.fixedRotation = buf.getByte(offset + 26) != 0;
         obj.startDelay = buf.getFloatLE(offset + 27);
         if ((nullBits[0] & 4) != 0) {
            obj.spawnRate = Rangef.deserialize(buf, offset + 31);
         }

         if ((nullBits[0] & 8) != 0) {
            obj.waveDelay = Rangef.deserialize(buf, offset + 39);
         }

         obj.totalSpawners = buf.getIntLE(offset + 47);
         obj.maxConcurrent = buf.getIntLE(offset + 51);
         if ((nullBits[0] & 16) != 0) {
            obj.initialVelocity = InitialVelocity.deserialize(buf, offset + 55);
         }

         if ((nullBits[0] & 32) != 0) {
            obj.emitOffset = RangeVector3f.deserialize(buf, offset + 80);
         }

         if ((nullBits[0] & 64) != 0) {
            obj.lifeSpan = Rangef.deserialize(buf, offset + 105);
         }

         if ((nullBits[0] & 128) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 113);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 121) {
               throw ProtocolException.invalidOffset("SpawnerId", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 121 + varPosBase0;
            int spawnerIdLen = VarInt.peek(buf, varPos0);
            if (spawnerIdLen < 0) {
               throw ProtocolException.invalidVarInt("SpawnerId");
            }

            int spawnerIdVarIntLen = VarInt.size(spawnerIdLen);
            if (spawnerIdLen > 4096000) {
               throw ProtocolException.stringTooLong("SpawnerId", spawnerIdLen, 4096000);
            }

            if (varPos0 + spawnerIdVarIntLen + spawnerIdLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("SpawnerId", varPos0 + spawnerIdVarIntLen + spawnerIdLen, buf.readableBytes());
            }

            obj.spawnerId = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
         }

         if ((nullBits[1] & 1) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 117);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 121) {
               throw ProtocolException.invalidOffset("Attractors", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 121 + varPosBase1;
            int attractorsCount = VarInt.peek(buf, varPos1);
            if (attractorsCount < 0) {
               throw ProtocolException.invalidVarInt("Attractors");
            }

            int varIntLen = VarInt.size(attractorsCount);
            if (attractorsCount > 4096000) {
               throw ProtocolException.arrayTooLong("Attractors", attractorsCount, 4096000);
            }

            if (varPos1 + varIntLen + attractorsCount * 85L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Attractors", varPos1 + varIntLen + attractorsCount * 85, buf.readableBytes());
            }

            obj.attractors = new ParticleAttractor[attractorsCount];
            int elemPos = varPos1 + varIntLen;

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
      int maxEnd = 121;
      if ((nullBits[0] & 128) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 113);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 121) {
            throw ProtocolException.invalidOffset("SpawnerId", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 121 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits[1] & 1) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 117);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 121) {
            throw ProtocolException.invalidOffset("Attractors", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 121 + fieldOffset1;
         int arrLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            pos1 += ParticleAttractor.computeBytesConsumed(buf, pos1);
         }

         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 121L;
   }

   @Nullable
   public static String getSpawnerId(MemorySegment mem) {
      return getSpawnerId(mem, 0);
   }

   @Nullable
   public static String getSpawnerId(MemorySegment mem, int offset) {
      return hasSpawnerId(mem, offset)
         ? PacketIO.readVarString("SpawnerId", mem, offset + getValidatedOffset(mem, offset, 113, 121, "SpawnerId"), 4096000, PacketIO.UTF8)
         : null;
   }

   @Nullable
   public static Vector3fc getPositionOffset(MemorySegment mem) {
      return getPositionOffset(mem, 0);
   }

   @Nullable
   public static Vector3fc getPositionOffset(MemorySegment mem, int offset) {
      return hasPositionOffset(mem, offset) ? PacketIO.readVector3f(mem, offset + 2) : null;
   }

   @Nullable
   public static Direction getRotationOffset(MemorySegment mem) {
      return getRotationOffset(mem, 0);
   }

   @Nullable
   public static Direction getRotationOffset(MemorySegment mem, int offset) {
      return hasRotationOffset(mem, offset) ? Direction.toObject(mem, offset + 14) : null;
   }

   public static boolean getFixedRotation(MemorySegment mem) {
      return getFixedRotation(mem, 0);
   }

   public static boolean getFixedRotation(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 26));
   }

   public static float getStartDelay(MemorySegment mem) {
      return getStartDelay(mem, 0);
   }

   public static float getStartDelay(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 27));
   }

   @Nullable
   public static Rangef getSpawnRate(MemorySegment mem) {
      return getSpawnRate(mem, 0);
   }

   @Nullable
   public static Rangef getSpawnRate(MemorySegment mem, int offset) {
      return hasSpawnRate(mem, offset) ? Rangef.toObject(mem, offset + 31) : null;
   }

   @Nullable
   public static Rangef getWaveDelay(MemorySegment mem) {
      return getWaveDelay(mem, 0);
   }

   @Nullable
   public static Rangef getWaveDelay(MemorySegment mem, int offset) {
      return hasWaveDelay(mem, offset) ? Rangef.toObject(mem, offset + 39) : null;
   }

   public static int getTotalSpawners(MemorySegment mem) {
      return getTotalSpawners(mem, 0);
   }

   public static int getTotalSpawners(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 47));
   }

   public static int getMaxConcurrent(MemorySegment mem) {
      return getMaxConcurrent(mem, 0);
   }

   public static int getMaxConcurrent(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 51));
   }

   @Nullable
   public static InitialVelocity getInitialVelocity(MemorySegment mem) {
      return getInitialVelocity(mem, 0);
   }

   @Nullable
   public static InitialVelocity getInitialVelocity(MemorySegment mem, int offset) {
      return hasInitialVelocity(mem, offset) ? InitialVelocity.toObject(mem, offset + 55) : null;
   }

   @Nullable
   public static RangeVector3f getEmitOffset(MemorySegment mem) {
      return getEmitOffset(mem, 0);
   }

   @Nullable
   public static RangeVector3f getEmitOffset(MemorySegment mem, int offset) {
      return hasEmitOffset(mem, offset) ? RangeVector3f.toObject(mem, offset + 80) : null;
   }

   @Nullable
   public static Rangef getLifeSpan(MemorySegment mem) {
      return getLifeSpan(mem, 0);
   }

   @Nullable
   public static Rangef getLifeSpan(MemorySegment mem, int offset) {
      return hasLifeSpan(mem, offset) ? Rangef.toObject(mem, offset + 105) : null;
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
         int off = offset + getValidatedOffset(mem, offset, 117, 121, "Attractors");
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

   public static boolean hasPositionOffset(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasRotationOffset(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasSpawnRate(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 4) != 0;
   }

   public static boolean hasWaveDelay(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 8) != 0;
   }

   public static boolean hasInitialVelocity(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 16) != 0;
   }

   public static boolean hasEmitOffset(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 32) != 0;
   }

   public static boolean hasLifeSpan(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 64) != 0;
   }

   public static boolean hasSpawnerId(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 128) != 0;
   }

   public static boolean hasAttractors(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1));
      return (b & 1) != 0;
   }

   private static int getValidatedOffset(MemorySegment buffer, int base, int slotPosition, int varBlockStart, String fieldName) {
      int offset = buffer.get(PacketIO.PROTO_INT, (long)(base + slotPosition));
      if (offset >= 0 && offset <= buffer.byteSize() - base - varBlockStart) {
         return varBlockStart + offset;
      } else {
         throw ProtocolException.invalidOffset(fieldName, offset, (int)buffer.byteSize());
      }
   }

   public static ParticleSpawnerGroup toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static ParticleSpawnerGroup toObject(MemorySegment mem, int offset) {
      if (offset + 121 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("ParticleSpawnerGroup", offset + 121, (int)mem.byteSize());
      } else {
         ParticleAttractor[] attractors = null;
         if (hasAttractors(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 117, 121, "Attractors");
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

         return new ParticleSpawnerGroup(
            hasSpawnerId(mem, offset)
               ? PacketIO.readVarString("SpawnerId", mem, offset + getValidatedOffset(mem, offset, 113, 121, "SpawnerId"), 4096000, PacketIO.UTF8)
               : null,
            hasPositionOffset(mem, offset) ? PacketIO.readVector3f(mem, offset + 2) : null,
            hasRotationOffset(mem, offset) ? Direction.toObject(mem, offset + 14) : null,
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 26)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 27)),
            hasSpawnRate(mem, offset) ? Rangef.toObject(mem, offset + 31) : null,
            hasWaveDelay(mem, offset) ? Rangef.toObject(mem, offset + 39) : null,
            mem.get(PacketIO.PROTO_INT, (long)(offset + 47)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 51)),
            hasInitialVelocity(mem, offset) ? InitialVelocity.toObject(mem, offset + 55) : null,
            hasEmitOffset(mem, offset) ? RangeVector3f.toObject(mem, offset + 80) : null,
            hasLifeSpan(mem, offset) ? Rangef.toObject(mem, offset + 105) : null,
            attractors
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte[] nullBits = new byte[2];
      if (this.positionOffset != null) {
         nullBits[0] = (byte)(nullBits[0] | 1);
      }

      if (this.rotationOffset != null) {
         nullBits[0] = (byte)(nullBits[0] | 2);
      }

      if (this.spawnRate != null) {
         nullBits[0] = (byte)(nullBits[0] | 4);
      }

      if (this.waveDelay != null) {
         nullBits[0] = (byte)(nullBits[0] | 8);
      }

      if (this.initialVelocity != null) {
         nullBits[0] = (byte)(nullBits[0] | 16);
      }

      if (this.emitOffset != null) {
         nullBits[0] = (byte)(nullBits[0] | 32);
      }

      if (this.lifeSpan != null) {
         nullBits[0] = (byte)(nullBits[0] | 64);
      }

      if (this.spawnerId != null) {
         nullBits[0] = (byte)(nullBits[0] | 128);
      }

      if (this.attractors != null) {
         nullBits[1] = (byte)(nullBits[1] | 1);
      }

      buf.writeBytes(nullBits);
      if (this.positionOffset != null) {
         PacketIO.writeVector3f(buf, this.positionOffset);
      } else {
         buf.writeZero(12);
      }

      if (this.rotationOffset != null) {
         this.rotationOffset.serialize(buf);
      } else {
         buf.writeZero(12);
      }

      buf.writeByte(this.fixedRotation ? 1 : 0);
      buf.writeFloatLE(this.startDelay);
      if (this.spawnRate != null) {
         this.spawnRate.serialize(buf);
      } else {
         buf.writeZero(8);
      }

      if (this.waveDelay != null) {
         this.waveDelay.serialize(buf);
      } else {
         buf.writeZero(8);
      }

      buf.writeIntLE(this.totalSpawners);
      buf.writeIntLE(this.maxConcurrent);
      if (this.initialVelocity != null) {
         this.initialVelocity.serialize(buf);
      } else {
         buf.writeZero(25);
      }

      if (this.emitOffset != null) {
         this.emitOffset.serialize(buf);
      } else {
         buf.writeZero(25);
      }

      if (this.lifeSpan != null) {
         this.lifeSpan.serialize(buf);
      } else {
         buf.writeZero(8);
      }

      int spawnerIdOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int attractorsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.spawnerId != null) {
         buf.setIntLE(spawnerIdOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.spawnerId, 4096000);
      } else {
         buf.setIntLE(spawnerIdOffsetSlot, -1);
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
      if (this.positionOffset != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.rotationOffset != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.spawnRate != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.waveDelay != null) {
         nullBits = (byte)(nullBits | 8);
      }

      if (this.initialVelocity != null) {
         nullBits = (byte)(nullBits | 16);
      }

      if (this.emitOffset != null) {
         nullBits = (byte)(nullBits | 32);
      }

      if (this.lifeSpan != null) {
         nullBits = (byte)(nullBits | 64);
      }

      if (this.spawnerId != null) {
         nullBits = (byte)(nullBits | 128);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      nullBits = 0;
      if (this.attractors != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 1), nullBits);
      if (this.positionOffset != null) {
         PacketIO.writeVector3f(mem, offset + 2, this.positionOffset);
      } else {
         mem.asSlice(offset + 2, 12L).fill((byte)0);
      }

      if (this.rotationOffset != null) {
         this.rotationOffset.serialize(mem, offset + 14);
      } else {
         mem.asSlice(offset + 14, 12L).fill((byte)0);
      }

      mem.set(PacketIO.PROTO_BOOL, offset + 26, this.fixedRotation);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 27), this.startDelay);
      if (this.spawnRate != null) {
         this.spawnRate.serialize(mem, offset + 31);
      } else {
         mem.asSlice(offset + 31, 8L).fill((byte)0);
      }

      if (this.waveDelay != null) {
         this.waveDelay.serialize(mem, offset + 39);
      } else {
         mem.asSlice(offset + 39, 8L).fill((byte)0);
      }

      mem.set(PacketIO.PROTO_INT, (long)(offset + 47), this.totalSpawners);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 51), this.maxConcurrent);
      if (this.initialVelocity != null) {
         this.initialVelocity.serialize(mem, offset + 55);
      } else {
         mem.asSlice(offset + 55, 25L).fill((byte)0);
      }

      if (this.emitOffset != null) {
         this.emitOffset.serialize(mem, offset + 80);
      } else {
         mem.asSlice(offset + 80, 25L).fill((byte)0);
      }

      if (this.lifeSpan != null) {
         this.lifeSpan.serialize(mem, offset + 105);
      } else {
         mem.asSlice(offset + 105, 8L).fill((byte)0);
      }

      int varOffset = offset + 121;
      if (this.spawnerId != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 113), varOffset - offset - 121);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.spawnerId, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 113), -1);
      }

      if (this.attractors != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 117), varOffset - offset - 121);
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
         mem.set(PacketIO.PROTO_INT, (long)(offset + 117), -1);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 121;
      if (this.spawnerId != null) {
         size += PacketIO.stringSize(this.spawnerId);
      }

      if (this.attractors != null) {
         size += VarInt.size(this.attractors.length) + this.attractors.length * 85;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 121) {
         return ValidationResult.error("Buffer too small: expected at least 121 bytes");
      } else {
         byte[] nullBits = PacketIO.readBytes(buffer, offset, 2);
         if ((nullBits[0] & 128) != 0) {
            int spawnerIdOffset = buffer.getIntLE(offset + 113);
            if (spawnerIdOffset < 0 || spawnerIdOffset > buffer.writerIndex() - offset - 121) {
               return ValidationResult.error("Invalid offset for SpawnerId");
            }

            int pos = offset + 121 + spawnerIdOffset;
            int spawnerIdLen = VarInt.peek(buffer, pos);
            if (spawnerIdLen < 0) {
               return ValidationResult.error("Invalid string length for SpawnerId");
            }

            if (spawnerIdLen > 4096000) {
               return ValidationResult.error("SpawnerId exceeds max length 4096000");
            }

            pos += VarInt.size(spawnerIdLen);
            pos += spawnerIdLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading SpawnerId");
            }
         }

         if ((nullBits[1] & 1) != 0) {
            int attractorsOffset = buffer.getIntLE(offset + 117);
            if (attractorsOffset < 0 || attractorsOffset > buffer.writerIndex() - offset - 121) {
               return ValidationResult.error("Invalid offset for Attractors");
            }

            int posx = offset + 121 + attractorsOffset;
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

   public ParticleSpawnerGroup clone() {
      ParticleSpawnerGroup copy = new ParticleSpawnerGroup();
      copy.spawnerId = this.spawnerId;
      copy.positionOffset = this.positionOffset;
      copy.rotationOffset = this.rotationOffset != null ? this.rotationOffset.clone() : null;
      copy.fixedRotation = this.fixedRotation;
      copy.startDelay = this.startDelay;
      copy.spawnRate = this.spawnRate != null ? this.spawnRate.clone() : null;
      copy.waveDelay = this.waveDelay != null ? this.waveDelay.clone() : null;
      copy.totalSpawners = this.totalSpawners;
      copy.maxConcurrent = this.maxConcurrent;
      copy.initialVelocity = this.initialVelocity != null ? this.initialVelocity.clone() : null;
      copy.emitOffset = this.emitOffset != null ? this.emitOffset.clone() : null;
      copy.lifeSpan = this.lifeSpan != null ? this.lifeSpan.clone() : null;
      copy.attractors = this.attractors != null ? Arrays.stream(this.attractors).map(e -> e.clone()).toArray(ParticleAttractor[]::new) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ParticleSpawnerGroup other)
            ? false
            : Objects.equals(this.spawnerId, other.spawnerId)
               && Objects.equals(this.positionOffset, other.positionOffset)
               && Objects.equals(this.rotationOffset, other.rotationOffset)
               && this.fixedRotation == other.fixedRotation
               && this.startDelay == other.startDelay
               && Objects.equals(this.spawnRate, other.spawnRate)
               && Objects.equals(this.waveDelay, other.waveDelay)
               && this.totalSpawners == other.totalSpawners
               && this.maxConcurrent == other.maxConcurrent
               && Objects.equals(this.initialVelocity, other.initialVelocity)
               && Objects.equals(this.emitOffset, other.emitOffset)
               && Objects.equals(this.lifeSpan, other.lifeSpan)
               && Arrays.equals((Object[])this.attractors, (Object[])other.attractors);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.spawnerId);
      result = 31 * result + Objects.hashCode(this.positionOffset);
      result = 31 * result + Objects.hashCode(this.rotationOffset);
      result = 31 * result + Boolean.hashCode(this.fixedRotation);
      result = 31 * result + Float.hashCode(this.startDelay);
      result = 31 * result + Objects.hashCode(this.spawnRate);
      result = 31 * result + Objects.hashCode(this.waveDelay);
      result = 31 * result + Integer.hashCode(this.totalSpawners);
      result = 31 * result + Integer.hashCode(this.maxConcurrent);
      result = 31 * result + Objects.hashCode(this.initialVelocity);
      result = 31 * result + Objects.hashCode(this.emitOffset);
      result = 31 * result + Objects.hashCode(this.lifeSpan);
      return 31 * result + Arrays.hashCode((Object[])this.attractors);
   }
}
