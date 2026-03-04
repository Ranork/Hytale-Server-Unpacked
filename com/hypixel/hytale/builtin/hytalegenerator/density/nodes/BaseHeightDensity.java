package com.hypixel.hytale.builtin.hytalegenerator.density.nodes;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import javax.annotation.Nonnull;

public class BaseHeightDensity extends Density {
   @Nonnull
   private final double baseHeight;
   private final boolean isDistance;

   public BaseHeightDensity(double baseHeight, boolean isDistance) {
      this.baseHeight = baseHeight;
      this.isDistance = isDistance;
   }

   @Override
   public double process(@Nonnull Density.Context context) {
      return this.isDistance ? context.position.y - this.baseHeight : this.baseHeight;
   }

   public boolean skipInputs(double y) {
      return true;
   }
}
