package com.hypixel.hytale.builtin.hytalegenerator.assets.patterns;

import com.hypixel.hytale.builtin.hytalegenerator.patterns.Pattern;
import com.hypixel.hytale.builtin.hytalegenerator.patterns.WallPattern;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import java.util.List;
import javax.annotation.Nonnull;

public class WallPatternAsset extends PatternAsset {
   @Nonnull
   public static final BuilderCodec<WallPatternAsset> CODEC = BuilderCodec.builder(WallPatternAsset.class, WallPatternAsset::new, PatternAsset.ABSTRACT_CODEC)
      .append(new KeyedCodec<>("Wall", PatternAsset.CODEC, true), (t, k) -> t.wall = k, k -> k.wall)
      .add()
      .append(new KeyedCodec<>("Origin", PatternAsset.CODEC, true), (t, k) -> t.origin = k, k -> k.origin)
      .add()
      .append(new KeyedCodec<>("RequireAllDirections", Codec.BOOLEAN, false), (t, k) -> t.matchAll = k, k -> k.matchAll)
      .add()
      .append(
         new KeyedCodec<>("Directions", new ArrayCodec<>(WallPattern.WallDirection.CODEC, WallPattern.WallDirection[]::new), true),
         (t, k) -> t.directions = k,
         k -> k.directions
      )
      .add()
      .build();
   private PatternAsset wall = new ConstantPatternAsset();
   private PatternAsset origin = new ConstantPatternAsset();
   private WallPattern.WallDirection[] directions = new WallPattern.WallDirection[0];
   private boolean matchAll = false;

   @Nonnull
   @Override
   public Pattern build(@Nonnull PatternAsset.Argument argument) {
      if (super.isSkipped()) {
         return Pattern.noPattern();
      } else {
         Pattern wallPattern = this.wall.build(argument);
         Pattern originPattern = this.origin.build(argument);
         return new WallPattern(wallPattern, originPattern, List.of(this.directions), this.matchAll);
      }
   }

   @Override
   public void cleanUp() {
      this.wall.cleanUp();
      this.origin.cleanUp();
   }
}
