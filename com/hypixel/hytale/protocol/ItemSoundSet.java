package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
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
      ItemSoundSet obj = new ItemSoundSet();
      byte nullBits = buf.getByte(offset);
      if ((nullBits & 1) != 0) {
         int varPos0 = offset + 9 + buf.getIntLE(offset + 1);
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
         int varPos1 = offset + 9 + buf.getIntLE(offset + 5);
         int soundEventIndicesCount = VarInt.peek(buf, varPos1);
         if (soundEventIndicesCount < 0) {
            throw ProtocolException.negativeLength("SoundEventIndices", soundEventIndicesCount);
         }

         if (soundEventIndicesCount > 4096000) {
            throw ProtocolException.dictionaryTooLarge("SoundEventIndices", soundEventIndicesCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos1);
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

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 9;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 1);
         int pos0 = offset + 9 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 5);
         int pos1 = offset + 9 + fieldOffset1;
         int dictLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.length(buf, pos1);

         for (int i = 0; i < dictLen; i++) {
            pos1 = ++pos1 + 4;
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
            if (idOffset < 0) {
               return ValidationResult.error("Invalid offset for Id");
            }

            int pos = offset + 9 + idOffset;
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
            int soundEventIndicesOffset = buffer.getIntLE(offset + 5);
            if (soundEventIndicesOffset < 0) {
               return ValidationResult.error("Invalid offset for SoundEventIndices");
            }

            int posx = offset + 9 + soundEventIndicesOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for SoundEventIndices");
            }

            int soundEventIndicesCount = VarInt.peek(buffer, posx);
            if (soundEventIndicesCount < 0) {
               return ValidationResult.error("Invalid dictionary count for SoundEventIndices");
            }

            if (soundEventIndicesCount > 4096000) {
               return ValidationResult.error("SoundEventIndices exceeds max length 4096000");
            }

            posx += VarInt.length(buffer, posx);

            for (int i = 0; i < soundEventIndicesCount; i++) {
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
