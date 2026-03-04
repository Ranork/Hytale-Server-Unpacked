package com.hypixel.hytale.builtin.hytalegenerator.materialproviders;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DownwardSpaceMaterialProvider<V> extends MaterialProvider<V> {
   @Nonnull
   private final MaterialProvider<V> materialProvider;
   private final int space;

   public DownwardSpaceMaterialProvider(@Nonnull MaterialProvider<V> materialProvider, int space) {
      this.materialProvider = materialProvider;
      this.space = space;
   }

   @Nullable
   @Override
   public V getVoxelTypeAt(@Nonnull MaterialProvider.Context context) {
      return this.space != context.spaceBelowCeiling ? null : this.materialProvider.getVoxelTypeAt(context);
   }
}
