package com.hypixel.hytale.builtin.hytalegenerator.density.nodes;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SwitchStateDensity extends Density {
   public static final int DEFAULT_SWITCH_STATE = 0;
   @Nullable
   private Density input;
   private final int switchState;
   @Nonnull
   private final Density.Context rChildContext;

   public SwitchStateDensity(Density input, int switchState) {
      this.input = input;
      this.switchState = switchState;
      this.rChildContext = new Density.Context();
   }

   @Override
   public double process(@Nonnull Density.Context context) {
      if (this.input == null) {
         return 0.0;
      } else {
         this.rChildContext.assign(context);
         this.rChildContext.switchState = this.switchState;
         return this.input.process(context);
      }
   }

   @Override
   public void setInputs(@Nonnull Density[] inputs) {
      if (inputs.length == 0) {
         this.input = null;
      }

      this.input = inputs[0];
   }
}
