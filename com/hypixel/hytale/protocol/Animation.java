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

public class Animation {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 22;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 30;
   public static final int MAX_SIZE = 32768040;
   @Nullable
   public String name;
   public float speed;
   public float blendingDuration = 0.2F;
   public boolean looping;
   public float weight;
   @Nullable
   public int[] footstepIntervals;
   public int soundEventIndex;
   public int passiveLoopCount;

   public Animation() {
   }

   public Animation(
      @Nullable String name,
      float speed,
      float blendingDuration,
      boolean looping,
      float weight,
      @Nullable int[] footstepIntervals,
      int soundEventIndex,
      int passiveLoopCount
   ) {
      this.name = name;
      this.speed = speed;
      this.blendingDuration = blendingDuration;
      this.looping = looping;
      this.weight = weight;
      this.footstepIntervals = footstepIntervals;
      this.soundEventIndex = soundEventIndex;
      this.passiveLoopCount = passiveLoopCount;
   }

   public Animation(@Nonnull Animation other) {
      this.name = other.name;
      this.speed = other.speed;
      this.blendingDuration = other.blendingDuration;
      this.looping = other.looping;
      this.weight = other.weight;
      this.footstepIntervals = other.footstepIntervals;
      this.soundEventIndex = other.soundEventIndex;
      this.passiveLoopCount = other.passiveLoopCount;
   }

   @Nonnull
   public static Animation deserialize(@Nonnull ByteBuf buf, int offset) {
      Animation obj = new Animation();
      byte nullBits = buf.getByte(offset);
      obj.speed = buf.getFloatLE(offset + 1);
      obj.blendingDuration = buf.getFloatLE(offset + 5);
      obj.looping = buf.getByte(offset + 9) != 0;
      obj.weight = buf.getFloatLE(offset + 10);
      obj.soundEventIndex = buf.getIntLE(offset + 14);
      obj.passiveLoopCount = buf.getIntLE(offset + 18);
      if ((nullBits & 1) != 0) {
         int varPos0 = offset + 30 + buf.getIntLE(offset + 22);
         int nameLen = VarInt.peek(buf, varPos0);
         if (nameLen < 0) {
            throw ProtocolException.negativeLength("Name", nameLen);
         }

         if (nameLen > 4096000) {
            throw ProtocolException.stringTooLong("Name", nameLen, 4096000);
         }

         obj.name = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
      }

      if ((nullBits & 2) != 0) {
         int varPos1 = offset + 30 + buf.getIntLE(offset + 26);
         int footstepIntervalsCount = VarInt.peek(buf, varPos1);
         if (footstepIntervalsCount < 0) {
            throw ProtocolException.negativeLength("FootstepIntervals", footstepIntervalsCount);
         }

         if (footstepIntervalsCount > 4096000) {
            throw ProtocolException.arrayTooLong("FootstepIntervals", footstepIntervalsCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos1);
         if (varPos1 + varIntLen + footstepIntervalsCount * 4L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("FootstepIntervals", varPos1 + varIntLen + footstepIntervalsCount * 4, buf.readableBytes());
         }

         obj.footstepIntervals = new int[footstepIntervalsCount];

         for (int i = 0; i < footstepIntervalsCount; i++) {
            obj.footstepIntervals[i] = buf.getIntLE(varPos1 + varIntLen + i * 4);
         }
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 30;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 22);
         int pos0 = offset + 30 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 26);
         int pos1 = offset + 30 + fieldOffset1;
         int arrLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.length(buf, pos1) + arrLen * 4;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.name != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.footstepIntervals != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      buf.writeFloatLE(this.speed);
      buf.writeFloatLE(this.blendingDuration);
      buf.writeByte(this.looping ? 1 : 0);
      buf.writeFloatLE(this.weight);
      buf.writeIntLE(this.soundEventIndex);
      buf.writeIntLE(this.passiveLoopCount);
      int nameOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int footstepIntervalsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.name != null) {
         buf.setIntLE(nameOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.name, 4096000);
      } else {
         buf.setIntLE(nameOffsetSlot, -1);
      }

      if (this.footstepIntervals != null) {
         buf.setIntLE(footstepIntervalsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.footstepIntervals.length > 4096000) {
            throw ProtocolException.arrayTooLong("FootstepIntervals", this.footstepIntervals.length, 4096000);
         }

         VarInt.write(buf, this.footstepIntervals.length);

         for (int item : this.footstepIntervals) {
            buf.writeIntLE(item);
         }
      } else {
         buf.setIntLE(footstepIntervalsOffsetSlot, -1);
      }
   }

   public int computeSize() {
      int size = 30;
      if (this.name != null) {
         size += PacketIO.stringSize(this.name);
      }

      if (this.footstepIntervals != null) {
         size += VarInt.size(this.footstepIntervals.length) + this.footstepIntervals.length * 4;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 30) {
         return ValidationResult.error("Buffer too small: expected at least 30 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int nameOffset = buffer.getIntLE(offset + 22);
            if (nameOffset < 0) {
               return ValidationResult.error("Invalid offset for Name");
            }

            int pos = offset + 30 + nameOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Name");
            }

            int nameLen = VarInt.peek(buffer, pos);
            if (nameLen < 0) {
               return ValidationResult.error("Invalid string length for Name");
            }

            if (nameLen > 4096000) {
               return ValidationResult.error("Name exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += nameLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Name");
            }
         }

         if ((nullBits & 2) != 0) {
            int footstepIntervalsOffset = buffer.getIntLE(offset + 26);
            if (footstepIntervalsOffset < 0) {
               return ValidationResult.error("Invalid offset for FootstepIntervals");
            }

            int posx = offset + 30 + footstepIntervalsOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for FootstepIntervals");
            }

            int footstepIntervalsCount = VarInt.peek(buffer, posx);
            if (footstepIntervalsCount < 0) {
               return ValidationResult.error("Invalid array count for FootstepIntervals");
            }

            if (footstepIntervalsCount > 4096000) {
               return ValidationResult.error("FootstepIntervals exceeds max length 4096000");
            }

            posx += VarInt.length(buffer, posx);
            posx += footstepIntervalsCount * 4;
            if (posx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading FootstepIntervals");
            }
         }

         return ValidationResult.OK;
      }
   }

   public Animation clone() {
      Animation copy = new Animation();
      copy.name = this.name;
      copy.speed = this.speed;
      copy.blendingDuration = this.blendingDuration;
      copy.looping = this.looping;
      copy.weight = this.weight;
      copy.footstepIntervals = this.footstepIntervals != null ? Arrays.copyOf(this.footstepIntervals, this.footstepIntervals.length) : null;
      copy.soundEventIndex = this.soundEventIndex;
      copy.passiveLoopCount = this.passiveLoopCount;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof Animation other)
            ? false
            : Objects.equals(this.name, other.name)
               && this.speed == other.speed
               && this.blendingDuration == other.blendingDuration
               && this.looping == other.looping
               && this.weight == other.weight
               && Arrays.equals(this.footstepIntervals, other.footstepIntervals)
               && this.soundEventIndex == other.soundEventIndex
               && this.passiveLoopCount == other.passiveLoopCount;
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.name);
      result = 31 * result + Float.hashCode(this.speed);
      result = 31 * result + Float.hashCode(this.blendingDuration);
      result = 31 * result + Boolean.hashCode(this.looping);
      result = 31 * result + Float.hashCode(this.weight);
      result = 31 * result + Arrays.hashCode(this.footstepIntervals);
      result = 31 * result + Integer.hashCode(this.soundEventIndex);
      return 31 * result + Integer.hashCode(this.passiveLoopCount);
   }
}
