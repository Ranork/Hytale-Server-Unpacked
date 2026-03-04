package com.hypixel.hytale.builtin.hytalegenerator.assets.density;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.ConstantValueDensity;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.YValueDensity;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class YValueDensityAsset extends DensityAsset {
   @Nonnull
   public static final BuilderCodec<YValueDensityAsset> CODEC = BuilderCodec.builder(
         YValueDensityAsset.class, YValueDensityAsset::new, DensityAsset.ABSTRACT_CODEC
      )
      .build();

   @Nonnull
   @Override
   public Density build(@Nonnull DensityAsset.Argument argument) {
      return (Density)(this.isSkipped() ? new ConstantValueDensity(0.0) : new YValueDensity());
   }

   @Override
   public void cleanUp() {
      this.cleanUpInputs();
   }
}
