package com.hypixel.hytale.builtin.hytalegenerator.biome;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import javax.annotation.Nonnull;

public interface Biome extends MaterialSource, PropsSource, EnvironmentSource, TintSource {
   @Nonnull
   Density getTerrainDensity();
}
