package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
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
      BlockUpdate obj = new BlockUpdate();
      obj.blockId = buf.getIntLE(offset + 0);
      obj.entityScale = buf.getFloatLE(offset + 4);
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 8;
   }

   @Override
   public int serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      buf.writeIntLE(this.blockId);
      buf.writeFloatLE(this.entityScale);
      return buf.writerIndex() - startPos;
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
