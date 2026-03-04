package com.hypixel.hytale.procedurallib.property;

import com.hypixel.hytale.math.util.MathUtil;

public class BlendNoiseProperty implements NoiseProperty {
   private final NoiseProperty alpha;
   private final NoiseProperty[] noises;
   private final double[] thresholds;
   private final transient double[] normalize;

   public BlendNoiseProperty(NoiseProperty alpha, NoiseProperty[] noises, double[] thresholds) {
      this.alpha = alpha;
      this.noises = noises;
      this.thresholds = thresholds;
      this.normalize = new double[thresholds.length];

      for (int i = 1; i < thresholds.length; i++) {
         if (thresholds[i] <= thresholds[i - 1]) {
            throw new IllegalStateException("Thresholds must be in ascending order");
         }

         this.normalize[i] = 1.0 / (thresholds[i] - thresholds[i - 1]);
      }
   }

   @Override
   public double get(int seed, double x, double y) {
      if (this.noises.length == 0) {
         return 0.0;
      } else {
         double alpha = this.alpha.get(seed, x, y);
         if (alpha <= this.thresholds[0]) {
            return this.noises[0].get(seed, x, y);
         } else if (alpha >= this.thresholds[this.thresholds.length - 1]) {
            return this.noises[this.noises.length - 1].get(seed, x, y);
         } else {
            for (int i = 1; i < this.noises.length; i++) {
               if (alpha <= this.thresholds[i]) {
                  double t = (alpha - this.thresholds[i - 1]) * this.normalize[i];
                  double lower = this.noises[i - 1].get(seed, x, y);
                  double upper = this.noises[i].get(seed, x, y);
                  return MathUtil.lerp(lower, upper, t);
               }
            }

            return 0.0;
         }
      }
   }

   @Override
   public double get(int seed, double x, double y, double z) {
      if (this.noises.length == 0) {
         return 0.0;
      } else {
         double alpha = this.alpha.get(seed, x, y, z);
         if (alpha <= this.thresholds[0]) {
            return this.noises[0].get(seed, x, y, z);
         } else if (alpha >= this.thresholds[this.thresholds.length - 1]) {
            return this.noises[this.noises.length - 1].get(seed, x, y, z);
         } else {
            for (int i = 1; i < this.noises.length; i++) {
               if (alpha <= this.thresholds[i]) {
                  double t = (alpha - this.thresholds[i - 1]) * this.normalize[i];
                  double lower = this.noises[i - 1].get(seed, x, y, z);
                  double upper = this.noises[i + 0].get(seed, x, y, z);
                  return MathUtil.lerp(lower, upper, t);
               }
            }

            return 0.0;
         }
      }
   }
}
