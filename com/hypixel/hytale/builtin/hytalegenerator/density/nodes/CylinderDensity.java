package com.hypixel.hytale.builtin.hytalegenerator.density.nodes;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.framework.math.Calculator;
import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import javax.annotation.Nonnull;

public class CylinderDensity extends Density {
   @Nonnull
   private final Double2DoubleFunction radialCurve;
   @Nonnull
   private final Double2DoubleFunction axialCurve;

   public CylinderDensity(@Nonnull Double2DoubleFunction radialCurve, @Nonnull Double2DoubleFunction axialCurve) {
      this.radialCurve = radialCurve;
      this.axialCurve = axialCurve;
   }

   @Override
   public double process(@Nonnull Density.Context context) {
      double radialDistance = Calculator.distance(context.position.x, context.position.z, 0.0, 0.0);
      return this.axialCurve.applyAsDouble(context.position.y) * this.radialCurve.applyAsDouble(radialDistance);
   }
}
