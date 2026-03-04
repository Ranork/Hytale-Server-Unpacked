package com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders;

import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.PositionProvider;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.cached.CachedPositionProvider;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import javax.annotation.Nonnull;

public class CachedPositionProviderAsset extends PositionProviderAsset {
   @Nonnull
   public static final BuilderCodec<CachedPositionProviderAsset> CODEC = BuilderCodec.builder(
         CachedPositionProviderAsset.class, CachedPositionProviderAsset::new, PositionProviderAsset.ABSTRACT_CODEC
      )
      .append(new KeyedCodec<>("Positions", PositionProviderAsset.CODEC, true), (asset, v) -> asset.childAsset = v, asset -> asset.childAsset)
      .add()
      .<Integer>append(new KeyedCodec<>("SectionSize", Codec.INTEGER, true), (asset, v) -> asset.sectionSize = v, asset -> asset.sectionSize)
      .addValidator(Validators.greaterThan(0))
      .add()
      .<Integer>append(new KeyedCodec<>("CacheSize", Codec.INTEGER, true), (asset, v) -> asset.cacheSize = v, asset -> asset.cacheSize)
      .addValidator(Validators.greaterThan(-1))
      .add()
      .build();
   private PositionProviderAsset childAsset = new ListPositionProviderAsset();
   private int sectionSize = 32;
   private int cacheSize = 100;

   @Nonnull
   @Override
   public PositionProvider build(@Nonnull PositionProviderAsset.Argument argument) {
      if (super.skip()) {
         return PositionProvider.noPositionProvider();
      } else {
         PositionProvider childPositions = this.childAsset.build(argument);
         return new CachedPositionProvider(childPositions, this.sectionSize, this.cacheSize, false);
      }
   }

   @Override
   public void cleanUp() {
      this.childAsset.cleanUp();
   }
}
