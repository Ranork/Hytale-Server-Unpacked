package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;

public class BlockSelectorToolData {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 4;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 4;
   public static final int MAX_SIZE = 4;
   public float durabilityLossOnUse;

   public BlockSelectorToolData() {
   }

   public BlockSelectorToolData(float durabilityLossOnUse) {
      this.durabilityLossOnUse = durabilityLossOnUse;
   }

   public BlockSelectorToolData(@Nonnull BlockSelectorToolData other) {
      this.durabilityLossOnUse = other.durabilityLossOnUse;
   }

   @Nonnull
   public static BlockSelectorToolData deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 4) {
         throw ProtocolException.bufferTooSmall("BlockSelectorToolData", 4, buf.readableBytes() - offset);
      } else {
         BlockSelectorToolData obj = new BlockSelectorToolData();
         obj.durabilityLossOnUse = buf.getFloatLE(offset + 0);
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 4;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 4L;
   }

   public static float getDurabilityLossOnUse(MemorySegment mem) {
      return getDurabilityLossOnUse(mem, 0);
   }

   public static float getDurabilityLossOnUse(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 0));
   }

   public static BlockSelectorToolData toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static BlockSelectorToolData toObject(MemorySegment mem, int offset) {
      if (offset + 4 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("BlockSelectorToolData", offset + 4, (int)mem.byteSize());
      } else {
         return new BlockSelectorToolData(mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 0)));
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeFloatLE(this.durabilityLossOnUse);
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 0), this.durabilityLossOnUse);
      return 4;
   }

   public int computeSize() {
      return 4;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 4 ? ValidationResult.error("Buffer too small: expected at least 4 bytes") : ValidationResult.OK;
   }

   public BlockSelectorToolData clone() {
      BlockSelectorToolData copy = new BlockSelectorToolData();
      copy.durabilityLossOnUse = this.durabilityLossOnUse;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof BlockSelectorToolData other ? this.durabilityLossOnUse == other.durabilityLossOnUse : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.durabilityLossOnUse);
   }
}
