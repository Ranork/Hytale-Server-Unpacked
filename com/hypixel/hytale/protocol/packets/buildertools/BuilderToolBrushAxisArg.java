package com.hypixel.hytale.protocol.packets.buildertools;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class BuilderToolBrushAxisArg {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 1;
   public static final int MAX_SIZE = 1;
   @Nonnull
   public BrushAxis defaultValue = BrushAxis.None;

   public BuilderToolBrushAxisArg() {
   }

   public BuilderToolBrushAxisArg(@Nonnull BrushAxis defaultValue) {
      this.defaultValue = defaultValue;
   }

   public BuilderToolBrushAxisArg(@Nonnull BuilderToolBrushAxisArg other) {
      this.defaultValue = other.defaultValue;
   }

   @Nonnull
   public static BuilderToolBrushAxisArg deserialize(@Nonnull ByteBuf buf, int offset) {
      BuilderToolBrushAxisArg obj = new BuilderToolBrushAxisArg();
      obj.defaultValue = BrushAxis.fromValue(buf.getByte(offset + 0));
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

   public BuilderToolBrushAxisArg clone() {
      BuilderToolBrushAxisArg copy = new BuilderToolBrushAxisArg();
      copy.defaultValue = this.defaultValue;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof BuilderToolBrushAxisArg other ? Objects.equals(this.defaultValue, other.defaultValue) : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.defaultValue);
   }
}
