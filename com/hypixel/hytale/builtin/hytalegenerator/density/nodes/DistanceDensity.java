package com.hypixel.hytale.builtin.hytalegenerator.density.nodes;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.framework.math.Calculator;
import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import javax.annotation.Nonnull;

public class DistanceDensity extends Density {
   public static final double ZERO_DELTA = 1.0E-9;
   @Nonnull
   private final Double2DoubleFunction falloffFunction;

   public DistanceDensity(@Nonnull Double2DoubleFunction falloffFunction) {
      this.falloffFunction = falloffFunction;
   }

   @Override
   public double process(@Nonnull Density.Context context) {
      double distance = Calculator.distance(context.position.x, context.position.y, context.position.z, 0.0, 0.0, 0.0);
      return this.falloffFunction.get(distance);
   }
}
