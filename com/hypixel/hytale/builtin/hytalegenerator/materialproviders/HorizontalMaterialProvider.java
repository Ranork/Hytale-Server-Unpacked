package com.hypixel.hytale.builtin.hytalegenerator.materialproviders;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class HorizontalMaterialProvider<V> extends MaterialProvider<V> {
   @Nonnull
   private final MaterialProvider<V> materialProvider;
   private double topY;
   private double bottomY;

   public HorizontalMaterialProvider(@Nonnull MaterialProvider<V> materialProvider, double topY, double bottomY) {
      this.materialProvider = materialProvider;
      this.topY = topY;
      this.bottomY = bottomY;
   }

   @Nullable
   @Override
   public V getVoxelTypeAt(@Nonnull MaterialProvider.Context context) {
      return !(context.position.y >= this.topY) && !(context.position.y < this.bottomY) ? this.materialProvider.getVoxelTypeAt(context) : null;
   }
}
