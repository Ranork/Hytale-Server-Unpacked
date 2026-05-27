package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Edge {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 9;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 9;
   public static final int MAX_SIZE = 9;
   @Nullable
   public ColorAlpha color;
   public float width;

   public Edge() {
   }

   public Edge(@Nullable ColorAlpha color, float width) {
      this.color = color;
      this.width = width;
   }

   public Edge(@Nonnull Edge other) {
      this.color = other.color;
      this.width = other.width;
   }

   @Nonnull
   public static Edge deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 9) {
         throw ProtocolException.bufferTooSmall("Edge", 9, buf.readableBytes() - offset);
      } else {
         Edge obj = new Edge();
         byte nullBits = buf.getByte(offset);
         if ((nullBits & 1) != 0) {
            obj.color = ColorAlpha.deserialize(buf, offset + 1);
         }

         obj.width = buf.getFloatLE(offset + 5);
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 9;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 9L;
   }

   @Nullable
   public static ColorAlpha getColor(MemorySegment mem) {
      return getColor(mem, 0);
   }

   @Nullable
   public static ColorAlpha getColor(MemorySegment mem, int offset) {
      return hasColor(mem, offset) ? ColorAlpha.toObject(mem, offset + 1) : null;
   }

   public static float getWidth(MemorySegment mem) {
      return getWidth(mem, 0);
   }

   public static float getWidth(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 5));
   }

   public static boolean hasColor(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static Edge toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static Edge toObject(MemorySegment mem, int offset) {
      if (offset + 9 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("Edge", offset + 9, (int)mem.byteSize());
      } else {
         return new Edge(hasColor(mem, offset) ? ColorAlpha.toObject(mem, offset + 1) : null, mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 5)));
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.color != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      if (this.color != null) {
         this.color.serialize(buf);
      } else {
         buf.writeZero(4);
      }

      buf.writeFloatLE(this.width);
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.color != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      if (this.color != null) {
         this.color.serialize(mem, offset + 1);
      } else {
         mem.asSlice(offset + 1, 4L).fill((byte)0);
      }

      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 5), this.width);
      return 9;
   }

   public int computeSize() {
      return 9;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 9) {
         return ValidationResult.error("Buffer too small: expected at least 9 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         return ValidationResult.OK;
      }
   }

   public Edge clone() {
      Edge copy = new Edge();
      copy.color = this.color != null ? this.color.clone() : null;
      copy.width = this.width;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof Edge other) ? false : Objects.equals(this.color, other.color) && this.width == other.width;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.color, this.width);
   }
}
