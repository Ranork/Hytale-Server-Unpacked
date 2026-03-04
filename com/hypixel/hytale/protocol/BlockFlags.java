package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
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
      BlockFlags obj = new BlockFlags();
      obj.isUsable = buf.getByte(offset + 0) != 0;
      obj.isStackable = buf.getByte(offset + 1) != 0;
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 2;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeByte(this.isUsable ? 1 : 0);
      buf.writeByte(this.isStackable ? 1 : 0);
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
