package com.hypixel.hytale.builtin.hytalegenerator.density.nodes;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import java.util.List;
import javax.annotation.Nonnull;

public class MaxDensity extends Density {
   public Density[] inputs;

   public MaxDensity(@Nonnull List<Density> inputs) {
      this.inputs = new Density[inputs.size()];
      inputs.toArray(this.inputs);
   }

   @Override
   public double process(@Nonnull Density.Context context) {
      if (this.inputs.length == 0) {
         return 0.0;
      } else {
         double max = Double.NEGATIVE_INFINITY;

         for (Density input : this.inputs) {
            double value = input.process(context);
            if (max < value) {
               max = value;
            }
         }

         return max;
      }
   }

   @Override
   public void setInputs(@Nonnull Density[] inputs) {
      this.inputs = inputs;
   }
}
