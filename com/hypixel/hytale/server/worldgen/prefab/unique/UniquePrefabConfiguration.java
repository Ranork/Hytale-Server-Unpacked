package com.hypixel.hytale.server.worldgen.prefab.unique;

import com.hypixel.hytale.math.vector.Vector2d;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.procedurallib.condition.IBlockFluidCondition;
import com.hypixel.hytale.procedurallib.condition.ICoordinateCondition;
import com.hypixel.hytale.procedurallib.condition.ICoordinateRndCondition;
import com.hypixel.hytale.procedurallib.condition.IIntCondition;
import com.hypixel.hytale.server.core.prefab.PrefabRotation;
import com.hypixel.hytale.server.worldgen.biome.Biome;
import com.hypixel.hytale.server.worldgen.util.condition.BlockMaskCondition;
import java.util.Random;
import javax.annotation.Nonnull;

public class UniquePrefabConfiguration {
   protected final ICoordinateRndCondition heightCondition;
   protected final BlockMaskCondition placementConfiguration;
   protected final PrefabRotation[] rotations;
   protected final IIntCondition biomeMask;
   protected final ICoordinateCondition mapCondition;
   protected final IBlockFluidCondition parent;
   protected final Vector2d anchor;
   protected final Vector3d spawnOffset;
   protected final double maxDistance;
   protected final boolean fitHeightmap;
   protected final boolean submerge;
   protected final boolean onWater;
   protected final int environmentId;
   protected final int maxAttempts;
   protected final double exclusionRadiusSquared;
   protected final boolean spawnLocation;
   protected final double zoneBorderExclusion;
   protected final boolean showOnMap;

   public UniquePrefabConfiguration(
      ICoordinateRndCondition heightCondition,
      BlockMaskCondition placementConfiguration,
      PrefabRotation[] rotations,
      IIntCondition biomeMask,
      ICoordinateCondition mapCondition,
      IBlockFluidCondition parent,
      Vector2d anchor,
      Vector3d spawnOffset,
      double maxDistance,
      boolean fitHeightmap,
      boolean submerge,
      boolean onWater,
      int environmentId,
      int maxAttempts,
      double exclusionRadius,
      boolean spawnLocation,
      double zoneBorderExclusion,
      boolean showOnMap
   ) {
      this.heightCondition = heightCondition;
      this.placementConfiguration = placementConfiguration;
      this.rotations = rotations;
      this.biomeMask = biomeMask;
      this.mapCondition = mapCondition;
      this.parent = parent;
      this.anchor = anchor;
      this.spawnOffset = spawnOffset;
      this.maxDistance = maxDistance;
      this.fitHeightmap = fitHeightmap;
      this.submerge = submerge;
      this.onWater = onWater;
      this.environmentId = environmentId;
      this.maxAttempts = maxAttempts;
      this.exclusionRadiusSquared = exclusionRadius * exclusionRadius;
      this.spawnLocation = spawnLocation;
      this.zoneBorderExclusion = zoneBorderExclusion;
      this.showOnMap = showOnMap;
   }

   public Vector2d getAnchor() {
      return this.anchor;
   }

   public double getMaxDistance() {
      return this.maxDistance;
   }

   public ICoordinateCondition getMapCondition() {
      return this.mapCondition;
   }

   public BlockMaskCondition getPlacementConfiguration() {
      return this.placementConfiguration;
   }

   public Vector3d getSpawnOffset() {
      return this.spawnOffset;
   }

   public boolean isValidParentBiome(@Nonnull Biome biome) {
      return this.biomeMask.eval(biome.getId());
   }

   public boolean isFitHeightmap() {
      return this.fitHeightmap;
   }

   public boolean isSubmerge() {
      return this.submerge;
   }

   public boolean isValidParentBlock(int block, int fluid) {
      return this.parent.eval(block, fluid);
   }

   public ICoordinateRndCondition getHeightCondition() {
      return this.heightCondition;
   }

   public PrefabRotation getRotation(@Nonnull Random random) {
      return this.rotations != null && this.rotations.length != 0 ? this.rotations[random.nextInt(this.rotations.length)] : PrefabRotation.ROTATION_0;
   }

   public boolean isOnWater() {
      return this.onWater;
   }

   public int getEnvironmentId() {
      return this.environmentId;
   }

   public int getMaxAttempts() {
      return this.maxAttempts;
   }

   public double getExclusionRadiusSquared() {
      return this.exclusionRadiusSquared;
   }

   public boolean isSpawnLocation() {
      return this.spawnLocation;
   }

   public double getZoneBorderExclusion() {
      return this.zoneBorderExclusion;
   }

   public boolean isShowOnMap() {
      return this.showOnMap;
   }
}
