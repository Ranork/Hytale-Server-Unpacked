package com.hypixel.hytale.builtin.hytalegenerator.assets.patterns;

import com.hypixel.hytale.builtin.hytalegenerator.patterns.Pattern;
import com.hypixel.hytale.builtin.hytalegenerator.patterns.SurfacePattern;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class CeilingPatternAsset extends PatternAsset {
   @Nonnull
   public static final BuilderCodec<CeilingPatternAsset> CODEC = BuilderCodec.builder(
         CeilingPatternAsset.class, CeilingPatternAsset::new, PatternAsset.ABSTRACT_CODEC
      )
      .append(new KeyedCodec<>("Ceiling", PatternAsset.CODEC, true), (t, k) -> t.ceiling = k, k -> k.ceiling)
      .add()
      .append(new KeyedCodec<>("Origin", PatternAsset.CODEC, true), (t, k) -> t.origin = k, k -> k.origin)
      .add()
      .build();
   private PatternAsset ceiling = new ConstantPatternAsset();
   private PatternAsset origin = new ConstantPatternAsset();

   @Nonnull
   @Override
   public Pattern build(@Nonnull PatternAsset.Argument argument) {
      if (super.isSkipped()) {
         return Pattern.noPattern();
      } else {
         Pattern ceilingPattern = this.ceiling.build(argument);
         Pattern originPattern = this.origin.build(argument);
         return new SurfacePattern(ceilingPattern, originPattern, 0.0, 0.0, SurfacePattern.Facing.D, 0, 0);
      }
   }

   @Override
   public void cleanUp() {
      this.ceiling.cleanUp();
      this.origin.cleanUp();
   }
}
