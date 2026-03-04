package com.hypixel.hytale.builtin.hytalegenerator.assets.curves;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import javax.annotation.Nonnull;

public class CeilingCurveAsset extends CurveAsset {
   @Nonnull
   public static final BuilderCodec<CeilingCurveAsset> CODEC = BuilderCodec.builder(CeilingCurveAsset.class, CeilingCurveAsset::new, CurveAsset.ABSTRACT_CODEC)
      .append(new KeyedCodec<>("Curve", CurveAsset.CODEC, true), (t, k) -> t.curveAsset = k, k -> k.curveAsset)
      .add()
      .append(new KeyedCodec<>("Ceiling", Codec.DOUBLE, true), (t, k) -> t.limit = k, k -> k.limit)
      .add()
      .build();
   private CurveAsset curveAsset = new ConstantCurveAsset();
   private double limit = 0.0;

   @Nonnull
   @Override
   public Double2DoubleFunction build() {
      if (this.curveAsset == null) {
         return in -> 0.0;
      } else {
         Double2DoubleFunction curve = this.curveAsset.build();
         return in -> Math.min(this.limit, curve.applyAsDouble(in));
      }
   }

   @Override
   public void cleanUp() {
      this.curveAsset.cleanUp();
   }
}
