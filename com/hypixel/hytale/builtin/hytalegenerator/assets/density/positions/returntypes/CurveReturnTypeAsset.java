package com.hypixel.hytale.builtin.hytalegenerator.assets.density.positions.returntypes;

import com.hypixel.hytale.builtin.hytalegenerator.assets.curves.ConstantCurveAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.curves.CurveAsset;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.positions.returntypes.CurveReturnType;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.positions.returntypes.ReturnType;
import com.hypixel.hytale.builtin.hytalegenerator.referencebundle.ReferenceBundle;
import com.hypixel.hytale.builtin.hytalegenerator.seed.SeedBox;
import com.hypixel.hytale.builtin.hytalegenerator.threadindexer.WorkerIndexer;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import javax.annotation.Nonnull;

public class CurveReturnTypeAsset extends ReturnTypeAsset {
   @Nonnull
   public static final BuilderCodec<CurveReturnTypeAsset> CODEC = BuilderCodec.builder(
         CurveReturnTypeAsset.class, CurveReturnTypeAsset::new, ReturnTypeAsset.ABSTRACT_CODEC
      )
      .append(new KeyedCodec<>("Curve", CurveAsset.CODEC, true), (t, k) -> t.curveAsset = k, t -> t.curveAsset)
      .add()
      .build();
   private CurveAsset curveAsset = new ConstantCurveAsset();

   @Nonnull
   @Override
   public ReturnType build(@Nonnull SeedBox parentSeed, @Nonnull ReferenceBundle referenceBundle, @Nonnull WorkerIndexer.Id workerId) {
      Double2DoubleFunction curve = this.curveAsset.build();
      return new CurveReturnType(curve);
   }
}
