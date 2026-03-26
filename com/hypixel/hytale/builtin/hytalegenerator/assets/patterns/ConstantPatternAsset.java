package com.hypixel.hytale.builtin.hytalegenerator.assets.patterns;

import com.hypixel.hytale.builtin.hytalegenerator.patterns.ConstantPattern;
import com.hypixel.hytale.builtin.hytalegenerator.patterns.Pattern;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class ConstantPatternAsset extends PatternAsset {
   @Nonnull
   public static final BuilderCodec<ConstantPatternAsset> CODEC = BuilderCodec.builder(
         ConstantPatternAsset.class, ConstantPatternAsset::new, PatternAsset.ABSTRACT_CODEC
      )
      .append(new KeyedCodec<>("Value", Codec.BOOLEAN, true), (asset, value) -> asset.value = value, value -> value.value)
      .add()
      .build();
   private boolean value = false;

   @Nonnull
   @Override
   public Pattern build(@Nonnull PatternAsset.Argument argument) {
      if (super.isSkipped()) {
         return ConstantPattern.INSTANCE_FALSE;
      } else {
         return this.value ? ConstantPattern.INSTANCE_TRUE : ConstantPattern.INSTANCE_FALSE;
      }
   }
}
