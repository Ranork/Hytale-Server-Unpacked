package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class BoolParamValue extends ParamValue {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 1;
   public static final int MAX_SIZE = 1;
   public boolean value;

   public BoolParamValue() {
   }

   public BoolParamValue(boolean value) {
      this.value = value;
   }

   public BoolParamValue(@Nonnull BoolParamValue other) {
      this.value = other.value;
   }

   @Nonnull
   public static BoolParamValue deserialize(@Nonnull ByteBuf buf, int offset) {
      BoolParamValue obj = new BoolParamValue();
      obj.value = buf.getByte(offset + 0) != 0;
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 1;
   }

   @Override
   public int serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      buf.writeByte(this.value ? 1 : 0);
      return buf.writerIndex() - startPos;
   }

   @Override
   public int computeSize() {
      return 1;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 1 ? ValidationResult.error("Buffer too small: expected at least 1 bytes") : ValidationResult.OK;
   }

   public BoolParamValue clone() {
      BoolParamValue copy = new BoolParamValue();
      copy.value = this.value;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof BoolParamValue other ? this.value == other.value : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.value);
   }
}
