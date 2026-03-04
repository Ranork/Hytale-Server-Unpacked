package com.hypixel.hytale.builtin.hytalegenerator.density.nodes;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import java.util.List;
import javax.annotation.Nonnull;

public class MultiplierDensity extends Density {
   private Density[] inputs;

   public MultiplierDensity(@Nonnull List<Density> inputs) {
      this.inputs = new Density[inputs.size()];
      inputs.toArray(this.inputs);
   }

   @Override
   public double process(@Nonnull Density.Context context) {
      double multiply = this.inputs.length == 0 ? 0.0 : 1.0;

      for (Density input : this.inputs) {
         multiply *= input.process(context);
         if (multiply == 0.0) {
            return 0.0;
         }
      }

      return multiply;
   }

   @Override
   public void setInputs(@Nonnull Density[] inputs) {
      this.inputs = inputs;
   }
}
