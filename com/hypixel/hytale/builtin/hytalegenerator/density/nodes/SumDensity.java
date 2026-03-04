package com.hypixel.hytale.builtin.hytalegenerator.density.nodes;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;

public class SumDensity extends Density {
   private Density[] inputs;

   public SumDensity(@Nonnull List<Density> inputs) {
      this.inputs = new Density[inputs.size()];
      inputs.toArray(this.inputs);
   }

   @Override
   public double process(@Nonnull Density.Context context) {
      if (this.inputs.length == 0) {
         return 0.0;
      } else {
         double sum = 0.0;

         for (Density input : this.inputs) {
            sum += input.process(context);
         }

         return sum;
      }
   }

   @Override
   public void setInputs(@Nonnull Density[] inputs) {
      this.inputs = Arrays.copyOf(inputs, inputs.length);
   }
}
