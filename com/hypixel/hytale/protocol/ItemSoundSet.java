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

public class ItemSoundSet {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 9;
   public static final int MAX_SIZE = 36864019;
   @Nullable
   public String id;
   @Nullable
   public Map<ItemSoundEvent, Integer> soundEventIndices;

   public ItemSoundSet() {
   }

   public ItemSoundSet(@Nullable String id, @Nullable Map<ItemSoundEvent, Integer> soundEventIndices) {
      this.id = id;
      this.soundEventIndices = soundEventIndices;
   }

   public ItemSoundSet(@Nonnull ItemSoundSet other) {
      this.id = other.id;
      this.soundEventIndices = other.soundEventIndices;
   }

   @Nonnull
   public static ItemSoundSet deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 9) {
         throw ProtocolException.bufferTooSmall("ItemSoundSet", 9, buf.readableBytes() - offset);
      } else {
         ItemSoundSet obj = new ItemSoundSet();
         byte nullBits = buf.getByte(offset);
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 1);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 9) {
               throw ProtocolException.invalidOffset("Id", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 9 + varPosBase0;
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
            int varPosBase1 = buf.getIntLE(offset + 5);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 9) {
               throw ProtocolException.invalidOffset("SoundEventIndices", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 9 + varPosBase1;
            int soundEventIndicesCount = VarInt.peek(buf, varPos1);
            if (soundEventIndicesCount < 0) {
               throw ProtocolException.invalidVarInt("SoundEventIndices");
            }

            int varIntLen = VarInt.size(soundEventIndicesCount);
            if (soundEventIndicesCount > 4096000) {
               throw ProtocolException.dictionaryTooLarge("SoundEventIndices", soundEventIndicesCount, 4096000);
            }

            obj.soundEventIndices = new HashMap<>(soundEventIndicesCount);
            int dictPos = varPos1 + varIntLen;

            for (int i = 0; i < soundEventIndicesCount; i++) {
               ItemSoundEvent key = ItemSoundEvent.fromValue(buf.getByte(dictPos));
               int val = buf.getIntLE(++dictPos);
               dictPos += 4;
               if (obj.soundEventIndices.put(key, val) != null) {
                  throw ProtocolException.duplicateKey("soundEventIndices", key);
               }
            }
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 9;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 1);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 9) {
            throw ProtocolException.invalidOffset("Id", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 9 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 5);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 9) {
            throw ProtocolException.invalidOffset("SoundEventIndices", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 9 + fieldOffset1;
         int dictLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(dictLen);

         for (int i = 0; i < dictLen; i++) {
            pos1 = ++pos1 + 4;
         }

         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 9L;
   }

   @Nullable
   public static String getId(MemorySegment mem) {
      return getId(mem, 0);
   }

   @Nullable
   public static String getId(MemorySegment mem, int offset) {
      return hasId(mem, offset) ? PacketIO.readVarString("Id", mem, offset + getValidatedOffset(mem, offset, 1, 9, "Id"), 4096000, PacketIO.UTF8) : null;
   }

   @Nullable
   public static Map<ItemSoundEvent, Integer> getSoundEventIndices(MemorySegment mem) {
      return getSoundEventIndices(mem, 0);
   }

   @Nullable
   public static Map<ItemSoundEvent, Integer> getSoundEventIndices(MemorySegment mem, int offset) {
      if (!hasSoundEventIndices(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 5, 9, "SoundEventIndices");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("SoundEventIndices", len);
         } else if (len > 4096000) {
            throw ProtocolException.dictionaryTooLarge("SoundEventIndices", len, 4096000);
         } else {
            Map<ItemSoundEvent, Integer> data = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               ItemSoundEvent key = ItemSoundEvent.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)off));
               int value = mem.get(PacketIO.PROTO_INT, (long)(++off));
               off += 4;
               if (data.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("SoundEventIndices", key);
               }
            }

            return data;
         }
      }
   }

   public static boolean hasId(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasSoundEventIndices(MemorySegment mem, int offset) {
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

   public static ItemSoundSet toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static ItemSoundSet toObject(MemorySegment mem, int offset) {
      if (offset + 9 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("ItemSoundSet", offset + 9, (int)mem.byteSize());
      } else {
         Map<ItemSoundEvent, Integer> soundEventIndices = null;
         if (hasSoundEventIndices(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 5, 9, "SoundEventIndices");
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("SoundEventIndices", len);
            }

            if (len > 4096000) {
               throw ProtocolException.dictionaryTooLarge("SoundEventIndices", len, 4096000);
            }

            soundEventIndices = new HashMap<>(len);
            off += (int)(packed >>> 32);

            for (int i = 0; i < len; i++) {
               ItemSoundEvent key = ItemSoundEvent.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)off));
               int value = mem.get(PacketIO.PROTO_INT, (long)(++off));
               off += 4;
               if (soundEventIndices.put(key, value) != null) {
                  throw ProtocolException.duplicateKey("SoundEventIndices", key);
               }
            }
         }

         return new ItemSoundSet(
            hasId(mem, offset) ? PacketIO.readVarString("Id", mem, offset + getValidatedOffset(mem, offset, 1, 9, "Id"), 4096000, PacketIO.UTF8) : null,
            soundEventIndices
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.id != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.soundEventIndices != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      int idOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int soundEventIndicesOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.id != null) {
         buf.setIntLE(idOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.id, 4096000);
      } else {
         buf.setIntLE(idOffsetSlot, -1);
      }

      if (this.soundEventIndices != null) {
         buf.setIntLE(soundEventIndicesOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.soundEventIndices.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("SoundEventIndices", this.soundEventIndices.size(), 4096000);
         }

         VarInt.write(buf, this.soundEventIndices.size());

         for (Entry<ItemSoundEvent, Integer> e : this.soundEventIndices.entrySet()) {
            buf.writeByte(e.getKey().getValue());
            buf.writeIntLE(e.getValue());
         }
      } else {
         buf.setIntLE(soundEventIndicesOffsetSlot, -1);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.id != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.soundEventIndices != null) {
         nullBits = (byte)(nullBits | 2);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      int varOffset = offset + 9;
      if (this.id != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 1), varOffset - offset - 9);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.id, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 1), -1);
      }

      if (this.soundEventIndices != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), varOffset - offset - 9);
         if (this.soundEventIndices.size() > 4096000) {
            throw ProtocolException.dictionaryTooLarge("SoundEventIndices", this.soundEventIndices.size(), 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.soundEventIndices.size());

         for (Entry<ItemSoundEvent, Integer> e : this.soundEventIndices.entrySet()) {
            mem.set(PacketIO.PROTO_BYTE, (long)varOffset, (byte)e.getKey().getValue());
            mem.set(PacketIO.PROTO_INT, (long)(++varOffset), e.getValue());
            varOffset += 4;
         }
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), -1);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 9;
      if (this.id != null) {
         size += PacketIO.stringSize(this.id);
      }

      if (this.soundEventIndices != null) {
         size += VarInt.size(this.soundEventIndices.size()) + this.soundEventIndices.size() * 5;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 9) {
         return ValidationResult.error("Buffer too small: expected at least 9 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int idOffset = buffer.getIntLE(offset + 1);
            if (idOffset < 0 || idOffset > buffer.writerIndex() - offset - 9) {
               return ValidationResult.error("Invalid offset for Id");
            }

            int pos = offset + 9 + idOffset;
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
            int soundEventIndicesOffset = buffer.getIntLE(offset + 5);
            if (soundEventIndicesOffset < 0 || soundEventIndicesOffset > buffer.writerIndex() - offset - 9) {
               return ValidationResult.error("Invalid offset for SoundEventIndices");
            }

            int posx = offset + 9 + soundEventIndicesOffset;
            int soundEventIndicesCount = VarInt.peek(buffer, posx);
            if (soundEventIndicesCount < 0) {
               return ValidationResult.error("Invalid dictionary count for SoundEventIndices");
            }

            if (soundEventIndicesCount > 4096000) {
               return ValidationResult.error("SoundEventIndices exceeds max length 4096000");
            }

            posx += VarInt.size(soundEventIndicesCount);

            for (int i = 0; i < soundEventIndicesCount; i++) {
               int v = buffer.getByte(posx) & 255;
               if (v >= 2) {
                  return ValidationResult.error("Invalid ItemSoundEvent value for key");
               }

               posx = ++posx + 4;
               if (posx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading value");
               }
            }
         }

         return ValidationResult.OK;
      }
   }

   public ItemSoundSet clone() {
      ItemSoundSet copy = new ItemSoundSet();
      copy.id = this.id;
      copy.soundEventIndices = this.soundEventIndices != null ? new HashMap<>(this.soundEventIndices) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ItemSoundSet other)
            ? false
            : Objects.equals(this.id, other.id) && Objects.equals(this.soundEventIndices, other.soundEventIndices);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.id, this.soundEventIndices);
   }
}
