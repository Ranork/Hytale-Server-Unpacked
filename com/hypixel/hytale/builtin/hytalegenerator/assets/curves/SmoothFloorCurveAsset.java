package com.hypixel.hytale.builtin.hytalegenerator.assets.curves;

import com.hypixel.hytale.builtin.hytalegenerator.framework.math.Calculator;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import javax.annotation.Nonnull;

public class SmoothFloorCurveAsset extends CurveAsset {
   @Nonnull
   public static final BuilderCodec<SmoothFloorCurveAsset> CODEC = BuilderCodec.builder(
         SmoothFloorCurveAsset.class, SmoothFloorCurveAsset::new, CurveAsset.ABSTRACT_CODEC
      )
      .append(new KeyedCodec<>("Curve", CurveAsset.CODEC, true), (t, k) -> t.curveAsset = k, k -> k.curveAsset)
      .add()
      .<Double>append(new KeyedCodec<>("Range", Codec.DOUBLE, true), (t, k) -> t.range = k, k -> k.range)
      .addValidator(Validators.greaterThanOrEqual(0.0))
      .add()
      .append(new KeyedCodec<>("Floor", Codec.DOUBLE, true), (t, k) -> t.limit = k, k -> k.limit)
      .add()
      .build();
   private CurveAsset curveAsset = new ConstantCurveAsset();
   private double range = 0.0;
   private double limit = 0.0;

   @Nonnull
   @Override
   public Double2DoubleFunction build() {
      if (this.curveAsset == null) {
         return in -> 0.0;
      } else {
         Double2DoubleFunction curve = this.curveAsset.build();
         return in -> Calculator.smoothMax(this.range, this.limit, curve.applyAsDouble(in));
      }
   }

   @Override
   public void cleanUp() {
      this.curveAsset.cleanUp();
   }
}
