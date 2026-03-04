package com.hypixel.hytale.server.core.util.thread;

import com.hypixel.hytale.common.plugin.PluginIdentifier;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.metrics.metric.HistoricMetric;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class TickingThread implements Runnable {
   public static final int NANOS_IN_ONE_MILLI = 1000000;
   public static final int NANOS_IN_ONE_SECOND = 1000000000;
   public static final int TPS = 30;
   public static long SLEEP_OFFSET = 3000000L;
   private final String threadName;
   private final boolean daemon;
   private final AtomicBoolean needsShutdown = new AtomicBoolean(true);
   private int tps;
   private int tickStepNanos;
   private HistoricMetric bufferedTickLengthMetricSet;
   @Nullable
   private Thread thread;
   @Nonnull
   private CompletableFuture<Void> startedFuture = new CompletableFuture<>();
   @Nullable
   private PluginIdentifier possibleFailureCause;
   @Nullable
   private Throwable failureException;

   public TickingThread(String threadName) {
      this(threadName, 30, false);
   }

   public TickingThread(String threadName, int tps, boolean daemon) {
      this.threadName = threadName;
      this.daemon = daemon;
      this.tps = tps;
      this.tickStepNanos = 1000000000 / tps;
      this.bufferedTickLengthMetricSet = HistoricMetric.builder(this.tickStepNanos, TimeUnit.NANOSECONDS)
         .addPeriod(10L, TimeUnit.SECONDS)
         .addPeriod(1L, TimeUnit.MINUTES)
         .addPeriod(5L, TimeUnit.MINUTES)
         .build();
   }

   @Override
   public void run() {
      try {
         this.onStart();
         this.startedFuture.complete(null);
         long beforeTick = System.nanoTime() - this.tickStepNanos;

         while (this.thread != null && !this.thread.isInterrupted()) {
            long delta;
            if (!this.isIdle()) {
               while ((delta = System.nanoTime() - beforeTick) < this.tickStepNanos) {
                  Thread.onSpinWait();
               }
            } else {
               delta = System.nanoTime() - beforeTick;
            }

            beforeTick = System.nanoTime();
            this.tick((float)delta / 1.0E9F);
            long tickLength = System.nanoTime() - beforeTick;
            this.bufferedTickLengthMetricSet.add(System.nanoTime(), tickLength);
            long sleepLength = this.tickStepNanos - tickLength;
            if (!this.isIdle()) {
               sleepLength -= SLEEP_OFFSET;
            }

            if (sleepLength > 0L) {
               Thread.sleep(sleepLength / 1000000L);
            }
         }
      } catch (InterruptedException var9) {
         Thread.currentThread().interrupt();
      } catch (Throwable var10) {
         this.failureException = var10;
         this.possibleFailureCause = PluginIdentifier.identifyThirdPartyPlugin(var10);
         if (this.possibleFailureCause == null) {
            ((HytaleLogger.Api)HytaleLogger.getLogger().at(Level.SEVERE).withCause(var10)).log("Exception in thread %s:", this.thread);
         } else {
            ((HytaleLogger.Api)HytaleLogger.getLogger().at(Level.SEVERE).withCause(var10))
               .log("Exception in thread %s potentially caused by %s:", this.thread, this.possibleFailureCause);
         }
      }

      if (this.needsShutdown.getAndSet(false)) {
         this.onShutdown();
      }
   }

   protected boolean isIdle() {
      return false;
   }

   protected abstract void tick(float var1);

   protected void onStart() {
   }

   protected abstract void onShutdown();

   @Nonnull
   public CompletableFuture<Void> start() {
      if (this.thread == null) {
         this.thread = new Thread(this, this.threadName);
         this.thread.setDaemon(this.daemon);
      } else if (this.thread.isAlive()) {
         throw new IllegalStateException("Thread '" + this.thread.getName() + "' is already started!");
      }

      this.thread.start();
      return this.startedFuture;
   }

   public boolean interrupt() {
      if (this.thread != null && this.thread.isAlive()) {
         this.thread.interrupt();
         return true;
      } else {
         return false;
      }
   }

   public void stop() {
      Thread thread = this.thread;
      if (thread != null) {
         try {
            int i = 0;

            while (thread.isAlive()) {
               thread.interrupt();
               thread.join(this.tickStepNanos / 1000000);
               i += this.tickStepNanos / 1000000;
               if (i > 30000) {
                  StringBuilder sb = new StringBuilder();

                  for (StackTraceElement traceElement : thread.getStackTrace()) {
                     sb.append("\tat ").append(traceElement).append('\n');
                  }

                  HytaleLogger.getLogger().at(Level.SEVERE).log("Forcing TickingThread %s to stop:\n%s", thread, sb.toString());
                  thread.stop();
                  Thread var9 = null;
                  if (this.needsShutdown.getAndSet(false)) {
                     this.onShutdown();
                  }

                  return;
               }
            }

            Thread var10 = null;
         } catch (InterruptedException var8) {
            Thread.currentThread().interrupt();
         }
      }
   }

   public void setTps(int tps) {
      this.debugAssertInTickingThread();
      if (tps > 0 && tps <= 2048) {
         this.tps = tps;
         this.tickStepNanos = 1000000000 / tps;
         this.bufferedTickLengthMetricSet = HistoricMetric.builder(this.tickStepNanos, TimeUnit.NANOSECONDS)
            .addPeriod(10L, TimeUnit.SECONDS)
            .addPeriod(1L, TimeUnit.MINUTES)
            .addPeriod(5L, TimeUnit.MINUTES)
            .build();
      } else {
         throw new IllegalArgumentException("UpdatesPerSecond is out of bounds (<=0 or >2048): " + tps);
      }
   }

   public int getTps() {
      return this.tps;
   }

   public int getTickStepNanos() {
      return this.tickStepNanos;
   }

   public HistoricMetric getBufferedTickLengthMetricSet() {
      return this.bufferedTickLengthMetricSet;
   }

   public void clearMetrics() {
      this.bufferedTickLengthMetricSet.clear();
   }

   public void debugAssertInTickingThread() {
      if (!Thread.currentThread().equals(this.thread) && this.thread != null) {
         throw new AssertionError("Assert not in ticking thread!");
      }
   }

   public boolean isInThread() {
      return Thread.currentThread().equals(this.thread);
   }

   public boolean isStarted() {
      return this.thread != null && this.thread.isAlive() && this.needsShutdown.get();
   }

   @Nullable
   public PluginIdentifier getPossibleFailureCause() {
      return this.possibleFailureCause;
   }

   @Nullable
   public Throwable getFailureException() {
      return this.failureException;
   }

   @Deprecated
   protected void setThread(Thread thread) {
      this.thread = thread;
   }

   @Nullable
   protected Thread getThread() {
      return this.thread;
   }
}
