package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ModelTransform {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 49;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 49;
   public static final int MAX_SIZE = 49;
   @Nullable
   public Position position;
   @Nullable
   public Direction bodyOrientation;
   @Nullable
   public Direction lookOrientation;

   public ModelTransform() {
   }

   public ModelTransform(@Nullable Position position, @Nullable Direction bodyOrientation, @Nullable Direction lookOrientation) {
      this.position = position;
      this.bodyOrientation = bodyOrientation;
      this.lookOrientation = lookOrientation;
   }

   public ModelTransform(@Nonnull ModelTransform other) {
      this.position = other.position;
      this.bodyOrientation = other.bodyOrientation;
      this.lookOrientation = other.lookOrientation;
   }

   @Nonnull
   public static ModelTransform deserialize(@Nonnull ByteBuf buf, int offset) {
      ModelTransform obj = new ModelTransform();
      byte nullBits = buf.getByte(offset);
      if ((nullBits & 1) != 0) {
         obj.position = Position.deserialize(buf, offset + 1);
      }

      if ((nullBits & 2) != 0) {
         obj.bodyOrientation = Direction.deserialize(buf, offset + 25);
      }

      if ((nullBits & 4) != 0) {
         obj.lookOrientation = Direction.deserialize(buf, offset + 37);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 49;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.position != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.bodyOrientation != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.lookOrientation != null) {
         nullBits = (byte)(nullBits | 4);
      }

      buf.writeByte(nullBits);
      if (this.position != null) {
         this.position.serialize(buf);
      } else {
         buf.writeZero(24);
      }

      if (this.bodyOrientation != null) {
         this.bodyOrientation.serialize(buf);
      } else {
         buf.writeZero(12);
      }

      if (this.lookOrientation != null) {
         this.lookOrientation.serialize(buf);
      } else {
         buf.writeZero(12);
      }
   }

   public int computeSize() {
      return 49;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 49 ? ValidationResult.error("Buffer too small: expected at least 49 bytes") : ValidationResult.OK;
   }

   public ModelTransform clone() {
      ModelTransform copy = new ModelTransform();
      copy.position = this.position != null ? this.position.clone() : null;
      copy.bodyOrientation = this.bodyOrientation != null ? this.bodyOrientation.clone() : null;
      copy.lookOrientation = this.lookOrientation != null ? this.lookOrientation.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ModelTransform other)
            ? false
            : Objects.equals(this.position, other.position)
               && Objects.equals(this.bodyOrientation, other.bodyOrientation)
               && Objects.equals(this.lookOrientation, other.lookOrientation);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.position, this.bodyOrientation, this.lookOrientation);
   }
}
