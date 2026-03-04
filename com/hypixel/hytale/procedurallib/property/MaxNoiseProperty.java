package com.hypixel.hytale.procedurallib.property;

import java.util.Arrays;
import javax.annotation.Nonnull;

public class MaxNoiseProperty implements NoiseProperty {
   public static final double MAX_EPSILON = 0.999999;
   protected final NoiseProperty[] noiseProperties;

   public MaxNoiseProperty(NoiseProperty[] noiseProperties) {
      this.noiseProperties = noiseProperties;
   }

   public NoiseProperty[] getNoiseProperties() {
      return this.noiseProperties;
   }

   @Override
   public double get(int seed, double x, double y) {
      double val = this.noiseProperties[0].get(seed, x, y);

      for (int i = 1; i < this.noiseProperties.length; i++) {
         if (val > 0.999999) {
            return 1.0;
         }

         double d;
         if (val < (d = this.noiseProperties[i].get(seed, x, y))) {
            val = d;
         }
      }

      return val;
   }

   @Override
   public double get(int seed, double x, double y, double z) {
      double val = this.noiseProperties[0].get(seed, x, y, z);

      for (int i = 1; i < this.noiseProperties.length; i++) {
         if (val > 0.999999) {
            return 1.0;
         }

         double d;
         if (val < (d = this.noiseProperties[i].get(seed, x, y, z))) {
            val = d;
         }
      }

      return val;
   }

   @Nonnull
   @Override
   public String toString() {
      return "MaxNoiseProperty{noiseProperties=" + Arrays.toString((Object[])this.noiseProperties) + "}";
   }
}
