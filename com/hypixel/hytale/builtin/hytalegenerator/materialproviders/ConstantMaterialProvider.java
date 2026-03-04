package com.hypixel.hytale.builtin.hytalegenerator.materialproviders;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ConstantMaterialProvider<V> extends MaterialProvider<V> {
   @Nullable
   private final V material;

   public ConstantMaterialProvider(@Nullable V material) {
      this.material = material;
   }

   @Nullable
   @Override
   public V getVoxelTypeAt(@Nonnull MaterialProvider.Context context) {
      return this.material;
   }
}
