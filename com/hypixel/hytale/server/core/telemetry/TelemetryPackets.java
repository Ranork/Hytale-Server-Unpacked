package com.hypixel.hytale.server.core.telemetry;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

final class TelemetryPackets {
   private TelemetryPackets() {
   }

   record ModInfo(@Nonnull String identifier, @Nonnull String version, @Nonnull String type, boolean builtin) {
   }

   record ServerCapacityInfo(int onlinePlayers, int maxPlayers, int activeWorlds, int totalEntities, int loadedChunks) {
   }

   record ServerConfigInfo(int maxPlayers, int worldCount, boolean dedicated) {
   }

   record ServerHardwareInfo(@Nonnull String machineIdHash, int cpuCores, int systemMemoryMb) {
   }

   record ServerHeartbeatPacket(
      @Nonnull String type,
      @Nonnull String sessionId,
      @Nonnull String timestamp,
      int sequence,
      int uptimeSeconds,
      @Nonnull TelemetryPackets.ServerPerformanceInfo performance,
      @Nonnull TelemetryPackets.ServerCapacityInfo capacity,
      @Nonnull TelemetryPackets.ServerMemoryInfo memory,
      @Nonnull TelemetryPackets.ServerNetworkInfo network
   ) {
      ServerHeartbeatPacket(
         @Nonnull String sessionId,
         @Nonnull String timestamp,
         int sequence,
         int uptimeSeconds,
         @Nonnull TelemetryPackets.ServerPerformanceInfo performance,
         @Nonnull TelemetryPackets.ServerCapacityInfo capacity,
         @Nonnull TelemetryPackets.ServerMemoryInfo memory,
         @Nonnull TelemetryPackets.ServerNetworkInfo network
      ) {
         this("heartbeat", sessionId, timestamp, sequence, uptimeSeconds, performance, capacity, memory, network);
      }
   }

   record ServerInfo(@Nonnull String version, @Nonnull String revisionId, @Nonnull String configuration) {
   }

   record ServerMemoryInfo(float heapUsedMb, float heapMaxMb, int gcCollections, long gcTimeMs) {
   }

   record ServerNetworkInfo(float sentBytesPerSecond, float receivedBytesPerSecond, float totalSentKb, float totalReceivedKb) {
   }

   record ServerNetworkSummary(float totalSentMb, float totalReceivedMb, int totalConnections) {
   }

   record ServerPerformanceInfo(float tickPerformancePercent, float tickDurationAvgMs, float tickDurationP99Ms) {
   }

   record ServerPerformanceSummary(float avgTickPerformancePercent, float minTickPerformancePercent, int performanceDropsBelow50Percent, long totalGcTimeMs) {
   }

   record ServerPlatformInfo(
      @Nonnull String os, @Nonnull String osVersion, @Nonnull String architecture, @Nonnull String javaVersion, @Nonnull String javaVendor
   ) {
   }

   record ServerSessionSummary(int totalUptimeSeconds, @Nonnull String shutdownReason, int peakPlayers, int totalUniquePlayers) {
   }

   record ServerStartPacket(
      @Nonnull String type,
      @Nonnull String sessionId,
      @Nonnull String timestamp,
      int sequence,
      long bootDurationMs,
      @Nonnull TelemetryPackets.ServerInfo server,
      @Nonnull TelemetryPackets.ServerPlatformInfo platform,
      @Nullable TelemetryPackets.ServerHardwareInfo hardware,
      @Nonnull TelemetryPackets.ServerConfigInfo config,
      @Nonnull List<TelemetryPackets.ModInfo> mods
   ) {
      ServerStartPacket(
         @Nonnull String sessionId,
         @Nonnull String timestamp,
         int sequence,
         long bootDurationMs,
         @Nonnull TelemetryPackets.ServerInfo server,
         @Nonnull TelemetryPackets.ServerPlatformInfo platform,
         @Nullable TelemetryPackets.ServerHardwareInfo hardware,
         @Nonnull TelemetryPackets.ServerConfigInfo config,
         @Nonnull List<TelemetryPackets.ModInfo> mods
      ) {
         this("server_start", sessionId, timestamp, sequence, bootDurationMs, server, platform, hardware, config, mods);
      }
   }

   record ServerStopPacket(
      @Nonnull String type,
      @Nonnull String sessionId,
      @Nonnull String timestamp,
      int sequence,
      @Nonnull TelemetryPackets.ServerSessionSummary sessionSummary,
      @Nonnull TelemetryPackets.ServerPerformanceSummary performanceSummary,
      @Nonnull TelemetryPackets.ServerNetworkSummary networkSummary
   ) {
      ServerStopPacket(
         @Nonnull String sessionId,
         @Nonnull String timestamp,
         int sequence,
         @Nonnull TelemetryPackets.ServerSessionSummary sessionSummary,
         @Nonnull TelemetryPackets.ServerPerformanceSummary performanceSummary,
         @Nonnull TelemetryPackets.ServerNetworkSummary networkSummary
      ) {
         this("server_stop", sessionId, timestamp, sequence, sessionSummary, performanceSummary, networkSummary);
      }
   }
}
