package com.hypixel.hytale.procedurallib.property;

import java.util.Arrays;
import javax.annotation.Nonnull;

public class MinNoiseProperty implements NoiseProperty {
   public static final double MIN_EPSILON = 1.0E-5;
   protected final NoiseProperty[] noiseProperties;

   public MinNoiseProperty(NoiseProperty[] noiseProperties) {
      this.noiseProperties = noiseProperties;
   }

   public NoiseProperty[] getNoiseProperties() {
      return this.noiseProperties;
   }

   @Override
   public double get(int seed, double x, double y) {
      double val = this.noiseProperties[0].get(seed, x, y);

      for (int i = 1; i < this.noiseProperties.length; i++) {
         if (val < 1.0E-5) {
            return 0.0;
         }

         double d;
         if (val > (d = this.noiseProperties[i].get(seed, x, y))) {
            val = d;
         }
      }

      return val;
   }

   @Override
   public double get(int seed, double x, double y, double z) {
      double val = this.noiseProperties[0].get(seed, x, y, z);

      for (int i = 1; i < this.noiseProperties.length; i++) {
         if (val < 1.0E-5) {
            return 0.0;
         }

         double d;
         if (val > (d = this.noiseProperties[i].get(seed, x, y, z))) {
            val = d;
         }
      }

      return val;
   }

   @Nonnull
   @Override
   public String toString() {
      return "MinNoiseProperty{noiseProperties=" + Arrays.toString((Object[])this.noiseProperties) + "}";
   }
}
