package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Edge {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 9;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 9;
   public static final int MAX_SIZE = 9;
   @Nullable
   public ColorAlpha color;
   public float width;

   public Edge() {
   }

   public Edge(@Nullable ColorAlpha color, float width) {
      this.color = color;
      this.width = width;
   }

   public Edge(@Nonnull Edge other) {
      this.color = other.color;
      this.width = other.width;
   }

   @Nonnull
   public static Edge deserialize(@Nonnull ByteBuf buf, int offset) {
      Edge obj = new Edge();
      byte nullBits = buf.getByte(offset);
      if ((nullBits & 1) != 0) {
         obj.color = ColorAlpha.deserialize(buf, offset + 1);
      }

      obj.width = buf.getFloatLE(offset + 5);
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 9;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.color != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      if (this.color != null) {
         this.color.serialize(buf);
      } else {
         buf.writeZero(4);
      }

      buf.writeFloatLE(this.width);
   }

   public int computeSize() {
      return 9;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 9 ? ValidationResult.error("Buffer too small: expected at least 9 bytes") : ValidationResult.OK;
   }

   public Edge clone() {
      Edge copy = new Edge();
      copy.color = this.color != null ? this.color.clone() : null;
      copy.width = this.width;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof Edge other) ? false : Objects.equals(this.color, other.color) && this.width == other.width;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.color, this.width);
   }
}
