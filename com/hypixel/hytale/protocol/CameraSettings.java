package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CameraSettings {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 13;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 21;
   public static final int MAX_SIZE = 8192049;
   @Nullable
   public Vector3f positionOffset;
   @Nullable
   public CameraAxis yaw;
   @Nullable
   public CameraAxis pitch;

   public CameraSettings() {
   }

   public CameraSettings(@Nullable Vector3f positionOffset, @Nullable CameraAxis yaw, @Nullable CameraAxis pitch) {
      this.positionOffset = positionOffset;
      this.yaw = yaw;
      this.pitch = pitch;
   }

   public CameraSettings(@Nonnull CameraSettings other) {
      this.positionOffset = other.positionOffset;
      this.yaw = other.yaw;
      this.pitch = other.pitch;
   }

   @Nonnull
   public static CameraSettings deserialize(@Nonnull ByteBuf buf, int offset) {
      CameraSettings obj = new CameraSettings();
      byte nullBits = buf.getByte(offset);
      if ((nullBits & 1) != 0) {
         obj.positionOffset = Vector3f.deserialize(buf, offset + 1);
      }

      if ((nullBits & 2) != 0) {
         int varPos0 = offset + 21 + buf.getIntLE(offset + 13);
         obj.yaw = CameraAxis.deserialize(buf, varPos0);
      }

      if ((nullBits & 4) != 0) {
         int varPos1 = offset + 21 + buf.getIntLE(offset + 17);
         obj.pitch = CameraAxis.deserialize(buf, varPos1);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 21;
      if ((nullBits & 2) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 13);
         int pos0 = offset + 21 + fieldOffset0;
         pos0 += CameraAxis.computeBytesConsumed(buf, pos0);
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 17);
         int pos1 = offset + 21 + fieldOffset1;
         pos1 += CameraAxis.computeBytesConsumed(buf, pos1);
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.positionOffset != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.yaw != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.pitch != null) {
         nullBits = (byte)(nullBits | 4);
      }

      buf.writeByte(nullBits);
      if (this.positionOffset != null) {
         this.positionOffset.serialize(buf);
      } else {
         buf.writeZero(12);
      }

      int yawOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int pitchOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.yaw != null) {
         buf.setIntLE(yawOffsetSlot, buf.writerIndex() - varBlockStart);
         this.yaw.serialize(buf);
      } else {
         buf.setIntLE(yawOffsetSlot, -1);
      }

      if (this.pitch != null) {
         buf.setIntLE(pitchOffsetSlot, buf.writerIndex() - varBlockStart);
         this.pitch.serialize(buf);
      } else {
         buf.setIntLE(pitchOffsetSlot, -1);
      }
   }

   public int computeSize() {
      int size = 21;
      if (this.yaw != null) {
         size += this.yaw.computeSize();
      }

      if (this.pitch != null) {
         size += this.pitch.computeSize();
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 21) {
         return ValidationResult.error("Buffer too small: expected at least 21 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 2) != 0) {
            int yawOffset = buffer.getIntLE(offset + 13);
            if (yawOffset < 0) {
               return ValidationResult.error("Invalid offset for Yaw");
            }

            int pos = offset + 21 + yawOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Yaw");
            }

            ValidationResult yawResult = CameraAxis.validateStructure(buffer, pos);
            if (!yawResult.isValid()) {
               return ValidationResult.error("Invalid Yaw: " + yawResult.error());
            }

            pos += CameraAxis.computeBytesConsumed(buffer, pos);
         }

         if ((nullBits & 4) != 0) {
            int pitchOffset = buffer.getIntLE(offset + 17);
            if (pitchOffset < 0) {
               return ValidationResult.error("Invalid offset for Pitch");
            }

            int posx = offset + 21 + pitchOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Pitch");
            }

            ValidationResult pitchResult = CameraAxis.validateStructure(buffer, posx);
            if (!pitchResult.isValid()) {
               return ValidationResult.error("Invalid Pitch: " + pitchResult.error());
            }

            posx += CameraAxis.computeBytesConsumed(buffer, posx);
         }

         return ValidationResult.OK;
      }
   }

   public CameraSettings clone() {
      CameraSettings copy = new CameraSettings();
      copy.positionOffset = this.positionOffset != null ? this.positionOffset.clone() : null;
      copy.yaw = this.yaw != null ? this.yaw.clone() : null;
      copy.pitch = this.pitch != null ? this.pitch.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof CameraSettings other)
            ? false
            : Objects.equals(this.positionOffset, other.positionOffset) && Objects.equals(this.yaw, other.yaw) && Objects.equals(this.pitch, other.pitch);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.positionOffset, this.yaw, this.pitch);
   }
}
