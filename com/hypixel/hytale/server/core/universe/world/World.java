package com.hypixel.hytale.server.core.universe.world;

import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.common.util.FormatUtil;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.IResourceStorage;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.data.unknown.UnknownComponents;
import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.event.IEventDispatcher;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.logger.sentry.SkipSentryException;
import com.hypixel.hytale.math.block.BlockUtil;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.metrics.ExecutorMetricsRegistry;
import com.hypixel.hytale.metrics.metric.HistoricMetric;
import com.hypixel.hytale.protocol.packets.entities.SetEntitySeed;
import com.hypixel.hytale.protocol.packets.player.JoinWorld;
import com.hypixel.hytale.protocol.packets.player.SetClientId;
import com.hypixel.hytale.protocol.packets.setup.ClientFeature;
import com.hypixel.hytale.protocol.packets.setup.SetTimeDilation;
import com.hypixel.hytale.protocol.packets.setup.SetUpdateRate;
import com.hypixel.hytale.protocol.packets.setup.UpdateFeatures;
import com.hypixel.hytale.protocol.packets.setup.ViewRadius;
import com.hypixel.hytale.protocol.packets.world.ServerSetPaused;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.ShutdownReason;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.gameplay.CombatConfig;
import com.hypixel.hytale.server.core.asset.type.gameplay.DeathConfig;
import com.hypixel.hytale.server.core.asset.type.gameplay.GameplayConfig;
import com.hypixel.hytale.server.core.asset.type.gameplay.PlayerConfig;
import com.hypixel.hytale.server.core.blocktype.component.BlockPhysics;
import com.hypixel.hytale.server.core.console.ConsoleModule;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.entity.EntityUtils;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.data.PlayerConfigData;
import com.hypixel.hytale.server.core.event.events.player.AddPlayerToWorldEvent;
import com.hypixel.hytale.server.core.event.events.player.DrainPlayerFromWorldEvent;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.player.ChunkTracker;
import com.hypixel.hytale.server.core.modules.entity.tracker.LegacyEntityTrackerSystems;
import com.hypixel.hytale.server.core.modules.time.TimeResource;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.prefab.selection.buffer.impl.IPrefabBuffer;
import com.hypixel.hytale.server.core.receiver.IMessageReceiver;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.accessor.ChunkAccessor;
import com.hypixel.hytale.server.core.universe.world.chunk.ChunkColumn;
import com.hypixel.hytale.server.core.universe.world.chunk.ChunkFlag;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.ChunkSection;
import com.hypixel.hytale.server.core.universe.world.events.StartWorldEvent;
import com.hypixel.hytale.server.core.universe.world.lighting.ChunkLightingManager;
import com.hypixel.hytale.server.core.universe.world.path.WorldPathConfig;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.universe.world.storage.resources.DiskResourceStorageProvider;
import com.hypixel.hytale.server.core.universe.world.worldgen.IWorldGen;
import com.hypixel.hytale.server.core.universe.world.worldgen.WorldGenLoadException;
import com.hypixel.hytale.server.core.universe.world.worldmap.IWorldMap;
import com.hypixel.hytale.server.core.universe.world.worldmap.WorldMapLoadException;
import com.hypixel.hytale.server.core.universe.world.worldmap.WorldMapManager;
import com.hypixel.hytale.server.core.util.FillerBlockUtil;
import com.hypixel.hytale.server.core.util.MessageUtil;
import com.hypixel.hytale.server.core.util.io.FileUtil;
import com.hypixel.hytale.server.core.util.thread.TickingThread;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.awt.Color;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntUnaryOperator;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class World extends TickingThread implements Executor, ExecutorMetricsRegistry.ExecutorMetric, ChunkAccessor<WorldChunk>, IWorldChunks, IMessageReceiver {
   public static final float SAVE_INTERVAL = 10.0F;
   public static final String DEFAULT = "default";
   @Nonnull
   public static final ExecutorMetricsRegistry<World> METRICS_REGISTRY = new ExecutorMetricsRegistry<World>()
      .register("Name", world -> world.name, Codec.STRING)
      .register("Alive", world -> world.alive.get(), Codec.BOOLEAN)
      .register("TickLength", TickingThread::getBufferedTickLengthMetricSet, HistoricMetric.METRICS_CODEC)
      .register("EntityStore", World::getEntityStore, EntityStore.METRICS_REGISTRY)
      .register("ChunkStore", World::getChunkStore, ChunkStore.METRICS_REGISTRY);
   @Nonnull
   private final HytaleLogger logger;
   @Nonnull
   private final String name;
   @Nonnull
   private final Path savePath;
   @Nonnull
   private final WorldConfig worldConfig;
   @Nonnull
   private final ChunkStore chunkStore = new ChunkStore(this);
   @Nonnull
   private final EntityStore entityStore = new EntityStore(this);
   @Nonnull
   private final ChunkLightingManager chunkLighting;
   @Nonnull
   private final WorldMapManager worldMapManager;
   private WorldPathConfig worldPathConfig;
   private final AtomicBoolean acceptingTasks = new AtomicBoolean(true);
   @Nonnull
   private final Deque<Runnable> taskQueue = new LinkedBlockingDeque<>();
   @Nonnull
   private final AtomicBoolean alive = new AtomicBoolean(true);
   @Nonnull
   private final EventRegistry eventRegistry = new EventRegistry(new CopyOnWriteArrayList<>(), () -> true, null, HytaleServer.get().getEventBus());
   @Nonnull
   private final WorldNotificationHandler notificationHandler = new WorldNotificationHandler(this);
   private boolean isTicking;
   private boolean isPaused;
   private long tick;
   @Nonnull
   private final Random random = new Random();
   @Nonnull
   private final AtomicInteger entitySeed = new AtomicInteger();
   @Nonnull
   private final Map<UUID, PlayerRef> players = new ConcurrentHashMap<>();
   @Nonnull
   private final Collection<PlayerRef> playerRefs = Collections.unmodifiableCollection(this.players.values());
   @Nonnull
   private final Map<ClientFeature, Boolean> features = Collections.synchronizedMap(new EnumMap<>(ClientFeature.class));
   private volatile boolean gcHasRun;

   public World(@Nonnull String name, @Nonnull Path savePath, @Nonnull WorldConfig worldConfig) throws IOException {
      super("WorldThread - " + name);
      this.name = name;
      this.logger = HytaleLogger.get("World|" + name);
      this.savePath = savePath;
      this.worldConfig = worldConfig;
      this.logger
         .at(Level.INFO)
         .log(
            "Loading world '%s' with generator type: '%s' and chunk storage: '%s'...",
            name,
            worldConfig.getWorldGenProvider(),
            worldConfig.getChunkStorageProvider()
         );
      this.worldMapManager = new WorldMapManager(this);
      this.chunkLighting = new ChunkLightingManager(this);
      this.isTicking = worldConfig.isTicking();

      for (ClientFeature feature : ClientFeature.VALUES) {
         this.features.put(feature, true);
      }

      GameplayConfig gameplayConfig = this.getGameplayConfig();
      CombatConfig combatConfig = gameplayConfig.getCombatConfig();
      this.features.put(ClientFeature.DisplayHealthBars, combatConfig.isDisplayHealthBars());
      this.features.put(ClientFeature.DisplayCombatText, combatConfig.isDisplayCombatText());
      PlayerConfig.ArmorVisibilityOption armorVisibilityOption = gameplayConfig.getPlayerConfig().getArmorVisibilityOption();
      this.features.put(ClientFeature.CanHideHelmet, armorVisibilityOption.canHideHelmet());
      this.features.put(ClientFeature.CanHideCuirass, armorVisibilityOption.canHideCuirass());
      this.features.put(ClientFeature.CanHideGauntlets, armorVisibilityOption.canHideGauntlets());
      this.features.put(ClientFeature.CanHidePants, armorVisibilityOption.canHidePants());
      this.logger.at(Level.INFO).log("Added world '%s' - Seed: %s, GameTime: %s", name, Long.toString(worldConfig.getSeed()), worldConfig.getGameTime());
   }

   @Nonnull
   public CompletableFuture<World> init() {
      CompletableFuture<Void> savingFuture;
      if (this.worldConfig.isSavingConfig()) {
         savingFuture = Universe.get().getWorldConfigProvider().save(this.savePath, this.worldConfig, this);
      } else {
         savingFuture = CompletableFuture.completedFuture(null);
      }

      CompletableFuture<World> loadWorldGen = CompletableFuture.supplyAsync(() -> {
         try {
            IWorldGen worldGen = this.worldConfig.getWorldGenProvider().getGenerator();
            this.chunkStore.setGenerator(worldGen);
            this.worldConfig.setDefaultSpawnProvider(worldGen);
            IWorldMap worldMap = this.worldConfig.getWorldMapProvider().getGenerator(this);
            this.worldMapManager.setGenerator(worldMap);
            return this;
         } catch (WorldGenLoadException var3x) {
            if (this.name.equals(HytaleServer.get().getConfig().getDefaults().getWorld())) {
               HytaleServer.get().shutdownServer(ShutdownReason.WORLD_GEN.withMessage(var3x.getTraceMessage("\n")));
            }

            throw new SkipSentryException("Failed to load WorldGen!", var3x);
         } catch (WorldMapLoadException var4) {
            if (this.name.equals(HytaleServer.get().getConfig().getDefaults().getWorld())) {
               HytaleServer.get().shutdownServer(ShutdownReason.WORLD_GEN.withMessage(var4.getTraceMessage("\n")));
            }

            throw new SkipSentryException("Failed to load WorldGen!", var4);
         }
      });
      CompletableFuture<Void> loadPaths = WorldPathConfig.load(this).thenAccept(config -> this.worldPathConfig = config);
      return this.worldConfig.getSpawnProvider() != null
         ? CompletableFuture.allOf(savingFuture, loadPaths).thenApply(v -> this)
         : CompletableFuture.allOf(savingFuture, loadPaths).thenCompose(v -> loadWorldGen);
   }

   @Override
   protected void onStart() {
      DiskResourceStorageProvider.migrateFiles(this);
      IResourceStorage resourceStorage = this.worldConfig.getResourceStorageProvider().getResourceStorage(this);
      this.chunkStore.start(resourceStorage);
      this.entityStore.start(resourceStorage);
      this.chunkLighting.start();
      this.worldMapManager.updateTickingState(this.worldMapManager.isStarted());
      Path rffPath = this.savePath.resolve("rff");
      if (Files.exists(rffPath)) {
         throw new RuntimeException(rffPath + " directory exists but this version of the server doesn't support migrating RFF worlds!");
      } else {
         IEventDispatcher<StartWorldEvent, StartWorldEvent> dispatcher = HytaleServer.get().getEventBus().dispatchFor(StartWorldEvent.class, this.name);
         if (dispatcher.hasListener()) {
            dispatcher.dispatch(new StartWorldEvent(this));
         }
      }
   }

   public void stopIndividualWorld() {
      this.stopIndividualWorld(this.players);
   }

   public void stopIndividualWorld(Map<UUID, PlayerRef> players) {
      this.logger.at(Level.INFO).log("Removing individual world: %s", this.name);
      World defaultWorld = Universe.get().getDefaultWorld();
      if (defaultWorld != null && !defaultWorld.equals(this)) {
         Message message;
         if (this.getFailureException() == null) {
            message = Message.translation("server.universe.worldRemoved");
         } else if (this.getPossibleFailureCause() == null) {
            message = Message.translation("server.universe.worldCrash.unknown");
         } else {
            message = Message.translation("server.universe.worldCrash.mod").param("mod", this.getPossibleFailureCause().toString());
         }

         message.color(Color.RED);

         for (PlayerRef playerRef : players.values()) {
            playerRef.sendMessage(message);
         }

         if (this.isInThread()) {
            this.drainPlayersTo(defaultWorld, players.values()).join();
         } else {
            CompletableFuture.<CompletableFuture<Void>>supplyAsync(() -> this.drainPlayersTo(defaultWorld, players.values()), this)
               .thenCompose(v -> (CompletionStage<Void>)v)
               .join();
         }
      } else {
         String messagex;
         if (this.getFailureException() == null) {
            messagex = "The world you were on was removed";
         } else if (this.getPossibleFailureCause() == null) {
            messagex = "The world you were on has crashed";
         } else {
            messagex = "The world you were on has crashed (possibly caused by " + this.getPossibleFailureCause() + ")";
         }

         for (PlayerRef playerRef : players.values()) {
            playerRef.getPacketHandler().disconnect(messagex);
         }
      }

      if (this.alive.getAndSet(false)) {
         try {
            super.stop();
         } catch (Throwable var6) {
            ((HytaleLogger.Api)this.logger.at(Level.SEVERE).withCause(var6)).log("Exception while shutting down world:");
         }
      }
   }

   public void validateDeleteOnRemove() {
      if (this.worldConfig.isDeleteOnRemove()) {
         try {
            this.deleteWorldFromDisk();
         } catch (Throwable var2) {
            ((HytaleLogger.Api)this.logger.at(Level.SEVERE).withCause(var2)).log("Exception while deleting world on remove:");
         }
      }
   }

   private void deleteWorldFromDisk() throws IOException {
      Path originDir = this.getSavePath();
      Path filename = originDir.getFileName();
      String noCollisionsName = filename + "_del" + UUID.randomUUID().toString().substring(0, 8);
      Path deletionDir = Universe.get().getWorldsDeletedPath().resolve(noCollisionsName);
      Files.createDirectories(deletionDir.getParent());
      FileUtil.atomicMove(originDir, deletionDir);
      FileUtil.deleteDirectory(deletionDir);
   }

   @Override
   protected boolean isIdle() {
      return this.players.isEmpty();
   }

   @Override
   protected void tick(float dt) {
      if (this.alive.get()) {
         TimeResource worldTimeResource = this.entityStore.getStore().getResource(TimeResource.getResourceType());
         dt *= worldTimeResource.getTimeDilationModifier();
         AssetRegistry.ASSET_LOCK.readLock().lock();

         try {
            this.consumeTaskQueue();
            if (!this.isPaused) {
               this.entityStore.getStore().tick(dt);
            } else {
               this.entityStore.getStore().pausedTick(dt);
            }

            if (this.isTicking && !this.isPaused) {
               this.chunkStore.getStore().tick(dt);
            } else {
               this.chunkStore.getStore().pausedTick(dt);
            }

            this.consumeTaskQueue();
         } finally {
            AssetRegistry.ASSET_LOCK.readLock().unlock();
         }

         this.tick++;
      }
   }

   @Override
   protected void onShutdown() {
      this.logger.at(Level.INFO).log("Stopping world %s...", this.name);
      this.logger.at(Level.INFO).log("Stopping background threads...");
      long start = System.nanoTime();

      while (this.chunkLighting.interrupt() || this.worldMapManager.interrupt()) {
         this.consumeTaskQueue();
         if (System.nanoTime() - start > 5000000000L) {
            break;
         }
      }

      this.chunkLighting.stop();
      this.worldMapManager.stop();
      this.logger.at(Level.INFO).log("Removing players...");
      Object2ObjectOpenHashMap<UUID, PlayerRef> currentPlayers = new Object2ObjectOpenHashMap(this.players);

      for (PlayerRef playerRef : this.playerRefs) {
         if (playerRef.getReference() != null) {
            playerRef.removeFromStore();
         }
      }

      this.consumeTaskQueue();
      this.logger.at(Level.INFO).log("Waiting for loading chunks...");
      this.chunkStore.waitForLoadingChunks();

      try {
         this.logger.at(Level.INFO).log("Shutting down stores...");
         HytaleServer.get().reportSingleplayerStatus("Saving world '" + this.name + "'");
         this.chunkStore.shutdown();
         this.consumeTaskQueue();
         this.entityStore.shutdown();
         this.consumeTaskQueue();
         this.eventRegistry.shutdownAndCleanup(true);
      } finally {
         this.logger.at(Level.INFO).log("Saving Config...");
         if (this.worldConfig.isSavingConfig()) {
            Universe.get().getWorldConfigProvider().save(this.savePath, this.worldConfig, this).join();
         }
      }

      this.acceptingTasks.set(false);
      if (this.alive.getAndSet(false)) {
         this.stopIndividualWorld(currentPlayers);
         Universe.get().removeWorldExceptionally(this.name, currentPlayers);
      }

      HytaleServer.get().reportSingleplayerStatus("Closing world '" + this.name + "'");
   }

   @Override
   public void setTps(int tps) {
      super.setTps(tps);
      SetUpdateRate setUpdateRatePacket = new SetUpdateRate(tps);
      this.entityStore
         .getStore()
         .forEachEntityParallel(
            PlayerRef.getComponentType(),
            (index, archetypeChunk, commandBuffer) -> archetypeChunk.getComponent(index, PlayerRef.getComponentType())
               .getPacketHandler()
               .writeNoCache(setUpdateRatePacket)
         );
   }

   public static void setTimeDilation(float timeDilationModifier, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      World world = componentAccessor.getExternalData().getWorld();
      if (!(timeDilationModifier <= 0.01) && !(timeDilationModifier > 4.0F)) {
         TimeResource worldTimeResource = componentAccessor.getResource(TimeResource.getResourceType());
         worldTimeResource.setTimeDilationModifier(timeDilationModifier);
         SetTimeDilation setTimeDilationPacket = new SetTimeDilation(timeDilationModifier);

         for (PlayerRef playerRef : world.playerRefs) {
            playerRef.getPacketHandler().writeNoCache(setTimeDilationPacket);
         }
      } else {
         throw new IllegalArgumentException("TimeDilation is out of bounds (<=0.01 or >4)");
      }
   }

   @Nonnull
   public String getName() {
      return this.name;
   }

   public boolean isAlive() {
      return this.alive.get();
   }

   @Nonnull
   public WorldConfig getWorldConfig() {
      return this.worldConfig;
   }

   @Nonnull
   public DeathConfig getDeathConfig() {
      DeathConfig override = this.worldConfig.getDeathConfigOverride();
      return override != null ? override : this.getGameplayConfig().getDeathConfig();
   }

   public int getDaytimeDurationSeconds() {
      Integer override = this.worldConfig.getDaytimeDurationSecondsOverride();
      return override != null ? override : this.getGameplayConfig().getWorldConfig().getDaytimeDurationSeconds();
   }

   public int getNighttimeDurationSeconds() {
      Integer override = this.worldConfig.getNighttimeDurationSecondsOverride();
      return override != null ? override : this.getGameplayConfig().getWorldConfig().getNighttimeDurationSeconds();
   }

   public boolean isTicking() {
      return this.isTicking;
   }

   public void setTicking(boolean ticking) {
      this.isTicking = ticking;
      this.worldConfig.setTicking(ticking);
      this.worldConfig.markChanged();
   }

   public boolean isPaused() {
      return this.isPaused;
   }

   public void setPaused(boolean paused) {
      if (this.isPaused != paused) {
         this.isPaused = paused;
         ServerSetPaused setPaused = new ServerSetPaused(paused);
         PlayerUtil.broadcastPacketToPlayersNoCache(this.entityStore.getStore(), setPaused);
      }
   }

   public long getTick() {
      return this.tick;
   }

   @Nonnull
   public HytaleLogger getLogger() {
      return this.logger;
   }

   public boolean isCompassUpdating() {
      return this.worldConfig.isCompassUpdating();
   }

   public void setCompassUpdating(boolean compassUpdating) {
      boolean before = this.worldMapManager.shouldTick();
      this.worldConfig.setCompassUpdating(compassUpdating);
      this.worldConfig.markChanged();
      this.worldMapManager.updateTickingState(before);
   }

   public <T> void getBlockBulkRelative(
      @Nonnull Long2ObjectMap<T> blocks,
      @Nonnull IntUnaryOperator xConvert,
      @Nonnull IntUnaryOperator yConvert,
      @Nonnull IntUnaryOperator zConvert,
      @Nonnull World.GenericBlockBulkUpdater<T> consumer
   ) {
      Long2ObjectMap<WorldChunk> chunks = new Long2ObjectOpenHashMap();
      blocks.forEach((a, b) -> {
         int localX = BlockUtil.unpackX(a);
         int localY = BlockUtil.unpackY(a);
         int localZ = BlockUtil.unpackZ(a);
         int x = xConvert.applyAsInt(localX);
         int y = yConvert.applyAsInt(localY);
         int z = zConvert.applyAsInt(localZ);
         long chunkIndex = ChunkUtil.indexChunkFromBlock(x, z);
         WorldChunk chunk = (WorldChunk)chunks.get(chunkIndex);
         if (chunk == null) {
            chunk = this.getNonTickingChunk(chunkIndex);
            chunks.put(chunkIndex, chunk);
         }

         consumer.apply(this, (T)b, chunkIndex, chunk, x, y, z, localX, localY, localZ);
      });
   }

   @Nullable
   public WorldChunk loadChunkIfInMemory(long index) {
      if (!this.isInThread()) {
         return CompletableFuture.<WorldChunk>supplyAsync(() -> this.loadChunkIfInMemory(index), this).join();
      } else {
         Ref<ChunkStore> reference = this.chunkStore.getChunkReference(index);
         if (reference == null) {
            return null;
         } else {
            WorldChunk worldChunkComponent = this.chunkStore.getStore().getComponent(reference, WorldChunk.getComponentType());

            assert worldChunkComponent != null;

            worldChunkComponent.setFlag(ChunkFlag.TICKING, true);
            return worldChunkComponent;
         }
      }
   }

   @Nullable
   public WorldChunk getChunkIfInMemory(long index) {
      Ref<ChunkStore> reference = this.chunkStore.getChunkReference(index);
      if (reference == null) {
         return null;
      } else {
         return !this.isInThread()
            ? CompletableFuture.<WorldChunk>supplyAsync(() -> this.getChunkIfInMemory(index), this).join()
            : this.chunkStore.getStore().getComponent(reference, WorldChunk.getComponentType());
      }
   }

   @Nullable
   public WorldChunk getChunkIfLoaded(long index) {
      if (!this.isInThread()) {
         return CompletableFuture.<WorldChunk>supplyAsync(() -> this.getChunkIfLoaded(index), this).join();
      } else {
         Ref<ChunkStore> reference = this.chunkStore.getChunkReference(index);
         if (reference == null) {
            return null;
         } else {
            WorldChunk worldChunkComponent = this.chunkStore.getStore().getComponent(reference, WorldChunk.getComponentType());

            assert worldChunkComponent != null;

            return worldChunkComponent.is(ChunkFlag.TICKING) ? worldChunkComponent : null;
         }
      }
   }

   @Nullable
   public WorldChunk getChunkIfNonTicking(long index) {
      if (!this.isInThread()) {
         return CompletableFuture.<WorldChunk>supplyAsync(() -> this.getChunkIfNonTicking(index), this).join();
      } else {
         Ref<ChunkStore> reference = this.chunkStore.getChunkReference(index);
         if (reference == null) {
            return null;
         } else {
            WorldChunk worldChunkComponent = this.chunkStore.getStore().getComponent(reference, WorldChunk.getComponentType());

            assert worldChunkComponent != null;

            return worldChunkComponent.is(ChunkFlag.TICKING) ? null : worldChunkComponent;
         }
      }
   }

   @Nonnull
   @Override
   public CompletableFuture<WorldChunk> getChunkAsync(long index) {
      return this.chunkStore
         .getChunkReferenceAsync(index, 4)
         .thenApplyAsync(
            reference -> reference == null ? null : this.chunkStore.getStore().getComponent((Ref<ChunkStore>)reference, WorldChunk.getComponentType()), this
         );
   }

   @Nonnull
   @Override
   public CompletableFuture<WorldChunk> getNonTickingChunkAsync(long index) {
      return this.chunkStore
         .getChunkReferenceAsync(index)
         .thenApplyAsync(
            reference -> reference == null ? null : this.chunkStore.getStore().getComponent((Ref<ChunkStore>)reference, WorldChunk.getComponentType()), this
         );
   }

   @Deprecated(forRemoval = true)
   public List<Player> getPlayers() {
      if (!this.isInThread()) {
         return !this.isStarted() ? Collections.emptyList() : CompletableFuture.supplyAsync(this::getPlayers, this).join();
      } else {
         ObjectArrayList<Player> players = new ObjectArrayList(32);
         this.entityStore.getStore().forEachChunk(Player.getComponentType(), (archetypeChunk, commandBuffer) -> {
            players.ensureCapacity(players.size() + archetypeChunk.size());

            for (int index = 0; index < archetypeChunk.size(); index++) {
               players.add(archetypeChunk.getComponent(index, Player.getComponentType()));
            }
         });
         return players;
      }
   }

   @Nullable
   @Deprecated
   public Entity getEntity(@Nonnull UUID uuid) {
      if (!this.isInThread()) {
         return CompletableFuture.<Entity>supplyAsync(() -> this.getEntity(uuid), this).join();
      } else {
         Ref<EntityStore> ref = this.entityStore.getRefFromUUID(uuid);
         return EntityUtils.getEntity(ref, this.entityStore.getStore());
      }
   }

   @Nullable
   public Ref<EntityStore> getEntityRef(@Nonnull UUID uuid) {
      return !this.isInThread()
         ? CompletableFuture.<Ref<EntityStore>>supplyAsync(() -> this.getEntityRef(uuid), this).join()
         : this.entityStore.getRefFromUUID(uuid);
   }

   public int getPlayerCount() {
      return this.players.size();
   }

   @Nonnull
   public Collection<PlayerRef> getPlayerRefs() {
      return this.playerRefs;
   }

   public void trackPlayerRef(@Nonnull PlayerRef playerRef) {
      this.players.put(playerRef.getUuid(), playerRef);
   }

   public void untrackPlayerRef(@Nonnull PlayerRef playerRef) {
      this.players.remove(playerRef.getUuid(), playerRef);
   }

   @Deprecated
   @Nullable
   public <T extends Entity> T spawnEntity(T entity, @Nonnull Vector3d position, Vector3f rotation) {
      return this.addEntity(entity, position, rotation, AddReason.SPAWN);
   }

   @Deprecated
   @Nullable
   public <T extends Entity> T addEntity(T entity, @Nonnull Vector3d position, @Nullable Vector3f rotation, @Nonnull AddReason reason) {
      if (!EntityModule.get().isKnown(entity)) {
         throw new IllegalArgumentException("Unknown entity");
      } else if (entity instanceof Player) {
         throw new IllegalArgumentException("Entity can't be a Player!");
      } else if (entity.getNetworkId() == -1) {
         throw new IllegalArgumentException("Entity id can't be Entity.UNASSIGNED_ID (-1)!");
      } else if (!this.equals(entity.getWorld())) {
         throw new IllegalStateException("Expected entity to already have its world set to " + this.getName() + " but it has " + entity.getWorld());
      } else if (entity.getReference() != null && entity.getReference().isValid()) {
         throw new IllegalArgumentException("Entity already has a valid EntityReference: " + entity.getReference());
      } else if (position.getY() < -32.0) {
         throw new IllegalArgumentException("Unable to spawn entity below the world! -32 < " + position);
      } else if (!this.isInThread()) {
         ((HytaleLogger.Api)this.logger.at(Level.WARNING).withCause(new SkipSentryException())).log("Warning addEntity was called off thread!");
         this.execute(() -> this.addEntity(entity, position, rotation, reason));
         return entity;
      } else {
         entity.unloadFromWorld();
         Holder<EntityStore> holder = entity.toHolder();
         HeadRotation headRotation = holder.ensureAndGetComponent(HeadRotation.getComponentType());
         if (rotation != null) {
            headRotation.teleportRotation(rotation);
         }

         holder.addComponent(TransformComponent.getComponentType(), new TransformComponent(position, rotation));
         holder.ensureComponent(UUIDComponent.getComponentType());
         this.entityStore.getStore().addEntity(holder, reason);
         return entity;
      }
   }

   @Override
   public void sendMessage(@Nonnull Message message) {
      if (!this.isInThread()) {
         this.execute(() -> this.sendMessage(message));
      } else {
         this.entityStore.getStore().forEachEntityParallel(PlayerRef.getComponentType(), (index, archetypeChunk, commandBuffer) -> {
            PlayerRef playerRefComponent = archetypeChunk.getComponent(index, PlayerRef.getComponentType());

            assert playerRefComponent != null;

            playerRefComponent.sendMessage(message);
         });
         this.logger.at(Level.INFO).log("[Broadcast] [Message] %s", MessageUtil.toAnsiString(message).toAnsi(ConsoleModule.get().getTerminal()));
      }
   }

   @Override
   public void execute(@Nonnull Runnable command) {
      if (!this.acceptingTasks.get()) {
         throw new SkipSentryException(new IllegalThreadStateException("World thread is not accepting tasks: " + this.name + ", " + this.getThread()));
      } else {
         this.taskQueue.offer(command);
      }
   }

   @Override
   public void consumeTaskQueue() {
      this.debugAssertInTickingThread();
      int tickStepNanos = this.getTickStepNanos();

      Runnable runnable;
      while ((runnable = this.taskQueue.poll()) != null) {
         try {
            long before = System.nanoTime();
            runnable.run();
            long after = System.nanoTime();
            long diff = after - before;
            if (diff > tickStepNanos) {
               this.logger.at(Level.WARNING).log("Task took %s ns: %s", FormatUtil.nanosToString(diff), runnable);
            }
         } catch (Exception var9) {
            ((HytaleLogger.Api)this.logger.at(Level.SEVERE).withCause(var9)).log("Failed to run task!");
         }
      }
   }

   @Nonnull
   public ChunkStore getChunkStore() {
      return this.chunkStore;
   }

   @Nonnull
   public EntityStore getEntityStore() {
      return this.entityStore;
   }

   @Nonnull
   public ChunkLightingManager getChunkLighting() {
      return this.chunkLighting;
   }

   @Nonnull
   public WorldMapManager getWorldMapManager() {
      return this.worldMapManager;
   }

   public WorldPathConfig getWorldPathConfig() {
      return this.worldPathConfig;
   }

   @Nonnull
   public WorldNotificationHandler getNotificationHandler() {
      return this.notificationHandler;
   }

   @Nonnull
   public EventRegistry getEventRegistry() {
      return this.eventRegistry;
   }

   @Nullable
   public CompletableFuture<PlayerRef> addPlayer(@Nonnull PlayerRef playerRef) {
      return this.addPlayer(playerRef, null);
   }

   @Nullable
   public CompletableFuture<PlayerRef> addPlayer(@Nonnull PlayerRef playerRef, @Nullable Transform transform) {
      return this.addPlayer(playerRef, transform, null, null);
   }

   @Nullable
   public CompletableFuture<PlayerRef> addPlayer(
      @Nonnull PlayerRef playerRef,
      @Deprecated(forRemoval = true) @Nullable Transform transform,
      @Nullable Boolean clearWorldOverride,
      @Nullable Boolean fadeInOutOverride
   ) {
      if (!this.alive.get()) {
         return CompletableFuture.failedFuture(new IllegalStateException("This world has already been shutdown!"));
      } else if (playerRef.getReference() != null) {
         throw new IllegalStateException("Player is already in a world");
      } else {
         PacketHandler packetHandler = playerRef.getPacketHandler();
         if (!packetHandler.stillActive()) {
            return null;
         } else {
            Holder<EntityStore> holder = playerRef.getHolder();

            assert holder != null;

            TransformComponent transformComponent = holder.getComponent(TransformComponent.getComponentType());
            if (transformComponent == null && transform == null) {
               transformComponent = SpawnUtil.applyFirstSpawnTransform(holder, this, this.worldConfig, playerRef.getUuid());
               if (transformComponent == null) {
                  return CompletableFuture.failedFuture(new IllegalStateException("Spawn provider cannot be null for positioning new entities!"));
               }
            }

            assert transformComponent != null;

            Player playerComponent = holder.getComponent(Player.getComponentType());

            assert playerComponent != null;

            boolean firstSpawn = !playerComponent.getPlayerConfigData().getPerWorldData().containsKey(this.name);
            playerComponent.setFirstSpawn(firstSpawn);
            if (transform != null) {
               SpawnUtil.applyTransform(holder, transform);
            }

            AddPlayerToWorldEvent event = HytaleServer.get()
               .getEventBus()
               .dispatchFor(AddPlayerToWorldEvent.class, this.name)
               .dispatch(new AddPlayerToWorldEvent(holder, this));
            ChunkTracker chunkTrackerComponent = holder.getComponent(ChunkTracker.getComponentType());
            boolean clearWorld = clearWorldOverride != null ? clearWorldOverride : true;
            boolean fadeInOut = fadeInOutOverride != null ? fadeInOutOverride : true;
            if (chunkTrackerComponent != null && (clearWorld || fadeInOut)) {
               chunkTrackerComponent.setReadyForChunks(false);
            }

            Vector3d spawnPosition = transformComponent.getPosition();
            long chunkIndex = ChunkUtil.indexChunkFromBlock(spawnPosition.getX(), spawnPosition.getZ());
            CompletableFuture<Void> loadTargetChunkFuture = this.chunkStore
               .getChunkReferenceAsync(chunkIndex)
               .thenAccept(v -> playerComponent.startClientReadyTimeout());
            CompletableFuture<Void> clientReadyFuture = new CompletableFuture<>();
            packetHandler.setClientReadyForChunksFuture(clientReadyFuture);
            CompletableFuture<Void> setupPlayerFuture = CompletableFuture.runAsync(
               () -> this.onSetupPlayerJoining(holder, playerComponent, playerRef, packetHandler, transform, clearWorld, fadeInOut)
            );
            CompletableFuture<Void> playerReadyFuture = clientReadyFuture.orTimeout(30L, TimeUnit.SECONDS);
            return CompletableFuture.allOf(setupPlayerFuture, playerReadyFuture, loadTargetChunkFuture)
               .thenApplyAsync(aVoid -> this.onFinishPlayerJoining(playerComponent, playerRef, packetHandler, event.shouldBroadcastJoinMessage()), this)
               .exceptionally(throwable -> {
                  ((HytaleLogger.Api)this.logger.at(Level.WARNING).withCause(throwable)).log("Exception when adding player to world!");
                  playerRef.getPacketHandler().disconnect("Exception when adding player to world!");
                  throw new RuntimeException("Exception when adding player '" + playerRef.getUsername() + "' to world '" + this.name + "'", throwable);
               });
         }
      }
   }

   @Nonnull
   private PlayerRef onFinishPlayerJoining(
      @Nonnull Player playerComponent, @Nonnull PlayerRef playerRefComponent, @Nonnull PacketHandler packetHandler, boolean broadcastJoin
   ) {
      TimeResource timeResource = this.entityStore.getStore().getResource(TimeResource.getResourceType());
      float timeDilationModifier = timeResource.getTimeDilationModifier();
      int maxViewRadius = HytaleServer.get().getConfig().getMaxViewRadius();
      packetHandler.write(
         new ViewRadius(maxViewRadius * 32),
         new SetEntitySeed(this.entitySeed.get()),
         new SetClientId(playerComponent.getNetworkId()),
         new SetTimeDilation(timeDilationModifier)
      );
      packetHandler.write(new UpdateFeatures(this.features));
      packetHandler.write(this.worldConfig.getClientEffects().createSunSettingsPacket());
      packetHandler.write(this.worldConfig.getClientEffects().createPostFxSettingsPacket());
      UUID playerUuid = playerRefComponent.getUuid();
      Store<EntityStore> store = this.entityStore.getStore();
      WorldTimeResource worldTimeResource = store.getResource(WorldTimeResource.getResourceType());
      World world = store.getExternalData().getWorld();
      packetHandler.writeNoCache(new SetUpdateRate(this.getTps()));
      if (this.isPaused) {
         this.setPaused(false);
      }

      Ref<EntityStore> ref = playerRefComponent.addToStore(store);
      if (ref != null && ref.isValid()) {
         worldTimeResource.sendTimePackets(playerRefComponent);
         WorldMapTracker worldMapTracker = playerComponent.getWorldMapTracker();
         worldMapTracker.clear();
         worldMapTracker.sendSettings(world);
         if (broadcastJoin) {
            Message message = Message.translation("server.general.playerJoinedWorld")
               .param("username", playerRefComponent.getUsername())
               .param("world", this.worldConfig.getDisplayName() != null ? this.worldConfig.getDisplayName() : WorldConfig.formatDisplayName(this.name));
            PlayerUtil.broadcastMessageToPlayers(playerUuid, message, store);
         }

         TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());

         assert transformComponent != null;

         String position = transformComponent.getPosition().toString();
         this.logger.at(Level.INFO).log("Player '%s' joined world '%s' at location %s (%s)", playerRefComponent.getUsername(), this.name, position, playerUuid);
         HeadRotation headRotationComponent = store.getComponent(ref, HeadRotation.getComponentType());

         assert headRotationComponent != null;

         return playerRefComponent;
      } else {
         throw new IllegalStateException("Failed to add player ref of joining player to the world store");
      }
   }

   private void onSetupPlayerJoining(
      @Nonnull Holder<EntityStore> holder,
      @Nonnull Player playerComponent,
      @Nonnull PlayerRef playerRefComponent,
      @Nonnull PacketHandler packetHandler,
      @Nullable Transform transform,
      boolean clearWorld,
      boolean fadeInOut
   ) {
      UUID playerUuid = playerRefComponent.getUuid();
      this.logger
         .at(Level.INFO)
         .log("Adding player '%s' to world '%s' at location %s (%s)", playerRefComponent.getUsername(), this.name, transform, playerUuid);
      int entityId = this.entityStore.takeNextNetworkId();
      playerComponent.setNetworkId(entityId);
      PlayerConfigData configData = playerComponent.getPlayerConfigData();
      configData.setWorld(this.name);
      if (clearWorld) {
         LegacyEntityTrackerSystems.clear(playerComponent, holder);
         ChunkTracker chunkTrackerComponent = holder.getComponent(ChunkTracker.getComponentType());
         if (chunkTrackerComponent != null) {
            chunkTrackerComponent.unloadAll(playerRefComponent);
         }
      }

      playerComponent.getPageManager().clearCustomPageAcknowledgements();
      JoinWorld packet = new JoinWorld(clearWorld, fadeInOut, this.worldConfig.getUuid());
      packetHandler.write(packet);
      packetHandler.tryFlush();
      HytaleLogger.getLogger().at(Level.INFO).log("%s: Sent %s", packetHandler.getIdentifier(), packet);
      packetHandler.setQueuePackets(true);
   }

   @Nonnull
   public CompletableFuture<Void> drainPlayersTo(@Nonnull World fallbackTargetWorld, Collection<PlayerRef> players) {
      ObjectArrayList<CompletableFuture<PlayerRef>> futures = new ObjectArrayList();

      for (PlayerRef playerRef : players) {
         Holder<EntityStore> holder = playerRef.getReference() != null ? playerRef.removeFromStore() : playerRef.getHolder();
         DrainPlayerFromWorldEvent event = HytaleServer.get()
            .getEventBus()
            .dispatchFor(DrainPlayerFromWorldEvent.class, this.name)
            .dispatch(new DrainPlayerFromWorldEvent(holder, fallbackTargetWorld, null));
         futures.add(event.getWorld().addPlayer(playerRef, event.getTransform()));
      }

      return CompletableFuture.allOf((CompletableFuture<?>[])futures.toArray(CompletableFuture[]::new));
   }

   @Nonnull
   public GameplayConfig getGameplayConfig() {
      String gameplayConfigId = this.worldConfig.getGameplayConfig();
      GameplayConfig gameplayConfig = GameplayConfig.getAssetMap().getAsset(gameplayConfigId);
      if (gameplayConfig == null) {
         gameplayConfig = GameplayConfig.DEFAULT;
      }

      return gameplayConfig;
   }

   @Nonnull
   public Map<ClientFeature, Boolean> getFeatures() {
      return Collections.unmodifiableMap(this.features);
   }

   public boolean isFeatureEnabled(@Nonnull ClientFeature feature) {
      return this.features.getOrDefault(feature, false);
   }

   public void registerFeature(@Nonnull ClientFeature feature, boolean enabled) {
      this.features.put(feature, enabled);
      this.broadcastFeatures();
   }

   public void broadcastFeatures() {
      UpdateFeatures packet = new UpdateFeatures(this.features);

      for (PlayerRef playerRef : this.playerRefs) {
         playerRef.getPacketHandler().write(packet);
      }
   }

   @Nonnull
   public Path getSavePath() {
      return this.savePath;
   }

   public void updateEntitySeed(@Nonnull Store<EntityStore> store) {
      int newEntitySeed = this.random.nextInt();
      this.entitySeed.set(newEntitySeed);
      PlayerUtil.broadcastPacketToPlayers(store, new SetEntitySeed(newEntitySeed));
   }

   public void markGCHasRun() {
      this.gcHasRun = true;
   }

   public boolean consumeGCHasRun() {
      boolean gcHasRun = this.gcHasRun;
      this.gcHasRun = false;
      return gcHasRun;
   }

   @Override
   public int hashCode() {
      return this.name != null ? this.name.hashCode() : 0;
   }

   @Override
   public boolean equals(@Nullable Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         World world = (World)o;
         return this.name.equals(world.name);
      } else {
         return false;
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "World{name='"
         + this.name
         + "', alive="
         + this.alive.get()
         + ", loadedChunksCount="
         + this.chunkStore.getLoadedChunksCount()
         + ", totalLoadedChunksCount="
         + this.chunkStore.getTotalLoadedChunksCount()
         + ", totalGeneratedChunksCount="
         + this.chunkStore.getTotalGeneratedChunksCount()
         + ", entityCount="
         + this.entityStore.getStore().getEntityCount()
         + "}";
   }

   public void validate(@Nonnull StringBuilder errors, @Nonnull IPrefabBuffer.RawBlockConsumer<Void> blockValidator, @Nonnull EnumSet<ValidationOption> options) throws IOException {
      this.setThread(Thread.currentThread());
      this.onStart();
      Store<ChunkStore> store = this.chunkStore.getStore();
      StringBuilder tempBuilder = new StringBuilder();
      LongIterator var6 = this.chunkStore.getLoader().getIndexes().iterator();

      while (var6.hasNext()) {
         long index = (Long)var6.next();
         int chunkX = ChunkUtil.xOfChunkIndex(index);
         int chunkZ = ChunkUtil.zOfChunkIndex(index);

         try {
            CompletableFuture<Ref<ChunkStore>> future = this.chunkStore.getChunkReferenceAsync(index, 2);

            while (!future.isDone()) {
               this.consumeTaskQueue();
            }

            Ref<ChunkStore> reference = future.join();
            if (reference != null && reference.isValid()) {
               WorldChunk chunk = store.getComponent(reference, WorldChunk.getComponentType());
               ChunkColumn chunkColumn = store.getComponent(reference, ChunkColumn.getComponentType());
               if (chunkColumn != null) {
                  for (Ref<ChunkStore> section : chunkColumn.getSections()) {
                     final ChunkSection sectionInfo = store.getComponent(section, ChunkSection.getComponentType());
                     BlockSection blockSection = store.getComponent(section, BlockSection.getComponentType());
                     if (blockSection != null) {
                        BlockPhysics blockPhys = store.getComponent(section, BlockPhysics.getComponentType());

                        for (int y = 0; y < 32; y++) {
                           int worldY = (sectionInfo.getY() << 5) + y;

                           for (int z = 0; z < 32; z++) {
                              for (int x = 0; x < 32; x++) {
                                 int blockId = blockSection.get(x, y, z);
                                 int filler = blockSection.getFiller(x, y, z);
                                 int rotation = blockSection.getRotationIndex(x, y, z);
                                 Holder<ChunkStore> holder = chunk.getBlockComponentHolder(x, worldY, z);
                                 int worldX = ChunkUtil.minBlock(chunk.getX()) + x;
                                 int worldZ = ChunkUtil.minBlock(chunk.getZ()) + z;
                                 blockValidator.accept(
                                    worldX, worldY, worldZ, blockId, 0, 1.0F, holder, blockPhys != null ? blockPhys.get(x, y, z) : 0, rotation, filler, null
                                 );
                                 if (options.contains(ValidationOption.BLOCK_FILLER)) {
                                    var fetcher = new FillerBlockUtil.FillerFetcher<BlockSection, ChunkStore>() {
                                       public int getBlock(BlockSection blockSection, ChunkStore chunkStore, int xx, int yx, int zx) {
                                          if (xx >= 0 && yx >= 0 && zx >= 0 && xx < 32 && yx < 32 && zx < 32) {
                                             return blockSection.get(xx, yx, zx);
                                          } else {
                                             int nx = sectionInfo.getX() + ChunkUtil.chunkCoordinate(xx);
                                             int ny = sectionInfo.getY() + ChunkUtil.chunkCoordinate(yx);
                                             int nz = sectionInfo.getZ() + ChunkUtil.chunkCoordinate(zx);
                                             CompletableFuture<Ref<ChunkStore>> refFuture = chunkStore.getChunkSectionReferenceAsync(nx, ny, nz);

                                             while (!refFuture.isDone()) {
                                                World.this.consumeTaskQueue();
                                             }

                                             Ref<ChunkStore> ref = refFuture.join();
                                             BlockSection blocks = chunkStore.getStore().getComponent(ref, BlockSection.getComponentType());
                                             return blocks == null ? Integer.MIN_VALUE : blocks.get(xx, yx, zx);
                                          }
                                       }

                                       public int getFiller(BlockSection blockSection, ChunkStore chunkStore, int xx, int yx, int zx) {
                                          if (xx >= 0 && yx >= 0 && zx >= 0 && xx < 32 && yx < 32 && zx < 32) {
                                             return blockSection.getFiller(xx, yx, zx);
                                          } else {
                                             int nx = sectionInfo.getX() + ChunkUtil.chunkCoordinate(xx);
                                             int ny = sectionInfo.getY() + ChunkUtil.chunkCoordinate(yx);
                                             int nz = sectionInfo.getZ() + ChunkUtil.chunkCoordinate(zx);
                                             CompletableFuture<Ref<ChunkStore>> refFuture = chunkStore.getChunkSectionReferenceAsync(nx, ny, nz);

                                             while (!refFuture.isDone()) {
                                                World.this.consumeTaskQueue();
                                             }

                                             Ref<ChunkStore> ref = refFuture.join();
                                             BlockSection blocks = chunkStore.getStore().getComponent(ref, BlockSection.getComponentType());
                                             return blocks == null ? Integer.MIN_VALUE : blocks.getFiller(xx, yx, zx);
                                          }
                                       }

                                       public int getRotationIndex(BlockSection blockSection, ChunkStore chunkStore, int xx, int yx, int zx) {
                                          if (xx >= 0 && yx >= 0 && zx >= 0 && xx < 32 && yx < 32 && zx < 32) {
                                             return blockSection.getFiller(xx, yx, zx);
                                          } else {
                                             int nx = sectionInfo.getX() + ChunkUtil.chunkCoordinate(xx);
                                             int ny = sectionInfo.getY() + ChunkUtil.chunkCoordinate(yx);
                                             int nz = sectionInfo.getZ() + ChunkUtil.chunkCoordinate(zx);
                                             CompletableFuture<Ref<ChunkStore>> refFuture = chunkStore.getChunkSectionReferenceAsync(nx, ny, nz);

                                             while (!refFuture.isDone()) {
                                                World.this.consumeTaskQueue();
                                             }

                                             Ref<ChunkStore> ref = refFuture.join();
                                             BlockSection blocks = chunkStore.getStore().getComponent(ref, BlockSection.getComponentType());
                                             return blocks == null ? Integer.MIN_VALUE : blocks.getRotationIndex(xx, yx, zx);
                                          }
                                       }
                                    };
                                    FillerBlockUtil.ValidationResult fillerResult = FillerBlockUtil.validateBlock(
                                       x, y, z, blockId, rotation, filler, blockSection, this.chunkStore, fetcher
                                    );
                                    switch (fillerResult) {
                                       case OK:
                                       default:
                                          break;
                                       case INVALID_BLOCK: {
                                          BlockType blockType = BlockType.getAssetMap().getAsset(blockId);
                                          tempBuilder.append("\tBlock ")
                                             .append(blockType != null ? blockType.getId() : "<missing>")
                                             .append(" at ")
                                             .append(x)
                                             .append(", ")
                                             .append(y)
                                             .append(", ")
                                             .append(z)
                                             .append(" is not valid filler")
                                             .append('\n');
                                          break;
                                       }
                                       case INVALID_FILLER: {
                                          BlockType blockType = BlockType.getAssetMap().getAsset(blockId);
                                          tempBuilder.append("\tBlock ")
                                             .append(blockType != null ? blockType.getId() : "<missing>")
                                             .append(" at ")
                                             .append(x)
                                             .append(", ")
                                             .append(y)
                                             .append(", ")
                                             .append(z)
                                             .append(" has invalid/missing filler blocks")
                                             .append('\n');
                                       }
                                    }
                                 }
                              }
                           }
                        }

                        if (!tempBuilder.isEmpty()) {
                           errors.append("\tChunk ")
                              .append(sectionInfo.getX())
                              .append(", ")
                              .append(sectionInfo.getY())
                              .append(", ")
                              .append(sectionInfo.getZ())
                              .append(" validation errors:")
                              .append((CharSequence)tempBuilder);
                           tempBuilder.setLength(0);
                        }
                     }
                  }

                  if (options.contains(ValidationOption.ENTITIES)) {
                     ComponentType<EntityStore, UnknownComponents<EntityStore>> unknownComponentType = EntityStore.REGISTRY.getUnknownComponentType();

                     for (Holder<EntityStore> entityHolder : chunk.getEntityChunk().getEntityHolders()) {
                        UnknownComponents<EntityStore> unknownComponents = entityHolder.getComponent(unknownComponentType);
                        if (unknownComponents != null && !unknownComponents.getUnknownComponents().isEmpty()) {
                           errors.append("\tUnknown Entity Components: ").append(unknownComponents.getUnknownComponents()).append("\n");
                        }
                     }
                  }

                  store.tick(1.0F);
               }
            }
         } catch (CompletionException var35) {
            ((HytaleLogger.Api)this.getLogger().at(Level.SEVERE).withCause(var35)).log("Failed to validate chunk: %d, %d", chunkX, chunkZ);
            errors.append('\t')
               .append("Exception validating chunk: ")
               .append(chunkX)
               .append(", ")
               .append(chunkZ)
               .append('\n')
               .append("\t\t")
               .append(var35.getCause().getMessage())
               .append('\n');
         }
      }

      if (this.alive.getAndSet(false)) {
         this.onShutdown();
      }

      this.setThread(null);
   }

   @FunctionalInterface
   public interface GenericBlockBulkUpdater<T> {
      void apply(World var1, T var2, long var3, WorldChunk var5, int var6, int var7, int var8, int var9, int var10, int var11);
   }
}
