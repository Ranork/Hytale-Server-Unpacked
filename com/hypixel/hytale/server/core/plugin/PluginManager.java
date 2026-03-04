package com.hypixel.hytale.server.core.plugin;

import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.util.RawJsonReader;
import com.hypixel.hytale.common.plugin.PluginIdentifier;
import com.hypixel.hytale.common.plugin.PluginManifest;
import com.hypixel.hytale.common.semver.SemverRange;
import com.hypixel.hytale.common.util.java.ManifestUtil;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.event.IEventDispatcher;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.metrics.MetricsRegistry;
import com.hypixel.hytale.server.core.Constants;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.HytaleServerConfig;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.Options;
import com.hypixel.hytale.server.core.ShutdownReason;
import com.hypixel.hytale.server.core.asset.AssetModule;
import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.config.ModConfig;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.AddPlayerToWorldEvent;
import com.hypixel.hytale.server.core.plugin.commands.PluginCommand;
import com.hypixel.hytale.server.core.plugin.event.PluginSetupEvent;
import com.hypixel.hytale.server.core.plugin.pending.PendingLoadJavaPlugin;
import com.hypixel.hytale.server.core.plugin.pending.PendingLoadPlugin;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PluginManager {
   @Nonnull
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   @Nonnull
   public static final Path MODS_PATH = Path.of("mods");
   @Nonnull
   public static final MetricsRegistry<PluginManager> METRICS_REGISTRY = new MetricsRegistry<PluginManager>()
      .register(
         "Plugins", pluginManager -> pluginManager.getPlugins().toArray(PluginBase[]::new), new ArrayCodec<>(PluginBase.METRICS_REGISTRY, PluginBase[]::new)
      );
   private static PluginManager instance;
   @Nonnull
   private final PluginClassLoader corePluginClassLoader = new PluginClassLoader(this, null, true);
   @Nonnull
   private final List<PendingLoadPlugin> corePlugins = new ObjectArrayList();
   private final PluginManager.PluginBridgeClassLoader bridgeClassLoader = new PluginManager.PluginBridgeClassLoader(this, PluginManager.class.getClassLoader());
   private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
   private final Map<PluginIdentifier, PluginBase> plugins = new Object2ObjectLinkedOpenHashMap();
   private final Map<Path, PluginClassLoader> classLoaders = new ConcurrentHashMap<>();
   private boolean hasOutdatedPlugins = false;
   private final boolean loadExternalPlugins = true;
   @Nonnull
   private PluginState state = PluginState.NONE;
   @Nullable
   private List<PendingLoadPlugin> loadOrder;
   @Nullable
   private Map<PluginIdentifier, PluginBase> loading;
   @Nonnull
   private final Map<PluginIdentifier, PluginManifest> availablePlugins = new Object2ObjectLinkedOpenHashMap();
   public PluginListPageManager pluginListPageManager;
   private ComponentType<EntityStore, PluginListPageManager.SessionSettings> sessionSettingsComponentType;

   public static PluginManager get() {
      return instance;
   }

   public PluginManager() {
      instance = this;
      this.pluginListPageManager = new PluginListPageManager();
   }

   public void registerCorePlugin(@Nonnull PluginManifest builder) {
      this.corePlugins.add(new PendingLoadJavaPlugin(null, builder, this.corePluginClassLoader));
   }

   private boolean canLoadOnBoot(@Nonnull PendingLoadPlugin plugin) {
      PluginIdentifier identifier = plugin.getIdentifier();
      PluginManifest manifest = plugin.getManifest();
      ModConfig modConfig = HytaleServer.get().getConfig().getModConfig().get(identifier);
      boolean enabled;
      if (modConfig != null && modConfig.getEnabled() != null) {
         enabled = modConfig.getEnabled();
      } else {
         HytaleServerConfig serverConfig = HytaleServer.get().getConfig();
         enabled = !manifest.isDisabledByDefault() && (plugin.isInServerClassPath() || serverConfig.getDefaultModsEnabled());
      }

      if (enabled) {
         return true;
      } else {
         LOGGER.at(Level.WARNING).log("Skipping mod %s (Disabled by server config)", identifier);
         return false;
      }
   }

   public void setup() {
      if (this.state != PluginState.NONE) {
         throw new IllegalStateException("Expected PluginState.NONE but found " + this.state);
      } else {
         this.state = PluginState.SETUP;
         CommandManager.get().registerSystemCommand(new PluginCommand());
         this.sessionSettingsComponentType = EntityStore.REGISTRY
            .registerComponent(PluginListPageManager.SessionSettings.class, PluginListPageManager.SessionSettings::new);
         HashMap<PluginIdentifier, PendingLoadPlugin> pending = new HashMap<>();
         this.availablePlugins.clear();
         LOGGER.at(Level.INFO).log("Loading pending core plugins!");

         for (int i = 0; i < this.corePlugins.size(); i++) {
            PendingLoadPlugin plugin = this.corePlugins.get(i);
            LOGGER.at(Level.INFO).log("- %s", plugin.getIdentifier());
            if (this.canLoadOnBoot(plugin)) {
               loadPendingPlugin(pending, plugin);
            } else {
               this.availablePlugins.put(plugin.getIdentifier(), plugin.getManifest());
            }
         }

         Path self;
         try {
            self = Paths.get(PluginManager.class.getProtectionDomain().getCodeSource().getLocation().toURI());
         } catch (URISyntaxException var30) {
            throw new RuntimeException(var30);
         }

         this.loadPluginsFromDirectory(pending, self.getParent().resolve("builtin"), false, this.availablePlugins);
         this.loadPluginsInClasspath(pending, this.availablePlugins);
         this.loadPluginsFromDirectory(pending, MODS_PATH, !Options.getOptionSet().has(Options.BARE), this.availablePlugins);

         for (Path modsPath : Options.getOptionSet().valuesOf(Options.MODS_DIRECTORIES)) {
            this.loadPluginsFromDirectory(pending, modsPath, false, this.availablePlugins);
         }

         this.lock.readLock().lock();

         try {
            this.plugins.keySet().forEach(key -> {
               pending.remove(key);
               LOGGER.at(Level.WARNING).log("Skipping loading of %s because it is already loaded!", key);
            });
            Iterator<PendingLoadPlugin> iterator = pending.values().iterator();

            while (iterator.hasNext()) {
               PendingLoadPlugin pendingLoadPlugin = iterator.next();

               try {
                  this.validatePluginDeps(pendingLoadPlugin, pending);
               } catch (MissingPluginDependencyException var29) {
                  LOGGER.at(Level.SEVERE).log(var29.getMessage());
                  iterator.remove();
               }
            }
         } finally {
            this.lock.readLock().unlock();
         }

         if (this.hasOutdatedPlugins && System.getProperty("hytale.allow_outdated_mods") == null) {
            LOGGER.at(Level.SEVERE)
               .log("One or more plugins are targeting a different server version. It is recommended to update these plugins to ensure compatibility.");

            try {
               if (!Constants.SINGLEPLAYER) {
                  Thread.sleep(Duration.ofSeconds(2L));
               }
            } catch (InterruptedException var32) {
               throw new RuntimeException(var32);
            }

            HytaleServer.get().getEventBus().registerGlobal(AddPlayerToWorldEvent.class, event -> {
               PlayerRef playerRef = event.getHolder().getComponent(PlayerRef.getComponentType());
               Player player = event.getHolder().getComponent(Player.getComponentType());
               if (playerRef != null && player != null) {
                  if (player.hasPermission("hytale.mods.outdated.notify")) {
                     playerRef.sendMessage(Message.translation("server.pluginManager.outOfDatePlugins").color(Color.RED));
                  }
               }
            });
         }

         this.loadOrder = PendingLoadPlugin.calculateLoadOrder(pending);
         this.loading = new Object2ObjectOpenHashMap();
         pending.forEach((identifier, pendingLoad) -> this.availablePlugins.put(identifier, pendingLoad.getManifest()));
         ObjectArrayList<CompletableFuture<Void>> preLoadFutures = new ObjectArrayList();
         ObjectArrayList<PluginIdentifier> failedBootPlugins = new ObjectArrayList();
         this.lock.writeLock().lock();

         try {
            LOGGER.at(Level.FINE).log("Loading plugins!");

            for (PendingLoadPlugin pendingLoadPlugin : this.loadOrder) {
               LOGGER.at(Level.FINE).log("- %s", pendingLoadPlugin.getIdentifier());

               try {
                  PluginBase plugin = pendingLoadPlugin.load();
                  this.plugins.put(plugin.getIdentifier(), plugin);
                  this.loading.put(plugin.getIdentifier(), plugin);
                  CompletableFuture<Void> future = plugin.preLoad();
                  if (future != null) {
                     preLoadFutures.add(future);
                  }
               } catch (ClassNotFoundException var26) {
                  ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(var26))
                     .log("Failed to load plugin %s. Failed to find main class!", pendingLoadPlugin.getPath());
                  failedBootPlugins.add(pendingLoadPlugin.getIdentifier());
               } catch (NoSuchMethodException var27) {
                  ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(var27))
                     .log("Failed to load plugin %s. Requires default constructor!", pendingLoadPlugin.getPath());
                  failedBootPlugins.add(pendingLoadPlugin.getIdentifier());
               } catch (Throwable var28) {
                  ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(var28)).log("Failed to load plugin %s", pendingLoadPlugin.getPath());
                  failedBootPlugins.add(pendingLoadPlugin.getIdentifier());
               }
            }
         } finally {
            this.lock.writeLock().unlock();
         }

         if (!failedBootPlugins.isEmpty() && !Constants.shouldSkipModValidation()) {
            StringBuilder sb = new StringBuilder("Failed to boot the following plugins:\n");
            ObjectListIterator var44 = failedBootPlugins.iterator();

            while (var44.hasNext()) {
               PluginIdentifier failed = (PluginIdentifier)var44.next();
               sb.append(" - ").append(failed).append('\n');
            }

            HytaleServer.get().shutdownServer(ShutdownReason.MOD_ERROR.withMessage(sb.toString().trim()));
         } else {
            CompletableFuture.allOf((CompletableFuture<?>[])preLoadFutures.toArray(CompletableFuture[]::new)).join();
            boolean hasFailed = false;

            for (PendingLoadPlugin pendingPlugin : this.loadOrder) {
               PluginBase plugin = this.loading.get(pendingPlugin.getIdentifier());
               if (plugin != null && !this.setup(plugin)) {
                  hasFailed = true;
               }
            }

            if (!Constants.shouldSkipModValidation() && hasFailed) {
               StringBuilder sb = new StringBuilder("Failed to setup the following plugins:\n");
               this.collectFailedPlugins(sb);
               HytaleServer.get().shutdownServer(ShutdownReason.MOD_ERROR.withMessage(sb.toString().trim()));
            } else {
               this.loading.values().removeIf(v -> v.getState().isInactive());
            }
         }
      }
   }

   public void start() {
      if (this.state != PluginState.SETUP) {
         throw new IllegalStateException("Expected PluginState.SETUP but found " + this.state);
      } else {
         this.state = PluginState.START;
         boolean hasFailed = false;

         for (PendingLoadPlugin pendingPlugin : this.loadOrder) {
            PluginBase plugin = this.loading.get(pendingPlugin.getIdentifier());
            if (plugin != null && !this.start(plugin)) {
               hasFailed = true;
            }
         }

         StringBuilder sb = new StringBuilder();

         for (Entry<PluginIdentifier, ModConfig> entry : HytaleServer.get().getConfig().getModConfig().entrySet()) {
            PluginIdentifier identifier = entry.getKey();
            ModConfig modConfig = entry.getValue();
            SemverRange requiredVersion = modConfig.getRequiredVersion();
            if (requiredVersion != null && !this.hasPlugin(identifier, requiredVersion)) {
               sb.append(String.format("%s, Version: %s\n", identifier, modConfig));
            }
         }

         if (!sb.isEmpty()) {
            String msg = "Failed to start server! Missing Mods:\n" + sb;
            LOGGER.at(Level.SEVERE).log(msg);
            HytaleServer.get().shutdownServer(ShutdownReason.MISSING_REQUIRED_PLUGIN.withMessage(msg));
         } else if (hasFailed && !Constants.shouldSkipModValidation()) {
            sb = new StringBuilder("Failed to start the following plugins:\n");
            this.collectFailedPlugins(sb);
            HytaleServer.get().shutdownServer(ShutdownReason.MOD_ERROR.withMessage(sb.toString().trim()));
         } else {
            this.loadOrder = null;
            this.loading = null;
         }
      }
   }

   private void collectFailedPlugins(StringBuilder sb) {
      if (this.loading != null) {
         for (Entry<PluginIdentifier, PluginBase> failed : this.loading.entrySet()) {
            if (failed.getValue().getState() == PluginState.FAILED) {
               Throwable reasonThrowable = failed.getValue().getFailureCause();
               String reason = reasonThrowable != null ? reasonThrowable.toString() : "Unknown";
               sb.append(" - ").append(failed.getKey()).append(": ").append(reason).append('\n');
            }
         }
      }
   }

   public void shutdown() {
      this.state = PluginState.SHUTDOWN;
      LOGGER.at(Level.INFO).log("Saving plugins config...");
      this.lock.writeLock().lock();

      try {
         List<PluginBase> list = new ObjectArrayList(this.plugins.values());

         for (int i = list.size() - 1; i >= 0; i--) {
            PluginBase plugin = list.get(i);
            if (plugin.getState() == PluginState.ENABLED) {
               LOGGER.at(Level.FINE).log("Shutting down %s %s", plugin.getType().getDisplayName(), plugin.getIdentifier());
               plugin.shutdown0(true);
               HytaleServer.get().doneStop(plugin);
               LOGGER.at(Level.INFO).log("Shut down plugin %s", plugin.getIdentifier());
            }
         }

         this.plugins.clear();
      } finally {
         this.lock.writeLock().unlock();
      }
   }

   @Nonnull
   public PluginState getState() {
      return this.state;
   }

   @Nonnull
   public PluginManager.PluginBridgeClassLoader getBridgeClassLoader() {
      return this.bridgeClassLoader;
   }

   private void validatePluginDeps(@Nonnull PendingLoadPlugin pendingLoadPlugin, @Nullable Map<PluginIdentifier, PendingLoadPlugin> pending) {
      String serverVersion = ManifestUtil.getVersion();
      if (!pendingLoadPlugin.getManifest().getGroup().equals("Hytale")) {
         String targetServerVersion = pendingLoadPlugin.getManifest().getServerVersion();
         if (targetServerVersion == null || serverVersion != null && !targetServerVersion.equals(serverVersion)) {
            if (targetServerVersion != null && !"*".equals(targetServerVersion)) {
               LOGGER.at(Level.WARNING)
                  .log(
                     "Plugin '%s' targets a different server version %s. You may encounter issues, please check for plugin updates.",
                     pendingLoadPlugin.getIdentifier(),
                     serverVersion
                  );
            } else {
               LOGGER.at(Level.WARNING)
                  .log(
                     "Plugin '%s' does not specify a target server version. You may encounter issues, please check for plugin updates. This will be a hard error in the future",
                     pendingLoadPlugin.getIdentifier()
                  );
            }

            this.hasOutdatedPlugins = true;
         }
      }

      for (Entry<PluginIdentifier, SemverRange> entry : pendingLoadPlugin.getManifest().getDependencies().entrySet()) {
         PluginIdentifier identifier = entry.getKey();
         PluginManifest dependency = null;
         if (pending != null) {
            PendingLoadPlugin pendingDependency = pending.get(identifier);
            if (pendingDependency != null) {
               dependency = pendingDependency.getManifest();
            }
         }

         if (dependency == null) {
            PluginBase loadedBase = this.plugins.get(identifier);
            if (loadedBase != null) {
               dependency = loadedBase.getManifest();
            }
         }

         if (dependency == null) {
            throw new MissingPluginDependencyException(
               String.format("Failed to load '%s' because the dependency '%s' could not be found!", pendingLoadPlugin.getIdentifier(), identifier)
            );
         }

         SemverRange expectedVersion = entry.getValue();
         if (!dependency.getVersion().satisfies(expectedVersion)) {
            throw new MissingPluginDependencyException(
               String.format(
                  "Failed to load '%s' because version of dependency '%s'(%s) does not satisfy '%s'!",
                  pendingLoadPlugin.getIdentifier(),
                  identifier,
                  dependency.getVersion(),
                  expectedVersion
               )
            );
         }
      }
   }

   private void loadPluginsFromDirectory(
      @Nonnull Map<PluginIdentifier, PendingLoadPlugin> pending,
      @Nonnull Path path,
      boolean create,
      @Nonnull Map<PluginIdentifier, PluginManifest> bootRejectMap
   ) {
      if (!Files.isDirectory(path)) {
         if (create) {
            try {
               Files.createDirectories(path);
            } catch (IOException var9) {
               ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(var9)).log("Failed to create directory: %s", path);
            }
         }
      } else {
         LOGGER.at(Level.INFO).log("Loading pending plugins from directory: " + path);

         try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
            for (Path file : stream) {
               if (Files.isRegularFile(file) && file.getFileName().toString().toLowerCase().endsWith(".jar")) {
                  PendingLoadJavaPlugin plugin = this.loadPendingJavaPlugin(file);
                  if (plugin != null) {
                     assert plugin.getPath() != null;

                     LOGGER.at(Level.INFO).log("- %s from path %s", plugin.getIdentifier(), path.relativize(plugin.getPath()));
                     if (this.canLoadOnBoot(plugin)) {
                        loadPendingPlugin(pending, plugin);
                     } else {
                        bootRejectMap.put(plugin.getIdentifier(), plugin.getManifest());
                     }
                  }
               }
            }
         } catch (IOException var12) {
            ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(var12)).log("Failed to find pending plugins from: %s", path);
         }
      }
   }

   @Nullable
   private PendingLoadJavaPlugin loadPendingJavaPlugin(@Nonnull Path file) {
      try {
         PendingLoadJavaPlugin var22;
         try (FileSystem fs = FileSystems.newFileSystem(file)) {
            Path resource = fs.getPath("manifest.json");
            if (!Files.exists(resource)) {
               LOGGER.at(Level.SEVERE).log("Failed to load pending plugin from '%s'. Failed to load manifest file!", file.toString());
               return null;
            }

            PluginManifest manifest;
            try (
               InputStream stream = Files.newInputStream(resource);
               InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
            ) {
               char[] buffer = RawJsonReader.READ_BUFFER.get();
               RawJsonReader rawJsonReader = new RawJsonReader(reader, buffer);
               ExtraInfo extraInfo = ExtraInfo.THREAD_LOCAL.get();
               manifest = PluginManifest.CODEC.decodeJson(rawJsonReader, extraInfo);
               if (manifest == null) {
                  LOGGER.at(Level.SEVERE).log("Failed to load pending plugin from '%s'. Failed to decode manifest file!", file.toString());
                  return null;
               }

               extraInfo.getValidationResults().logOrThrowValidatorExceptions(LOGGER);
            }

            URL url = file.toUri().toURL();
            PluginClassLoader pluginClassLoader = this.classLoaders
               .computeIfAbsent(file, path -> new PluginClassLoader(this, new PluginIdentifier(manifest), false, url));
            var22 = new PendingLoadJavaPlugin(file, manifest, pluginClassLoader);
         }

         return var22;
      } catch (MalformedURLException var17) {
         ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(var17))
            .log("Failed to load pending plugin from '%s'. Failed to create URLClassLoader!", file.toString());
      } catch (IOException var18) {
         ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(var18)).log("Failed to load pending plugin %s. Failed to load manifest file!", file.toString());
      }

      return null;
   }

   private void loadPluginsInClasspath(
      @Nonnull Map<PluginIdentifier, PendingLoadPlugin> pending, @Nonnull Map<PluginIdentifier, PluginManifest> rejectedBootList
   ) {
      LOGGER.at(Level.INFO).log("Loading pending classpath plugins!");

      try {
         URI uri = PluginManager.class.getProtectionDomain().getCodeSource().getLocation().toURI();
         ClassLoader classLoader = PluginManager.class.getClassLoader();

         try {
            for (URL manifestUrl : new HashSet<>(Collections.list(classLoader.getResources("manifest.json")))) {
               URLConnection connection = manifestUrl.openConnection();

               try (
                  InputStream stream = connection.getInputStream();
                  InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
               ) {
                  char[] buffer = RawJsonReader.READ_BUFFER.get();
                  RawJsonReader rawJsonReader = new RawJsonReader(reader, buffer);
                  ExtraInfo extraInfo = ExtraInfo.THREAD_LOCAL.get();
                  PluginManifest manifest = PluginManifest.CODEC.decodeJson(rawJsonReader, extraInfo);
                  extraInfo.getValidationResults().logOrThrowValidatorExceptions(LOGGER);
                  if (manifest != null) {
                     PendingLoadJavaPlugin plugin;
                     if (connection instanceof JarURLConnection jarURLConnection) {
                        URL classpathUrl = jarURLConnection.getJarFileURL();
                        Path path = Path.of(classpathUrl.toURI());
                        PluginClassLoader pluginClassLoader = this.classLoaders
                           .computeIfAbsent(path, f -> new PluginClassLoader(this, new PluginIdentifier(manifest), true, classpathUrl));
                        plugin = new PendingLoadJavaPlugin(path, manifest, pluginClassLoader);
                     } else {
                        URI pluginUri = manifestUrl.toURI().resolve(".");
                        Path path = Paths.get(pluginUri);
                        URL classpathUrl = pluginUri.toURL();
                        PluginClassLoader pluginClassLoader = this.classLoaders
                           .computeIfAbsent(path, f -> new PluginClassLoader(this, new PluginIdentifier(manifest), true, classpathUrl));
                        plugin = new PendingLoadJavaPlugin(path, manifest, pluginClassLoader);
                     }

                     LOGGER.at(Level.INFO).log("- %s", plugin.getIdentifier());
                     if (this.canLoadOnBoot(plugin)) {
                        loadPendingPlugin(pending, plugin);
                     } else {
                        rejectedBootList.put(plugin.getIdentifier(), plugin.getManifest());
                     }
                     continue;
                  }

                  LOGGER.at(Level.SEVERE).log("Failed to load pending plugin from '%s'. Failed to decode manifest file!", manifestUrl);
               }

               return;
            }

            URL manifestsUrl = classLoader.getResource("manifests.json");
            if (manifestsUrl != null) {
               try (
                  InputStream stream = manifestsUrl.openStream();
                  InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
               ) {
                  char[] buffer = RawJsonReader.READ_BUFFER.get();
                  RawJsonReader rawJsonReader = new RawJsonReader(reader, buffer);
                  ExtraInfo extraInfo = ExtraInfo.THREAD_LOCAL.get();
                  PluginManifest[] manifests = PluginManifest.ARRAY_CODEC.decodeJson(rawJsonReader, extraInfo);
                  extraInfo.getValidationResults().logOrThrowValidatorExceptions(LOGGER);
                  URL url = uri.toURL();
                  Path path = Paths.get(uri);
                  PluginClassLoader pluginClassLoader = this.classLoaders.computeIfAbsent(path, f -> new PluginClassLoader(this, null, true, url));

                  for (PluginManifest manifest : manifests) {
                     PendingLoadJavaPlugin pluginx = new PendingLoadJavaPlugin(path, manifest, pluginClassLoader);
                     LOGGER.at(Level.INFO).log("- %s", pluginx.getIdentifier());
                     if (this.canLoadOnBoot(pluginx)) {
                        loadPendingPlugin(pending, pluginx);
                     } else {
                        rejectedBootList.put(pluginx.getIdentifier(), pluginx.getManifest());
                     }
                  }
               }
            }
         } catch (IOException var29) {
            ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(var29))
               .log("Failed to load pending classpath plugin from '%s'. Failed to load manifest file!", uri.toString());
         }
      } catch (URISyntaxException var30) {
         ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(var30)).log("Failed to get jar path!");
      }
   }

   @Nonnull
   public List<PluginBase> getPlugins() {
      this.lock.readLock().lock();

      ObjectArrayList var1;
      try {
         var1 = new ObjectArrayList(this.plugins.values());
      } finally {
         this.lock.readLock().unlock();
      }

      return var1;
   }

   @Nullable
   public PluginBase getPlugin(PluginIdentifier identifier) {
      this.lock.readLock().lock();

      PluginBase var2;
      try {
         var2 = this.plugins.get(identifier);
      } finally {
         this.lock.readLock().unlock();
      }

      return var2;
   }

   public boolean hasPlugin(PluginIdentifier identifier, @Nonnull SemverRange range) {
      PluginBase plugin = this.getPlugin(identifier);
      return plugin != null && plugin.getManifest().getVersion().satisfies(range);
   }

   public boolean reload(@Nonnull PluginIdentifier identifier) {
      boolean result = this.unload(identifier) && this.load(identifier);
      this.pluginListPageManager.notifyPluginChange(this.plugins, identifier);
      return result;
   }

   public boolean unload(@Nonnull PluginIdentifier identifier) {
      this.lock.writeLock().lock();
      AssetRegistry.ASSET_LOCK.writeLock().lock();

      boolean var7;
      try {
         PluginBase plugin = this.plugins.get(identifier);
         if (plugin.getState() != PluginState.ENABLED) {
            this.pluginListPageManager.notifyPluginChange(this.plugins, identifier);
            return false;
         }

         plugin.shutdown0(false);
         HytaleServer.get().doneStop(plugin);
         this.plugins.remove(identifier);
         if (plugin instanceof JavaPlugin javaPlugin) {
            this.unloadJavaPlugin(javaPlugin);
         }

         this.pluginListPageManager.notifyPluginChange(this.plugins, identifier);
         var7 = true;
      } finally {
         AssetRegistry.ASSET_LOCK.writeLock().unlock();
         this.lock.writeLock().unlock();
      }

      return var7;
   }

   protected void unloadJavaPlugin(JavaPlugin plugin) {
      Path path = plugin.getFile();
      PluginClassLoader classLoader = this.classLoaders.remove(path);
      if (classLoader != null) {
         try {
            classLoader.close();
         } catch (IOException var5) {
            LOGGER.at(Level.SEVERE).log("Failed to close Class Loader for JavaPlugin %s", plugin.getIdentifier());
         }
      }
   }

   public boolean load(@Nonnull PluginIdentifier identifier) {
      this.lock.readLock().lock();

      try {
         PluginBase plugin = this.plugins.get(identifier);
         if (plugin != null) {
            this.pluginListPageManager.notifyPluginChange(this.plugins, identifier);
            return false;
         }
      } finally {
         this.lock.readLock().unlock();
      }

      boolean var7 = this.findAndLoadPlugin(identifier);
      this.pluginListPageManager.notifyPluginChange(this.plugins, identifier);
      return var7;
   }

   private boolean findAndLoadPlugin(PluginIdentifier identifier) {
      for (PendingLoadPlugin plugin : this.corePlugins) {
         if (plugin.getIdentifier().equals(identifier)) {
            return this.load(plugin);
         }
      }

      try {
         URI uri = PluginManager.class.getProtectionDomain().getCodeSource().getLocation().toURI();
         ClassLoader classLoader = PluginManager.class.getClassLoader();

         for (URL manifestUrl : new HashSet<>(Collections.list(classLoader.getResources("manifest.json")))) {
            boolean manifest;
            try (
               InputStream stream = manifestUrl.openStream();
               InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
            ) {
               char[] buffer = RawJsonReader.READ_BUFFER.get();
               RawJsonReader rawJsonReader = new RawJsonReader(reader, buffer);
               ExtraInfo extraInfo = ExtraInfo.THREAD_LOCAL.get();
               PluginManifest manifestx = PluginManifest.CODEC.decodeJson(rawJsonReader, extraInfo);
               extraInfo.getValidationResults().logOrThrowValidatorExceptions(LOGGER);
               if (!new PluginIdentifier(manifestx).equals(identifier)) {
                  continue;
               }

               PluginClassLoader pluginClassLoader = new PluginClassLoader(this, identifier, true, uri.toURL());
               PendingLoadJavaPlugin pluginx = new PendingLoadJavaPlugin(Paths.get(uri), manifestx, pluginClassLoader);
               manifest = this.load(pluginx);
            }

            return manifest;
         }

         URL manifestsUrl = classLoader.getResource("manifests.json");
         if (manifestsUrl != null) {
            try (
               InputStream stream = manifestsUrl.openStream();
               InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
            ) {
               char[] buffer = RawJsonReader.READ_BUFFER.get();
               RawJsonReader rawJsonReader = new RawJsonReader(reader, buffer);
               ExtraInfo extraInfo = ExtraInfo.THREAD_LOCAL.get();
               PluginManifest[] manifests = PluginManifest.ARRAY_CODEC.decodeJson(rawJsonReader, extraInfo);
               extraInfo.getValidationResults().logOrThrowValidatorExceptions(LOGGER);

               for (PluginManifest manifest : manifests) {
                  if (new PluginIdentifier(manifest).equals(identifier)) {
                     PluginClassLoader pluginClassLoader = new PluginClassLoader(this, identifier, true, uri.toURL());
                     PendingLoadJavaPlugin pluginx = new PendingLoadJavaPlugin(Paths.get(uri), manifest, pluginClassLoader);
                     return this.load(pluginx);
                  }
               }
            }
         }

         Path path = Paths.get(uri).getParent().resolve("builtin");
         if (Files.exists(path)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
               for (Path file : stream) {
                  if (Files.isRegularFile(file) && file.getFileName().toString().toLowerCase().endsWith(".jar")) {
                     PluginManifest manifestx = loadManifest(file);
                     if (manifestx != null && new PluginIdentifier(manifestx).equals(identifier)) {
                        PendingLoadJavaPlugin pendingLoadJavaPlugin = this.loadPendingJavaPlugin(file);
                        if (pendingLoadJavaPlugin != null) {
                           return this.load(pendingLoadJavaPlugin);
                        }
                        break;
                     }
                  }
               }
            } catch (IOException var29) {
               ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(var29)).log("Failed to find plugins!");
            }
         }
      } catch (URISyntaxException | IOException var30) {
         ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(var30)).log("Failed to load pending classpath plugin. Failed to load manifest file!");
      }

      Boolean result = this.findPluginInDirectory(identifier, MODS_PATH);
      if (result != null) {
         return result;
      } else {
         for (Path modsPath : Options.getOptionSet().valuesOf(Options.MODS_DIRECTORIES)) {
            result = this.findPluginInDirectory(identifier, modsPath);
            if (result != null) {
               return result;
            }
         }

         return false;
      }
   }

   @Nullable
   private Boolean findPluginInDirectory(@Nonnull PluginIdentifier identifier, @Nonnull Path modsPath) {
      if (!Files.isDirectory(modsPath)) {
         return null;
      } else {
         try (DirectoryStream<Path> stream = Files.newDirectoryStream(modsPath)) {
            for (Path file : stream) {
               if (Files.isRegularFile(file) && file.getFileName().toString().toLowerCase().endsWith(".jar")) {
                  PluginManifest manifest = loadManifest(file);
                  if (manifest != null && new PluginIdentifier(manifest).equals(identifier)) {
                     PendingLoadJavaPlugin pendingLoadJavaPlugin = this.loadPendingJavaPlugin(file);
                     if (pendingLoadJavaPlugin != null) {
                        return this.load(pendingLoadJavaPlugin);
                     }

                     return false;
                  }
               }
            }
         } catch (IOException var11) {
            ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(var11)).log("Failed to find plugins in %s!", modsPath);
         }

         return null;
      }
   }

   @Nullable
   private static PluginManifest loadManifest(@Nonnull Path file) {
      try {
         PluginManifest var8;
         try (
            URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{file.toUri().toURL()}, PluginManager.class.getClassLoader());
            InputStream stream = urlClassLoader.findResource("manifest.json").openStream();
            InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
         ) {
            char[] buffer = RawJsonReader.READ_BUFFER.get();
            RawJsonReader rawJsonReader = new RawJsonReader(reader, buffer);
            ExtraInfo extraInfo = ExtraInfo.THREAD_LOCAL.get();
            PluginManifest manifest = PluginManifest.CODEC.decodeJson(rawJsonReader, extraInfo);
            extraInfo.getValidationResults().logOrThrowValidatorExceptions(LOGGER);
            var8 = manifest;
         }

         return var8;
      } catch (IOException var15) {
         ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(var15)).log("Failed to load manifest %s.", file);
         return null;
      }
   }

   private boolean load(@Nullable PendingLoadPlugin pendingLoadPlugin) {
      if (pendingLoadPlugin == null) {
         return false;
      } else {
         try {
            this.validatePluginDeps(pendingLoadPlugin, null);
            PluginBase plugin = pendingLoadPlugin.load();
            this.lock.writeLock().lock();

            try {
               this.plugins.put(plugin.getIdentifier(), plugin);
            } finally {
               this.lock.writeLock().unlock();
            }

            CompletableFuture<Void> preload = plugin.preLoad();
            if (preload == null) {
               boolean result = this.setup(plugin) && this.start(plugin);
               this.pluginListPageManager.notifyPluginChange(this.plugins, plugin.getIdentifier());
               return result;
            }

            preload.thenAccept(v -> {
               if (!this.setup(plugin)) {
                  this.pluginListPageManager.notifyPluginChange(this.plugins, plugin.getIdentifier());
               } else if (!this.start(plugin)) {
                  this.pluginListPageManager.notifyPluginChange(this.plugins, plugin.getIdentifier());
               } else {
                  this.pluginListPageManager.notifyPluginChange(this.plugins, plugin.getIdentifier());
               }
            });
            this.pluginListPageManager.notifyPluginChange(this.plugins, pendingLoadPlugin.getIdentifier());
         } catch (ClassNotFoundException var10) {
            ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(var10))
               .log("Failed to load plugin %s. Failed to find main class!", pendingLoadPlugin.getPath());
         } catch (NoSuchMethodException var11) {
            ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(var11))
               .log("Failed to load plugin %s. Requires default constructor!", pendingLoadPlugin.getPath());
         } catch (Throwable var12) {
            ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(var12)).log("Failed to load plugin %s", pendingLoadPlugin.getPath());
         }

         return false;
      }
   }

   private boolean setup(@Nonnull PluginBase plugin) {
      if (plugin.getState() == PluginState.NONE && this.dependenciesMatchState(plugin, PluginState.SETUP, PluginState.SETUP)) {
         LOGGER.at(Level.FINE).log("Setting up plugin %s", plugin.getIdentifier());
         boolean prev = AssetStore.DISABLE_DYNAMIC_DEPENDENCIES;
         AssetStore.DISABLE_DYNAMIC_DEPENDENCIES = false;

         try {
            plugin.setup0();
         } finally {
            AssetStore.DISABLE_DYNAMIC_DEPENDENCIES = prev;
         }

         AssetModule.get().initPendingStores();
         HytaleServer.get().doneSetup(plugin);
         if (!plugin.getState().isInactive()) {
            IEventDispatcher<PluginSetupEvent, PluginSetupEvent> dispatch = HytaleServer.get()
               .getEventBus()
               .dispatchFor(PluginSetupEvent.class, (Class<? extends PluginBase>)plugin.getClass());
            if (dispatch.hasListener()) {
               dispatch.dispatch(new PluginSetupEvent(plugin));
            }

            return true;
         }

         plugin.shutdown0(false);
         this.plugins.remove(plugin.getIdentifier());
      } else {
         plugin.shutdown0(false);
         this.plugins.remove(plugin.getIdentifier());
      }

      return false;
   }

   private boolean start(@Nonnull PluginBase plugin) {
      if (plugin.getState() == PluginState.SETUP && this.dependenciesMatchState(plugin, PluginState.ENABLED, PluginState.START)) {
         LOGGER.at(Level.FINE).log("Starting plugin %s", plugin.getIdentifier());
         plugin.start0();
         HytaleServer.get().doneStart(plugin);
         if (!plugin.getState().isInactive()) {
            LOGGER.at(Level.INFO).log("Enabled plugin %s", plugin.getIdentifier());
            return true;
         }

         plugin.shutdown0(false);
         this.plugins.remove(plugin.getIdentifier());
      } else {
         plugin.shutdown0(false);
         this.plugins.remove(plugin.getIdentifier());
      }

      return false;
   }

   private boolean dependenciesMatchState(PluginBase plugin, PluginState requiredState, PluginState stage) {
      for (PluginIdentifier dependencyOnManifest : plugin.getManifest().getDependencies().keySet()) {
         PluginBase dependency = this.plugins.get(dependencyOnManifest);
         if (dependency == null || dependency.getState() != requiredState) {
            LOGGER.at(Level.SEVERE).log(plugin.getName() + " is lacking dependency " + dependencyOnManifest.getName() + " at stage " + stage);
            LOGGER.at(Level.SEVERE).log(plugin.getName() + " DISABLED!");
            plugin.setFailureCause(new Exception("Missing dependency " + dependencyOnManifest.getName()));
            return false;
         }
      }

      return true;
   }

   private static void loadPendingPlugin(@Nonnull Map<PluginIdentifier, PendingLoadPlugin> pending, @Nonnull PendingLoadPlugin plugin) {
      if (pending.putIfAbsent(plugin.getIdentifier(), plugin) != null) {
         throw new IllegalArgumentException("Tried to load duplicate plugin: " + plugin.getIdentifier());
      } else {
         for (PendingLoadPlugin subPlugin : plugin.createSubPendingLoadPlugins()) {
            loadPendingPlugin(pending, subPlugin);
         }
      }
   }

   @Nonnull
   public Map<PluginIdentifier, PluginManifest> getAvailablePlugins() {
      return this.availablePlugins;
   }

   public ComponentType<EntityStore, PluginListPageManager.SessionSettings> getSessionSettingsComponentType() {
      return this.sessionSettingsComponentType;
   }

   public static class PluginBridgeClassLoader extends ClassLoader {
      private final PluginManager pluginManager;

      public PluginBridgeClassLoader(PluginManager pluginManager, ClassLoader parent) {
         super(parent);
         this.pluginManager = pluginManager;
      }

      @Nonnull
      @Override
      protected Class<?> loadClass(@Nonnull String name, boolean resolve) throws ClassNotFoundException {
         return this.loadClass0(name, null);
      }

      @Nonnull
      public Class<?> loadClass0(@Nonnull String name, PluginClassLoader pluginClassLoader) throws ClassNotFoundException {
         this.pluginManager.lock.readLock().lock();

         Class var7;
         try {
            Iterator var3 = this.pluginManager.plugins.entrySet().iterator();

            Class<?> loadClass;
            do {
               if (!var3.hasNext()) {
                  throw new ClassNotFoundException();
               }

               Entry<PluginIdentifier, PluginBase> entry = (Entry<PluginIdentifier, PluginBase>)var3.next();
               PluginBase pluginBase = entry.getValue();
               loadClass = tryGetClass(name, pluginClassLoader, pluginBase);
            } while (loadClass == null);

            var7 = loadClass;
         } finally {
            this.pluginManager.lock.readLock().unlock();
         }

         return var7;
      }

      @Nonnull
      public Class<?> loadClass0(@Nonnull String name, PluginClassLoader pluginClassLoader, @Nonnull PluginManifest manifest) throws ClassNotFoundException {
         this.pluginManager.lock.readLock().lock();

         try {
            for (PluginIdentifier pluginIdentifier : manifest.getDependencies().keySet()) {
               PluginBase pluginBase = this.pluginManager.plugins.get(pluginIdentifier);
               Class<?> loadClass = tryGetClass(name, pluginClassLoader, pluginBase);
               if (loadClass != null) {
                  return loadClass;
               }
            }

            for (PluginIdentifier pluginIdentifierx : manifest.getOptionalDependencies().keySet()) {
               if (!manifest.getDependencies().containsKey(pluginIdentifierx)) {
                  PluginBase pluginBase = this.pluginManager.plugins.get(pluginIdentifierx);
                  if (pluginBase != null) {
                     Class<?> loadClass = tryGetClass(name, pluginClassLoader, pluginBase);
                     if (loadClass != null) {
                        return loadClass;
                     }
                  }
               }
            }

            for (Entry<PluginIdentifier, PluginBase> entry : this.pluginManager.plugins.entrySet()) {
               if (!manifest.getDependencies().containsKey(entry.getKey()) && !manifest.getOptionalDependencies().containsKey(entry.getKey())) {
                  PluginBase pluginBase = entry.getValue();
                  Class<?> loadClass = tryGetClass(name, pluginClassLoader, pluginBase);
                  if (loadClass != null) {
                     return loadClass;
                  }
               }
            }

            throw new ClassNotFoundException();
         } finally {
            this.pluginManager.lock.readLock().unlock();
         }
      }

      public static Class<?> tryGetClass(@Nonnull String name, PluginClassLoader pluginClassLoader, PluginBase pluginBase) {
         if (!(pluginBase instanceof JavaPlugin)) {
            return null;
         } else {
            try {
               PluginClassLoader classLoader = ((JavaPlugin)pluginBase).getClassLoader();
               if (classLoader != pluginClassLoader) {
                  Class<?> loadClass = classLoader.loadLocalClass(name);
                  if (loadClass != null) {
                     return loadClass;
                  }
               }
            } catch (ClassNotFoundException var5) {
            }

            return null;
         }
      }

      @Nullable
      public URL getResource0(@Nonnull String name, @Nullable PluginClassLoader pluginClassLoader) {
         this.pluginManager.lock.readLock().lock();

         URL var6;
         try {
            Iterator var3 = this.pluginManager.plugins.entrySet().iterator();

            URL resource;
            do {
               if (!var3.hasNext()) {
                  return null;
               }

               Entry<PluginIdentifier, PluginBase> entry = (Entry<PluginIdentifier, PluginBase>)var3.next();
               resource = tryGetResource(name, pluginClassLoader, entry.getValue());
            } while (resource == null);

            var6 = resource;
         } finally {
            this.pluginManager.lock.readLock().unlock();
         }

         return var6;
      }

      @Nullable
      public URL getResource0(@Nonnull String name, @Nullable PluginClassLoader pluginClassLoader, @Nonnull PluginManifest manifest) {
         this.pluginManager.lock.readLock().lock();

         try {
            for (PluginIdentifier pluginIdentifier : manifest.getDependencies().keySet()) {
               URL resource = tryGetResource(name, pluginClassLoader, this.pluginManager.plugins.get(pluginIdentifier));
               if (resource != null) {
                  return resource;
               }
            }

            for (PluginIdentifier pluginIdentifierx : manifest.getOptionalDependencies().keySet()) {
               if (!manifest.getDependencies().containsKey(pluginIdentifierx)) {
                  PluginBase pluginBase = this.pluginManager.plugins.get(pluginIdentifierx);
                  if (pluginBase != null) {
                     URL resource = tryGetResource(name, pluginClassLoader, pluginBase);
                     if (resource != null) {
                        return resource;
                     }
                  }
               }
            }

            for (Entry<PluginIdentifier, PluginBase> entry : this.pluginManager.plugins.entrySet()) {
               if (!manifest.getDependencies().containsKey(entry.getKey()) && !manifest.getOptionalDependencies().containsKey(entry.getKey())) {
                  URL resource = tryGetResource(name, pluginClassLoader, entry.getValue());
                  if (resource != null) {
                     return resource;
                  }
               }
            }

            return null;
         } finally {
            this.pluginManager.lock.readLock().unlock();
         }
      }

      @Nonnull
      public Enumeration<URL> getResources0(@Nonnull String name, @Nullable PluginClassLoader pluginClassLoader) {
         ObjectArrayList<URL> results = new ObjectArrayList();
         this.pluginManager.lock.readLock().lock();

         try {
            for (Entry<PluginIdentifier, PluginBase> entry : this.pluginManager.plugins.entrySet()) {
               URL resource = tryGetResource(name, pluginClassLoader, entry.getValue());
               if (resource != null) {
                  results.add(resource);
               }
            }
         } finally {
            this.pluginManager.lock.readLock().unlock();
         }

         return Collections.enumeration(results);
      }

      @Nonnull
      public Enumeration<URL> getResources0(@Nonnull String name, @Nullable PluginClassLoader pluginClassLoader, @Nonnull PluginManifest manifest) {
         ObjectArrayList<URL> results = new ObjectArrayList();
         this.pluginManager.lock.readLock().lock();

         try {
            for (PluginIdentifier pluginIdentifier : manifest.getDependencies().keySet()) {
               URL resource = tryGetResource(name, pluginClassLoader, this.pluginManager.plugins.get(pluginIdentifier));
               if (resource != null) {
                  results.add(resource);
               }
            }

            for (PluginIdentifier pluginIdentifierx : manifest.getOptionalDependencies().keySet()) {
               if (!manifest.getDependencies().containsKey(pluginIdentifierx)) {
                  PluginBase pluginBase = this.pluginManager.plugins.get(pluginIdentifierx);
                  if (pluginBase != null) {
                     URL resource = tryGetResource(name, pluginClassLoader, pluginBase);
                     if (resource != null) {
                        results.add(resource);
                     }
                  }
               }
            }

            for (Entry<PluginIdentifier, PluginBase> entry : this.pluginManager.plugins.entrySet()) {
               if (!manifest.getDependencies().containsKey(entry.getKey()) && !manifest.getOptionalDependencies().containsKey(entry.getKey())) {
                  URL resource = tryGetResource(name, pluginClassLoader, entry.getValue());
                  if (resource != null) {
                     results.add(resource);
                  }
               }
            }
         } finally {
            this.pluginManager.lock.readLock().unlock();
         }

         return Collections.enumeration(results);
      }

      @Nullable
      private static URL tryGetResource(@Nonnull String name, @Nullable PluginClassLoader pluginClassLoader, @Nullable PluginBase pluginBase) {
         if (pluginBase instanceof JavaPlugin javaPlugin) {
            PluginClassLoader classLoader = javaPlugin.getClassLoader();
            return classLoader != pluginClassLoader ? classLoader.findResource(name) : null;
         } else {
            return null;
         }
      }

      static {
         registerAsParallelCapable();
      }
   }
}
