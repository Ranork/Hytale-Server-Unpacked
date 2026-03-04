package com.hypixel.hytale.math.util;

import javax.annotation.Nonnull;

public class TrigMathUtil {
   public static final float PI = (float) Math.PI;
   public static final float PI_HALF = (float) (Math.PI / 2);
   public static final float PI_QUARTER = (float) (Math.PI / 4);
   public static final float PI2 = (float) (Math.PI * 2);
   public static final float PI4 = (float) (Math.PI * 4);
   public static final float radToDeg = 180.0F / (float)Math.PI;
   public static final float degToRad = (float) (Math.PI / 180.0);

   public static float sin(float radians) {
      return TrigMathUtil.Riven.sin(radians);
   }

   public static float cos(float radians) {
      return TrigMathUtil.Riven.cos(radians);
   }

   public static float sin(double radians) {
      return TrigMathUtil.Riven.sin((float)radians);
   }

   public static float cos(double radians) {
      return TrigMathUtil.Riven.cos((float)radians);
   }

   public static float atan2(float y, float x) {
      return TrigMathUtil.Icecore.atan2(y, x);
   }

   public static float atan2(double y, double x) {
      return TrigMathUtil.Icecore.atan2((float)y, (float)x);
   }

   public static float atan(double d) {
      return (float)Math.atan(d);
   }

   public static float asin(double d) {
      return (float)Math.asin(d);
   }

   private TrigMathUtil() {
   }

   private static final class Icecore {
      private static final int SIZE_AC = 100000;
      private static final int SIZE_AR = 100001;
      private static final float[] ATAN2 = new float[100001];

      public static float atan2(float y, float x) {
         if (y < 0.0F) {
            if (x < 0.0F) {
               return y < x ? -ATAN2[(int)(x / y * 100000.0F)] - (float) (Math.PI / 2) : ATAN2[(int)(y / x * 100000.0F)] - (float) Math.PI;
            } else {
               y = -y;
               return y > x ? ATAN2[(int)(x / y * 100000.0F)] - (float) (Math.PI / 2) : -ATAN2[(int)(y / x * 100000.0F)];
            }
         } else if (x < 0.0F) {
            x = -x;
            return y > x ? ATAN2[(int)(x / y * 100000.0F)] + (float) (Math.PI / 2) : -ATAN2[(int)(y / x * 100000.0F)] + (float) Math.PI;
         } else {
            return y > x ? -ATAN2[(int)(x / y * 100000.0F)] + (float) (Math.PI / 2) : ATAN2[(int)(y / x * 100000.0F)];
         }
      }

      static {
         for (int i = 0; i <= 100000; i++) {
            double d = i / 100000.0;
            double x = 1.0;
            double y = x * d;
            float v = (float)Math.atan2(y, x);
            ATAN2[i] = v;
         }
      }
   }

   private static final class Riven {
      private static final int SIN_BITS = 12;
      private static final int SIN_MASK = ~(-1 << SIN_BITS);
      private static final int SIN_COUNT = SIN_MASK + 1;
      private static final float radFull = (float) (Math.PI * 2);
      private static final float radToIndex = SIN_COUNT / radFull;
      private static final float degFull = 360.0F;
      private static final float degToIndex = SIN_COUNT / degFull;
      @Nonnull
      private static final float[] SIN = new float[SIN_COUNT];
      @Nonnull
      private static final float[] COS = new float[SIN_COUNT];

      public static float sin(float rad) {
         return SIN[(int)(rad * radToIndex) & SIN_MASK];
      }

      public static float cos(float rad) {
         return COS[(int)(rad * radToIndex) & SIN_MASK];
      }

      static {
         for (int i = 0; i < SIN_COUNT; i++) {
            SIN[i] = (float)Math.sin((i + 0.5F) / SIN_COUNT * radFull);
            COS[i] = (float)Math.cos((i + 0.5F) / SIN_COUNT * radFull);
         }

         for (int i = 0; i < 360; i += 90) {
            SIN[(int)(i * degToIndex) & SIN_MASK] = (float)Math.sin(i * Math.PI / 180.0);
            COS[(int)(i * degToIndex) & SIN_MASK] = (float)Math.cos(i * Math.PI / 180.0);
         }
      }
   }
}
