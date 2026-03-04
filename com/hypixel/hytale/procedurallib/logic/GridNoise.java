package com.hypixel.hytale.procedurallib.logic;

import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.procedurallib.NoiseFunction;
import javax.annotation.Nonnull;

public class GridNoise implements NoiseFunction {
   protected final double thicknessX;
   protected final double thicknessY;
   protected final double thicknessZ;
   protected final double thicknessX_m1;
   protected final double thicknessY_m1;
   protected final double thicknessZ_m1;

   public GridNoise(double thicknessX, double thicknessY, double thicknessZ) {
      this.thicknessX = thicknessX;
      this.thicknessY = thicknessY;
      this.thicknessZ = thicknessZ;
      this.thicknessX_m1 = 1.0 - thicknessX;
      this.thicknessY_m1 = 1.0 - thicknessY;
      this.thicknessZ_m1 = 1.0 - thicknessZ;
   }

   @Override
   public double get(int seed, int offsetSeed, double x, double y) {
      x -= MathUtil.floor(x);
      y -= MathUtil.floor(y);
      double d = 1.0;
      if (x < this.thicknessX) {
         d = x / this.thicknessX;
      } else if (x > this.thicknessX_m1) {
         d = (1.0 - x) / this.thicknessX;
      }

      if (y < this.thicknessY) {
         double t = y / this.thicknessY;
         if (t < d) {
            d = t;
         }
      } else if (y > this.thicknessY_m1) {
         double t = (1.0 - y) / this.thicknessY;
         if (t < d) {
            d = t;
         }
      }

      return d * 2.0 - 1.0;
   }

   @Override
   public double get(int seed, int offsetSeed, double x, double y, double z) {
      x -= MathUtil.floor(x);
      y -= MathUtil.floor(y);
      z -= MathUtil.floor(z);
      double d = 1.0;
      if (x < this.thicknessX) {
         d = x / this.thicknessX;
      } else if (x > this.thicknessX_m1) {
         d = (1.0 - x) / this.thicknessX;
      }

      if (y < this.thicknessY) {
         double t = y / this.thicknessY;
         if (t < d) {
            d = t;
         }
      } else if (y > this.thicknessY_m1) {
         double t = (1.0 - y) / this.thicknessY;
         if (t < d) {
            d = t;
         }
      }

      if (z < this.thicknessZ) {
         double t = z / this.thicknessZ;
         if (t < d) {
            d = t;
         }
      } else if (z > this.thicknessZ_m1) {
         double t = (1.0 - z) / this.thicknessZ;
         if (t < d) {
            d = t;
         }
      }

      return d * 2.0 - 1.0;
   }

   @Nonnull
   @Override
   public String toString() {
      return "GridNoise{thicknessX=" + this.thicknessX + ", thicknessY=" + this.thicknessY + ", thicknessZ=" + this.thicknessZ + "}";
   }
}
