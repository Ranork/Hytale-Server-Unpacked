package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class DoubleParamValue extends ParamValue {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 8;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 8;
   public static final int MAX_SIZE = 8;
   public double value;

   public DoubleParamValue() {
   }

   public DoubleParamValue(double value) {
      this.value = value;
   }

   public DoubleParamValue(@Nonnull DoubleParamValue other) {
      this.value = other.value;
   }

   @Nonnull
   public static DoubleParamValue deserialize(@Nonnull ByteBuf buf, int offset) {
      DoubleParamValue obj = new DoubleParamValue();
      obj.value = buf.getDoubleLE(offset + 0);
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 8;
   }

   @Override
   public int serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      buf.writeDoubleLE(this.value);
      return buf.writerIndex() - startPos;
   }

   @Override
   public int computeSize() {
      return 8;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 8 ? ValidationResult.error("Buffer too small: expected at least 8 bytes") : ValidationResult.OK;
   }

   public DoubleParamValue clone() {
      DoubleParamValue copy = new DoubleParamValue();
      copy.value = this.value;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof DoubleParamValue other ? this.value == other.value : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.value);
   }
}
