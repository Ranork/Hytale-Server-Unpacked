package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class LongParamValue extends ParamValue {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 8;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 8;
   public static final int MAX_SIZE = 8;
   public long value;

   public LongParamValue() {
   }

   public LongParamValue(long value) {
      this.value = value;
   }

   public LongParamValue(@Nonnull LongParamValue other) {
      this.value = other.value;
   }

   @Nonnull
   public static LongParamValue deserialize(@Nonnull ByteBuf buf, int offset) {
      LongParamValue obj = new LongParamValue();
      obj.value = buf.getLongLE(offset + 0);
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 8;
   }

   @Override
   public int serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      buf.writeLongLE(this.value);
      return buf.writerIndex() - startPos;
   }

   @Override
   public int computeSize() {
      return 8;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 8 ? ValidationResult.error("Buffer too small: expected at least 8 bytes") : ValidationResult.OK;
   }

   public LongParamValue clone() {
      LongParamValue copy = new LongParamValue();
      copy.value = this.value;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof LongParamValue other ? this.value == other.value : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.value);
   }
}
