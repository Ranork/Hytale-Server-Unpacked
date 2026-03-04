package com.hypixel.hytale.server.worldgen.cache;

import com.hypixel.hytale.metrics.metric.AverageCollector;
import com.hypixel.hytale.server.worldgen.biome.Biome;
import com.hypixel.hytale.server.worldgen.chunk.ZoneBiomeResult;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import javax.annotation.Nonnull;

public class InterpolatedBiomeCountList {
   @Nonnull
   private final Int2ObjectMap<InterpolatedBiomeCountList.BiomeCountResult> results = new Int2ObjectOpenHashMap();
   @Nonnull
   private final IntList biomeIds = new IntArrayList();
   private Biome center;

   public InterpolatedBiomeCountList.BiomeCountResult get(@Nonnull Biome biome) {
      return this.get(biome.getId());
   }

   public InterpolatedBiomeCountList.BiomeCountResult get(int index) {
      return (InterpolatedBiomeCountList.BiomeCountResult)this.results.get(index);
   }

   public void setCenter(@Nonnull ZoneBiomeResult result) {
      Biome biome = result.getBiome();
      this.center = biome;
      this.biomeIds.add(biome.getId());
      this.results.put(biome.getId(), new InterpolatedBiomeCountList.BiomeCountResult(biome, result.heightThresholdContext, result.heightmapNoise));
   }

   public void add(@Nonnull ZoneBiomeResult result, int distance2) {
      Biome biome = result.getBiome();
      int biomeId = biome.getId();
      if (this.center.getInterpolation().getBiomeRadius2(biomeId) >= distance2) {
         InterpolatedBiomeCountList.BiomeCountResult r = this.get(biomeId);
         if (r == null) {
            this.biomeIds.add(biomeId);
            this.results.put(biomeId, new InterpolatedBiomeCountList.BiomeCountResult(biome, result.heightThresholdContext, result.heightmapNoise));
         } else {
            r.heightNoise = AverageCollector.add(r.heightNoise, result.heightmapNoise, r.count);
            r.count++;
         }
      }
   }

   @Nonnull
   public IntList getBiomeIds() {
      return this.biomeIds;
   }

   @Nonnull
   @Override
   public String toString() {
      return "InterpolatedBiomeCountList{results=" + this.results + ", biomeIds=" + this.biomeIds + "}";
   }

   public static class BiomeCountResult {
      @Nonnull
      public final Biome biome;
      public double heightThresholdContext;
      public double heightNoise;
      public int count;

      public BiomeCountResult(@Nonnull Biome biome, double heightThresholdContext, double heightNoise) {
         this.biome = biome;
         this.heightThresholdContext = heightThresholdContext;
         this.heightNoise = heightNoise;
         this.count = 1;
      }

      @Nonnull
      @Override
      public String toString() {
         return "BiomeCountResult{biome="
            + this.biome
            + ", heightThresholdContext="
            + this.heightThresholdContext
            + ", heightNoise="
            + this.heightNoise
            + ", count="
            + this.count
            + "}";
      }
   }
}
