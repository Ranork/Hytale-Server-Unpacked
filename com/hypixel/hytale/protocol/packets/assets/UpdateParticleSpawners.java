package com.hypixel.hytale.protocol.packets.assets;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ParticleSpawner;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.UpdateType;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class UpdateParticleSpawners implements Packet, ToClientPacket {
   public static final int PACKET_ID = 50;
   public static final boolean IS_COMPRESSED = true;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 2;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 10;
   public static final int MAX_SIZE = 1677721600;
   @Nonnull
   public UpdateType type = UpdateType.Init;
   @Nullable
   public Map<String, ParticleSpawner> particleSpawners;
   @Nullable
   public String[] removedParticleSpawners;

   @Override
   public int getId() {
      return 50;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public UpdateParticleSpawners() {
   }

   public UpdateParticleSpawners(@Nonnull UpdateType type, @Nullable Map<String, ParticleSpawner> particleSpawners, @Nullable String[] removedParticleSpawners) {
      this.type = type;
      this.particleSpawners = particleSpawners;
      this.removedParticleSpawners = removedParticleSpawners;
   }

   public UpdateParticleSpawners(@Nonnull UpdateParticleSpawners other) {
      this.type = other.type;
      this.particleSpawners = other.particleSpawners;
      this.removedParticleSpawners = other.removedParticleSpawners;
   }

   @Nonnull
   public static UpdateParticleSpawners deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 10) {
         throw ProtocolException.bufferTooSmall("UpdateParticleSpawners", 10, buf.readableBytes() - offset);
      } else {
         UpdateParticleSpawners obj = new UpdateParticleSpawners();
         byte nullBits = buf.getByte(offset);
         obj.type = UpdateType.fromValue(buf.getByte(offset + 1));
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 2);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 10) {
               throw ProtocolException.invalidOffset("ParticleSpawners", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 10 + varPosBase0;
            int particleSpawnersCount = VarInt.peek(buf, varPos0);
            if (particleSpawnersCount < 0) {
               throw ProtocolException.invalidVarInt("ParticleSpawners");
            }

            int varIntLen = VarInt.size(particleSpawnersCount);
            if (particleSpawnersCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("ParticleSpawners", particleSpawnersCount, 4096000);
            }

            obj.particleSpawners = new HashMap<>(particleSpawnersCount);
            int dictPos = varPos0 + varIntLen;

            for (int i = 0; i < particleSpawnersCount; i++) {
               int keyLen = VarInt.peek(buf, dictPos);
               if (keyLen < 0) {
                  throw ProtocolException.invalidVarInt("key");
               }

               int keyVarLen = VarInt.size(keyLen);
               if (keyLen > 4096000) {
                  throw ProtocolException.stringTooLong("key", keyLen, 4096000);
               }

               if (dictPos + keyVarLen + keyLen > buf.readableBytes()) {
                  throw ProtocolException.bufferTooSmall("key", dictPos + keyVarLen + keyLen, buf.readableBytes());
               }

               String key = PacketIO.readVarString(buf, dictPos);
               dictPos += keyVarLen + keyLen;
               ParticleSpawner val = ParticleSpawner.deserialize(buf, dictPos);
               dictPos += ParticleSpawner.computeBytesConsumed(buf, dictPos);
               if (obj.particleSpawners.put(key, val) != null) {
                  throw ProtocolException.duplicateKey("particleSpawners", key);
               }
            }
         }

         if ((nullBits & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 6);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 10) {
               throw ProtocolException.invalidOffset("RemovedParticleSpawners", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 10 + varPosBase1;
            int removedParticleSpawnersCount = VarInt.peek(buf, varPos1);
            if (removedParticleSpawnersCount < 0) {
               throw ProtocolException.invalidVarInt("RemovedParticleSpawners");
            }

            int varIntLen = VarInt.size(removedParticleSpawnersCount);
            if (removedParticleSpawnersCount > 4096000) {
               throw ProtocolException.arrayTooLong("RemovedParticleSpawners", removedParticleSpawnersCount, 4096000);
            }

            if (varPos1 + varIntLen + removedParticleSpawnersCount * 1L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("RemovedParticleSpawners", varPos1 + varIntLen + removedParticleSpawnersCount * 1, buf.readableBytes());
            }

            obj.removedParticleSpawners = new String[removedParticleSpawnersCount];
            int elemPos = varPos1 + varIntLen;

            for (int i = 0; i < removedParticleSpawnersCount; i++) {
               int strLen = VarInt.peek(buf, elemPos);
               if (strLen < 0) {
                  throw ProtocolException.invalidVarInt("removedParticleSpawners[" + i + "]");
               }

               int strVarLen = VarInt.size(strLen);
               if (strLen > 4096000) {
                  throw ProtocolException.stringTooLong("removedParticleSpawners[" + i + "]", strLen, 4096000);
               }

               if (elemPos + strVarLen + strLen > buf.readableBytes()) {
                  throw ProtocolException.bufferTooSmall("removedParticleSpawners[" + i + "]", elemPos + strVarLen + strLen, buf.readableBytes());
               }

               obj.removedParticleSpawners[i] = PacketIO.readVarString(buf, elemPos);
               elemPos += strVarLen + strLen;
            }
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 10;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 2);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 10) {
            throw ProtocolException.invalidOffset("ParticleSpawners", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 10 + fieldOffset0;
         int dictLen = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(dictLen);

         for (int i = 0; i < dictLen; i++) {
            int sl = VarInt.peek(buf, pos0);
            pos0 += VarInt.size(sl) + sl;
            pos0 += ParticleSpawner.computeBytesConsumed(buf, pos0);
         }

         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 6);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 10) {
            throw ProtocolException.invalidOffset("RemovedParticleSpawners", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 10 + fieldOffset1;
         int arrLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            int sl = VarInt.peek(buf, pos1);
            pos1 += VarInt.size(sl) + sl;
         }

         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 10L;
   }

   public static UpdateType getType(MemorySegment mem) {
      return getType(mem, 0);
   }

   public static UpdateType getType(MemorySegment mem, int offset) {
      return UpdateType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1)));
   }

   @Nullable
   public static Map<String, ParticleSpawner> getParticleSpawners(MemorySegment mem) {
      return getParticleSpawners(mem, 0);
   }

   @Nullable
   public static Map<String, ParticleSpawner> getParticleSpawners(MemorySegment mem, int offset) {
      if (!hasParticleSpawners(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 2, 10, "ParticleSpawners");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("ParticleSpawners", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("ParticleSpawners", len, 4096000);
         } else {
            Map<String, ParticleSpawner> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               long keyPacked = VarInt.getWithLength(mem, off);
               int nkey = (int)keyPacked + (int)(keyPacked >>> 32);
               String key = PacketIO.readVarString("key", mem, off, 16384000, PacketIO.UTF8);
               off += nkey;
               ParticleSpawner value = ParticleSpawner.toObject(mem, off);
               off += value.computeSize();
               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("ParticleSpawners", key);
               }
            }

            return data;
         }
      }
   }

   @Nullable
   public static String[] getRemovedParticleSpawners(MemorySegment mem) {
      return getRemovedParticleSpawners(mem, 0);
   }

   @Nullable
   public static String[] getRemovedParticleSpawners(MemorySegment mem, int offset) {
      if (!hasRemovedParticleSpawners(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 6, 10, "RemovedParticleSpawners");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("RemovedParticleSpawners", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("RemovedParticleSpawners", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("RemovedParticleSpawners", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               String[] data = new String[len];

               for (int i = 0; i < len; i++) {
                  long sp = VarInt.getWithLength(mem, off);
                  int n = (int)sp + (int)(sp >>> 32);
                  data[i] = PacketIO.readVarString("RemovedParticleSpawners", mem, off, 16384000, PacketIO.UTF8);
                  off += n;
               }

               return data;
            }
         }
      }
   }

   public static boolean hasParticleSpawners(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasRemovedParticleSpawners(MemorySegment mem, int offset) {
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

   public static UpdateParticleSpawners toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static UpdateParticleSpawners toObject(MemorySegment mem, int offset) {
      if (offset + 10 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("UpdateParticleSpawners", offset + 10, (int)mem.byteSize());
      } else {
         Map<String, ParticleSpawner> particleSpawners = null;
         if (hasParticleSpawners(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 2, 10, "ParticleSpawners");
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("ParticleSpawners", len);
            }

            if (len > 4096000) {
               throw ProtocolException.dictionaryTooLarge("ParticleSpawners", len, 4096000);
            }

            particleSpawners = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               long keyPacked = VarInt.getWithLength(mem, off);
               int nkey = (int)keyPacked + (int)(keyPacked >>> 32);
               String key = PacketIO.readVarString("key", mem, off, 16384000, PacketIO.UTF8);
               off += nkey;
               ParticleSpawner value = ParticleSpawner.toObject(mem, off);
               off += value.computeSize();
               if (particleSpawners.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("ParticleSpawners", key);
               }
            }
         }

         String[] removedParticleSpawners = null;
         if (hasRemovedParticleSpawners(mem, offset)) {
            int offx = offset + getValidatedOffset(mem, offset, 6, 10, "RemovedParticleSpawners");
            long packedx = VarInt.getWithLength(mem, offx);
            int lenx = (int)packedx;
            if (lenx < 0) {
               throw ProtocolException.negativeLength("RemovedParticleSpawners", lenx);
            }

            if (lenx > 4096000) {
               throw ProtocolException.arrayTooLong("RemovedParticleSpawners", lenx, 4096000);
            }

            int lenOffset = (int)(packedx >>> 32);
            if (offx + lenOffset + lenx > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("RemovedParticleSpawners", offx + lenOffset + lenx, (int)mem.byteSize());
            }

            offx += lenOffset;
            removedParticleSpawners = new String[lenx];

            for (int ix = 0; ix < lenx; ix++) {
               long sp = VarInt.getWithLength(mem, offx);
               int n = (int)sp + (int)(sp >>> 32);
               removedParticleSpawners[ix] = PacketIO.readVarString("RemovedParticleSpawners", mem, offx, 16384000, PacketIO.UTF8);
               offx += n;
            }
         }

         return new UpdateParticleSpawners(UpdateType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1))), particleSpawners, removedParticleSpawners);
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.particleSpawners != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.removedParticleSpawners != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.type.getValue());
      int particleSpawnersOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int removedParticleSpawnersOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.particleSpawners != null) {
         buf.setIntLE(particleSpawnersOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.particleSpawners.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("ParticleSpawners", this.particleSpawners.size(), 4096000);
         }

         VarInt.write(buf, this.particleSpawners.size());

         for (Entry<String, ParticleSpawner> e : this.particleSpawners.entrySet()) {
            PacketIO.writeVarString(buf, e.getKey(), 4096000);
            e.getValue().serialize(buf);
         }
      } else {
         buf.setIntLE(particleSpawnersOffsetSlot, -1);
      }

      if (this.removedParticleSpawners != null) {
         buf.setIntLE(removedParticleSpawnersOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.removedParticleSpawners.length > 4096000) {
            throw ProtocolException.arrayTooLong("RemovedParticleSpawners", this.removedParticleSpawners.length, 4096000);
         }

         VarInt.write(buf, this.removedParticleSpawners.length);

         for (String item : this.removedParticleSpawners) {
            PacketIO.writeVarString(buf, item, 4096000);
         }
      } else {
         buf.setIntLE(removedParticleSpawnersOffsetSlot, -1);
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.particleSpawners != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.removedParticleSpawners != null) {
         nullBits = (byte)(nullBits | 2);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 1), (byte)this.type.getValue());
      int varOffset = offset + 10;
      if (this.particleSpawners != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 2), varOffset - offset - 10);
         if (this.particleSpawners.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("ParticleSpawners", this.particleSpawners.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.particleSpawners.size());

         for (Entry<String, ParticleSpawner> e : this.particleSpawners.entrySet()) {
            varOffset += PacketIO.writeVarString(mem, varOffset, e.getKey(), 16384000);
            varOffset += e.getValue().serialize(mem, varOffset);
         }
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 2), -1);
      }

      if (this.removedParticleSpawners != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 6), varOffset - offset - 10);
         if (this.removedParticleSpawners.length > 4096000) {
            throw ProtocolException.arrayTooLong("RemovedParticleSpawners", this.removedParticleSpawners.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.removedParticleSpawners.length);
         int removedParticleSpawnersValueOffset = 0;

         for (int i = 0; i < this.removedParticleSpawners.length; i++) {
            removedParticleSpawnersValueOffset += PacketIO.writeVarString(
               mem, varOffset + removedParticleSpawnersValueOffset, this.removedParticleSpawners[i], 16384000
            );
         }

         varOffset += removedParticleSpawnersValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 6), -1);
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 10;
      if (this.particleSpawners != null) {
         int particleSpawnersSize = 0;

         for (Entry<String, ParticleSpawner> kvp : this.particleSpawners.entrySet()) {
            particleSpawnersSize += PacketIO.stringSize(kvp.getKey()) + kvp.getValue().computeSize();
         }

         size += VarInt.size(this.particleSpawners.size()) + particleSpawnersSize;
      }

      if (this.removedParticleSpawners != null) {
         int removedParticleSpawnersSize = 0;

         for (String elem : this.removedParticleSpawners) {
            removedParticleSpawnersSize += PacketIO.stringSize(elem);
         }

         size += VarInt.size(this.removedParticleSpawners.length) + removedParticleSpawnersSize;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 10) {
         return ValidationResult.error("Buffer too small: expected at least 10 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int v = buffer.getByte(offset + 1) & 255;
         if (v >= 3) {
            return ValidationResult.error("Invalid UpdateType value for Type");
         } else {
            if ((nullBits & 1) != 0) {
               v = buffer.getIntLE(offset + 2);
               if (v < 0 || v > buffer.writerIndex() - offset - 10) {
                  return ValidationResult.error("Invalid offset for ParticleSpawners");
               }

               int pos = offset + 10 + v;
               int particleSpawnersCount = VarInt.peek(buffer, pos);
               if (particleSpawnersCount < 0) {
                  return ValidationResult.error("Invalid dictionary count for ParticleSpawners");
               }

               if (particleSpawnersCount > 4096000) {
                  return ValidationResult.error("ParticleSpawners exceeds max length 4096000");
               }

               pos += VarInt.size(particleSpawnersCount);

               for (int i = 0; i < particleSpawnersCount; i++) {
                  int keyLen = VarInt.peek(buffer, pos);
                  if (keyLen < 0) {
                     return ValidationResult.error("Invalid string length for key");
                  }

                  if (keyLen > 4096000) {
                     return ValidationResult.error("key exceeds max length 4096000");
                  }

                  pos += VarInt.size(keyLen);
                  pos += keyLen;
                  if (pos > buffer.writerIndex()) {
                     return ValidationResult.error("Buffer overflow reading key");
                  }

                  pos += ParticleSpawner.computeBytesConsumed(buffer, pos);
               }
            }

            if ((nullBits & 2) != 0) {
               v = buffer.getIntLE(offset + 6);
               if (v < 0 || v > buffer.writerIndex() - offset - 10) {
                  return ValidationResult.error("Invalid offset for RemovedParticleSpawners");
               }

               int posx = offset + 10 + v;
               int removedParticleSpawnersCount = VarInt.peek(buffer, posx);
               if (removedParticleSpawnersCount < 0) {
                  return ValidationResult.error("Invalid array count for RemovedParticleSpawners");
               }

               if (removedParticleSpawnersCount > 4096000) {
                  return ValidationResult.error("RemovedParticleSpawners exceeds max length 4096000");
               }

               posx += VarInt.size(removedParticleSpawnersCount);

               for (int i = 0; i < removedParticleSpawnersCount; i++) {
                  int strLen = VarInt.peek(buffer, posx);
                  if (strLen < 0) {
                     return ValidationResult.error("Invalid string length in RemovedParticleSpawners");
                  }

                  posx += VarInt.size(strLen);
                  posx += strLen;
                  if (posx > buffer.writerIndex()) {
                     return ValidationResult.error("Buffer overflow reading string in RemovedParticleSpawners");
                  }
               }
            }

            return ValidationResult.OK;
         }
      }
   }

   public UpdateParticleSpawners clone() {
      UpdateParticleSpawners copy = new UpdateParticleSpawners();
      copy.type = this.type;
      if (this.particleSpawners != null) {
         Map<String, ParticleSpawner> m = new HashMap<>();

         for (Entry<String, ParticleSpawner> e : this.particleSpawners.entrySet()) {
            m.put(e.getKey(), e.getValue().clone());
         }

         copy.particleSpawners = m;
      }

      copy.removedParticleSpawners = this.removedParticleSpawners != null
         ? Arrays.copyOf(this.removedParticleSpawners, this.removedParticleSpawners.length)
         : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof UpdateParticleSpawners other)
            ? false
            : Objects.equals(this.type, other.type)
               && Objects.equals(this.particleSpawners, other.particleSpawners)
               && Arrays.equals((Object[])this.removedParticleSpawners, (Object[])other.removedParticleSpawners);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.type);
      result = 31 * result + Objects.hashCode(this.particleSpawners);
      return 31 * result + Arrays.hashCode((Object[])this.removedParticleSpawners);
   }
}
