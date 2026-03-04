package com.hypixel.hytale.server.core.update;

import com.hypixel.hytale.common.plugin.PluginManifest;
import com.hypixel.hytale.common.util.SystemUtil;
import com.hypixel.hytale.common.util.java.ManifestUtil;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Constants;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.ShutdownReason;
import com.hypixel.hytale.server.core.auth.ServerAuthManager;
import com.hypixel.hytale.server.core.config.UpdateConfig;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.update.command.UpdateCommand;
import java.awt.Color;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class UpdateModule extends JavaPlugin {
   @Nonnull
   public static final PluginManifest MANIFEST = PluginManifest.corePlugin(UpdateModule.class).build();
   @Nonnull
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   public static final boolean KILL_SWITCH_ENABLED = SystemUtil.getEnvBoolean("HYTALE_DISABLE_UPDATES");
   private static UpdateModule instance;
   @Nonnull
   private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
      Thread t = new Thread(r, "UpdateChecker");
      t.setDaemon(true);
      return t;
   });
   @Nullable
   private ScheduledFuture<?> updateCheckTask;
   @Nullable
   private ScheduledFuture<?> autoApplyTask;
   private final AtomicReference<UpdateService.VersionManifest> latestKnownVersion = new AtomicReference<>();
   private final AtomicReference<CompletableFuture<?>> activeDownload = new AtomicReference<>();
   private final AtomicReference<Thread> activeDownloadThread = new AtomicReference<>();
   private final AtomicBoolean downloadLock = new AtomicBoolean(false);
   private final AtomicLong downloadStartTime = new AtomicLong(0L);
   private final AtomicLong downloadedBytes = new AtomicLong(0L);
   private final AtomicLong totalBytes = new AtomicLong(0L);
   private final AtomicLong autoApplyScheduledTime = new AtomicLong(0L);
   private final AtomicLong lastWarningTime = new AtomicLong(0L);

   public UpdateModule(@Nonnull JavaPluginInit init) {
      super(init);
      instance = this;
   }

   @Nullable
   public static UpdateModule get() {
      return instance;
   }

   @Override
   protected void setup() {
      if (KILL_SWITCH_ENABLED) {
         LOGGER.at(Level.INFO).log("Update commands disabled via HYTALE_DISABLE_UPDATES environment variable");
      }

      this.getCommandRegistry().registerCommand(new UpdateCommand());
   }

   @Override
   protected void start() {
      if (!KILL_SWITCH_ENABLED) {
         String stagedVersion = UpdateService.getStagedVersion();
         if (stagedVersion != null) {
            this.logStagedUpdateWarning(stagedVersion, true);
            this.startAutoApplyTaskIfNeeded();
         }

         if (this.shouldEnableUpdateChecker()) {
            UpdateConfig config = HytaleServer.get().getConfig().getUpdateConfig();
            int intervalSeconds = config.getCheckIntervalSeconds();
            LOGGER.at(Level.INFO).log("Update checker enabled (interval: %ds)", intervalSeconds);
            this.updateCheckTask = this.scheduler.scheduleAtFixedRate(this::performUpdateCheck, 60L, intervalSeconds, TimeUnit.SECONDS);
         }
      }
   }

   private synchronized void startAutoApplyTaskIfNeeded() {
      if (this.autoApplyTask == null) {
         UpdateConfig config = HytaleServer.get().getConfig().getUpdateConfig();
         UpdateConfig.AutoApplyMode autoApplyMode = config.getAutoApplyMode();
         if (autoApplyMode != UpdateConfig.AutoApplyMode.DISABLED) {
            LOGGER.at(Level.INFO).log("Starting auto-apply task (mode: %s, delay: %d min)", autoApplyMode, config.getAutoApplyDelayMinutes());
            this.autoApplyTask = this.scheduler.scheduleAtFixedRate(this::performAutoApplyCheck, 0L, 60L, TimeUnit.SECONDS);
         }
      }
   }

   @Override
   protected void shutdown() {
      if (this.updateCheckTask != null) {
         this.updateCheckTask.cancel(false);
      }

      if (this.autoApplyTask != null) {
         this.autoApplyTask.cancel(false);
      }

      this.scheduler.shutdown();
   }

   public void onServerReady() {
      if (!KILL_SWITCH_ENABLED) {
         String stagedVersion = UpdateService.getStagedVersion();
         if (stagedVersion != null) {
            this.logStagedUpdateWarning(stagedVersion, false);
         }
      }
   }

   @Nullable
   public UpdateService.VersionManifest getLatestKnownVersion() {
      return this.latestKnownVersion.get();
   }

   public void setLatestKnownVersion(@Nullable UpdateService.VersionManifest version) {
      this.latestKnownVersion.set(version);
   }

   public boolean isDownloadInProgress() {
      return this.downloadLock.get();
   }

   public boolean tryAcquireDownloadLock() {
      return this.downloadLock.compareAndSet(false, true);
   }

   public void setActiveDownload(@Nullable CompletableFuture<?> download, @Nullable Thread thread) {
      this.activeDownload.set(download);
      this.activeDownloadThread.set(thread);
   }

   public void releaseDownloadLock() {
      this.activeDownload.set(null);
      this.activeDownloadThread.set(null);
      this.downloadLock.set(false);
      this.downloadStartTime.set(0L);
      this.downloadedBytes.set(0L);
      this.totalBytes.set(0L);
   }

   public void updateDownloadProgress(long downloaded, long total) {
      if (this.downloadStartTime.get() == 0L) {
         this.downloadStartTime.set(System.currentTimeMillis());
      }

      this.downloadedBytes.set(downloaded);
      this.totalBytes.set(total);
   }

   @Nullable
   public UpdateModule.DownloadProgress getDownloadProgress() {
      if (!this.downloadLock.get()) {
         return null;
      } else {
         long start = this.downloadStartTime.get();
         long downloaded = this.downloadedBytes.get();
         long total = this.totalBytes.get();
         if (start != 0L && total > 0L) {
            int percent = (int)(downloaded * 100L / total);
            long elapsed = System.currentTimeMillis() - start;
            long etaSeconds = -1L;
            if (elapsed > 0L && downloaded > 0L) {
               double bytesPerMs = (double)downloaded / elapsed;
               long remaining = total - downloaded;
               etaSeconds = (long)(remaining / bytesPerMs / 1000.0);
            }

            return new UpdateModule.DownloadProgress(percent, downloaded, total, etaSeconds);
         } else {
            return new UpdateModule.DownloadProgress(0, 0L, total, -1L);
         }
      }
   }

   public boolean cancelDownload() {
      CompletableFuture<?> download = this.activeDownload.getAndSet(null);
      Thread thread = this.activeDownloadThread.getAndSet(null);
      if (thread != null) {
         thread.interrupt();
      }

      if (download == null && thread == null) {
         return false;
      } else {
         if (download != null) {
            download.cancel(true);
         }

         this.releaseDownloadLock();
         return true;
      }
   }

   private boolean shouldEnableUpdateChecker() {
      if (!ManifestUtil.isJar()) {
         LOGGER.at(Level.INFO).log("Update checker disabled: not running from JAR");
         return false;
      } else if (Constants.SINGLEPLAYER) {
         LOGGER.at(Level.INFO).log("Update checker disabled: singleplayer mode");
         return false;
      } else {
         UpdateConfig config = HytaleServer.get().getConfig().getUpdateConfig();
         if (!config.isEnabled()) {
            LOGGER.at(Level.INFO).log("Update checker disabled: disabled in config");
            return false;
         } else {
            String manifestPatchline = ManifestUtil.getPatchline();
            String configPatchline = config.getPatchline();
            if (!"dev".equals(manifestPatchline) || configPatchline != null && !configPatchline.isEmpty()) {
               if (!UpdateService.isValidUpdateLayout()) {
                  LOGGER.at(Level.WARNING)
                     .log("Update checker disabled: invalid folder layout. Expected to run from Server/ with Assets.zip and start.sh/bat in parent directory.");
                  return false;
               } else {
                  return true;
               }
            } else {
               LOGGER.at(Level.INFO).log("Update checker disabled: dev patchline (set Patchline in config to override)");
               return false;
            }
         }
      }
   }

   private void performUpdateCheck() {
      ServerAuthManager authManager = ServerAuthManager.getInstance();
      if (!authManager.hasSessionToken()) {
         LOGGER.at(Level.FINE).log("Not authenticated - skipping update check");
      } else {
         String stagedVersion = UpdateService.getStagedVersion();
         if (stagedVersion != null) {
            LOGGER.at(Level.FINE).log("Staged update already exists (%s) - skipping update check", stagedVersion);
            this.startAutoApplyTaskIfNeeded();
         } else if (this.isDownloadInProgress()) {
            LOGGER.at(Level.FINE).log("Download in progress - skipping update check");
         } else {
            UpdateService updateService = new UpdateService();
            String patchline = UpdateService.getEffectivePatchline();
            updateService.checkForUpdate(patchline).thenAccept(manifest -> {
               if (manifest == null) {
                  LOGGER.at(Level.FINE).log("Update check returned no result");
               } else {
                  this.setLatestKnownVersion(manifest);
                  String currentVersion = ManifestUtil.getImplementationVersion();
                  if (currentVersion != null && currentVersion.equals(manifest.version)) {
                     LOGGER.at(Level.FINE).log("Already running latest version: %s", currentVersion);
                  } else {
                     this.logUpdateAvailable(currentVersion, manifest.version);
                     UpdateConfig config = HytaleServer.get().getConfig().getUpdateConfig();
                     if (config.isNotifyPlayersOnAvailable()) {
                        this.notifyPlayers(manifest.version);
                     }

                     if (config.getAutoApplyMode() != UpdateConfig.AutoApplyMode.DISABLED) {
                        LOGGER.at(Level.INFO).log("Auto-downloading update %s...", manifest.version);
                        this.autoDownloadUpdate(updateService, manifest);
                     }
                  }
               }
            });
         }
      }
   }

   private void autoDownloadUpdate(@Nonnull UpdateService updateService, @Nonnull UpdateService.VersionManifest manifest) {
      if (UpdateService.getStagedVersion() == null && this.tryAcquireDownloadLock()) {
         UpdateService.DownloadTask downloadTask = updateService.downloadUpdate(
            manifest, UpdateService.getStagingDir(), (percent, downloaded, total) -> this.updateDownloadProgress(downloaded, total)
         );
         CompletableFuture<Boolean> downloadFuture = downloadTask.future().whenComplete((success, error) -> {
            this.releaseDownloadLock();
            if (Boolean.TRUE.equals(success)) {
               LOGGER.at(Level.INFO).log("Update %s downloaded and staged", manifest.version);
               this.startAutoApplyTaskIfNeeded();
            } else if (error instanceof CancellationException) {
               LOGGER.at(Level.INFO).log("Download of update %s was cancelled", manifest.version);
               UpdateService.deleteStagedUpdate();
            } else {
               LOGGER.at(Level.WARNING).log("Failed to download update %s: %s", manifest.version, error != null ? error.getMessage() : "unknown error");
               UpdateService.deleteStagedUpdate();
            }
         });
         this.setActiveDownload(downloadFuture, downloadTask.thread());
      }
   }

   private void performAutoApplyCheck() {
      String stagedVersion = UpdateService.getStagedVersion();
      if (stagedVersion == null) {
         if (this.autoApplyScheduledTime.getAndSet(0L) != 0L) {
            LOGGER.at(Level.FINE).log("No staged update - clearing auto-apply schedule");
         }

         this.lastWarningTime.set(0L);
      } else {
         LOGGER.at(Level.FINE).log("Auto-apply check: staged version %s", stagedVersion);
         this.checkAutoApply(stagedVersion);
      }
   }

   private void logUpdateAvailable(@Nullable String currentVersion, @Nonnull String latestVersion) {
      LOGGER.at(Level.INFO).log("Update available: %s (current: %s)", latestVersion, currentVersion);
      UpdateConfig config = HytaleServer.get().getConfig().getUpdateConfig();
      if (config.getAutoApplyMode() == UpdateConfig.AutoApplyMode.DISABLED) {
         LOGGER.at(Level.INFO).log("Run '/update download' to stage the update");
      }
   }

   private void logStagedUpdateWarning(@Nullable String version, boolean isStartup) {
      String border = "\u001b[0;33m===============================================================================================";
      LOGGER.at(Level.INFO).log(border);
      if (isStartup) {
         LOGGER.at(Level.INFO).log("%s         WARNING: Staged update %s not applied!", "\u001b[0;33m", version != null ? version : "unknown");
         LOGGER.at(Level.INFO).log("%s         Use launcher script (start.sh/bat) or manually move files from updater/staging/", "\u001b[0;33m");
      } else {
         LOGGER.at(Level.INFO).log("%s         REMINDER: Staged update %s waiting to be applied", "\u001b[0;33m", version != null ? version : "unknown");
         LOGGER.at(Level.INFO).log("%s         Run '/update status' for details or '/update cancel' to abort", "\u001b[0;33m");
      }

      LOGGER.at(Level.INFO).log(border);
   }

   private void checkAutoApply(@Nonnull String stagedVersion) {
      UpdateConfig config = HytaleServer.get().getConfig().getUpdateConfig();
      UpdateConfig.AutoApplyMode mode = config.getAutoApplyMode();
      if (mode != UpdateConfig.AutoApplyMode.DISABLED) {
         Universe universe = Universe.get();
         if (universe != null) {
            int playerCount = universe.getPlayers().size();
            if (playerCount == 0) {
               LOGGER.at(Level.INFO).log("No players online - auto-applying update %s", stagedVersion);
               this.triggerAutoApply();
            } else if (mode != UpdateConfig.AutoApplyMode.WHEN_EMPTY) {
               int delayMinutes = config.getAutoApplyDelayMinutes();
               long now = System.currentTimeMillis();
               long applyTime = now + delayMinutes * 60 * 1000L;
               if (this.autoApplyScheduledTime.compareAndSet(0L, applyTime)) {
                  LOGGER.at(Level.INFO).log("Update %s will be auto-applied in %d minutes (players online: %d)", stagedVersion, delayMinutes, playerCount);
                  this.broadcastToPlayers(
                     Message.translation("server.update.auto_apply_warning").param("version", stagedVersion).param("minutes", delayMinutes).color(Color.YELLOW)
                  );
               } else {
                  long scheduledTime = this.autoApplyScheduledTime.get();
                  if (now >= scheduledTime - 2000L) {
                     LOGGER.at(Level.INFO).log("Auto-apply delay expired - applying update %s", stagedVersion);
                     this.broadcastToPlayers(Message.translation("server.update.auto_apply_now").param("version", stagedVersion).color(Color.RED));
                     this.triggerAutoApply();
                  } else {
                     long remainingMinutes = (scheduledTime - now) / 60000L;
                     long warnInterval = remainingMinutes <= 1L ? 30000L : 300000L;
                     long lastWarn = this.lastWarningTime.get();
                     if (now - lastWarn >= warnInterval && this.lastWarningTime.compareAndSet(lastWarn, now)) {
                        LOGGER.at(Level.INFO).log("Update %s will be auto-applied in %d minute(s)", stagedVersion, Math.max(1L, remainingMinutes));
                        this.broadcastToPlayers(
                           Message.translation("server.update.auto_apply_warning")
                              .param("version", stagedVersion)
                              .param("minutes", Math.max(1L, remainingMinutes))
                              .color(Color.YELLOW)
                        );
                     }
                  }
               }
            }
         }
      }
   }

   private void triggerAutoApply() {
      this.autoApplyScheduledTime.set(0L);
      HytaleServer.get().shutdownServer(ShutdownReason.UPDATE);
   }

   private void broadcastToPlayers(@Nonnull Message message) {
      Universe universe = Universe.get();
      if (universe != null) {
         for (PlayerRef player : universe.getPlayers()) {
            player.sendMessage(message);
         }
      }
   }

   private void notifyPlayers(@Nonnull String version) {
      Universe universe = Universe.get();
      if (universe != null) {
         Message message = Message.translation("server.update.notify_players").param("version", version);
         PermissionsModule permissionsModule = PermissionsModule.get();

         for (PlayerRef player : universe.getPlayers()) {
            if (permissionsModule.hasPermission(player.getUuid(), "hytale.system.update.notify")) {
               player.sendMessage(message);
            }
         }
      }
   }

   public record DownloadProgress(int percent, long downloadedBytes, long totalBytes, long etaSeconds) {
   }
}
