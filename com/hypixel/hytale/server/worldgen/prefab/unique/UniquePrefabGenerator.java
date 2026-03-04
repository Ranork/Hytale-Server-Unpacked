package com.hypixel.hytale.server.worldgen.prefab.unique;

import com.hypixel.hytale.common.map.IWeightedMap;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.util.TrigMathUtil;
import com.hypixel.hytale.math.vector.Vector2i;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.procedurallib.condition.ICoordinateRndCondition;
import com.hypixel.hytale.server.worldgen.biome.Biome;
import com.hypixel.hytale.server.worldgen.chunk.ChunkGenerator;
import com.hypixel.hytale.server.worldgen.chunk.MaskProvider;
import com.hypixel.hytale.server.worldgen.chunk.ZoneBiomeResult;
import com.hypixel.hytale.server.worldgen.container.CoverContainer;
import com.hypixel.hytale.server.worldgen.container.UniquePrefabContainer;
import com.hypixel.hytale.server.worldgen.container.WaterContainer;
import com.hypixel.hytale.server.worldgen.loader.WorldGenPrefabSupplier;
import com.hypixel.hytale.server.worldgen.prefab.PrefabCategory;
import com.hypixel.hytale.server.worldgen.util.BlockFluidEntry;
import com.hypixel.hytale.server.worldgen.util.LogUtil;
import java.util.Random;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class UniquePrefabGenerator {
   private static final int UNIQUE_ZONE_PLACEMENT_HEURISTIC_ITERATIONS = 8;
   protected final String name;
   protected final PrefabCategory category;
   protected final IWeightedMap<WorldGenPrefabSupplier> prefabs;
   protected final UniquePrefabConfiguration configuration;
   protected final int zoneIndex;

   public UniquePrefabGenerator(
      String name, PrefabCategory category, IWeightedMap<WorldGenPrefabSupplier> prefabs, UniquePrefabConfiguration configuration, int zoneIndex
   ) {
      this.name = name;
      this.category = category;
      this.prefabs = prefabs;
      this.configuration = configuration;
      this.zoneIndex = zoneIndex;
   }

   public String getName() {
      return this.name;
   }

   public PrefabCategory getCategory() {
      return this.category;
   }

   public IWeightedMap<WorldGenPrefabSupplier> getPrefabs() {
      return this.prefabs;
   }

   @Nullable
   public WorldGenPrefabSupplier generatePrefab(Random random) {
      return this.prefabs.get(random);
   }

   @Nonnull
   public Vector3i generate(
      int seed,
      @Nullable Vector2i position,
      @Nonnull ChunkGenerator chunkGenerator,
      @Nonnull Random random,
      int maxFailed,
      @Nonnull UniquePrefabContainer.UniquePrefabEntry[] entries
   ) {
      if (position != null) {
         return this.forceUniqueZonePlacement(seed, position, chunkGenerator);
      } else {
         int failed = 0;

         Vector3i vec;
         while ((vec = this.tryPlacement(seed, chunkGenerator, random, entries)) == null) {
            if (++failed > maxFailed) {
               break;
            }
         }

         if (vec == null) {
            LogUtil.getLogger()
               .at(Level.SEVERE)
               .log(
                  "Failed to generate Unique-Prefab '%s' with anchor '%s', maxDistance: %s",
                  this.name,
                  this.configuration.getAnchor(),
                  this.configuration.getMaxDistance()
               );
            vec = this.forceGeneration(seed, chunkGenerator);
            LogUtil.getLogger().at(Level.WARNING).log("FORCED Unique-Prefab '%s' at %s after %s attempts!", this.name, vec, failed);
         } else {
            LogUtil.getLogger().at(Level.FINE).log("Generated Unique-Prefab '%s' at %s after %s attempts!", this.name, vec, failed);
         }

         return vec;
      }
   }

   @Nullable
   protected Vector3i tryPlacement(
      int seed, @Nonnull ChunkGenerator chunkGenerator, @Nonnull Random random, @Nonnull UniquePrefabContainer.UniquePrefabEntry[] entries
   ) {
      double x = this.configuration.getAnchor().getX();
      double z = this.configuration.getAnchor().getY();
      double distance = random.nextDouble() * this.configuration.getMaxDistance();
      float angle = random.nextFloat() * (float) (Math.PI * 2);
      x += TrigMathUtil.cos(angle) * distance;
      z += TrigMathUtil.sin(angle) * distance;
      int lx = MathUtil.floor(x);
      int lz = MathUtil.floor(z);

      for (UniquePrefabContainer.UniquePrefabEntry entry : entries) {
         if (entry != null) {
            double dx = entry.getPosition().x - x;
            double dz = entry.getPosition().z - z;
            double distancex = dx * dx + dz * dz;
            if (distancex <= entry.getExclusionRadiusSquared() || distancex <= this.configuration.getExclusionRadiusSquared()) {
               return null;
            }
         }
      }

      if (!this.isMatchingNoiseDensity(seed, lx, lz)) {
         return null;
      } else {
         ZoneBiomeResult result = chunkGenerator.getZoneBiomeResultAt(seed, lx, lz);
         if (result.getZoneResult().getZone().id() != this.zoneIndex) {
            return null;
         } else if (!this.configuration.isValidParentBiome(result.getBiome())) {
            return null;
         } else if (result.zoneResult.getBorderDistance() < this.configuration.getZoneBorderExclusion()) {
            return null;
         } else {
            int height = this.getHeight(seed, chunkGenerator, result.getBiome(), lx, lz);
            if (!this.isMatchingHeight(seed, lx, lz, random, height)) {
               return null;
            } else {
               return !this.isMatchingParentBlock(seed, lx, height, lz, random, result) ? null : new Vector3i(lx, height, lz);
            }
         }
      }
   }

   @Nonnull
   protected Vector3i forceGeneration(int seed, @Nonnull ChunkGenerator chunkGenerator) {
      double x = this.configuration.getAnchor().getX();
      double z = this.configuration.getAnchor().getY();
      int lx = MathUtil.floor(x);
      int lz = MathUtil.floor(z);
      ZoneBiomeResult result = chunkGenerator.getZoneBiomeResultAt(seed, lx, lz);
      int height = this.getHeight(seed, chunkGenerator, result.getBiome(), lx, lz);
      return new Vector3i(lx, height, lz);
   }

   @Nonnull
   protected Vector3i forceUniqueZonePlacement(int seed, @Nonnull Vector2i position, @Nonnull ChunkGenerator chunkGenerator) {
      MaskProvider maskProvider = chunkGenerator.getZonePatternProvider().getMaskProvider();
      int x = position.x;
      int z = position.y;

      for (int i = 0; i < 8; i++) {
         int px = MathUtil.floor(maskProvider.getX(seed, x, z));
         int pz = MathUtil.floor(maskProvider.getY(seed, x, z));
         int dx = px - position.x;
         int dz = pz - position.y;
         x -= dx / 2;
         z -= dz / 2;
      }

      ZoneBiomeResult result = chunkGenerator.getZoneBiomeResultAt(seed, x, z);
      int height = this.getHeight(seed, chunkGenerator, result.getBiome(), x, z);
      return new Vector3i(x, height, z);
   }

   protected int getHeight(int seed, @Nonnull ChunkGenerator chunkGenerator, @Nonnull Biome biome, int x, int z) {
      WaterContainer waterContainer = biome.getWaterContainer();
      return waterContainer.hasEntries() && this.configuration.isOnWater() ? waterContainer.getMaxHeight(seed, x, z) : chunkGenerator.getHeight(seed, x, z);
   }

   protected boolean isMatchingHeight(int seed, int x, int z, Random random, int y) {
      ICoordinateRndCondition heightCondition = this.configuration.getHeightCondition();
      return heightCondition == null || heightCondition.eval(seed, x, z, y, random);
   }

   protected boolean isMatchingNoiseDensity(int seed, int x, int z) {
      return this.configuration.getMapCondition().eval(seed, x, z);
   }

   protected boolean isMatchingParentBlock(int seed, int x, int y, int z, @Nonnull Random random, @Nonnull ZoneBiomeResult zoneAndBiomeResult) {
      BlockFluidEntry groundCover = this.getCoverInGroundAt(seed, x, y, z, random, zoneAndBiomeResult.getBiome());
      if (!groundCover.equals(BlockFluidEntry.EMPTY) && !this.configuration.isValidParentBlock(groundCover.blockId(), groundCover.fluidId())) {
         return false;
      } else {
         BlockFluidEntry block = zoneAndBiomeResult.getBiome().getLayerContainer().getTopBlockAt(seed, x, z);
         return this.configuration.isValidParentBlock(block.blockId(), block.fluidId());
      }
   }

   protected BlockFluidEntry getCoverInGroundAt(int seed, int x, int y, int z, @Nonnull Random random, @Nonnull Biome biome) {
      CoverContainer.CoverContainerEntry[] coverContainerEntries = biome.getCoverContainer().getEntries();

      for (CoverContainer.CoverContainerEntry coverContainerEntry : coverContainerEntries) {
         if (y < 320 && this.isMatchingCover(seed, coverContainerEntry, random, x, y, z)) {
            CoverContainer.CoverContainerEntry.CoverContainerEntryPart part = coverContainerEntry.get(random);
            if (part.getOffset() == -1) {
               return part.getEntry();
            }
         }
      }

      return BlockFluidEntry.EMPTY;
   }

   protected boolean isMatchingCover(int seed, @Nonnull CoverContainer.CoverContainerEntry coverContainerEntry, @Nonnull Random random, int x, int y, int z) {
      return random.nextDouble() < coverContainerEntry.getCoverDensity()
         && coverContainerEntry.getMapCondition().eval(seed, x, z)
         && coverContainerEntry.getHeightCondition().eval(seed, x, z, y, random);
   }

   public UniquePrefabConfiguration getConfiguration() {
      return this.configuration;
   }
}
