package com.hypixel.hytale.server.worldgen.cave;

import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.procedurallib.condition.ICoordinateCondition;
import com.hypixel.hytale.procedurallib.condition.IHeightThresholdInterpreter;
import com.hypixel.hytale.procedurallib.logic.point.IPointGenerator;
import com.hypixel.hytale.procedurallib.property.NoiseProperty;
import com.hypixel.hytale.procedurallib.supplier.IDoubleRange;
import com.hypixel.hytale.procedurallib.supplier.IFloatRange;
import com.hypixel.hytale.server.worldgen.util.BlockFluidEntry;
import com.hypixel.hytale.server.worldgen.util.condition.BlockMaskCondition;
import com.hypixel.hytale.server.worldgen.util.condition.flag.Int2FlagsCondition;
import java.util.Random;
import javax.annotation.Nonnull;

public class CaveType {
   protected final String name;
   protected final CaveNodeType entryNodeType;
   protected final IFloatRange yaw;
   protected final IFloatRange pitch;
   protected final IFloatRange depth;
   protected final IHeightThresholdInterpreter heightFactors;
   protected final IPointGenerator pointGenerator;
   protected final Int2FlagsCondition biomeMask;
   protected final BlockMaskCondition blockMask;
   protected final ICoordinateCondition mapCondition;
   protected final ICoordinateCondition heightCondition;
   protected final IDoubleRange fixedEntryHeight;
   protected final NoiseProperty fixedEntryHeightNoise;
   protected final CaveType.FluidLevel fluidLevel;
   protected final int environment;
   protected final boolean surfaceLimited;
   protected final boolean submerge;
   protected final double maximumSize;
   protected final int hashCode;

   public CaveType(
      String name,
      CaveNodeType entryNodeType,
      IFloatRange yaw,
      IFloatRange pitch,
      IFloatRange depth,
      IHeightThresholdInterpreter heightFactors,
      IPointGenerator pointGenerator,
      Int2FlagsCondition biomeMask,
      BlockMaskCondition blockMask,
      ICoordinateCondition mapCondition,
      ICoordinateCondition heightCondition,
      IDoubleRange fixedEntryHeight,
      NoiseProperty fixedEntryHeightNoise,
      CaveType.FluidLevel fluidLevel,
      int environment,
      boolean surfaceLimited,
      boolean submerge,
      double maximumSize
   ) {
      this.name = name;
      this.entryNodeType = entryNodeType;
      this.yaw = yaw;
      this.pitch = pitch;
      this.depth = depth;
      this.heightFactors = heightFactors;
      this.pointGenerator = pointGenerator;
      this.biomeMask = biomeMask;
      this.blockMask = blockMask;
      this.mapCondition = mapCondition;
      this.heightCondition = heightCondition;
      this.fixedEntryHeight = fixedEntryHeight;
      this.fixedEntryHeightNoise = fixedEntryHeightNoise;
      this.fluidLevel = fluidLevel;
      this.environment = environment;
      this.surfaceLimited = surfaceLimited;
      this.submerge = submerge;
      this.maximumSize = maximumSize;
      this.hashCode = this._hashCode();
   }

   public String getName() {
      return this.name;
   }

   public CaveNodeType getEntryNode() {
      return this.entryNodeType;
   }

   public int getModifiedStartHeight(int seed, int x, int y, int z, Random random) {
      if (this.fixedEntryHeight == null) {
         return y;
      } else {
         double val;
         if (this.fixedEntryHeightNoise != null) {
            val = this.fixedEntryHeight.getValue(this.fixedEntryHeightNoise.get(seed, x, z));
         } else {
            val = this.fixedEntryHeight.getValue(random);
         }

         return MathUtil.floor(val);
      }
   }

   public float getStartPitch(Random random) {
      return this.pitch.getValue(random);
   }

   public float getStartYaw(Random random) {
      return this.yaw.getValue(random);
   }

   public int getStartDepth(Random random) {
      return MathUtil.floor(this.depth.getValue(random));
   }

   public float getHeightRadiusFactor(int seed, double x, double z, int y) {
      return this.heightFactors.getThreshold(seed, x, z, y);
   }

   public ICoordinateCondition getHeightCondition() {
      return this.heightCondition;
   }

   public IPointGenerator getEntryPointGenerator() {
      return this.pointGenerator;
   }

   public Int2FlagsCondition getBiomeMask() {
      return this.biomeMask;
   }

   public BlockMaskCondition getBlockMask() {
      return this.blockMask;
   }

   public CaveType.FluidLevel getFluidLevel() {
      return this.fluidLevel;
   }

   public int getEnvironment() {
      return this.environment;
   }

   public boolean isSurfaceLimited() {
      return this.surfaceLimited;
   }

   public boolean isSubmerge() {
      return this.submerge;
   }

   public boolean isEntryThreshold(int seed, int x, int z) {
      return this.mapCondition.eval(seed, x, z);
   }

   public boolean isHeightThreshold(int seed, int x, int y, int z) {
      return this.heightCondition.eval(seed, x, y, z);
   }

   public double getMaximumSize() {
      return this.maximumSize;
   }

   private int _hashCode() {
      int result = this.name != null ? this.name.hashCode() : 0;
      result = 31 * result + (this.entryNodeType != null ? this.entryNodeType.hashCode() : 0);
      result = 31 * result + (this.yaw != null ? this.yaw.hashCode() : 0);
      result = 31 * result + (this.pitch != null ? this.pitch.hashCode() : 0);
      result = 31 * result + (this.depth != null ? this.depth.hashCode() : 0);
      result = 31 * result + (this.heightFactors != null ? this.heightFactors.hashCode() : 0);
      result = 31 * result + (this.pointGenerator != null ? this.pointGenerator.hashCode() : 0);
      result = 31 * result + (this.blockMask != null ? this.blockMask.hashCode() : 0);
      result = 31 * result + (this.mapCondition != null ? this.mapCondition.hashCode() : 0);
      result = 31 * result + (this.fixedEntryHeight != null ? this.fixedEntryHeight.hashCode() : 0);
      result = 31 * result + (this.fluidLevel != null ? this.fluidLevel.hashCode() : 0);
      result = 31 * result + (this.surfaceLimited ? 1 : 0);
      return 31 * result + (this.submerge ? 1 : 0);
   }

   @Override
   public int hashCode() {
      return this.hashCode;
   }

   @Nonnull
   @Override
   public String toString() {
      return "CaveType{name='"
         + this.name
         + "', entryNodeType="
         + this.entryNodeType
         + ", yaw="
         + this.yaw
         + ", pitch="
         + this.pitch
         + ", depth="
         + this.depth
         + ", heightFactors="
         + this.heightFactors
         + ", pointGenerator="
         + this.pointGenerator
         + ", placementConfiguration="
         + this.blockMask
         + ", mapCondition="
         + this.mapCondition
         + ", heightCondition="
         + this.heightCondition
         + ", fixedEntryHeight="
         + this.fixedEntryHeight
         + ", fluidLevel="
         + this.fluidLevel
         + ", environment="
         + this.environment
         + ", surfaceLimited="
         + this.surfaceLimited
         + ", submerge="
         + this.submerge
         + ", maximumSize="
         + this.maximumSize
         + ", hashCode="
         + this.hashCode
         + "}";
   }

   public static class FluidLevel {
      public static final CaveType.FluidLevel EMPTY = new CaveType.FluidLevel(new BlockFluidEntry(0, 0, 0), -1);
      private final BlockFluidEntry blockEntry;
      private final int height;

      public FluidLevel(BlockFluidEntry blockEntry, int height) {
         this.blockEntry = blockEntry;
         this.height = height;
      }

      public BlockFluidEntry getBlockEntry() {
         return this.blockEntry;
      }

      public int getHeight() {
         return this.height;
      }
   }
}
