package com.hypixel.hytale.builtin.hytalegenerator.materialproviders.spaceanddepth.layers;

import com.hypixel.hytale.builtin.hytalegenerator.materialproviders.MaterialProvider;
import com.hypixel.hytale.builtin.hytalegenerator.materialproviders.spaceanddepth.SpaceAndDepthMaterialProvider;
import com.hypixel.hytale.builtin.hytalegenerator.rng.RngField;
import com.hypixel.hytale.builtin.hytalegenerator.rng.SeedBox;
import com.hypixel.hytale.math.util.FastRandom;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RangedThicknessLayer<V> extends SpaceAndDepthMaterialProvider.Layer<V> {
   private final int min;
   private final int max;
   private final int delta;
   @Nonnull
   private final RngField rngField;
   @Nullable
   private final MaterialProvider<V> materialProvider;

   public RangedThicknessLayer(int minInclusive, int maxInclusive, @Nonnull SeedBox seedBox, @Nullable MaterialProvider<V> materialProvider) {
      this.min = minInclusive;
      this.max = maxInclusive;
      this.delta = this.max - this.min;
      if (this.delta < 0) {
         throw new IllegalArgumentException("min greater than max");
      } else {
         this.rngField = new RngField(seedBox.createSupplier().get());
         this.materialProvider = materialProvider;
      }
   }

   @Override
   public int getThicknessAt(
      int x, int y, int z, int depthIntoFloor, int depthIntoCeiling, int spaceAboveFloor, int spaceBelowCeiling, double distanceTOBiomeEdge
   ) {
      if (this.delta <= 0) {
         return this.min;
      } else {
         FastRandom random = new FastRandom(this.rngField.get(x, z));
         return random.nextInt(this.delta + 1) + this.min;
      }
   }

   @Override
   public MaterialProvider<V> getMaterialProvider() {
      return this.materialProvider;
   }
}
