package com.hypixel.hytale.builtin.hytalegenerator.assets.curves;

import com.hypixel.hytale.builtin.hytalegenerator.framework.math.Calculator;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import javax.annotation.Nonnull;

public class SmoothMaxCurveAsset extends CurveAsset {
   @Nonnull
   public static final BuilderCodec<SmoothMaxCurveAsset> CODEC = BuilderCodec.builder(
         SmoothMaxCurveAsset.class, SmoothMaxCurveAsset::new, CurveAsset.ABSTRACT_CODEC
      )
      .append(new KeyedCodec<>("CurveA", CurveAsset.CODEC, true), (t, k) -> t.curveAAsset = k, k -> k.curveAAsset)
      .add()
      .append(new KeyedCodec<>("CurveB", CurveAsset.CODEC, true), (t, k) -> t.curveBAsset = k, k -> k.curveBAsset)
      .add()
      .<Double>append(new KeyedCodec<>("Range", Codec.DOUBLE, true), (t, k) -> t.range = k, k -> k.range)
      .addValidator(Validators.greaterThanOrEqual(0.0))
      .add()
      .build();
   private CurveAsset curveAAsset = new ConstantCurveAsset();
   private CurveAsset curveBAsset = new ConstantCurveAsset();
   private double range = 0.0;

   @Nonnull
   @Override
   public Double2DoubleFunction build() {
      if (this.curveAAsset != null && this.curveBAsset != null) {
         Double2DoubleFunction curveA = this.curveAAsset.build();
         Double2DoubleFunction curveB = this.curveBAsset.build();
         return in -> Calculator.smoothMax(this.range, curveA.applyAsDouble(in), curveB.applyAsDouble(in));
      } else {
         return in -> 0.0;
      }
   }

   @Override
   public void cleanUp() {
      this.curveAAsset.cleanUp();
      this.curveBAsset.cleanUp();
   }
}
