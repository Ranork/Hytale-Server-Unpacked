package com.hypixel.hytale.builtin.hytalegenerator.density.nodes;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.framework.math.Calculator;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SmoothFloorDensity extends Density {
   private double limit;
   private double smoothRange;
   @Nullable
   private Density input;

   public SmoothFloorDensity(double limit, double smoothRange, Density input) {
      if (smoothRange < 0.0) {
         throw new IllegalArgumentException();
      } else {
         this.limit = limit;
         this.smoothRange = smoothRange;
         this.input = input;
      }
   }

   @Override
   public double process(@Nonnull Density.Context context) {
      return this.input == null ? 0.0 : Calculator.smoothMax(this.smoothRange, this.input.process(context), this.limit);
   }

   @Override
   public void setInputs(@Nonnull Density[] inputs) {
      if (inputs.length == 0) {
         this.input = null;
      }

      this.input = inputs[0];
   }
}
