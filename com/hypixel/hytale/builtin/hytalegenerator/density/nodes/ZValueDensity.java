package com.hypixel.hytale.builtin.hytalegenerator.density.nodes;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import javax.annotation.Nonnull;

public class ZValueDensity extends Density {
   @Override
   public double process(@Nonnull Density.Context context) {
      return context.position.z;
   }

   @Override
   public void setInputs(@Nonnull Density[] inputs) {
   }
}
