package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;

public class Tint {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 24;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 24;
   public static final int MAX_SIZE = 24;
   public int top;
   public int bottom;
   public int front;
   public int back;
   public int left;
   public int right;

   public Tint() {
   }

   public Tint(int top, int bottom, int front, int back, int left, int right) {
      this.top = top;
      this.bottom = bottom;
      this.front = front;
      this.back = back;
      this.left = left;
      this.right = right;
   }

   public Tint(@Nonnull Tint other) {
      this.top = other.top;
      this.bottom = other.bottom;
      this.front = other.front;
      this.back = other.back;
      this.left = other.left;
      this.right = other.right;
   }

   @Nonnull
   public static Tint deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 24) {
         throw ProtocolException.bufferTooSmall("Tint", 24, buf.readableBytes() - offset);
      } else {
         Tint obj = new Tint();
         obj.top = buf.getIntLE(offset + 0);
         obj.bottom = buf.getIntLE(offset + 4);
         obj.front = buf.getIntLE(offset + 8);
         obj.back = buf.getIntLE(offset + 12);
         obj.left = buf.getIntLE(offset + 16);
         obj.right = buf.getIntLE(offset + 20);
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 24;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 24L;
   }

   public static int getTop(MemorySegment mem) {
      return getTop(mem, 0);
   }

   public static int getTop(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 0));
   }

   public static int getBottom(MemorySegment mem) {
      return getBottom(mem, 0);
   }

   public static int getBottom(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 4));
   }

   public static int getFront(MemorySegment mem) {
      return getFront(mem, 0);
   }

   public static int getFront(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 8));
   }

   public static int getBack(MemorySegment mem) {
      return getBack(mem, 0);
   }

   public static int getBack(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 12));
   }

   public static int getLeft(MemorySegment mem) {
      return getLeft(mem, 0);
   }

   public static int getLeft(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 16));
   }

   public static int getRight(MemorySegment mem) {
      return getRight(mem, 0);
   }

   public static int getRight(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 20));
   }

   public static Tint toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static Tint toObject(MemorySegment mem, int offset) {
      if (offset + 24 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("Tint", offset + 24, (int)mem.byteSize());
      } else {
         return new Tint(
            mem.get(PacketIO.PROTO_INT, (long)(offset + 0)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 4)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 8)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 12)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 16)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 20))
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeIntLE(this.top);
      buf.writeIntLE(this.bottom);
      buf.writeIntLE(this.front);
      buf.writeIntLE(this.back);
      buf.writeIntLE(this.left);
      buf.writeIntLE(this.right);
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_INT, (long)(offset + 0), this.top);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 4), this.bottom);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 8), this.front);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 12), this.back);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 16), this.left);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 20), this.right);
      return 24;
   }

   public int computeSize() {
      return 24;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 24 ? ValidationResult.error("Buffer too small: expected at least 24 bytes") : ValidationResult.OK;
   }

   public Tint clone() {
      Tint copy = new Tint();
      copy.top = this.top;
      copy.bottom = this.bottom;
      copy.front = this.front;
      copy.back = this.back;
      copy.left = this.left;
      copy.right = this.right;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof Tint other)
            ? false
            : this.top == other.top
               && this.bottom == other.bottom
               && this.front == other.front
               && this.back == other.back
               && this.left == other.left
               && this.right == other.right;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.top, this.bottom, this.front, this.back, this.left, this.right);
   }
}
