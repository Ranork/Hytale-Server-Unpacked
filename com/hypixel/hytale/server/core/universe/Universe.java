package com.hypixel.hytale.server.core.universe;

import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.lookup.Priority;
import com.hypixel.hytale.common.plugin.PluginIdentifier;
import com.hypixel.hytale.common.plugin.PluginManifest;
import com.hypixel.hytale.common.semver.SemverRange;
import com.hypixel.hytale.common.util.CompletableFutureUtil;
import com.hypixel.hytale.common.util.PathUtil;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.event.IEventDispatcher;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.metrics.MetricProvider;
import com.hypixel.hytale.metrics.MetricResults;
import com.hypixel.hytale.metrics.MetricsRegistry;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.PlayerSkin;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.netty.ProtocolUtil;
import com.hypixel.hytale.protocol.packets.setup.ServerTags;
import com.hypixel.hytale.server.core.Constants;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.HytaleServerConfig;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.NameMatching;
import com.hypixel.hytale.server.core.Options;
import com.hypixel.hytale.server.core.auth.PlayerAuthentication;
import com.hypixel.hytale.server.core.command.system.CommandRegistry;
import com.hypixel.hytale.server.core.config.BackupConfig;
import com.hypixel.hytale.server.core.cosmetics.CosmeticsModule;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.data.PlayerConfigData;
import com.hypixel.hytale.server.core.event.events.PrepareUniverseEvent;
import com.hypixel.hytale.server.core.event.events.ShutdownEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.io.ProtocolVersion;
import com.hypixel.hytale.server.core.io.handlers.game.GamePacketHandler;
import com.hypixel.hytale.server.core.io.netty.NettyUtil;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.MovementAudioComponent;
import com.hypixel.hytale.server.core.modules.entity.component.PositionDataComponent;
import com.hypixel.hytale.server.core.modules.entity.player.ChunkTracker;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerConnectionFlushSystem;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerPingSystem;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSkinComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.EntityTrackerSystems;
import com.hypixel.hytale.server.core.modules.singleplayer.SingleplayerModule;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.plugin.PluginManager;
import com.hypixel.hytale.server.core.receiver.IMessageReceiver;
import com.hypixel.hytale.server.core.universe.playerdata.PlayerStorage;
import com.hypixel.hytale.server.core.universe.system.PlayerRefAddedSystem;
import com.hypixel.hytale.server.core.universe.system.WorldConfigSaveSystem;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldConfig;
import com.hypixel.hytale.server.core.universe.world.WorldConfigProvider;
import com.hypixel.hytale.server.core.universe.world.commands.SetTickingCommand;
import com.hypixel.hytale.server.core.universe.world.commands.block.BlockCommand;
import com.hypixel.hytale.server.core.universe.world.commands.block.BlockSelectCommand;
import com.hypixel.hytale.server.core.universe.world.commands.world.WorldCommand;
import com.hypixel.hytale.server.core.universe.world.events.AddWorldEvent;
import com.hypixel.hytale.server.core.universe.world.events.AllWorldsLoadedEvent;
import com.hypixel.hytale.server.core.universe.world.events.RemoveWorldEvent;
import com.hypixel.hytale.server.core.universe.world.spawn.FitToHeightMapSpawnProvider;
import com.hypixel.hytale.server.core.universe.world.spawn.GlobalSpawnProvider;
import com.hypixel.hytale.server.core.universe.world.spawn.ISpawnProvider;
import com.hypixel.hytale.server.core.universe.world.spawn.IndividualSpawnProvider;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.universe.world.storage.component.ChunkSavingSystems;
import com.hypixel.hytale.server.core.universe.world.storage.provider.DefaultChunkStorageProvider;
import com.hypixel.hytale.server.core.universe.world.storage.provider.EmptyChunkStorageProvider;
import com.hypixel.hytale.server.core.universe.world.storage.provider.IChunkStorageProvider;
import com.hypixel.hytale.server.core.universe.world.storage.provider.IndexedStorageChunkStorageProvider;
import com.hypixel.hytale.server.core.universe.world.storage.provider.MigrationChunkStorageProvider;
import com.hypixel.hytale.server.core.universe.world.storage.provider.RocksDbChunkStorageProvider;
import com.hypixel.hytale.server.core.universe.world.storage.resources.DefaultResourceStorageProvider;
import com.hypixel.hytale.server.core.universe.world.storage.resources.DiskResourceStorageProvider;
import com.hypixel.hytale.server.core.universe.world.storage.resources.EmptyResourceStorageProvider;
import com.hypixel.hytale.server.core.universe.world.storage.resources.IResourceStorageProvider;
import com.hypixel.hytale.server.core.universe.world.system.WorldPregenerateSystem;
import com.hypixel.hytale.server.core.universe.world.worldgen.provider.DummyWorldGenProvider;
import com.hypixel.hytale.server.core.universe.world.worldgen.provider.FlatWorldGenProvider;
import com.hypixel.hytale.server.core.universe.world.worldgen.provider.IWorldGenProvider;
import com.hypixel.hytale.server.core.universe.world.worldgen.provider.VoidWorldGenProvider;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.worldstore.WorldMarkersResource;
import com.hypixel.hytale.server.core.universe.world.worldmap.provider.DisabledWorldMapProvider;
import com.hypixel.hytale.server.core.universe.world.worldmap.provider.IWorldMapProvider;
import com.hypixel.hytale.server.core.universe.world.worldmap.provider.chunk.WorldGenWorldMapProvider;
import com.hypixel.hytale.server.core.util.AssetUtil;
import com.hypixel.hytale.server.core.util.BsonUtil;
import com.hypixel.hytale.server.core.util.backup.BackupTask;
import com.hypixel.hytale.server.core.util.io.FileUtil;
import com.hypixel.hytale.sneakythrow.SneakyThrow;
import io.netty.channel.Channel;
import io.netty.handler.codec.quic.QuicChannel;
import io.netty.handler.codec.quic.QuicStreamChannel;
import io.netty.handler.codec.quic.QuicStreamType;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiPredicate;
import java.util.logging.Level;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import joptsimple.OptionSet;
import org.bson.BsonDocument;
import org.bson.BsonValue;

public class Universe extends JavaPlugin implements IMessageReceiver, MetricProvider {
   @Nonnull
   public static final PluginManifest MANIFEST = PluginManifest.corePlugin(Universe.class).build();
   @Nonnull
   private static Map<Integer, String> LEGACY_BLOCK_ID_MAP = Collections.emptyMap();
   @Nonnull
   public static final MetricsRegistry<Universe> METRICS_REGISTRY = new MetricsRegistry<Universe>()
      .register("Worlds", universe -> universe.getWorlds().values().toArray(World[]::new), new ArrayCodec<>(World.METRICS_REGISTRY, World[]::new))
      .register("PlayerCount", Universe::getPlayerCount, Codec.INTEGER);
   private static Universe instance;
   private ComponentType<EntityStore, PlayerRef> playerRefComponentType;
   @Nonnull
   private final Path path = Constants.UNIVERSE_PATH;
   private final Path worldsPath = this.path.resolve("worlds");
   private final Path worldsDeletedPath = this.worldsPath.resolveSibling("worlds-deleted");
   @Nonnull
   private final Map<UUID, PlayerRef> players = new ConcurrentHashMap<>();
   @Nonnull
   private final Map<String, World> worlds = new ConcurrentHashMap<>();
   @Nonnull
   private final Map<UUID, World> worldsByUuid = new ConcurrentHashMap<>();
   @Nonnull
   private final Map<String, World> unmodifiableWorlds = Collections.unmodifiableMap(this.worlds);
   private PlayerStorage playerStorage;
   private WorldConfigProvider worldConfigProvider;
   private ResourceType<ChunkStore, WorldMarkersResource> worldMarkersResourceType;
   private CompletableFuture<Void> universeReady;
   private final AtomicBoolean isBackingUp = new AtomicBoolean(false);

   public static Universe get() {
      return instance;
   }

   public Universe(@Nonnull JavaPluginInit init) {
      super(init);
      instance = this;
      if (!Files.isDirectory(this.path) && !Options.getOptionSet().has(Options.BARE)) {
         try {
            Files.createDirectories(this.path);
         } catch (IOException var4) {
            throw new RuntimeException("Failed to create universe directory", var4);
         }
      }

      BackupConfig backupConfig = HytaleServer.get().getConfig().getBackupConfig();
      if (backupConfig.isConfigured()) {
         int frequencyMinutes = backupConfig.getFrequencyMinutes();
         this.getLogger().at(Level.INFO).log("Scheduled backup to run every %d minute(s)", frequencyMinutes);
         HytaleServer.SCHEDULED_EXECUTOR.scheduleWithFixedDelay(() -> {
            if (!this.isBackingUp.compareAndSet(false, true)) {
               this.getLogger().at(Level.WARNING).log("Skipping scheduled backup: previous backup still in progress");
            } else {
               try {
                  this.getLogger().at(Level.INFO).log("Backing up universe...");
                  this.runBackup().whenComplete((aVoid, throwable) -> {
                     this.isBackingUp.set(false);
                     if (throwable != null) {
                        ((HytaleLogger.Api)this.getLogger().at(Level.SEVERE).withCause(throwable)).log("Scheduled backup failed");
                     } else {
                        this.getLogger().at(Level.INFO).log("Completed scheduled backup.");
                     }
                  });
               } catch (Exception var2x) {
                  this.isBackingUp.set(false);
                  ((HytaleLogger.Api)this.getLogger().at(Level.SEVERE).withCause(var2x)).log("Error backing up universe");
               }
            }
         }, frequencyMinutes, frequencyMinutes, TimeUnit.MINUTES);
      }
   }

   @Nonnull
   public CompletableFuture<Void> runBackup() {
      Path backupDir = HytaleServer.get().getConfig().getBackupConfig().getDirectory();
      return backupDir == null
         ? CompletableFuture.failedFuture(new IllegalStateException("Backup directory not configured"))
         : CompletableFuture.allOf(this.worlds.values().stream().map(world -> CompletableFuture.<ChunkSavingSystems.Data>supplyAsync(() -> {
               Store<ChunkStore> componentStore = world.getChunkStore().getStore();
               ChunkSavingSystems.Data data = componentStore.getResource(ChunkStore.SAVE_RESOURCE);
               data.isSaving = false;
               return data;
            }, world).thenCompose(ChunkSavingSystems.Data::waitForSavingChunks)).toArray(CompletableFuture[]::new))
            .thenCompose(aVoid -> BackupTask.start(this.path, backupDir))
            .thenCompose(success -> CompletableFuture.allOf(this.worlds.values().stream().map(world -> CompletableFuture.runAsync(() -> {
               Store<ChunkStore> componentStore = world.getChunkStore().getStore();
               ChunkSavingSystems.Data data = componentStore.getResource(ChunkStore.SAVE_RESOURCE);
               data.isSaving = true;
            }, world)).toArray(CompletableFuture[]::new)).thenApply(aVoid -> success));
   }

   @Override
   protected void setup() {
      EventRegistry eventRegistry = this.getEventRegistry();
      ComponentRegistryProxy<ChunkStore> chunkStoreRegistry = this.getChunkStoreRegistry();
      ComponentRegistryProxy<EntityStore> entityStoreRegistry = this.getEntityStoreRegistry();
      CommandRegistry commandRegistry = this.getCommandRegistry();
      eventRegistry.register((short)-48, ShutdownEvent.class, event -> this.disconnectAllPLayers());
      eventRegistry.register((short)-32, ShutdownEvent.class, event -> this.shutdownAllWorlds());
      ISpawnProvider.CODEC.register("Global", GlobalSpawnProvider.class, GlobalSpawnProvider.CODEC);
      ISpawnProvider.CODEC.register("Individual", IndividualSpawnProvider.class, IndividualSpawnProvider.CODEC);
      ISpawnProvider.CODEC.register("FitToHeightMap", FitToHeightMapSpawnProvider.class, FitToHeightMapSpawnProvider.CODEC);
      IWorldGenProvider.CODEC.register("Flat", FlatWorldGenProvider.class, FlatWorldGenProvider.CODEC);
      IWorldGenProvider.CODEC.register("Dummy", DummyWorldGenProvider.class, DummyWorldGenProvider.CODEC);
      IWorldGenProvider.CODEC.register(Priority.DEFAULT, "Void", VoidWorldGenProvider.class, VoidWorldGenProvider.CODEC);
      IWorldMapProvider.CODEC.register("Disabled", DisabledWorldMapProvider.class, DisabledWorldMapProvider.CODEC);
      IWorldMapProvider.CODEC.register(Priority.DEFAULT, "WorldGen", WorldGenWorldMapProvider.class, WorldGenWorldMapProvider.CODEC);
      IChunkStorageProvider.CODEC.register(Priority.DEFAULT, "Hytale", DefaultChunkStorageProvider.class, DefaultChunkStorageProvider.CODEC);
      IChunkStorageProvider.CODEC.register("Migration", MigrationChunkStorageProvider.class, MigrationChunkStorageProvider.CODEC);
      IChunkStorageProvider.CODEC.register("IndexedStorage", IndexedStorageChunkStorageProvider.class, IndexedStorageChunkStorageProvider.CODEC);
      IChunkStorageProvider.CODEC.register("RocksDb", RocksDbChunkStorageProvider.class, RocksDbChunkStorageProvider.CODEC);
      IChunkStorageProvider.CODEC.register("Empty", EmptyChunkStorageProvider.class, EmptyChunkStorageProvider.CODEC);
      IResourceStorageProvider.CODEC.register(Priority.DEFAULT, "Hytale", DefaultResourceStorageProvider.class, DefaultResourceStorageProvider.CODEC);
      IResourceStorageProvider.CODEC.register("Disk", DiskResourceStorageProvider.class, DiskResourceStorageProvider.CODEC);
      IResourceStorageProvider.CODEC.register("Empty", EmptyResourceStorageProvider.class, EmptyResourceStorageProvider.CODEC);
      this.worldMarkersResourceType = chunkStoreRegistry.registerResource(WorldMarkersResource.class, "SharedUserMapMarkers", WorldMarkersResource.CODEC);
      chunkStoreRegistry.registerSystem(new WorldPregenerateSystem());
      entityStoreRegistry.registerSystem(new WorldConfigSaveSystem());
      this.playerRefComponentType = entityStoreRegistry.registerComponent(PlayerRef.class, () -> {
         throw new UnsupportedOperationException();
      });
      entityStoreRegistry.registerSystem(new PlayerPingSystem());
      entityStoreRegistry.registerSystem(new PlayerConnectionFlushSystem(this.playerRefComponentType));
      entityStoreRegistry.registerSystem(new PlayerRefAddedSystem(this.playerRefComponentType));
      commandRegistry.registerCommand(new SetTickingCommand());
      commandRegistry.registerCommand(new BlockCommand());
      commandRegistry.registerCommand(new BlockSelectCommand());
      commandRegistry.registerCommand(new WorldCommand());
   }

   @Override
   protected void start() {
      HytaleServerConfig config = HytaleServer.get().getConfig();
      if (config == null) {
         throw new IllegalStateException("Server config is not loaded!");
      } else {
         this.playerStorage = config.getPlayerStorageProvider().getPlayerStorage();
         WorldConfigProvider.Default defaultConfigProvider = new WorldConfigProvider.Default();
         PrepareUniverseEvent event = HytaleServer.get()
            .getEventBus()
            .<Void, PrepareUniverseEvent>dispatchFor(PrepareUniverseEvent.class)
            .dispatch(new PrepareUniverseEvent(defaultConfigProvider));
         WorldConfigProvider worldConfigProvider = event.getWorldConfigProvider();
         if (worldConfigProvider == null) {
            worldConfigProvider = defaultConfigProvider;
         }

         this.worldConfigProvider = worldConfigProvider;

         try {
            Path blockIdMapPath = this.path.resolve("blockIdMap.json");
            Path path = this.path.resolve("blockIdMap.legacy.json");
            if (Files.isRegularFile(blockIdMapPath)) {
               Files.move(blockIdMapPath, path, StandardCopyOption.REPLACE_EXISTING);
            }

            Files.deleteIfExists(this.path.resolve("blockIdMap.json.bak"));
            if (Files.isRegularFile(path)) {
               Map<Integer, String> map = new Int2ObjectOpenHashMap();

               for (BsonValue bsonValue : BsonUtil.readDocument(path).thenApply(document -> document.getArray("Blocks")).join()) {
                  BsonDocument bsonDocument = bsonValue.asDocument();
                  map.put(bsonDocument.getNumber("Id").intValue(), bsonDocument.getString("BlockType").getValue());
               }

               LEGACY_BLOCK_ID_MAP = Collections.unmodifiableMap(map);
            }
         } catch (IOException var15) {
            ((HytaleLogger.Api)this.getLogger().at(Level.SEVERE).withCause(var15)).log("Failed to delete blockIdMap.json");
         }

         if (Options.getOptionSet().has(Options.BARE)) {
            this.universeReady = CompletableFuture.completedFuture(null);
            HytaleServer.get().getEventBus().dispatch(AllWorldsLoadedEvent.class);
         } else {
            ObjectArrayList<CompletableFuture<?>> loadingWorlds = new ObjectArrayList();

            try {
               if (Files.exists(this.worldsDeletedPath)) {
                  FileUtil.deleteDirectory(this.worldsDeletedPath);
               }
            } catch (Throwable var14) {
               throw new RuntimeException("Failed to complete deletion of " + this.worldsDeletedPath.toAbsolutePath(), var14);
            }

            try {
               Files.createDirectories(this.worldsPath);

               try (DirectoryStream<Path> stream = Files.newDirectoryStream(this.worldsPath)) {
                  for (Path file : stream) {
                     if (HytaleServer.get().isShuttingDown()) {
                        return;
                     }

                     if (!file.equals(this.worldsPath) && Files.isDirectory(file)) {
                        String name = file.getFileName().toString();
                        if (this.getWorld(name) == null) {
                           loadingWorlds.add(this.loadWorldFromStart(file, name).exceptionally(throwable -> {
                              ((HytaleLogger.Api)this.getLogger().at(Level.SEVERE).withCause(throwable)).log("Failed to load world: %s", name);
                              return null;
                           }));
                        } else {
                           this.getLogger().at(Level.SEVERE).log("Skipping loading world '%s' because it already exists!", name);
                        }
                     }
                  }
               }

               this.universeReady = CompletableFutureUtil._catch(
                  CompletableFuture.allOf((CompletableFuture<?>[])loadingWorlds.toArray(CompletableFuture[]::new))
                     .thenCompose(
                        v -> {
                           String worldName = config.getDefaults().getWorld();
                           return worldName != null && !this.worlds.containsKey(worldName.toLowerCase())
                              ? CompletableFutureUtil._catch(this.addWorld(worldName))
                              : CompletableFuture.completedFuture(null);
                        }
                     )
                     .thenRun(() -> HytaleServer.get().getEventBus().dispatch(AllWorldsLoadedEvent.class))
               );
            } catch (IOException var13) {
               throw new RuntimeException("Failed to load Worlds", var13);
            }
         }
      }
   }

   @Override
   protected void shutdown() {
      this.disconnectAllPLayers();
      this.shutdownAllWorlds();
   }

   public void disconnectAllPLayers() {
      this.players.values().forEach(player -> player.getPacketHandler().disconnect("Stopping server!"));
   }

   public void shutdownAllWorlds() {
      Iterator<World> iterator = this.worlds.values().iterator();

      while (iterator.hasNext()) {
         World world = iterator.next();
         world.stop();
         iterator.remove();
      }
   }

   @Nonnull
   @Override
   public MetricResults toMetricResults() {
      return METRICS_REGISTRY.toMetricResults(this);
   }

   public CompletableFuture<Void> getUniverseReady() {
      return this.universeReady;
   }

   public ResourceType<ChunkStore, WorldMarkersResource> getWorldMarkersResourceType() {
      return this.worldMarkersResourceType;
   }

   public boolean isWorldLoadable(@Nonnull String name) {
      Path savePath = this.validateWorldPath(name);
      return Files.isDirectory(savePath) && (Files.exists(savePath.resolve("config.bson")) || Files.exists(savePath.resolve("config.json")));
   }

   @Nonnull
   @CheckReturnValue
   public CompletableFuture<World> addWorld(@Nonnull String name) {
      return this.addWorld(name, null, null);
   }

   @Nonnull
   @Deprecated
   @CheckReturnValue
   public CompletableFuture<World> addWorld(@Nonnull String name, @Nullable String generatorType, @Nullable String chunkStorageType) {
      if (this.worlds.containsKey(name)) {
         throw new IllegalArgumentException("World " + name + " already exists!");
      } else if (this.isWorldLoadable(name)) {
         throw new IllegalArgumentException("World " + name + " already exists on disk!");
      } else {
         Path savePath = this.validateWorldPath(name);
         return this.worldConfigProvider.load(savePath, name).thenCompose(worldConfig -> {
            if (generatorType != null && !"default".equals(generatorType)) {
               BuilderCodec<? extends IWorldGenProvider> providerCodec = IWorldGenProvider.CODEC.getCodecFor(generatorType);
               if (providerCodec == null) {
                  throw new IllegalArgumentException("Unknown generatorType '" + generatorType + "'");
               }

               IWorldGenProvider provider = providerCodec.getDefaultValue();
               worldConfig.setWorldGenProvider(provider);
               worldConfig.markChanged();
            }

            if (chunkStorageType != null && !"default".equals(chunkStorageType)) {
               BuilderCodec<? extends IChunkStorageProvider<?>> providerCodec = IChunkStorageProvider.CODEC.getCodecFor(chunkStorageType);
               if (providerCodec == null) {
                  throw new IllegalArgumentException("Unknown chunkStorageType '" + chunkStorageType + "'");
               }

               IChunkStorageProvider<?> provider = (IChunkStorageProvider<?>)providerCodec.getDefaultValue();
               worldConfig.setChunkStorageProvider(provider);
               worldConfig.markChanged();
            }

            return this.makeWorld(name, savePath, worldConfig);
         });
      }
   }

   public Path validateWorldPath(@Nonnull String name) {
      Path savePath = PathUtil.resolvePathWithinDir(this.worldsPath, name);
      if (savePath == null) {
         throw new IllegalArgumentException("World " + name + " contains invalid characters!");
      } else {
         return savePath;
      }
   }

   @Nonnull
   @CheckReturnValue
   public CompletableFuture<World> makeWorld(@Nonnull String name, @Nonnull Path savePath, @Nonnull WorldConfig worldConfig) {
      return this.makeWorld(name, savePath, worldConfig, true);
   }

   @Nonnull
   @CheckReturnValue
   public CompletableFuture<World> makeWorld(@Nonnull String name, @Nonnull Path savePath, @Nonnull WorldConfig worldConfig, boolean start) {
      if (!PathUtil.isChildOf(this.worldsPath, savePath) && !PathUtil.isInTrustedRoot(savePath)) {
         throw new IllegalArgumentException("Invalid path");
      } else {
         Map<PluginIdentifier, SemverRange> map = worldConfig.getRequiredPlugins();
         if (map != null) {
            PluginManager pluginManager = PluginManager.get();

            for (Entry<PluginIdentifier, SemverRange> entry : map.entrySet()) {
               if (!pluginManager.hasPlugin(entry.getKey(), entry.getValue())) {
                  this.getLogger().at(Level.SEVERE).log("Failed to load world! Missing plugin: %s, Version: %s", entry.getKey(), entry.getValue());
                  throw new IllegalStateException("Missing plugin");
               }
            }
         }

         if (this.worlds.containsKey(name)) {
            throw new IllegalArgumentException("World " + name + " already exists!");
         } else {
            return CompletableFuture.supplyAsync(
                  SneakyThrow.sneakySupplier(
                     () -> {
                        World world = new World(name, savePath, worldConfig);
                        AddWorldEvent event = HytaleServer.get().getEventBus().dispatchFor(AddWorldEvent.class, name).dispatch(new AddWorldEvent(world));
                        if (!event.isCancelled() && !HytaleServer.get().isShuttingDown()) {
                           World oldWorldByName = this.worlds.putIfAbsent(name.toLowerCase(), world);
                           if (oldWorldByName != null) {
                              throw new ConcurrentModificationException(
                                 "World with name " + name + " already exists but didn't before! Looks like you have a race condition."
                              );
                           } else {
                              World oldWorldByUuid = this.worldsByUuid.putIfAbsent(worldConfig.getUuid(), world);
                              if (oldWorldByUuid != null) {
                                 throw new ConcurrentModificationException(
                                    "World with UUID " + worldConfig.getUuid() + " already exists but didn't before! Looks like you have a race condition."
                                 );
                              } else {
                                 return world;
                              }
                           }
                        } else {
                           throw new WorldLoadCancelledException();
                        }
                     }
                  )
               )
               .thenCompose(World::init)
               .thenCompose(
                  world -> !Options.getOptionSet().has(Options.MIGRATIONS) && start
                     ? world.start().thenApply(v -> world)
                     : CompletableFuture.completedFuture(world)
               )
               .whenComplete((world, throwable) -> {
                  if (throwable != null) {
                     String nameLower = name.toLowerCase();
                     if (this.worlds.containsKey(nameLower)) {
                        try {
                           this.removeWorldExceptionally(name, Map.of());
                        } catch (Exception var6x) {
                           ((HytaleLogger.Api)this.getLogger().at(Level.WARNING).withCause(var6x))
                              .log("Failed to clean up world '%s' after init failure", name);
                        }
                     }
                  }
               });
         }
      }
   }

   private CompletableFuture<Void> loadWorldFromStart(@Nonnull Path savePath, @Nonnull String name) {
      return this.worldConfigProvider
         .load(savePath, name)
         .thenCompose(worldConfig -> worldConfig.isDeleteOnUniverseStart() ? CompletableFuture.runAsync(() -> {
            try {
               FileUtil.deleteDirectory(savePath);
               this.getLogger().at(Level.INFO).log("Deleted world " + name + " from DeleteOnUniverseStart flag on universe start at " + savePath);
            } catch (Throwable var4) {
               throw new RuntimeException("Error deleting world directory on universe start", var4);
            }
         }) : this.makeWorld(name, savePath, worldConfig).thenApply(x -> null));
   }

   @Nonnull
   @CheckReturnValue
   public CompletableFuture<World> loadWorld(@Nonnull String name) {
      if (this.worlds.containsKey(name)) {
         throw new IllegalArgumentException("World " + name + " already loaded!");
      } else {
         Path savePath = this.validateWorldPath(name);
         if (!Files.isDirectory(savePath)) {
            throw new IllegalArgumentException("World " + name + " does not exist!");
         } else {
            return this.worldConfigProvider.load(savePath, name).thenCompose(worldConfig -> this.makeWorld(name, savePath, worldConfig));
         }
      }
   }

   @Nullable
   public World getWorld(@Nullable String worldName) {
      return worldName == null ? null : this.worlds.get(worldName.toLowerCase());
   }

   @Nullable
   public World getWorld(@Nonnull UUID uuid) {
      return this.worldsByUuid.get(uuid);
   }

   @Nullable
   public World getDefaultWorld() {
      HytaleServerConfig config = HytaleServer.get().getConfig();
      if (config == null) {
         return null;
      } else {
         String worldName = config.getDefaults().getWorld();
         return worldName != null ? this.getWorld(worldName) : null;
      }
   }

   public boolean removeWorld(@Nonnull String name) {
      Objects.requireNonNull(name, "Name can't be null!");
      String nameLower = name.toLowerCase();
      World world = this.worlds.get(nameLower);
      if (world == null) {
         throw new NullPointerException("World " + name + " doesn't exist!");
      } else {
         RemoveWorldEvent event = HytaleServer.get()
            .getEventBus()
            .dispatchFor(RemoveWorldEvent.class, name)
            .dispatch(new RemoveWorldEvent(world, RemoveWorldEvent.RemovalReason.GENERAL));
         if (event.isCancelled()) {
            return false;
         } else {
            this.worlds.remove(nameLower);
            this.worldsByUuid.remove(world.getWorldConfig().getUuid());
            if (world.isAlive()) {
               if (world.isInThread()) {
                  world.stopIndividualWorld();
               } else {
                  CompletableFuture.runAsync(world::stopIndividualWorld).join();
               }
            }

            world.validateDeleteOnRemove();
            return true;
         }
      }
   }

   public void removeWorldExceptionally(@Nonnull String name, Map<UUID, PlayerRef> players) {
      Objects.requireNonNull(name, "Name can't be null!");
      this.getLogger().at(Level.INFO).log("Removing world exceptionally: %s", name);
      String nameLower = name.toLowerCase();
      World world = this.worlds.get(nameLower);
      if (world == null) {
         throw new NullPointerException("World " + name + " doesn't exist!");
      } else {
         HytaleServer.get()
            .getEventBus()
            .dispatchFor(RemoveWorldEvent.class, name)
            .dispatch(new RemoveWorldEvent(world, RemoveWorldEvent.RemovalReason.EXCEPTIONAL));
         this.worlds.remove(nameLower);
         this.worldsByUuid.remove(world.getWorldConfig().getUuid());
         if (world.isAlive()) {
            if (world.isInThread()) {
               world.stopIndividualWorld(players);
            } else {
               CompletableFuture.runAsync(() -> world.stopIndividualWorld(players)).join();
            }
         }

         world.validateDeleteOnRemove();
      }
   }

   @Nonnull
   public Path getPath() {
      return this.path;
   }

   public Path getWorldsPath() {
      return this.worldsPath;
   }

   public Path getWorldsDeletedPath() {
      return this.worldsDeletedPath;
   }

   @Nonnull
   public Map<String, World> getWorlds() {
      return this.unmodifiableWorlds;
   }

   @Nonnull
   public List<PlayerRef> getPlayers() {
      return new ObjectArrayList(this.players.values());
   }

   @Nullable
   public PlayerRef getPlayer(@Nonnull UUID uuid) {
      return this.players.get(uuid);
   }

   @Nullable
   public PlayerRef getPlayer(@Nonnull String value, @Nonnull NameMatching matching) {
      return matching.find(this.players.values(), value, v -> v.getComponent(PlayerRef.getComponentType()).getUsername());
   }

   @Nullable
   public PlayerRef getPlayer(@Nonnull String value, @Nonnull Comparator<String> comparator, @Nonnull BiPredicate<String, String> equality) {
      return NameMatching.find(this.players.values(), value, v -> v.getComponent(PlayerRef.getComponentType()).getUsername(), comparator, equality);
   }

   @Nullable
   public PlayerRef getPlayerByUsername(@Nonnull String value, @Nonnull NameMatching matching) {
      return matching.find(this.players.values(), value, PlayerRef::getUsername);
   }

   @Nullable
   public PlayerRef getPlayerByUsername(@Nonnull String value, @Nonnull Comparator<String> comparator, @Nonnull BiPredicate<String, String> equality) {
      return NameMatching.find(this.players.values(), value, PlayerRef::getUsername, comparator, equality);
   }

   public int getPlayerCount() {
      return this.players.size();
   }

   @Nonnull
   public CompletableFuture<PlayerRef> addPlayer(
      @Nonnull Channel channel,
      @Nonnull String language,
      @Nonnull ProtocolVersion protocolVersion,
      @Nonnull UUID uuid,
      @Nonnull String username,
      @Nonnull PlayerAuthentication auth,
      int clientViewRadiusChunks,
      @Nullable PlayerSkin skin
   ) {
      GamePacketHandler playerConnection = new GamePacketHandler(channel, protocolVersion, auth);
      playerConnection.setQueuePackets(false);
      this.getLogger().at(Level.INFO).log("Adding player '%s (%s)", username, uuid);
      CompletableFuture<Void> setupFuture;
      if (channel instanceof QuicStreamChannel streamChannel) {
         QuicChannel conn = streamChannel.parent();
         conn.attr(ProtocolUtil.STREAM_CHANNEL_KEY).set(NetworkChannel.Default);
         streamChannel.updatePriority(PacketHandler.DEFAULT_STREAM_PRIORITIES.get(NetworkChannel.Default));
         CompletableFuture<Void> chunkFuture = NettyUtil.createStream(
            conn, QuicStreamType.UNIDIRECTIONAL, NetworkChannel.Chunks, PacketHandler.DEFAULT_STREAM_PRIORITIES.get(NetworkChannel.Chunks), playerConnection
         );
         CompletableFuture<Void> worldMapFuture = NettyUtil.createStream(
            conn,
            QuicStreamType.UNIDIRECTIONAL,
            NetworkChannel.WorldMap,
            PacketHandler.DEFAULT_STREAM_PRIORITIES.get(NetworkChannel.WorldMap),
            playerConnection
         );
         setupFuture = CompletableFuture.allOf(chunkFuture, worldMapFuture);
      } else {
         playerConnection.setChannel(NetworkChannel.WorldMap, channel);
         playerConnection.setChannel(NetworkChannel.Chunks, channel);
         setupFuture = CompletableFuture.completedFuture(null);
      }

      return setupFuture.<Holder<EntityStore>, Holder<EntityStore>>thenCombine(this.playerStorage.load(uuid), (setupResult, playerData) -> playerData)
         .exceptionally(throwable -> {
            throw new RuntimeException("Exception when adding player to universe:", throwable);
         })
         .thenCompose(
            holder -> {
               ChunkTracker chunkTrackerComponent = new ChunkTracker();
               PlayerRef playerRefComponent = new PlayerRef((Holder<EntityStore>)holder, uuid, username, language, playerConnection, chunkTrackerComponent);
               chunkTrackerComponent.setDefaultMaxChunksPerSecond(playerRefComponent);
               holder.putComponent(PlayerRef.getComponentType(), playerRefComponent);
               holder.putComponent(ChunkTracker.getComponentType(), chunkTrackerComponent);
               holder.putComponent(UUIDComponent.getComponentType(), new UUIDComponent(uuid));
               holder.ensureComponent(PositionDataComponent.getComponentType());
               holder.ensureComponent(MovementAudioComponent.getComponentType());
               Player playerComponent = holder.ensureAndGetComponent(Player.getComponentType());
               playerComponent.init(uuid, playerRefComponent);
               PlayerConfigData playerConfig = playerComponent.getPlayerConfigData();
               playerConfig.cleanup(this);
               PacketHandler.logConnectionTimings(channel, "Load Player Config", Level.FINEST);
               if (skin != null) {
                  holder.putComponent(PlayerSkinComponent.getComponentType(), new PlayerSkinComponent(skin));
                  holder.putComponent(ModelComponent.getComponentType(), new ModelComponent(CosmeticsModule.get().createModel(skin)));
               }

               playerConnection.setPlayerRef(playerRefComponent, playerComponent);
               NettyUtil.setChannelHandler(channel, playerConnection);
               playerComponent.setClientViewRadius(clientViewRadiusChunks);
               EntityTrackerSystems.EntityViewer entityViewerComponent = holder.getComponent(EntityTrackerSystems.EntityViewer.getComponentType());
               if (entityViewerComponent != null) {
                  entityViewerComponent.viewRadiusBlocks = playerComponent.getViewRadius() * 32;
               } else {
                  entityViewerComponent = new EntityTrackerSystems.EntityViewer(playerComponent.getViewRadius() * 32, playerConnection);
                  holder.addComponent(EntityTrackerSystems.EntityViewer.getComponentType(), entityViewerComponent);
               }

               PlayerRef existingPlayer = this.players.putIfAbsent(uuid, playerRefComponent);
               if (existingPlayer != null) {
                  this.getLogger().at(Level.WARNING).log("Player '%s' (%s) already joining from another connection, rejecting duplicate", username, uuid);
                  playerConnection.disconnect("A connection with this account is already in progress");
                  return CompletableFuture.completedFuture(null);
               } else {
                  String lastWorldName = playerConfig.getWorld();
                  World lastWorld = this.getWorld(lastWorldName);
                  PlayerConnectEvent event = HytaleServer.get()
                     .getEventBus()
                     .<Void, PlayerConnectEvent>dispatchFor(PlayerConnectEvent.class)
                     .dispatch(new PlayerConnectEvent((Holder<EntityStore>)holder, playerRefComponent, lastWorld != null ? lastWorld : this.getDefaultWorld()));
                  if (!channel.isActive()) {
                     this.players.remove(uuid, playerRefComponent);
                     this.getLogger().at(Level.INFO).log("Player '%s' (%s) disconnected during PlayerConnectEvent, cleaned up", username, uuid);
                     return CompletableFuture.completedFuture(null);
                  } else {
                     World world = event.getWorld() != null ? event.getWorld() : this.getDefaultWorld();
                     if (world == null) {
                        this.players.remove(uuid, playerRefComponent);
                        playerConnection.disconnect("No world available to join");
                        this.getLogger().at(Level.SEVERE).log("Player '%s' (%s) could not join - no default world configured", username, uuid);
                        return CompletableFuture.completedFuture(null);
                     } else {
                        if (lastWorldName != null && lastWorld == null) {
                           playerComponent.sendMessage(
                              Message.translation("server.universe.failedToFindWorld").param("lastWorldName", lastWorldName).param("name", world.getName())
                           );
                        }

                        PacketHandler.logConnectionTimings(channel, "Processed Referral", Level.FINEST);
                        playerRefComponent.getPacketHandler().write(new ServerTags(AssetRegistry.getClientTags()));
                        CompletableFuture<PlayerRef> addPlayerFuture = world.addPlayer(playerRefComponent, null, false, false);
                        if (addPlayerFuture == null) {
                           this.players.remove(uuid, playerRefComponent);
                           this.getLogger().at(Level.INFO).log("Player '%s' (%s) disconnected before world addition, cleaned up", username, uuid);
                           return CompletableFuture.completedFuture(null);
                        } else {
                           return addPlayerFuture.<PlayerRef>thenApply(
                                 p -> {
                                    PacketHandler.logConnectionTimings(channel, "Add to World", Level.FINEST);
                                    if (!channel.isActive()) {
                                       if (p != null) {
                                          playerComponent.remove();
                                       }

                                       this.players.remove(uuid, playerRefComponent);
                                       this.getLogger()
                                          .at(Level.WARNING)
                                          .log("Player '%s' (%s) disconnected during world join, cleaned up from universe", username, uuid);
                                       return null;
                                    } else if (playerComponent.wasRemoved()) {
                                       this.players.remove(uuid, playerRefComponent);
                                       return null;
                                    } else {
                                       return (PlayerRef)p;
                                    }
                                 }
                              )
                              .exceptionally(throwable -> {
                                 this.players.remove(uuid, playerRefComponent);
                                 playerComponent.remove();
                                 throw new RuntimeException("Exception when adding player to universe:", throwable);
                              });
                        }
                     }
                  }
               }
            }
         );
   }

   public void removePlayer(@Nonnull PlayerRef playerRef) {
      this.getLogger().at(Level.INFO).log("Removing player '" + playerRef.getUsername() + "' (" + playerRef.getUuid() + ")");
      IEventDispatcher<PlayerDisconnectEvent, PlayerDisconnectEvent> eventDispatcher = HytaleServer.get()
         .getEventBus()
         .dispatchFor(PlayerDisconnectEvent.class);
      if (eventDispatcher.hasListener()) {
         eventDispatcher.dispatch(new PlayerDisconnectEvent(playerRef));
      }

      Ref<EntityStore> ref = playerRef.getReference();
      if (ref == null) {
         this.finalizePlayerRemoval(playerRef);
      } else {
         World world = ref.getStore().getExternalData().getWorld();
         if (world.isInThread()) {
            Player playerComponent = ref.getStore().getComponent(ref, Player.getComponentType());
            if (playerComponent != null) {
               playerComponent.remove();
            }

            this.finalizePlayerRemoval(playerRef);
         } else {
            CompletableFuture<Void> removedFuture = new CompletableFuture<>();
            CompletableFuture.runAsync(() -> {
               Player playerComponent = ref.getStore().getComponent(ref, Player.getComponentType());
               if (playerComponent != null) {
                  playerComponent.remove();
               }
            }, world).whenComplete((unused, throwable) -> {
               if (throwable != null) {
                  removedFuture.completeExceptionally(throwable);
               } else {
                  removedFuture.complete(unused);
               }
            });
            removedFuture.orTimeout(5L, TimeUnit.SECONDS)
               .whenComplete(
                  (result, error) -> {
                     if (error != null) {
                        ((HytaleLogger.Api)this.getLogger().at(Level.WARNING).withCause(error))
                           .log("Timeout or error waiting for player '%s' removal from world store", playerRef.getUsername());
                     }

                     this.finalizePlayerRemoval(playerRef);
                  }
               );
         }
      }
   }

   private void finalizePlayerRemoval(@Nonnull PlayerRef playerRef) {
      this.players.remove(playerRef.getUuid());
      if (Constants.SINGLEPLAYER) {
         if (this.players.isEmpty()) {
            this.getLogger().at(Level.INFO).log("No players left on singleplayer server shutting down!");
            HytaleServer.get().shutdownServer();
         } else if (SingleplayerModule.isOwner(playerRef)) {
            this.getLogger().at(Level.INFO).log("Owner left the singleplayer server shutting down!");
            this.getPlayers().forEach(p -> p.getPacketHandler().disconnect(playerRef.getUsername() + " left! Shutting down singleplayer world!"));
            HytaleServer.get().shutdownServer();
         }
      }
   }

   @Nonnull
   public CompletableFuture<PlayerRef> resetPlayer(@Nonnull PlayerRef oldPlayer) {
      return this.playerStorage.load(oldPlayer.getUuid()).exceptionally(throwable -> {
         throw new RuntimeException("Exception when adding player to universe:", throwable);
      }).thenCompose(holder -> this.resetPlayer(oldPlayer, (Holder<EntityStore>)holder));
   }

   @Nonnull
   public CompletableFuture<PlayerRef> resetPlayer(@Nonnull PlayerRef oldPlayer, @Nonnull Holder<EntityStore> holder) {
      return this.resetPlayer(oldPlayer, holder, null, null);
   }

   @Nonnull
   public CompletableFuture<PlayerRef> resetPlayer(
      @Nonnull PlayerRef playerRef, @Nonnull Holder<EntityStore> holder, @Nullable World world, @Nullable Transform transform
   ) {
      UUID uuid = playerRef.getUuid();
      Player oldPlayer = playerRef.getComponent(Player.getComponentType());
      World targetWorld;
      if (world == null) {
         targetWorld = oldPlayer.getWorld();
      } else {
         targetWorld = world;
      }

      this.getLogger()
         .at(Level.INFO)
         .log(
            "Resetting player '%s', moving to world '%s' at location %s (%s)",
            playerRef.getUsername(),
            world != null ? world.getName() : null,
            transform,
            playerRef.getUuid()
         );
      GamePacketHandler playerConnection = (GamePacketHandler)playerRef.getPacketHandler();
      Player newPlayer = holder.ensureAndGetComponent(Player.getComponentType());
      newPlayer.init(uuid, playerRef);
      CompletableFuture<Void> leaveWorld = new CompletableFuture<>();
      if (oldPlayer.getWorld() != null) {
         oldPlayer.getWorld().execute(() -> {
            playerRef.removeFromStore();
            leaveWorld.complete(null);
         });
      } else {
         leaveWorld.complete(null);
      }

      return leaveWorld.thenAccept(v -> {
         oldPlayer.resetManagers(holder);
         newPlayer.copyFrom(oldPlayer);
         EntityTrackerSystems.EntityViewer viewer = holder.getComponent(EntityTrackerSystems.EntityViewer.getComponentType());
         if (viewer != null) {
            viewer.viewRadiusBlocks = newPlayer.getViewRadius() * 32;
         } else {
            viewer = new EntityTrackerSystems.EntityViewer(newPlayer.getViewRadius() * 32, playerConnection);
            holder.addComponent(EntityTrackerSystems.EntityViewer.getComponentType(), viewer);
         }

         playerConnection.setPlayerRef(playerRef, newPlayer);
         playerRef.replaceHolder(holder);
         holder.putComponent(PlayerRef.getComponentType(), playerRef);
      }).thenCompose(v -> targetWorld.addPlayer(playerRef, transform));
   }

   @Override
   public void sendMessage(@Nonnull Message message) {
      for (PlayerRef ref : this.players.values()) {
         ref.sendMessage(message);
      }
   }

   public void broadcastPacket(@Nonnull ToClientPacket packet) {
      for (PlayerRef player : this.players.values()) {
         player.getPacketHandler().write(packet);
      }
   }

   public void broadcastPacketNoCache(@Nonnull ToClientPacket packet) {
      for (PlayerRef player : this.players.values()) {
         player.getPacketHandler().writeNoCache(packet);
      }
   }

   public void broadcastPacket(@Nonnull ToClientPacket... packets) {
      for (PlayerRef player : this.players.values()) {
         player.getPacketHandler().write(packets);
      }
   }

   public PlayerStorage getPlayerStorage() {
      return this.playerStorage;
   }

   public void setPlayerStorage(@Nonnull PlayerStorage playerStorage) {
      this.playerStorage = playerStorage;
   }

   public WorldConfigProvider getWorldConfigProvider() {
      return this.worldConfigProvider;
   }

   @Nonnull
   public ComponentType<EntityStore, PlayerRef> getPlayerRefComponentType() {
      return this.playerRefComponentType;
   }

   @Nonnull
   @Deprecated
   public static Map<Integer, String> getLegacyBlockIdMap() {
      return LEGACY_BLOCK_ID_MAP;
   }

   public static Path getWorldGenPath() {
      OptionSet optionSet = Options.getOptionSet();
      Path worldGenPath;
      if (optionSet.has(Options.WORLD_GEN_DIRECTORY)) {
         worldGenPath = (Path)optionSet.valueOf(Options.WORLD_GEN_DIRECTORY);
      } else {
         worldGenPath = AssetUtil.getHytaleAssetsPath().resolve("Server").resolve("World");
      }

      return worldGenPath;
   }
}
