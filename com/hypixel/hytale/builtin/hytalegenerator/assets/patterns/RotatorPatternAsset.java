package com.hypixel.hytale.builtin.hytalegenerator.assets.patterns;

import com.hypixel.hytale.builtin.hytalegenerator.assets.material.OrthogonalRotationAsset;
import com.hypixel.hytale.builtin.hytalegenerator.patterns.ConstantPattern;
import com.hypixel.hytale.builtin.hytalegenerator.patterns.Pattern;
import com.hypixel.hytale.builtin.hytalegenerator.patterns.RotatorPattern;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class RotatorPatternAsset extends PatternAsset {
   @Nonnull
   public static final BuilderCodec<RotatorPatternAsset> CODEC = BuilderCodec.builder(
         RotatorPatternAsset.class, RotatorPatternAsset::new, PatternAsset.ABSTRACT_CODEC
      )
      .append(new KeyedCodec<>("Pattern", PatternAsset.CODEC, true), (asset, value) -> asset.patternAsset = value, asset -> asset.patternAsset)
      .add()
      .append(new KeyedCodec<>("Rotation", OrthogonalRotationAsset.CODEC, true), (asset, value) -> asset.rotationAsset = value, asset -> asset.rotationAsset)
      .add()
      .build();
   @Nonnull
   private PatternAsset patternAsset = new ConstantPatternAsset();
   @Nonnull
   private OrthogonalRotationAsset rotationAsset = new OrthogonalRotationAsset();

   @Nonnull
   @Override
   public Pattern build(@Nonnull PatternAsset.Argument argument) {
      return (Pattern)(super.isSkipped()
         ? ConstantPattern.INSTANCE_FALSE
         : new RotatorPattern(this.patternAsset.build(argument), this.rotationAsset.build(), argument.materialCache));
   }

   @Override
   public void cleanUp() {
      this.patternAsset.cleanUp();
   }
}
