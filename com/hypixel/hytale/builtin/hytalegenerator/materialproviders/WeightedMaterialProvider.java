package com.hypixel.hytale.builtin.hytalegenerator.materialproviders;

import com.hypixel.hytale.builtin.hytalegenerator.WeightedMap;
import com.hypixel.hytale.builtin.hytalegenerator.rng.RngField;
import com.hypixel.hytale.builtin.hytalegenerator.rng.SeedBox;
import com.hypixel.hytale.math.util.FastRandom;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WeightedMaterialProvider<V> extends MaterialProvider<V> {
   @Nonnull
   private final WeightedMap<MaterialProvider<V>> weightedMap;
   @Nonnull
   private final RngField rngField;
   @Nonnull
   private final FastRandom random;
   private final double noneProbability;

   public WeightedMaterialProvider(@Nonnull WeightedMap<MaterialProvider<V>> weightedMap, @Nonnull SeedBox seedBox, double noneProbability) {
      this.weightedMap = weightedMap;
      this.rngField = new RngField(seedBox.createSupplier().get());
      this.noneProbability = noneProbability;
      this.random = new FastRandom();
   }

   @Nullable
   @Override
   public V getVoxelTypeAt(@Nonnull MaterialProvider.Context context) {
      int localSeed = this.rngField.get(context.position.x, context.position.y, context.position.z);
      if (this.weightedMap.size() != 0 && !(this.random.nextDouble() < this.noneProbability)) {
         MaterialProvider<V> pick = this.weightedMap.pick(this.random);
         return pick.getVoxelTypeAt(context);
      } else {
         return null;
      }
   }
}
