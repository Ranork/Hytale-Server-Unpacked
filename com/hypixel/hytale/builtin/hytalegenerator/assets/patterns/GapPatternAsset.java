package com.hypixel.hytale.builtin.hytalegenerator.assets.patterns;

import com.hypixel.hytale.builtin.hytalegenerator.patterns.GapPattern;
import com.hypixel.hytale.builtin.hytalegenerator.patterns.Pattern;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.Validators;
import java.util.ArrayList;
import javax.annotation.Nonnull;

public class GapPatternAsset extends PatternAsset {
   @Nonnull
   public static final BuilderCodec<GapPatternAsset> CODEC = BuilderCodec.builder(GapPatternAsset.class, GapPatternAsset::new, PatternAsset.ABSTRACT_CODEC)
      .append(new KeyedCodec<>("GapPattern", PatternAsset.CODEC, true), (t, k) -> t.gapPatternAsset = k, k -> k.gapPatternAsset)
      .add()
      .append(new KeyedCodec<>("AnchorPattern", PatternAsset.CODEC, true), (t, k) -> t.anchorPatternAsset = k, k -> k.anchorPatternAsset)
      .add()
      .<Double>append(new KeyedCodec<>("GapSize", Codec.DOUBLE, true), (t, k) -> t.gapSize = k, k -> k.gapSize)
      .addValidator(Validators.greaterThanOrEqual(0.0))
      .add()
      .<Double>append(new KeyedCodec<>("AnchorSize", Codec.DOUBLE, true), (t, k) -> t.anchorSize = k, k -> k.anchorSize)
      .addValidator(Validators.greaterThanOrEqual(0.0))
      .add()
      .<Double>append(new KeyedCodec<>("AnchorRoughness", Codec.DOUBLE, true), (t, k) -> t.anchorRoughness = k, k -> k.anchorRoughness)
      .addValidator(Validators.greaterThanOrEqual(0.0))
      .add()
      .<Integer>append(new KeyedCodec<>("DepthDown", Codec.INTEGER, true), (t, k) -> t.depthDown = k, k -> k.depthDown)
      .addValidator(Validators.greaterThanOrEqual(0))
      .add()
      .<Integer>append(new KeyedCodec<>("DepthUp", Codec.INTEGER, true), (t, k) -> t.depthUp = k, k -> k.depthUp)
      .addValidator(Validators.greaterThanOrEqual(0))
      .add()
      .append(new KeyedCodec<>("Angles", new ArrayCodec<>(Codec.FLOAT, Float[]::new), true), (t, k) -> t.angles = k, k -> k.angles)
      .add()
      .build();
   private PatternAsset gapPatternAsset = new ConstantPatternAsset();
   private PatternAsset anchorPatternAsset = new ConstantPatternAsset();
   private double gapSize = 0.0;
   private double anchorSize = 0.0;
   private double anchorRoughness = 0.0;
   private int depthDown = 0;
   private int depthUp = 0;
   private Float[] angles = new Float[0];

   @Nonnull
   @Override
   public Pattern build(@Nonnull PatternAsset.Argument argument) {
      if (super.isSkipped()) {
         return Pattern.noPattern();
      } else {
         Pattern gapPattern = this.gapPatternAsset.build(argument);
         Pattern wallPattern = this.anchorPatternAsset.build(argument);
         ArrayList<Float> angleList = new ArrayList<>();

         for (Float a : this.angles) {
            if (a != null && !Float.isNaN(a)) {
               a = a * 180.0F;
               angleList.add(a);
            }
         }

         return new GapPattern(angleList, this.gapSize, this.anchorSize, this.anchorRoughness, this.depthDown, this.depthUp, gapPattern, wallPattern);
      }
   }

   @Override
   public void cleanUp() {
      this.gapPatternAsset.cleanUp();
      this.anchorPatternAsset.cleanUp();
   }
}
