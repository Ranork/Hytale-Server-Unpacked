package com.hypixel.hytale.builtin.hytalegenerator.vectorproviders;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.newsystem.TerrainDensityProvider;
import com.hypixel.hytale.math.vector.Vector3d;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class VectorProvider {
   public abstract void process(@Nonnull VectorProvider.Context var1, @Nonnull Vector3d var2);

   public static class Context {
      @Nonnull
      public Vector3d position;
      @Nullable
      public TerrainDensityProvider terrainDensityProvider;

      public Context(@Nonnull Vector3d position, @Nullable TerrainDensityProvider terrainDensityProvider) {
         this.position = position;
         this.terrainDensityProvider = terrainDensityProvider;
      }

      public Context(@Nonnull VectorProvider.Context other) {
         this.position = other.position;
         this.terrainDensityProvider = other.terrainDensityProvider;
      }

      public Context(@Nonnull Density.Context other) {
         this.position = other.position;
         this.terrainDensityProvider = other.terrainDensityProvider;
      }

      public void assign(@Nonnull Density.Context other) {
         this.position = other.position;
         this.terrainDensityProvider = other.terrainDensityProvider;
      }
   }
}
