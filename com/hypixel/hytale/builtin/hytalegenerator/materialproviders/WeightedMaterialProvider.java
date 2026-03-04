package com.hypixel.hytale.builtin.hytalegenerator.materialproviders;

import com.hypixel.hytale.builtin.hytalegenerator.datastructures.WeightedMap;
import com.hypixel.hytale.builtin.hytalegenerator.framework.math.SeedGenerator;
import com.hypixel.hytale.builtin.hytalegenerator.seed.SeedBox;
import com.hypixel.hytale.math.util.FastRandom;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WeightedMaterialProvider<V> extends MaterialProvider<V> {
   @Nonnull
   private final WeightedMap<MaterialProvider<V>> weightedMap;
   @Nonnull
   private final SeedGenerator seedGenerator;
   private final double noneProbability;

   public WeightedMaterialProvider(@Nonnull WeightedMap<MaterialProvider<V>> weightedMap, @Nonnull SeedBox seedBox, double noneProbability) {
      this.weightedMap = weightedMap;
      this.seedGenerator = new SeedGenerator(seedBox.createSupplier().get().intValue());
      this.noneProbability = noneProbability;
   }

   @Nullable
   @Override
   public V getVoxelTypeAt(@Nonnull MaterialProvider.Context context) {
      long seed = this.seedGenerator.seedAt((long)context.position.x, (long)context.position.y, (long)context.position.z);
      FastRandom random = new FastRandom(seed);
      if (this.weightedMap.size() != 0 && !(random.nextDouble() < this.noneProbability)) {
         MaterialProvider<V> pick = this.weightedMap.pick(random);
         return pick.getVoxelTypeAt(context);
      } else {
         return null;
      }
   }
}
