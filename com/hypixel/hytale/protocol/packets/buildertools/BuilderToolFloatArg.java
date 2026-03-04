package com.hypixel.hytale.protocol.packets.buildertools;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class BuilderToolFloatArg {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 12;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 12;
   public static final int MAX_SIZE = 12;
   public float defaultValue;
   public float min;
   public float max;

   public BuilderToolFloatArg() {
   }

   public BuilderToolFloatArg(float defaultValue, float min, float max) {
      this.defaultValue = defaultValue;
      this.min = min;
      this.max = max;
   }

   public BuilderToolFloatArg(@Nonnull BuilderToolFloatArg other) {
      this.defaultValue = other.defaultValue;
      this.min = other.min;
      this.max = other.max;
   }

   @Nonnull
   public static BuilderToolFloatArg deserialize(@Nonnull ByteBuf buf, int offset) {
      BuilderToolFloatArg obj = new BuilderToolFloatArg();
      obj.defaultValue = buf.getFloatLE(offset + 0);
      obj.min = buf.getFloatLE(offset + 4);
      obj.max = buf.getFloatLE(offset + 8);
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 12;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeFloatLE(this.defaultValue);
      buf.writeFloatLE(this.min);
      buf.writeFloatLE(this.max);
   }

   public int computeSize() {
      return 12;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 12 ? ValidationResult.error("Buffer too small: expected at least 12 bytes") : ValidationResult.OK;
   }

   public BuilderToolFloatArg clone() {
      BuilderToolFloatArg copy = new BuilderToolFloatArg();
      copy.defaultValue = this.defaultValue;
      copy.min = this.min;
      copy.max = this.max;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof BuilderToolFloatArg other) ? false : this.defaultValue == other.defaultValue && this.min == other.min && this.max == other.max;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.defaultValue, this.min, this.max);
   }
}
