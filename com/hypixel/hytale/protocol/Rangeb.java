package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class Rangeb {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 2;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 2;
   public static final int MAX_SIZE = 2;
   public byte min;
   public byte max;

   public Rangeb() {
   }

   public Rangeb(byte min, byte max) {
      this.min = min;
      this.max = max;
   }

   public Rangeb(@Nonnull Rangeb other) {
      this.min = other.min;
      this.max = other.max;
   }

   @Nonnull
   public static Rangeb deserialize(@Nonnull ByteBuf buf, int offset) {
      Rangeb obj = new Rangeb();
      obj.min = buf.getByte(offset + 0);
      obj.max = buf.getByte(offset + 1);
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 2;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeByte(this.min);
      buf.writeByte(this.max);
   }

   public int computeSize() {
      return 2;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 2 ? ValidationResult.error("Buffer too small: expected at least 2 bytes") : ValidationResult.OK;
   }

   public Rangeb clone() {
      Rangeb copy = new Rangeb();
      copy.min = this.min;
      copy.max = this.max;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof Rangeb other) ? false : this.min == other.min && this.max == other.max;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.min, this.max);
   }
}
