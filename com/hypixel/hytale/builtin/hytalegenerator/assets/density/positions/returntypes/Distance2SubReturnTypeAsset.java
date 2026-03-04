package com.hypixel.hytale.builtin.hytalegenerator.assets.density.positions.returntypes;

import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.positions.returntypes.Distance2SubReturnType;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.positions.returntypes.ReturnType;
import com.hypixel.hytale.builtin.hytalegenerator.referencebundle.ReferenceBundle;
import com.hypixel.hytale.builtin.hytalegenerator.seed.SeedBox;
import com.hypixel.hytale.builtin.hytalegenerator.threadindexer.WorkerIndexer;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class Distance2SubReturnTypeAsset extends ReturnTypeAsset {
   @Nonnull
   public static final BuilderCodec<Distance2SubReturnTypeAsset> CODEC = BuilderCodec.builder(
         Distance2SubReturnTypeAsset.class, Distance2SubReturnTypeAsset::new, ReturnTypeAsset.ABSTRACT_CODEC
      )
      .build();

   @Nonnull
   @Override
   public ReturnType build(@Nonnull SeedBox parentSeed, @Nonnull ReferenceBundle referenceBundle, @Nonnull WorkerIndexer.Id workerId) {
      return new Distance2SubReturnType();
   }
}
