package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class FloatRange {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 8;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 8;
   public static final int MAX_SIZE = 8;
   public float inclusiveMin;
   public float inclusiveMax;

   public FloatRange() {
   }

   public FloatRange(float inclusiveMin, float inclusiveMax) {
      this.inclusiveMin = inclusiveMin;
      this.inclusiveMax = inclusiveMax;
   }

   public FloatRange(@Nonnull FloatRange other) {
      this.inclusiveMin = other.inclusiveMin;
      this.inclusiveMax = other.inclusiveMax;
   }

   @Nonnull
   public static FloatRange deserialize(@Nonnull ByteBuf buf, int offset) {
      FloatRange obj = new FloatRange();
      obj.inclusiveMin = buf.getFloatLE(offset + 0);
      obj.inclusiveMax = buf.getFloatLE(offset + 4);
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 8;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeFloatLE(this.inclusiveMin);
      buf.writeFloatLE(this.inclusiveMax);
   }

   public int computeSize() {
      return 8;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 8 ? ValidationResult.error("Buffer too small: expected at least 8 bytes") : ValidationResult.OK;
   }

   public FloatRange clone() {
      FloatRange copy = new FloatRange();
      copy.inclusiveMin = this.inclusiveMin;
      copy.inclusiveMax = this.inclusiveMax;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof FloatRange other) ? false : this.inclusiveMin == other.inclusiveMin && this.inclusiveMax == other.inclusiveMax;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.inclusiveMin, this.inclusiveMax);
   }
}
