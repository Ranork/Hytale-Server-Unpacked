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
      if (buf.readableBytes() - offset < 22) {
         throw ProtocolException.bufferTooSmall("ParticleSystem", 22, buf.readableBytes() - offset);
      } else {
         ParticleSystem obj = new ParticleSystem();
         byte nullBits = buf.getByte(offset);
         obj.lifeSpan = buf.getFloatLE(offset + 1);
         obj.cullDistance = buf.getFloatLE(offset + 5);
         obj.boundingRadius = buf.getFloatLE(offset + 9);
         obj.isImportant = buf.getByte(offset + 13) != 0;
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 14);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 22) {
               throw ProtocolException.invalidOffset("Id", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 22 + varPosBase0;
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

         if ((nullBits & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 18);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 22) {
               throw ProtocolException.invalidOffset("Spawners", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 22 + varPosBase1;
            int spawnersCount = VarInt.peek(buf, varPos1);
            if (spawnersCount < 0) {
               throw ProtocolException.invalidVarInt("Spawners");
            }

            int varIntLen = VarInt.size(spawnersCount);
            if (spawnersCount > 4096000) {
               throw ProtocolException.arrayTooLong("Spawners", spawnersCount, 4096000);
            }

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
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 22;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 14);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 22) {
            throw ProtocolException.invalidOffset("Id", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 22 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 18);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 22) {
            throw ProtocolException.invalidOffset("Spawners", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 22 + fieldOffset1;
         int arrLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            pos1 += ParticleSpawnerGroup.computeBytesConsumed(buf, pos1);
         }

         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 22L;
   }

   @Nullable
   public static String getId(MemorySegment mem) {
      return getId(mem, 0);
   }

   @Nullable
   public static String getId(MemorySegment mem, int offset) {
      return hasId(mem, offset) ? PacketIO.readVarString("Id", mem, offset + getValidatedOffset(mem, offset, 14, 22, "Id"), 4096000, PacketIO.UTF8) : null;
   }

   @Nullable
   public static ParticleSpawnerGroup[] getSpawners(MemorySegment mem) {
      return getSpawners(mem, 0);
   }

   @Nullable
   public static ParticleSpawnerGroup[] getSpawners(MemorySegment mem, int offset) {
      if (!hasSpawners(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 18, 22, "Spawners");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Spawners", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("Spawners", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Spawners", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               ParticleSpawnerGroup[] data = new ParticleSpawnerGroup[len];

               for (int i = 0; i < len; i++) {
                  data[i] = ParticleSpawnerGroup.toObject(mem, off);
                  off += data[i].computeSize();
               }

               return data;
            }
         }
      }
   }

   public static float getLifeSpan(MemorySegment mem) {
      return getLifeSpan(mem, 0);
   }

   public static float getLifeSpan(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 1));
   }

   public static float getCullDistance(MemorySegment mem) {
      return getCullDistance(mem, 0);
   }

   public static float getCullDistance(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 5));
   }

   public static float getBoundingRadius(MemorySegment mem) {
      return getBoundingRadius(mem, 0);
   }

   public static float getBoundingRadius(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 9));
   }

   public static boolean getIsImportant(MemorySegment mem) {
      return getIsImportant(mem, 0);
   }

   public static boolean getIsImportant(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 13));
   }

   public static boolean hasId(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasSpawners(MemorySegment mem, int offset) {
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

   public static ParticleSystem toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static ParticleSystem toObject(MemorySegment mem, int offset) {
      if (offset + 22 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("ParticleSystem", offset + 22, (int)mem.byteSize());
      } else {
         ParticleSpawnerGroup[] spawners = null;
         if (hasSpawners(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 18, 22, "Spawners");
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Spawners", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("Spawners", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Spawners", off + lenOffset + len, (int)mem.byteSize());
            }

            off += lenOffset;
            spawners = new ParticleSpawnerGroup[len];

            for (int i = 0; i < len; i++) {
               spawners[i] = ParticleSpawnerGroup.toObject(mem, off);
               off += spawners[i].computeSize();
            }
         }

         return new ParticleSystem(
            hasId(mem, offset) ? PacketIO.readVarString("Id", mem, offset + getValidatedOffset(mem, offset, 14, 22, "Id"), 4096000, PacketIO.UTF8) : null,
            spawners,
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 1)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 5)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 9)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 13))
         );
      }
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

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.id != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.spawners != null) {
         nullBits = (byte)(nullBits | 2);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 1), this.lifeSpan);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 5), this.cullDistance);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 9), this.boundingRadius);
      mem.set(PacketIO.PROTO_BOOL, offset + 13, this.isImportant);
      int varOffset = offset + 22;
      if (this.id != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 14), varOffset - offset - 22);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.id, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 14), -1);
      }

      if (this.spawners != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 18), varOffset - offset - 22);
         if (this.spawners.length > 4096000) {
            throw ProtocolException.arrayTooLong("Spawners", this.spawners.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.spawners.length);
         int spawnersValueOffset = 0;

         for (int i = 0; i < this.spawners.length; i++) {
            spawnersValueOffset += this.spawners[i].serialize(mem, varOffset + spawnersValueOffset);
         }

         varOffset += spawnersValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 18), -1);
      }

      return varOffset - offset;
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
            if (idOffset < 0 || idOffset > buffer.writerIndex() - offset - 22) {
               return ValidationResult.error("Invalid offset for Id");
            }

            int pos = offset + 22 + idOffset;
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

         if ((nullBits & 2) != 0) {
            int spawnersOffset = buffer.getIntLE(offset + 18);
            if (spawnersOffset < 0 || spawnersOffset > buffer.writerIndex() - offset - 22) {
               return ValidationResult.error("Invalid offset for Spawners");
            }

            int posx = offset + 22 + spawnersOffset;
            int spawnersCount = VarInt.peek(buffer, posx);
            if (spawnersCount < 0) {
               return ValidationResult.error("Invalid array count for Spawners");
            }

            if (spawnersCount > 4096000) {
               return ValidationResult.error("Spawners exceeds max length 4096000");
            }

            posx += VarInt.size(spawnersCount);

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
