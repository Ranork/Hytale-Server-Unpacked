package com.hypixel.hytale.server.worldgen.cache;

import com.hypixel.hytale.server.worldgen.chunk.ZoneBiomeResult;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CoreDataCacheEntry implements Function<CoreDataCacheEntry, CoreDataCacheEntry> {
   public static final int NO_HEIGHT = -1;
   public static final double NO_HEIGHT_NOISE = Double.NEGATIVE_INFINITY;
   public final ZoneBiomeResult zoneBiomeResult;
   @Nullable
   public volatile InterpolatedBiomeCountList biomeCountList;
   public volatile int height;
   public volatile double heightNoise;

   public CoreDataCacheEntry() {
      this(new ZoneBiomeResult());
   }

   public CoreDataCacheEntry(@Nonnull ZoneBiomeResult zoneBiomeResult) {
      this.zoneBiomeResult = zoneBiomeResult;
      this.biomeCountList = null;
      this.height = -1;
      this.heightNoise = Double.NEGATIVE_INFINITY;
   }

   public CoreDataCacheEntry apply(CoreDataCacheEntry coreDataCacheEntry) {
      this.zoneBiomeResult.biome = coreDataCacheEntry.zoneBiomeResult.biome;
      this.zoneBiomeResult.zoneResult = coreDataCacheEntry.zoneBiomeResult.zoneResult;
      this.zoneBiomeResult.heightThresholdContext = coreDataCacheEntry.zoneBiomeResult.heightThresholdContext;
      this.zoneBiomeResult.heightmapNoise = coreDataCacheEntry.zoneBiomeResult.heightmapNoise;
      this.biomeCountList = coreDataCacheEntry.biomeCountList;
      this.height = coreDataCacheEntry.height;
      this.heightNoise = coreDataCacheEntry.heightNoise;
      return this;
   }
}
