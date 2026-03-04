package com.hypixel.hytale.builtin.hytalegenerator.assets.curves;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import javax.annotation.Nonnull;

public class InverterCurveAsset extends CurveAsset {
   @Nonnull
   public static final BuilderCodec<InverterCurveAsset> CODEC = BuilderCodec.builder(
         InverterCurveAsset.class, InverterCurveAsset::new, CurveAsset.ABSTRACT_CODEC
      )
      .append(new KeyedCodec<>("Curve", CurveAsset.CODEC, true), (t, k) -> t.curveAsset = k, k -> k.curveAsset)
      .add()
      .build();
   private CurveAsset curveAsset = new ConstantCurveAsset();

   @Nonnull
   @Override
   public Double2DoubleFunction build() {
      if (this.curveAsset == null) {
         return in -> 0.0;
      } else {
         Double2DoubleFunction inputCurve = this.curveAsset.build();
         return in -> -inputCurve.applyAsDouble(in);
      }
   }

   @Override
   public void cleanUp() {
      this.curveAsset.cleanUp();
   }
}
