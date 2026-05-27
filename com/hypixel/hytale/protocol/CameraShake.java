package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;

public class CameraShake {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 0;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 8;
   public static final int MAX_SIZE = 1130496176;
   @Nonnull
   public CameraShakeConfig firstPerson = new CameraShakeConfig();
   @Nonnull
   public CameraShakeConfig thirdPerson = new CameraShakeConfig();

   public CameraShake() {
   }

   public CameraShake(@Nonnull CameraShakeConfig firstPerson, @Nonnull CameraShakeConfig thirdPerson) {
      this.firstPerson = firstPerson;
      this.thirdPerson = thirdPerson;
   }

   public CameraShake(@Nonnull CameraShake other) {
      this.firstPerson = other.firstPerson;
      this.thirdPerson = other.thirdPerson;
   }

   @Nonnull
   public static CameraShake deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 8) {
         throw ProtocolException.bufferTooSmall("CameraShake", 8, buf.readableBytes() - offset);
      } else {
         CameraShake obj = new CameraShake();
         int varPosBase0 = buf.getIntLE(offset + 0);
         if (varPosBase0 >= 0 && varPosBase0 <= buf.writerIndex() - offset - 8) {
            int varPos0 = offset + 8 + varPosBase0;
            obj.firstPerson = CameraShakeConfig.deserialize(buf, varPos0);
            varPosBase0 = buf.getIntLE(offset + 4);
            if (varPosBase0 >= 0 && varPosBase0 <= buf.writerIndex() - offset - 8) {
               varPos0 = offset + 8 + varPosBase0;
               obj.thirdPerson = CameraShakeConfig.deserialize(buf, varPos0);
               return obj;
            } else {
               throw ProtocolException.invalidOffset("ThirdPerson", varPosBase0, buf.readableBytes());
            }
         } else {
            throw ProtocolException.invalidOffset("FirstPerson", varPosBase0, buf.readableBytes());
         }
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      int maxEnd = 8;
      int fieldOffset0 = buf.getIntLE(offset + 0);
      if (fieldOffset0 >= 0 && fieldOffset0 <= buf.writerIndex() - offset - 8) {
         int pos0 = offset + 8 + fieldOffset0;
         pos0 += CameraShakeConfig.computeBytesConsumed(buf, pos0);
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }

         fieldOffset0 = buf.getIntLE(offset + 4);
         if (fieldOffset0 >= 0 && fieldOffset0 <= buf.writerIndex() - offset - 8) {
            pos0 = offset + 8 + fieldOffset0;
            pos0 += CameraShakeConfig.computeBytesConsumed(buf, pos0);
            if (pos0 - offset > maxEnd) {
               maxEnd = pos0 - offset;
            }

            return maxEnd;
         } else {
            throw ProtocolException.invalidOffset("ThirdPerson", fieldOffset0, maxEnd);
         }
      } else {
         throw ProtocolException.invalidOffset("FirstPerson", fieldOffset0, maxEnd);
      }
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 8L;
   }

   public static CameraShakeConfig getFirstPerson(MemorySegment mem) {
      return getFirstPerson(mem, 0);
   }

   public static CameraShakeConfig getFirstPerson(MemorySegment mem, int offset) {
      return CameraShakeConfig.toObject(mem, offset + getValidatedOffset(mem, offset, 0, 8, "FirstPerson"));
   }

   public static CameraShakeConfig getThirdPerson(MemorySegment mem) {
      return getThirdPerson(mem, 0);
   }

   public static CameraShakeConfig getThirdPerson(MemorySegment mem, int offset) {
      return CameraShakeConfig.toObject(mem, offset + getValidatedOffset(mem, offset, 4, 8, "ThirdPerson"));
   }

   private static int getValidatedOffset(MemorySegment buffer, int base, int slotPosition, int varBlockStart, String fieldName) {
      int offset = buffer.get(PacketIO.PROTO_INT, (long)(base + slotPosition));
      if (offset >= 0 && offset <= buffer.byteSize() - base - varBlockStart) {
         return varBlockStart + offset;
      } else {
         throw ProtocolException.invalidOffset(fieldName, offset, (int)buffer.byteSize());
      }
   }

   public static CameraShake toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static CameraShake toObject(MemorySegment mem, int offset) {
      if (offset + 8 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("CameraShake", offset + 8, (int)mem.byteSize());
      } else {
         return new CameraShake(
            CameraShakeConfig.toObject(mem, offset + getValidatedOffset(mem, offset, 0, 8, "FirstPerson")),
            CameraShakeConfig.toObject(mem, offset + getValidatedOffset(mem, offset, 4, 8, "ThirdPerson"))
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      int firstPersonOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int thirdPersonOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      buf.setIntLE(firstPersonOffsetSlot, buf.writerIndex() - varBlockStart);
      this.firstPerson.serialize(buf);
      buf.setIntLE(thirdPersonOffsetSlot, buf.writerIndex() - varBlockStart);
      this.thirdPerson.serialize(buf);
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      int varOffset = offset + 8;
      mem.set(PacketIO.PROTO_INT, (long)(offset + 0), varOffset - offset - 8);
      varOffset += this.firstPerson.serialize(mem, varOffset);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 4), varOffset - offset - 8);
      varOffset += this.thirdPerson.serialize(mem, varOffset);
      return varOffset - offset;
   }

   public int computeSize() {
      int size = 8;
      size += this.firstPerson.computeSize();
      return size + this.thirdPerson.computeSize();
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 8) {
         return ValidationResult.error("Buffer too small: expected at least 8 bytes");
      } else {
         int firstPersonOffset = buffer.getIntLE(offset + 0);
         if (firstPersonOffset >= 0 && firstPersonOffset <= buffer.writerIndex() - offset - 8) {
            int pos = offset + 8 + firstPersonOffset;
            ValidationResult firstPersonResult = CameraShakeConfig.validateStructure(buffer, pos);
            if (!firstPersonResult.isValid()) {
               return ValidationResult.error("Invalid FirstPerson: " + firstPersonResult.error());
            } else {
               pos += CameraShakeConfig.computeBytesConsumed(buffer, pos);
               firstPersonOffset = buffer.getIntLE(offset + 4);
               if (firstPersonOffset >= 0 && firstPersonOffset <= buffer.writerIndex() - offset - 8) {
                  pos = offset + 8 + firstPersonOffset;
                  firstPersonResult = CameraShakeConfig.validateStructure(buffer, pos);
                  if (!firstPersonResult.isValid()) {
                     return ValidationResult.error("Invalid ThirdPerson: " + firstPersonResult.error());
                  } else {
                     pos += CameraShakeConfig.computeBytesConsumed(buffer, pos);
                     return ValidationResult.OK;
                  }
               } else {
                  return ValidationResult.error("Invalid offset for ThirdPerson");
               }
            }
         } else {
            return ValidationResult.error("Invalid offset for FirstPerson");
         }
      }
   }

   public CameraShake clone() {
      CameraShake copy = new CameraShake();
      copy.firstPerson = this.firstPerson.clone();
      copy.thirdPerson = this.thirdPerson.clone();
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof CameraShake other)
            ? false
            : Objects.equals(this.firstPerson, other.firstPerson) && Objects.equals(this.thirdPerson, other.thirdPerson);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.firstPerson, this.thirdPerson);
   }
}
