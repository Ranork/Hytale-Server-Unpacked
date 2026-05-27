package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CameraShakeConfig {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 20;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 28;
   public static final int MAX_SIZE = 565248084;
   public float duration;
   public float startTime;
   public boolean continuous;
   @Nullable
   public EasingConfig easeIn;
   @Nullable
   public EasingConfig easeOut;
   @Nullable
   public OffsetNoise offset;
   @Nullable
   public RotationNoise rotation;

   public CameraShakeConfig() {
   }

   public CameraShakeConfig(
      float duration,
      float startTime,
      boolean continuous,
      @Nullable EasingConfig easeIn,
      @Nullable EasingConfig easeOut,
      @Nullable OffsetNoise offset,
      @Nullable RotationNoise rotation
   ) {
      this.duration = duration;
      this.startTime = startTime;
      this.continuous = continuous;
      this.easeIn = easeIn;
      this.easeOut = easeOut;
      this.offset = offset;
      this.rotation = rotation;
   }

   public CameraShakeConfig(@Nonnull CameraShakeConfig other) {
      this.duration = other.duration;
      this.startTime = other.startTime;
      this.continuous = other.continuous;
      this.easeIn = other.easeIn;
      this.easeOut = other.easeOut;
      this.offset = other.offset;
      this.rotation = other.rotation;
   }

   @Nonnull
   public static CameraShakeConfig deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 28) {
         throw ProtocolException.bufferTooSmall("CameraShakeConfig", 28, buf.readableBytes() - offset);
      } else {
         CameraShakeConfig obj = new CameraShakeConfig();
         byte nullBits = buf.getByte(offset);
         obj.duration = buf.getFloatLE(offset + 1);
         obj.startTime = buf.getFloatLE(offset + 5);
         obj.continuous = buf.getByte(offset + 9) != 0;
         if ((nullBits & 1) != 0) {
            obj.easeIn = EasingConfig.deserialize(buf, offset + 10);
         }

         if ((nullBits & 2) != 0) {
            obj.easeOut = EasingConfig.deserialize(buf, offset + 15);
         }

         if ((nullBits & 4) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 20);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 28) {
               throw ProtocolException.invalidOffset("Offset", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 28 + varPosBase0;
            obj.offset = OffsetNoise.deserialize(buf, varPos0);
         }

         if ((nullBits & 8) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 24);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 28) {
               throw ProtocolException.invalidOffset("Rotation", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 28 + varPosBase1;
            obj.rotation = RotationNoise.deserialize(buf, varPos1);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 28;
      if ((nullBits & 4) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 20);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 28) {
            throw ProtocolException.invalidOffset("Offset", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 28 + fieldOffset0;
         pos0 += OffsetNoise.computeBytesConsumed(buf, pos0);
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 8) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 24);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 28) {
            throw ProtocolException.invalidOffset("Rotation", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 28 + fieldOffset1;
         pos1 += RotationNoise.computeBytesConsumed(buf, pos1);
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 28L;
   }

   public static float getDuration(MemorySegment mem) {
      return getDuration(mem, 0);
   }

   public static float getDuration(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 1));
   }

   public static float getStartTime(MemorySegment mem) {
      return getStartTime(mem, 0);
   }

   public static float getStartTime(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 5));
   }

   public static boolean getContinuous(MemorySegment mem) {
      return getContinuous(mem, 0);
   }

   public static boolean getContinuous(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 9));
   }

   @Nullable
   public static EasingConfig getEaseIn(MemorySegment mem) {
      return getEaseIn(mem, 0);
   }

   @Nullable
   public static EasingConfig getEaseIn(MemorySegment mem, int offset) {
      return hasEaseIn(mem, offset) ? EasingConfig.toObject(mem, offset + 10) : null;
   }

   @Nullable
   public static EasingConfig getEaseOut(MemorySegment mem) {
      return getEaseOut(mem, 0);
   }

   @Nullable
   public static EasingConfig getEaseOut(MemorySegment mem, int offset) {
      return hasEaseOut(mem, offset) ? EasingConfig.toObject(mem, offset + 15) : null;
   }

   @Nullable
   public static OffsetNoise getOffset(MemorySegment mem) {
      return getOffset(mem, 0);
   }

   @Nullable
   public static OffsetNoise getOffset(MemorySegment mem, int offset) {
      return hasOffset(mem, offset) ? OffsetNoise.toObject(mem, offset + getValidatedOffset(mem, offset, 20, 28, "Offset")) : null;
   }

   @Nullable
   public static RotationNoise getRotation(MemorySegment mem) {
      return getRotation(mem, 0);
   }

   @Nullable
   public static RotationNoise getRotation(MemorySegment mem, int offset) {
      return hasRotation(mem, offset) ? RotationNoise.toObject(mem, offset + getValidatedOffset(mem, offset, 24, 28, "Rotation")) : null;
   }

   public static boolean hasEaseIn(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasEaseOut(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasOffset(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 4) != 0;
   }

   public static boolean hasRotation(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 8) != 0;
   }

   private static int getValidatedOffset(MemorySegment buffer, int base, int slotPosition, int varBlockStart, String fieldName) {
      int offset = buffer.get(PacketIO.PROTO_INT, (long)(base + slotPosition));
      if (offset >= 0 && offset <= buffer.byteSize() - base - varBlockStart) {
         return varBlockStart + offset;
      } else {
         throw ProtocolException.invalidOffset(fieldName, offset, (int)buffer.byteSize());
      }
   }

   public static CameraShakeConfig toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static CameraShakeConfig toObject(MemorySegment mem, int offset) {
      if (offset + 28 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("CameraShakeConfig", offset + 28, (int)mem.byteSize());
      } else {
         return new CameraShakeConfig(
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 1)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 5)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 9)),
            hasEaseIn(mem, offset) ? EasingConfig.toObject(mem, offset + 10) : null,
            hasEaseOut(mem, offset) ? EasingConfig.toObject(mem, offset + 15) : null,
            hasOffset(mem, offset) ? OffsetNoise.toObject(mem, offset + getValidatedOffset(mem, offset, 20, 28, "Offset")) : null,
            hasRotation(mem, offset) ? RotationNoise.toObject(mem, offset + getValidatedOffset(mem, offset, 24, 28, "Rotation")) : null
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.easeIn != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.easeOut != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.offset != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.rotation != null) {
         nullBits = (byte)(nullBits | 8);
      }

      buf.writeByte(nullBits);
      buf.writeFloatLE(this.duration);
      buf.writeFloatLE(this.startTime);
      buf.writeByte(this.continuous ? 1 : 0);
      if (this.easeIn != null) {
         this.easeIn.serialize(buf);
      } else {
         buf.writeZero(5);
      }

      if (this.easeOut != null) {
         this.easeOut.serialize(buf);
      } else {
         buf.writeZero(5);
      }

      int offsetOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int rotationOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.offset != null) {
         buf.setIntLE(offsetOffsetSlot, buf.writerIndex() - varBlockStart);
         this.offset.serialize(buf);
      } else {
         buf.setIntLE(offsetOffsetSlot, -1);
      }

      if (this.rotation != null) {
         buf.setIntLE(rotationOffsetSlot, buf.writerIndex() - varBlockStart);
         this.rotation.serialize(buf);
      } else {
         buf.setIntLE(rotationOffsetSlot, -1);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.easeIn != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.easeOut != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.offset != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.rotation != null) {
         nullBits = (byte)(nullBits | 8);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 1), this.duration);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 5), this.startTime);
      mem.set(PacketIO.PROTO_BOOL, offset + 9, this.continuous);
      if (this.easeIn != null) {
         this.easeIn.serialize(mem, offset + 10);
      } else {
         mem.asSlice(offset + 10, 5L).fill((byte)0);
      }

      if (this.easeOut != null) {
         this.easeOut.serialize(mem, offset + 15);
      } else {
         mem.asSlice(offset + 15, 5L).fill((byte)0);
      }

      int varOffset = offset + 28;
      if (this.offset != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 20), varOffset - offset - 28);
         varOffset += this.offset.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 20), -1);
      }

      if (this.rotation != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 24), varOffset - offset - 28);
         varOffset += this.rotation.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 24), -1);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 28;
      if (this.offset != null) {
         size += this.offset.computeSize();
      }

      if (this.rotation != null) {
         size += this.rotation.computeSize();
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 28) {
         return ValidationResult.error("Buffer too small: expected at least 28 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 4) != 0) {
            int offsetOffset = buffer.getIntLE(offset + 20);
            if (offsetOffset < 0 || offsetOffset > buffer.writerIndex() - offset - 28) {
               return ValidationResult.error("Invalid offset for Offset");
            }

            int pos = offset + 28 + offsetOffset;
            ValidationResult offsetResult = OffsetNoise.validateStructure(buffer, pos);
            if (!offsetResult.isValid()) {
               return ValidationResult.error("Invalid Offset: " + offsetResult.error());
            }

            pos += OffsetNoise.computeBytesConsumed(buffer, pos);
         }

         if ((nullBits & 8) != 0) {
            int rotationOffset = buffer.getIntLE(offset + 24);
            if (rotationOffset < 0 || rotationOffset > buffer.writerIndex() - offset - 28) {
               return ValidationResult.error("Invalid offset for Rotation");
            }

            int pos = offset + 28 + rotationOffset;
            ValidationResult rotationResult = RotationNoise.validateStructure(buffer, pos);
            if (!rotationResult.isValid()) {
               return ValidationResult.error("Invalid Rotation: " + rotationResult.error());
            }

            pos += RotationNoise.computeBytesConsumed(buffer, pos);
         }

         return ValidationResult.OK;
      }
   }

   public CameraShakeConfig clone() {
      CameraShakeConfig copy = new CameraShakeConfig();
      copy.duration = this.duration;
      copy.startTime = this.startTime;
      copy.continuous = this.continuous;
      copy.easeIn = this.easeIn != null ? this.easeIn.clone() : null;
      copy.easeOut = this.easeOut != null ? this.easeOut.clone() : null;
      copy.offset = this.offset != null ? this.offset.clone() : null;
      copy.rotation = this.rotation != null ? this.rotation.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof CameraShakeConfig other)
            ? false
            : this.duration == other.duration
               && this.startTime == other.startTime
               && this.continuous == other.continuous
               && Objects.equals(this.easeIn, other.easeIn)
               && Objects.equals(this.easeOut, other.easeOut)
               && Objects.equals(this.offset, other.offset)
               && Objects.equals(this.rotation, other.rotation);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.duration, this.startTime, this.continuous, this.easeIn, this.easeOut, this.offset, this.rotation);
   }
}
