package com.hypixel.hytale.builtin.hytalegenerator.density.nodes.positions.distancefunctions;

import javax.annotation.Nonnull;
import org.joml.Vector3d;

public class ManhattanDistanceFunction extends DistanceFunction {
   @Override
   public double getDistance(@Nonnull Vector3d point) {
      return Math.abs(point.x) + Math.abs(point.y) + Math.abs(point.z);
   }
}
