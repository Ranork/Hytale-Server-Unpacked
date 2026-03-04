package com.hypixel.hytale.procedurallib.logic;

import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.procedurallib.NoiseFunction;

public class PointNoise implements NoiseFunction {
   private final double x;
   private final double y;
   private final double z;
   private final double innerRadius2;
   private final double outerRadius2;
   private final transient double invRange2;

   public PointNoise(double x, double y, double z, double innerRadius, double outerRadius) {
      this.x = x;
      this.y = y;
      this.z = z;
      this.innerRadius2 = innerRadius * innerRadius;
      this.outerRadius2 = outerRadius * outerRadius;
      double range = this.outerRadius2 - this.innerRadius2;
      this.invRange2 = range == 0.0 ? 1.0 : 1.0 / range;
   }

   @Override
   public double get(int seed, int seedOffset, double x, double y) {
      double dist2 = MathUtil.lengthSquared(x - this.x, y - this.y);
      if (dist2 <= this.innerRadius2) {
         return -1.0;
      } else {
         return dist2 >= this.outerRadius2 ? 1.0 : -1.0 + 2.0 * (dist2 - this.innerRadius2) * this.invRange2;
      }
   }

   @Override
   public double get(int seed, int seedOffset, double x, double y, double z) {
      double dist2 = MathUtil.lengthSquared(x - this.x, y - this.y, this.z - z);
      if (dist2 <= this.innerRadius2) {
         return -1.0;
      } else {
         return dist2 >= this.outerRadius2 ? 1.0 : -1.0 + 2.0 * (dist2 - this.innerRadius2) * this.invRange2;
      }
   }
}
