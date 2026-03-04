package com.hypixel.hytale.builtin.hytalegenerator.assets.patterns;

import com.hypixel.hytale.builtin.hytalegenerator.patterns.Pattern;
import com.hypixel.hytale.builtin.hytalegenerator.patterns.SurfacePattern;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class FloorPatternAsset extends PatternAsset {
   @Nonnull
   public static final BuilderCodec<FloorPatternAsset> CODEC = BuilderCodec.builder(
         FloorPatternAsset.class, FloorPatternAsset::new, PatternAsset.ABSTRACT_CODEC
      )
      .append(new KeyedCodec<>("Floor", PatternAsset.CODEC, true), (t, k) -> t.floor = k, k -> k.floor)
      .add()
      .append(new KeyedCodec<>("Origin", PatternAsset.CODEC, true), (t, k) -> t.origin = k, k -> k.origin)
      .add()
      .build();
   private PatternAsset floor = new ConstantPatternAsset();
   private PatternAsset origin = new ConstantPatternAsset();

   @Nonnull
   @Override
   public Pattern build(@Nonnull PatternAsset.Argument argument) {
      if (super.isSkipped()) {
         return Pattern.noPattern();
      } else {
         Pattern floorPattern = this.floor.build(argument);
         Pattern originPattern = this.origin.build(argument);
         return new SurfacePattern(floorPattern, originPattern, 0.0, 0.0, SurfacePattern.Facing.U, 0, 0);
      }
   }

   @Override
   public void cleanUp() {
      this.floor.cleanUp();
      this.origin.cleanUp();
   }
}
