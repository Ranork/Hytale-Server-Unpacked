package com.hypixel.hytale.math;

import io.netty.buffer.ByteBuf;
import javax.annotation.Nonnull;

public class Mat4f {
   public static final int SIZE = 64;
   public final float m11;
   public final float m12;
   public final float m13;
   public final float m14;
   public final float m21;
   public final float m22;
   public final float m23;
   public final float m24;
   public final float m31;
   public final float m32;
   public final float m33;
   public final float m34;
   public final float m41;
   public final float m42;
   public final float m43;
   public final float m44;

   public Mat4f(
      float m11,
      float m12,
      float m13,
      float m14,
      float m21,
      float m22,
      float m23,
      float m24,
      float m31,
      float m32,
      float m33,
      float m34,
      float m41,
      float m42,
      float m43,
      float m44
   ) {
      this.m11 = m11;
      this.m12 = m12;
      this.m13 = m13;
      this.m14 = m14;
      this.m21 = m21;
      this.m22 = m22;
      this.m23 = m23;
      this.m24 = m24;
      this.m31 = m31;
      this.m32 = m32;
      this.m33 = m33;
      this.m34 = m34;
      this.m41 = m41;
      this.m42 = m42;
      this.m43 = m43;
      this.m44 = m44;
   }

   @Nonnull
   public static Mat4f identity() {
      return new Mat4f(1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F);
   }

   @Nonnull
   public static Mat4f deserialize(@Nonnull ByteBuf buf, int offset) {
      return new Mat4f(
         Float.intBitsToFloat(buf.getIntLE(offset)),
         Float.intBitsToFloat(buf.getIntLE(offset + 4)),
         Float.intBitsToFloat(buf.getIntLE(offset + 8)),
         Float.intBitsToFloat(buf.getIntLE(offset + 12)),
         Float.intBitsToFloat(buf.getIntLE(offset + 16)),
         Float.intBitsToFloat(buf.getIntLE(offset + 20)),
         Float.intBitsToFloat(buf.getIntLE(offset + 24)),
         Float.intBitsToFloat(buf.getIntLE(offset + 28)),
         Float.intBitsToFloat(buf.getIntLE(offset + 32)),
         Float.intBitsToFloat(buf.getIntLE(offset + 36)),
         Float.intBitsToFloat(buf.getIntLE(offset + 40)),
         Float.intBitsToFloat(buf.getIntLE(offset + 44)),
         Float.intBitsToFloat(buf.getIntLE(offset + 48)),
         Float.intBitsToFloat(buf.getIntLE(offset + 52)),
         Float.intBitsToFloat(buf.getIntLE(offset + 56)),
         Float.intBitsToFloat(buf.getIntLE(offset + 60))
      );
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeIntLE(Float.floatToRawIntBits(this.m11));
      buf.writeIntLE(Float.floatToRawIntBits(this.m12));
      buf.writeIntLE(Float.floatToRawIntBits(this.m13));
      buf.writeIntLE(Float.floatToRawIntBits(this.m14));
      buf.writeIntLE(Float.floatToRawIntBits(this.m21));
      buf.writeIntLE(Float.floatToRawIntBits(this.m22));
      buf.writeIntLE(Float.floatToRawIntBits(this.m23));
      buf.writeIntLE(Float.floatToRawIntBits(this.m24));
      buf.writeIntLE(Float.floatToRawIntBits(this.m31));
      buf.writeIntLE(Float.floatToRawIntBits(this.m32));
      buf.writeIntLE(Float.floatToRawIntBits(this.m33));
      buf.writeIntLE(Float.floatToRawIntBits(this.m34));
      buf.writeIntLE(Float.floatToRawIntBits(this.m41));
      buf.writeIntLE(Float.floatToRawIntBits(this.m42));
      buf.writeIntLE(Float.floatToRawIntBits(this.m43));
      buf.writeIntLE(Float.floatToRawIntBits(this.m44));
   }
}
