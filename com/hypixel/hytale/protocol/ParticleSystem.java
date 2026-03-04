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

public class ParticleSystem {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 14;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 22;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public String id;
   @Nullable
   public ParticleSpawnerGroup[] spawners;
   public float lifeSpan;
   public float cullDistance;
   public float boundingRadius;
   public boolean isImportant;

   public ParticleSystem() {
   }

   public ParticleSystem(
      @Nullable String id, @Nullable ParticleSpawnerGroup[] spawners, float lifeSpan, float cullDistance, float boundingRadius, boolean isImportant
   ) {
      this.id = id;
      this.spawners = spawners;
      this.lifeSpan = lifeSpan;
      this.cullDistance = cullDistance;
      this.boundingRadius = boundingRadius;
      this.isImportant = isImportant;
   }

   public ParticleSystem(@Nonnull ParticleSystem other) {
      this.id = other.id;
      this.spawners = other.spawners;
      this.lifeSpan = other.lifeSpan;
      this.cullDistance = other.cullDistance;
      this.boundingRadius = other.boundingRadius;
      this.isImportant = other.isImportant;
   }

   @Nonnull
   public static ParticleSystem deserialize(@Nonnull ByteBuf buf, int offset) {
      ParticleSystem obj = new ParticleSystem();
      byte nullBits = buf.getByte(offset);
      obj.lifeSpan = buf.getFloatLE(offset + 1);
      obj.cullDistance = buf.getFloatLE(offset + 5);
      obj.boundingRadius = buf.getFloatLE(offset + 9);
      obj.isImportant = buf.getByte(offset + 13) != 0;
      if ((nullBits & 1) != 0) {
         int varPos0 = offset + 22 + buf.getIntLE(offset + 14);
         int idLen = VarInt.peek(buf, varPos0);
         if (idLen < 0) {
            throw ProtocolException.negativeLength("Id", idLen);
         }

         if (idLen > 4096000) {
            throw ProtocolException.stringTooLong("Id", idLen, 4096000);
         }

         obj.id = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
      }

      if ((nullBits & 2) != 0) {
         int varPos1 = offset + 22 + buf.getIntLE(offset + 18);
         int spawnersCount = VarInt.peek(buf, varPos1);
         if (spawnersCount < 0) {
            throw ProtocolException.negativeLength("Spawners", spawnersCount);
         }

         if (spawnersCount > 4096000) {
            throw ProtocolException.arrayTooLong("Spawners", spawnersCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos1);
         if (varPos1 + varIntLen + spawnersCount * 113L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("Spawners", varPos1 + varIntLen + spawnersCount * 113, buf.readableBytes());
         }

         obj.spawners = new ParticleSpawnerGroup[spawnersCount];
         int elemPos = varPos1 + varIntLen;

         for (int i = 0; i < spawnersCount; i++) {
            obj.spawners[i] = ParticleSpawnerGroup.deserialize(buf, elemPos);
            elemPos += ParticleSpawnerGroup.computeBytesConsumed(buf, elemPos);
         }
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 22;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 14);
         int pos0 = offset + 22 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 18);
         int pos1 = offset + 22 + fieldOffset1;
         int arrLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.length(buf, pos1);

         for (int i = 0; i < arrLen; i++) {
            pos1 += ParticleSpawnerGroup.computeBytesConsumed(buf, pos1);
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
      if (this.id != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.spawners != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      buf.writeFloatLE(this.lifeSpan);
      buf.writeFloatLE(this.cullDistance);
      buf.writeFloatLE(this.boundingRadius);
      buf.writeByte(this.isImportant ? 1 : 0);
      int idOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int spawnersOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.id != null) {
         buf.setIntLE(idOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.id, 4096000);
      } else {
         buf.setIntLE(idOffsetSlot, -1);
      }

      if (this.spawners != null) {
         buf.setIntLE(spawnersOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.spawners.length > 4096000) {
            throw ProtocolException.arrayTooLong("Spawners", this.spawners.length, 4096000);
         }

         VarInt.write(buf, this.spawners.length);

         for (ParticleSpawnerGroup item : this.spawners) {
            item.serialize(buf);
         }
      } else {
         buf.setIntLE(spawnersOffsetSlot, -1);
      }
   }

   public int computeSize() {
      int size = 22;
      if (this.id != null) {
         size += PacketIO.stringSize(this.id);
      }

      if (this.spawners != null) {
         int spawnersSize = 0;

         for (ParticleSpawnerGroup elem : this.spawners) {
            spawnersSize += elem.computeSize();
         }

         size += VarInt.size(this.spawners.length) + spawnersSize;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 22) {
         return ValidationResult.error("Buffer too small: expected at least 22 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int idOffset = buffer.getIntLE(offset + 14);
            if (idOffset < 0) {
               return ValidationResult.error("Invalid offset for Id");
            }

            int pos = offset + 22 + idOffset;
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

         if ((nullBits & 2) != 0) {
            int spawnersOffset = buffer.getIntLE(offset + 18);
            if (spawnersOffset < 0) {
               return ValidationResult.error("Invalid offset for Spawners");
            }

            int posx = offset + 22 + spawnersOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Spawners");
            }

            int spawnersCount = VarInt.peek(buffer, posx);
            if (spawnersCount < 0) {
               return ValidationResult.error("Invalid array count for Spawners");
            }

            if (spawnersCount > 4096000) {
               return ValidationResult.error("Spawners exceeds max length 4096000");
            }

            posx += VarInt.length(buffer, posx);

            for (int i = 0; i < spawnersCount; i++) {
               ValidationResult structResult = ParticleSpawnerGroup.validateStructure(buffer, posx);
               if (!structResult.isValid()) {
                  return ValidationResult.error("Invalid ParticleSpawnerGroup in Spawners[" + i + "]: " + structResult.error());
               }

               posx += ParticleSpawnerGroup.computeBytesConsumed(buffer, posx);
            }
         }

         return ValidationResult.OK;
      }
   }

   public ParticleSystem clone() {
      ParticleSystem copy = new ParticleSystem();
      copy.id = this.id;
      copy.spawners = this.spawners != null ? Arrays.stream(this.spawners).map(e -> e.clone()).toArray(ParticleSpawnerGroup[]::new) : null;
      copy.lifeSpan = this.lifeSpan;
      copy.cullDistance = this.cullDistance;
      copy.boundingRadius = this.boundingRadius;
      copy.isImportant = this.isImportant;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ParticleSystem other)
            ? false
            : Objects.equals(this.id, other.id)
               && Arrays.equals((Object[])this.spawners, (Object[])other.spawners)
               && this.lifeSpan == other.lifeSpan
               && this.cullDistance == other.cullDistance
               && this.boundingRadius == other.boundingRadius
               && this.isImportant == other.isImportant;
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.id);
      result = 31 * result + Arrays.hashCode((Object[])this.spawners);
      result = 31 * result + Float.hashCode(this.lifeSpan);
      result = 31 * result + Float.hashCode(this.cullDistance);
      result = 31 * result + Float.hashCode(this.boundingRadius);
      return 31 * result + Boolean.hashCode(this.isImportant);
   }
}
