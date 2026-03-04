package com.hypixel.hytale.builtin.hytalegenerator.newsystem;

import com.hypixel.hytale.math.vector.Vector3i;
import javax.annotation.Nonnull;

@FunctionalInterface
public interface TerrainDensityProvider {
   double get(@Nonnull Vector3i var1);
}
