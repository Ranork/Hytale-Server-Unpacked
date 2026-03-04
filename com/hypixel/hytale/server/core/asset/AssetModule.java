package com.hypixel.hytale.server.core.asset;

import com.hypixel.hytale.assetstore.AssetLoadResult;
import com.hypixel.hytale.assetstore.AssetMap;
import com.hypixel.hytale.assetstore.AssetPack;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.event.RegisterAssetStoreEvent;
import com.hypixel.hytale.assetstore.event.RemoveAssetStoreEvent;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.util.RawJsonReader;
import com.hypixel.hytale.common.plugin.PluginIdentifier;
import com.hypixel.hytale.common.plugin.PluginManifest;
import com.hypixel.hytale.common.util.FormatUtil;
import com.hypixel.hytale.common.util.PathUtil;
import com.hypixel.hytale.common.util.java.ManifestUtil;
import com.hypixel.hytale.event.EventPriority;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Constants;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.HytaleServerConfig;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.Options;
import com.hypixel.hytale.server.core.ShutdownReason;
import com.hypixel.hytale.server.core.asset.monitor.AssetMonitor;
import com.hypixel.hytale.server.core.asset.type.gameplay.respawn.HomeOrSpawnPoint;
import com.hypixel.hytale.server.core.asset.type.gameplay.respawn.RespawnController;
import com.hypixel.hytale.server.core.asset.type.gameplay.respawn.WorldSpawnPoint;
import com.hypixel.hytale.server.core.asset.type.item.DroplistCommand;
import com.hypixel.hytale.server.core.config.ModConfig;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.BootEvent;
import com.hypixel.hytale.server.core.event.events.player.AddPlayerToWorldEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.plugin.PluginManager;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.worldgen.IWorldGen;
import com.hypixel.hytale.server.core.universe.world.worldgen.ValidatableWorldGen;
import com.hypixel.hytale.server.core.universe.world.worldgen.WorldGenLoadException;
import com.hypixel.hytale.server.core.universe.world.worldgen.provider.IWorldGenProvider;
import com.hypixel.hytale.server.core.universe.world.worldmap.IWorldMap;
import com.hypixel.hytale.server.core.universe.world.worldmap.provider.IWorldMapProvider;
import com.hypixel.hytale.sneakythrow.SneakyThrow;
import it.unimi.dsi.fastutil.objects.ObjectBooleanPair;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AssetModule extends JavaPlugin {
   public static final PluginManifest MANIFEST = PluginManifest.corePlugin(AssetModule.class).build();
   private static AssetModule instance;
   @Nullable
   private AssetMonitor assetMonitor;
   @Nonnull
   private final List<AssetPack> assetPacks = new CopyOnWriteArrayList<>();
   private final List<ObjectBooleanPair<AssetPack>> pendingAssetPacks = new ArrayList<>();
   private boolean hasSetup = false;
   private boolean hasLoaded = false;
   private final List<AssetStore<?, ?, ?>> pendingAssetStores = new CopyOnWriteArrayList<>();

   public static AssetModule get() {
      return instance;
   }

   public AssetModule(@Nonnull JavaPluginInit init) {
      super(init);
      instance = this;
   }

   @Override
   protected void setup() {
      if (Options.getOptionSet().has(Options.DISABLE_FILE_WATCHER)) {
         this.getLogger().at(Level.WARNING).log("Not running asset watcher because --disable-file-watcher was set");
      } else {
         try {
            this.assetMonitor = new AssetMonitor();
            this.getLogger().at(Level.INFO).log("Asset monitor enabled!");
         } catch (IOException var8) {
            ((HytaleLogger.Api)this.getLogger().at(Level.SEVERE).withCause(var8)).log("Failed to create asset monitor!");
         }
      }

      for (Path path : Options.getOptionSet().valuesOf(Options.ASSET_DIRECTORY)) {
         this.loadAndRegisterPack(path, false);
      }

      this.hasSetup = true;

      for (ObjectBooleanPair<AssetPack> p : this.pendingAssetPacks) {
         if (this.getAssetPack(((AssetPack)p.left()).getName()) != null) {
            if (!p.rightBoolean()) {
               throw new IllegalStateException("Asset pack with name '" + ((AssetPack)p.left()).getName() + "' already exists");
            }

            this.getLogger()
               .at(Level.WARNING)
               .log(
                  "Asset pack with name '%s' already exists, skipping registration from path: %s",
                  ((AssetPack)p.left()).getName(),
                  ((AssetPack)p.left()).getRoot()
               );
         } else {
            this.assetPacks.add((AssetPack)p.left());
         }
      }

      this.pendingAssetPacks.clear();
      this.loadPacksFromDirectory(PluginManager.MODS_PATH);

      for (Path modsPath : Options.getOptionSet().valuesOf(Options.MODS_DIRECTORIES)) {
         this.loadPacksFromDirectory(modsPath);
      }

      if (this.assetPacks.isEmpty()) {
         HytaleServer.get().shutdownServer(ShutdownReason.MISSING_ASSETS.withMessage("Failed to load any asset packs"));
      } else {
         boolean hasOutdatedPacks = false;
         String serverVersion = ManifestUtil.getVersion();

         for (AssetPack pack : this.assetPacks) {
            if (!pack.getName().equals("Hytale:Hytale")) {
               PluginManifest manifest = pack.getManifest();
               String targetServerVersion = manifest.getServerVersion();
               if (targetServerVersion == null || !targetServerVersion.equals(serverVersion)) {
                  hasOutdatedPacks = true;
                  if (targetServerVersion != null && !"*".equals(targetServerVersion)) {
                     this.getLogger()
                        .at(Level.WARNING)
                        .log(
                           "Plugin '%s' targets a different server version %s. You may encounter issues, please check for plugin updates.",
                           pack.getName(),
                           serverVersion
                        );
                  } else {
                     this.getLogger()
                        .at(Level.WARNING)
                        .log(
                           "Plugin '%s' does not specify a target server version. You may encounter issues, please check for plugin updates. This will be a hard error in the future",
                           pack.getName()
                        );
                  }
               }
            }
         }

         if (hasOutdatedPacks && System.getProperty("hytale.allow_outdated_mods") == null) {
            this.getLogger()
               .at(Level.SEVERE)
               .log("One or more asset packs are targeting an older server version. It is recommended to update these plugins to ensure compatibility.");

            try {
               if (!Constants.SINGLEPLAYER) {
                  Thread.sleep(Duration.ofSeconds(2L));
               }
            } catch (InterruptedException var9) {
               throw new RuntimeException(var9);
            }

            HytaleServer.get().getEventBus().registerGlobal(AddPlayerToWorldEvent.class, event -> {
               PlayerRef playerRef = event.getHolder().getComponent(PlayerRef.getComponentType());
               Player player = event.getHolder().getComponent(Player.getComponentType());
               if (playerRef != null && player != null) {
                  if (player.hasPermission("hytale.mods.outdated.notify")) {
                     playerRef.sendMessage(Message.translation("server.assetModule.outOfDatePacks").color(Color.RED));
                  }
               }
            });
         }

         this.getEventRegistry().register((short)-16, LoadAssetEvent.class, event -> {
            if (this.hasLoaded) {
               throw new IllegalStateException("LoadAssetEvent has already been dispatched");
            } else {
               AssetRegistry.ASSET_LOCK.writeLock().lock();

               try {
                  this.hasLoaded = true;
                  AssetRegistryLoader.preLoadAssets(event);

                  for (AssetPack packx : this.assetPacks) {
                     AssetRegistryLoader.loadAssets(event, packx);
                  }
               } finally {
                  AssetRegistry.ASSET_LOCK.writeLock().unlock();
               }
            }
         });
         this.getEventRegistry().register((short)-16, AssetPackRegisterEvent.class, event -> AssetRegistryLoader.loadAssets(null, event.getAssetPack()));
         this.getEventRegistry().register(AssetPackUnregisterEvent.class, event -> {
            for (AssetStore<?, ?, ?> assetStore : AssetRegistry.getStoreMap().values()) {
               assetStore.removeAssetPack(event.getAssetPack().getName());
            }
         });
         this.getEventRegistry().register(LoadAssetEvent.class, AssetModule::validateWorldGen);
         this.getEventRegistry().register(EventPriority.FIRST, LoadAssetEvent.class, SneakyThrow.sneakyConsumer(AssetRegistryLoader::writeSchemas));
         this.getEventRegistry().register(RegisterAssetStoreEvent.class, this::onNewStore);
         this.getEventRegistry().register(RemoveAssetStoreEvent.class, this::onRemoveStore);
         this.getEventRegistry().registerGlobal(BootEvent.class, event -> {
            StringBuilder sb = new StringBuilder("Total Loaded Assets: ");
            AssetStore[] assetStores = AssetRegistry.getStoreMap().values().toArray(AssetStore[]::new);
            Arrays.sort(assetStores, Comparator.comparingInt(o -> o.getAssetMap().getAssetCount()));

            for (int i = assetStores.length - 1; i >= 0; i--) {
               AssetStore assetStore = assetStores[i];
               String simpleName = assetStore.getAssetClass().getSimpleName();
               int assetCount = assetStore.getAssetMap().getAssetCount();
               sb.append(simpleName).append(": ").append(assetCount).append(", ");
            }

            sb.setLength(sb.length() - 2);
            this.getLogger().at(Level.INFO).log(sb.toString());
         });
         RespawnController.CODEC.register("HomeOrSpawnPoint", HomeOrSpawnPoint.class, HomeOrSpawnPoint.CODEC);
         RespawnController.CODEC.register("WorldSpawnPoint", WorldSpawnPoint.class, WorldSpawnPoint.CODEC);
         this.getCommandRegistry().registerCommand(new DroplistCommand());
      }
   }

   @Override
   protected void shutdown() {
      if (this.assetMonitor != null) {
         this.assetMonitor.shutdown();
         this.assetMonitor = null;
      }

      for (AssetPack pack : this.assetPacks) {
         if (pack.getFileSystem() != null) {
            try {
               pack.getFileSystem().close();
            } catch (IOException var4) {
               ((HytaleLogger.Api)this.getLogger().at(Level.WARNING).withCause(var4)).log("Failed to close asset pack filesystem: %s", pack.getName());
            }
         }
      }

      this.assetPacks.clear();
   }

   @Nonnull
   public AssetPack getBaseAssetPack() {
      return this.assetPacks.getFirst();
   }

   @Nonnull
   public List<AssetPack> getAssetPacks() {
      return this.assetPacks;
   }

   @Nullable
   public AssetMonitor getAssetMonitor() {
      return this.assetMonitor;
   }

   @Nullable
   public AssetPack findAssetPackForPath(Path path) {
      path = path.toAbsolutePath().normalize();

      for (AssetPack pack : this.assetPacks) {
         if (path.getFileSystem() == pack.getRoot().getFileSystem() && path.startsWith(pack.getRoot())) {
            return pack;
         }
      }

      return null;
   }

   public boolean isWithinPackSubDir(@Nonnull Path path, @Nonnull String subDir) {
      for (AssetPack pack : this.assetPacks) {
         Path packSubDir = pack.getRoot().resolve(subDir);
         if (PathUtil.isChildOf(packSubDir, path)) {
            return true;
         }
      }

      return false;
   }

   public boolean isAssetPathImmutable(@Nonnull Path path) {
      AssetPack pack = this.findAssetPackForPath(path);
      return pack != null && pack.isImmutable();
   }

   @Nullable
   private PluginManifest loadPackManifest(Path packPath) throws IOException {
      if (packPath.getFileName().toString().toLowerCase().endsWith(".zip")) {
         try (FileSystem fs = FileSystems.newFileSystem(packPath, (ClassLoader)null)) {
            Path manifestPath = fs.getPath("manifest.json");
            if (Files.exists(manifestPath)) {
               try (BufferedReader reader = Files.newBufferedReader(manifestPath, StandardCharsets.UTF_8)) {
                  char[] buffer = RawJsonReader.READ_BUFFER.get();
                  RawJsonReader rawJsonReader = new RawJsonReader(reader, buffer);
                  ExtraInfo extraInfo = ExtraInfo.THREAD_LOCAL.get();
                  PluginManifest manifest = PluginManifest.CODEC.decodeJson(rawJsonReader, extraInfo);
                  extraInfo.getValidationResults().logOrThrowValidatorExceptions(this.getLogger());
                  return manifest;
               }
            }

            return null;
         }
      } else if (Files.isDirectory(packPath)) {
         Path manifestPath = packPath.resolve("manifest.json");
         if (Files.exists(manifestPath)) {
            PluginManifest manifest;
            try (FileReader reader = new FileReader(manifestPath.toFile(), StandardCharsets.UTF_8)) {
               char[] buffer = RawJsonReader.READ_BUFFER.get();
               RawJsonReader rawJsonReader = new RawJsonReader(reader, buffer);
               ExtraInfo extraInfo = ExtraInfo.THREAD_LOCAL.get();
               PluginManifest manifestx = PluginManifest.CODEC.decodeJson(rawJsonReader, extraInfo);
               extraInfo.getValidationResults().logOrThrowValidatorExceptions(this.getLogger());
               manifest = manifestx;
            }

            return manifest;
         }
      }

      return null;
   }

   private void loadPacksFromDirectory(Path modsPath) {
      if (Files.isDirectory(modsPath)) {
         this.getLogger().at(Level.INFO).log("Loading packs from directory: %s", modsPath);

         try (DirectoryStream<Path> stream = Files.newDirectoryStream(modsPath)) {
            for (Path packPath : stream) {
               if (packPath.getFileName() != null && !packPath.getFileName().toString().toLowerCase().endsWith(".jar")) {
                  this.loadAndRegisterPack(packPath, true);
               }
            }
         } catch (IOException var7) {
            ((HytaleLogger.Api)this.getLogger().at(Level.SEVERE).withCause(var7)).log("Failed to load mods from: %s", modsPath);
         }
      }
   }

   private void loadAndRegisterPack(Path packPath, boolean isExternal) {
      PluginManifest manifest;
      try {
         manifest = this.loadPackManifest(packPath);
         if (manifest == null) {
            this.getLogger().at(Level.WARNING).log("Skipping pack at %s: missing or invalid manifest.json", packPath.getFileName());
            return;
         }
      } catch (Exception var9) {
         ((HytaleLogger.Api)this.getLogger().at(Level.WARNING).withCause(var9)).log("Failed to load manifest for pack at %s", packPath);
         return;
      }

      PluginIdentifier packIdentifier = new PluginIdentifier(manifest);
      HytaleServerConfig serverConfig = HytaleServer.get().getConfig();
      ModConfig modConfig = serverConfig.getModConfig().get(packIdentifier);
      boolean enabled;
      if (modConfig != null && modConfig.getEnabled() != null) {
         enabled = modConfig.getEnabled();
      } else {
         enabled = !manifest.isDisabledByDefault() && (!isExternal || serverConfig.getDefaultModsEnabled());
      }

      String packId = packIdentifier.toString();
      if (enabled) {
         this.registerPack(packId, packPath, manifest, false);
         this.getLogger().at(Level.INFO).log("Loaded pack: %s from %s", packId, packPath.getFileName());
      } else {
         this.getLogger().at(Level.INFO).log("Skipped disabled pack: %s", packId);
      }
   }

   public void registerPack(@Nonnull String name, @Nonnull Path path, @Nonnull PluginManifest manifest, boolean ignoreIfExists) {
      Path absolutePath = path.toAbsolutePath().normalize();
      Path packLocation = absolutePath;
      FileSystem fileSystem = null;
      boolean isImmutable = false;
      String lowerFileName = absolutePath.getFileName().toString().toLowerCase();
      if (!lowerFileName.endsWith(".zip") && !lowerFileName.endsWith(".jar")) {
         isImmutable = Files.isRegularFile(absolutePath.resolve("CommonAssetsIndex.hashes"));
      } else {
         try {
            fileSystem = FileSystems.newFileSystem(absolutePath, (ClassLoader)null);
            absolutePath = fileSystem.getPath("").toAbsolutePath().normalize();
            isImmutable = true;
         } catch (IOException var14) {
            throw SneakyThrow.sneakyThrow(var14);
         }
      }

      AssetPack pack = new AssetPack(packLocation, name, absolutePath, fileSystem, isImmutable, manifest);
      if (!this.hasSetup) {
         this.pendingAssetPacks.add(ObjectBooleanPair.of(pack, ignoreIfExists));
      } else if (this.getAssetPack(name) != null) {
         if (ignoreIfExists) {
            this.getLogger().at(Level.WARNING).log("Asset pack with name '%s' already exists, skipping registration from path: %s", name, path);
         } else {
            throw new IllegalStateException("Asset pack with name '" + name + "' already exists");
         }
      } else {
         this.assetPacks.add(pack);
         AssetRegistry.ASSET_LOCK.writeLock().lock();

         try {
            if (this.hasLoaded) {
               HytaleServer.get()
                  .getEventBus()
                  .<Void, AssetPackRegisterEvent>dispatchFor(AssetPackRegisterEvent.class)
                  .dispatch(new AssetPackRegisterEvent(pack));
               return;
            }
         } finally {
            AssetRegistry.ASSET_LOCK.writeLock().unlock();
         }
      }
   }

   public void unregisterPack(@Nonnull String name) {
      AssetPack pack = this.getAssetPack(name);
      if (pack == null) {
         this.getLogger().at(Level.WARNING).log("Tried to unregister non-existent asset pack: %s", name);
      } else {
         this.assetPacks.remove(pack);
         if (pack.getFileSystem() != null) {
            try {
               pack.getFileSystem().close();
            } catch (IOException var8) {
               throw SneakyThrow.sneakyThrow(var8);
            }
         }

         AssetRegistry.ASSET_LOCK.writeLock().lock();

         try {
            HytaleServer.get()
               .getEventBus()
               .<Void, AssetPackUnregisterEvent>dispatchFor(AssetPackUnregisterEvent.class)
               .dispatch(new AssetPackUnregisterEvent(pack));
         } finally {
            AssetRegistry.ASSET_LOCK.writeLock().unlock();
         }
      }
   }

   public AssetPack getAssetPack(@Nonnull String name) {
      for (AssetPack pack : this.assetPacks) {
         if (name.equals(pack.getName())) {
            return pack;
         }
      }

      return null;
   }

   private void onRemoveStore(@Nonnull RemoveAssetStoreEvent event) {
      AssetStore<?, ? extends JsonAssetWithMap<?, ? extends AssetMap<?, ?>>, ? extends AssetMap<?, ? extends JsonAssetWithMap<?, ?>>> assetStore = (AssetStore<?, ? extends JsonAssetWithMap<?, ? extends AssetMap<?, ?>>, ? extends AssetMap<?, ? extends JsonAssetWithMap<?, ?>>>)event.getAssetStore();
      String path = assetStore.getPath();
      if (path != null) {
         for (AssetPack pack : this.assetPacks) {
            if (!pack.isImmutable()) {
               Path assetsPath = pack.getRoot().resolve("Server").resolve(path);
               if (Files.isDirectory(assetsPath)) {
                  assetStore.removeFileMonitor(assetsPath);
               }
            }
         }
      }
   }

   private void onNewStore(@Nonnull RegisterAssetStoreEvent event) {
      if (AssetRegistry.HAS_INIT) {
         this.pendingAssetStores.add(event.getAssetStore());
      }
   }

   public void initPendingStores() {
      for (int i = 0; i < this.pendingAssetStores.size(); i++) {
         this.initStore(this.pendingAssetStores.get(i));
      }

      this.pendingAssetStores.clear();
   }

   private void initStore(@Nonnull AssetStore<?, ?, ?> assetStore) {
      AssetRegistry.ASSET_LOCK.writeLock().lock();

      try {
         List<?> preAddedAssets = assetStore.getPreAddedAssets();
         if (preAddedAssets != null && !preAddedAssets.isEmpty()) {
            AssetLoadResult loadResult = assetStore.loadAssets("Hytale:Hytale", preAddedAssets);
            if (loadResult.hasFailed()) {
               throw new RuntimeException("Failed to load asset store: " + assetStore.getAssetClass());
            }
         }

         for (AssetPack pack : this.assetPacks) {
            Path serverAssetDirectory = pack.getRoot().resolve("Server");
            String path = assetStore.getPath();
            if (path != null) {
               Path assetsPath = serverAssetDirectory.resolve(path);
               if (Files.isDirectory(assetsPath)) {
                  AssetLoadResult<?, ? extends JsonAssetWithMap<?, ? extends AssetMap<?, ?>>> loadResult = (AssetLoadResult<?, ? extends JsonAssetWithMap<?, ? extends AssetMap<?, ?>>>)assetStore.loadAssetsFromDirectory(
                     pack.getName(), assetsPath
                  );
                  if (loadResult.hasFailed()) {
                     throw new RuntimeException("Failed to load asset store: " + assetStore.getAssetClass());
                  }
               } else {
                  this.getLogger()
                     .at(Level.SEVERE)
                     .log("Path for %s isn't a directory or doesn't exist: %s", assetStore.getAssetClass().getSimpleName(), assetsPath);
               }
            }

            assetStore.validateCodecDefaults();
            if (path != null) {
               Path assetsPath = serverAssetDirectory.resolve(path);
               if (Files.isDirectory(assetsPath)) {
                  assetStore.addFileMonitor(pack.getName(), assetsPath);
               }
            }
         }
      } catch (IOException var12) {
         throw SneakyThrow.sneakyThrow(var12);
      } finally {
         AssetRegistry.ASSET_LOCK.writeLock().unlock();
      }
   }

   private static void validateWorldGen(@Nonnull LoadAssetEvent event) {
      if (Options.getOptionSet().has(Options.VALIDATE_WORLD_GEN)) {
         long start = System.nanoTime();

         try {
            IWorldGenProvider provider = IWorldGenProvider.CODEC.getDefault();
            IWorldGen generator = provider.getGenerator();
            generator.getDefaultSpawnProvider(0);
            if (generator instanceof ValidatableWorldGen) {
               boolean valid = ((ValidatableWorldGen)generator).validate();
               if (!valid) {
                  event.failed(true, "failed to validate world gen");
               }
            }

            if (generator instanceof IWorldMapProvider worldMapProvider) {
               IWorldMap worldMap = worldMapProvider.getGenerator(null);
               worldMap.getWorldMapSettings();
            }
         } catch (WorldGenLoadException var7) {
            ((HytaleLogger.Api)HytaleLogger.getLogger().at(Level.SEVERE).withCause(var7)).log("Failed to load default world gen!");
            HytaleLogger.getLogger().at(Level.SEVERE).log("\n" + var7.getTraceMessage("\n"));
            event.failed(true, "failed to validate world gen: " + var7.getTraceMessage(" -> "));
         } catch (Throwable var8) {
            ((HytaleLogger.Api)HytaleLogger.getLogger().at(Level.SEVERE).withCause(var8)).log("Failed to load default world gen!");
            event.failed(true, "failed to validate world gen");
         }

         HytaleLogger.getLogger()
            .at(Level.INFO)
            .log(
               "Validate world gen phase completed! Boot time %s, Took %s",
               FormatUtil.nanosToString(System.nanoTime() - event.getBootStart()),
               FormatUtil.nanosToString(System.nanoTime() - start)
            );
      }
   }
}
