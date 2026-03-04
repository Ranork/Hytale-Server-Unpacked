package com.hypixel.hytale.server.worldgen.biome;

import com.hypixel.hytale.procedurallib.condition.IDoubleThreshold;
import com.hypixel.hytale.procedurallib.condition.IIntCondition;
import com.hypixel.hytale.procedurallib.property.NoiseProperty;
import com.hypixel.hytale.server.worldgen.zone.ZoneGeneratorResult;
import javax.annotation.Nonnull;

public class CustomBiomeGenerator {
   protected final NoiseProperty noiseProperty;
   protected final IDoubleThreshold threshold;
   protected final IIntCondition biomeMask;
   protected final int priority;

   public CustomBiomeGenerator(NoiseProperty noiseProperty, IDoubleThreshold threshold, IIntCondition biomeMask, int priority) {
      this.noiseProperty = noiseProperty;
      this.threshold = threshold;
      this.biomeMask = biomeMask;
      this.priority = priority;
   }

   public boolean shouldGenerateAt(int seed, double x, double z, @Nonnull ZoneGeneratorResult zoneResult, @Nonnull Biome customBiome) {
      double noise = this.noiseProperty.get(seed, x, z);
      if (zoneResult.getBorderDistance() < customBiome.getFadeContainer().getMaskFadeSum()) {
         double factor = customBiome.getFadeContainer().getMaskFactor(zoneResult);
         return this.isThreshold(noise, factor);
      } else {
         return this.isThreshold(noise);
      }
   }

   public boolean isThreshold(double d) {
      return this.threshold.eval(d);
   }

   public boolean isThreshold(double d, double factor) {
      return factor >= 1.0E-5 && this.threshold.eval(d, factor);
   }

   public boolean isValidParentBiome(int index) {
      return this.biomeMask.eval(index);
   }

   public int getPriority() {
      return this.priority;
   }

   @Nonnull
   @Override
   public String toString() {
      return "CustomBiomeGenerator{noiseProperty="
         + this.noiseProperty
         + ", threshold="
         + this.threshold
         + ", biomeMask="
         + this.biomeMask
         + ", priority="
         + this.priority
         + "}";
   }
}
