package com.hypixel.hytale.protocol.packets.window;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class ChangeBlockAction extends WindowAction {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 1;
   public static final int MAX_SIZE = 1;
   public boolean down;

   public ChangeBlockAction() {
   }

   public ChangeBlockAction(boolean down) {
      this.down = down;
   }

   public ChangeBlockAction(@Nonnull ChangeBlockAction other) {
      this.down = other.down;
   }

   @Nonnull
   public static ChangeBlockAction deserialize(@Nonnull ByteBuf buf, int offset) {
      ChangeBlockAction obj = new ChangeBlockAction();
      obj.down = buf.getByte(offset + 0) != 0;
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 1;
   }

   @Override
   public int serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      buf.writeByte(this.down ? 1 : 0);
      return buf.writerIndex() - startPos;
   }

   @Override
   public int computeSize() {
      return 1;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 1 ? ValidationResult.error("Buffer too small: expected at least 1 bytes") : ValidationResult.OK;
   }

   public ChangeBlockAction clone() {
      ChangeBlockAction copy = new ChangeBlockAction();
      copy.down = this.down;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof ChangeBlockAction other ? this.down == other.down : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.down);
   }
}
