package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SoundSet {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 2;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 10;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public String id;
   @Nullable
   public Map<String, Integer> sounds;
   @Nonnull
   public SoundCategory category = SoundCategory.Music;

   public SoundSet() {
   }

   public SoundSet(@Nullable String id, @Nullable Map<String, Integer> sounds, @Nonnull SoundCategory category) {
      this.id = id;
      this.sounds = sounds;
      this.category = category;
   }

   public SoundSet(@Nonnull SoundSet other) {
      this.id = other.id;
      this.sounds = other.sounds;
      this.category = other.category;
   }

   @Nonnull
   public static SoundSet deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 10) {
         throw ProtocolException.bufferTooSmall("SoundSet", 10, buf.readableBytes() - offset);
      } else {
         SoundSet obj = new SoundSet();
         byte nullBits = buf.getByte(offset);
         obj.category = SoundCategory.fromValue(buf.getByte(offset + 1));
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 2);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 10) {
               throw ProtocolException.invalidOffset("Id", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 10 + varPosBase0;
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
            int varPosBase1 = buf.getIntLE(offset + 6);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 10) {
               throw ProtocolException.invalidOffset("Sounds", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 10 + varPosBase1;
            int soundsCount = VarInt.peek(buf, varPos1);
            if (soundsCount < 0) {
               throw ProtocolException.invalidVarInt("Sounds");
            }

            int varIntLen = VarInt.size(soundsCount);
            if (soundsCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("Sounds", soundsCount, 4096000);
            }

            obj.sounds = new HashMap<>(soundsCount);
            int dictPos = varPos1 + varIntLen;

            for (int i = 0; i < soundsCount; i++) {
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
               int val = buf.getIntLE(dictPos);
               dictPos += 4;
               if (obj.sounds.put(key, val) != null) {
                  throw ProtocolException.duplicateKey("sounds", key);
               }
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
            throw ProtocolException.invalidOffset("Id", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 10 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 6);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 10) {
            throw ProtocolException.invalidOffset("Sounds", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 10 + fieldOffset1;
         int dictLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(dictLen);

         for (int i = 0; i < dictLen; i++) {
            int sl = VarInt.peek(buf, pos1);
            pos1 += VarInt.size(sl) + sl;
            pos1 += 4;
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

   @Nullable
   public static String getId(MemorySegment mem) {
      return getId(mem, 0);
   }

   @Nullable
   public static String getId(MemorySegment mem, int offset) {
      return hasId(mem, offset) ? PacketIO.readVarString("Id", mem, offset + getValidatedOffset(mem, offset, 2, 10, "Id"), 4096000, PacketIO.UTF8) : null;
   }

   @Nullable
   public static Map<String, Integer> getSounds(MemorySegment mem) {
      return getSounds(mem, 0);
   }

   @Nullable
   public static Map<String, Integer> getSounds(MemorySegment mem, int offset) {
      if (!hasSounds(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 6, 10, "Sounds");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Sounds", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Sounds", len, 4096000);
         } else {
            Map<String, Integer> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               long keyPacked = VarInt.getWithLength(mem, off);
               int nkey = (int)keyPacked + (int)(keyPacked >>> 32);
               String key = PacketIO.readVarString("key", mem, off, 16384000, PacketIO.UTF8);
               off += nkey;
               int value = mem.get(PacketIO.PROTO_INT, (long)off);
               off += 4;
               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("Sounds", key);
               }
            }

            return data;
         }
      }
   }

   public static SoundCategory getCategory(MemorySegment mem) {
      return getCategory(mem, 0);
   }

   public static SoundCategory getCategory(MemorySegment mem, int offset) {
      return SoundCategory.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1)));
   }

   public static boolean hasId(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasSounds(MemorySegment mem, int offset) {
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

   public static SoundSet toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static SoundSet toObject(MemorySegment mem, int offset) {
      if (offset + 10 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("SoundSet", offset + 10, (int)mem.byteSize());
      } else {
         Map<String, Integer> sounds = null;
         if (hasSounds(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 6, 10, "Sounds");
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Sounds", len);
            }

            if (len > 4096000) {
               throw ProtocolException.dictionaryTooLarge("Sounds", len, 4096000);
            }

            sounds = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               long keyPacked = VarInt.getWithLength(mem, off);
               int nkey = (int)keyPacked + (int)(keyPacked >>> 32);
               String key = PacketIO.readVarString("key", mem, off, 16384000, PacketIO.UTF8);
               off += nkey;
               int value = mem.get(PacketIO.PROTO_INT, (long)off);
               off += 4;
               if (sounds.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("Sounds", key);
               }
            }
         }

         return new SoundSet(
            hasId(mem, offset) ? PacketIO.readVarString("Id", mem, offset + getValidatedOffset(mem, offset, 2, 10, "Id"), 4096000, PacketIO.UTF8) : null,
            sounds,
            SoundCategory.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1)))
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.id != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.sounds != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.category.getValue());
      int idOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int soundsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.id != null) {
         buf.setIntLE(idOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.id, 4096000);
      } else {
         buf.setIntLE(idOffsetSlot, -1);
      }

      if (this.sounds != null) {
         buf.setIntLE(soundsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.sounds.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Sounds", this.sounds.size(), 4096000);
         }

         VarInt.write(buf, this.sounds.size());

         for (Entry<String, Integer> e : this.sounds.entrySet()) {
            PacketIO.writeVarString(buf, e.getKey(), 4096000);
            buf.writeIntLE(e.getValue());
         }
      } else {
         buf.setIntLE(soundsOffsetSlot, -1);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.id != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.sounds != null) {
         nullBits = (byte)(nullBits | 2);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 1), (byte)this.category.getValue());
      int varOffset = offset + 10;
      if (this.id != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 2), varOffset - offset - 10);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.id, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 2), -1);
      }

      if (this.sounds != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 6), varOffset - offset - 10);
         if (this.sounds.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("Sounds", this.sounds.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.sounds.size());

         for (Entry<String, Integer> e : this.sounds.entrySet()) {
            varOffset += PacketIO.writeVarString(mem, varOffset, e.getKey(), 16384000);
            mem.set(PacketIO.PROTO_INT, (long)varOffset, e.getValue());
            varOffset += 4;
         }
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 6), -1);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 10;
      if (this.id != null) {
         size += PacketIO.stringSize(this.id);
      }

      if (this.sounds != null) {
         int soundsSize = 0;

         for (Entry<String, Integer> kvp : this.sounds.entrySet()) {
            soundsSize += PacketIO.stringSize(kvp.getKey()) + 4;
         }

         size += VarInt.size(this.sounds.size()) + soundsSize;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 10) {
         return ValidationResult.error("Buffer too small: expected at least 10 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int v = buffer.getByte(offset + 1) & 255;
         if (v >= 5) {
            return ValidationResult.error("Invalid SoundCategory value for Category");
         } else {
            if ((nullBits & 1) != 0) {
               v = buffer.getIntLE(offset + 2);
               if (v < 0 || v > buffer.writerIndex() - offset - 10) {
                  return ValidationResult.error("Invalid offset for Id");
               }

               int pos = offset + 10 + v;
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
               v = buffer.getIntLE(offset + 6);
               if (v < 0 || v > buffer.writerIndex() - offset - 10) {
                  return ValidationResult.error("Invalid offset for Sounds");
               }

               int posx = offset + 10 + v;
               int soundsCount = VarInt.peek(buffer, posx);
               if (soundsCount < 0) {
                  return ValidationResult.error("Invalid dictionary count for Sounds");
               }

               if (soundsCount > 4096000) {
                  return ValidationResult.error("Sounds exceeds max length 4096000");
               }

               posx += VarInt.size(soundsCount);

               for (int i = 0; i < soundsCount; i++) {
                  int keyLen = VarInt.peek(buffer, posx);
                  if (keyLen < 0) {
                     return ValidationResult.error("Invalid string length for key");
                  }

                  if (keyLen > 4096000) {
                     return ValidationResult.error("key exceeds max length 4096000");
                  }

                  posx += VarInt.size(keyLen);
                  posx += keyLen;
                  if (posx > buffer.writerIndex()) {
                     return ValidationResult.error("Buffer overflow reading key");
                  }

                  posx += 4;
                  if (posx > buffer.writerIndex()) {
                     return ValidationResult.error("Buffer overflow reading value");
                  }
               }
            }

            return ValidationResult.OK;
         }
      }
   }

   public SoundSet clone() {
      SoundSet copy = new SoundSet();
      copy.id = this.id;
      copy.sounds = this.sounds != null ? new HashMap<>(this.sounds) : null;
      copy.category = this.category;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof SoundSet other)
            ? false
            : Objects.equals(this.id, other.id) && Objects.equals(this.sounds, other.sounds) && Objects.equals(this.category, other.category);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.id, this.sounds, this.category);
   }
}
