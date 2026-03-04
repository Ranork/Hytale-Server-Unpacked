package com.hypixel.hytale.procedurallib.property;

import java.util.Arrays;
import javax.annotation.Nonnull;

public class MultiplyNoiseProperty implements NoiseProperty {
   protected final NoiseProperty[] noiseProperties;

   public MultiplyNoiseProperty(NoiseProperty[] noiseProperties) {
      this.noiseProperties = noiseProperties;
   }

   public NoiseProperty[] getNoiseProperties() {
      return this.noiseProperties;
   }

   @Override
   public double get(int seed, double x, double y) {
      double val = this.noiseProperties[0].get(seed, x, y);

      for (int i = 1; i < this.noiseProperties.length; i++) {
         val *= this.noiseProperties[i].get(seed, x, y);
      }

      return val;
   }

   @Override
   public double get(int seed, double x, double y, double z) {
      double val = this.noiseProperties[0].get(seed, x, y, z);

      for (int i = 1; i < this.noiseProperties.length; i++) {
         val *= this.noiseProperties[i].get(seed, x, y, z);
      }

      return val;
   }

   @Nonnull
   @Override
   public String toString() {
      return "MultiplyNoiseProperty{noiseProperties=" + Arrays.toString((Object[])this.noiseProperties) + "}";
   }
}
