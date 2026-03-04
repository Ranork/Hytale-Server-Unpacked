package com.hypixel.hytale.builtin.hytalegenerator.assets.density.positions.returntypes;

import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.positions.returntypes.Distance2MulReturnType;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.positions.returntypes.ReturnType;
import com.hypixel.hytale.builtin.hytalegenerator.referencebundle.ReferenceBundle;
import com.hypixel.hytale.builtin.hytalegenerator.seed.SeedBox;
import com.hypixel.hytale.builtin.hytalegenerator.threadindexer.WorkerIndexer;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class Distance2MulReturnTypeAsset extends ReturnTypeAsset {
   @Nonnull
   public static final BuilderCodec<Distance2MulReturnTypeAsset> CODEC = BuilderCodec.builder(
         Distance2MulReturnTypeAsset.class, Distance2MulReturnTypeAsset::new, ReturnTypeAsset.ABSTRACT_CODEC
      )
      .build();

   @Nonnull
   @Override
   public ReturnType build(@Nonnull SeedBox parentSeed, @Nonnull ReferenceBundle referenceBundle, @Nonnull WorkerIndexer.Id workerId) {
      return new Distance2MulReturnType();
   }
}
