package com.hypixel.hytale.builtin.hytalegenerator.density.nodes;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import javax.annotation.Nonnull;

public class CubeDensity extends Density {
   @Nonnull
   private final Double2DoubleFunction falloffFunction;

   public CubeDensity(@Nonnull Double2DoubleFunction falloffFunction) {
      this.falloffFunction = falloffFunction;
   }

   @Override
   public double process(@Nonnull Density.Context context) {
      double distance = Math.max(Math.abs(context.position.x), Math.abs(context.position.y));
      distance = Math.max(distance, Math.abs(context.position.z));
      return this.falloffFunction.get(distance);
   }
}
