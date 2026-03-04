package com.hypixel.hytale.builtin.hytalegenerator.materialproviders;

import javax.annotation.Nonnull;

public class SolidityMaterialProvider<V> extends MaterialProvider<V> {
   @Nonnull
   private final MaterialProvider<V> solidMaterialProvider;
   @Nonnull
   private final MaterialProvider<V> emptyMaterialProvider;

   public SolidityMaterialProvider(@Nonnull MaterialProvider<V> solidMaterialProvider, @Nonnull MaterialProvider<V> emptyMaterialProvider) {
      this.solidMaterialProvider = solidMaterialProvider;
      this.emptyMaterialProvider = emptyMaterialProvider;
   }

   @Override
   public V getVoxelTypeAt(@Nonnull MaterialProvider.Context context) {
      return context.depthIntoFloor <= 0 ? this.emptyMaterialProvider.getVoxelTypeAt(context) : this.solidMaterialProvider.getVoxelTypeAt(context);
   }
}
