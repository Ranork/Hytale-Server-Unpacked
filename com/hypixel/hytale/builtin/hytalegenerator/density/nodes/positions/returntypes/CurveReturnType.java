package com.hypixel.hytale.builtin.hytalegenerator.density.nodes.positions.returntypes;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.math.vector.Vector3d;
import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CurveReturnType extends ReturnType {
   @Nonnull
   private final Double2DoubleFunction curve;

   public CurveReturnType(@Nonnull Double2DoubleFunction curve) {
      this.curve = curve;
   }

   @Override
   public double get(
      double distance0,
      double distance1,
      @Nonnull Vector3d samplePosition,
      @Nullable Vector3d closestPoint0,
      Vector3d closestPoint1,
      @Nullable Density.Context context
   ) {
      return this.curve.get(distance0);
   }
}
