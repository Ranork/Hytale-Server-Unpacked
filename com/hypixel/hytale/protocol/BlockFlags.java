package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;

public class BlockFlags {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 2;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 2;
   public static final int MAX_SIZE = 2;
   public boolean isUsable;
   public boolean isStackable;

   public BlockFlags() {
   }

   public BlockFlags(boolean isUsable, boolean isStackable) {
      this.isUsable = isUsable;
      this.isStackable = isStackable;
   }

   public BlockFlags(@Nonnull BlockFlags other) {
      this.isUsable = other.isUsable;
      this.isStackable = other.isStackable;
   }

   @Nonnull
   public static BlockFlags deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 2) {
         throw ProtocolException.bufferTooSmall("BlockFlags", 2, buf.readableBytes() - offset);
      } else {
         BlockFlags obj = new BlockFlags();
         obj.isUsable = buf.getByte(offset + 0) != 0;
         obj.isStackable = buf.getByte(offset + 1) != 0;
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 2;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 2L;
   }

   public static boolean getIsUsable(MemorySegment mem) {
      return getIsUsable(mem, 0);
   }

   public static boolean getIsUsable(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 0));
   }

   public static boolean getIsStackable(MemorySegment mem) {
      return getIsStackable(mem, 0);
   }

   public static boolean getIsStackable(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 1));
   }

   public static BlockFlags toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static BlockFlags toObject(MemorySegment mem, int offset) {
      if (offset + 2 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("BlockFlags", offset + 2, (int)mem.byteSize());
      } else {
         return new BlockFlags(mem.get(PacketIO.PROTO_BOOL, (long)(offset + 0)), mem.get(PacketIO.PROTO_BOOL, (long)(offset + 1)));
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeByte(this.isUsable ? 1 : 0);
      buf.writeByte(this.isStackable ? 1 : 0);
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_BOOL, offset + 0, this.isUsable);
      mem.set(PacketIO.PROTO_BOOL, offset + 1, this.isStackable);
      return 2;
   }

   public int computeSize() {
      return 2;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 2 ? ValidationResult.error("Buffer too small: expected at least 2 bytes") : ValidationResult.OK;
   }

   public BlockFlags clone() {
      BlockFlags copy = new BlockFlags();
      copy.isUsable = this.isUsable;
      copy.isStackable = this.isStackable;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof BlockFlags other) ? false : this.isUsable == other.isUsable && this.isStackable == other.isStackable;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.isUsable, this.isStackable);
   }
}
