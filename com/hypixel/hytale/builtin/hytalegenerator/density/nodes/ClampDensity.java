package com.hypixel.hytale.builtin.hytalegenerator.density.nodes;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.framework.math.Calculator;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ClampDensity extends Density {
   private final double wallA;
   private final double wallB;
   @Nullable
   private Density input;

   public ClampDensity(double wallA, double wallB, Density input) {
      this.wallA = wallA;
      this.wallB = wallB;
      this.input = input;
   }

   @Override
   public double process(@Nonnull Density.Context context) {
      return this.input == null ? 0.0 : Calculator.clamp(this.wallA, this.input.process(context), this.wallB);
   }

   @Override
   public void setInputs(@Nonnull Density[] inputs) {
      if (inputs.length == 0) {
         this.input = null;
      }

      this.input = inputs[0];
   }
}
