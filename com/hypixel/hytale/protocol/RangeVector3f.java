package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RangeVector3f {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 25;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 25;
   public static final int MAX_SIZE = 25;
   @Nullable
   public Rangef x;
   @Nullable
   public Rangef y;
   @Nullable
   public Rangef z;

   public RangeVector3f() {
   }

   public RangeVector3f(@Nullable Rangef x, @Nullable Rangef y, @Nullable Rangef z) {
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public RangeVector3f(@Nonnull RangeVector3f other) {
      this.x = other.x;
      this.y = other.y;
      this.z = other.z;
   }

   @Nonnull
   public static RangeVector3f deserialize(@Nonnull ByteBuf buf, int offset) {
      RangeVector3f obj = new RangeVector3f();
      byte nullBits = buf.getByte(offset);
      if ((nullBits & 1) != 0) {
         obj.x = Rangef.deserialize(buf, offset + 1);
      }

      if ((nullBits & 2) != 0) {
         obj.y = Rangef.deserialize(buf, offset + 9);
      }

      if ((nullBits & 4) != 0) {
         obj.z = Rangef.deserialize(buf, offset + 17);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 25;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.x != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.y != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.z != null) {
         nullBits = (byte)(nullBits | 4);
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

      if (this.z != null) {
         this.z.serialize(buf);
      } else {
         buf.writeZero(8);
      }
   }

   public int computeSize() {
      return 25;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 25 ? ValidationResult.error("Buffer too small: expected at least 25 bytes") : ValidationResult.OK;
   }

   public RangeVector3f clone() {
      RangeVector3f copy = new RangeVector3f();
      copy.x = this.x != null ? this.x.clone() : null;
      copy.y = this.y != null ? this.y.clone() : null;
      copy.z = this.z != null ? this.z.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof RangeVector3f other)
            ? false
            : Objects.equals(this.x, other.x) && Objects.equals(this.y, other.y) && Objects.equals(this.z, other.z);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.x, this.y, this.z);
   }
}
