package com.hypixel.hytale.builtin.hytalegenerator.assets.curves;

import com.hypixel.hytale.builtin.hytalegenerator.framework.math.Calculator;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import javax.annotation.Nonnull;

public class SmoothClampCurveAsset extends CurveAsset {
   @Nonnull
   public static final BuilderCodec<SmoothClampCurveAsset> CODEC = BuilderCodec.builder(
         SmoothClampCurveAsset.class, SmoothClampCurveAsset::new, CurveAsset.ABSTRACT_CODEC
      )
      .append(new KeyedCodec<>("Curve", CurveAsset.CODEC, false), (t, k) -> t.curveAsset = k, k -> k.curveAsset)
      .add()
      .append(new KeyedCodec<>("WallA", Codec.DOUBLE, false), (t, k) -> t.wallA = k, k -> k.wallA)
      .add()
      .append(new KeyedCodec<>("WallB", Codec.DOUBLE, false), (t, k) -> t.wallB = k, k -> k.wallB)
      .add()
      .<Double>append(new KeyedCodec<>("Range", Codec.DOUBLE, false), (t, k) -> t.range = k, k -> k.range)
      .addValidator(Validators.greaterThanOrEqual(0.0))
      .add()
      .build();
   private CurveAsset curveAsset = new ConstantCurveAsset();
   private double wallA = 1.0;
   private double wallB = -1.0;
   private double range = 0.0;

   @Nonnull
   @Override
   public Double2DoubleFunction build() {
      double defaultValue = (this.wallA + this.wallB) / 2.0;
      if (this.curveAsset == null) {
         return in -> defaultValue;
      } else {
         double min = Math.min(this.wallA, this.wallB);
         double max = Math.max(this.wallA, this.wallB);
         Double2DoubleFunction inputCurve = this.curveAsset.build();
         return this.range == 0.0 ? in -> Calculator.clamp(this.wallA, inputCurve.applyAsDouble(in), this.wallB) : in -> {
            double value = inputCurve.applyAsDouble(in);
            double smoothedMax = Calculator.smoothMax(this.range, min, value);
            double smoothedMin = Calculator.smoothMin(this.range, max, value);
            return (smoothedMin + smoothedMax) / 2.0;
         };
      }
   }

   @Override
   public void cleanUp() {
      this.curveAsset.cleanUp();
   }
}
