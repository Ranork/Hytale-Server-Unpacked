package com.hypixel.hytale.protocol.packets.buildertools;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class BuilderToolBrushOriginArg {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 1;
   public static final int MAX_SIZE = 1;
   @Nonnull
   public BrushOrigin defaultValue = BrushOrigin.Center;

   public BuilderToolBrushOriginArg() {
   }

   public BuilderToolBrushOriginArg(@Nonnull BrushOrigin defaultValue) {
      this.defaultValue = defaultValue;
   }

   public BuilderToolBrushOriginArg(@Nonnull BuilderToolBrushOriginArg other) {
      this.defaultValue = other.defaultValue;
   }

   @Nonnull
   public static BuilderToolBrushOriginArg deserialize(@Nonnull ByteBuf buf, int offset) {
      BuilderToolBrushOriginArg obj = new BuilderToolBrushOriginArg();
      obj.defaultValue = BrushOrigin.fromValue(buf.getByte(offset + 0));
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 1;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeByte(this.defaultValue.getValue());
   }

   public int computeSize() {
      return 1;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 1 ? ValidationResult.error("Buffer too small: expected at least 1 bytes") : ValidationResult.OK;
   }

   public BuilderToolBrushOriginArg clone() {
      BuilderToolBrushOriginArg copy = new BuilderToolBrushOriginArg();
      copy.defaultValue = this.defaultValue;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof BuilderToolBrushOriginArg other ? Objects.equals(this.defaultValue, other.defaultValue) : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.defaultValue);
   }
}
