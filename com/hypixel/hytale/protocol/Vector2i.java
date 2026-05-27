package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;

public class Vector2i {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 8;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 8;
   public static final int MAX_SIZE = 8;
   public int x;
   public int y;

   public Vector2i() {
   }

   public Vector2i(int x, int y) {
      this.x = x;
      this.y = y;
   }

   public Vector2i(@Nonnull Vector2i other) {
      this.x = other.x;
      this.y = other.y;
   }

   @Nonnull
   public static Vector2i deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 8) {
         throw ProtocolException.bufferTooSmall("Vector2i", 8, buf.readableBytes() - offset);
      } else {
         Vector2i obj = new Vector2i();
         obj.x = buf.getIntLE(offset + 0);
         obj.y = buf.getIntLE(offset + 4);
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 8;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 8L;
   }

   public static int getX(MemorySegment mem) {
      return getX(mem, 0);
   }

   public static int getX(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 0));
   }

   public static int getY(MemorySegment mem) {
      return getY(mem, 0);
   }

   public static int getY(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 4));
   }

   public static Vector2i toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static Vector2i toObject(MemorySegment mem, int offset) {
      if (offset + 8 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("Vector2i", offset + 8, (int)mem.byteSize());
      } else {
         return new Vector2i(mem.get(PacketIO.PROTO_INT, (long)(offset + 0)), mem.get(PacketIO.PROTO_INT, (long)(offset + 4)));
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeIntLE(this.x);
      buf.writeIntLE(this.y);
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_INT, (long)(offset + 0), this.x);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 4), this.y);
      return 8;
   }

   public int computeSize() {
      return 8;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 8 ? ValidationResult.error("Buffer too small: expected at least 8 bytes") : ValidationResult.OK;
   }

   public Vector2i clone() {
      Vector2i copy = new Vector2i();
      copy.x = this.x;
      copy.y = this.y;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof Vector2i other) ? false : this.x == other.x && this.y == other.y;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.x, this.y);
   }
}
