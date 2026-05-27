package com.hypixel.hytale.protocol.packets.assets;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ParticleSystem;
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

public class UpdateParticleSystems implements Packet, ToClientPacket {
   public static final int PACKET_ID = 49;
   public static final boolean IS_COMPRESSED = true;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 2;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 10;
   public static final int MAX_SIZE = 1677721600;
   @Nonnull
   public UpdateType type = UpdateType.Init;
   @Nullable
   public Map<String, ParticleSystem> particleSystems;
   @Nullable
   public String[] removedParticleSystems;

   @Override
   public int getId() {
      return 49;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public UpdateParticleSystems() {
   }

   public UpdateParticleSystems(@Nonnull UpdateType type, @Nullable Map<String, ParticleSystem> particleSystems, @Nullable String[] removedParticleSystems) {
      this.type = type;
      this.particleSystems = particleSystems;
      this.removedParticleSystems = removedParticleSystems;
   }

   public UpdateParticleSystems(@Nonnull UpdateParticleSystems other) {
      this.type = other.type;
      this.particleSystems = other.particleSystems;
      this.removedParticleSystems = other.removedParticleSystems;
   }

   @Nonnull
   public static UpdateParticleSystems deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 10) {
         throw ProtocolException.bufferTooSmall("UpdateParticleSystems", 10, buf.readableBytes() - offset);
      } else {
         UpdateParticleSystems obj = new UpdateParticleSystems();
         byte nullBits = buf.getByte(offset);
         obj.type = UpdateType.fromValue(buf.getByte(offset + 1));
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 2);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 10) {
               throw ProtocolException.invalidOffset("ParticleSystems", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 10 + varPosBase0;
            int particleSystemsCount = VarInt.peek(buf, varPos0);
            if (particleSystemsCount < 0) {
               throw ProtocolException.invalidVarInt("ParticleSystems");
            }

            int varIntLen = VarInt.size(particleSystemsCount);
            if (particleSystemsCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("ParticleSystems", particleSystemsCount, 4096000);
            }

            obj.particleSystems = new HashMap<>(particleSystemsCount);
            int dictPos = varPos0 + varIntLen;

            for (int i = 0; i < particleSystemsCount; i++) {
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
               ParticleSystem val = ParticleSystem.deserialize(buf, dictPos);
               dictPos += ParticleSystem.computeBytesConsumed(buf, dictPos);
               if (obj.particleSystems.put(key, val) != null) {
                  throw ProtocolException.duplicateKey("particleSystems", key);
               }
            }
         }

         if ((nullBits & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 6);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 10) {
               throw ProtocolException.invalidOffset("RemovedParticleSystems", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 10 + varPosBase1;
            int removedParticleSystemsCount = VarInt.peek(buf, varPos1);
            if (removedParticleSystemsCount < 0) {
               throw ProtocolException.invalidVarInt("RemovedParticleSystems");
            }

            int varIntLen = VarInt.size(removedParticleSystemsCount);
            if (removedParticleSystemsCount > 4096000) {
               throw ProtocolException.arrayTooLong("RemovedParticleSystems", removedParticleSystemsCount, 4096000);
            }

            if (varPos1 + varIntLen + removedParticleSystemsCount * 1L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("RemovedParticleSystems", varPos1 + varIntLen + removedParticleSystemsCount * 1, buf.readableBytes());
            }

            obj.removedParticleSystems = new String[removedParticleSystemsCount];
            int elemPos = varPos1 + varIntLen;

            for (int i = 0; i < removedParticleSystemsCount; i++) {
               int strLen = VarInt.peek(buf, elemPos);
               if (strLen < 0) {
                  throw ProtocolException.invalidVarInt("removedParticleSystems[" + i + "]");
               }

               int strVarLen = VarInt.size(strLen);
               if (strLen > 4096000) {
                  throw ProtocolException.stringTooLong("removedParticleSystems[" + i + "]", strLen, 4096000);
               }

               if (elemPos + strVarLen + strLen > buf.readableBytes()) {
                  throw ProtocolException.bufferTooSmall("removedParticleSystems[" + i + "]", elemPos + strVarLen + strLen, buf.readableBytes());
               }

               obj.removedParticleSystems[i] = PacketIO.readVarString(buf, elemPos);
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
            throw ProtocolException.invalidOffset("ParticleSystems", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 10 + fieldOffset0;
         int dictLen = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(dictLen);

         for (int i = 0; i < dictLen; i++) {
            int sl = VarInt.peek(buf, pos0);
            pos0 += VarInt.size(sl) + sl;
            pos0 += ParticleSystem.computeBytesConsumed(buf, pos0);
         }

         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 6);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 10) {
            throw ProtocolException.invalidOffset("RemovedParticleSystems", fieldOffset1, maxEnd);
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
   public static Map<String, ParticleSystem> getParticleSystems(MemorySegment mem) {
      return getParticleSystems(mem, 0);
   }

   @Nullable
   public static Map<String, ParticleSystem> getParticleSystems(MemorySegment mem, int offset) {
      if (!hasParticleSystems(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 2, 10, "ParticleSystems");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("ParticleSystems", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("ParticleSystems", len, 4096000);
         } else {
            Map<String, ParticleSystem> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               long keyPacked = VarInt.getWithLength(mem, off);
               int nkey = (int)keyPacked + (int)(keyPacked >>> 32);
               String key = PacketIO.readVarString("key", mem, off, 16384000, PacketIO.UTF8);
               off += nkey;
               ParticleSystem value = ParticleSystem.toObject(mem, off);
               off += value.computeSize();
               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("ParticleSystems", key);
               }
            }

            return data;
         }
      }
   }

   @Nullable
   public static String[] getRemovedParticleSystems(MemorySegment mem) {
      return getRemovedParticleSystems(mem, 0);
   }

   @Nullable
   public static String[] getRemovedParticleSystems(MemorySegment mem, int offset) {
      if (!hasRemovedParticleSystems(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 6, 10, "RemovedParticleSystems");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("RemovedParticleSystems", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("RemovedParticleSystems", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("RemovedParticleSystems", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               String[] data = new String[len];

               for (int i = 0; i < len; i++) {
                  long sp = VarInt.getWithLength(mem, off);
                  int n = (int)sp + (int)(sp >>> 32);
                  data[i] = PacketIO.readVarString("RemovedParticleSystems", mem, off, 16384000, PacketIO.UTF8);
                  off += n;
               }

               return data;
            }
         }
      }
   }

   public static boolean hasParticleSystems(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasRemovedParticleSystems(MemorySegment mem, int offset) {
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

   public static UpdateParticleSystems toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static UpdateParticleSystems toObject(MemorySegment mem, int offset) {
      if (offset + 10 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("UpdateParticleSystems", offset + 10, (int)mem.byteSize());
      } else {
         Map<String, ParticleSystem> particleSystems = null;
         if (hasParticleSystems(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 2, 10, "ParticleSystems");
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("ParticleSystems", len);
            }

            if (len > 4096000) {
               throw ProtocolException.dictionaryTooLarge("ParticleSystems", len, 4096000);
            }

            particleSystems = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               long keyPacked = VarInt.getWithLength(mem, off);
               int nkey = (int)keyPacked + (int)(keyPacked >>> 32);
               String key = PacketIO.readVarString("key", mem, off, 16384000, PacketIO.UTF8);
               off += nkey;
               ParticleSystem value = ParticleSystem.toObject(mem, off);
               off += value.computeSize();
               if (particleSystems.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("ParticleSystems", key);
               }
            }
         }

         String[] removedParticleSystems = null;
         if (hasRemovedParticleSystems(mem, offset)) {
            int offx = offset + getValidatedOffset(mem, offset, 6, 10, "RemovedParticleSystems");
            long packedx = VarInt.getWithLength(mem, offx);
            int lenx = (int)packedx;
            if (lenx < 0) {
               throw ProtocolException.negativeLength("RemovedParticleSystems", lenx);
            }

            if (lenx > 4096000) {
               throw ProtocolException.arrayTooLong("RemovedParticleSystems", lenx, 4096000);
            }

            int lenOffset = (int)(packedx >>> 32);
            if (offx + lenOffset + lenx > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("RemovedParticleSystems", offx + lenOffset + lenx, (int)mem.byteSize());
            }

            offx += lenOffset;
            removedParticleSystems = new String[lenx];

            for (int ix = 0; ix < lenx; ix++) {
               long sp = VarInt.getWithLength(mem, offx);
               int n = (int)sp + (int)(sp >>> 32);
               removedParticleSystems[ix] = PacketIO.readVarString("RemovedParticleSystems", mem, offx, 16384000, PacketIO.UTF8);
               offx += n;
            }
         }

         return new UpdateParticleSystems(UpdateType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1))), particleSystems, removedParticleSystems);
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.particleSystems != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.removedParticleSystems != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.type.getValue());
      int particleSystemsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int removedParticleSystemsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.particleSystems != null) {
         buf.setIntLE(particleSystemsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.particleSystems.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("ParticleSystems", this.particleSystems.size(), 4096000);
         }

         VarInt.write(buf, this.particleSystems.size());

         for (Entry<String, ParticleSystem> e : this.particleSystems.entrySet()) {
            PacketIO.writeVarString(buf, e.getKey(), 4096000);
            e.getValue().serialize(buf);
         }
      } else {
         buf.setIntLE(particleSystemsOffsetSlot, -1);
      }

      if (this.removedParticleSystems != null) {
         buf.setIntLE(removedParticleSystemsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.removedParticleSystems.length > 4096000) {
            throw ProtocolException.arrayTooLong("RemovedParticleSystems", this.removedParticleSystems.length, 4096000);
         }

         VarInt.write(buf, this.removedParticleSystems.length);

         for (String item : this.removedParticleSystems) {
            PacketIO.writeVarString(buf, item, 4096000);
         }
      } else {
         buf.setIntLE(removedParticleSystemsOffsetSlot, -1);
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.particleSystems != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.removedParticleSystems != null) {
         nullBits = (byte)(nullBits | 2);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 1), (byte)this.type.getValue());
      int varOffset = offset + 10;
      if (this.particleSystems != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 2), varOffset - offset - 10);
         if (this.particleSystems.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("ParticleSystems", this.particleSystems.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.particleSystems.size());

         for (Entry<String, ParticleSystem> e : this.particleSystems.entrySet()) {
            varOffset += PacketIO.writeVarString(mem, varOffset, e.getKey(), 16384000);
            varOffset += e.getValue().serialize(mem, varOffset);
         }
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 2), -1);
      }

      if (this.removedParticleSystems != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 6), varOffset - offset - 10);
         if (this.removedParticleSystems.length > 4096000) {
            throw ProtocolException.arrayTooLong("RemovedParticleSystems", this.removedParticleSystems.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.removedParticleSystems.length);
         int removedParticleSystemsValueOffset = 0;

         for (int i = 0; i < this.removedParticleSystems.length; i++) {
            removedParticleSystemsValueOffset += PacketIO.writeVarString(
               mem, varOffset + removedParticleSystemsValueOffset, this.removedParticleSystems[i], 16384000
            );
         }

         varOffset += removedParticleSystemsValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 6), -1);
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 10;
      if (this.particleSystems != null) {
         int particleSystemsSize = 0;

         for (Entry<String, ParticleSystem> kvp : this.particleSystems.entrySet()) {
            particleSystemsSize += PacketIO.stringSize(kvp.getKey()) + kvp.getValue().computeSize();
         }

         size += VarInt.size(this.particleSystems.size()) + particleSystemsSize;
      }

      if (this.removedParticleSystems != null) {
         int removedParticleSystemsSize = 0;

         for (String elem : this.removedParticleSystems) {
            removedParticleSystemsSize += PacketIO.stringSize(elem);
         }

         size += VarInt.size(this.removedParticleSystems.length) + removedParticleSystemsSize;
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
                  return ValidationResult.error("Invalid offset for ParticleSystems");
               }

               int pos = offset + 10 + v;
               int particleSystemsCount = VarInt.peek(buffer, pos);
               if (particleSystemsCount < 0) {
                  return ValidationResult.error("Invalid dictionary count for ParticleSystems");
               }

               if (particleSystemsCount > 4096000) {
                  return ValidationResult.error("ParticleSystems exceeds max length 4096000");
               }

               pos += VarInt.size(particleSystemsCount);

               for (int i = 0; i < particleSystemsCount; i++) {
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

                  pos += ParticleSystem.computeBytesConsumed(buffer, pos);
               }
            }

            if ((nullBits & 2) != 0) {
               v = buffer.getIntLE(offset + 6);
               if (v < 0 || v > buffer.writerIndex() - offset - 10) {
                  return ValidationResult.error("Invalid offset for RemovedParticleSystems");
               }

               int posx = offset + 10 + v;
               int removedParticleSystemsCount = VarInt.peek(buffer, posx);
               if (removedParticleSystemsCount < 0) {
                  return ValidationResult.error("Invalid array count for RemovedParticleSystems");
               }

               if (removedParticleSystemsCount > 4096000) {
                  return ValidationResult.error("RemovedParticleSystems exceeds max length 4096000");
               }

               posx += VarInt.size(removedParticleSystemsCount);

               for (int i = 0; i < removedParticleSystemsCount; i++) {
                  int strLen = VarInt.peek(buffer, posx);
                  if (strLen < 0) {
                     return ValidationResult.error("Invalid string length in RemovedParticleSystems");
                  }

                  posx += VarInt.size(strLen);
                  posx += strLen;
                  if (posx > buffer.writerIndex()) {
                     return ValidationResult.error("Buffer overflow reading string in RemovedParticleSystems");
                  }
               }
            }

            return ValidationResult.OK;
         }
      }
   }

   public UpdateParticleSystems clone() {
      UpdateParticleSystems copy = new UpdateParticleSystems();
      copy.type = this.type;
      if (this.particleSystems != null) {
         Map<String, ParticleSystem> m = new HashMap<>();

         for (Entry<String, ParticleSystem> e : this.particleSystems.entrySet()) {
            m.put(e.getKey(), e.getValue().clone());
         }

         copy.particleSystems = m;
      }

      copy.removedParticleSystems = this.removedParticleSystems != null ? Arrays.copyOf(this.removedParticleSystems, this.removedParticleSystems.length) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof UpdateParticleSystems other)
            ? false
            : Objects.equals(this.type, other.type)
               && Objects.equals(this.particleSystems, other.particleSystems)
               && Arrays.equals((Object[])this.removedParticleSystems, (Object[])other.removedParticleSystems);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.type);
      result = 31 * result + Objects.hashCode(this.particleSystems);
      return 31 * result + Arrays.hashCode((Object[])this.removedParticleSystems);
   }
}
