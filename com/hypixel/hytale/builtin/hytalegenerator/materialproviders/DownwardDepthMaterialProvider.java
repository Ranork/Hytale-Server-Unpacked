package com.hypixel.hytale.builtin.hytalegenerator.materialproviders;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DownwardDepthMaterialProvider<V> extends MaterialProvider<V> {
   @Nonnull
   private final MaterialProvider<V> materialProvider;
   private final int depth;

   public DownwardDepthMaterialProvider(@Nonnull MaterialProvider<V> materialProvider, int depth) {
      this.materialProvider = materialProvider;
      this.depth = depth;
   }

   @Nullable
   @Override
   public V getVoxelTypeAt(@Nonnull MaterialProvider.Context context) {
      return this.depth != context.depthIntoFloor ? null : this.materialProvider.getVoxelTypeAt(context);
   }
}
