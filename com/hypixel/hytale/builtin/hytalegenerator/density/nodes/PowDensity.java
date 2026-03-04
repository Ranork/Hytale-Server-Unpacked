package com.hypixel.hytale.builtin.hytalegenerator.density.nodes;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PowDensity extends Density {
   private final double exponent;
   @Nullable
   private Density input;

   public PowDensity(double exponent, Density input) {
      this.exponent = exponent;
      this.input = input;
   }

   @Override
   public double process(@Nonnull Density.Context context) {
      if (this.input == null) {
         return 0.0;
      } else {
         double value = this.input.process(context);
         return value < 0.0 ? -Math.pow(-value, this.exponent) : Math.pow(value, this.exponent);
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
