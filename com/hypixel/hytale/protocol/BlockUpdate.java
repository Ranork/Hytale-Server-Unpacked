package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;

public class BlockUpdate extends ComponentUpdate {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 8;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 8;
   public static final int MAX_SIZE = 8;
   public int blockId;
   public float entityScale;

   public BlockUpdate() {
   }

   public BlockUpdate(int blockId, float entityScale) {
      this.blockId = blockId;
      this.entityScale = entityScale;
   }

   public BlockUpdate(@Nonnull BlockUpdate other) {
      this.blockId = other.blockId;
      this.entityScale = other.entityScale;
   }

   @Nonnull
   public static BlockUpdate deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 8) {
         throw ProtocolException.bufferTooSmall("BlockUpdate", 8, buf.readableBytes() - offset);
      } else {
         BlockUpdate obj = new BlockUpdate();
         obj.blockId = buf.getIntLE(offset + 0);
         obj.entityScale = buf.getFloatLE(offset + 4);
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 8;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 8L;
   }

   public static int getBlockId(MemorySegment mem) {
      return getBlockId(mem, 0);
   }

   public static int getBlockId(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 0));
   }

   public static float getEntityScale(MemorySegment mem) {
      return getEntityScale(mem, 0);
   }

   public static float getEntityScale(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 4));
   }

   public static BlockUpdate toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static BlockUpdate toObject(MemorySegment mem, int offset) {
      if (offset + 8 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("BlockUpdate", offset + 8, (int)mem.byteSize());
      } else {
         return new BlockUpdate(mem.get(PacketIO.PROTO_INT, (long)(offset + 0)), mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 4)));
      }
   }

   @Override
   public int serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      buf.writeIntLE(this.blockId);
      buf.writeFloatLE(this.entityScale);
      return buf.writerIndex() - startPos;
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_INT, (long)(offset + 0), this.blockId);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 4), this.entityScale);
      return 8;
   }

   @Override
   public int computeSize() {
      return 8;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 8 ? ValidationResult.error("Buffer too small: expected at least 8 bytes") : ValidationResult.OK;
   }

   public BlockUpdate clone() {
      BlockUpdate copy = new BlockUpdate();
      copy.blockId = this.blockId;
      copy.entityScale = this.entityScale;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof BlockUpdate other) ? false : this.blockId == other.blockId && this.entityScale == other.entityScale;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.blockId, this.entityScale);
   }
}
