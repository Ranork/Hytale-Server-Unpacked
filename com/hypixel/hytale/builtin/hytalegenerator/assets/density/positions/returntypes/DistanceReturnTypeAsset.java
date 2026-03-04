package com.hypixel.hytale.builtin.hytalegenerator.assets.density.positions.returntypes;

import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.positions.returntypes.DistanceReturnType;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.positions.returntypes.ReturnType;
import com.hypixel.hytale.builtin.hytalegenerator.referencebundle.ReferenceBundle;
import com.hypixel.hytale.builtin.hytalegenerator.seed.SeedBox;
import com.hypixel.hytale.builtin.hytalegenerator.threadindexer.WorkerIndexer;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class DistanceReturnTypeAsset extends ReturnTypeAsset {
   @Nonnull
   public static final BuilderCodec<DistanceReturnTypeAsset> CODEC = BuilderCodec.builder(
         DistanceReturnTypeAsset.class, DistanceReturnTypeAsset::new, ReturnTypeAsset.ABSTRACT_CODEC
      )
      .build();

   @Nonnull
   @Override
   public ReturnType build(@Nonnull SeedBox parentSeed, @Nonnull ReferenceBundle referenceBundle, @Nonnull WorkerIndexer.Id workerId) {
      return new DistanceReturnType();
   }
}
