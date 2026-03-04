package com.hypixel.hytale.builtin.hytalegenerator.density.nodes;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class InverterDensity extends Density {
   @Nullable
   private Density input;

   public InverterDensity(Density input) {
      this.input = input;
   }

   @Override
   public double process(@Nonnull Density.Context context) {
      return this.input == null ? 0.0 : -this.input.process(context);
   }

   @Override
   public void setInputs(@Nonnull Density[] inputs) {
      if (inputs.length == 0) {
         this.input = null;
      }

      this.input = inputs[0];
   }
}
