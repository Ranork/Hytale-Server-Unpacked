package com.hypixel.hytale.builtin.hytalegenerator.assets.curves;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import javax.annotation.Nonnull;

public class MinCurveAsset extends CurveAsset {
   @Nonnull
   public static final BuilderCodec<MinCurveAsset> CODEC = BuilderCodec.builder(MinCurveAsset.class, MinCurveAsset::new, CurveAsset.ABSTRACT_CODEC)
      .append(new KeyedCodec<>("Curves", new ArrayCodec<>(CurveAsset.CODEC, CurveAsset[]::new), true), (t, k) -> t.curveAssets = k, k -> k.curveAssets)
      .add()
      .build();
   private CurveAsset[] curveAssets = new CurveAsset[0];

   @Nonnull
   @Override
   public Double2DoubleFunction build() {
      if (this.curveAssets.length == 0) {
         return in -> 0.0;
      } else {
         Double2DoubleFunction[] inputCurves = new Double2DoubleFunction[this.curveAssets.length];

         for (int i = 0; i < this.curveAssets.length; i++) {
            inputCurves[i] = this.curveAssets[i].build();
         }

         if (inputCurves.length == 1) {
            Double2DoubleFunction curve = inputCurves[0];
            return curve::applyAsDouble;
         } else if (inputCurves.length == 2) {
            Double2DoubleFunction curveA = inputCurves[0];
            Double2DoubleFunction curveB = inputCurves[1];
            return in -> Math.min(curveA.applyAsDouble(in), curveB.applyAsDouble(in));
         } else if (inputCurves.length == 3) {
            Double2DoubleFunction curveA = inputCurves[0];
            Double2DoubleFunction curveB = inputCurves[1];
            Double2DoubleFunction curveC = inputCurves[2];
            return in -> Math.min(Math.min(curveA.applyAsDouble(in), curveB.applyAsDouble(in)), curveC.applyAsDouble(in));
         } else {
            return in -> {
               double value = inputCurves[0].applyAsDouble(in);

               for (int i = 1; i < inputCurves.length; i++) {
                  value = Math.min(value, inputCurves[i].applyAsDouble(in));
               }

               return value;
            };
         }
      }
   }

   @Override
   public void cleanUp() {
      for (CurveAsset curveAsset : this.curveAssets) {
         curveAsset.cleanUp();
      }
   }
}
