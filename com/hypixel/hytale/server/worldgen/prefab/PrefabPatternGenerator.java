package com.hypixel.hytale.server.worldgen.prefab;

import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.procedurallib.condition.IBlockFluidCondition;
import com.hypixel.hytale.procedurallib.condition.ICoordinateCondition;
import com.hypixel.hytale.procedurallib.condition.ICoordinateRndCondition;
import com.hypixel.hytale.procedurallib.condition.IHeightThresholdInterpreter;
import com.hypixel.hytale.procedurallib.logic.point.IPointGenerator;
import com.hypixel.hytale.server.core.prefab.PrefabRotation;
import com.hypixel.hytale.server.worldgen.util.condition.BlockMaskCondition;
import com.hypixel.hytale.server.worldgen.util.function.ICoordinateDoubleSupplier;
import java.util.Arrays;
import javax.annotation.Nonnull;

public class PrefabPatternGenerator {
   protected final int seedOffset;
   protected final PrefabCategory category;
   protected final IPointGenerator gridGenerator;
   protected final ICoordinateRndCondition heightCondition;
   protected final IHeightThresholdInterpreter heightThresholdInterpreter;
   protected final BlockMaskCondition prefabPlacementConfiguration;
   protected final ICoordinateCondition mapCondition;
   protected final IBlockFluidCondition parentCondition;
   protected final PrefabRotation[] rotations;
   protected final ICoordinateDoubleSupplier displacement;
   protected final boolean fitHeightmap;
   protected final boolean onWater;
   protected final boolean deepSearch;
   protected final boolean submerge;
   protected final int maxSize;
   protected final int exclusionRadius;

   public PrefabPatternGenerator(
      int seedOffset,
      PrefabCategory category,
      IPointGenerator gridGenerator,
      ICoordinateRndCondition heightCondition,
      IHeightThresholdInterpreter heightThresholdInterpreter,
      BlockMaskCondition prefabPlacementConfiguration,
      ICoordinateCondition mapCondition,
      IBlockFluidCondition parentCondition,
      PrefabRotation[] rotations,
      ICoordinateDoubleSupplier displacement,
      boolean fitHeightmap,
      boolean onWater,
      boolean deepSearch,
      boolean submerge,
      int maxSize,
      int exclusionRadius
   ) {
      this.seedOffset = seedOffset;
      this.category = category;
      this.gridGenerator = gridGenerator;
      this.heightCondition = heightCondition;
      this.heightThresholdInterpreter = heightThresholdInterpreter;
      this.prefabPlacementConfiguration = prefabPlacementConfiguration;
      this.mapCondition = mapCondition;
      this.parentCondition = parentCondition;
      this.rotations = rotations;
      this.displacement = displacement;
      this.fitHeightmap = fitHeightmap;
      this.onWater = onWater;
      this.deepSearch = deepSearch;
      this.submerge = submerge;
      this.maxSize = maxSize;
      this.exclusionRadius = exclusionRadius;
   }

   public PrefabCategory getCategory() {
      return this.category;
   }

   public IPointGenerator getGridGenerator() {
      return this.gridGenerator;
   }

   public ICoordinateCondition getMapCondition() {
      return this.mapCondition;
   }

   public BlockMaskCondition getPrefabPlacementConfiguration() {
      return this.prefabPlacementConfiguration;
   }

   public boolean isFitHeightmap() {
      return this.fitHeightmap;
   }

   public IBlockFluidCondition getParentCondition() {
      return this.parentCondition;
   }

   public ICoordinateRndCondition getHeightCondition() {
      return this.heightCondition;
   }

   public IHeightThresholdInterpreter getHeightThresholdInterpreter() {
      return this.heightThresholdInterpreter;
   }

   public PrefabRotation[] getRotations() {
      return this.rotations;
   }

   public int getDisplacement(int seed, int x, int z) {
      return MathUtil.floor(this.displacement.apply(seed + this.seedOffset, x, z));
   }

   public boolean isOnWater() {
      return this.onWater;
   }

   public boolean isDeepSearch() {
      return this.deepSearch;
   }

   public boolean isSubmerge() {
      return this.submerge;
   }

   public int getMaxSize() {
      return this.maxSize;
   }

   public int getExclusionRadius() {
      return this.exclusionRadius;
   }

   @Nonnull
   @Override
   public String toString() {
      return "PrefabPatternGenerator{seedOffset="
         + this.seedOffset
         + ", gridGenerator="
         + this.gridGenerator
         + ", heightCondition="
         + this.heightCondition
         + ", heightThresholdInterpreter="
         + this.heightThresholdInterpreter
         + ", prefabPlacementConfiguration="
         + this.prefabPlacementConfiguration
         + ", mapCondition="
         + this.mapCondition
         + ", parentCondition="
         + this.parentCondition
         + ", rotations="
         + Arrays.toString((Object[])this.rotations)
         + ", displacement="
         + this.displacement
         + ", fitHeightmap="
         + this.fitHeightmap
         + ", submerge="
         + this.submerge
         + ", maxSize="
         + this.maxSize
         + ", onWater="
         + this.onWater
         + ", deepSearch="
         + this.deepSearch
         + "}";
   }
}
