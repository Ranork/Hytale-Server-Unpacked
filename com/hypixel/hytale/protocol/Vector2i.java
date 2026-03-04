package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class Vector2i {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 8;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 8;
   public static final int MAX_SIZE = 8;
   public int x;
   public int y;

   public Vector2i() {
   }

   public Vector2i(int x, int y) {
      this.x = x;
      this.y = y;
   }

   public Vector2i(@Nonnull Vector2i other) {
      this.x = other.x;
      this.y = other.y;
   }

   @Nonnull
   public static Vector2i deserialize(@Nonnull ByteBuf buf, int offset) {
      Vector2i obj = new Vector2i();
      obj.x = buf.getIntLE(offset + 0);
      obj.y = buf.getIntLE(offset + 4);
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 8;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeIntLE(this.x);
      buf.writeIntLE(this.y);
   }

   public int computeSize() {
      return 8;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 8 ? ValidationResult.error("Buffer too small: expected at least 8 bytes") : ValidationResult.OK;
   }

   public Vector2i clone() {
      Vector2i copy = new Vector2i();
      copy.x = this.x;
      copy.y = this.y;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof Vector2i other) ? false : this.x == other.x && this.y == other.y;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.x, this.y);
   }
}
