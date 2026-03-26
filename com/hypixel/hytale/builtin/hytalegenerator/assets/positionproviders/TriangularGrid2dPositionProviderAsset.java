package com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders;

import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.EmptyPositionProvider;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.PositionProvider;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.TriangularGrid2dPositionProvider;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class TriangularGrid2dPositionProviderAsset extends PositionProviderAsset {
   @Nonnull
   public static final BuilderCodec<TriangularGrid2dPositionProviderAsset> CODEC = BuilderCodec.builder(
         TriangularGrid2dPositionProviderAsset.class, TriangularGrid2dPositionProviderAsset::new, PositionProviderAsset.ABSTRACT_CODEC
      )
      .build();

   @Nonnull
   @Override
   public PositionProvider build(@Nonnull PositionProviderAsset.Argument argument) {
      return (PositionProvider)(super.skip() ? EmptyPositionProvider.INSTANCE : new TriangularGrid2dPositionProvider());
   }
}
