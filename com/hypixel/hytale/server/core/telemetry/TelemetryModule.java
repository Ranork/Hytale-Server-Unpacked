package com.hypixel.hytale.server.core.telemetry;

import com.hypixel.hytale.common.plugin.PluginManifest;
import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.HytaleServerConfig;
import com.hypixel.hytale.server.core.ShutdownReason;
import com.hypixel.hytale.server.core.event.events.BootEvent;
import com.hypixel.hytale.server.core.event.events.ShutdownEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class TelemetryModule extends JavaPlugin {
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   @Nonnull
   public static final PluginManifest MANIFEST = PluginManifest.corePlugin(TelemetryModule.class).depends(Universe.class).build();
   @Nonnull
   private final TelemetryService telemetryService = new TelemetryService();

   public TelemetryModule(@Nonnull JavaPluginInit init) {
      super(init);
   }

   @Override
   protected void setup() {
      HytaleServerConfig.Module moduleConfig = HytaleServer.get().getConfig().getModule("Telemetry");
      boolean enabled = moduleConfig.isEnabled(true);
      this.telemetryService.setEnabled(enabled);
      if (!enabled) {
         LOGGER.at(Level.INFO).log("Server telemetry is disabled");
      } else {
         LOGGER.at(Level.INFO).log("Server telemetry is enabled");
         EventRegistry eventRegistry = this.getEventRegistry();
         eventRegistry.register(BootEvent.class, this::onBoot);
         eventRegistry.register(PlayerConnectEvent.class, this::onPlayerConnect);
         eventRegistry.register((short)-56, ShutdownEvent.class, this::onShutdown);
      }
   }

   @Override
   protected void start() {
   }

   @Override
   protected void shutdown() {
      this.telemetryService.shutdown();
   }

   private void onBoot(@Nonnull BootEvent event) {
      if (this.telemetryService.isEnabled()) {
         LOGGER.at(Level.INFO).log("Sending server start telemetry...");
         this.telemetryService.sendServerStart();
      }
   }

   private void onPlayerConnect(@Nonnull PlayerConnectEvent event) {
      if (this.telemetryService.isEnabled()) {
         PlayerRef playerRef = event.getPlayerRef();
         this.telemetryService.recordPlayerConnect(playerRef.getUuid());
      }
   }

   private void onShutdown(@Nonnull ShutdownEvent event) {
      if (this.telemetryService.isEnabled()) {
         ShutdownReason reason = HytaleServer.get().getShutdownReason();
         String reasonStr = reason != null ? reason.getName() : "unknown";
         LOGGER.at(Level.INFO).log("Sending server stop telemetry (reason: %s)...", reasonStr);
         this.telemetryService.sendServerStop(reasonStr);
      }
   }
}
