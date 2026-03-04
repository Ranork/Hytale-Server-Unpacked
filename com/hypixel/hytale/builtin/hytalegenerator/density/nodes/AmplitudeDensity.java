package com.hypixel.hytale.builtin.hytalegenerator.density.nodes;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.framework.math.NodeFunction;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AmplitudeDensity extends Density {
   public static final double ZERO_DELTA = 1.0E-9;
   private NodeFunction amplitudeFunc;
   @Nullable
   private Density input;

   public AmplitudeDensity(@Nonnull NodeFunction offsetFunction, Density input) {
      this.amplitudeFunc = offsetFunction;
      this.input = input;
   }

   @Override
   public double process(@Nonnull Density.Context context) {
      return this.input != null && !this.skipInputs(context.position.y) ? this.input.process(context) * this.amplitudeFunc.get(context.position.y) : 0.0;
   }

   public boolean skipInputs(double y) {
      double v = this.amplitudeFunc.get(y);
      return v < 1.0E-9 && v > -1.0E-9;
   }

   @Override
   public void setInputs(@Nonnull Density[] inputs) {
      if (inputs.length == 0) {
         this.input = null;
      }

      this.input = inputs[0];
   }
}
