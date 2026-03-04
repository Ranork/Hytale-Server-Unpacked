package com.hypixel.hytale.protocol.packets.window;

import com.hypixel.hytale.protocol.SortType;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class SortItemsAction extends WindowAction {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 1;
   public static final int MAX_SIZE = 1;
   @Nonnull
   public SortType sortType = SortType.Name;

   public SortItemsAction() {
   }

   public SortItemsAction(@Nonnull SortType sortType) {
      this.sortType = sortType;
   }

   public SortItemsAction(@Nonnull SortItemsAction other) {
      this.sortType = other.sortType;
   }

   @Nonnull
   public static SortItemsAction deserialize(@Nonnull ByteBuf buf, int offset) {
      SortItemsAction obj = new SortItemsAction();
      obj.sortType = SortType.fromValue(buf.getByte(offset + 0));
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 1;
   }

   @Override
   public int serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      buf.writeByte(this.sortType.getValue());
      return buf.writerIndex() - startPos;
   }

   @Override
   public int computeSize() {
      return 1;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 1 ? ValidationResult.error("Buffer too small: expected at least 1 bytes") : ValidationResult.OK;
   }

   public SortItemsAction clone() {
      SortItemsAction copy = new SortItemsAction();
      copy.sortType = this.sortType;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof SortItemsAction other ? Objects.equals(this.sortType, other.sortType) : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.sortType);
   }
}
