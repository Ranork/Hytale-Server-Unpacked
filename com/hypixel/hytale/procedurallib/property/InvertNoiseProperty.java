package com.hypixel.hytale.procedurallib.property;

public class InvertNoiseProperty implements NoiseProperty {
   protected final NoiseProperty noiseProperty;

   public InvertNoiseProperty(NoiseProperty noiseProperty) {
      this.noiseProperty = noiseProperty;
   }

   @Override
   public double get(int seed, double x, double y) {
      return 1.0 - this.noiseProperty.get(seed, x, y);
   }

   @Override
   public double get(int seed, double x, double y, double z) {
      return 1.0 - this.noiseProperty.get(seed, x, y, z);
   }
}
