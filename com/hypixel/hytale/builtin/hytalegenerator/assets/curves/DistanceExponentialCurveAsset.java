package com.hypixel.hytale.builtin.hytalegenerator.assets.curves;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import javax.annotation.Nonnull;

public class DistanceExponentialCurveAsset extends CurveAsset {
   @Nonnull
   public static final BuilderCodec<DistanceExponentialCurveAsset> CODEC = BuilderCodec.builder(
         DistanceExponentialCurveAsset.class, DistanceExponentialCurveAsset::new, CurveAsset.ABSTRACT_CODEC
      )
      .append(new KeyedCodec<>("Exponent", Codec.DOUBLE, true), (t, k) -> t.exponent = k, k -> k.exponent)
      .addValidator(Validators.greaterThanOrEqual(0.0))
      .add()
      .<Double>append(new KeyedCodec<>("Range", Codec.DOUBLE, true), (t, k) -> t.range = k, k -> k.range)
      .addValidator(Validators.greaterThanOrEqual(0.0))
      .add()
      .build();
   private double exponent = 1.0;
   private double range = 1.0;

   @Nonnull
   @Override
   public Double2DoubleFunction build() {
      return in -> {
         if (in < 0.0) {
            return 1.0;
         } else if (in > this.range) {
            return 0.0;
         } else {
            in /= this.range;
            in *= -1.0;
            return Math.pow(++in, this.exponent);
         }
      };
   }

   @Override
   public void cleanUp() {
   }
}
