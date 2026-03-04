package com.hypixel.hytale.builtin.hytalegenerator.density.nodes.positions.distancefunctions;

import com.hypixel.hytale.math.vector.Vector3d;
import javax.annotation.Nonnull;

public abstract class DistanceFunction {
   public abstract double getDistance(@Nonnull Vector3d var1);
}
