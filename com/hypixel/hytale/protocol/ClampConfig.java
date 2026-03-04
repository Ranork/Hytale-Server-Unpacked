package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class ClampConfig {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 9;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 9;
   public static final int MAX_SIZE = 9;
   public float min;
   public float max;
   public boolean normalize;

   public ClampConfig() {
   }

   public ClampConfig(float min, float max, boolean normalize) {
      this.min = min;
      this.max = max;
      this.normalize = normalize;
   }

   public ClampConfig(@Nonnull ClampConfig other) {
      this.min = other.min;
      this.max = other.max;
      this.normalize = other.normalize;
   }

   @Nonnull
   public static ClampConfig deserialize(@Nonnull ByteBuf buf, int offset) {
      ClampConfig obj = new ClampConfig();
      obj.min = buf.getFloatLE(offset + 0);
      obj.max = buf.getFloatLE(offset + 4);
      obj.normalize = buf.getByte(offset + 8) != 0;
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 9;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeFloatLE(this.min);
      buf.writeFloatLE(this.max);
      buf.writeByte(this.normalize ? 1 : 0);
   }

   public int computeSize() {
      return 9;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 9 ? ValidationResult.error("Buffer too small: expected at least 9 bytes") : ValidationResult.OK;
   }

   public ClampConfig clone() {
      ClampConfig copy = new ClampConfig();
      copy.min = this.min;
      copy.max = this.max;
      copy.normalize = this.normalize;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ClampConfig other) ? false : this.min == other.min && this.max == other.max && this.normalize == other.normalize;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.min, this.max, this.normalize);
   }
}
