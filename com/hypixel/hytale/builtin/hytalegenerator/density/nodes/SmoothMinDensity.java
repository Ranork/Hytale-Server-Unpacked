package com.hypixel.hytale.builtin.hytalegenerator.density.nodes;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.framework.math.Calculator;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SmoothMinDensity extends Density {
   private final double range;
   @Nullable
   private Density inputA;
   @Nullable
   private Density inputB;

   public SmoothMinDensity(double range, Density inputA, Density inputB) {
      this.range = range;
      this.inputA = inputA;
      this.inputB = inputB;
   }

   @Override
   public double process(@Nonnull Density.Context context) {
      return this.inputA != null && this.inputB != null ? Calculator.smoothMin(this.range, this.inputA.process(context), this.inputB.process(context)) : 0.0;
   }

   @Override
   public void setInputs(@Nonnull Density[] inputs) {
      if (inputs.length < 2) {
         this.inputA = null;
         this.inputB = null;
      }

      this.inputA = inputs[0];
      this.inputB = inputs[1];
   }
}
