package com.hypixel.hytale.builtin.hytalegenerator.materialproviders.spaceanddepth.layers;

import com.hypixel.hytale.builtin.hytalegenerator.datastructures.WeightedMap;
import com.hypixel.hytale.builtin.hytalegenerator.framework.math.SeedGenerator;
import com.hypixel.hytale.builtin.hytalegenerator.materialproviders.MaterialProvider;
import com.hypixel.hytale.builtin.hytalegenerator.materialproviders.spaceanddepth.SpaceAndDepthMaterialProvider;
import com.hypixel.hytale.builtin.hytalegenerator.seed.SeedBox;
import com.hypixel.hytale.math.util.FastRandom;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WeightedThicknessLayer<V> extends SpaceAndDepthMaterialProvider.Layer<V> {
   @Nonnull
   private final WeightedMap<Integer> thicknessPool;
   @Nonnull
   private final SeedGenerator seedGenerator;
   @Nullable
   private final MaterialProvider<V> materialProvider;

   public WeightedThicknessLayer(@Nonnull WeightedMap<Integer> thicknessPool, @Nullable MaterialProvider<V> materialProvider, @Nonnull SeedBox seedBox) {
      this.seedGenerator = new SeedGenerator(seedBox.createSupplier().get().intValue());
      this.materialProvider = materialProvider;
      this.thicknessPool = thicknessPool;
   }

   @Override
   public int getThicknessAt(
      int x, int y, int z, int depthIntoFloor, int depthIntoCeiling, int spaceAboveFloor, int spaceBelowCeiling, double distanceTOBiomeEdge
   ) {
      if (this.thicknessPool.size() == 0) {
         return 0;
      } else {
         FastRandom random = new FastRandom(this.seedGenerator.seedAt(x, z));
         return this.thicknessPool.pick(random);
      }
   }

   @Override
   public MaterialProvider<V> getMaterialProvider() {
      return this.materialProvider;
   }
}
