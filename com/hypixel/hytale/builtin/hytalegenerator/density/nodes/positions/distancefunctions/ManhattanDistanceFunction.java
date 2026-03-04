package com.hypixel.hytale.builtin.hytalegenerator.density.nodes.positions.distancefunctions;

import com.hypixel.hytale.math.vector.Vector3d;
import javax.annotation.Nonnull;

public class ManhattanDistanceFunction extends DistanceFunction {
   @Override
   public double getDistance(@Nonnull Vector3d point) {
      return Math.abs(point.x) + Math.abs(point.y) + Math.abs(point.z);
   }
}
