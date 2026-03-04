package com.hypixel.hytale.protocol.packets.buildertools;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class BuilderToolBoolArg {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 1;
   public static final int MAX_SIZE = 1;
   public boolean defaultValue;

   public BuilderToolBoolArg() {
   }

   public BuilderToolBoolArg(boolean defaultValue) {
      this.defaultValue = defaultValue;
   }

   public BuilderToolBoolArg(@Nonnull BuilderToolBoolArg other) {
      this.defaultValue = other.defaultValue;
   }

   @Nonnull
   public static BuilderToolBoolArg deserialize(@Nonnull ByteBuf buf, int offset) {
      BuilderToolBoolArg obj = new BuilderToolBoolArg();
      obj.defaultValue = buf.getByte(offset + 0) != 0;
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 1;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeByte(this.defaultValue ? 1 : 0);
   }

   public int computeSize() {
      return 1;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 1 ? ValidationResult.error("Buffer too small: expected at least 1 bytes") : ValidationResult.OK;
   }

   public BuilderToolBoolArg clone() {
      BuilderToolBoolArg copy = new BuilderToolBoolArg();
      copy.defaultValue = this.defaultValue;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof BuilderToolBoolArg other ? this.defaultValue == other.defaultValue : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.defaultValue);
   }
}
