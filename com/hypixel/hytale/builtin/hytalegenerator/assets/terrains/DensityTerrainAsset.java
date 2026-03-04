package com.hypixel.hytale.builtin.hytalegenerator.assets.terrains;

import com.hypixel.hytale.builtin.hytalegenerator.assets.density.ConstantDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.DensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.referencebundle.ReferenceBundle;
import com.hypixel.hytale.builtin.hytalegenerator.seed.SeedBox;
import com.hypixel.hytale.builtin.hytalegenerator.threadindexer.WorkerIndexer;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class DensityTerrainAsset extends TerrainAsset {
   @Nonnull
   public static final BuilderCodec<DensityTerrainAsset> CODEC = BuilderCodec.builder(
         DensityTerrainAsset.class, DensityTerrainAsset::new, TerrainAsset.ABSTRACT_CODEC
      )
      .append(new KeyedCodec<>("Density", DensityAsset.CODEC, true), (t, k) -> t.densityAsset = k, t -> t.densityAsset)
      .add()
      .build();
   @Nonnull
   private DensityAsset densityAsset = new ConstantDensityAsset();

   @Nonnull
   @Override
   public Density buildDensity(@Nonnull SeedBox parentSeed, @Nonnull ReferenceBundle referenceBundle, @Nonnull WorkerIndexer.Id workerId) {
      return this.densityAsset.build(new DensityAsset.Argument(parentSeed, referenceBundle, workerId));
   }

   @Override
   public void cleanUp() {
      this.densityAsset.cleanUp();
   }
}
