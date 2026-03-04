package com.hypixel.hytale.builtin.hytalegenerator.assets.patterns;

import com.hypixel.hytale.builtin.hytalegenerator.patterns.NotPattern;
import com.hypixel.hytale.builtin.hytalegenerator.patterns.Pattern;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class NotPatternAsset extends PatternAsset {
   @Nonnull
   public static final BuilderCodec<NotPatternAsset> CODEC = BuilderCodec.builder(NotPatternAsset.class, NotPatternAsset::new, PatternAsset.ABSTRACT_CODEC)
      .append(new KeyedCodec<>("Pattern", PatternAsset.CODEC, true), (t, k) -> t.patternAsset = k, k -> k.patternAsset)
      .add()
      .build();
   private PatternAsset patternAsset = new ConstantPatternAsset();

   @Nonnull
   @Override
   public Pattern build(@Nonnull PatternAsset.Argument argument) {
      return (Pattern)(super.isSkipped() ? Pattern.noPattern() : new NotPattern(this.patternAsset.build(argument)));
   }

   @Override
   public void cleanUp() {
      this.patternAsset.cleanUp();
   }
}
