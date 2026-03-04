package com.hypixel.hytale.builtin.hytalegenerator.density.nodes;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import javax.annotation.Nonnull;

public class DistanceToBiomeEdgeDensity extends Density {
   @Override
   public double process(@Nonnull Density.Context context) {
      return context.distanceToBiomeEdge;
   }
}
