package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
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
         int varPos0 = offset + 28 + buf.getIntLE(offset + 20);
         obj.offset = OffsetNoise.deserialize(buf, varPos0);
      }

      if ((nullBits & 8) != 0) {
         int varPos1 = offset + 28 + buf.getIntLE(offset + 24);
         obj.rotation = RotationNoise.deserialize(buf, varPos1);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 28;
      if ((nullBits & 4) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 20);
         int pos0 = offset + 28 + fieldOffset0;
         pos0 += OffsetNoise.computeBytesConsumed(buf, pos0);
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 8) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 24);
         int pos1 = offset + 28 + fieldOffset1;
         pos1 += RotationNoise.computeBytesConsumed(buf, pos1);
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
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
            if (offsetOffset < 0) {
               return ValidationResult.error("Invalid offset for Offset");
            }

            int pos = offset + 28 + offsetOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Offset");
            }

            ValidationResult offsetResult = OffsetNoise.validateStructure(buffer, pos);
            if (!offsetResult.isValid()) {
               return ValidationResult.error("Invalid Offset: " + offsetResult.error());
            }

            pos += OffsetNoise.computeBytesConsumed(buffer, pos);
         }

         if ((nullBits & 8) != 0) {
            int rotationOffset = buffer.getIntLE(offset + 24);
            if (rotationOffset < 0) {
               return ValidationResult.error("Invalid offset for Rotation");
            }

            int posx = offset + 28 + rotationOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Rotation");
            }

            ValidationResult rotationResult = RotationNoise.validateStructure(buffer, posx);
            if (!rotationResult.isValid()) {
               return ValidationResult.error("Invalid Rotation: " + rotationResult.error());
            }

            posx += RotationNoise.computeBytesConsumed(buffer, posx);
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
