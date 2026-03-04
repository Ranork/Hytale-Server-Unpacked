package com.hypixel.hytale.builtin.hytalegenerator.density.nodes;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.framework.math.Calculator;
import com.hypixel.hytale.builtin.hytalegenerator.framework.math.Normalizer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SelectorDensity extends Density {
   private final double fromMin;
   private final double fromMax;
   private final double toMin;
   private final double toMax;
   private final double smoothRange;
   @Nullable
   private Density input;

   public SelectorDensity(double fromMin, double fromMax, double toMin, double toMax, double smoothRange, Density input) {
      if (!(fromMin > fromMax) && !(toMin > toMax) && !(smoothRange < 0.0)) {
         this.fromMin = fromMin;
         this.fromMax = fromMax;
         this.toMin = toMin;
         this.toMax = toMax;
         this.smoothRange = smoothRange;
         this.input = input;
      } else {
         throw new IllegalArgumentException("min larger than max");
      }
   }

   @Override
   public double process(@Nonnull Density.Context context) {
      double v = 0.0;
      if (this.input != null) {
         v = this.input.process(context);
      }

      v = Normalizer.normalize(this.fromMin, this.fromMax, this.toMin, this.toMax, v);
      if (this.smoothRange == 0.0) {
         v = Math.max(this.toMin, v);
         return Math.min(this.toMax, v);
      } else {
         v = Calculator.smoothMax(this.smoothRange, this.toMin, v);
         return Calculator.smoothMin(this.smoothRange, v, this.toMax);
      }
   }

   @Override
   public void setInputs(@Nonnull Density[] inputs) {
      if (inputs.length == 0) {
         this.input = null;
      }

      this.input = inputs[0];
   }
}
