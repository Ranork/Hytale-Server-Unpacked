package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RangeVector2f {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 17;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 17;
   public static final int MAX_SIZE = 17;
   @Nullable
   public Rangef x;
   @Nullable
   public Rangef y;

   public RangeVector2f() {
   }

   public RangeVector2f(@Nullable Rangef x, @Nullable Rangef y) {
      this.x = x;
      this.y = y;
   }

   public RangeVector2f(@Nonnull RangeVector2f other) {
      this.x = other.x;
      this.y = other.y;
   }

   @Nonnull
   public static RangeVector2f deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 17) {
         throw ProtocolException.bufferTooSmall("RangeVector2f", 17, buf.readableBytes() - offset);
      } else {
         RangeVector2f obj = new RangeVector2f();
         byte nullBits = buf.getByte(offset);
         if ((nullBits & 1) != 0) {
            obj.x = Rangef.deserialize(buf, offset + 1);
         }

         if ((nullBits & 2) != 0) {
            obj.y = Rangef.deserialize(buf, offset + 9);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 17;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 17L;
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

   public static boolean hasX(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasY(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static RangeVector2f toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static RangeVector2f toObject(MemorySegment mem, int offset) {
      if (offset + 17 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("RangeVector2f", offset + 17, (int)mem.byteSize());
      } else {
         return new RangeVector2f(hasX(mem, offset) ? Rangef.toObject(mem, offset + 1) : null, hasY(mem, offset) ? Rangef.toObject(mem, offset + 9) : null);
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
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.x != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.y != null) {
         nullBits = (byte)(nullBits | 2);
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

      return 17;
   }

   public int computeSize() {
      return 17;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 17) {
         return ValidationResult.error("Buffer too small: expected at least 17 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         return ValidationResult.OK;
      }
   }

   public RangeVector2f clone() {
      RangeVector2f copy = new RangeVector2f();
      copy.x = this.x != null ? this.x.clone() : null;
      copy.y = this.y != null ? this.y.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof RangeVector2f other) ? false : Objects.equals(this.x, other.x) && Objects.equals(this.y, other.y);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.x, this.y);
   }
}
