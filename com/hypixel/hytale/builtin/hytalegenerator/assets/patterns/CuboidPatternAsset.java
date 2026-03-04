package com.hypixel.hytale.builtin.hytalegenerator.assets.patterns;

import com.hypixel.hytale.builtin.hytalegenerator.patterns.CuboidPattern;
import com.hypixel.hytale.builtin.hytalegenerator.patterns.Pattern;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.vector.Vector3i;
import javax.annotation.Nonnull;

public class CuboidPatternAsset extends PatternAsset {
   @Nonnull
   public static final BuilderCodec<CuboidPatternAsset> CODEC = BuilderCodec.builder(
         CuboidPatternAsset.class, CuboidPatternAsset::new, PatternAsset.ABSTRACT_CODEC
      )
      .append(new KeyedCodec<>("SubPattern", PatternAsset.CODEC, true), (t, k) -> t.subPatternAsset = k, k -> k.subPatternAsset)
      .add()
      .append(new KeyedCodec<>("Min", Vector3i.CODEC, true), (t, k) -> t.min = k, k -> k.min)
      .add()
      .append(new KeyedCodec<>("Max", Vector3i.CODEC, true), (t, k) -> t.max = k, k -> k.max)
      .add()
      .build();
   private PatternAsset subPatternAsset = new ConstantPatternAsset();
   private Vector3i min = new Vector3i();
   private Vector3i max = new Vector3i();

   @Nonnull
   @Override
   public Pattern build(@Nonnull PatternAsset.Argument argument) {
      if (super.isSkipped()) {
         return Pattern.noPattern();
      } else {
         Pattern subPattern = this.subPatternAsset.build(argument);
         return new CuboidPattern(subPattern, this.min, this.max);
      }
   }

   @Override
   public void cleanUp() {
      this.subPatternAsset.cleanUp();
   }
}
