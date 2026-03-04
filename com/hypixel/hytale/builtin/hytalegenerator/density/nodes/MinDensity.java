package com.hypixel.hytale.builtin.hytalegenerator.density.nodes;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import java.util.List;
import javax.annotation.Nonnull;

public class MinDensity extends Density {
   private Density[] inputs;

   public MinDensity(@Nonnull List<Density> inputs) {
      this.inputs = new Density[inputs.size()];
      inputs.toArray(this.inputs);
   }

   @Override
   public double process(@Nonnull Density.Context context) {
      if (this.inputs.length == 0) {
         return 0.0;
      } else {
         double min = Double.POSITIVE_INFINITY;

         for (Density input : this.inputs) {
            double value = input.process(context);
            if (min > value) {
               min = value;
            }
         }

         return min;
      }
   }

   @Override
   public void setInputs(@Nonnull Density[] inputs) {
      this.inputs = inputs;
   }
}
