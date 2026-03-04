package com.hypixel.hytale.protocol.packets.window;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class SelectSlotAction extends WindowAction {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 4;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 4;
   public static final int MAX_SIZE = 4;
   public int slot;

   public SelectSlotAction() {
   }

   public SelectSlotAction(int slot) {
      this.slot = slot;
   }

   public SelectSlotAction(@Nonnull SelectSlotAction other) {
      this.slot = other.slot;
   }

   @Nonnull
   public static SelectSlotAction deserialize(@Nonnull ByteBuf buf, int offset) {
      SelectSlotAction obj = new SelectSlotAction();
      obj.slot = buf.getIntLE(offset + 0);
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 4;
   }

   @Override
   public int serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      buf.writeIntLE(this.slot);
      return buf.writerIndex() - startPos;
   }

   @Override
   public int computeSize() {
      return 4;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 4 ? ValidationResult.error("Buffer too small: expected at least 4 bytes") : ValidationResult.OK;
   }

   public SelectSlotAction clone() {
      SelectSlotAction copy = new SelectSlotAction();
      copy.slot = this.slot;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof SelectSlotAction other ? this.slot == other.slot : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.slot);
   }
}
