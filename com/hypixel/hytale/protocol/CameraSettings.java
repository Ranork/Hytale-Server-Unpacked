package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3fc;

public class CameraSettings {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 13;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 21;
   public static final int MAX_SIZE = 8192049;
   @Nullable
   public Vector3fc positionOffset;
   @Nullable
   public CameraAxis yaw;
   @Nullable
   public CameraAxis pitch;

   public CameraSettings() {
   }

   public CameraSettings(@Nullable Vector3fc positionOffset, @Nullable CameraAxis yaw, @Nullable CameraAxis pitch) {
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
      if (buf.readableBytes() - offset < 21) {
         throw ProtocolException.bufferTooSmall("CameraSettings", 21, buf.readableBytes() - offset);
      } else {
         CameraSettings obj = new CameraSettings();
         byte nullBits = buf.getByte(offset);
         if ((nullBits & 1) != 0) {
            obj.positionOffset = PacketIO.readVector3f(buf, offset + 1);
         }

         if ((nullBits & 2) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 13);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 21) {
               throw ProtocolException.invalidOffset("Yaw", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 21 + varPosBase0;
            obj.yaw = CameraAxis.deserialize(buf, varPos0);
         }

         if ((nullBits & 4) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 17);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 21) {
               throw ProtocolException.invalidOffset("Pitch", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 21 + varPosBase1;
            obj.pitch = CameraAxis.deserialize(buf, varPos1);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 21;
      if ((nullBits & 2) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 13);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 21) {
            throw ProtocolException.invalidOffset("Yaw", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 21 + fieldOffset0;
         pos0 += CameraAxis.computeBytesConsumed(buf, pos0);
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 17);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 21) {
            throw ProtocolException.invalidOffset("Pitch", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 21 + fieldOffset1;
         pos1 += CameraAxis.computeBytesConsumed(buf, pos1);
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 21L;
   }

   @Nullable
   public static Vector3fc getPositionOffset(MemorySegment mem) {
      return getPositionOffset(mem, 0);
   }

   @Nullable
   public static Vector3fc getPositionOffset(MemorySegment mem, int offset) {
      return hasPositionOffset(mem, offset) ? PacketIO.readVector3f(mem, offset + 1) : null;
   }

   @Nullable
   public static CameraAxis getYaw(MemorySegment mem) {
      return getYaw(mem, 0);
   }

   @Nullable
   public static CameraAxis getYaw(MemorySegment mem, int offset) {
      return hasYaw(mem, offset) ? CameraAxis.toObject(mem, offset + getValidatedOffset(mem, offset, 13, 21, "Yaw")) : null;
   }

   @Nullable
   public static CameraAxis getPitch(MemorySegment mem) {
      return getPitch(mem, 0);
   }

   @Nullable
   public static CameraAxis getPitch(MemorySegment mem, int offset) {
      return hasPitch(mem, offset) ? CameraAxis.toObject(mem, offset + getValidatedOffset(mem, offset, 17, 21, "Pitch")) : null;
   }

   public static boolean hasPositionOffset(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasYaw(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasPitch(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 4) != 0;
   }

   private static int getValidatedOffset(MemorySegment buffer, int base, int slotPosition, int varBlockStart, String fieldName) {
      int offset = buffer.get(PacketIO.PROTO_INT, (long)(base + slotPosition));
      if (offset >= 0 && offset <= buffer.byteSize() - base - varBlockStart) {
         return varBlockStart + offset;
      } else {
         throw ProtocolException.invalidOffset(fieldName, offset, (int)buffer.byteSize());
      }
   }

   public static CameraSettings toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static CameraSettings toObject(MemorySegment mem, int offset) {
      if (offset + 21 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("CameraSettings", offset + 21, (int)mem.byteSize());
      } else {
         return new CameraSettings(
            hasPositionOffset(mem, offset) ? PacketIO.readVector3f(mem, offset + 1) : null,
            hasYaw(mem, offset) ? CameraAxis.toObject(mem, offset + getValidatedOffset(mem, offset, 13, 21, "Yaw")) : null,
            hasPitch(mem, offset) ? CameraAxis.toObject(mem, offset + getValidatedOffset(mem, offset, 17, 21, "Pitch")) : null
         );
      }
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
         PacketIO.writeVector3f(buf, this.positionOffset);
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

   public int serialize(@Nonnull MemorySegment mem, int offset) {
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

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      if (this.positionOffset != null) {
         PacketIO.writeVector3f(mem, offset + 1, this.positionOffset);
      } else {
         mem.asSlice(offset + 1, 12L).fill((byte)0);
      }

      int varOffset = offset + 21;
      if (this.yaw != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 13), varOffset - offset - 21);
         varOffset += this.yaw.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 13), -1);
      }

      if (this.pitch != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 17), varOffset - offset - 21);
         varOffset += this.pitch.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 17), -1);
      }

      return varOffset - offset;
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
            if (yawOffset < 0 || yawOffset > buffer.writerIndex() - offset - 21) {
               return ValidationResult.error("Invalid offset for Yaw");
            }

            int pos = offset + 21 + yawOffset;
            ValidationResult yawResult = CameraAxis.validateStructure(buffer, pos);
            if (!yawResult.isValid()) {
               return ValidationResult.error("Invalid Yaw: " + yawResult.error());
            }

            pos += CameraAxis.computeBytesConsumed(buffer, pos);
         }

         if ((nullBits & 4) != 0) {
            int pitchOffset = buffer.getIntLE(offset + 17);
            if (pitchOffset < 0 || pitchOffset > buffer.writerIndex() - offset - 21) {
               return ValidationResult.error("Invalid offset for Pitch");
            }

            int pos = offset + 21 + pitchOffset;
            ValidationResult pitchResult = CameraAxis.validateStructure(buffer, pos);
            if (!pitchResult.isValid()) {
               return ValidationResult.error("Invalid Pitch: " + pitchResult.error());
            }

            pos += CameraAxis.computeBytesConsumed(buffer, pos);
         }

         return ValidationResult.OK;
      }
   }

   public CameraSettings clone() {
      CameraSettings copy = new CameraSettings();
      copy.positionOffset = this.positionOffset;
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
