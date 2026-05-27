package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;

public class Size {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 8;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 8;
   public static final int MAX_SIZE = 8;
   public int width;
   public int height;

   public Size() {
   }

   public Size(int width, int height) {
      this.width = width;
      this.height = height;
   }

   public Size(@Nonnull Size other) {
      this.width = other.width;
      this.height = other.height;
   }

   @Nonnull
   public static Size deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 8) {
         throw ProtocolException.bufferTooSmall("Size", 8, buf.readableBytes() - offset);
      } else {
         Size obj = new Size();
         obj.width = buf.getIntLE(offset + 0);
         obj.height = buf.getIntLE(offset + 4);
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 8;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 8L;
   }

   public static int getWidth(MemorySegment mem) {
      return getWidth(mem, 0);
   }

   public static int getWidth(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 0));
   }

   public static int getHeight(MemorySegment mem) {
      return getHeight(mem, 0);
   }

   public static int getHeight(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 4));
   }

   public static Size toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static Size toObject(MemorySegment mem, int offset) {
      if (offset + 8 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("Size", offset + 8, (int)mem.byteSize());
      } else {
         return new Size(mem.get(PacketIO.PROTO_INT, (long)(offset + 0)), mem.get(PacketIO.PROTO_INT, (long)(offset + 4)));
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeIntLE(this.width);
      buf.writeIntLE(this.height);
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_INT, (long)(offset + 0), this.width);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 4), this.height);
      return 8;
   }

   public int computeSize() {
      return 8;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 8 ? ValidationResult.error("Buffer too small: expected at least 8 bytes") : ValidationResult.OK;
   }

   public Size clone() {
      Size copy = new Size();
      copy.width = this.width;
      copy.height = this.height;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof Size other) ? false : this.width == other.width && this.height == other.height;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.width, this.height);
   }
}
