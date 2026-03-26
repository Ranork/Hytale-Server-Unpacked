package com.hypixel.hytale.builtin.hytalegenerator.plugin;

import com.hypixel.hytale.builtin.hytalegenerator.FutureUtils;
import com.hypixel.hytale.builtin.hytalegenerator.LoggerUtil;
import com.hypixel.hytale.builtin.hytalegenerator.PropRuntime;
import com.hypixel.hytale.builtin.hytalegenerator.assets.AssetManager;
import com.hypixel.hytale.builtin.hytalegenerator.assets.SettingsAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders.PositionProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.worldstructures.WorldStructureAsset;
import com.hypixel.hytale.builtin.hytalegenerator.biome.Biome;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3d;
import com.hypixel.hytale.builtin.hytalegenerator.commands.ViewportCommand;
import com.hypixel.hytale.builtin.hytalegenerator.engine.bufferbundle.buffers.CountedPixelBuffer;
import com.hypixel.hytale.builtin.hytalegenerator.engine.bufferbundle.buffers.EntityBuffer;
import com.hypixel.hytale.builtin.hytalegenerator.engine.bufferbundle.buffers.SimplePixelBuffer;
import com.hypixel.hytale.builtin.hytalegenerator.engine.bufferbundle.buffers.VoxelBuffer;
import com.hypixel.hytale.builtin.hytalegenerator.engine.bufferbundle.buffers.type.BufferType;
import com.hypixel.hytale.builtin.hytalegenerator.engine.bufferbundle.buffers.type.ParametrizedBufferType;
import com.hypixel.hytale.builtin.hytalegenerator.engine.chunkgenerator.ChunkGenerator;
import com.hypixel.hytale.builtin.hytalegenerator.engine.chunkgenerator.ChunkRequest;
import com.hypixel.hytale.builtin.hytalegenerator.engine.chunkgenerator.FallbackGenerator;
import com.hypixel.hytale.builtin.hytalegenerator.engine.chunkgenerator.StagedChunkGenerator;
import com.hypixel.hytale.builtin.hytalegenerator.engine.performanceinstruments.TimeInstrument;
import com.hypixel.hytale.builtin.hytalegenerator.engine.stages.BiomeDistanceStage;
import com.hypixel.hytale.builtin.hytalegenerator.engine.stages.BiomeStage;
import com.hypixel.hytale.builtin.hytalegenerator.engine.stages.EnvironmentStage;
import com.hypixel.hytale.builtin.hytalegenerator.engine.stages.PropStage;
import com.hypixel.hytale.builtin.hytalegenerator.engine.stages.Stage;
import com.hypixel.hytale.builtin.hytalegenerator.engine.stages.TerrainStage;
import com.hypixel.hytale.builtin.hytalegenerator.engine.stages.TintStage;
import com.hypixel.hytale.builtin.hytalegenerator.material.MaterialCache;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.PositionProvider;
import com.hypixel.hytale.builtin.hytalegenerator.referencebundle.ReferenceBundle;
import com.hypixel.hytale.builtin.hytalegenerator.rng.SeedBox;
import com.hypixel.hytale.builtin.hytalegenerator.workerindexer.WorkerIndexer;
import com.hypixel.hytale.builtin.hytalegenerator.worldstructure.WorldStructure;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.events.RemoveWorldEvent;
import com.hypixel.hytale.server.core.universe.world.worldgen.GeneratedChunk;
import com.hypixel.hytale.server.core.universe.world.worldgen.provider.IWorldGenProvider;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;

public class HytaleGenerator extends JavaPlugin {
   private AssetManager assetManager;
   private Runnable assetReloadListener;
   @Nonnull
   private final Map<ChunkRequest.GeneratorProfile, ChunkGenerator> generators = new HashMap<>();
   @Nonnull
   private final Semaphore chunkGenerationSemaphore = new Semaphore(1);
   private int concurrency;
   private ExecutorService mainExecutor;
   private ThreadPoolExecutor concurrentExecutor;
   private int worldCounter;
   @Nonnull
   public static Vector3d DEFAULT_SPAWN_POSITION = new Vector3d(0.0, 140.0, 0.0);

   @Override
   protected void start() {
      super.start();
      if (this.mainExecutor == null) {
         this.loadExecutors(this.assetManager.getSettingsAsset());
      }

      if (this.assetReloadListener == null) {
         this.assetReloadListener = () -> this.reloadGenerators();
         this.assetManager.registerReloadListener(this.assetReloadListener);
      }
   }

   @Nonnull
   public List<Vector3d> getSpawnPositions(@Nonnull ChunkRequest.GeneratorProfile profile, int maxPositionsCount) {
      assert maxPositionsCount >= 0;

      if (profile.worldStructureName() == null) {
         LoggerUtil.getLogger().warning("World Structure asset not loaded.");
         return List.of(DEFAULT_SPAWN_POSITION);
      } else {
         WorldStructureAsset worldStructureAsset = this.assetManager.getWorldStructureAsset(profile.worldStructureName());
         if (worldStructureAsset == null) {
            LoggerUtil.getLogger().warning("World Structure asset not found: " + profile.worldStructureName());
            return List.of(DEFAULT_SPAWN_POSITION);
         } else {
            SeedBox seed = new SeedBox(profile.seed());
            PositionProvider spawnPositionProvider = worldStructureAsset.getSpawnPositionsAsset()
               .build(new PositionProviderAsset.Argument(seed, new ReferenceBundle(), WorkerIndexer.Id.MAIN));
            List<Vector3d> positions = new ArrayList<>(maxPositionsCount);
            Bounds3d bounds = new Bounds3d(
               new Vector3d(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY),
               new Vector3d(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)
            );
            PositionProvider.Context context = new PositionProvider.Context(bounds, (position, control) -> {
               if (positions.size() >= maxPositionsCount) {
                  control.stop = true;
               } else {
                  positions.add(position);
               }
            }, null);
            spawnPositionProvider.generate(context);
            return positions;
         }
      }
   }

   @Nonnull
   public CompletableFuture<GeneratedChunk> submitChunkRequest(@Nonnull ChunkRequest request) {
      return CompletableFuture.<GeneratedChunk>supplyAsync(() -> {
         GeneratedChunk var3;
         try {
            this.chunkGenerationSemaphore.acquireUninterruptibly();
            ChunkGenerator generator = this.getGenerator(request.generatorProfile());
            var3 = generator.generate(request.arguments());
         } finally {
            this.chunkGenerationSemaphore.release();
         }

         return var3;
      }, this.mainExecutor).handle((r, e) -> {
         if (e == null) {
            return (GeneratedChunk)r;
         } else {
            LoggerUtil.logException("generation of the chunk with request " + request, e, LoggerUtil.getLogger());
            return FallbackGenerator.INSTANCE.generate(request.arguments());
         }
      });
   }

   @Override
   protected void setup() {
      this.assetManager = new AssetManager(this.getEventRegistry(), this.getLogger());
      BuilderCodec<HandleProvider> generatorProvider = BuilderCodec.builder(HandleProvider.class, () -> new HandleProvider(this, this.worldCounter++))
         .documentation("The standard generator for Hytale.")
         .append(new KeyedCodec<>("WorldStructure", Codec.STRING, true), HandleProvider::setWorldStructureName, HandleProvider::getWorldStructureName)
         .documentation("The world structure to be used for this world.")
         .add()
         .append(new KeyedCodec<>("SeedOverride", Codec.STRING, false), HandleProvider::setSeedOverride, HandleProvider::getSeedOverride)
         .documentation("If set, this will override the world's seed to ensure consistency.")
         .add()
         .build();
      IWorldGenProvider.CODEC.register("HytaleGenerator", HandleProvider.class, generatorProvider);
      this.getCommandRegistry().registerCommand(new ViewportCommand(this.assetManager));
      this.getEventRegistry().registerGlobal(RemoveWorldEvent.class, event -> {
         if (event.getWorld().getChunkStore().getGenerator() instanceof Handle handle) {
            this.generators.remove(handle.getProfile());
         }
      });
   }

   @Nonnull
   public StagedChunkGenerator createStagedChunkGenerator(
      @Nonnull ChunkRequest.GeneratorProfile generatorProfile, @Nonnull WorldStructureAsset worldStructureAsset, @Nonnull SettingsAsset settingsAsset
   ) {
      WorkerIndexer workerIndexer = new WorkerIndexer(this.concurrency);
      SeedBox seed = new SeedBox(generatorProfile.seed());
      MaterialCache materialCache = new MaterialCache();
      WorkerIndexer.Session workerSession = workerIndexer.createSession();
      WorkerIndexer.Data<WorldStructure> worldStructure_workerData = new WorkerIndexer.Data<>(workerIndexer.getWorkerCount(), () -> null);
      TimeInstrument.Probe assetLoad_timeProbe = new TimeInstrument.Probe("Assets Loading").start();
      List<CompletableFuture<Void>> futures = new ArrayList<>();

      while (workerSession.hasNext()) {
         WorkerIndexer.Id workerId = workerSession.next();
         CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            WorldStructure worldStructure = worldStructureAsset.build(new WorldStructureAsset.Argument(materialCache, seed, workerId));
            worldStructure_workerData.set(workerId, worldStructure);
         }, this.concurrentExecutor).handle((r, e) -> {
            if (e == null) {
               return (Void)r;
            } else {
               LoggerUtil.logException("during async initialization of world-gen logic from assets", e);
               return null;
            }
         });
         futures.add(future);
      }

      FutureUtils.allOf(futures).join();
      worldStructureAsset.cleanUp();
      assetLoad_timeProbe.stop();
      String assetLoadingTime_ms = LoggerUtil.nsToMsDecimal(assetLoad_timeProbe.getTotalTime_ns());
      LoggerUtil.getLogger().info("Loaded World Structure " + generatorProfile.worldStructureName() + ": " + assetLoadingTime_ms + " ms");
      StagedChunkGenerator.Builder generatorBuilder = new StagedChunkGenerator.Builder();
      WorldStructure worldStructure_worker0 = worldStructure_workerData.get(workerIndexer.createSession().next());
      List<Biome> allBiomes = worldStructure_worker0.getBiomeRegistry().getAllValues();
      List<Integer> allRuntimes = new ArrayList<>(getAllPossibleRuntimeIndices(allBiomes));
      allRuntimes.sort(Comparator.naturalOrder());
      int bufferTypeIndexCounter = 0;
      ParametrizedBufferType biome_bufferType = new ParametrizedBufferType(
         "Biome", bufferTypeIndexCounter++, BiomeStage.bufferClass, BiomeStage.biomeClass, () -> new CountedPixelBuffer<>(BiomeStage.biomeClass)
      );
      Stage biomeStage = new BiomeStage("BiomeStage", biome_bufferType, worldStructure_workerData);
      generatorBuilder.appendStage(biomeStage);
      ParametrizedBufferType biomeDistance_bufferType = new ParametrizedBufferType(
         "BiomeDistance",
         bufferTypeIndexCounter++,
         BiomeDistanceStage.biomeDistanceBufferClass,
         BiomeDistanceStage.biomeDistanceClass,
         () -> new SimplePixelBuffer<>(BiomeDistanceStage.biomeDistanceClass)
      );
      int MAX_BIOME_DISTANCE_RADIUS = 512;
      int interpolationRadius = Math.clamp((long)(worldStructure_worker0.getBiomeTransitionDistance() / 2), 0, 512);
      int biomeEdgeRadius = Math.clamp((long)worldStructure_worker0.getMaxBiomeEdgeDistance(), 0, 512);
      int maxDistance = Math.max(interpolationRadius, biomeEdgeRadius);
      Stage biomeDistanceStage = new BiomeDistanceStage("BiomeDistanceStage", biome_bufferType, biomeDistance_bufferType, maxDistance);
      generatorBuilder.appendStage(biomeDistanceStage);
      int materialBufferIndexCounter = 0;
      ParametrizedBufferType material0_bufferType = generatorBuilder.MATERIAL_OUTPUT_BUFFER_TYPE;
      if (!allRuntimes.isEmpty()) {
         material0_bufferType = new ParametrizedBufferType(
            "Material" + materialBufferIndexCounter,
            bufferTypeIndexCounter++,
            TerrainStage.materialBufferClass,
            TerrainStage.materialClass,
            () -> new VoxelBuffer<>(TerrainStage.materialClass)
         );
         materialBufferIndexCounter++;
      }

      Stage terrainStage = new TerrainStage(
         "TerrainStage",
         biome_bufferType,
         biomeDistance_bufferType,
         material0_bufferType,
         interpolationRadius,
         materialCache,
         workerIndexer,
         worldStructure_workerData
      );
      generatorBuilder.appendStage(terrainStage);
      ParametrizedBufferType materialInput_bufferType = material0_bufferType;
      BufferType entityInput_bufferType = null;

      for (int i = 0; i < allRuntimes.size() - 1; i++) {
         int runtime = allRuntimes.get(i);
         String runtimeString = Integer.toString(runtime);
         ParametrizedBufferType materialOutput_bufferType = new ParametrizedBufferType(
            "Material" + materialBufferIndexCounter,
            bufferTypeIndexCounter++,
            TerrainStage.materialBufferClass,
            TerrainStage.materialClass,
            () -> new VoxelBuffer<>(TerrainStage.materialClass)
         );
         BufferType entityOutput_bufferType = new BufferType(
            "Entity" + materialBufferIndexCounter, bufferTypeIndexCounter++, EntityBuffer.class, EntityBuffer::new
         );
         Stage propStage = new PropStage(
            "PropStage" + runtimeString,
            biome_bufferType,
            biomeDistance_bufferType,
            materialInput_bufferType,
            entityInput_bufferType,
            materialOutput_bufferType,
            entityOutput_bufferType,
            materialCache,
            worldStructure_workerData,
            runtime
         );
         generatorBuilder.appendStage(propStage);
         materialInput_bufferType = materialOutput_bufferType;
         entityInput_bufferType = entityOutput_bufferType;
         materialBufferIndexCounter++;
      }

      if (!allRuntimes.isEmpty()) {
         int runtime = allRuntimes.getLast();
         String runtimeString = Integer.toString(runtime);
         Stage propStage = new PropStage(
            "PropStage" + runtimeString,
            biome_bufferType,
            biomeDistance_bufferType,
            materialInput_bufferType,
            entityInput_bufferType,
            generatorBuilder.MATERIAL_OUTPUT_BUFFER_TYPE,
            generatorBuilder.ENTITY_OUTPUT_BUFFER_TYPE,
            materialCache,
            worldStructure_workerData,
            runtime
         );
         generatorBuilder.appendStage(propStage);
      }

      Stage tintStage = new TintStage("TintStage", biome_bufferType, generatorBuilder.TINT_OUTPUT_BUFFER_TYPE, worldStructure_workerData);
      generatorBuilder.appendStage(tintStage);
      Stage environmentStage = new EnvironmentStage(
         "EnvironmentStage", biome_bufferType, generatorBuilder.ENVIRONMENT_OUTPUT_BUFFER_TYPE, worldStructure_workerData
      );
      generatorBuilder.appendStage(environmentStage);
      double bufferCapacityFactor = Math.max(0.0, settingsAsset.getBufferCapacityFactor());
      double targetViewDistance = Math.max(0.0, settingsAsset.getTargetViewDistance());
      double targetPlayerCount = Math.max(0.0, settingsAsset.getTargetPlayerCount());
      Set<Integer> statsCheckpoints = new HashSet<>(settingsAsset.getStatsCheckpoints());
      return generatorBuilder.withStats("WorldStructure Name: " + generatorProfile.worldStructureName(), statsCheckpoints)
         .withMaterialCache(materialCache)
         .withConcurrentExecutor(this.concurrentExecutor, workerIndexer)
         .withBufferCapacity(bufferCapacityFactor, targetViewDistance, targetPlayerCount)
         .withSpawnPositions(worldStructure_worker0.getSpawnPositions())
         .build();
   }

   @Nonnull
   private static Set<Integer> getAllPossibleRuntimeIndices(@Nonnull List<Biome> biomes) {
      Set<Integer> allRuntimes = new HashSet<>();

      for (Biome biome : biomes) {
         for (PropRuntime propRuntime : biome.getPropRuntimes()) {
            allRuntimes.add(propRuntime.getRuntimeIndex());
         }
      }

      return allRuntimes;
   }

   @Nonnull
   private ChunkGenerator getGenerator(@Nonnull ChunkRequest.GeneratorProfile profile) {
      ChunkGenerator generator = this.generators.get(profile);
      if (generator == null) {
         if (profile.worldStructureName() == null) {
            LoggerUtil.getLogger().warning("World Structure asset not loaded.");
            return FallbackGenerator.INSTANCE;
         }

         WorldStructureAsset worldStructureAsset = this.assetManager.getWorldStructureAsset(profile.worldStructureName());
         if (worldStructureAsset == null) {
            LoggerUtil.getLogger().warning("World Structure asset not found: " + profile.worldStructureName());
            return FallbackGenerator.INSTANCE;
         }

         SettingsAsset settingsAsset = this.assetManager.getSettingsAsset();
         if (settingsAsset == null) {
            LoggerUtil.getLogger().warning("Settings asset not found.");
            return FallbackGenerator.INSTANCE;
         }

         generator = this.createStagedChunkGenerator(profile, worldStructureAsset, settingsAsset);
         this.generators.put(profile, generator);
      }

      return generator;
   }

   private void loadExecutors(@Nonnull SettingsAsset settingsAsset) {
      int newConcurrency = getConcurrency(settingsAsset);
      if (newConcurrency != this.concurrency || this.mainExecutor == null || this.concurrentExecutor == null) {
         this.concurrency = newConcurrency;
         if (this.mainExecutor == null) {
            this.mainExecutor = Executors.newSingleThreadExecutor();
         }

         if (this.concurrentExecutor != null && !this.concurrentExecutor.isShutdown()) {
            try {
               this.concurrentExecutor.shutdown();
               if (!this.concurrentExecutor.awaitTermination(1L, TimeUnit.MINUTES)) {
               }
            } catch (InterruptedException var4) {
               throw new RuntimeException(var4);
            }
         }

         this.concurrentExecutor = new ThreadPoolExecutor(this.concurrency, this.concurrency, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), r -> {
            Thread t = new Thread(r, "HytaleGenerator-Worker");
            t.setPriority(1);
            t.setDaemon(true);
            return t;
         });
         if (this.mainExecutor == null || this.mainExecutor.isShutdown()) {
            this.mainExecutor = Executors.newSingleThreadExecutor();
         }
      }
   }

   private static int getConcurrency(@Nonnull SettingsAsset settingsAsset) {
      int concurrencySetting = settingsAsset.getCustomConcurrency();
      int availableProcessors = Runtime.getRuntime().availableProcessors();
      int value = 1;
      if (concurrencySetting < 1) {
         value = Math.max(availableProcessors, 1);
      } else {
         if (concurrencySetting > availableProcessors) {
            LoggerUtil.getLogger().warning("Concurrency setting " + concurrencySetting + " exceeds available processors " + availableProcessors);
         }

         value = concurrencySetting;
      }

      return value;
   }

   private void reloadGenerators() {
      try {
         this.chunkGenerationSemaphore.acquireUninterruptibly();
         this.loadExecutors(this.assetManager.getSettingsAsset());
         this.generators.clear();
      } finally {
         this.chunkGenerationSemaphore.release();
      }

      LoggerUtil.getLogger().info("Reloaded HytaleGenerator.");
   }

   public HytaleGenerator(@Nonnull JavaPluginInit init) {
      super(init);
   }
}
