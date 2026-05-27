package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ModelTransform {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 49;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 49;
   public static final int MAX_SIZE = 49;
   @Nullable
   public Position position;
   @Nullable
   public Direction bodyOrientation;
   @Nullable
   public Direction lookOrientation;

   public ModelTransform() {
   }

   public ModelTransform(@Nullable Position position, @Nullable Direction bodyOrientation, @Nullable Direction lookOrientation) {
      this.position = position;
      this.bodyOrientation = bodyOrientation;
      this.lookOrientation = lookOrientation;
   }

   public ModelTransform(@Nonnull ModelTransform other) {
      this.position = other.position;
      this.bodyOrientation = other.bodyOrientation;
      this.lookOrientation = other.lookOrientation;
   }

   @Nonnull
   public static ModelTransform deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 49) {
         throw ProtocolException.bufferTooSmall("ModelTransform", 49, buf.readableBytes() - offset);
      } else {
         ModelTransform obj = new ModelTransform();
         byte nullBits = buf.getByte(offset);
         if ((nullBits & 1) != 0) {
            obj.position = Position.deserialize(buf, offset + 1);
         }

         if ((nullBits & 2) != 0) {
            obj.bodyOrientation = Direction.deserialize(buf, offset + 25);
         }

         if ((nullBits & 4) != 0) {
            obj.lookOrientation = Direction.deserialize(buf, offset + 37);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 49;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 49L;
   }

   @Nullable
   public static Position getPosition(MemorySegment mem) {
      return getPosition(mem, 0);
   }

   @Nullable
   public static Position getPosition(MemorySegment mem, int offset) {
      return hasPosition(mem, offset) ? Position.toObject(mem, offset + 1) : null;
   }

   @Nullable
   public static Direction getBodyOrientation(MemorySegment mem) {
      return getBodyOrientation(mem, 0);
   }

   @Nullable
   public static Direction getBodyOrientation(MemorySegment mem, int offset) {
      return hasBodyOrientation(mem, offset) ? Direction.toObject(mem, offset + 25) : null;
   }

   @Nullable
   public static Direction getLookOrientation(MemorySegment mem) {
      return getLookOrientation(mem, 0);
   }

   @Nullable
   public static Direction getLookOrientation(MemorySegment mem, int offset) {
      return hasLookOrientation(mem, offset) ? Direction.toObject(mem, offset + 37) : null;
   }

   public static boolean hasPosition(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasBodyOrientation(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasLookOrientation(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 4) != 0;
   }

   public static ModelTransform toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static ModelTransform toObject(MemorySegment mem, int offset) {
      if (offset + 49 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("ModelTransform", offset + 49, (int)mem.byteSize());
      } else {
         return new ModelTransform(
            hasPosition(mem, offset) ? Position.toObject(mem, offset + 1) : null,
            hasBodyOrientation(mem, offset) ? Direction.toObject(mem, offset + 25) : null,
            hasLookOrientation(mem, offset) ? Direction.toObject(mem, offset + 37) : null
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.position != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.bodyOrientation != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.lookOrientation != null) {
         nullBits = (byte)(nullBits | 4);
      }

      buf.writeByte(nullBits);
      if (this.position != null) {
         this.position.serialize(buf);
      } else {
         buf.writeZero(24);
      }

      if (this.bodyOrientation != null) {
         this.bodyOrientation.serialize(buf);
      } else {
         buf.writeZero(12);
      }

      if (this.lookOrientation != null) {
         this.lookOrientation.serialize(buf);
      } else {
         buf.writeZero(12);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.position != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.bodyOrientation != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.lookOrientation != null) {
         nullBits = (byte)(nullBits | 4);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      if (this.position != null) {
         this.position.serialize(mem, offset + 1);
      } else {
         mem.asSlice(offset + 1, 24L).fill((byte)0);
      }

      if (this.bodyOrientation != null) {
         this.bodyOrientation.serialize(mem, offset + 25);
      } else {
         mem.asSlice(offset + 25, 12L).fill((byte)0);
      }

      if (this.lookOrientation != null) {
         this.lookOrientation.serialize(mem, offset + 37);
      } else {
         mem.asSlice(offset + 37, 12L).fill((byte)0);
      }

      return 49;
   }

   public int computeSize() {
      return 49;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 49) {
         return ValidationResult.error("Buffer too small: expected at least 49 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         return ValidationResult.OK;
      }
   }

   public ModelTransform clone() {
      ModelTransform copy = new ModelTransform();
      copy.position = this.position != null ? this.position.clone() : null;
      copy.bodyOrientation = this.bodyOrientation != null ? this.bodyOrientation.clone() : null;
      copy.lookOrientation = this.lookOrientation != null ? this.lookOrientation.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ModelTransform other)
            ? false
            : Objects.equals(this.position, other.position)
               && Objects.equals(this.bodyOrientation, other.bodyOrientation)
               && Objects.equals(this.lookOrientation, other.lookOrientation);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.position, this.bodyOrientation, this.lookOrientation);
   }
}
