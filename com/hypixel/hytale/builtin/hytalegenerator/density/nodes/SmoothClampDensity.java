package com.hypixel.hytale.builtin.hytalegenerator.density.nodes;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.framework.math.Calculator;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SmoothClampDensity extends Density {
   private final double max;
   private final double min;
   private final double range;
   @Nullable
   private Density input;

   public SmoothClampDensity(double min, double max, double range, Density input) {
      if (range <= 0.0) {
         throw new IllegalArgumentException("invalid range + range");
      } else if (max < min) {
         throw new IllegalArgumentException("max smaller than min");
      } else {
         this.max = max;
         this.min = min;
         this.range = range;
         this.input = input;
      }
   }

   @Override
   public double process(@Nonnull Density.Context context) {
      if (this.input == null) {
         return 0.0;
      } else {
         double value = this.input.process(context);
         value = Calculator.smoothMin(this.range, this.max, value);
         return Calculator.smoothMax(this.range, this.min, value);
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
