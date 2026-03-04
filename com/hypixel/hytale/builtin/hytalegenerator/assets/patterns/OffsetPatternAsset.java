package com.hypixel.hytale.builtin.hytalegenerator.assets.patterns;

import com.hypixel.hytale.builtin.hytalegenerator.patterns.OffsetPattern;
import com.hypixel.hytale.builtin.hytalegenerator.patterns.Pattern;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.vector.Vector3i;
import javax.annotation.Nonnull;

public class OffsetPatternAsset extends PatternAsset {
   @Nonnull
   public static final BuilderCodec<OffsetPatternAsset> CODEC = BuilderCodec.builder(
         OffsetPatternAsset.class, OffsetPatternAsset::new, PatternAsset.ABSTRACT_CODEC
      )
      .append(new KeyedCodec<>("Pattern", PatternAsset.CODEC, true), (t, k) -> t.patternAsset = k, k -> k.patternAsset)
      .add()
      .append(new KeyedCodec<>("Offset", Vector3i.CODEC, true), (t, k) -> t.offset = k, k -> k.offset)
      .add()
      .build();
   private PatternAsset patternAsset = new ConstantPatternAsset();
   private Vector3i offset = new Vector3i();

   @Nonnull
   @Override
   public Pattern build(@Nonnull PatternAsset.Argument argument) {
      if (super.isSkipped()) {
         return Pattern.noPattern();
      } else {
         Pattern pattern = this.patternAsset.build(argument);
         return new OffsetPattern(pattern, this.offset.clone());
      }
   }

   @Override
   public void cleanUp() {
      this.patternAsset.cleanUp();
   }
}
