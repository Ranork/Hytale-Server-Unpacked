package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;

public class BlockPosition {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 12;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 12;
   public static final int MAX_SIZE = 12;
   public int x;
   public int y;
   public int z;

   public BlockPosition() {
   }

   public BlockPosition(int x, int y, int z) {
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public BlockPosition(@Nonnull BlockPosition other) {
      this.x = other.x;
      this.y = other.y;
      this.z = other.z;
   }

   @Nonnull
   public static BlockPosition deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 12) {
         throw ProtocolException.bufferTooSmall("BlockPosition", 12, buf.readableBytes() - offset);
      } else {
         BlockPosition obj = new BlockPosition();
         obj.x = buf.getIntLE(offset + 0);
         obj.y = buf.getIntLE(offset + 4);
         obj.z = buf.getIntLE(offset + 8);
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 12;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 12L;
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

   public static int getZ(MemorySegment mem) {
      return getZ(mem, 0);
   }

   public static int getZ(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 8));
   }

   public static BlockPosition toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static BlockPosition toObject(MemorySegment mem, int offset) {
      if (offset + 12 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("BlockPosition", offset + 12, (int)mem.byteSize());
      } else {
         return new BlockPosition(
            mem.get(PacketIO.PROTO_INT, (long)(offset + 0)), mem.get(PacketIO.PROTO_INT, (long)(offset + 4)), mem.get(PacketIO.PROTO_INT, (long)(offset + 8))
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeIntLE(this.x);
      buf.writeIntLE(this.y);
      buf.writeIntLE(this.z);
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_INT, (long)(offset + 0), this.x);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 4), this.y);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 8), this.z);
      return 12;
   }

   public int computeSize() {
      return 12;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 12 ? ValidationResult.error("Buffer too small: expected at least 12 bytes") : ValidationResult.OK;
   }

   public BlockPosition clone() {
      BlockPosition copy = new BlockPosition();
      copy.x = this.x;
      copy.y = this.y;
      copy.z = this.z;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof BlockPosition other) ? false : this.x == other.x && this.y == other.y && this.z == other.z;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.x, this.y, this.z);
   }
}
