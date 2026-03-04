package com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders;

import com.hypixel.hytale.builtin.hytalegenerator.assets.density.ConstantDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.DensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.FieldFunctionOccurrencePositionProvider;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.PositionProvider;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class FieldFunctionOccurrencePositionProviderAsset extends PositionProviderAsset {
   @Nonnull
   public static final BuilderCodec<FieldFunctionOccurrencePositionProviderAsset> CODEC = BuilderCodec.builder(
         FieldFunctionOccurrencePositionProviderAsset.class, FieldFunctionOccurrencePositionProviderAsset::new, PositionProviderAsset.ABSTRACT_CODEC
      )
      .append(new KeyedCodec<>("Seed", Codec.STRING, true), (asset, v) -> asset.seed = v, asset -> asset.seed)
      .add()
      .append(new KeyedCodec<>("FieldFunction", DensityAsset.CODEC, true), (asset, v) -> asset.densityAsset = v, asset -> asset.densityAsset)
      .add()
      .append(
         new KeyedCodec<>("Positions", PositionProviderAsset.CODEC, true), (asset, v) -> asset.positionProviderAsset = v, asset -> asset.positionProviderAsset
      )
      .add()
      .build();
   private String seed = "";
   private DensityAsset densityAsset = new ConstantDensityAsset();
   private PositionProviderAsset positionProviderAsset = new ListPositionProviderAsset();

   @Nonnull
   @Override
   public PositionProvider build(@Nonnull PositionProviderAsset.Argument argument) {
      if (super.skip()) {
         return PositionProvider.noPositionProvider();
      } else {
         Density functionTree = this.densityAsset.build(DensityAsset.from(argument));
         PositionProvider positionProvider = this.positionProviderAsset.build(argument);
         int intSeed = argument.parentSeed.child(this.seed).createSupplier().get();
         return new FieldFunctionOccurrencePositionProvider(functionTree, positionProvider, intSeed);
      }
   }

   @Override
   public void cleanUp() {
      this.densityAsset.cleanUp();
      this.positionProviderAsset.cleanUp();
   }
}
