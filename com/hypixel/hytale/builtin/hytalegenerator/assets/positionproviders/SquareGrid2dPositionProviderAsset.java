package com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders;

import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.EmptyPositionProvider;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.PositionProvider;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.SquareGrid2dPositionProvider;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class SquareGrid2dPositionProviderAsset extends PositionProviderAsset {
   @Nonnull
   public static final BuilderCodec<SquareGrid2dPositionProviderAsset> CODEC = BuilderCodec.builder(
         SquareGrid2dPositionProviderAsset.class, SquareGrid2dPositionProviderAsset::new, PositionProviderAsset.ABSTRACT_CODEC
      )
      .build();

   @Nonnull
   @Override
   public PositionProvider build(@Nonnull PositionProviderAsset.Argument argument) {
      return (PositionProvider)(super.skip() ? EmptyPositionProvider.INSTANCE : new SquareGrid2dPositionProvider());
   }
}
