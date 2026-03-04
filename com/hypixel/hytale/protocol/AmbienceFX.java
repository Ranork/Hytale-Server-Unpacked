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

public class AmbienceFX {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 18;
   public static final int VARIABLE_FIELD_COUNT = 6;
   public static final int VARIABLE_BLOCK_START = 42;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public String id;
   @Nullable
   public AmbienceFXConditions conditions;
   @Nullable
   public AmbienceFXSound[] sounds;
   @Nullable
   public AmbienceFXMusic music;
   @Nullable
   public AmbienceFXAmbientBed ambientBed;
   @Nullable
   public AmbienceFXSoundEffect soundEffect;
   public int priority;
   @Nullable
   public int[] blockedAmbienceFxIndices;
   public int audioCategoryIndex;

   public AmbienceFX() {
   }

   public AmbienceFX(
      @Nullable String id,
      @Nullable AmbienceFXConditions conditions,
      @Nullable AmbienceFXSound[] sounds,
      @Nullable AmbienceFXMusic music,
      @Nullable AmbienceFXAmbientBed ambientBed,
      @Nullable AmbienceFXSoundEffect soundEffect,
      int priority,
      @Nullable int[] blockedAmbienceFxIndices,
      int audioCategoryIndex
   ) {
      this.id = id;
      this.conditions = conditions;
      this.sounds = sounds;
      this.music = music;
      this.ambientBed = ambientBed;
      this.soundEffect = soundEffect;
      this.priority = priority;
      this.blockedAmbienceFxIndices = blockedAmbienceFxIndices;
      this.audioCategoryIndex = audioCategoryIndex;
   }

   public AmbienceFX(@Nonnull AmbienceFX other) {
      this.id = other.id;
      this.conditions = other.conditions;
      this.sounds = other.sounds;
      this.music = other.music;
      this.ambientBed = other.ambientBed;
      this.soundEffect = other.soundEffect;
      this.priority = other.priority;
      this.blockedAmbienceFxIndices = other.blockedAmbienceFxIndices;
      this.audioCategoryIndex = other.audioCategoryIndex;
   }

   @Nonnull
   public static AmbienceFX deserialize(@Nonnull ByteBuf buf, int offset) {
      AmbienceFX obj = new AmbienceFX();
      byte nullBits = buf.getByte(offset);
      if ((nullBits & 1) != 0) {
         obj.soundEffect = AmbienceFXSoundEffect.deserialize(buf, offset + 1);
      }

      obj.priority = buf.getIntLE(offset + 10);
      obj.audioCategoryIndex = buf.getIntLE(offset + 14);
      if ((nullBits & 2) != 0) {
         int varPos0 = offset + 42 + buf.getIntLE(offset + 18);
         int idLen = VarInt.peek(buf, varPos0);
         if (idLen < 0) {
            throw ProtocolException.negativeLength("Id", idLen);
         }

         if (idLen > 4096000) {
            throw ProtocolException.stringTooLong("Id", idLen, 4096000);
         }

         obj.id = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
      }

      if ((nullBits & 4) != 0) {
         int varPos1 = offset + 42 + buf.getIntLE(offset + 22);
         obj.conditions = AmbienceFXConditions.deserialize(buf, varPos1);
      }

      if ((nullBits & 8) != 0) {
         int varPos2 = offset + 42 + buf.getIntLE(offset + 26);
         int soundsCount = VarInt.peek(buf, varPos2);
         if (soundsCount < 0) {
            throw ProtocolException.negativeLength("Sounds", soundsCount);
         }

         if (soundsCount > 4096000) {
            throw ProtocolException.arrayTooLong("Sounds", soundsCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos2);
         if (varPos2 + varIntLen + soundsCount * 27L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("Sounds", varPos2 + varIntLen + soundsCount * 27, buf.readableBytes());
         }

         obj.sounds = new AmbienceFXSound[soundsCount];
         int elemPos = varPos2 + varIntLen;

         for (int i = 0; i < soundsCount; i++) {
            obj.sounds[i] = AmbienceFXSound.deserialize(buf, elemPos);
            elemPos += AmbienceFXSound.computeBytesConsumed(buf, elemPos);
         }
      }

      if ((nullBits & 16) != 0) {
         int varPos3 = offset + 42 + buf.getIntLE(offset + 30);
         obj.music = AmbienceFXMusic.deserialize(buf, varPos3);
      }

      if ((nullBits & 32) != 0) {
         int varPos4 = offset + 42 + buf.getIntLE(offset + 34);
         obj.ambientBed = AmbienceFXAmbientBed.deserialize(buf, varPos4);
      }

      if ((nullBits & 64) != 0) {
         int varPos5 = offset + 42 + buf.getIntLE(offset + 38);
         int blockedAmbienceFxIndicesCount = VarInt.peek(buf, varPos5);
         if (blockedAmbienceFxIndicesCount < 0) {
            throw ProtocolException.negativeLength("BlockedAmbienceFxIndices", blockedAmbienceFxIndicesCount);
         }

         if (blockedAmbienceFxIndicesCount > 4096000) {
            throw ProtocolException.arrayTooLong("BlockedAmbienceFxIndices", blockedAmbienceFxIndicesCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos5);
         if (varPos5 + varIntLen + blockedAmbienceFxIndicesCount * 4L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("BlockedAmbienceFxIndices", varPos5 + varIntLen + blockedAmbienceFxIndicesCount * 4, buf.readableBytes());
         }

         obj.blockedAmbienceFxIndices = new int[blockedAmbienceFxIndicesCount];

         for (int i = 0; i < blockedAmbienceFxIndicesCount; i++) {
            obj.blockedAmbienceFxIndices[i] = buf.getIntLE(varPos5 + varIntLen + i * 4);
         }
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 42;
      if ((nullBits & 2) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 18);
         int pos0 = offset + 42 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 22);
         int pos1 = offset + 42 + fieldOffset1;
         pos1 += AmbienceFXConditions.computeBytesConsumed(buf, pos1);
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 8) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 26);
         int pos2 = offset + 42 + fieldOffset2;
         int arrLen = VarInt.peek(buf, pos2);
         pos2 += VarInt.length(buf, pos2);

         for (int i = 0; i < arrLen; i++) {
            pos2 += AmbienceFXSound.computeBytesConsumed(buf, pos2);
         }

         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      if ((nullBits & 16) != 0) {
         int fieldOffset3 = buf.getIntLE(offset + 30);
         int pos3 = offset + 42 + fieldOffset3;
         pos3 += AmbienceFXMusic.computeBytesConsumed(buf, pos3);
         if (pos3 - offset > maxEnd) {
            maxEnd = pos3 - offset;
         }
      }

      if ((nullBits & 32) != 0) {
         int fieldOffset4 = buf.getIntLE(offset + 34);
         int pos4 = offset + 42 + fieldOffset4;
         pos4 += AmbienceFXAmbientBed.computeBytesConsumed(buf, pos4);
         if (pos4 - offset > maxEnd) {
            maxEnd = pos4 - offset;
         }
      }

      if ((nullBits & 64) != 0) {
         int fieldOffset5 = buf.getIntLE(offset + 38);
         int pos5 = offset + 42 + fieldOffset5;
         int arrLen = VarInt.peek(buf, pos5);
         pos5 += VarInt.length(buf, pos5) + arrLen * 4;
         if (pos5 - offset > maxEnd) {
            maxEnd = pos5 - offset;
         }
      }

      return maxEnd;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.soundEffect != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.id != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.conditions != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.sounds != null) {
         nullBits = (byte)(nullBits | 8);
      }

      if (this.music != null) {
         nullBits = (byte)(nullBits | 16);
      }

      if (this.ambientBed != null) {
         nullBits = (byte)(nullBits | 32);
      }

      if (this.blockedAmbienceFxIndices != null) {
         nullBits = (byte)(nullBits | 64);
      }

      buf.writeByte(nullBits);
      if (this.soundEffect != null) {
         this.soundEffect.serialize(buf);
      } else {
         buf.writeZero(9);
      }

      buf.writeIntLE(this.priority);
      buf.writeIntLE(this.audioCategoryIndex);
      int idOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int conditionsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int soundsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int musicOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int ambientBedOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int blockedAmbienceFxIndicesOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.id != null) {
         buf.setIntLE(idOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.id, 4096000);
      } else {
         buf.setIntLE(idOffsetSlot, -1);
      }

      if (this.conditions != null) {
         buf.setIntLE(conditionsOffsetSlot, buf.writerIndex() - varBlockStart);
         this.conditions.serialize(buf);
      } else {
         buf.setIntLE(conditionsOffsetSlot, -1);
      }

      if (this.sounds != null) {
         buf.setIntLE(soundsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.sounds.length > 4096000) {
            throw ProtocolException.arrayTooLong("Sounds", this.sounds.length, 4096000);
         }

         VarInt.write(buf, this.sounds.length);

         for (AmbienceFXSound item : this.sounds) {
            item.serialize(buf);
         }
      } else {
         buf.setIntLE(soundsOffsetSlot, -1);
      }

      if (this.music != null) {
         buf.setIntLE(musicOffsetSlot, buf.writerIndex() - varBlockStart);
         this.music.serialize(buf);
      } else {
         buf.setIntLE(musicOffsetSlot, -1);
      }

      if (this.ambientBed != null) {
         buf.setIntLE(ambientBedOffsetSlot, buf.writerIndex() - varBlockStart);
         this.ambientBed.serialize(buf);
      } else {
         buf.setIntLE(ambientBedOffsetSlot, -1);
      }

      if (this.blockedAmbienceFxIndices != null) {
         buf.setIntLE(blockedAmbienceFxIndicesOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.blockedAmbienceFxIndices.length > 4096000) {
            throw ProtocolException.arrayTooLong("BlockedAmbienceFxIndices", this.blockedAmbienceFxIndices.length, 4096000);
         }

         VarInt.write(buf, this.blockedAmbienceFxIndices.length);

         for (int item : this.blockedAmbienceFxIndices) {
            buf.writeIntLE(item);
         }
      } else {
         buf.setIntLE(blockedAmbienceFxIndicesOffsetSlot, -1);
      }
   }

   public int computeSize() {
      int size = 42;
      if (this.id != null) {
         size += PacketIO.stringSize(this.id);
      }

      if (this.conditions != null) {
         size += this.conditions.computeSize();
      }

      if (this.sounds != null) {
         size += VarInt.size(this.sounds.length) + this.sounds.length * 27;
      }

      if (this.music != null) {
         size += this.music.computeSize();
      }

      if (this.ambientBed != null) {
         size += this.ambientBed.computeSize();
      }

      if (this.blockedAmbienceFxIndices != null) {
         size += VarInt.size(this.blockedAmbienceFxIndices.length) + this.blockedAmbienceFxIndices.length * 4;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 42) {
         return ValidationResult.error("Buffer too small: expected at least 42 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 2) != 0) {
            int idOffset = buffer.getIntLE(offset + 18);
            if (idOffset < 0) {
               return ValidationResult.error("Invalid offset for Id");
            }

            int pos = offset + 42 + idOffset;
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

         if ((nullBits & 4) != 0) {
            int conditionsOffset = buffer.getIntLE(offset + 22);
            if (conditionsOffset < 0) {
               return ValidationResult.error("Invalid offset for Conditions");
            }

            int posx = offset + 42 + conditionsOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Conditions");
            }

            ValidationResult conditionsResult = AmbienceFXConditions.validateStructure(buffer, posx);
            if (!conditionsResult.isValid()) {
               return ValidationResult.error("Invalid Conditions: " + conditionsResult.error());
            }

            posx += AmbienceFXConditions.computeBytesConsumed(buffer, posx);
         }

         if ((nullBits & 8) != 0) {
            int soundsOffset = buffer.getIntLE(offset + 26);
            if (soundsOffset < 0) {
               return ValidationResult.error("Invalid offset for Sounds");
            }

            int posxx = offset + 42 + soundsOffset;
            if (posxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Sounds");
            }

            int soundsCount = VarInt.peek(buffer, posxx);
            if (soundsCount < 0) {
               return ValidationResult.error("Invalid array count for Sounds");
            }

            if (soundsCount > 4096000) {
               return ValidationResult.error("Sounds exceeds max length 4096000");
            }

            posxx += VarInt.length(buffer, posxx);
            posxx += soundsCount * 27;
            if (posxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Sounds");
            }
         }

         if ((nullBits & 16) != 0) {
            int musicOffset = buffer.getIntLE(offset + 30);
            if (musicOffset < 0) {
               return ValidationResult.error("Invalid offset for Music");
            }

            int posxxx = offset + 42 + musicOffset;
            if (posxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Music");
            }

            ValidationResult musicResult = AmbienceFXMusic.validateStructure(buffer, posxxx);
            if (!musicResult.isValid()) {
               return ValidationResult.error("Invalid Music: " + musicResult.error());
            }

            posxxx += AmbienceFXMusic.computeBytesConsumed(buffer, posxxx);
         }

         if ((nullBits & 32) != 0) {
            int ambientBedOffset = buffer.getIntLE(offset + 34);
            if (ambientBedOffset < 0) {
               return ValidationResult.error("Invalid offset for AmbientBed");
            }

            int posxxxx = offset + 42 + ambientBedOffset;
            if (posxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for AmbientBed");
            }

            ValidationResult ambientBedResult = AmbienceFXAmbientBed.validateStructure(buffer, posxxxx);
            if (!ambientBedResult.isValid()) {
               return ValidationResult.error("Invalid AmbientBed: " + ambientBedResult.error());
            }

            posxxxx += AmbienceFXAmbientBed.computeBytesConsumed(buffer, posxxxx);
         }

         if ((nullBits & 64) != 0) {
            int blockedAmbienceFxIndicesOffset = buffer.getIntLE(offset + 38);
            if (blockedAmbienceFxIndicesOffset < 0) {
               return ValidationResult.error("Invalid offset for BlockedAmbienceFxIndices");
            }

            int posxxxxx = offset + 42 + blockedAmbienceFxIndicesOffset;
            if (posxxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for BlockedAmbienceFxIndices");
            }

            int blockedAmbienceFxIndicesCount = VarInt.peek(buffer, posxxxxx);
            if (blockedAmbienceFxIndicesCount < 0) {
               return ValidationResult.error("Invalid array count for BlockedAmbienceFxIndices");
            }

            if (blockedAmbienceFxIndicesCount > 4096000) {
               return ValidationResult.error("BlockedAmbienceFxIndices exceeds max length 4096000");
            }

            posxxxxx += VarInt.length(buffer, posxxxxx);
            posxxxxx += blockedAmbienceFxIndicesCount * 4;
            if (posxxxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading BlockedAmbienceFxIndices");
            }
         }

         return ValidationResult.OK;
      }
   }

   public AmbienceFX clone() {
      AmbienceFX copy = new AmbienceFX();
      copy.id = this.id;
      copy.conditions = this.conditions != null ? this.conditions.clone() : null;
      copy.sounds = this.sounds != null ? Arrays.stream(this.sounds).map(e -> e.clone()).toArray(AmbienceFXSound[]::new) : null;
      copy.music = this.music != null ? this.music.clone() : null;
      copy.ambientBed = this.ambientBed != null ? this.ambientBed.clone() : null;
      copy.soundEffect = this.soundEffect != null ? this.soundEffect.clone() : null;
      copy.priority = this.priority;
      copy.blockedAmbienceFxIndices = this.blockedAmbienceFxIndices != null
         ? Arrays.copyOf(this.blockedAmbienceFxIndices, this.blockedAmbienceFxIndices.length)
         : null;
      copy.audioCategoryIndex = this.audioCategoryIndex;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof AmbienceFX other)
            ? false
            : Objects.equals(this.id, other.id)
               && Objects.equals(this.conditions, other.conditions)
               && Arrays.equals((Object[])this.sounds, (Object[])other.sounds)
               && Objects.equals(this.music, other.music)
               && Objects.equals(this.ambientBed, other.ambientBed)
               && Objects.equals(this.soundEffect, other.soundEffect)
               && this.priority == other.priority
               && Arrays.equals(this.blockedAmbienceFxIndices, other.blockedAmbienceFxIndices)
               && this.audioCategoryIndex == other.audioCategoryIndex;
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.id);
      result = 31 * result + Objects.hashCode(this.conditions);
      result = 31 * result + Arrays.hashCode((Object[])this.sounds);
      result = 31 * result + Objects.hashCode(this.music);
      result = 31 * result + Objects.hashCode(this.ambientBed);
      result = 31 * result + Objects.hashCode(this.soundEffect);
      result = 31 * result + Integer.hashCode(this.priority);
      result = 31 * result + Arrays.hashCode(this.blockedAmbienceFxIndices);
      return 31 * result + Integer.hashCode(this.audioCategoryIndex);
   }
}
