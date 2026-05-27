package com.hypixel.hytale.builtin.hytalegenerator.density.nodes.positions.distancefunctions;

import javax.annotation.Nonnull;
import org.joml.Vector3d;

public abstract class DistanceFunction {
   public abstract double getDistance(@Nonnull Vector3d var1);
}
