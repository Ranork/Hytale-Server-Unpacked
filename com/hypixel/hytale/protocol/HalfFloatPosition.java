package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class HalfFloatPosition {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 6;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 6;
   public static final int MAX_SIZE = 6;
   public short x;
   public short y;
   public short z;

   public HalfFloatPosition() {
   }

   public HalfFloatPosition(short x, short y, short z) {
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public HalfFloatPosition(@Nonnull HalfFloatPosition other) {
      this.x = other.x;
      this.y = other.y;
      this.z = other.z;
   }

   @Nonnull
   public static HalfFloatPosition deserialize(@Nonnull ByteBuf buf, int offset) {
      HalfFloatPosition obj = new HalfFloatPosition();
      obj.x = buf.getShortLE(offset + 0);
      obj.y = buf.getShortLE(offset + 2);
      obj.z = buf.getShortLE(offset + 4);
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 6;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeShortLE(this.x);
      buf.writeShortLE(this.y);
      buf.writeShortLE(this.z);
   }

   public int computeSize() {
      return 6;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 6 ? ValidationResult.error("Buffer too small: expected at least 6 bytes") : ValidationResult.OK;
   }

   public HalfFloatPosition clone() {
      HalfFloatPosition copy = new HalfFloatPosition();
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
         return !(obj instanceof HalfFloatPosition other) ? false : this.x == other.x && this.y == other.y && this.z == other.z;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.x, this.y, this.z);
   }
}
