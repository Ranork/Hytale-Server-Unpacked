package com.hypixel.hytale.server.worldgen.chunk;

import com.hypixel.hytale.server.worldgen.cache.CoreDataCacheEntry;
import com.hypixel.hytale.server.worldgen.cache.InterpolatedBiomeCountList;
import javax.annotation.Nonnull;

public class HeightThresholdInterpolator {
   public static final int MAX_RADIUS = 5;
   public static final int MAX_RADIUS2 = 25;
   private final ChunkGeneratorExecution execution;
   @Nonnull
   private final CoreDataCacheEntry[] entries;
   private final int radius;
   private final int size;
   private final int totalSize;

   public HeightThresholdInterpolator(ChunkGeneratorExecution execution) {
      this.execution = execution;
      this.radius = 5;
      this.size = 32;
      this.totalSize = this.size + 2 * this.radius;
      this.entries = new CoreDataCacheEntry[this.totalSize * this.totalSize];
   }

   @Nonnull
   public CoreDataCacheEntry[] getEntries() {
      return this.entries;
   }

   @Nonnull
   public HeightThresholdInterpolator populate(int seed) {
      ChunkGenerator generator = this.execution.getChunkGenerator();
      int cx = -this.radius;

      for (int mx = this.size + this.radius; cx < mx; cx++) {
         int cz = -this.radius;

         for (int mz = this.size + this.radius; cz < mz; cz++) {
            this.setTableEntry(cx, cz, generator.getCoreData(seed, this.execution.globalX(cx), this.execution.globalZ(cz)));
         }
      }

      for (int cxx = 0; cxx < this.size; cxx++) {
         for (int cz = 0; cz < this.size; cz++) {
            CoreDataCacheEntry entry = this.tableEntry(cxx, cz);
            if (entry.biomeCountList == null) {
               InterpolatedBiomeCountList list = new InterpolatedBiomeCountList();
               this.generateInterpolatedBiomeCountAt(cxx, cz, list);
               entry.biomeCountList = list;
            }

            if (entry.heightNoise == Double.NEGATIVE_INFINITY) {
               entry.heightNoise = generator.generateInterpolatedHeightNoise(entry.biomeCountList);
            }
         }
      }

      return this;
   }

   public void generateInterpolatedBiomeCountAt(int cx, int cz, @Nonnull InterpolatedBiomeCountList biomeCountList) {
      ZoneBiomeResult center = this.tableEntry(cx, cz).zoneBiomeResult;
      biomeCountList.setCenter(center);
      int radius = center.getBiome().getInterpolation().getRadius();
      int radius2 = radius * radius;

      for (int ix = -radius; ix <= radius; ix++) {
         for (int iz = -radius; iz <= radius; iz++) {
            if (ix != 0 || iz != 0) {
               int distance2 = ix * ix + iz * iz;
               if (distance2 <= radius2) {
                  ZoneBiomeResult biomeResult = this.tableEntry(cx + ix, cz + iz).zoneBiomeResult;
                  biomeCountList.add(biomeResult, distance2);
               }
            }
         }
      }

      if (biomeCountList.getBiomeIds().size() == 1) {
         InterpolatedBiomeCountList.BiomeCountResult result = biomeCountList.get(center.getBiome());
         result.heightNoise = center.heightmapNoise;
         result.count = 1;
      }
   }

   public double getHeightNoise(int cx, int cz) {
      return this.tableEntry(cx, cz).heightNoise;
   }

   public float getHeightThreshold(int seed, int x, int z, int y) {
      return this.interpolateHeightThreshold(seed, x, z, y);
   }

   private float interpolateHeightThreshold(int seed, int x, int z, int y) {
      CoreDataCacheEntry entry = this.tableEntry(this.execution.localX(x), this.execution.localZ(z));
      return ChunkGenerator.generateInterpolatedThreshold(seed, x, z, y, entry.biomeCountList);
   }

   protected CoreDataCacheEntry tableEntry(int cx, int cz) {
      return this.entries[this.indexLocal(cx, cz)];
   }

   protected void setTableEntry(int cx, int cz, CoreDataCacheEntry entry) {
      this.entries[this.indexLocal(cx, cz)] = entry;
   }

   protected ZoneBiomeResult zoneBiomeResult(int cx, int cz) {
      return this.tableEntry(cx, cz).zoneBiomeResult;
   }

   public int getLowestNonOne(int cx, int cz) {
      return this.execution.getChunkGenerator().generateLowestThresholdDependent(this.tableEntry(cx, cz).biomeCountList);
   }

   public int getHighestNonZero(int cx, int cz) {
      return this.execution.getChunkGenerator().generateHighestThresholdDependent(this.tableEntry(cx, cz).biomeCountList);
   }

   protected int indexLocal(int x, int z) {
      return (x + this.radius) * this.totalSize + z + this.radius;
   }
}
