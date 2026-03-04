package com.hypixel.hytale.builtin.hytalegenerator.density.nodes.positions.distancefunctions;

import com.hypixel.hytale.math.vector.Vector3d;
import javax.annotation.Nonnull;

public class EuclideanDistanceFunction extends DistanceFunction {
   @Override
   public double getDistance(@Nonnull Vector3d point) {
      return point.x * point.x + point.y * point.y + point.z * point.z;
   }
}
