package com.hypixel.hytale.server.worldgen.zone;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class ZonePatternGeneratorCache {
   protected final Function<Integer, ZonePatternGenerator> compute;
   protected final Map<Integer, ZonePatternGenerator> cache = new ConcurrentHashMap<>();

   public ZonePatternGeneratorCache(ZonePatternProvider provider) {
      this.compute = provider::createGenerator;
   }

   public ZonePatternGenerator get(int seed) {
      try {
         return this.cache.computeIfAbsent(seed, this.compute);
      } catch (Exception var3) {
         throw new Error("Failed to receive UniquePrefabEntry for " + seed, var3);
      }
   }
}
