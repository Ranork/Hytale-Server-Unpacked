package com.hypixel.hytale.server.core.liveconfig;

import com.hypixel.hytale.common.plugin.PluginManifest;
import com.hypixel.hytale.common.util.java.ManifestUtil;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.auth.ServerAuthManager;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import io.sentry.Breadcrumb;
import io.sentry.Sentry;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LiveConfigModule extends JavaPlugin {
   @Nonnull
   public static final PluginManifest MANIFEST = PluginManifest.corePlugin(LiveConfigModule.class).build();
   @Nonnull
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   private static final long REFRESH_INTERVAL_SECONDS = 300L;
   private static final long INITIAL_DELAY_SECONDS = Long.getLong("liveconfig.initial.delay", 10L);
   private static LiveConfigModule instance;
   private volatile LiveConfigSnapshot snapshot = LiveConfigSnapshot.EMPTY;
   @Nullable
   private ScheduledFuture<?> refreshTask;
   private final LiveConfigService liveConfigService = new LiveConfigService();
   @Nonnull
   private final String patchline;
   @Nonnull
   private final String os;
   @Nonnull
   private final String arch;

   public LiveConfigModule(@Nonnull JavaPluginInit init) {
      super(init);
      instance = this;
      this.patchline = resolvePatchline();
      this.os = System.getProperty("liveconfig.os", LiveConfigService.detectOS());
      this.arch = System.getProperty("liveconfig.arch", LiveConfigService.detectArch());
   }

   @Nullable
   public static LiveConfigModule get() {
      return instance;
   }

   public boolean getBoolFlag(@Nonnull String name, boolean defaultValue) {
      return this.snapshot.getBoolFlag(name, defaultValue);
   }

   public int getIntFlag(@Nonnull String name, int defaultValue) {
      return this.snapshot.getIntFlag(name, defaultValue);
   }

   @Nonnull
   public String getStringFlag(@Nonnull String name, @Nonnull String defaultValue) {
      return this.snapshot.getStringFlag(name, defaultValue);
   }

   public boolean hasFlag(@Nonnull String name) {
      return this.snapshot.hasFlag(name);
   }

   @Nonnull
   public LiveConfigSnapshot getSnapshot() {
      return this.snapshot;
   }

   public void forceRefresh() {
      this.performRefresh();
   }

   @Override
   protected void setup() {
   }

   @Override
   protected void start() {
      LOGGER.at(Level.INFO).log("LiveConfig starting (patchline=%s, os=%s, arch=%s)", this.patchline, this.os, this.arch);
      this.refreshTask = HytaleServer.SCHEDULED_EXECUTOR.scheduleAtFixedRate(this::performRefresh, INITIAL_DELAY_SECONDS, 300L, TimeUnit.SECONDS);
   }

   @Override
   protected void shutdown() {
      if (this.refreshTask != null) {
         this.refreshTask.cancel(false);
      }
   }

   private void performRefresh() {
      if (!LiveConfigService.hasUrlOverride()) {
         ServerAuthManager authManager = ServerAuthManager.getInstance();
         if (!authManager.hasSessionToken()) {
            LOGGER.at(Level.FINE).log("No session token - skipping live config refresh");
            return;
         }
      }

      try {
         String currentEtag = this.snapshot.getEtag();
         LiveConfigSnapshot newSnapshot = this.liveConfigService
            .fetchServerConfigs(this.patchline, this.os, this.arch, ManifestUtil.getImplementationVersion(), currentEtag);
         if (newSnapshot != null) {
            String previousVersion = this.snapshot.getVersion();
            this.snapshot = newSnapshot;
            String newVersion = newSnapshot.getVersion();
            if (previousVersion == null || !previousVersion.equals(newVersion)) {
               updateSentryFlagContext(newSnapshot);
               LOGGER.at(Level.INFO).log("LiveConfig updated: version=%s, %d flag(s)", newVersion, newSnapshot.getFlagCount());
            }
         }
      } catch (Exception var5) {
         ((HytaleLogger.Api)LOGGER.at(Level.WARNING).withCause(var5)).log("Failed to refresh live config");
      }
   }

   private static void updateSentryFlagContext(@Nonnull LiveConfigSnapshot snapshot) {
      Map<String, LiveConfigSnapshot.ResolvedFlag> allFlags = snapshot.getAllFlags();
      HashMap<String, String> flagContext = new HashMap<>(allFlags.size());

      for (Entry<String, LiveConfigSnapshot.ResolvedFlag> entry : allFlags.entrySet()) {
         Object value = entry.getValue().value();
         flagContext.put(entry.getKey(), value != null ? value.toString() : "null");
      }

      try {
         Sentry.configureScope(scope -> scope.setContexts("feature_flags", flagContext));

         for (Entry<String, LiveConfigSnapshot.ResolvedFlag> entry : allFlags.entrySet()) {
            if (entry.getValue().value() instanceof Boolean b) {
               Sentry.addFeatureFlag(entry.getKey(), b);
            }
         }

         Sentry.addBreadcrumb(Breadcrumb.info(String.format("LiveConfig refreshed with %d flag(s)", allFlags.size())));
      } catch (Exception var7) {
         ((HytaleLogger.Api)LOGGER.at(Level.WARNING).withCause(var7)).log("Failed to update Sentry feature flag context");
      }
   }

   @Nonnull
   private static String resolvePatchline() {
      String override = System.getProperty("liveconfig.patchline");
      if (override != null) {
         return override;
      } else {
         String patchline = ManifestUtil.getPatchline();
         return patchline != null ? patchline : "dev";
      }
   }
}
