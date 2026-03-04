package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AOECylinderSelector extends Selector {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 21;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 21;
   public static final int MAX_SIZE = 21;
   public float range;
   public float height;
   @Nullable
   public Vector3f offset;

   public AOECylinderSelector() {
   }

   public AOECylinderSelector(float range, float height, @Nullable Vector3f offset) {
      this.range = range;
      this.height = height;
      this.offset = offset;
   }

   public AOECylinderSelector(@Nonnull AOECylinderSelector other) {
      this.range = other.range;
      this.height = other.height;
      this.offset = other.offset;
   }

   @Nonnull
   public static AOECylinderSelector deserialize(@Nonnull ByteBuf buf, int offset) {
      AOECylinderSelector obj = new AOECylinderSelector();
      byte nullBits = buf.getByte(offset);
      obj.range = buf.getFloatLE(offset + 1);
      obj.height = buf.getFloatLE(offset + 5);
      if ((nullBits & 1) != 0) {
         obj.offset = Vector3f.deserialize(buf, offset + 9);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 21;
   }

   @Override
   public int serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.offset != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeFloatLE(this.range);
      buf.writeFloatLE(this.height);
      if (this.offset != null) {
         this.offset.serialize(buf);
      } else {
         buf.writeZero(12);
      }

      return buf.writerIndex() - startPos;
   }

   @Override
   public int computeSize() {
      return 21;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 21 ? ValidationResult.error("Buffer too small: expected at least 21 bytes") : ValidationResult.OK;
   }

   public AOECylinderSelector clone() {
      AOECylinderSelector copy = new AOECylinderSelector();
      copy.range = this.range;
      copy.height = this.height;
      copy.offset = this.offset != null ? this.offset.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof AOECylinderSelector other)
            ? false
            : this.range == other.range && this.height == other.height && Objects.equals(this.offset, other.offset);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.range, this.height, this.offset);
   }
}
