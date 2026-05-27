package com.hypixel.hytale.protocol.packets.assets;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ProjectileConfig;
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

public class UpdateProjectileConfigs implements Packet, ToClientPacket {
   public static final int PACKET_ID = 85;
   public static final boolean IS_COMPRESSED = true;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 2;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 10;
   public static final int MAX_SIZE = 1677721600;
   @Nonnull
   public UpdateType type = UpdateType.Init;
   @Nullable
   public Map<String, ProjectileConfig> configs;
   @Nullable
   public String[] removedConfigs;

   @Override
   public int getId() {
      return 85;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public UpdateProjectileConfigs() {
   }

   public UpdateProjectileConfigs(@Nonnull UpdateType type, @Nullable Map<String, ProjectileConfig> configs, @Nullable String[] removedConfigs) {
      this.type = type;
      this.configs = configs;
      this.removedConfigs = removedConfigs;
   }

   public UpdateProjectileConfigs(@Nonnull UpdateProjectileConfigs other) {
      this.type = other.type;
      this.configs = other.configs;
      this.removedConfigs = other.removedConfigs;
   }

   @Nonnull
   public static UpdateProjectileConfigs deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 10) {
         throw ProtocolException.bufferTooSmall("UpdateProjectileConfigs", 10, buf.readableBytes() - offset);
      } else {
         UpdateProjectileConfigs obj = new UpdateProjectileConfigs();
         byte nullBits = buf.getByte(offset);
         obj.type = UpdateType.fromValue(buf.getByte(offset + 1));
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 2);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 10) {
               throw ProtocolException.invalidOffset("Configs", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 10 + varPosBase0;
            int configsCount = VarInt.peek(buf, varPos0);
            if (configsCount < 0) {
               throw ProtocolException.invalidVarInt("Configs");
            }

            int varIntLen = VarInt.size(configsCount);
            if (configsCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("Configs", configsCount, 4096000);
            }

            obj.configs = new HashMap<>(configsCount);
            int dictPos = varPos0 + varIntLen;

            for (int i = 0; i < configsCount; i++) {
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
               ProjectileConfig val = ProjectileConfig.deserialize(buf, dictPos);
               dictPos += ProjectileConfig.computeBytesConsumed(buf, dictPos);
               if (obj.configs.put(key, val) != null) {
                  throw ProtocolException.duplicateKey("configs", key);
               }
            }
         }

         if ((nullBits & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 6);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 10) {
               throw ProtocolException.invalidOffset("RemovedConfigs", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 10 + varPosBase1;
            int removedConfigsCount = VarInt.peek(buf, varPos1);
            if (removedConfigsCount < 0) {
               throw ProtocolException.invalidVarInt("RemovedConfigs");
            }

            int varIntLen = VarInt.size(removedConfigsCount);
            if (removedConfigsCount > 4096000) {
               throw ProtocolException.arrayTooLong("RemovedConfigs", removedConfigsCount, 4096000);
            }

            if (varPos1 + varIntLen + removedConfigsCount * 1L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("RemovedConfigs", varPos1 + varIntLen + removedConfigsCount * 1, buf.readableBytes());
            }

            obj.removedConfigs = new String[removedConfigsCount];
            int elemPos = varPos1 + varIntLen;

            for (int i = 0; i < removedConfigsCount; i++) {
               int strLen = VarInt.peek(buf, elemPos);
               if (strLen < 0) {
                  throw ProtocolException.invalidVarInt("removedConfigs[" + i + "]");
               }

               int strVarLen = VarInt.size(strLen);
               if (strLen > 4096000) {
                  throw ProtocolException.stringTooLong("removedConfigs[" + i + "]", strLen, 4096000);
               }

               if (elemPos + strVarLen + strLen > buf.readableBytes()) {
                  throw ProtocolException.bufferTooSmall("removedConfigs[" + i + "]", elemPos + strVarLen + strLen, buf.readableBytes());
               }

               obj.removedConfigs[i] = PacketIO.readVarString(buf, elemPos);
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
            throw ProtocolException.invalidOffset("Configs", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 10 + fieldOffset0;
         int dictLen = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(dictLen);

         for (int i = 0; i < dictLen; i++) {
            int sl = VarInt.peek(buf, pos0);
            pos0 += VarInt.size(sl) + sl;
            pos0 += ProjectileConfig.computeBytesConsumed(buf, pos0);
         }

         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 6);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 10) {
            throw ProtocolException.invalidOffset("RemovedConfigs", fieldOffset1, maxEnd);
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
   public static Map<String, ProjectileConfig> getConfigs(MemorySegment mem) {
      return getConfigs(mem, 0);
   }

   @Nullable
   public static Map<String, ProjectileConfig> getConfigs(MemorySegment mem, int offset) {
      if (!hasConfigs(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 2, 10, "Configs");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Configs", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Configs", len, 4096000);
         } else {
            Map<String, ProjectileConfig> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               long keyPacked = VarInt.getWithLength(mem, off);
               int nkey = (int)keyPacked + (int)(keyPacked >>> 32);
               String key = PacketIO.readVarString("key", mem, off, 16384000, PacketIO.UTF8);
               off += nkey;
               ProjectileConfig value = ProjectileConfig.toObject(mem, off);
               off += value.computeSize();
               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("Configs", key);
               }
            }

            return data;
         }
      }
   }

   @Nullable
   public static String[] getRemovedConfigs(MemorySegment mem) {
      return getRemovedConfigs(mem, 0);
   }

   @Nullable
   public static String[] getRemovedConfigs(MemorySegment mem, int offset) {
      if (!hasRemovedConfigs(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 6, 10, "RemovedConfigs");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("RemovedConfigs", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("RemovedConfigs", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("RemovedConfigs", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               String[] data = new String[len];

               for (int i = 0; i < len; i++) {
                  long sp = VarInt.getWithLength(mem, off);
                  int n = (int)sp + (int)(sp >>> 32);
                  data[i] = PacketIO.readVarString("RemovedConfigs", mem, off, 16384000, PacketIO.UTF8);
                  off += n;
               }

               return data;
            }
         }
      }
   }

   public static boolean hasConfigs(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasRemovedConfigs(MemorySegment mem, int offset) {
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

   public static UpdateProjectileConfigs toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static UpdateProjectileConfigs toObject(MemorySegment mem, int offset) {
      if (offset + 10 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("UpdateProjectileConfigs", offset + 10, (int)mem.byteSize());
      } else {
         Map<String, ProjectileConfig> configs = null;
         if (hasConfigs(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 2, 10, "Configs");
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Configs", len);
            }

            if (len > 4096000) {
               throw ProtocolException.dictionaryTooLarge("Configs", len, 4096000);
            }

            configs = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               long keyPacked = VarInt.getWithLength(mem, off);
               int nkey = (int)keyPacked + (int)(keyPacked >>> 32);
               String key = PacketIO.readVarString("key", mem, off, 16384000, PacketIO.UTF8);
               off += nkey;
               ProjectileConfig value = ProjectileConfig.toObject(mem, off);
               off += value.computeSize();
               if (configs.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("Configs", key);
               }
            }
         }

         String[] removedConfigs = null;
         if (hasRemovedConfigs(mem, offset)) {
            int offx = offset + getValidatedOffset(mem, offset, 6, 10, "RemovedConfigs");
            long packedx = VarInt.getWithLength(mem, offx);
            int lenx = (int)packedx;
            if (lenx < 0) {
               throw ProtocolException.negativeLength("RemovedConfigs", lenx);
            }

            if (lenx > 4096000) {
               throw ProtocolException.arrayTooLong("RemovedConfigs", lenx, 4096000);
            }

            int lenOffset = (int)(packedx >>> 32);
            if (offx + lenOffset + lenx > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("RemovedConfigs", offx + lenOffset + lenx, (int)mem.byteSize());
            }

            offx += lenOffset;
            removedConfigs = new String[lenx];

            for (int ix = 0; ix < lenx; ix++) {
               long sp = VarInt.getWithLength(mem, offx);
               int n = (int)sp + (int)(sp >>> 32);
               removedConfigs[ix] = PacketIO.readVarString("RemovedConfigs", mem, offx, 16384000, PacketIO.UTF8);
               offx += n;
            }
         }

         return new UpdateProjectileConfigs(UpdateType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1))), configs, removedConfigs);
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.configs != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.removedConfigs != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.type.getValue());
      int configsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int removedConfigsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.configs != null) {
         buf.setIntLE(configsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.configs.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Configs", this.configs.size(), 4096000);
         }

         VarInt.write(buf, this.configs.size());

         for (Entry<String, ProjectileConfig> e : this.configs.entrySet()) {
            PacketIO.writeVarString(buf, e.getKey(), 4096000);
            e.getValue().serialize(buf);
         }
      } else {
         buf.setIntLE(configsOffsetSlot, -1);
      }

      if (this.removedConfigs != null) {
         buf.setIntLE(removedConfigsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.removedConfigs.length > 4096000) {
            throw ProtocolException.arrayTooLong("RemovedConfigs", this.removedConfigs.length, 4096000);
         }

         VarInt.write(buf, this.removedConfigs.length);

         for (String item : this.removedConfigs) {
            PacketIO.writeVarString(buf, item, 4096000);
         }
      } else {
         buf.setIntLE(removedConfigsOffsetSlot, -1);
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.configs != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.removedConfigs != null) {
         nullBits = (byte)(nullBits | 2);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 1), (byte)this.type.getValue());
      int varOffset = offset + 10;
      if (this.configs != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 2), varOffset - offset - 10);
         if (this.configs.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Configs", this.configs.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.configs.size());

         for (Entry<String, ProjectileConfig> e : this.configs.entrySet()) {
            varOffset += PacketIO.writeVarString(mem, varOffset, e.getKey(), 16384000);
            varOffset += e.getValue().serialize(mem, varOffset);
         }
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 2), -1);
      }

      if (this.removedConfigs != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 6), varOffset - offset - 10);
         if (this.removedConfigs.length > 4096000) {
            throw ProtocolException.arrayTooLong("RemovedConfigs", this.removedConfigs.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.removedConfigs.length);
         int removedConfigsValueOffset = 0;

         for (int i = 0; i < this.removedConfigs.length; i++) {
            removedConfigsValueOffset += PacketIO.writeVarString(mem, varOffset + removedConfigsValueOffset, this.removedConfigs[i], 16384000);
         }

         varOffset += removedConfigsValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 6), -1);
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 10;
      if (this.configs != null) {
         int configsSize = 0;

         for (Entry<String, ProjectileConfig> kvp : this.configs.entrySet()) {
            configsSize += PacketIO.stringSize(kvp.getKey()) + kvp.getValue().computeSize();
         }

         size += VarInt.size(this.configs.size()) + configsSize;
      }

      if (this.removedConfigs != null) {
         int removedConfigsSize = 0;

         for (String elem : this.removedConfigs) {
            removedConfigsSize += PacketIO.stringSize(elem);
         }

         size += VarInt.size(this.removedConfigs.length) + removedConfigsSize;
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
                  return ValidationResult.error("Invalid offset for Configs");
               }

               int pos = offset + 10 + v;
               int configsCount = VarInt.peek(buffer, pos);
               if (configsCount < 0) {
                  return ValidationResult.error("Invalid dictionary count for Configs");
               }

               if (configsCount > 4096000) {
                  return ValidationResult.error("Configs exceeds max length 4096000");
               }

               pos += VarInt.size(configsCount);

               for (int i = 0; i < configsCount; i++) {
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

                  pos += ProjectileConfig.computeBytesConsumed(buffer, pos);
               }
            }

            if ((nullBits & 2) != 0) {
               v = buffer.getIntLE(offset + 6);
               if (v < 0 || v > buffer.writerIndex() - offset - 10) {
                  return ValidationResult.error("Invalid offset for RemovedConfigs");
               }

               int posx = offset + 10 + v;
               int removedConfigsCount = VarInt.peek(buffer, posx);
               if (removedConfigsCount < 0) {
                  return ValidationResult.error("Invalid array count for RemovedConfigs");
               }

               if (removedConfigsCount > 4096000) {
                  return ValidationResult.error("RemovedConfigs exceeds max length 4096000");
               }

               posx += VarInt.size(removedConfigsCount);

               for (int i = 0; i < removedConfigsCount; i++) {
                  int strLen = VarInt.peek(buffer, posx);
                  if (strLen < 0) {
                     return ValidationResult.error("Invalid string length in RemovedConfigs");
                  }

                  posx += VarInt.size(strLen);
                  posx += strLen;
                  if (posx > buffer.writerIndex()) {
                     return ValidationResult.error("Buffer overflow reading string in RemovedConfigs");
                  }
               }
            }

            return ValidationResult.OK;
         }
      }
   }

   public UpdateProjectileConfigs clone() {
      UpdateProjectileConfigs copy = new UpdateProjectileConfigs();
      copy.type = this.type;
      if (this.configs != null) {
         Map<String, ProjectileConfig> m = new HashMap<>();

         for (Entry<String, ProjectileConfig> e : this.configs.entrySet()) {
            m.put(e.getKey(), e.getValue().clone());
         }

         copy.configs = m;
      }

      copy.removedConfigs = this.removedConfigs != null ? Arrays.copyOf(this.removedConfigs, this.removedConfigs.length) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof UpdateProjectileConfigs other)
            ? false
            : Objects.equals(this.type, other.type)
               && Objects.equals(this.configs, other.configs)
               && Arrays.equals((Object[])this.removedConfigs, (Object[])other.removedConfigs);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.type);
      result = 31 * result + Objects.hashCode(this.configs);
      return 31 * result + Arrays.hashCode((Object[])this.removedConfigs);
   }
}
