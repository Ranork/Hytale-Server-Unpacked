package com.hypixel.hytale.math;

import io.netty.buffer.ByteBuf;
import javax.annotation.Nonnull;

public final class Vec2f {
   public static final int SIZE = 8;
   public float x;
   public float y;

   public Vec2f(float x, float y) {
      this.x = x;
      this.y = y;
   }

   public Vec2f() {
   }

   @Nonnull
   public static Vec2f deserialize(@Nonnull ByteBuf buf, int offset) {
      return new Vec2f(Float.intBitsToFloat(buf.getIntLE(offset)), Float.intBitsToFloat(buf.getIntLE(offset + 4)));
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeIntLE(Float.floatToRawIntBits(this.x));
      buf.writeIntLE(Float.floatToRawIntBits(this.y));
   }

   @Override
   public String toString() {
      return "Vec2f(" + this.x + ", " + this.y + ")";
   }
}
