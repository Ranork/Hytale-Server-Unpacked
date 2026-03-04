package com.hypixel.hytale.builtin.hytalegenerator.density.nodes;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CurveMapperDensity extends Density {
   @Nonnull
   private final Double2DoubleFunction curveFunction;
   @Nullable
   private Density input;

   public CurveMapperDensity(@Nonnull Double2DoubleFunction curveFunction, Density input) {
      this.curveFunction = curveFunction;
      this.input = input;
   }

   @Override
   public double process(@Nonnull Density.Context context) {
      return this.input == null ? this.curveFunction.get(0.0) : this.curveFunction.applyAsDouble(this.input.process(context));
   }

   @Override
   public void setInputs(@Nonnull Density[] inputs) {
      if (inputs.length == 0) {
         this.input = null;
      }

      this.input = inputs[0];
   }
}
