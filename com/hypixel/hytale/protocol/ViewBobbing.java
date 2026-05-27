package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ViewBobbing {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 1;
   public static final int MAX_SIZE = 565248085;
   @Nullable
   public CameraShakeConfig firstPerson;

   public ViewBobbing() {
   }

   public ViewBobbing(@Nullable CameraShakeConfig firstPerson) {
      this.firstPerson = firstPerson;
   }

   public ViewBobbing(@Nonnull ViewBobbing other) {
      this.firstPerson = other.firstPerson;
   }

   @Nonnull
   public static ViewBobbing deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 1) {
         throw ProtocolException.bufferTooSmall("ViewBobbing", 1, buf.readableBytes() - offset);
      } else {
         ViewBobbing obj = new ViewBobbing();
         byte nullBits = buf.getByte(offset);
         int pos = offset + 1;
         if ((nullBits & 1) != 0) {
            obj.firstPerson = CameraShakeConfig.deserialize(buf, pos);
            pos += CameraShakeConfig.computeBytesConsumed(buf, pos);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 1;
      if ((nullBits & 1) != 0) {
         pos += CameraShakeConfig.computeBytesConsumed(buf, pos);
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 1L;
   }

   @Nullable
   public static CameraShakeConfig getFirstPerson(MemorySegment mem) {
      return getFirstPerson(mem, 0);
   }

   @Nullable
   public static CameraShakeConfig getFirstPerson(MemorySegment mem, int offset) {
      return hasFirstPerson(mem, offset) ? CameraShakeConfig.toObject(mem, offset + 1) : null;
   }

   public static boolean hasFirstPerson(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static ViewBobbing toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static ViewBobbing toObject(MemorySegment mem, int offset) {
      if (offset + 1 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("ViewBobbing", offset + 1, (int)mem.byteSize());
      } else {
         return new ViewBobbing(hasFirstPerson(mem, offset) ? CameraShakeConfig.toObject(mem, offset + 1) : null);
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.firstPerson != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      if (this.firstPerson != null) {
         this.firstPerson.serialize(buf);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.firstPerson != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      int varOffset = offset + 1;
      if (this.firstPerson != null) {
         varOffset += this.firstPerson.serialize(mem, varOffset);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 1;
      if (this.firstPerson != null) {
         size += this.firstPerson.computeSize();
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 1) {
         return ValidationResult.error("Buffer too small: expected at least 1 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 1;
         if ((nullBits & 1) != 0) {
            ValidationResult firstPersonResult = CameraShakeConfig.validateStructure(buffer, pos);
            if (!firstPersonResult.isValid()) {
               return ValidationResult.error("Invalid FirstPerson: " + firstPersonResult.error());
            }

            pos += CameraShakeConfig.computeBytesConsumed(buffer, pos);
         }

         return ValidationResult.OK;
      }
   }

   public ViewBobbing clone() {
      ViewBobbing copy = new ViewBobbing();
      copy.firstPerson = this.firstPerson != null ? this.firstPerson.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof ViewBobbing other ? Objects.equals(this.firstPerson, other.firstPerson) : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.firstPerson);
   }
}
