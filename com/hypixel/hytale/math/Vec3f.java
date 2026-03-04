package com.hypixel.hytale.math;

import io.netty.buffer.ByteBuf;
import javax.annotation.Nonnull;

public final class Vec3f {
   public static final int SIZE = 12;
   public float x;
   public float y;
   public float z;

   public Vec3f(float x, float y, float z) {
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public Vec3f() {
   }

   @Nonnull
   public static Vec3f deserialize(@Nonnull ByteBuf buf, int offset) {
      return new Vec3f(
         Float.intBitsToFloat(buf.getIntLE(offset)), Float.intBitsToFloat(buf.getIntLE(offset + 4)), Float.intBitsToFloat(buf.getIntLE(offset + 8))
      );
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeIntLE(Float.floatToRawIntBits(this.x));
      buf.writeIntLE(Float.floatToRawIntBits(this.y));
      buf.writeIntLE(Float.floatToRawIntBits(this.z));
   }

   @Override
   public String toString() {
      return "Vec3f(" + this.x + ", " + this.y + ", " + this.z + ")";
   }
}
