package com.hypixel.hytale.builtin.hytalegenerator.engine;

import javax.annotation.Nonnull;
import org.joml.Vector3i;

@FunctionalInterface
public interface TerrainDensityProvider {
   double get(@Nonnull Vector3i var1);
}
