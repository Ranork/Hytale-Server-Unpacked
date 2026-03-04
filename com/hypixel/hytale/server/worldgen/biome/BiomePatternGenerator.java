package com.hypixel.hytale.server.worldgen.biome;

import com.hypixel.hytale.common.map.IWeightedMap;
import com.hypixel.hytale.math.util.HashUtil;
import com.hypixel.hytale.procedurallib.logic.ResultBuffer;
import com.hypixel.hytale.procedurallib.logic.point.IPointGenerator;
import com.hypixel.hytale.server.worldgen.zone.ZoneGeneratorResult;
import java.util.Arrays;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BiomePatternGenerator {
   protected final IPointGenerator pointGenerator;
   @Nonnull
   protected final IWeightedMap<TileBiome> tileBiomes;
   @Nonnull
   protected final CustomBiome[] customBiomes;
   @Nonnull
   protected final Biome[] biomes;
   protected final int extents;

   public BiomePatternGenerator(IPointGenerator pointGenerator, @Nonnull IWeightedMap<TileBiome> tileBiomes, @Nonnull CustomBiome[] customBiomes) {
      this.pointGenerator = pointGenerator;
      this.tileBiomes = tileBiomes;
      this.customBiomes = customBiomes;
      this.biomes = new Biome[tileBiomes.size() + customBiomes.length];
      int n = 0;

      for (TileBiome biome : tileBiomes.internalKeys()) {
         this.biomes[n++] = biome;
      }

      for (CustomBiome biome : customBiomes) {
         this.biomes[n++] = biome;
      }

      this.extents = getExtents(this.biomes);
   }

   public int getExtents() {
      return this.extents;
   }

   @Nonnull
   public Biome[] getBiomes() {
      return this.biomes;
   }

   @Nonnull
   public CustomBiome[] getCustomBiomes() {
      return this.customBiomes;
   }

   @Nullable
   public TileBiome getBiome(int seed, int x, int z) {
      return this.tileBiomes.get(seed, x, z, (iSeed, ix, iz, generator) -> generator.getBiomeIndex(iSeed, ix, iz), this);
   }

   protected double getBiomeIndex(int seed, int x, int z) {
      ResultBuffer.ResultBuffer2d buf = this.pointGenerator.nearest2D(seed, x, z);
      return HashUtil.random(seed, buf.ix, buf.iy);
   }

   @Nullable
   public TileBiome getBiomeDirect(int seed, int x, int z) {
      return this.tileBiomes.get(HashUtil.random(seed, Double.doubleToLongBits(x), Double.doubleToLongBits(z)));
   }

   @Nonnull
   public Biome generateBiomeAt(@Nonnull ZoneGeneratorResult zoneResult, int seed, int x, int z) {
      TileBiome parentResult = this.getBiome(seed, x, z);
      CustomBiome customBiome = this.getCustomBiomeAt(seed, x, z, zoneResult, parentResult);
      return Objects.requireNonNullElse(customBiome, parentResult);
   }

   @Nullable
   public CustomBiome getCustomBiomeAt(int seed, double x, double z, @Nonnull ZoneGeneratorResult zoneResult, @Nonnull Biome parentResult) {
      if (this.customBiomes.length > 0) {
         int parentBiomeIndex = parentResult.getId();

         for (CustomBiome customBiome : this.customBiomes) {
            CustomBiomeGenerator customBiomeGenerator = customBiome.getCustomBiomeGenerator();
            if (customBiomeGenerator.isValidParentBiome(parentBiomeIndex) && customBiomeGenerator.shouldGenerateAt(seed, x, z, zoneResult, customBiome)) {
               return customBiome;
            }
         }
      }

      return null;
   }

   @Nonnull
   @Override
   public String toString() {
      return "BiomePatternGenerator{pointGenerator="
         + this.pointGenerator
         + ", tileBiomes="
         + this.tileBiomes
         + ", customBiomes="
         + Arrays.toString((Object[])this.customBiomes)
         + ", biomes="
         + Arrays.toString((Object[])this.biomes)
         + "}";
   }

   private static int getExtents(@Nonnull Biome[] biomes) {
      int maxExtent = 0;

      for (Biome biome : biomes) {
         if (biome.getPrefabContainer() != null) {
            maxExtent = Math.max(maxExtent, biome.getPrefabContainer().getMaxSize());
         }
      }

      return maxExtent;
   }
}
