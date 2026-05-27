package com.hypixel.hytale.server.core.telemetry;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.io.PacketStatsRecorder;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.auth.AuthConfig;
import com.hypixel.hytale.server.core.auth.ServerAuthManager;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import java.io.IOException;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class TelemetryService {
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   private static final int DEFAULT_HEARTBEAT_INTERVAL_SECONDS = 300;
   private static final String HEARTBEAT_INTERVAL_HEADER = "X-Telemetry-Heartbeat-Interval";
   private static final int MAX_RETRY_ATTEMPTS = 3;
   private static final int RETRY_DELAY_MS = 2500;
   private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10L);
   @Nullable
   private static TelemetryService instance;
   @Nonnull
   private final String sessionId;
   @Nonnull
   private final Instant bootTime;
   @Nonnull
   private final HttpClient httpClient;
   @Nonnull
   private final AtomicInteger sequenceNumber = new AtomicInteger(0);
   @Nonnull
   private final AtomicBoolean sessionStarted = new AtomicBoolean(false);
   @Nonnull
   private final AtomicBoolean pendingServerStart = new AtomicBoolean(false);
   @Nonnull
   private final AtomicBoolean enabled = new AtomicBoolean(true);
   @Nullable
   private ScheduledFuture<?> heartbeatFuture;
   @Nonnull
   private final AtomicLong totalBytesSent = new AtomicLong();
   @Nonnull
   private final AtomicLong totalBytesReceived = new AtomicLong();
   @Nonnull
   private final AtomicLong lastBytesSent = new AtomicLong();
   @Nonnull
   private final AtomicLong lastBytesReceived = new AtomicLong();
   @Nonnull
   private final AtomicLong lastNetworkUpdateTime = new AtomicLong(System.nanoTime());
   private volatile float sentBytesPerSecond;
   private volatile float receivedBytesPerSecond;
   private int peakPlayers;
   @Nonnull
   private final Set<UUID> uniquePlayers = ConcurrentHashMap.newKeySet();
   private int totalConnections;
   private float performanceSum;
   private int performanceCount;
   private float minPerformance = Float.MAX_VALUE;
   private int performanceDropsBelow50;
   @Nonnull
   private volatile String shutdownReason = "normal";
   @Nonnull
   private final TelemetryStorage storage;

   TelemetryService() {
      this.sessionId = UUID.randomUUID().toString();
      this.bootTime = Instant.now();
      this.httpClient = HttpClient.newBuilder().connectTimeout(REQUEST_TIMEOUT).build();
      this.storage = new TelemetryStorage(this.sessionId);
      instance = this;
      LOGGER.at(Level.INFO).log("Telemetry service initialized with session ID: %s", this.sessionId);
   }

   @Nullable
   public static TelemetryService get() {
      return instance;
   }

   boolean isEnabled() {
      return this.enabled.get();
   }

   void setEnabled(boolean enabled) {
      this.enabled.set(enabled);
      if (!enabled) {
         this.stopHeartbeat();
      }

      LOGGER.at(Level.INFO).log("Telemetry %s", enabled ? "enabled" : "disabled");
   }

   public void onAuthenticated() {
      if (this.pendingServerStart.get()) {
         LOGGER.at(Level.FINE).log("Server authenticated, sending pending server_start telemetry");
         this.sendServerStart();
      }
   }

   void sendServerStart() {
      if (this.enabled.get() && !this.sessionStarted.get()) {
         ServerAuthManager authManager = ServerAuthManager.getInstance();
         if (!authManager.hasSessionToken()) {
            this.pendingServerStart.set(true);
            LOGGER.at(Level.FINE).log("Server not yet authenticated, telemetry will start after authentication");
         } else if (this.sessionStarted.compareAndSet(false, true)) {
            this.pendingServerStart.set(false);
            TelemetryPackets.ServerStartPacket packet = TelemetryDataCollector.collectServerStart(this.sessionId, getUtcTimestamp(), this.getNextSequence());
            this.sendServerStartPacket(packet);
         }
      }
   }

   private void sendServerStartPacket(@Nonnull Object packet) {
      HttpRequest request = this.buildRequest(packet, "server_start");
      if (request == null) {
         this.startHeartbeat(300);
      } else {
         this.sendWithRetryAsync(request, "server_start", 0, this::onServerStartComplete);
      }
   }

   private void onServerStartComplete(@Nullable HttpResponse<String> response) {
      int intervalSeconds = 300;
      if (response != null) {
         Optional<String> headerValue = response.headers().firstValue("X-Telemetry-Heartbeat-Interval");
         if (headerValue.isPresent()) {
            try {
               int parsed = Integer.parseInt(headerValue.get());
               if (parsed > 0) {
                  intervalSeconds = parsed;
                  LOGGER.at(Level.FINE).log("Heartbeat interval set to %d seconds from server response", parsed);
               }
            } catch (NumberFormatException var5) {
               LOGGER.at(Level.WARNING).log("Invalid heartbeat interval header value: %s", headerValue.get());
            }
         }
      }

      this.startHeartbeat(intervalSeconds);
   }

   private void sendHeartbeat() {
      if (this.enabled.get() && this.sessionStarted.get()) {
         this.updateNetworkStats();
         int uptimeSeconds = (int)Duration.between(this.bootTime, Instant.now()).toSeconds();
         TelemetryService.NetworkStats networkStats = new TelemetryService.NetworkStats(
            this.sentBytesPerSecond, this.receivedBytesPerSecond, (float)this.totalBytesSent.get() / 1024.0F, (float)this.totalBytesReceived.get() / 1024.0F
         );
         TelemetryPackets.ServerHeartbeatPacket packet = TelemetryDataCollector.collectHeartbeat(
            this.sessionId, getUtcTimestamp(), this.getNextSequence(), uptimeSeconds, networkStats
         );
         this.trackPerformance(packet.performance().tickPerformancePercent());
         Universe universe = Universe.get();
         if (universe != null) {
            int currentPlayers = universe.getPlayerCount();
            if (currentPlayers > this.peakPlayers) {
               this.peakPlayers = currentPlayers;
            }
         }

         this.sendPacketAsync(packet, "heartbeat");
      }
   }

   void sendServerStop(@Nonnull String reason) {
      if (this.enabled.get() && this.sessionStarted.get()) {
         this.shutdownReason = reason;
         this.stopHeartbeat();
         int uptimeSeconds = (int)Duration.between(this.bootTime, Instant.now()).toSeconds();
         float avgPerformance = this.performanceCount > 0 ? this.performanceSum / this.performanceCount : 0.0F;
         long totalGcTimeMs = 0L;

         for (GarbageCollectorMXBean gcBean : ManagementFactory.getGarbageCollectorMXBeans()) {
            long time = gcBean.getCollectionTime();
            if (time >= 0L) {
               totalGcTimeMs += time;
            }
         }

         TelemetryPackets.ServerStopPacket packet = new TelemetryPackets.ServerStopPacket(
            this.sessionId,
            getUtcTimestamp(),
            this.getNextSequence(),
            new TelemetryPackets.ServerSessionSummary(uptimeSeconds, this.shutdownReason, this.peakPlayers, this.uniquePlayers.size()),
            new TelemetryPackets.ServerPerformanceSummary(
               avgPerformance, this.minPerformance == Float.MAX_VALUE ? 0.0F : this.minPerformance, this.performanceDropsBelow50, totalGcTimeMs
            ),
            new TelemetryPackets.ServerNetworkSummary(
               (float)this.totalBytesSent.get() / 1048576.0F, (float)this.totalBytesReceived.get() / 1048576.0F, this.totalConnections
            )
         );
         this.sendPacketSync(packet, "server_stop");
         this.sessionStarted.set(false);
         LOGGER.at(Level.FINE).log("Telemetry session ended. Uptime: %ds", uptimeSeconds);
      }
   }

   void recordPlayerConnect(@Nonnull UUID playerUuid) {
      this.uniquePlayers.add(playerUuid);
      this.totalConnections++;
   }

   private void updateNetworkStats() {
      Universe universe = Universe.get();
      if (universe != null) {
         long totalSent = 0L;
         long totalReceived = 0L;

         for (PlayerRef playerRef : universe.getPlayers()) {
            PacketHandler handler = playerRef.getPacketHandler();
            PacketStatsRecorder recorder = handler.getPacketStatsRecorder();
            if (recorder != null) {
               for (int i = 0; i < 512; i++) {
                  PacketStatsRecorder.PacketStatsEntry entry = recorder.getEntry(i);
                  if (entry.hasData()) {
                     totalSent += entry.getSentCompressedTotal();
                     totalReceived += entry.getReceivedCompressedTotal();
                  }
               }
            }
         }

         long now = System.nanoTime();
         long elapsed = now - this.lastNetworkUpdateTime.get();
         if (elapsed > 0L) {
            float elapsedSeconds = (float)elapsed / 1.0E9F;
            long sentDelta = totalSent - this.lastBytesSent.get();
            long receivedDelta = totalReceived - this.lastBytesReceived.get();
            this.sentBytesPerSecond = (float)sentDelta / elapsedSeconds;
            this.receivedBytesPerSecond = (float)receivedDelta / elapsedSeconds;
            this.lastNetworkUpdateTime.set(now);
         }

         this.lastBytesSent.set(totalSent);
         this.lastBytesReceived.set(totalReceived);
         this.totalBytesSent.set(totalSent);
         this.totalBytesReceived.set(totalReceived);
      }
   }

   private void trackPerformance(float performancePercent) {
      this.performanceSum += performancePercent;
      this.performanceCount++;
      if (performancePercent < this.minPerformance) {
         this.minPerformance = performancePercent;
      }

      if (performancePercent < 50.0F) {
         this.performanceDropsBelow50++;
      }
   }

   private void startHeartbeat(int intervalSeconds) {
      this.stopHeartbeat();
      LOGGER.at(Level.FINE).log("Starting telemetry heartbeat with interval: %d seconds", intervalSeconds);
      this.heartbeatFuture = HytaleServer.SCHEDULED_EXECUTOR.scheduleWithFixedDelay(this::sendHeartbeat, intervalSeconds, intervalSeconds, TimeUnit.SECONDS);
   }

   private void stopHeartbeat() {
      if (this.heartbeatFuture != null) {
         this.heartbeatFuture.cancel(false);
         this.heartbeatFuture = null;
      }
   }

   private int getNextSequence() {
      return this.sequenceNumber.getAndIncrement();
   }

   @Nonnull
   private static String getUtcTimestamp() {
      return Instant.now().toString();
   }

   private void sendPacketAsync(@Nonnull Object packet, @Nonnull String packetType) {
      HttpRequest request = this.buildRequest(packet, packetType);
      if (request != null) {
         this.sendWithRetryAsync(request, packetType, 0, null);
      }
   }

   private void sendWithRetryAsync(@Nonnull HttpRequest request, @Nonnull String packetType, int attempt, @Nullable Consumer<HttpResponse<String>> onComplete) {
      this.httpClient.sendAsync(request, BodyHandlers.ofString()).whenComplete((response, error) -> {
         if (error != null) {
            this.handleAsyncError(error, request, packetType, attempt, onComplete);
         } else if (response.statusCode() >= 200 && response.statusCode() < 300) {
            if (onComplete != null) {
               onComplete.accept((HttpResponse<String>)response);
            }
         } else {
            LOGGER.at(Level.WARNING).log("Telemetry request failed: %s (status %d: %s)", packetType, response.statusCode(), response.body());
            if (response.statusCode() >= 500 && attempt < 2) {
               this.scheduleRetry(request, packetType, attempt, onComplete);
            } else if (onComplete != null) {
               onComplete.accept(null);
            }
         }
      });
   }

   private void handleAsyncError(
      @Nonnull Throwable error, @Nonnull HttpRequest request, @Nonnull String packetType, int attempt, @Nullable Consumer<HttpResponse<String>> onComplete
   ) {
      Throwable cause = error;
      if (error instanceof CompletionException && error.getCause() != null) {
         cause = error.getCause();
      }

      boolean shouldRetry;
      if (cause instanceof HttpTimeoutException) {
         LOGGER.at(Level.WARNING).log("Telemetry %s timed out (attempt %d/%d)", packetType, attempt + 1, 3);
         shouldRetry = true;
      } else if (cause instanceof IOException) {
         LOGGER.at(Level.WARNING)
            .log("Telemetry %s failed (attempt %d/%d): %s: %s", packetType, attempt + 1, 3, cause.getClass().getSimpleName(), cause.getMessage());
         shouldRetry = true;
      } else {
         LOGGER.at(Level.WARNING)
            .log("Telemetry %s failed with unexpected error: %s", packetType, cause.getClass().getSimpleName() + ": " + cause.getMessage());
         shouldRetry = false;
      }

      if (shouldRetry && attempt < 2) {
         this.scheduleRetry(request, packetType, attempt, onComplete);
      } else if (onComplete != null) {
         onComplete.accept(null);
      }
   }

   private void scheduleRetry(@Nonnull HttpRequest request, @Nonnull String packetType, int attempt, @Nullable Consumer<HttpResponse<String>> onComplete) {
      long delayMs = 2500L * (1L << attempt);
      HytaleServer.SCHEDULED_EXECUTOR.schedule(() -> this.sendWithRetryAsync(request, packetType, attempt + 1, onComplete), delayMs, TimeUnit.MILLISECONDS);
   }

   private void sendPacketSync(@Nonnull Object packet, @Nonnull String packetType) {
      HttpRequest request = this.buildRequest(packet, packetType);
      if (request != null) {
         for (int attempt = 0; attempt < 3; attempt++) {
            try {
               HttpResponse<String> response = this.httpClient.send(request, BodyHandlers.ofString());
               if (response.statusCode() >= 200 && response.statusCode() < 300) {
                  return;
               }

               LOGGER.at(Level.WARNING).log("Telemetry request failed: %s (status %d: %s)", packetType, response.statusCode(), response.body());
               if (response.statusCode() < 500) {
                  return;
               }
            } catch (HttpTimeoutException var7) {
               LOGGER.at(Level.WARNING).log("Telemetry request timed out (attempt %d/%d)", attempt + 1, 3);
            } catch (IOException var8) {
               LOGGER.at(Level.WARNING).log("Telemetry request failed (attempt %d/%d): %s", attempt + 1, 3, var8.getMessage());
            } catch (InterruptedException var9) {
               Thread.currentThread().interrupt();
               return;
            } catch (Exception var10) {
               ((HytaleLogger.Api)LOGGER.at(Level.WARNING).withCause(var10)).log("Unexpected error sending telemetry");
               return;
            }

            if (attempt < 2) {
               try {
                  Thread.sleep(2500L * (1L << attempt));
               } catch (InterruptedException var6) {
                  Thread.currentThread().interrupt();
                  return;
               }
            }
         }

         LOGGER.at(Level.WARNING).log("Telemetry packet %s failed after %d attempts", packetType, 3);
      }
   }

   void shutdown() {
      this.stopHeartbeat();
      this.storage.closeAndCompress();
      instance = null;
   }

   @Nullable
   private HttpRequest buildRequest(@Nonnull Object packet, @Nonnull String packetType) {
      String json;
      try {
         json = TelemetryJsonSerializer.serialize(packet);
      } catch (Exception var5) {
         ((HytaleLogger.Api)LOGGER.at(Level.WARNING).withCause(var5)).log("Failed to serialize telemetry packet: %s", packetType);
         return null;
      }

      this.storage.writeEntry(json);
      ServerAuthManager authManager = ServerAuthManager.getInstance();
      return !authManager.hasSessionToken()
         ? null
         : HttpRequest.newBuilder()
            .uri(URI.create("https://telemetry.hytale.com/telemetry/server"))
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .header("User-Agent", AuthConfig.USER_AGENT)
            .header("Authorization", "Bearer " + authManager.getSessionToken())
            .timeout(REQUEST_TIMEOUT)
            .POST(BodyPublishers.ofString(json))
            .build();
   }

   record NetworkStats(float sentBytesPerSecond, float receivedBytesPerSecond, float totalSentKb, float totalReceivedKb) {
   }
}
