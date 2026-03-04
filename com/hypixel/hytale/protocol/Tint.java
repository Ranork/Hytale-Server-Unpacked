package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class Tint {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 24;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 24;
   public static final int MAX_SIZE = 24;
   public int top;
   public int bottom;
   public int front;
   public int back;
   public int left;
   public int right;

   public Tint() {
   }

   public Tint(int top, int bottom, int front, int back, int left, int right) {
      this.top = top;
      this.bottom = bottom;
      this.front = front;
      this.back = back;
      this.left = left;
      this.right = right;
   }

   public Tint(@Nonnull Tint other) {
      this.top = other.top;
      this.bottom = other.bottom;
      this.front = other.front;
      this.back = other.back;
      this.left = other.left;
      this.right = other.right;
   }

   @Nonnull
   public static Tint deserialize(@Nonnull ByteBuf buf, int offset) {
      Tint obj = new Tint();
      obj.top = buf.getIntLE(offset + 0);
      obj.bottom = buf.getIntLE(offset + 4);
      obj.front = buf.getIntLE(offset + 8);
      obj.back = buf.getIntLE(offset + 12);
      obj.left = buf.getIntLE(offset + 16);
      obj.right = buf.getIntLE(offset + 20);
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 24;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeIntLE(this.top);
      buf.writeIntLE(this.bottom);
      buf.writeIntLE(this.front);
      buf.writeIntLE(this.back);
      buf.writeIntLE(this.left);
      buf.writeIntLE(this.right);
   }

   public int computeSize() {
      return 24;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 24 ? ValidationResult.error("Buffer too small: expected at least 24 bytes") : ValidationResult.OK;
   }

   public Tint clone() {
      Tint copy = new Tint();
      copy.top = this.top;
      copy.bottom = this.bottom;
      copy.front = this.front;
      copy.back = this.back;
      copy.left = this.left;
      copy.right = this.right;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof Tint other)
            ? false
            : this.top == other.top
               && this.bottom == other.bottom
               && this.front == other.front
               && this.back == other.back
               && this.left == other.left
               && this.right == other.right;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.top, this.bottom, this.front, this.back, this.left, this.right);
   }
}
