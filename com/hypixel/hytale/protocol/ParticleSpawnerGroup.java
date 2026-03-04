package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ParticleSpawnerGroup {
   public static final int NULLABLE_BIT_FIELD_SIZE = 2;
   public static final int FIXED_BLOCK_SIZE = 113;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 121;
   public static final int MAX_SIZE = 364544131;
   @Nullable
   public String spawnerId;
   @Nullable
   public Vector3f positionOffset;
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
      @Nullable Vector3f positionOffset,
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
      ParticleSpawnerGroup obj = new ParticleSpawnerGroup();
      byte[] nullBits = PacketIO.readBytes(buf, offset, 2);
      if ((nullBits[0] & 1) != 0) {
         obj.positionOffset = Vector3f.deserialize(buf, offset + 2);
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
         int varPos0 = offset + 121 + buf.getIntLE(offset + 113);
         int spawnerIdLen = VarInt.peek(buf, varPos0);
         if (spawnerIdLen < 0) {
            throw ProtocolException.negativeLength("SpawnerId", spawnerIdLen);
         }

         if (spawnerIdLen > 4096000) {
            throw ProtocolException.stringTooLong("SpawnerId", spawnerIdLen, 4096000);
         }

         obj.spawnerId = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
      }

      if ((nullBits[1] & 1) != 0) {
         int varPos1 = offset + 121 + buf.getIntLE(offset + 117);
         int attractorsCount = VarInt.peek(buf, varPos1);
         if (attractorsCount < 0) {
            throw ProtocolException.negativeLength("Attractors", attractorsCount);
         }

         if (attractorsCount > 4096000) {
            throw ProtocolException.arrayTooLong("Attractors", attractorsCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos1);
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

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte[] nullBits = PacketIO.readBytes(buf, offset, 2);
      int maxEnd = 121;
      if ((nullBits[0] & 128) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 113);
         int pos0 = offset + 121 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits[1] & 1) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 117);
         int pos1 = offset + 121 + fieldOffset1;
         int arrLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.length(buf, pos1);

         for (int i = 0; i < arrLen; i++) {
            pos1 += ParticleAttractor.computeBytesConsumed(buf, pos1);
         }

         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
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
         this.positionOffset.serialize(buf);
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
            if (spawnerIdOffset < 0) {
               return ValidationResult.error("Invalid offset for SpawnerId");
            }

            int pos = offset + 121 + spawnerIdOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for SpawnerId");
            }

            int spawnerIdLen = VarInt.peek(buffer, pos);
            if (spawnerIdLen < 0) {
               return ValidationResult.error("Invalid string length for SpawnerId");
            }

            if (spawnerIdLen > 4096000) {
               return ValidationResult.error("SpawnerId exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += spawnerIdLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading SpawnerId");
            }
         }

         if ((nullBits[1] & 1) != 0) {
            int attractorsOffset = buffer.getIntLE(offset + 117);
            if (attractorsOffset < 0) {
               return ValidationResult.error("Invalid offset for Attractors");
            }

            int posx = offset + 121 + attractorsOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Attractors");
            }

            int attractorsCount = VarInt.peek(buffer, posx);
            if (attractorsCount < 0) {
               return ValidationResult.error("Invalid array count for Attractors");
            }

            if (attractorsCount > 4096000) {
               return ValidationResult.error("Attractors exceeds max length 4096000");
            }

            posx += VarInt.length(buffer, posx);
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
      copy.positionOffset = this.positionOffset != null ? this.positionOffset.clone() : null;
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
