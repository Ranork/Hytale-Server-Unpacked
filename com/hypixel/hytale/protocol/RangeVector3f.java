package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RangeVector3f {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 25;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 25;
   public static final int MAX_SIZE = 25;
   @Nullable
   public Rangef x;
   @Nullable
   public Rangef y;
   @Nullable
   public Rangef z;

   public RangeVector3f() {
   }

   public RangeVector3f(@Nullable Rangef x, @Nullable Rangef y, @Nullable Rangef z) {
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public RangeVector3f(@Nonnull RangeVector3f other) {
      this.x = other.x;
      this.y = other.y;
      this.z = other.z;
   }

   @Nonnull
   public static RangeVector3f deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 25) {
         throw ProtocolException.bufferTooSmall("RangeVector3f", 25, buf.readableBytes() - offset);
      } else {
         RangeVector3f obj = new RangeVector3f();
         byte nullBits = buf.getByte(offset);
         if ((nullBits & 1) != 0) {
            obj.x = Rangef.deserialize(buf, offset + 1);
         }

         if ((nullBits & 2) != 0) {
            obj.y = Rangef.deserialize(buf, offset + 9);
         }

         if ((nullBits & 4) != 0) {
            obj.z = Rangef.deserialize(buf, offset + 17);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 25;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 25L;
   }

   @Nullable
   public static Rangef getX(MemorySegment mem) {
      return getX(mem, 0);
   }

   @Nullable
   public static Rangef getX(MemorySegment mem, int offset) {
      return hasX(mem, offset) ? Rangef.toObject(mem, offset + 1) : null;
   }

   @Nullable
   public static Rangef getY(MemorySegment mem) {
      return getY(mem, 0);
   }

   @Nullable
   public static Rangef getY(MemorySegment mem, int offset) {
      return hasY(mem, offset) ? Rangef.toObject(mem, offset + 9) : null;
   }

   @Nullable
   public static Rangef getZ(MemorySegment mem) {
      return getZ(mem, 0);
   }

   @Nullable
   public static Rangef getZ(MemorySegment mem, int offset) {
      return hasZ(mem, offset) ? Rangef.toObject(mem, offset + 17) : null;
   }

   public static boolean hasX(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasY(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasZ(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 4) != 0;
   }

   public static RangeVector3f toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static RangeVector3f toObject(MemorySegment mem, int offset) {
      if (offset + 25 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("RangeVector3f", offset + 25, (int)mem.byteSize());
      } else {
         return new RangeVector3f(
            hasX(mem, offset) ? Rangef.toObject(mem, offset + 1) : null,
            hasY(mem, offset) ? Rangef.toObject(mem, offset + 9) : null,
            hasZ(mem, offset) ? Rangef.toObject(mem, offset + 17) : null
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.x != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.y != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.z != null) {
         nullBits = (byte)(nullBits | 4);
      }

      buf.writeByte(nullBits);
      if (this.x != null) {
         this.x.serialize(buf);
      } else {
         buf.writeZero(8);
      }

      if (this.y != null) {
         this.y.serialize(buf);
      } else {
         buf.writeZero(8);
      }

      if (this.z != null) {
         this.z.serialize(buf);
      } else {
         buf.writeZero(8);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.x != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.y != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.z != null) {
         nullBits = (byte)(nullBits | 4);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      if (this.x != null) {
         this.x.serialize(mem, offset + 1);
      } else {
         mem.asSlice(offset + 1, 8L).fill((byte)0);
      }

      if (this.y != null) {
         this.y.serialize(mem, offset + 9);
      } else {
         mem.asSlice(offset + 9, 8L).fill((byte)0);
      }

      if (this.z != null) {
         this.z.serialize(mem, offset + 17);
      } else {
         mem.asSlice(offset + 17, 8L).fill((byte)0);
      }

      return 25;
   }

   public int computeSize() {
      return 25;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 25) {
         return ValidationResult.error("Buffer too small: expected at least 25 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         return ValidationResult.OK;
      }
   }

   public RangeVector3f clone() {
      RangeVector3f copy = new RangeVector3f();
      copy.x = this.x != null ? this.x.clone() : null;
      copy.y = this.y != null ? this.y.clone() : null;
      copy.z = this.z != null ? this.z.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof RangeVector3f other)
            ? false
            : Objects.equals(this.x, other.x) && Objects.equals(this.y, other.y) && Objects.equals(this.z, other.z);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.x, this.y, this.z);
   }
}
