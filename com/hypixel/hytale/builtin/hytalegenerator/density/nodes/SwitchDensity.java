package com.hypixel.hytale.builtin.hytalegenerator.density.nodes;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import java.util.List;
import javax.annotation.Nonnull;

public class SwitchDensity extends Density {
   private Density[] inputs;
   @Nonnull
   private final int[] switchStates;

   public SwitchDensity(@Nonnull List<Density> inputs, @Nonnull List<Integer> switchStates) {
      if (inputs.size() != switchStates.size()) {
         throw new IllegalArgumentException("inputs and switch states have different sizes");
      } else {
         this.inputs = new Density[inputs.size()];
         this.switchStates = new int[switchStates.size()];
         inputs.toArray(this.inputs);

         for (int i = 0; i < switchStates.size(); i++) {
            this.switchStates[i] = switchStates.get(i);
         }
      }
   }

   @Override
   public double process(@Nonnull Density.Context context) {
      if (context == null) {
         return 0.0;
      } else {
         int contextSwitchState = context.switchState;

         for (int i = 0; i < this.switchStates.length; i++) {
            if (this.switchStates[i] == contextSwitchState) {
               Density node = this.inputs[i];
               if (node == null) {
                  return 0.0;
               }

               return node.process(context);
            }
         }

         return 0.0;
      }
   }

   @Override
   public void setInputs(@Nonnull Density[] inputs) {
      if (inputs.length == 0) {
         for (int i = 0; i < this.switchStates.length; i++) {
            this.inputs[i] = null;
         }
      } else if (inputs.length < this.inputs.length) {
         System.arraycopy(inputs, 0, this.inputs, 0, inputs.length);

         for (int i = inputs.length; i < this.inputs.length; i++) {
            this.inputs[i] = null;
         }
      } else {
         System.arraycopy(inputs, 0, this.inputs, 0, this.inputs.length);
      }
   }
}
