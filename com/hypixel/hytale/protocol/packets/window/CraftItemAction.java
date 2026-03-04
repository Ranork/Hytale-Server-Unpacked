package com.hypixel.hytale.protocol.packets.window;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import javax.annotation.Nonnull;

public class CraftItemAction extends WindowAction {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 0;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 0;
   public static final int MAX_SIZE = 0;

   @Nonnull
   public static CraftItemAction deserialize(@Nonnull ByteBuf buf, int offset) {
      return new CraftItemAction();
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 0;
   }

   @Override
   public int serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      return buf.writerIndex() - startPos;
   }

   @Override
   public int computeSize() {
      return 0;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 0 ? ValidationResult.error("Buffer too small: expected at least 0 bytes") : ValidationResult.OK;
   }

   public CraftItemAction clone() {
      return new CraftItemAction();
   }

   @Override
   public boolean equals(Object obj) {
      return this == obj ? true : obj instanceof CraftItemAction other;
   }

   @Override
   public int hashCode() {
      return 0;
   }
}
