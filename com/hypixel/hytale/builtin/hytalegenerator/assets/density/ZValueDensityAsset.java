package com.hypixel.hytale.builtin.hytalegenerator.assets.density;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.ConstantValueDensity;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.ZValueDensity;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class ZValueDensityAsset extends DensityAsset {
   @Nonnull
   public static final BuilderCodec<ZValueDensityAsset> CODEC = BuilderCodec.builder(
         ZValueDensityAsset.class, ZValueDensityAsset::new, DensityAsset.ABSTRACT_CODEC
      )
      .build();

   @Nonnull
   @Override
   public Density build(@Nonnull DensityAsset.Argument argument) {
      return (Density)(this.isSkipped() ? new ConstantValueDensity(0.0) : new ZValueDensity());
   }

   @Override
   public void cleanUp() {
      this.cleanUpInputs();
   }
}
