package com.hypixel.hytale.server.core.modules.voice;

import com.hypixel.hytale.logger.HytaleLogger;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class VoicePlayerState {
   private static final long MILLISECONDS_PER_SECOND = 1000L;
   private static final long RATE_LIMIT_LOG_THROTTLE_MS = 10000L;
   static final int MAX_CONSECUTIVE_ERRORS = 10;
   private final UUID playerId;
   private final HytaleLogger logger;
   private volatile boolean isSpeaking;
   private volatile boolean silenced = false;
   private volatile long lastPacketTime;
   private volatile double tokenBucket;
   private volatile long lastTokenRefillTime;
   private volatile long lastRateLimitLogTime;
   private final AtomicLong totalPacketsReceived = new AtomicLong();
   private final AtomicLong totalPacketsRateLimited = new AtomicLong();
   private final AtomicInteger speakingStateChanges = new AtomicInteger();
   private final AtomicInteger consecutiveErrors = new AtomicInteger();
   private volatile boolean routingDisabled = false;

   public VoicePlayerState(@Nonnull UUID playerId, @Nonnull HytaleLogger logger, int burstCapacity) {
      this.playerId = playerId;
      this.logger = logger;
      long now = System.currentTimeMillis();
      this.lastTokenRefillTime = now;
      this.tokenBucket = burstCapacity;
   }

   @Nonnull
   public UUID getPlayerId() {
      return this.playerId;
   }

   public boolean isSpeaking() {
      return this.isSpeaking;
   }

   public void setSpeaking(boolean speaking) {
      if (this.isSpeaking != speaking) {
         int changes = this.speakingStateChanges.incrementAndGet();
         this.logger
            .at(Level.FINE)
            .log("[PlayerState] Speaking changed: player=%s, speaking=%s -> %s, totalChanges=%d", this.playerId, this.isSpeaking, speaking, changes);
      }

      this.isSpeaking = speaking;
   }

   public boolean isSilenced() {
      return this.silenced;
   }

   public void setSilenced(boolean silenced) {
      this.silenced = silenced;
   }

   public boolean isRoutingDisabled() {
      return this.routingDisabled;
   }

   public void setRoutingDisabled(boolean disabled) {
      this.routingDisabled = disabled;
   }

   public int incrementConsecutiveErrors() {
      return this.consecutiveErrors.incrementAndGet();
   }

   public void resetConsecutiveErrors() {
      this.consecutiveErrors.set(0);
   }

   public int getConsecutiveErrors() {
      return this.consecutiveErrors.get();
   }

   public synchronized boolean checkRateLimit(int maxPacketsPerSecond, int burstCapacity) {
      long now = System.currentTimeMillis();
      long elapsed = now - this.lastTokenRefillTime;
      if (elapsed > 0L) {
         double tokensToAdd = elapsed / 1000.0 * maxPacketsPerSecond;
         this.tokenBucket = Math.min((double)burstCapacity, this.tokenBucket + tokensToAdd);
         this.lastTokenRefillTime = now;
         if (tokensToAdd >= 1.0) {
            this.logger
               .at(Level.FINEST)
               .log("[PlayerState] TokenRefill: player=%s, tokensAdded=%.2f, bucket=%.2f", this.playerId, tokensToAdd, this.tokenBucket);
         }
      }

      this.totalPacketsReceived.incrementAndGet();
      if (this.tokenBucket < 1.0) {
         this.totalPacketsRateLimited.incrementAndGet();
         return false;
      } else {
         this.tokenBucket--;
         this.lastPacketTime = now;
         return true;
      }
   }

   public synchronized boolean shouldLogRateLimit() {
      long now = System.currentTimeMillis();
      if (now - this.lastRateLimitLogTime >= 10000L) {
         this.lastRateLimitLogTime = now;
         return true;
      } else {
         return false;
      }
   }

   public long getLastPacketTime() {
      return this.lastPacketTime;
   }

   public double getTokenBucket() {
      return this.tokenBucket;
   }

   public long getTotalPacketsReceived() {
      return this.totalPacketsReceived.get();
   }

   public long getTotalPacketsRateLimited() {
      return this.totalPacketsRateLimited.get();
   }

   public int getSpeakingStateChanges() {
      return this.speakingStateChanges.get();
   }

   public String getStatsString() {
      return String.format(
         "packets=%d, rateLimited=%d, stateChanges=%d, routingErrors=%d",
         this.totalPacketsReceived.get(),
         this.totalPacketsRateLimited.get(),
         this.speakingStateChanges.get(),
         this.consecutiveErrors.get()
      );
   }
}
