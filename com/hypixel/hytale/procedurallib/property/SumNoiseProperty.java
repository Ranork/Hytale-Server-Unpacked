package com.hypixel.hytale.procedurallib.property;

import com.hypixel.hytale.procedurallib.logic.GeneralNoise;
import java.util.Arrays;
import javax.annotation.Nonnull;

public class SumNoiseProperty implements NoiseProperty {
   protected final SumNoiseProperty.Entry[] entries;

   public SumNoiseProperty(SumNoiseProperty.Entry[] entries) {
      this.entries = entries;
   }

   public SumNoiseProperty.Entry[] getEntries() {
      return this.entries;
   }

   @Override
   public double get(int seed, double x, double y) {
      double val = 0.0;

      for (SumNoiseProperty.Entry entry : this.entries) {
         val += entry.noiseProperty.get(seed, x, y) * entry.factor;
      }

      return GeneralNoise.limit(val);
   }

   @Override
   public double get(int seed, double x, double y, double z) {
      double val = 0.0;

      for (SumNoiseProperty.Entry entry : this.entries) {
         val += entry.noiseProperty.get(seed, x, y, z) * entry.factor;
      }

      return GeneralNoise.limit(val);
   }

   @Nonnull
   @Override
   public String toString() {
      return "SumNoiseProperty{entries=" + Arrays.toString((Object[])this.entries) + "}";
   }

   public static class Entry {
      private NoiseProperty noiseProperty;
      private double factor;

      public Entry(NoiseProperty noiseProperty, double factor) {
         this.noiseProperty = noiseProperty;
         this.factor = factor;
      }

      public NoiseProperty getNoiseProperty() {
         return this.noiseProperty;
      }

      public void setNoiseProperty(NoiseProperty noiseProperty) {
         this.noiseProperty = noiseProperty;
      }

      public double getFactor() {
         return this.factor;
      }

      public void setFactor(double factor) {
         this.factor = factor;
      }

      @Nonnull
      @Override
      public String toString() {
         return "Entry{noiseProperty=" + this.noiseProperty + ", factor=" + this.factor + "}";
      }
   }
}
