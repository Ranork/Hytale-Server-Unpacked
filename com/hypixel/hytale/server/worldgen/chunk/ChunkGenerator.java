package com.hypixel.hytale.server.worldgen.chunk;

import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.logger.sentry.SkipSentryException;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector2i;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.metrics.MetricProvider;
import com.hypixel.hytale.metrics.MetricResults;
import com.hypixel.hytale.procedurallib.condition.IHeightThresholdInterpreter;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.worldgen.GeneratedBlockChunk;
import com.hypixel.hytale.server.core.universe.world.worldgen.GeneratedBlockStateChunk;
import com.hypixel.hytale.server.core.universe.world.worldgen.GeneratedChunk;
import com.hypixel.hytale.server.core.universe.world.worldgen.GeneratedEntityChunk;
import com.hypixel.hytale.server.core.universe.world.worldgen.IBenchmarkableWorldGen;
import com.hypixel.hytale.server.core.universe.world.worldgen.ValidatableWorldGen;
import com.hypixel.hytale.server.core.universe.world.worldgen.WorldGenTimingsCollector;
import com.hypixel.hytale.server.core.universe.world.worldmap.IWorldMap;
import com.hypixel.hytale.server.core.universe.world.worldmap.WorldMapLoadException;
import com.hypixel.hytale.server.core.universe.world.worldmap.provider.IWorldMapProvider;
import com.hypixel.hytale.server.worldgen.ChunkGeneratorResource;
import com.hypixel.hytale.server.worldgen.benchmark.ChunkWorldgenBenchmark;
import com.hypixel.hytale.server.worldgen.biome.Biome;
import com.hypixel.hytale.server.worldgen.cache.CaveGeneratorCache;
import com.hypixel.hytale.server.worldgen.cache.ChunkGeneratorCache;
import com.hypixel.hytale.server.worldgen.cache.CoreDataCacheEntry;
import com.hypixel.hytale.server.worldgen.cache.InterpolatedBiomeCountList;
import com.hypixel.hytale.server.worldgen.cache.UniquePrefabCache;
import com.hypixel.hytale.server.worldgen.cave.Cave;
import com.hypixel.hytale.server.worldgen.cave.CaveGenerator;
import com.hypixel.hytale.server.worldgen.cave.CaveType;
import com.hypixel.hytale.server.worldgen.container.FadeContainer;
import com.hypixel.hytale.server.worldgen.container.UniquePrefabContainer;
import com.hypixel.hytale.server.worldgen.map.GeneratorChunkWorldMap;
import com.hypixel.hytale.server.worldgen.prefab.PrefabLoadingCache;
import com.hypixel.hytale.server.worldgen.util.ArrayUtli;
import com.hypixel.hytale.server.worldgen.util.ChunkThreadPoolExecutor;
import com.hypixel.hytale.server.worldgen.util.ChunkWorkerThreadFactory;
import com.hypixel.hytale.server.worldgen.util.LogUtil;
import com.hypixel.hytale.server.worldgen.zone.Zone;
import com.hypixel.hytale.server.worldgen.zone.ZoneGeneratorResult;
import com.hypixel.hytale.server.worldgen.zone.ZonePatternGenerator;
import com.hypixel.hytale.server.worldgen.zone.ZonePatternGeneratorCache;
import com.hypixel.hytale.server.worldgen.zone.ZonePatternProvider;
import it.unimi.dsi.fastutil.ints.IntList;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.LongPredicate;
import java.util.function.Supplier;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ChunkGenerator implements IBenchmarkableWorldGen, ValidatableWorldGen, MetricProvider, IWorldMapProvider {
   public static final int TINT_INTERPOLATION_RADIUS = 4;
   private static final ThreadLocal<ChunkGeneratorResource> THREAD_LOCAL = ThreadLocal.withInitial(ChunkGeneratorResource::new);
   public static final int POOL_SIZE = Math.max(2, MathUtil.fastCeil(Runtime.getRuntime().availableProcessors() * 0.75F));
   @Nonnull
   private final ThreadPoolExecutor executor;
   @Nonnull
   private final WorldGenTimingsCollector timings;
   private final ZonePatternProvider zonePatternProvider;
   private final ZonePatternGeneratorCache zonePatternGeneratorCache;
   @Nonnull
   private final ChunkGeneratorCache generatorCache;
   @Nonnull
   private final CaveGeneratorCache caveGeneratorCache;
   @Nonnull
   private final PrefabLoadingCache prefabLoadingCache;
   @Nonnull
   private final UniquePrefabCache uniquePrefabCache;
   @Nonnull
   private final ChunkWorldgenBenchmark benchmark;
   @Nonnull
   private final Supplier<GeneratedChunk> generatedChunkSupplier;
   private final Path dataFolder;

   public ChunkGenerator(ZonePatternProvider zonePatternProvider, Path dataFolder) {
      this.dataFolder = dataFolder;
      this.executor = new ChunkThreadPoolExecutor(
         POOL_SIZE,
         POOL_SIZE,
         60L,
         TimeUnit.SECONDS,
         new LinkedBlockingQueue<>(),
         new ChunkWorkerThreadFactory(this, "ChunkGenerator-%d-Worker-%d"),
         this::onExecutorShutdown
      );
      this.executor.allowCoreThreadTimeOut(true);
      this.timings = new WorldGenTimingsCollector(this.executor);
      this.zonePatternProvider = zonePatternProvider;
      this.zonePatternGeneratorCache = new ZonePatternGeneratorCache(zonePatternProvider);
      this.generatorCache = new ChunkGeneratorCache(
         this::generateZoneBiomeResultAt, this::generateInterpolatedBiomeCountAt, this::generateHeight, this::generateInterpolatedHeightNoise, 50000, 20L
      );
      this.caveGeneratorCache = new CaveGeneratorCache(this::generateCave, 5000, 30L);
      this.uniquePrefabCache = new UniquePrefabCache(this::generateUniquePrefabs, 50, 300L);
      this.prefabLoadingCache = new PrefabLoadingCache();
      this.generatedChunkSupplier = GeneratedChunk::new;
      this.benchmark = new ChunkWorldgenBenchmark();
   }

   public ZonePatternProvider getZonePatternProvider() {
      return this.zonePatternProvider;
   }

   @Override
   public WorldGenTimingsCollector getTimings() {
      return this.timings;
   }

   @Nonnull
   @Override
   public IWorldMap getGenerator(World world) throws WorldMapLoadException {
      return new GeneratorChunkWorldMap(this, this.executor);
   }

   @Override
   public Transform[] getSpawnPoints(int seed) {
      return CompletableFuture.<Transform[]>supplyAsync(() -> {
         ArrayList<Transform> list = new ArrayList<>();

         for (UniquePrefabContainer.UniquePrefabEntry entry : this.getUniquePrefabs(seed)) {
            if (entry.isSpawnLocation()) {
               Vector3i position = entry.getPosition();
               Vector3d spawnPosition = new Vector3d(entry.getSpawnOffset());
               Vector3f spawnRotation = new Vector3f(Vector3f.ZERO);
               entry.getRotation().rotate(spawnPosition);
               spawnRotation.addYaw(-entry.getRotation().getYaw());
               list.add(new Transform(spawnPosition.add(position).add(0.5, 0.0, 0.5), spawnRotation));
            }
         }

         if (list.isEmpty()) {
            list.add(new Transform(16.5, -1.0, 16.5));
         }

         Transform[] array = list.toArray(Transform[]::new);
         Random random = getResource().random;
         random.setSeed(seed * 1494360372L);
         ArrayUtli.shuffleArray((Transform[])array, random);
         return array;
      }, this.executor).join();
   }

   @Nonnull
   public ChunkWorldgenBenchmark getBenchmark() {
      return this.benchmark;
   }

   public Path getDataFolder() {
      return this.dataFolder;
   }

   @Nullable
   public CoreDataCacheEntry getCoreData(int seed, int x, int z) {
      return this.generatorCache.get(seed, x, z);
   }

   @Nonnull
   public ZonePatternGenerator getZonePatternGenerator(int seed) {
      return this.zonePatternGeneratorCache.get(seed);
   }

   public ZoneBiomeResult getZoneBiomeResultAt(int seed, int x, int z) {
      return this.generatorCache.getZoneBiomeResult(seed, x, z);
   }

   public int getHeight(int seed, int x, int z) {
      return this.generatorCache.getHeight(seed, x, z);
   }

   public void putHeight(int seed, int x, int z, int y) {
      this.generatorCache.putHeight(seed, x, z, y);
   }

   @Nullable
   public InterpolatedBiomeCountList getInterpolatedBiomeCountAt(int seed, int x, int z) {
      return this.generatorCache.getBiomeCountResult(seed, x, z);
   }

   @Nullable
   public Cave getCave(@Nonnull CaveType caveType, int seed, int x, int z) {
      return this.caveGeneratorCache.get(caveType, seed, x, z);
   }

   @Nonnull
   public PrefabLoadingCache getPrefabLoadingCache() {
      return this.prefabLoadingCache;
   }

   @Nullable
   public UniquePrefabContainer.UniquePrefabEntry[] getUniquePrefabs(int seed) {
      return this.uniquePrefabCache.get(seed);
   }

   @Nonnull
   @Override
   public CompletableFuture<GeneratedChunk> generate(int seed, long index, int x, int z, @Nullable LongPredicate stillNeeded) {
      return CompletableFuture.<GeneratedChunk>supplyAsync(() -> {
         if (stillNeeded != null && !stillNeeded.test(index)) {
            return null;
         } else {
            long start = -System.nanoTime();
            GeneratedChunk generatedChunk = this.generatedChunkSupplier.get();
            GeneratedBlockChunk blockChunk = generatedChunk.getBlockChunk();
            blockChunk.setCoordinates(index, x, z);
            GeneratedBlockStateChunk blockStateChunk = generatedChunk.getBlockStateChunk();
            GeneratedEntityChunk entityChunk = generatedChunk.getEntityChunk();
            Holder<ChunkStore>[] sections = generatedChunk.getSections();
            new ChunkGeneratorExecution(seed, this, blockChunk, blockStateChunk, entityChunk, sections).execute(seed);
            long end = System.nanoTime();
            double time = (end + start) / 1.0E9;
            double avg = this.timings.reportChunk(end + start);
            if (avg != this.timings.getWarmupValue()) {
               LogUtil.getLogger().at(Level.FINE).log("Time taken: %s (avg: %s) (%s)", time, avg, this.timings);
            } else {
               LogUtil.getLogger().at(Level.FINE).log("Time taken: %s (warming up)", time);
            }

            return generatedChunk;
         }
      }, this.executor).exceptionally(t -> {
         throw new SkipSentryException(t);
      });
   }

   @Override
   public void shutdown() {
      this.executor.shutdown();
   }

   @Nonnull
   public ZoneBiomeResult generateZoneBiomeResultAt(int seed, int x, int z) {
      return this.generateZoneBiomeResultAt(seed, x, z, new ZoneBiomeResult());
   }

   @Nonnull
   public ZoneBiomeResult generateZoneBiomeResultAt(int seed, int x, int z, @Nonnull ZoneBiomeResult result) {
      long time = -System.nanoTime();
      ZonePatternGenerator zonePatternGenerator = this.getZonePatternGenerator(seed);
      ZoneGeneratorResult tempZoneResult = result.getZoneResult();
      ZoneGeneratorResult zoneResult = zonePatternGenerator.generate(seed, x, z, tempZoneResult != null ? tempZoneResult : new ZoneGeneratorResult());
      Biome biome = zoneResult.getZone().biomePatternGenerator().generateBiomeAt(zoneResult, seed, x, z);
      double heightThresholdContext = biome.getHeightmapInterpreter().getContext(seed, x, z);
      double heightmapNoise = biome.getHeightmapNoise().get(seed, x, z);
      FadeContainer fadeContainer = biome.getFadeContainer();
      if (fadeContainer.shouldFade()) {
         double factor = fadeContainer.getTerrainFactor(zoneResult);
         heightmapNoise = heightmapNoise * (1.0 - factor) + fadeContainer.getFadeHeightmap() * factor;
      }

      result.setZoneResult(zoneResult);
      result.setBiome(biome);
      result.setHeightThresholdContext(heightThresholdContext);
      result.setHeightmapNoise(heightmapNoise);
      this.timings.reportZoneBiomeResult(time + System.nanoTime());
      return result;
   }

   public void generateInterpolatedBiomeCountAt(int seed, int x, int z, @Nonnull InterpolatedBiomeCountList biomeCountList) {
      ZoneBiomeResult center = this.getZoneBiomeResultAt(seed, x, z);
      biomeCountList.setCenter(center);
      int radius = center.getBiome().getInterpolation().getRadius();
      int radius2 = radius * radius;

      for (int ix = -radius; ix <= radius; ix++) {
         for (int iz = -radius; iz <= radius; iz++) {
            if (ix != 0 || iz != 0) {
               int distance2 = ix * ix + iz * iz;
               if (distance2 <= radius2) {
                  ZoneBiomeResult biomeResult = this.getZoneBiomeResultAt(seed, x + ix, z + iz);
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

   public int generateLowestThresholdDependent(@Nonnull InterpolatedBiomeCountList biomeCounts) {
      int lowestNonOne = 320;
      IntList biomes = biomeCounts.getBiomeIds();
      int i = 0;

      for (int size = biomes.size(); i < size; i++) {
         int id = biomes.getInt(i);
         int v;
         if ((v = biomeCounts.get(id).biome.getHeightmapInterpreter().getLowestNonOne()) < lowestNonOne) {
            lowestNonOne = v;
         }
      }

      return lowestNonOne;
   }

   public int generateHighestThresholdDependent(@Nonnull InterpolatedBiomeCountList biomeCounts) {
      int highestNonZero = -1;
      IntList biomes = biomeCounts.getBiomeIds();
      int i = 0;

      for (int size = biomes.size(); i < size; i++) {
         int id = biomes.getInt(i);
         int v = biomeCounts.get(id).biome.getHeightmapInterpreter().getHighestNonZero();
         if (v > highestNonZero) {
            highestNonZero = v;
         }
      }

      return highestNonZero;
   }

   public static float generateInterpolatedThreshold(int seed, int x, int z, int y, @Nonnull InterpolatedBiomeCountList biomeCounts) {
      float threshold = 0.0F;
      int counter = 0;
      IntList biomes = biomeCounts.getBiomeIds();
      int i = 0;

      for (int size = biomes.size(); i < size; i++) {
         InterpolatedBiomeCountList.BiomeCountResult r = biomeCounts.get(biomes.getInt(i));
         threshold += r.biome.getHeightmapInterpreter().getThreshold(seed, x, z, y, r.heightThresholdContext) * r.count;
         counter += r.count;
      }

      return threshold / counter;
   }

   public double generateInterpolatedHeightNoise(@Nonnull InterpolatedBiomeCountList biomeCounts) {
      double n = 0.0;
      int counter = 0;
      IntList biomes = biomeCounts.getBiomeIds();
      int i = 0;

      for (int size = biomes.size(); i < size; i++) {
         InterpolatedBiomeCountList.BiomeCountResult r = biomeCounts.get(biomes.getInt(i));
         n += r.heightNoise * r.count;
         counter += r.count;
      }

      return n / counter;
   }

   public int generateHeight(int seed, int x, int z) {
      CoreDataCacheEntry entry = this.getCoreData(seed, x, z);
      this.generatorCache.ensureHeightNoise(seed, x, z, entry);
      InterpolatedBiomeCountList biomeCounts = entry.biomeCountList;
      double heightNoise = entry.heightNoise;

      for (int y = this.generateHighestThresholdDependent(biomeCounts); y > 0; y--) {
         float threshold = generateInterpolatedThreshold(seed, x, z, y, biomeCounts);
         if (threshold > heightNoise || threshold == 1.0) {
            return y;
         }
      }

      return 0;
   }

   public int generateHeightBetween(int seed, int x, int z, @Nonnull IHeightThresholdInterpreter interpreter) {
      CoreDataCacheEntry entry = this.getCoreData(seed, x, z);
      this.generatorCache.ensureHeightNoise(seed, x, z, entry);
      InterpolatedBiomeCountList biomeCounts = entry.biomeCountList;
      double heightNoise = entry.heightNoise;

      for (int y = this.generateHighestThresholdDependent(biomeCounts); y > 0; y--) {
         if (interpreter.isSpawnable(y)) {
            float threshold = generateInterpolatedThreshold(seed, x, z, y, biomeCounts);
            if (threshold > heightNoise || threshold == 1.0) {
               return y;
            }
         }
      }

      return 0;
   }

   @Nullable
   public Cave generateCave(@Nonnull CaveType caveType, int seed, int x, int z) {
      ZoneBiomeResult zoneBiomeResult = this.getZoneBiomeResultAt(seed, x, z);
      CaveGenerator caveGenerator = zoneBiomeResult.zoneResult.getZone().caveGenerator();
      if (caveGenerator == null) {
         return null;
      } else {
         int height = this.getHeight(seed, x, z);
         return caveGenerator.generate(seed, this, caveType, x, height, z);
      }
   }

   @Nonnull
   public UniquePrefabContainer.UniquePrefabEntry[] generateUniquePrefabs(int seed) {
      ZonePatternGenerator zonePatternGenerator = this.getZonePatternGenerator(seed);
      ArrayList<UniquePrefabContainer.UniquePrefabEntry> entries = new ArrayList<>();
      BitSet visited = new BitSet(zonePatternGenerator.getZones().length);

      for (Zone.Unique uniqueZone : zonePatternGenerator.getUniqueZones()) {
         Vector2i position = uniqueZone.getPosition();
         UniquePrefabContainer.UniquePrefabEntry[] zoneEntries = uniqueZone.zone().uniquePrefabContainer().generate(seed, position, this);
         entries.addAll(Arrays.asList(zoneEntries));
         visited.set(uniqueZone.zone().id());
      }

      for (Zone zone : zonePatternGenerator.getZones()) {
         if (!visited.get(zone.id())) {
            UniquePrefabContainer.UniquePrefabEntry[] zoneEntries = zone.uniquePrefabContainer().generate(seed, (Vector2i)null, this);
            entries.addAll(Arrays.asList(zoneEntries));
         }
      }

      return entries.toArray(UniquePrefabContainer.UniquePrefabEntry[]::new);
   }

   protected final void onExecutorShutdown() {
      this.prefabLoadingCache.clear();
   }

   public static ChunkGeneratorResource getResource() {
      return THREAD_LOCAL.get();
   }

   @Override
   public boolean validate() {
      return !ValidationUtil.isInvalid(this.zonePatternProvider, this.executor);
   }

   @Nonnull
   @Override
   public MetricResults toMetricResults() {
      return WorldGenTimingsCollector.METRICS_REGISTRY.toMetricResults(this.timings);
   }

   @Nonnull
   public String toString(boolean timings, boolean zonePatternGenerator) {
      return "ChunkGenerator{timings="
         + (timings ? this.timings : "-hidden-")
         + ", zonePatternProvider="
         + (zonePatternGenerator ? this.zonePatternProvider : "-hidden-")
         + ", generatorCache="
         + this.generatorCache
         + ", caveGeneratorCache="
         + this.caveGeneratorCache
         + ", uniquePrefabCache="
         + this.uniquePrefabCache
         + "}";
   }

   @Nonnull
   @Override
   public String toString() {
      return this.toString(true, true);
   }
}
