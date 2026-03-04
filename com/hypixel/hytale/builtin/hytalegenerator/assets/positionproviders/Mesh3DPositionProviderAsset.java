package com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders;

import com.hypixel.hytale.builtin.hytalegenerator.assets.pointgenerators.NoPointGeneratorAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.pointgenerators.PointGeneratorAsset;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.Mesh3DPositionProvider;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.PositionProvider;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class Mesh3DPositionProviderAsset extends PositionProviderAsset {
   @Nonnull
   public static final BuilderCodec<Mesh3DPositionProviderAsset> CODEC = BuilderCodec.builder(
         Mesh3DPositionProviderAsset.class, Mesh3DPositionProviderAsset::new, PositionProviderAsset.ABSTRACT_CODEC
      )
      .append(
         new KeyedCodec<>("PointGenerator", PointGeneratorAsset.CODEC, true), (asset, v) -> asset.pointGeneratorAsset = v, asset -> asset.pointGeneratorAsset
      )
      .add()
      .build();
   private PointGeneratorAsset pointGeneratorAsset = new NoPointGeneratorAsset();

   @Nonnull
   @Override
   public PositionProvider build(@Nonnull PositionProviderAsset.Argument argument) {
      return (PositionProvider)(super.skip()
         ? PositionProvider.noPositionProvider()
         : new Mesh3DPositionProvider(this.pointGeneratorAsset.build(argument.parentSeed)));
   }
}
