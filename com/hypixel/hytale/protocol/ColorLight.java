package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class ColorLight {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 4;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 4;
   public static final int MAX_SIZE = 4;
   public byte radius;
   public byte red;
   public byte green;
   public byte blue;

   public ColorLight() {
   }

   public ColorLight(byte radius, byte red, byte green, byte blue) {
      this.radius = radius;
      this.red = red;
      this.green = green;
      this.blue = blue;
   }

   public ColorLight(@Nonnull ColorLight other) {
      this.radius = other.radius;
      this.red = other.red;
      this.green = other.green;
      this.blue = other.blue;
   }

   @Nonnull
   public static ColorLight deserialize(@Nonnull ByteBuf buf, int offset) {
      ColorLight obj = new ColorLight();
      obj.radius = buf.getByte(offset + 0);
      obj.red = buf.getByte(offset + 1);
      obj.green = buf.getByte(offset + 2);
      obj.blue = buf.getByte(offset + 3);
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 4;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeByte(this.radius);
      buf.writeByte(this.red);
      buf.writeByte(this.green);
      buf.writeByte(this.blue);
   }

   public int computeSize() {
      return 4;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 4 ? ValidationResult.error("Buffer too small: expected at least 4 bytes") : ValidationResult.OK;
   }

   public ColorLight clone() {
      ColorLight copy = new ColorLight();
      copy.radius = this.radius;
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
         return !(obj instanceof ColorLight other)
            ? false
            : this.radius == other.radius && this.red == other.red && this.green == other.green && this.blue == other.blue;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.radius, this.red, this.green, this.blue);
   }
}
