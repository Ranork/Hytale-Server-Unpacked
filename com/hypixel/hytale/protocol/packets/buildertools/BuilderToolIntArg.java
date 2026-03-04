package com.hypixel.hytale.protocol.packets.buildertools;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class BuilderToolIntArg {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 12;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 12;
   public static final int MAX_SIZE = 12;
   public int defaultValue;
   public int min;
   public int max;

   public BuilderToolIntArg() {
   }

   public BuilderToolIntArg(int defaultValue, int min, int max) {
      this.defaultValue = defaultValue;
      this.min = min;
      this.max = max;
   }

   public BuilderToolIntArg(@Nonnull BuilderToolIntArg other) {
      this.defaultValue = other.defaultValue;
      this.min = other.min;
      this.max = other.max;
   }

   @Nonnull
   public static BuilderToolIntArg deserialize(@Nonnull ByteBuf buf, int offset) {
      BuilderToolIntArg obj = new BuilderToolIntArg();
      obj.defaultValue = buf.getIntLE(offset + 0);
      obj.min = buf.getIntLE(offset + 4);
      obj.max = buf.getIntLE(offset + 8);
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 12;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeIntLE(this.defaultValue);
      buf.writeIntLE(this.min);
      buf.writeIntLE(this.max);
   }

   public int computeSize() {
      return 12;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 12 ? ValidationResult.error("Buffer too small: expected at least 12 bytes") : ValidationResult.OK;
   }

   public BuilderToolIntArg clone() {
      BuilderToolIntArg copy = new BuilderToolIntArg();
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
         return !(obj instanceof BuilderToolIntArg other) ? false : this.defaultValue == other.defaultValue && this.min == other.min && this.max == other.max;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.defaultValue, this.min, this.max);
   }
}
