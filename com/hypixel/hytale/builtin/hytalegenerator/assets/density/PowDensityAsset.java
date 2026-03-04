package com.hypixel.hytale.builtin.hytalegenerator.assets.density;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.ConstantValueDensity;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.PowDensity;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class PowDensityAsset extends DensityAsset {
   @Nonnull
   public static final BuilderCodec<PowDensityAsset> CODEC = BuilderCodec.builder(PowDensityAsset.class, PowDensityAsset::new, DensityAsset.ABSTRACT_CODEC)
      .append(new KeyedCodec<>("Exponent", Codec.DOUBLE, true), (t, k) -> t.exponent = k, t -> t.exponent)
      .add()
      .build();
   private double exponent = 1.0;

   @Nonnull
   @Override
   public Density build(@Nonnull DensityAsset.Argument argument) {
      return (Density)(this.isSkipped() ? new ConstantValueDensity(0.0) : new PowDensity(this.exponent, this.buildFirstInput(argument)));
   }

   @Override
   public void cleanUp() {
      this.cleanUpInputs();
   }
}
