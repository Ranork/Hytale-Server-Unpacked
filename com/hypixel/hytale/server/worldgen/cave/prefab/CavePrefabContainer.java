package com.hypixel.hytale.server.worldgen.cave.prefab;

import com.hypixel.hytale.common.map.IWeightedMap;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.procedurallib.condition.ICoordinateCondition;
import com.hypixel.hytale.procedurallib.condition.ICoordinateRndCondition;
import com.hypixel.hytale.procedurallib.condition.IIntCondition;
import com.hypixel.hytale.procedurallib.supplier.IDoubleCoordinateHashSupplier;
import com.hypixel.hytale.procedurallib.supplier.IDoubleRange;
import com.hypixel.hytale.server.core.prefab.PrefabRotation;
import com.hypixel.hytale.server.worldgen.biome.Biome;
import com.hypixel.hytale.server.worldgen.cave.CavePrefabPlacement;
import com.hypixel.hytale.server.worldgen.cave.element.CaveNode;
import com.hypixel.hytale.server.worldgen.loader.WorldGenPrefabSupplier;
import com.hypixel.hytale.server.worldgen.util.condition.BlockMaskCondition;
import java.util.Random;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CavePrefabContainer {
   protected final CavePrefabContainer.CavePrefabEntry[] entries;

   public CavePrefabContainer(CavePrefabContainer.CavePrefabEntry[] entries) {
      this.entries = entries;
   }

   public CavePrefabContainer.CavePrefabEntry[] getEntries() {
      return this.entries;
   }

   public static class CavePrefabEntry {
      protected final IWeightedMap<WorldGenPrefabSupplier> prefabs;
      protected final CavePrefabContainer.CavePrefabEntry.CavePrefabConfig config;

      public CavePrefabEntry(IWeightedMap<WorldGenPrefabSupplier> prefabs, CavePrefabContainer.CavePrefabEntry.CavePrefabConfig config) {
         this.prefabs = prefabs;
         this.config = config;
      }

      public IWeightedMap<WorldGenPrefabSupplier> getPrefabs() {
         return this.prefabs;
      }

      @Nullable
      public WorldGenPrefabSupplier getPrefab(double random) {
         return this.prefabs.get(random);
      }

      public CavePrefabContainer.CavePrefabEntry.CavePrefabConfig getConfig() {
         return this.config;
      }

      public static class CavePrefabConfig {
         protected final PrefabRotation[] rotations;
         protected final CavePrefabPlacement placement;
         protected final IIntCondition biomeMask;
         protected final BlockMaskCondition blockMask;
         protected final IDoubleRange iterations;
         protected final IDoubleCoordinateHashSupplier displacementSupplier;
         protected final ICoordinateCondition maskCondition;
         protected final ICoordinateRndCondition heightCondition;

         public CavePrefabConfig(
            PrefabRotation[] rotations,
            CavePrefabPlacement placement,
            IIntCondition biomeMask,
            BlockMaskCondition blockMask,
            IDoubleRange iterations,
            IDoubleCoordinateHashSupplier displacementSupplier,
            ICoordinateCondition maskCondition,
            ICoordinateRndCondition heightCondition
         ) {
            this.rotations = rotations;
            this.placement = placement;
            this.biomeMask = biomeMask;
            this.blockMask = blockMask;
            this.iterations = iterations;
            this.displacementSupplier = displacementSupplier;
            this.maskCondition = maskCondition;
            this.heightCondition = heightCondition;
         }

         public PrefabRotation getRotation(@Nonnull Random random) {
            return this.rotations[random.nextInt(this.rotations.length)];
         }

         public IIntCondition getBiomeMask() {
            return this.biomeMask;
         }

         public BlockMaskCondition getBlockMask() {
            return this.blockMask;
         }

         public int getIterations(double random) {
            return MathUtil.floor(this.iterations.getValue(random));
         }

         public double getDisplacement(int seed, int x, int z, @Nonnull CaveNode caveNode) {
            return this.displacementSupplier.get(seed, x, z, seed + caveNode.getSeedOffset());
         }

         public int getHeight(int seed, int x, int z, @Nonnull CaveNode caveNode) {
            int y = this.placement.getFunction().generate(seed, x, z, caveNode);
            return y == -1 ? -1 : (int)(y + this.getDisplacement(seed, x, z, caveNode));
         }

         public boolean isMatchingNoiseDensity(int seed, int x, int z) {
            return this.maskCondition.eval(seed, x, z);
         }

         public boolean isMatchingHeight(int seed, int x, int y, int z, Random random) {
            return this.heightCondition.eval(seed, x, z, y, random);
         }

         public boolean isMatchingBiome(@Nonnull Biome biome) {
            return this.biomeMask.eval(biome.getId());
         }
      }
   }
}
