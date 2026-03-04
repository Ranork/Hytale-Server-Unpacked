package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RangeVector2f {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 17;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 17;
   public static final int MAX_SIZE = 17;
   @Nullable
   public Rangef x;
   @Nullable
   public Rangef y;

   public RangeVector2f() {
   }

   public RangeVector2f(@Nullable Rangef x, @Nullable Rangef y) {
      this.x = x;
      this.y = y;
   }

   public RangeVector2f(@Nonnull RangeVector2f other) {
      this.x = other.x;
      this.y = other.y;
   }

   @Nonnull
   public static RangeVector2f deserialize(@Nonnull ByteBuf buf, int offset) {
      RangeVector2f obj = new RangeVector2f();
      byte nullBits = buf.getByte(offset);
      if ((nullBits & 1) != 0) {
         obj.x = Rangef.deserialize(buf, offset + 1);
      }

      if ((nullBits & 2) != 0) {
         obj.y = Rangef.deserialize(buf, offset + 9);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 17;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.x != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.y != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      if (this.x != null) {
         this.x.serialize(buf);
      } else {
         buf.writeZero(8);
      }

      if (this.y != null) {
         this.y.serialize(buf);
      } else {
         buf.writeZero(8);
      }
   }

   public int computeSize() {
      return 17;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 17 ? ValidationResult.error("Buffer too small: expected at least 17 bytes") : ValidationResult.OK;
   }

   public RangeVector2f clone() {
      RangeVector2f copy = new RangeVector2f();
      copy.x = this.x != null ? this.x.clone() : null;
      copy.y = this.y != null ? this.y.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof RangeVector2f other) ? false : Objects.equals(this.x, other.x) && Objects.equals(this.y, other.y);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.x, this.y);
   }
}
