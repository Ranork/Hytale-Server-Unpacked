package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Transform {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 37;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 37;
   public static final int MAX_SIZE = 37;
   @Nullable
   public Position position;
   @Nullable
   public Direction orientation;

   public Transform() {
   }

   public Transform(@Nullable Position position, @Nullable Direction orientation) {
      this.position = position;
      this.orientation = orientation;
   }

   public Transform(@Nonnull Transform other) {
      this.position = other.position;
      this.orientation = other.orientation;
   }

   @Nonnull
   public static Transform deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 37) {
         throw ProtocolException.bufferTooSmall("Transform", 37, buf.readableBytes() - offset);
      } else {
         Transform obj = new Transform();
         byte nullBits = buf.getByte(offset);
         if ((nullBits & 1) != 0) {
            obj.position = Position.deserialize(buf, offset + 1);
         }

         if ((nullBits & 2) != 0) {
            obj.orientation = Direction.deserialize(buf, offset + 25);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 37;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 37L;
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
   public static Direction getOrientation(MemorySegment mem) {
      return getOrientation(mem, 0);
   }

   @Nullable
   public static Direction getOrientation(MemorySegment mem, int offset) {
      return hasOrientation(mem, offset) ? Direction.toObject(mem, offset + 25) : null;
   }

   public static boolean hasPosition(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasOrientation(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static Transform toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static Transform toObject(MemorySegment mem, int offset) {
      if (offset + 37 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("Transform", offset + 37, (int)mem.byteSize());
      } else {
         return new Transform(
            hasPosition(mem, offset) ? Position.toObject(mem, offset + 1) : null, hasOrientation(mem, offset) ? Direction.toObject(mem, offset + 25) : null
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.position != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.orientation != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      if (this.position != null) {
         this.position.serialize(buf);
      } else {
         buf.writeZero(24);
      }

      if (this.orientation != null) {
         this.orientation.serialize(buf);
      } else {
         buf.writeZero(12);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.position != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.orientation != null) {
         nullBits = (byte)(nullBits | 2);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      if (this.position != null) {
         this.position.serialize(mem, offset + 1);
      } else {
         mem.asSlice(offset + 1, 24L).fill((byte)0);
      }

      if (this.orientation != null) {
         this.orientation.serialize(mem, offset + 25);
      } else {
         mem.asSlice(offset + 25, 12L).fill((byte)0);
      }

      return 37;
   }

   public int computeSize() {
      return 37;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 37) {
         return ValidationResult.error("Buffer too small: expected at least 37 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         return ValidationResult.OK;
      }
   }

   public Transform clone() {
      Transform copy = new Transform();
      copy.position = this.position != null ? this.position.clone() : null;
      copy.orientation = this.orientation != null ? this.orientation.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof Transform other)
            ? false
            : Objects.equals(this.position, other.position) && Objects.equals(this.orientation, other.orientation);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.position, this.orientation);
   }
}
