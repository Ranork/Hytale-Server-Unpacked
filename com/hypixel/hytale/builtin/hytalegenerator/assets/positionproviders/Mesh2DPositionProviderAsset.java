package com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders;

import com.hypixel.hytale.builtin.hytalegenerator.assets.pointgenerators.NoPointGeneratorAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.pointgenerators.PointGeneratorAsset;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.Mesh2DPositionProvider;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.PositionProvider;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class Mesh2DPositionProviderAsset extends PositionProviderAsset {
   @Nonnull
   public static final BuilderCodec<Mesh2DPositionProviderAsset> CODEC = BuilderCodec.builder(
         Mesh2DPositionProviderAsset.class, Mesh2DPositionProviderAsset::new, PositionProviderAsset.ABSTRACT_CODEC
      )
      .append(
         new KeyedCodec<>("PointGenerator", PointGeneratorAsset.CODEC, true), (asset, v) -> asset.pointGeneratorAsset = v, asset -> asset.pointGeneratorAsset
      )
      .add()
      .append(new KeyedCodec<>("PointsY", Codec.INTEGER, true), (asset, v) -> asset.y = v, asset -> asset.y)
      .add()
      .build();
   private PointGeneratorAsset pointGeneratorAsset = new NoPointGeneratorAsset();
   private int y = 0;

   @Nonnull
   @Override
   public PositionProvider build(@Nonnull PositionProviderAsset.Argument argument) {
      return (PositionProvider)(super.skip()
         ? PositionProvider.noPositionProvider()
         : new Mesh2DPositionProvider(this.pointGeneratorAsset.build(argument.parentSeed), this.y));
   }
}
