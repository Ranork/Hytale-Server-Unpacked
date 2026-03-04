package com.hypixel.hytale.builtin.hytalegenerator.materialproviders;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class UpwardDepthMaterialProvider<V> extends MaterialProvider<V> {
   @Nonnull
   private final MaterialProvider<V> materialProvider;
   private final int depth;

   public UpwardDepthMaterialProvider(@Nonnull MaterialProvider<V> materialProvider, int depth) {
      this.materialProvider = materialProvider;
      this.depth = depth;
   }

   @Nullable
   @Override
   public V getVoxelTypeAt(@Nonnull MaterialProvider.Context context) {
      return this.depth != context.depthIntoCeiling ? null : this.materialProvider.getVoxelTypeAt(context);
   }
}
