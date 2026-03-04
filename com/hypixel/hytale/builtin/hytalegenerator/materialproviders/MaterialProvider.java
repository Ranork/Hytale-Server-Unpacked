package com.hypixel.hytale.builtin.hytalegenerator.materialproviders;

import com.hypixel.hytale.builtin.hytalegenerator.newsystem.TerrainDensityProvider;
import com.hypixel.hytale.math.vector.Vector3i;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class MaterialProvider<V> {
   @Nullable
   public abstract V getVoxelTypeAt(@Nonnull MaterialProvider.Context var1);

   @Nonnull
   public static <V> MaterialProvider<V> noMaterialProvider() {
      return new ConstantMaterialProvider<>(null);
   }

   public static class Context {
      @Nonnull
      public Vector3i position;
      public double density;
      public int depthIntoFloor;
      public int depthIntoCeiling;
      public int spaceAboveFloor;
      public int spaceBelowCeiling;
      @Nullable
      public TerrainDensityProvider terrainDensityProvider;
      public double distanceToBiomeEdge;

      public Context(
         @Nonnull Vector3i position,
         double density,
         int depthIntoFloor,
         int depthIntoCeiling,
         int spaceAboveFloor,
         int spaceBelowCeiling,
         @Nullable TerrainDensityProvider terrainDensityProvider,
         double distanceToBiomeEdge
      ) {
         this.position = position;
         this.density = density;
         this.depthIntoFloor = depthIntoFloor;
         this.depthIntoCeiling = depthIntoCeiling;
         this.spaceAboveFloor = spaceAboveFloor;
         this.spaceBelowCeiling = spaceBelowCeiling;
         this.terrainDensityProvider = terrainDensityProvider;
         this.distanceToBiomeEdge = distanceToBiomeEdge;
      }

      public Context(@Nonnull MaterialProvider.Context other) {
         this.position = other.position;
         this.density = other.density;
         this.depthIntoFloor = other.depthIntoFloor;
         this.depthIntoCeiling = other.depthIntoCeiling;
         this.spaceAboveFloor = other.spaceAboveFloor;
         this.spaceBelowCeiling = other.spaceBelowCeiling;
         this.terrainDensityProvider = other.terrainDensityProvider;
         this.distanceToBiomeEdge = other.distanceToBiomeEdge;
      }
   }
}
