package com.hypixel.hytale.builtin.hytalegenerator.assets.props.prefabprop.directionality;

import com.hypixel.hytale.builtin.hytalegenerator.assets.patterns.ConstantPatternAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.patterns.PatternAsset;
import com.hypixel.hytale.builtin.hytalegenerator.props.directionality.Directionality;
import com.hypixel.hytale.builtin.hytalegenerator.props.directionality.RandomDirectionality;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class RandomDirectionalityAsset extends DirectionalityAsset {
   @Nonnull
   public static final BuilderCodec<RandomDirectionalityAsset> CODEC = BuilderCodec.builder(
         RandomDirectionalityAsset.class, RandomDirectionalityAsset::new, DirectionalityAsset.ABSTRACT_CODEC
      )
      .append(new KeyedCodec<>("Seed", Codec.STRING, true), (asset, v) -> asset.seed = v, asset -> asset.seed)
      .add()
      .append(new KeyedCodec<>("Pattern", PatternAsset.CODEC, true), (asset, v) -> asset.patternAsset = v, asset -> asset.patternAsset)
      .add()
      .build();
   private String seed = "A";
   private PatternAsset patternAsset = new ConstantPatternAsset();

   @Nonnull
   @Override
   public Directionality build(@Nonnull DirectionalityAsset.Argument argument) {
      return new RandomDirectionality(this.patternAsset.build(PatternAsset.argumentFrom(argument)), argument.parentSeed.child(this.seed).createSupplier().get());
   }
}
