package com.hypixel.hytale.server.core.discovery;

import com.hypixel.hytale.common.plugin.PluginManifest;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.auth.ServerAuthManager;
import com.hypixel.hytale.server.core.discovery.command.DiscoveryCommand;
import com.hypixel.hytale.server.core.event.events.BootEvent;
import com.hypixel.hytale.server.core.liveconfig.LiveConfigModule;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.util.Config;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class DiscoveryModule extends JavaPlugin {
   private static final int MAX_CONSECUTIVE_AUTH_FAILURES = 5;
   @Nonnull
   public static final PluginManifest MANIFEST = PluginManifest.corePlugin(DiscoveryModule.class).build();
   private final Config<DiscoveryModuleConfig> config = this.withConfig("DiscoveryModule", DiscoveryModuleConfig.CODEC);
   private final DiscoveryService service = new DiscoveryService();
   @Nullable
   private ScheduledFuture<?> heartbeatTask;
   private final AtomicInteger consecutiveAuthFailures = new AtomicInteger(0);

   public DiscoveryModule(@NonNullDecl JavaPluginInit init) {
      super(init);
   }

   @Override
   protected void setup() {
      if (ServerAuthManager.getInstance().getAuthMode() != ServerAuthManager.AuthMode.SINGLEPLAYER) {
         this.getCommandRegistry().registerCommand(new DiscoveryCommand(this));
         this.getEventRegistry().register(BootEvent.class, this::onBoot);
      }
   }

   @Override
   protected void shutdown() {
      this.stopSendingHeartbeats();
   }

   private void onBoot(BootEvent event) {
      if (this.config.get().getDiscoveryToken() != null) {
         this.startSendingHeartbeats();
      }
   }

   public void setDiscoveryToken(@Nullable String token) {
      this.config.get().setDiscoveryToken(token);
      this.config.save();
      if (token == null) {
         this.stopSendingHeartbeats();
      } else {
         this.startSendingHeartbeats();
      }
   }

   public boolean hasDiscoveryToken() {
      return this.config.get().getDiscoveryToken() != null;
   }

   public DiscoveryService getService() {
      return this.service;
   }

   public static boolean isDiscoveryEnabled() {
      LiveConfigModule liveConfigModule = LiveConfigModule.get();
      return liveConfigModule == null ? true : liveConfigModule.getBoolFlag("enable_server_discovery_heartbeats", true);
   }

   private void startSendingHeartbeats() {
      if (this.heartbeatTask != null) {
         this.stopSendingHeartbeats();
      }

      this.heartbeatTask = HytaleServer.SCHEDULED_EXECUTOR.scheduleAtFixedRate(this::sendHeartbeat, 20L, 60L, TimeUnit.SECONDS);
   }

   private void stopSendingHeartbeats() {
      if (this.heartbeatTask != null) {
         this.heartbeatTask.cancel(false);
         this.heartbeatTask = null;
      }
   }

   private void sendHeartbeat() {
      String discoveryToken = this.config.get().getDiscoveryToken();
      if (discoveryToken == null) {
         this.stopSendingHeartbeats();
      } else if (ServerAuthManager.getInstance().hasSessionToken()) {
         if (isDiscoveryEnabled()) {
            boolean authFailure = this.service.sendHeartbeat(discoveryToken);
            if (authFailure) {
               if (this.consecutiveAuthFailures.incrementAndGet() > 5) {
                  this.setDiscoveryToken(null);
               }
            } else {
               this.consecutiveAuthFailures.set(0);
            }
         }
      }
   }
}
