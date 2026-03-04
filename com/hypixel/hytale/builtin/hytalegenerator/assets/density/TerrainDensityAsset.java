package com.hypixel.hytale.builtin.hytalegenerator.assets.density;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.ConstantValueDensity;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.TerrainDensity;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class TerrainDensityAsset extends DensityAsset {
   @Nonnull
   public static final BuilderCodec<TerrainDensityAsset> CODEC = BuilderCodec.builder(
         TerrainDensityAsset.class, TerrainDensityAsset::new, DensityAsset.ABSTRACT_CODEC
      )
      .build();

   @Nonnull
   @Override
   public Density build(@Nonnull DensityAsset.Argument argument) {
      return (Density)(this.isSkipped() ? new ConstantValueDensity(0.0) : new TerrainDensity());
   }

   @Override
   public void cleanUp() {
      this.cleanUpInputs();
   }
}
