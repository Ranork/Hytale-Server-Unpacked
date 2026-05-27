package com.hypixel.hytale.builtin.hytalegenerator.density.nodes;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.math.vector.Vector3dUtil;
import javax.annotation.Nonnull;

public class TerrainDensity extends Density {
   @Override
   public double process(@Nonnull Density.Context context) {
      return context.terrainDensityProvider == null ? 0.0 : context.terrainDensityProvider.get(Vector3dUtil.toVector3i(context.position));
   }
}
