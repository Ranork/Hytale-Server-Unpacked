package com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders;

import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.EmptyPositionProvider;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.PositionProvider;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.SquareGrid3dPositionProvider;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class SquareGrid3dPositionProviderAsset extends PositionProviderAsset {
   @Nonnull
   public static final BuilderCodec<SquareGrid3dPositionProviderAsset> CODEC = BuilderCodec.builder(
         SquareGrid3dPositionProviderAsset.class, SquareGrid3dPositionProviderAsset::new, PositionProviderAsset.ABSTRACT_CODEC
      )
      .build();

   @Nonnull
   @Override
   public PositionProvider build(@Nonnull PositionProviderAsset.Argument argument) {
      return (PositionProvider)(super.skip() ? EmptyPositionProvider.INSTANCE : new SquareGrid3dPositionProvider());
   }
}
