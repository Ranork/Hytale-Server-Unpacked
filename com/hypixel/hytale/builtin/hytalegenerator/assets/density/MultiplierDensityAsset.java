package com.hypixel.hytale.builtin.hytalegenerator.assets.density;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.ConstantValueDensity;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.MultiplierDensity;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class MultiplierDensityAsset extends DensityAsset {
   @Nonnull
   public static final BuilderCodec<MultiplierDensityAsset> CODEC = BuilderCodec.builder(
         MultiplierDensityAsset.class, MultiplierDensityAsset::new, DensityAsset.ABSTRACT_CODEC
      )
      .build();

   @Nonnull
   @Override
   public Density build(@Nonnull DensityAsset.Argument argument) {
      return (Density)(this.isSkipped() ? new ConstantValueDensity(0.0) : new MultiplierDensity(this.buildInputs(argument, true)));
   }

   @Override
   public void cleanUp() {
      this.cleanUpInputs();
   }
}
