package com.hypixel.hytale.server.core.telemetry;

import com.hypixel.hytale.common.plugin.PluginManifest;
import com.hypixel.hytale.common.util.java.ManifestUtil;
import com.hypixel.hytale.metrics.metric.HistoricMetric;
import com.hypixel.hytale.server.core.Constants;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.HytaleServerConfig;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.PluginBase;
import com.hypixel.hytale.server.core.plugin.PluginManager;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.sun.management.OperatingSystemMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.RuntimeMXBean;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HexFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

final class TelemetryDataCollector {
   private static final int ONE_MINUTE_INDEX = 1;

   private TelemetryDataCollector() {
   }

   @Nonnull
   static TelemetryPackets.ServerStartPacket collectServerStart(@Nonnull String sessionId, @Nonnull String timestamp, int sequence) {
      return new TelemetryPackets.ServerStartPacket(
         sessionId,
         timestamp,
         sequence,
         TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - HytaleServer.get().getBootStart()),
         collectServerInfo(),
         collectPlatformInfo(),
         collectHardwareInfo(),
         collectConfigInfo(),
         collectModsInfo()
      );
   }

   @Nonnull
   static TelemetryPackets.ServerHeartbeatPacket collectHeartbeat(
      @Nonnull String sessionId, @Nonnull String timestamp, int sequence, int uptimeSeconds, @Nonnull TelemetryService.NetworkStats networkStats
   ) {
      return new TelemetryPackets.ServerHeartbeatPacket(
         sessionId, timestamp, sequence, uptimeSeconds, collectPerformanceInfo(), collectCapacityInfo(), collectMemoryInfo(), collectNetworkInfo(networkStats)
      );
   }

   @Nonnull
   private static TelemetryPackets.ServerInfo collectServerInfo() {
      String version = ManifestUtil.getImplementationVersion();
      String revisionId = ManifestUtil.getImplementationRevisionId();
      String configuration = "debug";
      return new TelemetryPackets.ServerInfo(version != null ? version : "unknown", revisionId != null ? revisionId : "unknown", configuration);
   }

   @Nonnull
   private static TelemetryPackets.ServerPlatformInfo collectPlatformInfo() {
      String os = System.getProperty("os.name", "unknown");
      String osVersion = System.getProperty("os.version", "unknown");
      String architecture = System.getProperty("os.arch", "unknown");
      RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
      String javaVersion = runtimeMXBean.getSpecVersion();
      String javaVendor = runtimeMXBean.getVmVendor();
      return new TelemetryPackets.ServerPlatformInfo(os, osVersion, architecture, javaVersion, javaVendor);
   }

   @Nullable
   private static TelemetryPackets.ServerHardwareInfo collectHardwareInfo() {
      if (Constants.SINGLEPLAYER) {
         return null;
      } else {
         String machineIdHash = generateMachineIdHash();
         int cpuCores = Runtime.getRuntime().availableProcessors();
         int systemMemoryMb = 0;
         if (ManagementFactory.getOperatingSystemMXBean() instanceof OperatingSystemMXBean sunBean) {
            systemMemoryMb = (int)(sunBean.getTotalMemorySize() / 1048576L);
         }

         return new TelemetryPackets.ServerHardwareInfo(machineIdHash, cpuCores, systemMemoryMb);
      }
   }

   @Nonnull
   private static String generateMachineIdHash() {
      try {
         StringBuilder sb = new StringBuilder();
         Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

         while (interfaces.hasMoreElements()) {
            NetworkInterface netInterface = interfaces.nextElement();
            byte[] mac = netInterface.getHardwareAddress();
            if (mac != null) {
               sb.append(HexFormat.of().formatHex(mac));
            }
         }

         sb.append(System.getenv("COMPUTERNAME"));
         sb.append(System.getenv("HOSTNAME"));
         MessageDigest digest = MessageDigest.getInstance("SHA-256");
         byte[] hash = digest.digest(sb.toString().getBytes(StandardCharsets.UTF_8));
         return HexFormat.of().formatHex(hash);
      } catch (Exception var4) {
         return "unknown";
      }
   }

   @Nonnull
   private static TelemetryPackets.ServerConfigInfo collectConfigInfo() {
      HytaleServerConfig config = HytaleServer.get().getConfig();
      int maxPlayers = config.getMaxPlayers();
      Universe universe = Universe.get();
      int worldCount = universe != null ? universe.getWorlds().size() : 0;
      return new TelemetryPackets.ServerConfigInfo(maxPlayers, worldCount, !Constants.SINGLEPLAYER);
   }

   @Nonnull
   private static List<TelemetryPackets.ModInfo> collectModsInfo() {
      PluginManager pluginManager = PluginManager.get();
      if (pluginManager == null) {
         return List.of();
      } else {
         List<PluginBase> plugins = pluginManager.getPlugins();
         List<TelemetryPackets.ModInfo> mods = new ArrayList<>(plugins.size());

         for (PluginBase plugin : plugins) {
            PluginManifest manifest = plugin.getManifest();
            String identifier = plugin.getIdentifier().toString();
            String version = manifest.getVersion() != null ? manifest.getVersion().toString() : "unknown";
            String type = plugin.getType().name().toLowerCase();
            boolean builtin = plugin instanceof JavaPlugin javaPlugin && javaPlugin.getClassLoader().isInServerClassPath();
            mods.add(new TelemetryPackets.ModInfo(identifier, version, type, builtin));
         }

         return mods;
      }
   }

   @Nonnull
   private static TelemetryPackets.ServerPerformanceInfo collectPerformanceInfo() {
      Universe universe = Universe.get();
      if (universe == null) {
         return new TelemetryPackets.ServerPerformanceInfo(0.0F, 0.0F, 0.0F);
      } else {
         Collection<World> worlds = universe.getWorlds().values();
         if (worlds.isEmpty()) {
            return new TelemetryPackets.ServerPerformanceInfo(0.0F, 0.0F, 0.0F);
         } else {
            float totalTickDuration = 0.0F;
            float maxTickDuration = 0.0F;
            float totalPerformancePercent = 0.0F;
            int worldCount = 0;

            for (World world : worlds) {
               if (world.isAlive()) {
                  int worldTps = world.getTps();
                  HistoricMetric tickMetric = world.getBufferedTickLengthMetricSet();
                  float avgTickNanos = (float)tickMetric.getAverage(1);
                  float p99TickNanos = (float)tickMetric.calculateMax(1);
                  totalTickDuration += avgTickNanos;
                  if (p99TickNanos > maxTickDuration) {
                     maxTickDuration = p99TickNanos;
                  }

                  float targetTickNanos = 1.0E9F / worldTps;
                  float worldPerformancePercent = avgTickNanos > 0.0F ? Math.min(100.0F, targetTickNanos / avgTickNanos * 100.0F) : 100.0F;
                  totalPerformancePercent += worldPerformancePercent;
                  worldCount++;
               }
            }

            if (worldCount == 0) {
               return new TelemetryPackets.ServerPerformanceInfo(0.0F, 0.0F, 0.0F);
            } else {
               float avgTickDurationNanos = totalTickDuration / worldCount;
               float avgTickDurationMs = avgTickDurationNanos / 1000000.0F;
               float p99TickDurationMs = maxTickDuration / 1000000.0F;
               float tickPerformancePercent = totalPerformancePercent / worldCount;
               return new TelemetryPackets.ServerPerformanceInfo(tickPerformancePercent, avgTickDurationMs, p99TickDurationMs);
            }
         }
      }
   }

   @Nonnull
   private static TelemetryPackets.ServerCapacityInfo collectCapacityInfo() {
      Universe universe = Universe.get();
      HytaleServerConfig config = HytaleServer.get().getConfig();
      int onlinePlayers = universe != null ? universe.getPlayerCount() : 0;
      int maxPlayers = config != null ? config.getMaxPlayers() : 100;
      int activeWorlds = 0;
      int totalEntities = 0;
      int loadedChunks = 0;
      if (universe != null) {
         for (World world : universe.getWorlds().values()) {
            if (world.isAlive()) {
               activeWorlds++;
               totalEntities += world.getEntityStore().getStore().getEntityCount();
               loadedChunks += world.getChunkStore().getLoadedChunksCount();
            }
         }
      }

      return new TelemetryPackets.ServerCapacityInfo(onlinePlayers, maxPlayers, activeWorlds, totalEntities, loadedChunks);
   }

   @Nonnull
   private static TelemetryPackets.ServerMemoryInfo collectMemoryInfo() {
      MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
      MemoryUsage heapUsage = memoryMXBean.getHeapMemoryUsage();
      float heapUsedMb = (float)heapUsage.getUsed() / 1048576.0F;
      float heapMaxMb = (float)heapUsage.getMax() / 1048576.0F;
      int gcCollections = 0;
      long gcTimeMs = 0L;

      for (GarbageCollectorMXBean gcBean : ManagementFactory.getGarbageCollectorMXBeans()) {
         long count = gcBean.getCollectionCount();
         long time = gcBean.getCollectionTime();
         if (count >= 0L) {
            gcCollections = (int)(gcCollections + count);
         }

         if (time >= 0L) {
            gcTimeMs += time;
         }
      }

      return new TelemetryPackets.ServerMemoryInfo(heapUsedMb, heapMaxMb, gcCollections, gcTimeMs);
   }

   @Nonnull
   private static TelemetryPackets.ServerNetworkInfo collectNetworkInfo(@Nonnull TelemetryService.NetworkStats stats) {
      return new TelemetryPackets.ServerNetworkInfo(stats.sentBytesPerSecond(), stats.receivedBytesPerSecond(), stats.totalSentKb(), stats.totalReceivedKb());
   }
}
