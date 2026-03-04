package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Transform {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 37;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 37;
   public static final int MAX_SIZE = 37;
   @Nullable
   public Position position;
   @Nullable
   public Direction orientation;

   public Transform() {
   }

   public Transform(@Nullable Position position, @Nullable Direction orientation) {
      this.position = position;
      this.orientation = orientation;
   }

   public Transform(@Nonnull Transform other) {
      this.position = other.position;
      this.orientation = other.orientation;
   }

   @Nonnull
   public static Transform deserialize(@Nonnull ByteBuf buf, int offset) {
      Transform obj = new Transform();
      byte nullBits = buf.getByte(offset);
      if ((nullBits & 1) != 0) {
         obj.position = Position.deserialize(buf, offset + 1);
      }

      if ((nullBits & 2) != 0) {
         obj.orientation = Direction.deserialize(buf, offset + 25);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 37;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.position != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.orientation != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      if (this.position != null) {
         this.position.serialize(buf);
      } else {
         buf.writeZero(24);
      }

      if (this.orientation != null) {
         this.orientation.serialize(buf);
      } else {
         buf.writeZero(12);
      }
   }

   public int computeSize() {
      return 37;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 37 ? ValidationResult.error("Buffer too small: expected at least 37 bytes") : ValidationResult.OK;
   }

   public Transform clone() {
      Transform copy = new Transform();
      copy.position = this.position != null ? this.position.clone() : null;
      copy.orientation = this.orientation != null ? this.orientation.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof Transform other)
            ? false
            : Objects.equals(this.position, other.position) && Objects.equals(this.orientation, other.orientation);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.position, this.orientation);
   }
}
