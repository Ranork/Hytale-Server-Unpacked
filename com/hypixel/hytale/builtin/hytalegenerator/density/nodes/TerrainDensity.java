package com.hypixel.hytale.builtin.hytalegenerator.density.nodes;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import javax.annotation.Nonnull;

public class TerrainDensity extends Density {
   @Override
   public double process(@Nonnull Density.Context context) {
      return context.terrainDensityProvider == null ? 0.0 : context.terrainDensityProvider.get(context.position.toVector3i());
   }
}
