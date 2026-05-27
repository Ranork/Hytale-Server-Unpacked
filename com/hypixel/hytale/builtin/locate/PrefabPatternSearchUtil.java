package com.hypixel.hytale.builtin.locate;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.iterator.SpiralIterator;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.FastRandom;
import com.hypixel.hytale.math.util.HashUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.server.worldgen.biome.Biome;
import com.hypixel.hytale.server.worldgen.chunk.ChunkGenerator;
import com.hypixel.hytale.server.worldgen.chunk.ZoneBiomeResult;
import com.hypixel.hytale.server.worldgen.container.PrefabContainer;
import com.hypixel.hytale.server.worldgen.container.UniquePrefabContainer;
import com.hypixel.hytale.server.worldgen.loader.WorldGenPrefabSupplier;
import com.hypixel.hytale.server.worldgen.prefab.PrefabPatternGenerator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.joml.Vector3i;

public final class PrefabPatternSearchUtil {
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   private static final int MAX_ITERATIONS = 10000000;

   private PrefabPatternSearchUtil() {
   }

   @Nonnull
   public static Collection<String> collectAllPrefabKeys(@Nonnull ChunkGenerator generator) {
      return Arrays.stream(generator.getZonePatternProvider().getZones())
         .flatMap(zone -> Arrays.stream(zone.biomePatternGenerator().getBiomes()))
         .map(Biome::getPrefabContainer)
         .filter(Objects::nonNull)
         .flatMap(container -> Arrays.stream(container.getEntries()))
         .flatMap(entry -> Arrays.stream(entry.getPrefabs().internalKeys()))
         .map(supplier -> stripWildcard(supplier.getPrefabKey()))
         .collect(Collectors.toCollection(LinkedHashSet::new));
   }

   @Nonnull
   public static List<Entry<String, Vector3i>> search(
      @Nonnull ChunkGenerator generator, int seed, int startX, int startZ, int maxRadius, @Nonnull String prefabKey, int maxResults
   ) {
      int chunkX = Math.floorDiv(startX, 32);
      int chunkZ = Math.floorDiv(startZ, 32);
      int radiusInChunks = maxRadius / 32;
      String searchKeyLower = prefabKey.toLowerCase(Locale.ROOT);
      long[] effectiveRadiusSq = new long[]{(long)maxRadius * maxRadius};
      UniquePrefabContainer.UniquePrefabEntry[] uniquePrefabs = generator.getUniquePrefabs(seed);
      ArrayList<Entry<String, Vector3i>> results = new ArrayList<>();
      HashSet<Long> seen = new HashSet<>();
      FastRandom random = new FastRandom(0L);
      Comparator<Entry<String, Vector3i>> byDistanceSq = Comparator.comparingLong(e -> {
         long ex = e.getValue().x - startX;
         long ez = e.getValue().z - startZ;
         return ex * ex + ez * ez;
      });
      SpiralIterator iterator = new SpiralIterator(chunkX, chunkZ, radiusInChunks);
      int iterations = 0;

      while (iterator.hasNext()) {
         if (iterations++ >= 10000000) {
            LOGGER.at(Level.WARNING).log("Prefab search for '%s' hit iteration cap (%d) with %d results found so far", prefabKey, 10000000, results.size());
            break;
         }

         long key = iterator.next();
         int blockX = ChunkUtil.xOfChunkIndex(key) * 32;
         int blockZ = ChunkUtil.zOfChunkIndex(key) * 32;
         ZoneBiomeResult zbr = generator.getZoneBiomeResultAt(seed, blockX, blockZ);
         Biome biome = zbr.getBiome();
         PrefabContainer container = biome.getPrefabContainer();
         if (container != null) {
            int id = 0;

            for (PrefabContainer.PrefabContainerEntry entry : container.getEntries()) {
               id++;
               if (entryMatchesPrefabKey(entry, searchKeyLower)) {
                  long prefabSeed = HashUtil.hash(seed, biome.getId(), id);
                  PrefabPatternGenerator patternGenerator = entry.getPrefabPatternGenerator();
                  int maxSize = patternGenerator.getMaxSize();
                  patternGenerator.getGridGenerator()
                     .collect(seed, blockX - maxSize, blockZ - maxSize, blockX + 32 + maxSize, blockZ + 32 + maxSize, (px, pz) -> {
                        int x = (int)MathUtil.fastFloor(px);
                        int z = (int)MathUtil.fastFloor(pz);
                        long posKey = (long)x << 32 | z & 4294967295L;
                        if (seen.add(posKey)) {
                           if (patternGenerator.getMapCondition().eval(seed, x, z)) {
                              ZoneBiomeResult candidateZbr = generator.getZoneBiomeResultAt(seed, x, z);
                              if (candidateZbr.getBiome() == biome) {
                                 if (uniquePrefabs == null || !isWithinUniquePrefabExclusionRange(x, z, patternGenerator, uniquePrefabs)) {
                                    random.setSeed(HashUtil.hash(prefabSeed, x, z) * 1609272495L);
                                    WorldGenPrefabSupplier supplier = entry.getPrefabs().get(random);
                                    if (supplier != null) {
                                       if (matchesPrefabKey(supplier, searchKeyLower)) {
                                          long dx = x - startX;
                                          long dz = z - startZ;
                                          long distSq = dx * dx + dz * dz;
                                          if (distSq <= effectiveRadiusSq[0]) {
                                             int y = estimateHeight(generator, seed, x, z, patternGenerator, biome);
                                             results.add(Map.entry(stripWildcard(supplier.getPrefabKey()), new Vector3i(x, y, z)));
                                             if (results.size() >= maxResults * 2) {
                                                results.sort(byDistanceSq);
                                                results.subList(maxResults, results.size()).clear();
                                                Vector3i worst = results.get(results.size() - 1).getValue();
                                                long wdx = worst.x - startX;
                                                long wdz = worst.z - startZ;
                                                effectiveRadiusSq[0] = wdx * wdx + wdz * wdz;
                                             }
                                          }
                                       }
                                    }
                                 }
                              }
                           }
                        }
                     });
               }
            }
         }
      }

      results.sort(byDistanceSq);
      if (results.size() > maxResults) {
         results.subList(maxResults, results.size()).clear();
      }

      return results;
   }

   private static boolean entryMatchesPrefabKey(@Nonnull PrefabContainer.PrefabContainerEntry entry, @Nonnull String searchKeyLower) {
      for (WorldGenPrefabSupplier supplier : entry.getPrefabs().internalKeys()) {
         if (matchesPrefabKey(supplier, searchKeyLower)) {
            return true;
         }
      }

      return false;
   }

   private static boolean matchesPrefabKey(@Nonnull WorldGenPrefabSupplier supplier, @Nonnull String searchKeyLower) {
      String keyLower = stripWildcard(supplier.getPrefabKey()).toLowerCase(Locale.ROOT);
      return keyLower.equals(searchKeyLower) || keyLower.startsWith(searchKeyLower + ".");
   }

   private static boolean isWithinUniquePrefabExclusionRange(
      int x, int z, @Nonnull PrefabPatternGenerator generator, @Nonnull UniquePrefabContainer.UniquePrefabEntry[] uniquePrefabs
   ) {
      long radius = generator.getExclusionRadius();
      if (radius <= 0L) {
         return false;
      } else {
         long radius2 = radius * radius;
         int priority = generator.getCategory().priority();

         for (UniquePrefabContainer.UniquePrefabEntry unique : uniquePrefabs) {
            if (priority < unique.getCategory().priority()) {
               long dx = x - unique.getPosition().x();
               long dz = z - unique.getPosition().z();
               if (dx * dx + dz * dz <= radius2) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   private static int estimateHeight(
      @Nonnull ChunkGenerator generator, int seed, int x, int z, @Nonnull PrefabPatternGenerator patternGenerator, @Nonnull Biome biome
   ) {
      int height;
      if (patternGenerator.isOnWater()) {
         height = biome.getWaterContainer().getMaxHeight(seed, x, z);
      } else if (patternGenerator.isDeepSearch()) {
         height = generator.generateHeightBetween(seed, x, z, patternGenerator.getHeightThresholdInterpreter());
      } else {
         height = generator.getHeight(seed, x, z);
      }

      return height + patternGenerator.getDisplacement(seed, x, z);
   }

   @Nonnull
   static String stripWildcard(@Nonnull String key) {
      return key.endsWith(".*") ? key.substring(0, key.length() - 2) : key;
   }
}
