package com.hypixel.hytale.builtin.hytalegenerator.density.nodes.positions.returntypes;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.math.vector.Vector3d;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Distance2DivReturnType extends ReturnType {
   @Override
   public double get(
      double distance0,
      double distance1,
      @Nonnull Vector3d samplePosition,
      @Nullable Vector3d closestPoint0,
      Vector3d closestPoint1,
      @Nullable Density.Context context
   ) {
      if (this.maxDistance <= 0.0) {
         return 0.0;
      } else if (closestPoint0 == null) {
         return 1.0;
      } else {
         distance0 /= this.maxDistance;
         distance1 /= this.maxDistance;
         return distance0 / distance1 * 2.0 - 1.0;
      }
   }
}
