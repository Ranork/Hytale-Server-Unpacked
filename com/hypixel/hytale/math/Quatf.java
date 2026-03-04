package com.hypixel.hytale.math;

import io.netty.buffer.ByteBuf;
import javax.annotation.Nonnull;

public class Quatf {
   public static final int SIZE = 16;
   public final float x;
   public final float y;
   public final float z;
   public final float w;

   public Quatf(float x, float y, float z, float w) {
      this.x = x;
      this.y = y;
      this.z = z;
      this.w = w;
   }

   @Nonnull
   public static Quatf deserialize(@Nonnull ByteBuf buf, int offset) {
      return new Quatf(
         Float.intBitsToFloat(buf.getIntLE(offset)),
         Float.intBitsToFloat(buf.getIntLE(offset + 4)),
         Float.intBitsToFloat(buf.getIntLE(offset + 8)),
         Float.intBitsToFloat(buf.getIntLE(offset + 12))
      );
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeIntLE(Float.floatToRawIntBits(this.x));
      buf.writeIntLE(Float.floatToRawIntBits(this.y));
      buf.writeIntLE(Float.floatToRawIntBits(this.z));
      buf.writeIntLE(Float.floatToRawIntBits(this.w));
   }
}
