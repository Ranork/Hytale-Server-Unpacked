package com.hypixel.hytale.procedurallib.property;

import com.hypixel.hytale.procedurallib.logic.GeneralNoise;
import javax.annotation.Nonnull;

public class NormalizeNoiseProperty implements NoiseProperty {
   protected final NoiseProperty noiseProperty;
   protected final double min;
   protected final double range;

   public NormalizeNoiseProperty(NoiseProperty noiseProperty, double min, double range) {
      this.noiseProperty = noiseProperty;
      this.min = min;
      this.range = range;
   }

   public NoiseProperty getNoiseProperty() {
      return this.noiseProperty;
   }

   public double getMin() {
      return this.min;
   }

   public double getRange() {
      return this.range;
   }

   @Override
   public double get(int seed, double x, double y) {
      return GeneralNoise.limit((this.noiseProperty.get(seed, x, y) - this.min) / this.range);
   }

   @Override
   public double get(int seed, double x, double y, double z) {
      return GeneralNoise.limit((this.noiseProperty.get(seed, x, y, z) - this.min) / this.range);
   }

   @Nonnull
   @Override
   public String toString() {
      return "NormalizeNoiseProperty{noiseProperty=" + this.noiseProperty + ", min=" + this.min + ", range=" + this.range + "}";
   }
}
