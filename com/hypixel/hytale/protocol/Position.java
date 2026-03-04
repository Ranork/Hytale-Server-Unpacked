package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class Position {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 24;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 24;
   public static final int MAX_SIZE = 24;
   public double x;
   public double y;
   public double z;

   public Position() {
   }

   public Position(double x, double y, double z) {
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public Position(@Nonnull Position other) {
      this.x = other.x;
      this.y = other.y;
      this.z = other.z;
   }

   @Nonnull
   public static Position deserialize(@Nonnull ByteBuf buf, int offset) {
      Position obj = new Position();
      obj.x = buf.getDoubleLE(offset + 0);
      obj.y = buf.getDoubleLE(offset + 8);
      obj.z = buf.getDoubleLE(offset + 16);
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 24;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeDoubleLE(this.x);
      buf.writeDoubleLE(this.y);
      buf.writeDoubleLE(this.z);
   }

   public int computeSize() {
      return 24;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 24 ? ValidationResult.error("Buffer too small: expected at least 24 bytes") : ValidationResult.OK;
   }

   public Position clone() {
      Position copy = new Position();
      copy.x = this.x;
      copy.y = this.y;
      copy.z = this.z;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof Position other) ? false : this.x == other.x && this.y == other.y && this.z == other.z;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.x, this.y, this.z);
   }
}
