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
      if (buf.readableBytes() - offset < 30) {
         throw ProtocolException.bufferTooSmall("Animation", 30, buf.readableBytes() - offset);
      } else {
         Animation obj = new Animation();
         byte nullBits = buf.getByte(offset);
         obj.speed = buf.getFloatLE(offset + 1);
         obj.blendingDuration = buf.getFloatLE(offset + 5);
         obj.looping = buf.getByte(offset + 9) != 0;
         obj.weight = buf.getFloatLE(offset + 10);
         obj.soundEventIndex = buf.getIntLE(offset + 14);
         obj.passiveLoopCount = buf.getIntLE(offset + 18);
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 22);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 30) {
               throw ProtocolException.invalidOffset("Name", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 30 + varPosBase0;
            int nameLen = VarInt.peek(buf, varPos0);
            if (nameLen < 0) {
               throw ProtocolException.invalidVarInt("Name");
            }

            int nameVarIntLen = VarInt.size(nameLen);
            if (nameLen > 4096000) {
               throw ProtocolException.stringTooLong("Name", nameLen, 4096000);
            }

            if (varPos0 + nameVarIntLen + nameLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Name", varPos0 + nameVarIntLen + nameLen, buf.readableBytes());
            }

            obj.name = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
         }

         if ((nullBits & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 26);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 30) {
               throw ProtocolException.invalidOffset("FootstepIntervals", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 30 + varPosBase1;
            int footstepIntervalsCount = VarInt.peek(buf, varPos1);
            if (footstepIntervalsCount < 0) {
               throw ProtocolException.invalidVarInt("FootstepIntervals");
            }

            int varIntLen = VarInt.size(footstepIntervalsCount);
            if (footstepIntervalsCount > 4096000) {
               throw ProtocolException.arrayTooLong("FootstepIntervals", footstepIntervalsCount, 4096000);
            }

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
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 30;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 22);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 30) {
            throw ProtocolException.invalidOffset("Name", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 30 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 26);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 30) {
            throw ProtocolException.invalidOffset("FootstepIntervals", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 30 + fieldOffset1;
         int arrLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(arrLen) + arrLen * 4;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 30L;
   }

   @Nullable
   public static String getName(MemorySegment mem) {
      return getName(mem, 0);
   }

   @Nullable
   public static String getName(MemorySegment mem, int offset) {
      return hasName(mem, offset)
         ? PacketIO.readVarString("Name", mem, offset + getValidatedOffset(mem, offset, 22, 30, "Name"), 4096000, PacketIO.UTF8)
         : null;
   }

   public static float getSpeed(MemorySegment mem) {
      return getSpeed(mem, 0);
   }

   public static float getSpeed(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 1));
   }

   public static float getBlendingDuration(MemorySegment mem) {
      return getBlendingDuration(mem, 0);
   }

   public static float getBlendingDuration(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 5));
   }

   public static boolean getLooping(MemorySegment mem) {
      return getLooping(mem, 0);
   }

   public static boolean getLooping(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 9));
   }

   public static float getWeight(MemorySegment mem) {
      return getWeight(mem, 0);
   }

   public static float getWeight(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 10));
   }

   @Nullable
   public static int[] getFootstepIntervals(MemorySegment mem) {
      return getFootstepIntervals(mem, 0);
   }

   @Nullable
   public static int[] getFootstepIntervals(MemorySegment mem, int offset) {
      if (!hasFootstepIntervals(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 26, 30, "FootstepIntervals");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("FootstepIntervals", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("FootstepIntervals", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 4L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("FootstepIntervals", off + lenOffset + len * 4, (int)mem.byteSize());
            } else {
               off += lenOffset;
               int[] data = new int[len];
               MemorySegment.copy(mem, PacketIO.PROTO_INT, off, data, 0, len);
               return data;
            }
         }
      }
   }

   public static int getSoundEventIndex(MemorySegment mem) {
      return getSoundEventIndex(mem, 0);
   }

   public static int getSoundEventIndex(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 14));
   }

   public static int getPassiveLoopCount(MemorySegment mem) {
      return getPassiveLoopCount(mem, 0);
   }

   public static int getPassiveLoopCount(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 18));
   }

   public static boolean hasName(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasFootstepIntervals(MemorySegment mem, int offset) {
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

   public static Animation toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static Animation toObject(MemorySegment mem, int offset) {
      if (offset + 30 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("Animation", offset + 30, (int)mem.byteSize());
      } else {
         int[] footstepIntervals = null;
         if (hasFootstepIntervals(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 26, 30, "FootstepIntervals");
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("FootstepIntervals", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("FootstepIntervals", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 4L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("FootstepIntervals", off + lenOffset + len * 4, (int)mem.byteSize());
            }

            off += lenOffset;
            footstepIntervals = new int[len];
            MemorySegment.copy(mem, PacketIO.PROTO_INT, off, footstepIntervals, 0, len);
         }

         return new Animation(
            hasName(mem, offset) ? PacketIO.readVarString("Name", mem, offset + getValidatedOffset(mem, offset, 22, 30, "Name"), 4096000, PacketIO.UTF8) : null,
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 1)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 5)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 9)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 10)),
            footstepIntervals,
            mem.get(PacketIO.PROTO_INT, (long)(offset + 14)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 18))
         );
      }
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

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.name != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.footstepIntervals != null) {
         nullBits = (byte)(nullBits | 2);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 1), this.speed);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 5), this.blendingDuration);
      mem.set(PacketIO.PROTO_BOOL, offset + 9, this.looping);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 10), this.weight);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 14), this.soundEventIndex);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 18), this.passiveLoopCount);
      int varOffset = offset + 30;
      if (this.name != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 22), varOffset - offset - 30);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.name, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 22), -1);
      }

      if (this.footstepIntervals != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 26), varOffset - offset - 30);
         if (this.footstepIntervals.length > 4096000) {
            throw ProtocolException.arrayTooLong("FootstepIntervals", this.footstepIntervals.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.footstepIntervals.length);
         MemorySegment.copy(this.footstepIntervals, 0, mem, PacketIO.PROTO_INT, varOffset, this.footstepIntervals.length);
         varOffset += this.footstepIntervals.length * 4;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 26), -1);
      }

      return varOffset - offset;
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
            if (nameOffset < 0 || nameOffset > buffer.writerIndex() - offset - 30) {
               return ValidationResult.error("Invalid offset for Name");
            }

            int pos = offset + 30 + nameOffset;
            int nameLen = VarInt.peek(buffer, pos);
            if (nameLen < 0) {
               return ValidationResult.error("Invalid string length for Name");
            }

            if (nameLen > 4096000) {
               return ValidationResult.error("Name exceeds max length 4096000");
            }

            pos += VarInt.size(nameLen);
            pos += nameLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Name");
            }
         }

         if ((nullBits & 2) != 0) {
            int footstepIntervalsOffset = buffer.getIntLE(offset + 26);
            if (footstepIntervalsOffset < 0 || footstepIntervalsOffset > buffer.writerIndex() - offset - 30) {
               return ValidationResult.error("Invalid offset for FootstepIntervals");
            }

            int posx = offset + 30 + footstepIntervalsOffset;
            int footstepIntervalsCount = VarInt.peek(buffer, posx);
            if (footstepIntervalsCount < 0) {
               return ValidationResult.error("Invalid array count for FootstepIntervals");
            }

            if (footstepIntervalsCount > 4096000) {
               return ValidationResult.error("FootstepIntervals exceeds max length 4096000");
            }

            posx += VarInt.size(footstepIntervalsCount);
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
