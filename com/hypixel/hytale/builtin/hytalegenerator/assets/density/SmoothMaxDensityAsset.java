package com.hypixel.hytale.builtin.hytalegenerator.assets.density;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.ConstantValueDensity;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.SmoothMaxDensity;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import javax.annotation.Nonnull;

public class SmoothMaxDensityAsset extends DensityAsset {
   @Nonnull
   public static final BuilderCodec<SmoothMaxDensityAsset> CODEC = BuilderCodec.builder(
         SmoothMaxDensityAsset.class, SmoothMaxDensityAsset::new, DensityAsset.ABSTRACT_CODEC
      )
      .append(new KeyedCodec<>("Range", Codec.DOUBLE, true), (t, k) -> t.range = k, k -> k.range)
      .addValidator(Validators.greaterThanOrEqual(0.0))
      .add()
      .build();
   private double range = 1.0;

   @Nonnull
   @Override
   public Density build(@Nonnull DensityAsset.Argument argument) {
      return (Density)(this.isSkipped()
         ? new ConstantValueDensity(0.0)
         : new SmoothMaxDensity(this.range, this.buildFirstInput(argument), this.buildSecondInput(argument)));
   }

   @Override
   public void cleanUp() {
      this.cleanUpInputs();
   }
}
