package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class SavedMovementStates {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 1;
   public static final int MAX_SIZE = 1;
   public boolean flying;

   public SavedMovementStates() {
   }

   public SavedMovementStates(boolean flying) {
      this.flying = flying;
   }

   public SavedMovementStates(@Nonnull SavedMovementStates other) {
      this.flying = other.flying;
   }

   @Nonnull
   public static SavedMovementStates deserialize(@Nonnull ByteBuf buf, int offset) {
      SavedMovementStates obj = new SavedMovementStates();
      obj.flying = buf.getByte(offset + 0) != 0;
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 1;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeByte(this.flying ? 1 : 0);
   }

   public int computeSize() {
      return 1;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 1 ? ValidationResult.error("Buffer too small: expected at least 1 bytes") : ValidationResult.OK;
   }

   public SavedMovementStates clone() {
      SavedMovementStates copy = new SavedMovementStates();
      copy.flying = this.flying;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof SavedMovementStates other ? this.flying == other.flying : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.flying);
   }
}
