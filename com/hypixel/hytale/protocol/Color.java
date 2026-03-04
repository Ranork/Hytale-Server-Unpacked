package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class Color {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 3;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 3;
   public static final int MAX_SIZE = 3;
   public byte red;
   public byte green;
   public byte blue;

   public Color() {
   }

   public Color(byte red, byte green, byte blue) {
      this.red = red;
      this.green = green;
      this.blue = blue;
   }

   public Color(@Nonnull Color other) {
      this.red = other.red;
      this.green = other.green;
      this.blue = other.blue;
   }

   @Nonnull
   public static Color deserialize(@Nonnull ByteBuf buf, int offset) {
      Color obj = new Color();
      obj.red = buf.getByte(offset + 0);
      obj.green = buf.getByte(offset + 1);
      obj.blue = buf.getByte(offset + 2);
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 3;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeByte(this.red);
      buf.writeByte(this.green);
      buf.writeByte(this.blue);
   }

   public int computeSize() {
      return 3;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 3 ? ValidationResult.error("Buffer too small: expected at least 3 bytes") : ValidationResult.OK;
   }

   public Color clone() {
      Color copy = new Color();
      copy.red = this.red;
      copy.green = this.green;
      copy.blue = this.blue;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof Color other) ? false : this.red == other.red && this.green == other.green && this.blue == other.blue;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.red, this.green, this.blue);
   }
}
