package com.hypixel.hytale.server.core.util;

import com.hypixel.hytale.common.plugin.PluginManifest;
import com.hypixel.hytale.common.util.FormatUtil;
import com.hypixel.hytale.common.util.StringUtil;
import com.hypixel.hytale.component.ComponentRegistry;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.metric.ArchetypeChunkData;
import com.hypixel.hytale.component.system.ISystem;
import com.hypixel.hytale.logger.backend.HytaleFileHandler;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.metrics.InitStackThread;
import com.hypixel.hytale.metrics.MetricsRegistry;
import com.hypixel.hytale.metrics.metric.HistoricMetric;
import com.hypixel.hytale.plugin.early.ClassTransformer;
import com.hypixel.hytale.plugin.early.EarlyPluginLoader;
import com.hypixel.hytale.protocol.io.PacketStatsRecorder;
import com.hypixel.hytale.protocol.packets.connection.PongType;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.HytaleServerConfig;
import com.hypixel.hytale.server.core.ShutdownReason;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.CameraManager;
import com.hypixel.hytale.server.core.entity.entities.player.movement.MovementManager;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.PluginBase;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.universe.world.worldgen.WorldGenTimingsCollector;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.management.ClassLoadingMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bouncycastle.util.io.TeeOutputStream;
import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.bson.BsonValue;

public class DumpUtil {
   @Nonnull
   public static Path dumpToJson() throws IOException {
      Map<UUID, BsonDocument> playerComponents = collectPlayerComponentMetrics();
      BsonDocument bson = HytaleServer.METRICS_REGISTRY.dumpToBson(HytaleServer.get()).asDocument();
      BsonDocument universeBson = Universe.METRICS_REGISTRY.dumpToBson(Universe.get()).asDocument();
      BsonArray playersArray = new BsonArray();

      for (PlayerRef ref : Universe.get().getPlayers()) {
         BsonDocument playerBson = PlayerRef.METRICS_REGISTRY.dumpToBson(ref).asDocument();
         BsonDocument componentData = playerComponents.get(ref.getUuid());
         if (componentData != null) {
            playerBson.putAll(componentData);
         }

         playersArray.add(playerBson);
      }

      universeBson.put("Players", playersArray);
      bson.put("Universe", universeBson);
      BsonArray earlyPluginsArray = new BsonArray();

      for (ClassTransformer transformer : EarlyPluginLoader.getTransformers()) {
         earlyPluginsArray.add(new BsonString(transformer.getClass().getName()));
      }

      bson.put("EarlyPlugins", earlyPluginsArray);
      Path path = MetricsRegistry.createDumpPath(".dump.json");
      Files.writeString(path, BsonUtil.toJson(bson));
      return path;
   }

   @Nonnull
   private static Map<UUID, BsonDocument> collectPlayerComponentMetrics() {
      ConcurrentHashMap<UUID, BsonDocument> result = new ConcurrentHashMap<>();
      Collection<World> worlds = Universe.get().getWorlds().values();
      CompletableFuture[] futures = worlds.stream().map(world -> CompletableFuture.runAsync(() -> {
         for (PlayerRef playerRef : world.getPlayerRefs()) {
            BsonValue bson = PlayerRef.COMPONENT_METRICS_REGISTRY.dumpToBson(playerRef);
            result.put(playerRef.getUuid(), bson.asDocument());
         }
      }, world).orTimeout(30L, TimeUnit.SECONDS)).toArray(CompletableFuture[]::new);
      CompletableFuture.allOf(futures).join();
      return result;
   }

   @Nonnull
   public static Map<UUID, DumpUtil.PlayerTextData> collectPlayerTextData() {
      ConcurrentHashMap<UUID, DumpUtil.PlayerTextData> result = new ConcurrentHashMap<>();
      Collection<World> worlds = Universe.get().getWorlds().values();
      CompletableFuture[] futures = worlds.stream()
         .map(
            world -> CompletableFuture.runAsync(
                  () -> {
                     for (PlayerRef playerRef : world.getPlayerRefs()) {
                        Ref<EntityStore> entityRef = playerRef.getReference();
                        if (entityRef != null) {
                           Store<EntityStore> store = entityRef.getStore();
                           MovementStatesComponent ms = store.getComponent(entityRef, MovementStatesComponent.getComponentType());
                           MovementManager mm = store.getComponent(entityRef, MovementManager.getComponentType());
                           CameraManager cm = store.getComponent(entityRef, CameraManager.getComponentType());
                           result.put(
                              playerRef.getUuid(),
                              new DumpUtil.PlayerTextData(
                                 playerRef.getUuid(),
                                 ms != null ? ms.getMovementStates().toString() : null,
                                 mm != null ? mm.toString() : null,
                                 cm != null ? cm.toString() : null
                              )
                           );
                        }
                     }
                  },
                  world
               )
               .orTimeout(30L, TimeUnit.SECONDS)
         )
         .toArray(CompletableFuture[]::new);
      CompletableFuture.allOf(futures).join();
      return result;
   }

   @Nonnull
   public static String hexDump(@Nonnull ByteBuf buf) {
      int readerIndex = buf.readerIndex();
      byte[] data = new byte[buf.readableBytes()];
      buf.readBytes(data);
      buf.readerIndex(readerIndex);
      return hexDump(data);
   }

   @Nonnull
   public static String hexDump(@Nonnull byte[] data) {
      return data.length == 0 ? "[EMPTY ARRAY]" : ByteBufUtil.hexDump(data);
   }

   @Nonnull
   public static Path dump(boolean crash, boolean printToConsole) {
      Path filePath = createDumpPath(crash, "dump.txt");
      FileOutputStream fileOutputStream = null;

      OutputStream outputStream;
      try {
         fileOutputStream = new FileOutputStream(filePath.toFile());
         if (printToConsole) {
            outputStream = new TeeOutputStream(fileOutputStream, System.err);
         } else {
            outputStream = fileOutputStream;
         }
      } catch (IOException var13) {
         var13.printStackTrace();
         System.err.println();
         System.err.println("FAILED TO GET OUTPUT STREAM FOR " + filePath);
         System.err.println("FAILED TO GET OUTPUT STREAM FOR " + filePath);
         System.err.println();
         outputStream = System.err;
      }

      try {
         write(new PrintWriter(new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)), true));
      } finally {
         if (fileOutputStream != null) {
            try {
               fileOutputStream.close();
            } catch (IOException var12) {
            }
         }
      }

      return filePath;
   }

   @Nonnull
   public static Path createDumpPath(boolean crash, String ext) {
      Path path = Paths.get("dumps");

      try {
         if (!Files.exists(path)) {
            Files.createDirectories(path);
         }
      } catch (IOException var6) {
         var6.printStackTrace();
      }

      String name = (crash ? "crash-" : "") + HytaleFileHandler.LOG_FILE_DATE_FORMAT.format(LocalDateTime.now());
      Path filePath = path.resolve(name + "." + ext);
      int i = 0;

      while (Files.exists(filePath)) {
         filePath = path.resolve(name + "_" + i++ + "." + ext);
      }

      return filePath;
   }

   private static void write(@Nonnull PrintWriter writer) {
      int width = 200;
      int height = 20;
      long startNanos = System.nanoTime();
      section(
         "Summary",
         () -> {
            Universe universe = Universe.get();
            writer.println("World Count: " + universe.getWorlds().size());

            for (World world : universe.getWorlds().values()) {
               writer.println("- " + world.getName());
               HistoricMetric metrics = world.getBufferedTickLengthMetricSet();
               long[] periodsNanos = metrics.getPeriodsNanos();
               int periodIndex = periodsNanos.length - 1;
               long lastTime = periodsNanos[periodIndex];
               double average = metrics.getAverage(periodIndex);
               long max = metrics.calculateMax(periodIndex);
               long min = metrics.calculateMin(periodIndex);
               String length = FormatUtil.timeUnitToString(lastTime, TimeUnit.NANOSECONDS, true);
               String value = FormatUtil.simpleTimeUnitFormat(min, average, max, TimeUnit.NANOSECONDS, TimeUnit.MILLISECONDS, 3);
               String limit = FormatUtil.simpleTimeUnitFormat(world.getTickStepNanos(), TimeUnit.NANOSECONDS, 3);
               writer.printf("\tTick (%s): %s (Limit: %s)\n", length, value, limit);
               writer.printf("\tPlayer count: %d\n", world.getPlayerCount());
            }

            writer.println("Player count: " + universe.getPlayerCount());

            for (PlayerRef ref : universe.getPlayers()) {
               writer.printf("- %s (%s)\n", ref.getUsername(), ref.getUuid());
               PacketHandler.PingInfo pingInfo = ref.getPacketHandler().getPingInfo(PongType.Raw);
               HistoricMetric pingMetricSet = pingInfo.getPingMetricSet();
               long min = pingMetricSet.calculateMin(1);
               long average = (long)pingMetricSet.getAverage(1);
               long max = pingMetricSet.calculateMax(1);
               writer.println(
                  "\tPing(raw) Min: "
                     + FormatUtil.timeUnitToString(min, PacketHandler.PingInfo.TIME_UNIT)
                     + ", Avg: "
                     + FormatUtil.timeUnitToString(average, PacketHandler.PingInfo.TIME_UNIT)
                     + ", Max: "
                     + FormatUtil.timeUnitToString(max, PacketHandler.PingInfo.TIME_UNIT)
               );
            }
         },
         writer
      );
      section("Server Lifecycle", () -> {
         HytaleServer server = HytaleServer.get();
         writer.println("Boot Timestamp: " + server.getBoot());
         writer.println("Boot Start (nanos): " + server.getBootStart());
         writer.println("Booting: " + server.isBooting());
         writer.println("Booted: " + server.isBooted());
         writer.println("Shutting Down: " + server.isShuttingDown());
         ShutdownReason shutdownReason = server.getShutdownReason();
         if (shutdownReason != null) {
            writer.println("Shutdown Reason: " + shutdownReason);
         }
      }, writer);
      section("Early Plugins", () -> {
         List<ClassTransformer> transformers = EarlyPluginLoader.getTransformers();
         writer.println("Class Transformer Count: " + transformers.size());

         for (ClassTransformer transformer : transformers) {
            writer.println("- " + transformer.getClass().getName() + " (priority=" + transformer.priority() + ")");
         }
      }, writer);
      section("Plugins", () -> {
         List<PluginBase> plugins = HytaleServer.get().getPluginManager().getPlugins();
         writer.println("Plugin Count: " + plugins.size());

         for (PluginBase plugin : plugins) {
            boolean isBuiltin = plugin instanceof JavaPlugin javaPlugin && javaPlugin.getClassLoader().isInServerClassPath();
            writer.println("- " + plugin.getIdentifier() + (isBuiltin ? " [Builtin]" : " [External]"));
            writer.println("\tType: " + plugin.getType().getDisplayName());
            writer.println("\tState: " + plugin.getState());
            writer.println("\tManifest:");
            BsonDocument manifestBson = PluginManifest.CODEC.encode(plugin.getManifest()).asDocument();
            printIndented(writer, BsonUtil.toJson(manifestBson), "\t\t");
         }
      }, writer);
      section("Server Config", () -> {
         HytaleServerConfig config = HytaleServer.get().getConfig();
         BsonDocument bson = HytaleServerConfig.CODEC.encode(config).asDocument();
         printIndented(writer, BsonUtil.toJson(bson), "\t");
      }, writer);
      Map<UUID, DumpUtil.PlayerTextData> playerTextData = collectPlayerTextData();
      section(
         "Server Info",
         () -> {
            writer.println("HytaleServer:");
            writer.println("\t- " + HytaleServer.get());
            writer.println("\tBooted: " + HytaleServer.get().isBooting());
            writer.println("\tShutting Down: " + HytaleServer.get().isShuttingDown());
            writer.println();
            writer.println("Worlds: ");
            Map<String, World> worlds = Universe.get().getWorlds();
            worlds.forEach(
               (worldName, world) -> {
                  writer.println("- " + worldName + ":");
                  writer.println("\t" + world);
                  HistoricMetric bufferedDeltaMetricSet = world.getBufferedTickLengthMetricSet();
                  long[] periods = bufferedDeltaMetricSet.getPeriodsNanos();

                  for (int i = 0; i < periods.length; i++) {
                     long period = periods[i];
                     String historyLengthFormatted = FormatUtil.timeUnitToString(period, TimeUnit.NANOSECONDS, true);
                     double average = bufferedDeltaMetricSet.getAverage(i);
                     long minxx = bufferedDeltaMetricSet.calculateMin(i);
                     long maxxx = bufferedDeltaMetricSet.calculateMax(i);
                     writer.println(
                        "\tTick ("
                           + historyLengthFormatted
                           + "): Min: "
                           + FormatUtil.simpleTimeUnitFormat(minxx, TimeUnit.NANOSECONDS, 3)
                           + ", Avg: "
                           + FormatUtil.simpleTimeUnitFormat(Math.round(average), TimeUnit.NANOSECONDS, 3)
                           + "ns, Max: "
                           + FormatUtil.simpleTimeUnitFormat(maxxx, TimeUnit.NANOSECONDS, 3)
                     );
                     long[] historyTimestamps = bufferedDeltaMetricSet.getTimestamps(i);
                     long[] historyValues = bufferedDeltaMetricSet.getValues(i);
                     StringBuilder sb = new StringBuilder();
                     sb.append("\tTick Graph ").append(historyLengthFormatted).append(":\n");
                     StringUtil.generateGraph(
                        sb,
                        width,
                        height,
                        startNanos - period,
                        startNanos,
                        0.0,
                        Math.max(maxxx, (long)world.getTickStepNanos()),
                        value -> FormatUtil.simpleTimeUnitFormat(MathUtil.fastCeil(value), TimeUnit.NANOSECONDS, 3),
                        historyTimestamps.length,
                        ii -> historyTimestamps[ii],
                        ii -> historyValues[ii]
                     );
                     writer.println(sb);
                  }

                  writer.println("\tPlayers: ");

                  for (Player player : world.getPlayers()) {
                     writer.println("\t- " + player);
                     DumpUtil.PlayerTextData playerData = playerTextData.get(player.getUuid());
                     writer.println("\t\tMovement States: " + (playerData != null ? playerData.movementStates() : "N/A"));
                     writer.println("\t\tMovement Manager: " + (playerData != null ? playerData.movementManager() : "N/A"));
                     writer.println("\t\tPage Manager: " + player.getPageManager());
                     writer.println("\t\tHud Manager: " + player.getHudManager());
                     writer.println("\t\tCamera Manager: " + (playerData != null ? playerData.cameraManager() : "N/A"));
                     writer.println("\t\tChunk Tracker:");

                     for (String line : player.getPlayerRef().getChunkTracker().getLoadedChunksDebug().split("\n")) {
                        writer.println("\t\t\t" + line);
                     }

                     writer.println("\t\tQueued Packets Count: " + player.getPlayerConnection().getQueuedPacketsCount());
                     writer.println("\t\tPing:");

                     for (PongType pongType : PongType.values()) {
                        PacketHandler.PingInfo pingInfox = player.getPlayerConnection().getPingInfo(pongType);
                        writer.println("\t\t- " + pongType.name() + ":");
                        HistoricMetric pingMetricSetx = pingInfox.getPingMetricSet();
                        long average = (long)pingMetricSetx.getAverage(1);
                        long minx = pingMetricSetx.calculateMin(1);
                        long maxxxx = pingMetricSetx.calculateMax(1);
                        writer.println("\t\t\tPing: Min: " + minx + ", Avg: " + average + ", Max: " + maxxxx);
                        writer.println(
                           "\t\t\t      Min: "
                              + FormatUtil.timeUnitToString(minx, PacketHandler.PingInfo.TIME_UNIT)
                              + ", Avg: "
                              + FormatUtil.timeUnitToString(average, PacketHandler.PingInfo.TIME_UNIT)
                              + ", Max: "
                              + FormatUtil.timeUnitToString(maxxxx, PacketHandler.PingInfo.TIME_UNIT)
                        );
                        long[] pingPeriods = pingMetricSetx.getPeriodsNanos();

                        for (int i = 0; i < pingPeriods.length; i++) {
                           minx = pingPeriods[i];
                           maxxxx = pingMetricSetx.calculateMin(1);
                           long maxx = pingMetricSetx.calculateMax(1);
                           long[] historyTimestamps = pingMetricSetx.getTimestamps(i);
                           long[] historyValues = pingMetricSetx.getValues(i);
                           String historyLengthFormatted = FormatUtil.timeUnitToString(minx, TimeUnit.NANOSECONDS, true);
                           StringBuilder sb = new StringBuilder();
                           sb.append("\t\t\tPing Graph ").append(historyLengthFormatted).append(":\n");
                           StringUtil.generateGraph(
                              sb,
                              width,
                              height,
                              startNanos - minx,
                              startNanos,
                              maxxxx,
                              maxx,
                              value -> FormatUtil.timeUnitToString(MathUtil.fastCeil(value), PacketHandler.PingInfo.TIME_UNIT),
                              historyTimestamps.length,
                              ii -> historyTimestamps[ii],
                              ii -> historyValues[ii]
                           );
                           writer.println(sb);
                        }

                        writer.println(
                           "\t\t\tPacket Queue: Min: "
                              + pingInfox.getPacketQueueMetric().getMin()
                              + ", Avg: "
                              + (long)pingInfox.getPacketQueueMetric().getAverage()
                              + ", Max: "
                              + pingInfox.getPacketQueueMetric().getMax()
                        );
                     }

                     writer.println();
                     PacketStatsRecorder recorder = player.getPlayerConnection().getPacketStatsRecorder();
                     if (recorder != null) {
                        int recentSeconds = 30;
                        long totalSentCount = 0L;
                        long totalSentUncompressed = 0L;
                        long totalSentWire = 0L;
                        int recentSentCount = 0;
                        long recentSentUncompressed = 0L;
                        long recentSentWire = 0L;
                        writer.println("\t\tPackets Sent:");

                        for (int i = 0; i < 512; i++) {
                           PacketStatsRecorder.PacketStatsEntry entry = recorder.getEntry(i);
                           if (entry.getSentCount() > 0) {
                              totalSentCount += entry.getSentCount();
                              totalSentUncompressed += entry.getSentUncompressedTotal();
                              totalSentWire += entry.getSentCompressedTotal() > 0L ? entry.getSentCompressedTotal() : entry.getSentUncompressedTotal();
                              writer.println("\t\t\t" + entry.getName() + " (" + i + "):");
                              printPacketStats(
                                 writer,
                                 "\t\t\t\t",
                                 "Total",
                                 entry.getSentCount(),
                                 entry.getSentUncompressedTotal(),
                                 entry.getSentCompressedTotal(),
                                 entry.getSentUncompressedMin(),
                                 entry.getSentUncompressedMax(),
                                 entry.getSentCompressedMin(),
                                 entry.getSentCompressedMax(),
                                 entry.getSentUncompressedAvg(),
                                 entry.getSentCompressedAvg(),
                                 0
                              );
                              PacketStatsRecorder.RecentStats recent = entry.getSentRecently();
                              if (recent.count() > 0) {
                                 recentSentCount += recent.count();
                                 recentSentUncompressed += recent.uncompressedTotal();
                                 recentSentWire += recent.compressedTotal() > 0L ? recent.compressedTotal() : recent.uncompressedTotal();
                                 printPacketStats(
                                    writer,
                                    "\t\t\t\t",
                                    "Recent",
                                    recent.count(),
                                    recent.uncompressedTotal(),
                                    recent.compressedTotal(),
                                    recent.uncompressedMin(),
                                    recent.uncompressedMax(),
                                    recent.compressedMin(),
                                    recent.compressedMax(),
                                    (double)recent.uncompressedTotal() / recent.count(),
                                    recent.compressedTotal() > 0L ? (double)recent.compressedTotal() / recent.count() : 0.0,
                                    recentSeconds
                                 );
                              }
                           }
                        }

                        writer.println("\t\t\t--- Summary ---");
                        writer.println(
                           "\t\t\t\tTotal: "
                              + totalSentCount
                              + " packets, "
                              + FormatUtil.bytesToString(totalSentUncompressed)
                              + " serialized, "
                              + FormatUtil.bytesToString(totalSentWire)
                              + " wire"
                        );
                        if (recentSentCount > 0) {
                           writer.println(
                              String.format(
                                 "\t\t\t\tRecent: %d packets (%.1f/sec), %s serialized, %s wire",
                                 recentSentCount,
                                 (double)recentSentCount / recentSeconds,
                                 FormatUtil.bytesToString(recentSentUncompressed),
                                 FormatUtil.bytesToString(recentSentWire)
                              )
                           );
                        }

                        writer.println();
                        long totalRecvCount = 0L;
                        long totalRecvUncompressed = 0L;
                        long totalRecvWire = 0L;
                        int recentRecvCount = 0;
                        long recentRecvUncompressed = 0L;
                        long recentRecvWire = 0L;
                        writer.println("\t\tPackets Received:");

                        for (int ix = 0; ix < 512; ix++) {
                           PacketStatsRecorder.PacketStatsEntry entry = recorder.getEntry(ix);
                           if (entry.getReceivedCount() > 0) {
                              totalRecvCount += entry.getReceivedCount();
                              totalRecvUncompressed += entry.getReceivedUncompressedTotal();
                              totalRecvWire += entry.getReceivedCompressedTotal() > 0L
                                 ? entry.getReceivedCompressedTotal()
                                 : entry.getReceivedUncompressedTotal();
                              writer.println("\t\t\t" + entry.getName() + " (" + ix + "):");
                              printPacketStats(
                                 writer,
                                 "\t\t\t\t",
                                 "Total",
                                 entry.getReceivedCount(),
                                 entry.getReceivedUncompressedTotal(),
                                 entry.getReceivedCompressedTotal(),
                                 entry.getReceivedUncompressedMin(),
                                 entry.getReceivedUncompressedMax(),
                                 entry.getReceivedCompressedMin(),
                                 entry.getReceivedCompressedMax(),
                                 entry.getReceivedUncompressedAvg(),
                                 entry.getReceivedCompressedAvg(),
                                 0
                              );
                              PacketStatsRecorder.RecentStats recent = entry.getReceivedRecently();
                              if (recent.count() > 0) {
                                 recentRecvCount += recent.count();
                                 recentRecvUncompressed += recent.uncompressedTotal();
                                 recentRecvWire += recent.compressedTotal() > 0L ? recent.compressedTotal() : recent.uncompressedTotal();
                                 printPacketStats(
                                    writer,
                                    "\t\t\t\t",
                                    "Recent",
                                    recent.count(),
                                    recent.uncompressedTotal(),
                                    recent.compressedTotal(),
                                    recent.uncompressedMin(),
                                    recent.uncompressedMax(),
                                    recent.compressedMin(),
                                    recent.compressedMax(),
                                    (double)recent.uncompressedTotal() / recent.count(),
                                    recent.compressedTotal() > 0L ? (double)recent.compressedTotal() / recent.count() : 0.0,
                                    recentSeconds
                                 );
                              }
                           }
                        }

                        writer.println("\t\t\t--- Summary ---");
                        writer.println(
                           "\t\t\t\tTotal: "
                              + totalRecvCount
                              + " packets, "
                              + FormatUtil.bytesToString(totalRecvUncompressed)
                              + " serialized, "
                              + FormatUtil.bytesToString(totalRecvWire)
                              + " wire"
                        );
                        if (recentRecvCount > 0) {
                           writer.println(
                              String.format(
                                 "\t\t\t\tRecent: %d packets (%.1f/sec), %s serialized, %s wire",
                                 recentRecvCount,
                                 (double)recentRecvCount / recentSeconds,
                                 FormatUtil.bytesToString(recentRecvUncompressed),
                                 FormatUtil.bytesToString(recentRecvWire)
                              )
                           );
                        }

                        writer.println();
                     }
                  }

                  writer.println("\tComponent Stores:");

                  try {
                     CompletableFuture.runAsync(() -> {
                        printComponentStore(writer, width, height, "Chunks", startNanos, world.getChunkStore().getStore());
                        printComponentStore(writer, width, height, "Entities", startNanos, world.getEntityStore().getStore());
                     }, world).orTimeout(30L, TimeUnit.SECONDS).join();
                  } catch (CompletionException var40) {
                     if (!(var40.getCause() instanceof TimeoutException)) {
                        var40.printStackTrace();
                        writer.println("\t\tFAILED TO DUMP COMPONENT STORES! EXCEPTION!");
                     } else {
                        writer.println("\t\tFAILED TO DUMP COMPONENT STORES! TIMEOUT!");
                     }
                  }

                  writer.println();
                  writer.println();
                  WorldGenTimingsCollector timings = world.getChunkStore().getGenerator().getTimings();
                  writer.println("\tWorld Gen Timings: ");
                  if (timings != null) {
                     writer.println("\t\tChunk Count: " + timings.getChunkCounter());
                     writer.println("\t\tChunk Time: " + timings.getChunkTime());
                     writer.println("\t\tZone Biome Result Time: " + timings.zoneBiomeResult());
                     writer.println("\t\tPrepare Time: " + timings.prepare());
                     writer.println("\t\tBlock Generation Time: " + timings.blocksGeneration());
                     writer.println("\t\tCave Generation Time: " + timings.caveGeneration());
                     writer.println("\t\tPrefab Generation: " + timings.prefabGeneration());
                     writer.println("\t\tQueue Length: " + timings.getQueueLength());
                     writer.println("\t\tGenerating Count: " + timings.getGeneratingCount());
                  } else {
                     writer.println("\t\tNo Timings Data Collected!");
                  }
               }
            );
            List<PlayerRef> playersNotInWorld = Universe.get().getPlayers().stream().filter(refx -> refx.getReference() == null).toList();
            if (!playersNotInWorld.isEmpty()) {
               writer.println();
               writer.println("Players not in world (" + playersNotInWorld.size() + "):");

               for (PlayerRef ref : playersNotInWorld) {
                  writer.println("- " + ref.getUsername() + " (" + ref.getUuid() + ")");
                  writer.println("\tQueued Packets: " + ref.getPacketHandler().getQueuedPacketsCount());
                  PacketHandler.PingInfo pingInfo = ref.getPacketHandler().getPingInfo(PongType.Raw);
                  HistoricMetric pingMetricSet = pingInfo.getPingMetricSet();
                  long min = pingMetricSet.calculateMin(1);
                  long avg = (long)pingMetricSet.getAverage(1);
                  long max = pingMetricSet.calculateMax(1);
                  writer.println(
                     "\tPing(raw): Min: "
                        + FormatUtil.timeUnitToString(min, PacketHandler.PingInfo.TIME_UNIT)
                        + ", Avg: "
                        + FormatUtil.timeUnitToString(avg, PacketHandler.PingInfo.TIME_UNIT)
                        + ", Max: "
                        + FormatUtil.timeUnitToString(max, PacketHandler.PingInfo.TIME_UNIT)
                  );
               }
            }
         },
         writer
      );
      section(
         "System info",
         () -> {
            RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
            OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
            long currentTimeMillis = System.currentTimeMillis();
            writer.println("Start Time: " + new Date(runtimeMXBean.getStartTime()) + " (" + runtimeMXBean.getStartTime() + "ms)");
            writer.println("Current Time: " + new Date(currentTimeMillis) + " (" + currentTimeMillis + "ms)");
            writer.println(
               "Process Uptime: " + FormatUtil.timeUnitToString(runtimeMXBean.getUptime(), TimeUnit.MILLISECONDS) + " (" + runtimeMXBean.getUptime() + "ms)"
            );
            writer.println(
               "Available processors (cores): " + Runtime.getRuntime().availableProcessors() + " - " + operatingSystemMXBean.getAvailableProcessors()
            );
            writer.println("System Load Average: " + operatingSystemMXBean.getSystemLoadAverage());
            writer.println();
            if (operatingSystemMXBean instanceof com.sun.management.OperatingSystemMXBean sunOSBean) {
               writer.println(
                  "Total Physical Memory: "
                     + FormatUtil.bytesToString(sunOSBean.getTotalPhysicalMemorySize())
                     + " ("
                     + sunOSBean.getTotalPhysicalMemorySize()
                     + " Bytes)"
               );
               writer.println(
                  "Free Physical Memory: "
                     + FormatUtil.bytesToString(sunOSBean.getFreePhysicalMemorySize())
                     + " ("
                     + sunOSBean.getFreePhysicalMemorySize()
                     + " Bytes)"
               );
               writer.println(
                  "Total Swap Memory: " + FormatUtil.bytesToString(sunOSBean.getTotalSwapSpaceSize()) + " (" + sunOSBean.getTotalSwapSpaceSize() + " Bytes)"
               );
               writer.println(
                  "Free Swap Memory: " + FormatUtil.bytesToString(sunOSBean.getFreeSwapSpaceSize()) + " (" + sunOSBean.getFreeSwapSpaceSize() + " Bytes)"
               );
               writer.println("System CPU Load: " + sunOSBean.getSystemCpuLoad());
               writer.println("Process CPU Load: " + sunOSBean.getProcessCpuLoad());
               writer.println();
            }

            writer.println("Processor Identifier: " + System.getenv("PROCESSOR_IDENTIFIER"));
            writer.println("Processor Architecture: " + System.getenv("PROCESSOR_ARCHITECTURE"));
            writer.println("Processor Architecture W64/32: " + System.getenv("PROCESSOR_ARCHITEW6432"));
            writer.println("Number of Processors: " + System.getenv("NUMBER_OF_PROCESSORS"));
            writer.println();
            writer.println("Runtime Name: " + runtimeMXBean.getName());
            writer.println();
            writer.println("OS Name: " + operatingSystemMXBean.getName());
            writer.println("OS Arch: " + operatingSystemMXBean.getArch());
            writer.println("OS Version: " + operatingSystemMXBean.getVersion());
            writer.println();
            writer.println("Spec Name: " + runtimeMXBean.getSpecName());
            writer.println("Spec Vendor: " + runtimeMXBean.getSpecVendor());
            writer.println("Spec Version: " + runtimeMXBean.getSpecVersion());
            writer.println();
            writer.println("VM Name: " + runtimeMXBean.getVmName());
            writer.println("VM Vendor: " + runtimeMXBean.getVmVendor());
            writer.println("VM Version: " + runtimeMXBean.getVmVersion());
            writer.println();
            writer.println("Management Spec Version: " + runtimeMXBean.getManagementSpecVersion());
            writer.println();
            writer.println("Library Path: " + runtimeMXBean.getLibraryPath());

            try {
               writer.println("Boot ClassPath: " + runtimeMXBean.getBootClassPath());
            } catch (UnsupportedOperationException var6x) {
            }

            writer.println("ClassPath: " + runtimeMXBean.getClassPath());
            writer.println();
            writer.println("Input Arguments: " + runtimeMXBean.getInputArguments());
            writer.println("System Properties: " + runtimeMXBean.getSystemProperties());
         },
         writer
      );
      section("Current process info", () -> {
         MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
         writeMemoryUsage(writer, "Heap Memory Usage: ", memoryMXBean.getHeapMemoryUsage());
         writeMemoryUsage(writer, "Non-Heap Memory Usage: ", memoryMXBean.getNonHeapMemoryUsage());
         writer.println("Objects Pending Finalization Count: " + memoryMXBean.getObjectPendingFinalizationCount());
      }, writer);
      section("Garbage collector", () -> {
         for (GarbageCollectorMXBean garbageCollectorMXBean : ManagementFactory.getGarbageCollectorMXBeans()) {
            writer.println("Name: " + garbageCollectorMXBean.getName());
            writer.println("\tMemory Pool Names: " + Arrays.toString((Object[])garbageCollectorMXBean.getMemoryPoolNames()));
            writer.println("\tCollection Count: " + garbageCollectorMXBean.getCollectionCount());
            writer.println("\tCollection Time: " + garbageCollectorMXBean.getCollectionTime());
            writer.println();
         }
      }, writer);
      section("Memory pools", () -> {
         for (MemoryPoolMXBean memoryPoolMXBean : ManagementFactory.getMemoryPoolMXBeans()) {
            writer.println("Name: " + memoryPoolMXBean.getName());
            writer.println("\tType: " + memoryPoolMXBean.getType());
            writer.println("\tPeak Usage: " + memoryPoolMXBean.getPeakUsage());
            writer.println("\tUsage: " + memoryPoolMXBean.getUsage());
            writer.println("\tUsage Threshold Supported: " + memoryPoolMXBean.isUsageThresholdSupported());
            if (memoryPoolMXBean.isUsageThresholdSupported()) {
               writer.println("\tUsage Threshold: " + memoryPoolMXBean.getUsageThreshold());
               writer.println("\tUsage Threshold Count: " + memoryPoolMXBean.getUsageThresholdCount());
               writer.println("\tUsage Threshold Exceeded: " + memoryPoolMXBean.isUsageThresholdExceeded());
            }

            writer.println("\tCollection Usage: " + memoryPoolMXBean.getCollectionUsage());
            writer.println("\tCollection Usage Threshold Supported: " + memoryPoolMXBean.isCollectionUsageThresholdSupported());
            if (memoryPoolMXBean.isCollectionUsageThresholdSupported()) {
               writer.println("\tCollection Usage Threshold: " + memoryPoolMXBean.getCollectionUsageThreshold());
               writer.println("\tCollection Usage Threshold Count: " + memoryPoolMXBean.getCollectionUsageThresholdCount());
               writer.println("\tCollection Usage Threshold Exceeded: " + memoryPoolMXBean.isCollectionUsageThresholdExceeded());
            }

            writer.println();
         }
      }, writer);
      ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
      ThreadInfo[] threadInfos = threadMXBean.dumpAllThreads(true, true);
      section("Threads (Count: " + threadInfos.length + ")", () -> {
         Map<Thread, StackTraceElement[]> allStackTraces = Thread.getAllStackTraces();
         Long2ObjectMap<Thread> threadIdMap = new Long2ObjectOpenHashMap();

         for (Thread thread : allStackTraces.keySet()) {
            threadIdMap.put(thread.getId(), thread);
         }

         for (ThreadInfo threadInfo : threadInfos) {
            Thread thread = (Thread)threadIdMap.get(threadInfo.getThreadId());
            if (thread == null) {
               writer.println("Failed to find thread!!!");
            } else {
               writer.println("Name: " + thread.getName());
               writer.println("State: " + threadInfo.getThreadState());
               writer.println("Thread Class: " + thread.getClass());
               writer.println("Thread Group: " + thread.getThreadGroup());
               writer.println("Priority: " + thread.getPriority());
               writer.println("CPU Time: " + threadMXBean.getThreadCpuTime(threadInfo.getThreadId()));
               writer.println("Waited Time: " + threadInfo.getWaitedTime());
               writer.println("Waited Count: " + threadInfo.getWaitedCount());
               writer.println("Blocked Time: " + threadInfo.getBlockedTime());
               writer.println("Blocked Count: " + threadInfo.getBlockedCount());
               writer.println("Lock Name: " + threadInfo.getLockName());
               writer.println("Lock Owner Id: " + threadInfo.getLockOwnerId());
               writer.println("Lock Owner Name: " + threadInfo.getLockOwnerName());
               writer.println("Daemon: " + thread.isDaemon());
               writer.println("Interrupted: " + thread.isInterrupted());
               writer.println("Uncaught Exception Handler: " + thread.getUncaughtExceptionHandler().getClass());
               if (thread instanceof InitStackThread) {
                  writer.println("Init Stack: ");
                  StackTraceElement[] trace = ((InitStackThread)thread).getInitStack();

                  for (StackTraceElement traceElement : trace) {
                     writer.println("\tat " + traceElement);
                  }
               }

               writer.println("Current Stack: ");
               StackTraceElement[] trace = allStackTraces.get(thread);

               for (StackTraceElement traceElement : trace) {
                  writer.println("\tat " + traceElement);
               }
            }

            writer.println(threadInfo);
         }
      }, writer);
      section("Security Manager", () -> {
         SecurityManager securityManager = System.getSecurityManager();
         if (securityManager != null) {
            writer.println("Class: " + securityManager.getClass().getName());
         } else {
            writer.println("No Security Manager found!");
         }
      }, writer);
      section("Classes", () -> {
         ClassLoadingMXBean classLoadingMXBean = ManagementFactory.getClassLoadingMXBean();
         writer.println("Loaded Class Count: " + classLoadingMXBean.getLoadedClassCount());
         writer.println("Unloaded Class Count: " + classLoadingMXBean.getUnloadedClassCount());
         writer.println("Total Loaded Class Count: " + classLoadingMXBean.getTotalLoadedClassCount());
      }, writer);
      section("System Classloader", () -> {
         ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
         writeClassLoader(writer, systemClassLoader);
      }, writer);
      section("DumpUtil Classloader", () -> {
         ClassLoader systemClassLoader = DumpUtil.class.getClassLoader();
         writeClassLoader(writer, systemClassLoader);
      }, writer);
   }

   private static void printPacketStats(
      @Nonnull PrintWriter writer,
      @Nonnull String indent,
      @Nonnull String label,
      int count,
      long uncompressedTotal,
      long compressedTotal,
      long uncompressedMin,
      long uncompressedMax,
      long compressedMin,
      long compressedMax,
      double uncompressedAvg,
      double compressedAvg,
      int recentSeconds
   ) {
      StringBuilder sb = new StringBuilder();
      sb.append(label).append(": ").append(count).append(" packet").append(count != 1 ? "s" : "");
      if (recentSeconds > 0) {
         sb.append(String.format(" (%.1f/sec)", (double)count / recentSeconds));
      }

      sb.append("\n").append(indent).append("  Size: ").append(FormatUtil.bytesToString(uncompressedTotal));
      if (compressedTotal > 0L) {
         sb.append(" -> ").append(FormatUtil.bytesToString(compressedTotal)).append(" wire");
         double ratio = 100.0 * compressedTotal / uncompressedTotal;
         sb.append(String.format(" (%.1f%%)", ratio));
      }

      sb.append("\n").append(indent).append("  Avg: ").append(FormatUtil.bytesToString((long)uncompressedAvg));
      if (compressedAvg > 0.0) {
         sb.append(" -> ").append(FormatUtil.bytesToString((long)compressedAvg)).append(" wire");
      }

      sb.append("\n")
         .append(indent)
         .append("  Range: ")
         .append(FormatUtil.bytesToString(uncompressedMin))
         .append(" - ")
         .append(FormatUtil.bytesToString(uncompressedMax));
      if (compressedMax > 0L) {
         sb.append(" (wire: ").append(FormatUtil.bytesToString(compressedMin)).append(" - ").append(FormatUtil.bytesToString(compressedMax)).append(")");
      }

      writer.println(indent + sb);
   }

   private static void printComponentStore(@Nonnull PrintWriter writer, int width, int height, String name, long startNanos, @Nonnull Store<?> componentStore) {
      writer.println("\t- " + name + ":");
      writer.println("\t Archetype Chunk Count: " + componentStore.getArchetypeChunkCount());
      writer.println("\t Entity Count: " + componentStore.getEntityCount());
      ComponentRegistry.Data<?> data = componentStore.getRegistry().getData();
      HistoricMetric[] systemMetrics = componentStore.getSystemMetrics();

      for (int systemIndex = 0; systemIndex < data.getSystemSize(); systemIndex++) {
         ISystem<?> system = data.getSystem(systemIndex);
         HistoricMetric systemMetric = systemMetrics[systemIndex];
         writer.println("\t\t " + system.getClass().getName());
         writer.println("\t\t " + system);
         writer.println("\t\t Archetype Chunk Count: " + componentStore.getArchetypeChunkCountFor(systemIndex));
         writer.println("\t\t Entity Count: " + componentStore.getEntityCountFor(systemIndex));
         if (systemMetric != null) {
            long[] periods = systemMetric.getPeriodsNanos();

            for (int i = 0; i < periods.length; i++) {
               long period = periods[i];
               String historyLengthFormatted = FormatUtil.timeUnitToString(period, TimeUnit.NANOSECONDS, true);
               double average = systemMetric.getAverage(i);
               long min = systemMetric.calculateMin(i);
               long max = systemMetric.calculateMax(i);
               writer.println(
                  "\t\t\t("
                     + historyLengthFormatted
                     + "): Min: "
                     + FormatUtil.timeUnitToString(min, TimeUnit.NANOSECONDS)
                     + ", Avg: "
                     + FormatUtil.timeUnitToString((long)average, TimeUnit.NANOSECONDS)
                     + ", Max: "
                     + FormatUtil.timeUnitToString(max, TimeUnit.NANOSECONDS)
               );
               long[] historyTimestamps = systemMetric.getTimestamps(i);
               long[] historyValues = systemMetric.getValues(i);
               StringBuilder sb = new StringBuilder();
               StringUtil.generateGraph(
                  sb,
                  width,
                  height,
                  startNanos - period,
                  startNanos,
                  min,
                  max,
                  value -> FormatUtil.timeUnitToString(MathUtil.fastCeil(value), TimeUnit.NANOSECONDS),
                  historyTimestamps.length,
                  ii -> historyTimestamps[ii],
                  ii -> historyValues[ii]
               );
               writer.println(sb);
            }
         }
      }

      writer.println("\t\t Archetype Chunks:");

      for (ArchetypeChunkData chunkData : componentStore.collectArchetypeChunkData()) {
         writer.println("\t\t\t- Entities: " + chunkData.getEntityCount() + ", Components: " + Arrays.toString((Object[])chunkData.getComponentTypes()));
      }
   }

   private static void section(String name, @Nonnull Runnable runnable, @Nonnull PrintWriter writer) {
      writer.println("**** " + name + " ****");

      try {
         runnable.run();
      } catch (Throwable var4) {
         new RuntimeException("Failed to get data for section: " + name, var4).printStackTrace(writer);
      }

      writer.println();
      writer.println();
   }

   private static void printIndented(@Nonnull PrintWriter writer, @Nonnull String text, @Nonnull String indent) {
      for (String line : text.split("\n")) {
         writer.println(indent + line);
      }
   }

   private static void writeMemoryUsage(@Nonnull PrintWriter writer, String title, @Nonnull MemoryUsage memoryUsage) {
      writer.println(title);
      writer.println("\tInit: " + FormatUtil.bytesToString(memoryUsage.getInit()) + " (" + memoryUsage.getInit() + " Bytes)");
      writer.println("\tUsed: " + FormatUtil.bytesToString(memoryUsage.getUsed()) + " (" + memoryUsage.getUsed() + " Bytes)");
      writer.println("\tCommitted: " + FormatUtil.bytesToString(memoryUsage.getCommitted()) + " (" + memoryUsage.getCommitted() + " Bytes)");
      long max = memoryUsage.getMax();
      if (max > 0L) {
         writer.println("\tMax: " + FormatUtil.bytesToString(max) + " (" + max + " Bytes)");
         long free = max - memoryUsage.getCommitted();
         writer.println("\tFree: " + FormatUtil.bytesToString(free) + " (" + free + " Bytes)");
      }
   }

   private static void writeClassLoader(@Nonnull PrintWriter writer, @Nullable ClassLoader systemClassLoader) {
      if (systemClassLoader != null) {
         writer.println("Class: " + systemClassLoader.getClass().getName());

         while (systemClassLoader.getParent() != null) {
            systemClassLoader = systemClassLoader.getParent();
            writer.println(" - Parent: " + systemClassLoader.getClass().getName());
         }
      } else {
         writer.println("No class loader found!");
      }
   }

   public record PlayerTextData(@Nonnull UUID uuid, @Nullable String movementStates, @Nullable String movementManager, @Nullable String cameraManager) {
   }
}
