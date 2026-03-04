package com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders;

import com.hypixel.hytale.builtin.hytalegenerator.assets.bounds.DecimalBounds3dAsset;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.BoundPositionProvider;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.PositionProvider;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class BoundPositionProviderAsset extends PositionProviderAsset {
   @Nonnull
   public static final BuilderCodec<BoundPositionProviderAsset> CODEC = BuilderCodec.builder(
         BoundPositionProviderAsset.class, BoundPositionProviderAsset::new, PositionProviderAsset.ABSTRACT_CODEC
      )
      .append(new KeyedCodec<>("Bounds", DecimalBounds3dAsset.CODEC, true), (asset, value) -> asset.bounds = value, asset -> asset.bounds)
      .add()
      .append(
         new KeyedCodec<>("Positions", PositionProviderAsset.CODEC, true),
         (asset, value) -> asset.positionProviderAsset = value,
         asset -> asset.positionProviderAsset
      )
      .add()
      .build();
   private DecimalBounds3dAsset bounds = new DecimalBounds3dAsset();
   private PositionProviderAsset positionProviderAsset = new ListPositionProviderAsset();

   @Nonnull
   @Override
   public PositionProvider build(@Nonnull PositionProviderAsset.Argument argument) {
      return (PositionProvider)(super.skip()
         ? PositionProvider.noPositionProvider()
         : new BoundPositionProvider(this.positionProviderAsset.build(argument), this.bounds.build()));
   }

   @Override
   public void cleanUp() {
      this.positionProviderAsset.cleanUp();
   }
}
