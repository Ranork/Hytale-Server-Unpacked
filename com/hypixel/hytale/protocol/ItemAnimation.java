package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemAnimation {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 12;
   public static final int VARIABLE_FIELD_COUNT = 5;
   public static final int VARIABLE_BLOCK_START = 32;
   public static final int MAX_SIZE = 81920057;
   @Nullable
   public String thirdPerson;
   @Nullable
   public String thirdPersonMoving;
   @Nullable
   public String thirdPersonFace;
   @Nullable
   public String firstPerson;
   @Nullable
   public String firstPersonOverride;
   public boolean keepPreviousFirstPersonAnimation;
   public float speed;
   public float blendingDuration = 0.2F;
   public boolean looping;
   public boolean clipsGeometry;

   public ItemAnimation() {
   }

   public ItemAnimation(
      @Nullable String thirdPerson,
      @Nullable String thirdPersonMoving,
      @Nullable String thirdPersonFace,
      @Nullable String firstPerson,
      @Nullable String firstPersonOverride,
      boolean keepPreviousFirstPersonAnimation,
      float speed,
      float blendingDuration,
      boolean looping,
      boolean clipsGeometry
   ) {
      this.thirdPerson = thirdPerson;
      this.thirdPersonMoving = thirdPersonMoving;
      this.thirdPersonFace = thirdPersonFace;
      this.firstPerson = firstPerson;
      this.firstPersonOverride = firstPersonOverride;
      this.keepPreviousFirstPersonAnimation = keepPreviousFirstPersonAnimation;
      this.speed = speed;
      this.blendingDuration = blendingDuration;
      this.looping = looping;
      this.clipsGeometry = clipsGeometry;
   }

   public ItemAnimation(@Nonnull ItemAnimation other) {
      this.thirdPerson = other.thirdPerson;
      this.thirdPersonMoving = other.thirdPersonMoving;
      this.thirdPersonFace = other.thirdPersonFace;
      this.firstPerson = other.firstPerson;
      this.firstPersonOverride = other.firstPersonOverride;
      this.keepPreviousFirstPersonAnimation = other.keepPreviousFirstPersonAnimation;
      this.speed = other.speed;
      this.blendingDuration = other.blendingDuration;
      this.looping = other.looping;
      this.clipsGeometry = other.clipsGeometry;
   }

   @Nonnull
   public static ItemAnimation deserialize(@Nonnull ByteBuf buf, int offset) {
      ItemAnimation obj = new ItemAnimation();
      byte nullBits = buf.getByte(offset);
      obj.keepPreviousFirstPersonAnimation = buf.getByte(offset + 1) != 0;
      obj.speed = buf.getFloatLE(offset + 2);
      obj.blendingDuration = buf.getFloatLE(offset + 6);
      obj.looping = buf.getByte(offset + 10) != 0;
      obj.clipsGeometry = buf.getByte(offset + 11) != 0;
      if ((nullBits & 1) != 0) {
         int varPos0 = offset + 32 + buf.getIntLE(offset + 12);
         int thirdPersonLen = VarInt.peek(buf, varPos0);
         if (thirdPersonLen < 0) {
            throw ProtocolException.negativeLength("ThirdPerson", thirdPersonLen);
         }

         if (thirdPersonLen > 4096000) {
            throw ProtocolException.stringTooLong("ThirdPerson", thirdPersonLen, 4096000);
         }

         obj.thirdPerson = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
      }

      if ((nullBits & 2) != 0) {
         int varPos1 = offset + 32 + buf.getIntLE(offset + 16);
         int thirdPersonMovingLen = VarInt.peek(buf, varPos1);
         if (thirdPersonMovingLen < 0) {
            throw ProtocolException.negativeLength("ThirdPersonMoving", thirdPersonMovingLen);
         }

         if (thirdPersonMovingLen > 4096000) {
            throw ProtocolException.stringTooLong("ThirdPersonMoving", thirdPersonMovingLen, 4096000);
         }

         obj.thirdPersonMoving = PacketIO.readVarString(buf, varPos1, PacketIO.UTF8);
      }

      if ((nullBits & 4) != 0) {
         int varPos2 = offset + 32 + buf.getIntLE(offset + 20);
         int thirdPersonFaceLen = VarInt.peek(buf, varPos2);
         if (thirdPersonFaceLen < 0) {
            throw ProtocolException.negativeLength("ThirdPersonFace", thirdPersonFaceLen);
         }

         if (thirdPersonFaceLen > 4096000) {
            throw ProtocolException.stringTooLong("ThirdPersonFace", thirdPersonFaceLen, 4096000);
         }

         obj.thirdPersonFace = PacketIO.readVarString(buf, varPos2, PacketIO.UTF8);
      }

      if ((nullBits & 8) != 0) {
         int varPos3 = offset + 32 + buf.getIntLE(offset + 24);
         int firstPersonLen = VarInt.peek(buf, varPos3);
         if (firstPersonLen < 0) {
            throw ProtocolException.negativeLength("FirstPerson", firstPersonLen);
         }

         if (firstPersonLen > 4096000) {
            throw ProtocolException.stringTooLong("FirstPerson", firstPersonLen, 4096000);
         }

         obj.firstPerson = PacketIO.readVarString(buf, varPos3, PacketIO.UTF8);
      }

      if ((nullBits & 16) != 0) {
         int varPos4 = offset + 32 + buf.getIntLE(offset + 28);
         int firstPersonOverrideLen = VarInt.peek(buf, varPos4);
         if (firstPersonOverrideLen < 0) {
            throw ProtocolException.negativeLength("FirstPersonOverride", firstPersonOverrideLen);
         }

         if (firstPersonOverrideLen > 4096000) {
            throw ProtocolException.stringTooLong("FirstPersonOverride", firstPersonOverrideLen, 4096000);
         }

         obj.firstPersonOverride = PacketIO.readVarString(buf, varPos4, PacketIO.UTF8);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 32;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 12);
         int pos0 = offset + 32 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 16);
         int pos1 = offset + 32 + fieldOffset1;
         int sl = VarInt.peek(buf, pos1);
         pos1 += VarInt.length(buf, pos1) + sl;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 20);
         int pos2 = offset + 32 + fieldOffset2;
         int sl = VarInt.peek(buf, pos2);
         pos2 += VarInt.length(buf, pos2) + sl;
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      if ((nullBits & 8) != 0) {
         int fieldOffset3 = buf.getIntLE(offset + 24);
         int pos3 = offset + 32 + fieldOffset3;
         int sl = VarInt.peek(buf, pos3);
         pos3 += VarInt.length(buf, pos3) + sl;
         if (pos3 - offset > maxEnd) {
            maxEnd = pos3 - offset;
         }
      }

      if ((nullBits & 16) != 0) {
         int fieldOffset4 = buf.getIntLE(offset + 28);
         int pos4 = offset + 32 + fieldOffset4;
         int sl = VarInt.peek(buf, pos4);
         pos4 += VarInt.length(buf, pos4) + sl;
         if (pos4 - offset > maxEnd) {
            maxEnd = pos4 - offset;
         }
      }

      return maxEnd;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.thirdPerson != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.thirdPersonMoving != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.thirdPersonFace != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.firstPerson != null) {
         nullBits = (byte)(nullBits | 8);
      }

      if (this.firstPersonOverride != null) {
         nullBits = (byte)(nullBits | 16);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.keepPreviousFirstPersonAnimation ? 1 : 0);
      buf.writeFloatLE(this.speed);
      buf.writeFloatLE(this.blendingDuration);
      buf.writeByte(this.looping ? 1 : 0);
      buf.writeByte(this.clipsGeometry ? 1 : 0);
      int thirdPersonOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int thirdPersonMovingOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int thirdPersonFaceOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int firstPersonOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int firstPersonOverrideOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.thirdPerson != null) {
         buf.setIntLE(thirdPersonOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.thirdPerson, 4096000);
      } else {
         buf.setIntLE(thirdPersonOffsetSlot, -1);
      }

      if (this.thirdPersonMoving != null) {
         buf.setIntLE(thirdPersonMovingOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.thirdPersonMoving, 4096000);
      } else {
         buf.setIntLE(thirdPersonMovingOffsetSlot, -1);
      }

      if (this.thirdPersonFace != null) {
         buf.setIntLE(thirdPersonFaceOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.thirdPersonFace, 4096000);
      } else {
         buf.setIntLE(thirdPersonFaceOffsetSlot, -1);
      }

      if (this.firstPerson != null) {
         buf.setIntLE(firstPersonOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.firstPerson, 4096000);
      } else {
         buf.setIntLE(firstPersonOffsetSlot, -1);
      }

      if (this.firstPersonOverride != null) {
         buf.setIntLE(firstPersonOverrideOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.firstPersonOverride, 4096000);
      } else {
         buf.setIntLE(firstPersonOverrideOffsetSlot, -1);
      }
   }

   public int computeSize() {
      int size = 32;
      if (this.thirdPerson != null) {
         size += PacketIO.stringSize(this.thirdPerson);
      }

      if (this.thirdPersonMoving != null) {
         size += PacketIO.stringSize(this.thirdPersonMoving);
      }

      if (this.thirdPersonFace != null) {
         size += PacketIO.stringSize(this.thirdPersonFace);
      }

      if (this.firstPerson != null) {
         size += PacketIO.stringSize(this.firstPerson);
      }

      if (this.firstPersonOverride != null) {
         size += PacketIO.stringSize(this.firstPersonOverride);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 32) {
         return ValidationResult.error("Buffer too small: expected at least 32 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int thirdPersonOffset = buffer.getIntLE(offset + 12);
            if (thirdPersonOffset < 0) {
               return ValidationResult.error("Invalid offset for ThirdPerson");
            }

            int pos = offset + 32 + thirdPersonOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for ThirdPerson");
            }

            int thirdPersonLen = VarInt.peek(buffer, pos);
            if (thirdPersonLen < 0) {
               return ValidationResult.error("Invalid string length for ThirdPerson");
            }

            if (thirdPersonLen > 4096000) {
               return ValidationResult.error("ThirdPerson exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += thirdPersonLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading ThirdPerson");
            }
         }

         if ((nullBits & 2) != 0) {
            int thirdPersonMovingOffset = buffer.getIntLE(offset + 16);
            if (thirdPersonMovingOffset < 0) {
               return ValidationResult.error("Invalid offset for ThirdPersonMoving");
            }

            int posx = offset + 32 + thirdPersonMovingOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for ThirdPersonMoving");
            }

            int thirdPersonMovingLen = VarInt.peek(buffer, posx);
            if (thirdPersonMovingLen < 0) {
               return ValidationResult.error("Invalid string length for ThirdPersonMoving");
            }

            if (thirdPersonMovingLen > 4096000) {
               return ValidationResult.error("ThirdPersonMoving exceeds max length 4096000");
            }

            posx += VarInt.length(buffer, posx);
            posx += thirdPersonMovingLen;
            if (posx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading ThirdPersonMoving");
            }
         }

         if ((nullBits & 4) != 0) {
            int thirdPersonFaceOffset = buffer.getIntLE(offset + 20);
            if (thirdPersonFaceOffset < 0) {
               return ValidationResult.error("Invalid offset for ThirdPersonFace");
            }

            int posxx = offset + 32 + thirdPersonFaceOffset;
            if (posxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for ThirdPersonFace");
            }

            int thirdPersonFaceLen = VarInt.peek(buffer, posxx);
            if (thirdPersonFaceLen < 0) {
               return ValidationResult.error("Invalid string length for ThirdPersonFace");
            }

            if (thirdPersonFaceLen > 4096000) {
               return ValidationResult.error("ThirdPersonFace exceeds max length 4096000");
            }

            posxx += VarInt.length(buffer, posxx);
            posxx += thirdPersonFaceLen;
            if (posxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading ThirdPersonFace");
            }
         }

         if ((nullBits & 8) != 0) {
            int firstPersonOffset = buffer.getIntLE(offset + 24);
            if (firstPersonOffset < 0) {
               return ValidationResult.error("Invalid offset for FirstPerson");
            }

            int posxxx = offset + 32 + firstPersonOffset;
            if (posxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for FirstPerson");
            }

            int firstPersonLen = VarInt.peek(buffer, posxxx);
            if (firstPersonLen < 0) {
               return ValidationResult.error("Invalid string length for FirstPerson");
            }

            if (firstPersonLen > 4096000) {
               return ValidationResult.error("FirstPerson exceeds max length 4096000");
            }

            posxxx += VarInt.length(buffer, posxxx);
            posxxx += firstPersonLen;
            if (posxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading FirstPerson");
            }
         }

         if ((nullBits & 16) != 0) {
            int firstPersonOverrideOffset = buffer.getIntLE(offset + 28);
            if (firstPersonOverrideOffset < 0) {
               return ValidationResult.error("Invalid offset for FirstPersonOverride");
            }

            int posxxxx = offset + 32 + firstPersonOverrideOffset;
            if (posxxxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for FirstPersonOverride");
            }

            int firstPersonOverrideLen = VarInt.peek(buffer, posxxxx);
            if (firstPersonOverrideLen < 0) {
               return ValidationResult.error("Invalid string length for FirstPersonOverride");
            }

            if (firstPersonOverrideLen > 4096000) {
               return ValidationResult.error("FirstPersonOverride exceeds max length 4096000");
            }

            posxxxx += VarInt.length(buffer, posxxxx);
            posxxxx += firstPersonOverrideLen;
            if (posxxxx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading FirstPersonOverride");
            }
         }

         return ValidationResult.OK;
      }
   }

   public ItemAnimation clone() {
      ItemAnimation copy = new ItemAnimation();
      copy.thirdPerson = this.thirdPerson;
      copy.thirdPersonMoving = this.thirdPersonMoving;
      copy.thirdPersonFace = this.thirdPersonFace;
      copy.firstPerson = this.firstPerson;
      copy.firstPersonOverride = this.firstPersonOverride;
      copy.keepPreviousFirstPersonAnimation = this.keepPreviousFirstPersonAnimation;
      copy.speed = this.speed;
      copy.blendingDuration = this.blendingDuration;
      copy.looping = this.looping;
      copy.clipsGeometry = this.clipsGeometry;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ItemAnimation other)
            ? false
            : Objects.equals(this.thirdPerson, other.thirdPerson)
               && Objects.equals(this.thirdPersonMoving, other.thirdPersonMoving)
               && Objects.equals(this.thirdPersonFace, other.thirdPersonFace)
               && Objects.equals(this.firstPerson, other.firstPerson)
               && Objects.equals(this.firstPersonOverride, other.firstPersonOverride)
               && this.keepPreviousFirstPersonAnimation == other.keepPreviousFirstPersonAnimation
               && this.speed == other.speed
               && this.blendingDuration == other.blendingDuration
               && this.looping == other.looping
               && this.clipsGeometry == other.clipsGeometry;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(
         this.thirdPerson,
         this.thirdPersonMoving,
         this.thirdPersonFace,
         this.firstPerson,
         this.firstPersonOverride,
         this.keepPreviousFirstPersonAnimation,
         this.speed,
         this.blendingDuration,
         this.looping,
         this.clipsGeometry
      );
   }
}
