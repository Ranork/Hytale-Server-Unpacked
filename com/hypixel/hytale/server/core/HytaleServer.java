package com.hypixel.hytale.server.core;

import com.hypixel.hytale.assetstore.AssetPack;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.common.plugin.PluginManifest;
import com.hypixel.hytale.common.thread.HytaleForkJoinThreadFactory;
import com.hypixel.hytale.common.util.FormatUtil;
import com.hypixel.hytale.common.util.GCUtil;
import com.hypixel.hytale.common.util.HardwareUtil;
import com.hypixel.hytale.common.util.NetworkUtil;
import com.hypixel.hytale.common.util.java.ManifestUtil;
import com.hypixel.hytale.event.EventBus;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.logger.backend.HytaleLogManager;
import com.hypixel.hytale.logger.backend.HytaleLoggerBackend;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.util.TrigMathUtil;
import com.hypixel.hytale.metrics.JVMMetrics;
import com.hypixel.hytale.metrics.MetricsRegistry;
import com.hypixel.hytale.plugin.early.EarlyPluginLoader;
import com.hypixel.hytale.server.core.asset.AssetModule;
import com.hypixel.hytale.server.core.asset.AssetRegistryLoader;
import com.hypixel.hytale.server.core.asset.LoadAssetEvent;
import com.hypixel.hytale.server.core.auth.ServerAuthManager;
import com.hypixel.hytale.server.core.auth.SessionServiceClient;
import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.console.ConsoleSender;
import com.hypixel.hytale.server.core.event.events.BootEvent;
import com.hypixel.hytale.server.core.event.events.ShutdownEvent;
import com.hypixel.hytale.server.core.io.ServerManager;
import com.hypixel.hytale.server.core.io.netty.NettyUtil;
import com.hypixel.hytale.server.core.modules.singleplayer.SingleplayerModule;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.PluginBase;
import com.hypixel.hytale.server.core.plugin.PluginClassLoader;
import com.hypixel.hytale.server.core.plugin.PluginManager;
import com.hypixel.hytale.server.core.plugin.PluginState;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.datastore.DataStoreProvider;
import com.hypixel.hytale.server.core.universe.datastore.DiskDataStoreProvider;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.update.UpdateModule;
import com.hypixel.hytale.server.core.util.concurrent.ThreadUtil;
import io.netty.handler.codec.quic.Quic;
import io.sentry.Sentry;
import io.sentry.SentryOptions;
import io.sentry.protocol.Contexts;
import io.sentry.protocol.OperatingSystem;
import io.sentry.protocol.User;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import joptsimple.OptionSet;

public class HytaleServer {
   public static final int DEFAULT_PORT = 5520;
   public static final ScheduledExecutorService SCHEDULED_EXECUTOR = Executors.newSingleThreadScheduledExecutor(ThreadUtil.daemon("Scheduler"));
   @Nonnull
   public static final MetricsRegistry<HytaleServer> METRICS_REGISTRY = new MetricsRegistry<HytaleServer>()
      .register("Time", server -> Instant.now(), Codec.INSTANT)
      .register("Boot", server -> server.boot, Codec.INSTANT)
      .register("BootStart", server -> server.bootStart, Codec.LONG)
      .register("Booting", server -> server.booting.get(), Codec.BOOLEAN)
      .register("ShutdownReason", server -> {
         ShutdownReason reason = server.shutdown.get();
         return reason == null ? null : reason.toString();
      }, Codec.STRING)
      .register("PluginManager", HytaleServer::getPluginManager, PluginManager.METRICS_REGISTRY)
      .register("Config", HytaleServer::getConfig, HytaleServerConfig.CODEC)
      .register("JVM", JVMMetrics.METRICS_REGISTRY);
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   private static HytaleServer instance;
   private final Semaphore aliveLock = new Semaphore(0);
   private final AtomicBoolean booting = new AtomicBoolean(false);
   private final AtomicBoolean booted = new AtomicBoolean(false);
   private final AtomicReference<ShutdownReason> shutdown = new AtomicReference<>();
   private final EventBus eventBus = new EventBus(Options.getOptionSet().has(Options.EVENT_DEBUG));
   private final PluginManager pluginManager = new PluginManager();
   private final CommandManager commandManager = new CommandManager();
   @Nonnull
   private final HytaleServerConfig hytaleServerConfig;
   private final Instant boot;
   private final long bootStart;
   private int pluginsProgress;

   public HytaleServer() throws IOException {
      instance = this;
      Quic.ensureAvailability();
      HytaleLoggerBackend.setIndent(25);
      ThreadUtil.forceTimeHighResolution();
      ThreadUtil.createKeepAliveThread(this.aliveLock);
      this.boot = Instant.now();
      this.bootStart = System.nanoTime();
      LOGGER.at(Level.INFO).log("Starting HytaleServer");
      Constants.init();
      DataStoreProvider.CODEC.register("Disk", DiskDataStoreProvider.class, DiskDataStoreProvider.CODEC);
      LOGGER.at(Level.INFO).log("Loading config...");
      this.hytaleServerConfig = HytaleServerConfig.load();
      HytaleLoggerBackend.reloadLogLevels();
      System.setProperty("java.util.concurrent.ForkJoinPool.common.threadFactory", HytaleForkJoinThreadFactory.class.getName());
      OptionSet optionSet = Options.getOptionSet();
      LOGGER.at(Level.INFO).log("Authentication mode: %s", optionSet.valueOf(Options.AUTH_MODE));
      ServerAuthManager.getInstance().initialize();
      if (EarlyPluginLoader.hasTransformers()) {
         LOGGER.at(Level.INFO).log("Early plugins loaded!! Disabling Sentry!!");
      } else if (!ManifestUtil.isJar() || ManifestUtil.getVersion() == null) {
         LOGGER.at(Level.INFO).log("Sentry disabled: development build (no version)");
      } else if (!optionSet.has(Options.DISABLE_SENTRY)) {
         LOGGER.at(Level.INFO).log("Enabling Sentry");
         SentryOptions options = new SentryOptions();
         options.setDsn("https://6043a13c7b5c45b5c834b6d896fb378e@sentry.hytale.com/4");
         options.setRelease(ManifestUtil.getImplementationVersion());
         options.setDist(ManifestUtil.getImplementationRevisionId());
         options.setEnvironment("release");
         options.setTag("patchline", ManifestUtil.getPatchline());
         options.setServerName(NetworkUtil.getHostName());
         UUID distinctId = HardwareUtil.getUUID();
         if (distinctId != null) {
            options.setDistinctId(distinctId.toString());
         }

         options.setBeforeSend((event, hint) -> {
            Throwable throwable = event.getThrowable();
            if (PluginClassLoader.isFromThirdPartyPlugin(throwable)) {
               return null;
            } else {
               Contexts contexts = event.getContexts();
               HashMap<String, Object> serverContext = new HashMap<>();
               serverContext.put("name", this.getServerName());
               serverContext.put("max-players", this.getConfig().getMaxPlayers());
               ServerManager serverManager = ServerManager.get();
               if (serverManager != null) {
                  serverContext.put("listeners", serverManager.getListeners().stream().map(Object::toString).toList());
               }

               contexts.put("server", serverContext);
               Universe universe = Universe.get();
               if (universe != null) {
                  HashMap<String, Object> universeContext = new HashMap<>();
                  universeContext.put("path", universe.getPath().toString());
                  universeContext.put("player-count", universe.getPlayerCount());
                  universeContext.put("worlds", universe.getWorlds().keySet().stream().toList());
                  contexts.put("universe", universeContext);
               }

               HashMap<String, Object> pluginsContext = new HashMap<>();
               boolean hasExternalPlugins = false;

               for (PluginBase plugin : this.pluginManager.getPlugins()) {
                  PluginManifest manifestxx = plugin.getManifest();
                  HashMap<String, Object> pluginInfo = new HashMap<>();
                  pluginInfo.put("version", manifestxx.getVersion().toString());
                  pluginInfo.put("state", plugin.getState().name());
                  pluginsContext.put(plugin.getIdentifier().toString(), pluginInfo);
                  if (plugin instanceof JavaPlugin jp && !jp.getClassLoader().isInServerClassPath()) {
                     hasExternalPlugins = true;
                  }
               }

               contexts.put("plugins", pluginsContext);
               AssetModule assetModule = AssetModule.get();
               boolean hasUserPacks = false;
               if (assetModule != null) {
                  HashMap<String, Object> packsContext = new HashMap<>();

                  for (AssetPack pack : assetModule.getAssetPacks()) {
                     HashMap<String, Object> packInfo = new HashMap<>();
                     PluginManifest manifestx = pack.getManifest();
                     if (manifestx != null && manifestx.getVersion() != null) {
                        packInfo.put("version", manifestx.getVersion().toString());
                     }

                     packInfo.put("immutable", pack.isImmutable());
                     packsContext.put(pack.getName(), packInfo);
                     if (!pack.isImmutable()) {
                        hasUserPacks = true;
                     }
                  }

                  contexts.put("packs", packsContext);
               }

               event.setTag("has-plugins", String.valueOf(hasExternalPlugins));
               event.setTag("has-packs", String.valueOf(hasUserPacks));
               User user = new User();
               HashMap<String, Object> unknown = new HashMap<>();
               user.setUnknown(unknown);
               UUID hardwareUUID = HardwareUtil.getUUID();
               if (hardwareUUID != null) {
                  unknown.put("hardware-uuid", hardwareUUID.toString());
               }

               ServerAuthManager authManager = ServerAuthManager.getInstance();
               unknown.put("auth-mode", authManager.getAuthMode().toString());
               SessionServiceClient.GameProfile profile = authManager.getSelectedProfile();
               if (profile != null) {
                  user.setUsername(profile.username);
                  user.setId(profile.uuid.toString());
               }

               user.setIpAddress("{{auto}}");
               event.setUser(user);
               return event;
            }
         });
         Sentry.init(options);
         Sentry.startSession();
         Sentry.configureScope(
            scope -> {
               UUID hardwareUUID = HardwareUtil.getUUID();
               if (hardwareUUID != null) {
                  scope.setContexts("hardware", Map.of("uuid", hardwareUUID.toString()));
               }

               OperatingSystem os = new OperatingSystem();
               os.setName(System.getProperty("os.name"));
               os.setVersion(System.getProperty("os.version"));
               scope.getContexts().setOperatingSystem(os);
               scope.setContexts(
                  "build",
                  Map.of(
                     "version",
                     String.valueOf(ManifestUtil.getImplementationVersion()),
                     "revision-id",
                     String.valueOf(ManifestUtil.getImplementationRevisionId()),
                     "patchline",
                     String.valueOf(ManifestUtil.getPatchline()),
                     "environment",
                     "release"
                  )
               );
               if (Constants.SINGLEPLAYER) {
                  scope.setContexts(
                     "singleplayer", Map.of("owner-uuid", String.valueOf(SingleplayerModule.getUuid()), "owner-name", SingleplayerModule.getUsername())
                  );
               }
            }
         );
         HytaleLogger.getLogger().setSentryClient(Sentry.getCurrentScopes());
      }

      ServerAuthManager.getInstance().checkPendingFatalError();
      NettyUtil.init();
      float sin = TrigMathUtil.sin(0.0F);
      float atan2 = TrigMathUtil.atan2(0.0F, 0.0F);
      Thread shutdownHook = new Thread(() -> {
         if (this.shutdown.getAndSet(ShutdownReason.SIGINT) == null) {
            this.shutdown0(ShutdownReason.SIGINT);
         }
      }, "ShutdownHook");
      shutdownHook.setDaemon(false);
      Runtime.getRuntime().addShutdownHook(shutdownHook);
      AssetRegistryLoader.init();

      for (PluginManifest manifest : Constants.CORE_PLUGINS) {
         this.pluginManager.registerCorePlugin(manifest);
      }

      GCUtil.register(info -> {
         Universe universe = Universe.get();
         if (universe != null) {
            for (World world : universe.getWorlds().values()) {
               world.markGCHasRun();
            }
         }
      });
      this.boot();
   }

   @Nonnull
   public EventBus getEventBus() {
      return this.eventBus;
   }

   @Nonnull
   public PluginManager getPluginManager() {
      return this.pluginManager;
   }

   @Nonnull
   public CommandManager getCommandManager() {
      return this.commandManager;
   }

   @Nonnull
   public HytaleServerConfig getConfig() {
      return this.hytaleServerConfig;
   }

   private void boot() {
      if (!this.booting.getAndSet(true)) {
         LOGGER.at(Level.INFO)
            .log("Booting up HytaleServer - Version: " + ManifestUtil.getImplementationVersion() + ", Revision: " + ManifestUtil.getImplementationRevisionId());

         try {
            this.pluginsProgress = 0;
            this.sendSingleplayerProgress();
            if (this.isShuttingDown()) {
               return;
            }

            LOGGER.at(Level.INFO).log("Setup phase...");
            this.commandManager.registerCommands();
            this.pluginManager.setup();
            ServerAuthManager.getInstance().initializeCredentialStore();
            LOGGER.at(Level.INFO).log("Setup phase completed! Boot time %s", FormatUtil.nanosToString(System.nanoTime() - this.bootStart));
            if (this.isShuttingDown()) {
               return;
            }

            LoadAssetEvent loadAssetEvent = get()
               .getEventBus()
               .<Void, LoadAssetEvent>dispatchFor(LoadAssetEvent.class)
               .dispatch(new LoadAssetEvent(this.bootStart));
            if (this.isShuttingDown()) {
               return;
            }

            if (loadAssetEvent.isShouldShutdown()) {
               List<String> reasons = loadAssetEvent.getReasons();
               String join = String.join("\n", reasons);
               LOGGER.at(Level.SEVERE).log("Asset validation FAILED with %d reason(s):\n%s", reasons.size(), join);
               this.shutdownServer(ShutdownReason.VALIDATE_ERROR.withMessage(join));
               return;
            }

            if (Options.getOptionSet().has(Options.SHUTDOWN_AFTER_VALIDATE)) {
               LOGGER.at(Level.INFO).log("Asset validation passed");
               this.shutdownServer(ShutdownReason.SHUTDOWN);
               return;
            }

            this.pluginsProgress = 0;
            this.sendSingleplayerProgress();
            if (this.isShuttingDown()) {
               return;
            }

            LOGGER.at(Level.INFO).log("Starting plugin manager...");
            this.pluginManager.start();
            LOGGER.at(Level.INFO).log("Plugin manager started! Startup time so far: %s", FormatUtil.nanosToString(System.nanoTime() - this.bootStart));
            if (this.isShuttingDown()) {
               return;
            }

            this.sendSingleplayerSignal("-=|Enabled|0");
         } catch (Throwable var6) {
            ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(var6)).log("Failed to boot HytaleServer!");
            Throwable t = var6;

            while (t.getCause() != null) {
               t = t.getCause();
            }

            this.shutdownServer(ShutdownReason.CRASH.withMessage("Failed to start server! " + t.getMessage()));
         }

         if (this.hytaleServerConfig.consumeHasChanged()) {
            HytaleServerConfig.save(this.hytaleServerConfig).join();
         }

         SCHEDULED_EXECUTOR.scheduleWithFixedDelay(() -> {
            try {
               if (this.hytaleServerConfig.consumeHasChanged()) {
                  HytaleServerConfig.save(this.hytaleServerConfig).join();
               }
            } catch (Exception var2x) {
               ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(var2x)).log("Failed to save server config!");
            }
         }, 1L, 1L, TimeUnit.MINUTES);
         LOGGER.at(Level.INFO).log("Getting Hytale Universe ready...");
         Universe.get().getUniverseReady().join();
         LOGGER.at(Level.INFO).log("Universe ready!");
         List<String> tags = new ObjectArrayList();
         if (Constants.SINGLEPLAYER) {
            tags.add("Singleplayer");
         } else {
            tags.add("Multiplayer");
         }

         if (Constants.FRESH_UNIVERSE) {
            tags.add("Fresh Universe");
         }

         this.booted.set(true);
         ServerManager.get().waitForBindComplete();
         this.eventBus.dispatch(BootEvent.class);
         List<String> bootCommands = Options.getOptionSet().valuesOf(Options.BOOT_COMMAND);
         if (!bootCommands.isEmpty()) {
            CommandManager.get().handleCommands(ConsoleSender.INSTANCE, new ArrayDeque<>(bootCommands)).join();
         }

         String border = "\u001b[0;32m===============================================================================================";
         LOGGER.at(Level.INFO).log("\u001b[0;32m===============================================================================================");
         LOGGER.at(Level.INFO)
            .log(
               "%s         Hytale Server Booted! [%s] took %s",
               "\u001b[0;32m",
               String.join(", ", tags),
               FormatUtil.nanosToString(System.nanoTime() - this.bootStart)
            );
         LOGGER.at(Level.INFO).log("\u001b[0;32m===============================================================================================");
         UpdateModule updateModule = UpdateModule.get();
         if (updateModule != null) {
            updateModule.onServerReady();
         }

         ServerAuthManager authManager = ServerAuthManager.getInstance();
         if (!authManager.isSingleplayer() && authManager.getAuthMode() == ServerAuthManager.AuthMode.NONE) {
            LOGGER.at(Level.WARNING).log("%sNo server tokens configured. Use /auth login to authenticate.", "\u001b[0;31m");
         }

         this.sendSingleplayerSignal(">> Singleplayer Ready <<");
      }
   }

   public void shutdownServer() {
      this.shutdownServer(ShutdownReason.SHUTDOWN);
   }

   public void shutdownServer(@Nonnull ShutdownReason reason) {
      Objects.requireNonNull(reason, "Server shutdown reason can't be null!");
      if (this.shutdown.getAndSet(reason) == null) {
         if (reason.getMessage() != null) {
            this.sendSingleplayerSignal("-=|Shutdown|" + reason.getMessage().replace("\n", "\\n"));
         }

         Thread shutdownThread = new Thread(() -> this.shutdown0(reason), "ShutdownThread");
         shutdownThread.setDaemon(false);
         shutdownThread.start();
      }
   }

   void shutdown0(@Nonnull ShutdownReason reason) {
      LOGGER.at(Level.INFO).log("Shutdown triggered!!!");

      try {
         LOGGER.at(Level.INFO).log("Shutting down... %d  '%s'", reason.getExitCode(), reason.getMessage());
         this.eventBus.dispatch(ShutdownEvent.class);
         this.pluginManager.shutdown();
         this.commandManager.shutdown();
         this.eventBus.shutdown();
         ServerAuthManager.getInstance().shutdown();
         LOGGER.at(Level.INFO).log("Saving config...");
         if (this.hytaleServerConfig.consumeHasChanged()) {
            HytaleServerConfig.save(this.hytaleServerConfig).join();
         }

         LOGGER.at(Level.INFO).log("Shutdown completed!");
      } catch (Throwable var3) {
         ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(var3)).log("Exception while shutting down:");
      }

      this.aliveLock.release();
      HytaleLogManager.resetFinally();
      Sentry.endSession();
      SCHEDULED_EXECUTOR.schedule(() -> {
         LOGGER.at(Level.SEVERE).log("Forcing shutdown!");
         Runtime.getRuntime().halt(reason.getExitCode());
      }, 3L, TimeUnit.SECONDS);
      if (reason != ShutdownReason.SIGINT) {
         System.exit(reason.getExitCode());
      }
   }

   public void doneSetup(PluginBase plugin) {
      this.pluginsProgress++;
      this.sendSingleplayerProgress();
   }

   public void doneStart(PluginBase plugin) {
      this.pluginsProgress++;
      this.sendSingleplayerProgress();
   }

   public void doneStop(PluginBase plugin) {
      this.pluginsProgress--;
      this.sendSingleplayerProgress();
   }

   public void sendSingleplayerProgress() {
      List<PluginBase> plugins = this.pluginManager.getPlugins();
      if (this.shutdown.get() != null) {
         this.sendSingleplayerSignal("-=|Shutdown Modules|" + MathUtil.round((double)(plugins.size() - this.pluginsProgress) / plugins.size(), 2) * 100.0);
      } else if (this.pluginManager.getState() == PluginState.SETUP) {
         this.sendSingleplayerSignal("-=|Setup|" + MathUtil.round((double)this.pluginsProgress / plugins.size(), 2) * 100.0);
      } else if (this.pluginManager.getState() == PluginState.START) {
         this.sendSingleplayerSignal("-=|Starting|" + MathUtil.round((double)this.pluginsProgress / plugins.size(), 2) * 100.0);
      }
   }

   public String getServerName() {
      return this.getConfig().getServerName();
   }

   public boolean isBooting() {
      return this.booting.get();
   }

   public boolean isBooted() {
      return this.booted.get();
   }

   public boolean isShuttingDown() {
      return this.shutdown.get() != null;
   }

   @Nonnull
   public Instant getBoot() {
      return this.boot;
   }

   public long getBootStart() {
      return this.bootStart;
   }

   @Nullable
   public ShutdownReason getShutdownReason() {
      return this.shutdown.get();
   }

   private void sendSingleplayerSignal(String message) {
      if (Constants.SINGLEPLAYER) {
         HytaleLoggerBackend.rawLog(message);
      }
   }

   public void reportSingleplayerStatus(String message) {
      if (Constants.SINGLEPLAYER) {
         HytaleLoggerBackend.rawLog("-=|" + message + "|0");
      }
   }

   public void reportSingleplayerStatus(String message, double progress) {
      if (Constants.SINGLEPLAYER) {
         HytaleLoggerBackend.rawLog("-=|" + message + "|" + progress);
      }
   }

   public void reportSaveProgress(@Nonnull World world, int saved, int total) {
      if (this.isShuttingDown()) {
         double progress = MathUtil.round((double)saved / total, 2) * 100.0;
         if (Constants.SINGLEPLAYER) {
            this.sendSingleplayerSignal("-=|Saving world " + world.getName() + " chunks|" + progress);
         } else if (total < 10 || saved % (total / 10) == 0) {
            world.getLogger().at(Level.INFO).log("Saving chunks: %.0f%%", progress);
         }
      }
   }

   public static HytaleServer get() {
      return instance;
   }
}
