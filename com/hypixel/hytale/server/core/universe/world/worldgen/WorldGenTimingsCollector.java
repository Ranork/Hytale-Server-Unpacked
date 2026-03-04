package com.hypixel.hytale.server.core.universe.world.worldgen;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.metrics.MetricsRegistry;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;
import javax.annotation.Nonnull;

public class WorldGenTimingsCollector {
   public static final MetricsRegistry<WorldGenTimingsCollector> METRICS_REGISTRY = new MetricsRegistry<WorldGenTimingsCollector>()
      .register("Chunks", worldGenTimingsCollector -> worldGenTimingsCollector.chunkCounter.get(), Codec.LONG)
      .register("ChunkTime", worldGenTimingsCollector -> worldGenTimingsCollector.getChunkTime(), Codec.DOUBLE)
      .register("ZoneBiomeResultTime", worldGenTimingsCollector -> worldGenTimingsCollector.zoneBiomeResult(), Codec.DOUBLE)
      .register("PrepareTime", worldGenTimingsCollector -> worldGenTimingsCollector.prepare(), Codec.DOUBLE)
      .register("BlocksTime", worldGenTimingsCollector -> worldGenTimingsCollector.blocksGeneration(), Codec.DOUBLE)
      .register("CaveTime", worldGenTimingsCollector -> worldGenTimingsCollector.caveGeneration(), Codec.DOUBLE)
      .register("PrefabTime", worldGenTimingsCollector -> worldGenTimingsCollector.prefabGeneration(), Codec.DOUBLE)
      .register("QueueLength", WorldGenTimingsCollector::getQueueLength, Codec.INTEGER)
      .register("GeneratingCount", WorldGenTimingsCollector::getGeneratingCount, Codec.INTEGER);
   private static final double NANOS_TO_SECONDS = 1.0E-9;
   private static final int WARMUP = 100;
   private static final double WARMUP_VALUE = Double.NEGATIVE_INFINITY;
   private static final int CHUNKS = 0;
   private static final int ZONE_BIOME_RESULT = 1;
   private static final int PREPARE = 2;
   private static final int BLOCKS = 3;
   private static final int CAVES = 4;
   private static final int PREFABS = 5;
   private final AtomicLong chunkCounter = new AtomicLong();
   private final AtomicLongArray times = new AtomicLongArray(6);
   private final AtomicLongArray counts = new AtomicLongArray(6);
   private final ThreadPoolExecutor threadPoolExecutor;

   public WorldGenTimingsCollector(ThreadPoolExecutor threadPoolExecutor) {
      this.threadPoolExecutor = threadPoolExecutor;
   }

   public double reportChunk(long nanos) {
      return this.chunkCounter.incrementAndGet() > 100L ? this.addAndGet(0, nanos) : Double.NEGATIVE_INFINITY;
   }

   public double reportZoneBiomeResult(long nanos) {
      return this.chunkCounter.get() > 100L ? this.addAndGet(1, nanos) : Double.NEGATIVE_INFINITY;
   }

   public double reportPrepare(long nanos) {
      return this.chunkCounter.get() > 100L ? this.addAndGet(2, nanos) : Double.NEGATIVE_INFINITY;
   }

   public double reportBlocksGeneration(long nanos) {
      return this.chunkCounter.get() > 100L ? this.addAndGet(3, nanos) : Double.NEGATIVE_INFINITY;
   }

   public double reportCaveGeneration(long nanos) {
      return this.chunkCounter.get() > 100L ? this.addAndGet(4, nanos) : Double.NEGATIVE_INFINITY;
   }

   public double reportPrefabGeneration(long nanos) {
      return this.chunkCounter.get() > 100L ? this.addAndGet(5, nanos) : Double.NEGATIVE_INFINITY;
   }

   public double getWarmupValue() {
      return Double.NEGATIVE_INFINITY;
   }

   public double zoneBiomeResult() {
      return this.get(1);
   }

   public double prepare() {
      return this.get(2);
   }

   public double blocksGeneration() {
      return this.get(3);
   }

   public double caveGeneration() {
      return this.get(4);
   }

   public double prefabGeneration() {
      return this.get(5);
   }

   public long getChunkCounter() {
      return this.chunkCounter.get();
   }

   public double getChunkTime() {
      return this.get(0);
   }

   public int getQueueLength() {
      return this.threadPoolExecutor.getQueue().size();
   }

   public int getGeneratingCount() {
      return this.threadPoolExecutor.getActiveCount();
   }

   @Nonnull
   @Override
   public String toString() {
      return String.format(
         "cnt: %s, zbr: %s, pp: %s, b: %s, c: %s, pf: %s",
         this.getChunkCounter(),
         this.zoneBiomeResult(),
         this.prepare(),
         this.blocksGeneration(),
         this.caveGeneration(),
         this.prefabGeneration()
      );
   }

   protected double get(int index) {
      long sum = this.times.get(index);
      long count = this.counts.get(index);
      return getAvgSeconds(sum, count);
   }

   protected double addAndGet(int index, long nanos) {
      long sum = this.times.addAndGet(index, nanos);
      long count = this.counts.incrementAndGet(index);
      return getAvgSeconds(sum, count);
   }

   protected static double getAvgSeconds(long nanos, long count) {
      return count == 0L ? 0.0 : nanos * 1.0E-9 / count;
   }
}
