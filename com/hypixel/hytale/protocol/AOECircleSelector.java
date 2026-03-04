package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AOECircleSelector extends Selector {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 17;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 17;
   public static final int MAX_SIZE = 17;
   public float range;
   @Nullable
   public Vector3f offset;

   public AOECircleSelector() {
   }

   public AOECircleSelector(float range, @Nullable Vector3f offset) {
      this.range = range;
      this.offset = offset;
   }

   public AOECircleSelector(@Nonnull AOECircleSelector other) {
      this.range = other.range;
      this.offset = other.offset;
   }

   @Nonnull
   public static AOECircleSelector deserialize(@Nonnull ByteBuf buf, int offset) {
      AOECircleSelector obj = new AOECircleSelector();
      byte nullBits = buf.getByte(offset);
      obj.range = buf.getFloatLE(offset + 1);
      if ((nullBits & 1) != 0) {
         obj.offset = Vector3f.deserialize(buf, offset + 5);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 17;
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
      if (this.offset != null) {
         this.offset.serialize(buf);
      } else {
         buf.writeZero(12);
      }

      return buf.writerIndex() - startPos;
   }

   @Override
   public int computeSize() {
      return 17;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 17 ? ValidationResult.error("Buffer too small: expected at least 17 bytes") : ValidationResult.OK;
   }

   public AOECircleSelector clone() {
      AOECircleSelector copy = new AOECircleSelector();
      copy.range = this.range;
      copy.offset = this.offset != null ? this.offset.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof AOECircleSelector other) ? false : this.range == other.range && Objects.equals(this.offset, other.offset);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.range, this.offset);
   }
}
