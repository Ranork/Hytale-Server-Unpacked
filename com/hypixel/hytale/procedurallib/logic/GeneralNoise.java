package com.hypixel.hytale.procedurallib.logic;

import javax.annotation.Nonnull;

public final class GeneralNoise {
   public static final int X_PRIME = 1619;
   public static final int Y_PRIME = 31337;
   public static final int Z_PRIME = 6971;
   private static final DoubleArray.Double2[] GRAD_2D = new DoubleArray.Double2[]{
      new DoubleArray.Double2(-1.0, -1.0),
      new DoubleArray.Double2(1.0, -1.0),
      new DoubleArray.Double2(-1.0, 1.0),
      new DoubleArray.Double2(1.0, 1.0),
      new DoubleArray.Double2(0.0, -1.0),
      new DoubleArray.Double2(-1.0, 0.0),
      new DoubleArray.Double2(0.0, 1.0),
      new DoubleArray.Double2(1.0, 0.0)
   };
   private static final DoubleArray.Double3[] GRAD_3D = new DoubleArray.Double3[]{
      new DoubleArray.Double3(1.0, 1.0, 0.0),
      new DoubleArray.Double3(-1.0, 1.0, 0.0),
      new DoubleArray.Double3(1.0, -1.0, 0.0),
      new DoubleArray.Double3(-1.0, -1.0, 0.0),
      new DoubleArray.Double3(1.0, 0.0, 1.0),
      new DoubleArray.Double3(-1.0, 0.0, 1.0),
      new DoubleArray.Double3(1.0, 0.0, -1.0),
      new DoubleArray.Double3(-1.0, 0.0, -1.0),
      new DoubleArray.Double3(0.0, 1.0, 1.0),
      new DoubleArray.Double3(0.0, -1.0, 1.0),
      new DoubleArray.Double3(0.0, 1.0, -1.0),
      new DoubleArray.Double3(0.0, -1.0, -1.0),
      new DoubleArray.Double3(1.0, 1.0, 0.0),
      new DoubleArray.Double3(0.0, -1.0, 1.0),
      new DoubleArray.Double3(-1.0, 1.0, 0.0),
      new DoubleArray.Double3(0.0, -1.0, -1.0)
   };

   private GeneralNoise() {
      throw new UnsupportedOperationException();
   }

   public static int fastFloor(double f) {
      return f >= 0.0 ? (int)f : (int)f - 1;
   }

   public static int fastCeil(double f) {
      return f >= 0.0 ? (int)f + 1 : (int)f;
   }

   public static double lerp(double a, double b, double t) {
      return a + t * (b - a);
   }

   public static int hash2D(int seed, int x, int y) {
      int hash = seed ^ 1619 * x;
      hash ^= 31337 * y;
      hash = hash * hash * hash * 60493;
      return hash >> 13 ^ hash;
   }

   public static int hash3D(int seed, int x, int y, int z) {
      int hash = seed ^ 1619 * x;
      hash ^= 31337 * y;
      hash ^= 6971 * z;
      hash = hash * hash * hash * 60493;
      return hash >> 13 ^ hash;
   }

   public static double gradCoord2D(int seed, int x, int y, double xd, double yd) {
      int hash = hash2D(seed, x, y);
      DoubleArray.Double2 g = GRAD_2D[hash & 7];
      return xd * g.x + yd * g.y;
   }

   public static double gradCoord3D(int seed, int x, int y, int z, double xd, double yd, double zd) {
      int hash = hash3D(seed, x, y, z);
      DoubleArray.Double3 g = GRAD_3D[hash & 15];
      return xd * g.x + yd * g.y + zd * g.z;
   }

   public static double limit(double val) {
      if (val < 0.0) {
         return 0.0;
      } else {
         return val > 1.0 ? 1.0 : val;
      }
   }

   @FunctionalInterface
   public interface InterpolationFunction {
      double interpolate(double var1);
   }

   public static enum InterpolationMode {
      LINEAR(new GeneralNoise.InterpolationFunction() {
         @Override
         public double interpolate(double t) {
            return t;
         }

         @Nonnull
         @Override
         public String toString() {
            return "LinearInterpolationFunction{}";
         }
      }),
      HERMITE(new GeneralNoise.InterpolationFunction() {
         @Override
         public double interpolate(double t) {
            return t * t * (3.0 - 2.0 * t);
         }

         @Nonnull
         @Override
         public String toString() {
            return "HermiteInterpolationFunction{}";
         }
      }),
      QUINTIC(new GeneralNoise.InterpolationFunction() {
         @Override
         public double interpolate(double t) {
            return t * t * t * (t * (t * 6.0 - 15.0) + 10.0);
         }

         @Nonnull
         @Override
         public String toString() {
            return "QuinticInterpolationFunction{}";
         }
      });

      public final GeneralNoise.InterpolationFunction function;

      private InterpolationMode(GeneralNoise.InterpolationFunction function) {
         this.function = function;
      }

      public GeneralNoise.InterpolationFunction getFunction() {
         return this.function;
      }
   }
}
