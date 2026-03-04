package com.hypixel.hytale.builtin.hytalegenerator.density.nodes;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import javax.annotation.Nonnull;

public class ConstantValueDensity extends Density {
   private final double value;

   public ConstantValueDensity(double value) {
      this.value = value;
   }

   @Override
   public double process(@Nonnull Density.Context context) {
      return this.value;
   }
}
