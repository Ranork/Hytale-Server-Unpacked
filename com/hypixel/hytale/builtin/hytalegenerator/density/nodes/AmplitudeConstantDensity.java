package com.hypixel.hytale.builtin.hytalegenerator.density.nodes;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AmplitudeConstantDensity extends Density {
   private final double amplitude;
   @Nullable
   private Density input;

   public AmplitudeConstantDensity(double amplitude, Density input) {
      this.amplitude = amplitude;
      this.input = input;
   }

   @Override
   public double process(@Nonnull Density.Context context) {
      return this.input == null ? 0.0 : this.input.process(context) * this.amplitude;
   }

   @Override
   public void setInputs(@Nonnull Density[] inputs) {
      if (inputs.length == 0) {
         this.input = null;
      }

      this.input = inputs[0];
   }
}
