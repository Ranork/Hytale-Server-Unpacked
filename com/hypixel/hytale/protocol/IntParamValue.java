package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class IntParamValue extends ParamValue {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 4;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 4;
   public static final int MAX_SIZE = 4;
   public int value;

   public IntParamValue() {
   }

   public IntParamValue(int value) {
      this.value = value;
   }

   public IntParamValue(@Nonnull IntParamValue other) {
      this.value = other.value;
   }

   @Nonnull
   public static IntParamValue deserialize(@Nonnull ByteBuf buf, int offset) {
      IntParamValue obj = new IntParamValue();
      obj.value = buf.getIntLE(offset + 0);
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 4;
   }

   @Override
   public int serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      buf.writeIntLE(this.value);
      return buf.writerIndex() - startPos;
   }

   @Override
   public int computeSize() {
      return 4;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 4 ? ValidationResult.error("Buffer too small: expected at least 4 bytes") : ValidationResult.OK;
   }

   public IntParamValue clone() {
      IntParamValue copy = new IntParamValue();
      copy.value = this.value;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof IntParamValue other ? this.value == other.value : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.value);
   }
}
