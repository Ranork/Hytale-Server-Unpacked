package com.hypixel.hytale.server.core.io;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.metrics.MetricsRegistry;
import com.hypixel.hytale.metrics.metric.AverageCollector;
import com.hypixel.hytale.protocol.PacketRegistry;
import com.hypixel.hytale.protocol.io.PacketStatsRecorder;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PacketStatsRecorderImpl implements PacketStatsRecorder {
   public static final MetricsRegistry<PacketStatsRecorderImpl> METRICS_REGISTRY = new MetricsRegistry<PacketStatsRecorderImpl>()
      .register("Packets", recorder -> {
         ArrayList<PacketStatsRecorderImpl.PacketStatsEntry> entries = new ArrayList<>();

         for (int i = 0; i < 512; i++) {
            PacketStatsRecorderImpl.PacketStatsEntry entry = recorder.entries[i];
            if (entry.hasData()) {
               entries.add(entry);
            }
         }

         return entries.toArray(PacketStatsRecorderImpl.PacketStatsEntry[]::new);
      }, new ArrayCodec<>(PacketStatsRecorderImpl.PacketStatsEntry.METRICS_REGISTRY, PacketStatsRecorderImpl.PacketStatsEntry[]::new));
   private final PacketStatsRecorderImpl.PacketStatsEntry[] entries = new PacketStatsRecorderImpl.PacketStatsEntry[512];

   public PacketStatsRecorderImpl() {
      for (int i = 0; i < this.entries.length; i++) {
         this.entries[i] = new PacketStatsRecorderImpl.PacketStatsEntry(i);
      }
   }

   @Override
   public void recordSend(int packetId, int uncompressedSize, int compressedSize) {
      if (packetId >= 0 && packetId < this.entries.length) {
         this.entries[packetId].recordSend(uncompressedSize, compressedSize);
      }
   }

   @Override
   public void recordReceive(int packetId, int uncompressedSize, int compressedSize) {
      if (packetId >= 0 && packetId < this.entries.length) {
         this.entries[packetId].recordReceive(uncompressedSize, compressedSize);
      }
   }

   @Nonnull
   public PacketStatsRecorderImpl.PacketStatsEntry getEntry(int packetId) {
      return this.entries[packetId];
   }

   public static class PacketStatsEntry implements PacketStatsRecorder.PacketStatsEntry {
      public static final MetricsRegistry<PacketStatsRecorderImpl.PacketStatsEntry> METRICS_REGISTRY = new MetricsRegistry<PacketStatsRecorderImpl.PacketStatsEntry>()
         .register("PacketId", PacketStatsRecorderImpl.PacketStatsEntry::getPacketId, Codec.INTEGER)
         .register("Name", PacketStatsRecorderImpl.PacketStatsEntry::getName, Codec.STRING)
         .register("SentCount", PacketStatsRecorderImpl.PacketStatsEntry::getSentCount, Codec.INTEGER)
         .register("SentUncompressedTotal", PacketStatsRecorderImpl.PacketStatsEntry::getSentUncompressedTotal, Codec.LONG)
         .register("SentCompressedTotal", PacketStatsRecorderImpl.PacketStatsEntry::getSentCompressedTotal, Codec.LONG)
         .register("SentUncompressedMin", PacketStatsRecorderImpl.PacketStatsEntry::getSentUncompressedMin, Codec.LONG)
         .register("SentUncompressedMax", PacketStatsRecorderImpl.PacketStatsEntry::getSentUncompressedMax, Codec.LONG)
         .register("SentCompressedMin", PacketStatsRecorderImpl.PacketStatsEntry::getSentCompressedMin, Codec.LONG)
         .register("SentCompressedMax", PacketStatsRecorderImpl.PacketStatsEntry::getSentCompressedMax, Codec.LONG)
         .register("ReceivedCount", PacketStatsRecorderImpl.PacketStatsEntry::getReceivedCount, Codec.INTEGER)
         .register("ReceivedUncompressedTotal", PacketStatsRecorderImpl.PacketStatsEntry::getReceivedUncompressedTotal, Codec.LONG)
         .register("ReceivedCompressedTotal", PacketStatsRecorderImpl.PacketStatsEntry::getReceivedCompressedTotal, Codec.LONG)
         .register("ReceivedUncompressedMin", PacketStatsRecorderImpl.PacketStatsEntry::getReceivedUncompressedMin, Codec.LONG)
         .register("ReceivedUncompressedMax", PacketStatsRecorderImpl.PacketStatsEntry::getReceivedUncompressedMax, Codec.LONG)
         .register("ReceivedCompressedMin", PacketStatsRecorderImpl.PacketStatsEntry::getReceivedCompressedMin, Codec.LONG)
         .register("ReceivedCompressedMax", PacketStatsRecorderImpl.PacketStatsEntry::getReceivedCompressedMax, Codec.LONG);
      private final int packetId;
      private final AtomicInteger sentCount = new AtomicInteger();
      private final AtomicLong sentUncompressedTotal = new AtomicLong();
      private final AtomicLong sentCompressedTotal = new AtomicLong();
      private final AtomicLong sentUncompressedMin = new AtomicLong(Long.MAX_VALUE);
      private final AtomicLong sentUncompressedMax = new AtomicLong();
      private final AtomicLong sentCompressedMin = new AtomicLong(Long.MAX_VALUE);
      private final AtomicLong sentCompressedMax = new AtomicLong();
      private final AverageCollector sentUncompressedAvg = new AverageCollector();
      private final AverageCollector sentCompressedAvg = new AverageCollector();
      private final Queue<PacketStatsRecorderImpl.PacketStatsEntry.SizeRecord> sentRecently = new ConcurrentLinkedQueue<>();
      private final AtomicInteger receivedCount = new AtomicInteger();
      private final AtomicLong receivedUncompressedTotal = new AtomicLong();
      private final AtomicLong receivedCompressedTotal = new AtomicLong();
      private final AtomicLong receivedUncompressedMin = new AtomicLong(Long.MAX_VALUE);
      private final AtomicLong receivedUncompressedMax = new AtomicLong();
      private final AtomicLong receivedCompressedMin = new AtomicLong(Long.MAX_VALUE);
      private final AtomicLong receivedCompressedMax = new AtomicLong();
      private final AverageCollector receivedUncompressedAvg = new AverageCollector();
      private final AverageCollector receivedCompressedAvg = new AverageCollector();
      private final Queue<PacketStatsRecorderImpl.PacketStatsEntry.SizeRecord> receivedRecently = new ConcurrentLinkedQueue<>();

      public PacketStatsEntry(int packetId) {
         this.packetId = packetId;
      }

      void recordSend(int uncompressedSize, int compressedSize) {
         this.sentCount.incrementAndGet();
         this.sentUncompressedTotal.addAndGet(uncompressedSize);
         this.sentCompressedTotal.addAndGet(compressedSize);
         this.sentUncompressedMin.accumulateAndGet(uncompressedSize, Math::min);
         this.sentUncompressedMax.accumulateAndGet(uncompressedSize, Math::max);
         this.sentCompressedMin.accumulateAndGet(compressedSize, Math::min);
         this.sentCompressedMax.accumulateAndGet(compressedSize, Math::max);
         this.sentUncompressedAvg.add(uncompressedSize);
         this.sentCompressedAvg.add(compressedSize);
         long now = System.nanoTime();
         this.sentRecently.add(new PacketStatsRecorderImpl.PacketStatsEntry.SizeRecord(now, uncompressedSize, compressedSize));
         this.pruneOld(this.sentRecently, now);
      }

      void recordReceive(int uncompressedSize, int compressedSize) {
         this.receivedCount.incrementAndGet();
         this.receivedUncompressedTotal.addAndGet(uncompressedSize);
         this.receivedCompressedTotal.addAndGet(compressedSize);
         this.receivedUncompressedMin.accumulateAndGet(uncompressedSize, Math::min);
         this.receivedUncompressedMax.accumulateAndGet(uncompressedSize, Math::max);
         this.receivedCompressedMin.accumulateAndGet(compressedSize, Math::min);
         this.receivedCompressedMax.accumulateAndGet(compressedSize, Math::max);
         this.receivedUncompressedAvg.add(uncompressedSize);
         this.receivedCompressedAvg.add(compressedSize);
         long now = System.nanoTime();
         this.receivedRecently.add(new PacketStatsRecorderImpl.PacketStatsEntry.SizeRecord(now, uncompressedSize, compressedSize));
         this.pruneOld(this.receivedRecently, now);
      }

      private void pruneOld(Queue<PacketStatsRecorderImpl.PacketStatsEntry.SizeRecord> queue, long now) {
         long cutoff = now - TimeUnit.SECONDS.toNanos(30L);

         for (PacketStatsRecorderImpl.PacketStatsEntry.SizeRecord head = queue.peek(); head != null && head.nanos < cutoff; head = queue.peek()) {
            queue.poll();
         }
      }

      @Override
      public boolean hasData() {
         return this.sentCount.get() > 0 || this.receivedCount.get() > 0;
      }

      @Override
      public int getPacketId() {
         return this.packetId;
      }

      @Nullable
      @Override
      public String getName() {
         PacketRegistry.PacketInfo info = PacketRegistry.all().get(this.packetId);
         return info != null ? info.name() : null;
      }

      @Override
      public int getSentCount() {
         return this.sentCount.get();
      }

      @Override
      public long getSentUncompressedTotal() {
         return this.sentUncompressedTotal.get();
      }

      @Override
      public long getSentCompressedTotal() {
         return this.sentCompressedTotal.get();
      }

      @Override
      public long getSentUncompressedMin() {
         return this.sentCount.get() > 0 ? this.sentUncompressedMin.get() : 0L;
      }

      @Override
      public long getSentUncompressedMax() {
         return this.sentUncompressedMax.get();
      }

      @Override
      public long getSentCompressedMin() {
         return this.sentCount.get() > 0 ? this.sentCompressedMin.get() : 0L;
      }

      @Override
      public long getSentCompressedMax() {
         return this.sentCompressedMax.get();
      }

      @Override
      public double getSentUncompressedAvg() {
         return this.sentUncompressedAvg.get();
      }

      @Override
      public double getSentCompressedAvg() {
         return this.sentCompressedAvg.get();
      }

      @Override
      public int getReceivedCount() {
         return this.receivedCount.get();
      }

      @Override
      public long getReceivedUncompressedTotal() {
         return this.receivedUncompressedTotal.get();
      }

      @Override
      public long getReceivedCompressedTotal() {
         return this.receivedCompressedTotal.get();
      }

      @Override
      public long getReceivedUncompressedMin() {
         return this.receivedCount.get() > 0 ? this.receivedUncompressedMin.get() : 0L;
      }

      @Override
      public long getReceivedUncompressedMax() {
         return this.receivedUncompressedMax.get();
      }

      @Override
      public long getReceivedCompressedMin() {
         return this.receivedCount.get() > 0 ? this.receivedCompressedMin.get() : 0L;
      }

      @Override
      public long getReceivedCompressedMax() {
         return this.receivedCompressedMax.get();
      }

      @Override
      public double getReceivedUncompressedAvg() {
         return this.receivedUncompressedAvg.get();
      }

      @Override
      public double getReceivedCompressedAvg() {
         return this.receivedCompressedAvg.get();
      }

      @Nonnull
      @Override
      public PacketStatsRecorder.RecentStats getSentRecently() {
         return this.computeRecentStats(this.sentRecently);
      }

      @Nonnull
      @Override
      public PacketStatsRecorder.RecentStats getReceivedRecently() {
         return this.computeRecentStats(this.receivedRecently);
      }

      private PacketStatsRecorder.RecentStats computeRecentStats(Queue<PacketStatsRecorderImpl.PacketStatsEntry.SizeRecord> queue) {
         int count = 0;
         long uncompressedTotal = 0L;
         long compressedTotal = 0L;
         int uncompressedMin = Integer.MAX_VALUE;
         int uncompressedMax = 0;
         int compressedMin = Integer.MAX_VALUE;
         int compressedMax = 0;

         for (PacketStatsRecorderImpl.PacketStatsEntry.SizeRecord record : queue) {
            count++;
            uncompressedTotal += record.uncompressedSize;
            compressedTotal += record.compressedSize;
            uncompressedMin = Math.min(uncompressedMin, record.uncompressedSize);
            uncompressedMax = Math.max(uncompressedMax, record.uncompressedSize);
            compressedMin = Math.min(compressedMin, record.compressedSize);
            compressedMax = Math.max(compressedMax, record.compressedSize);
         }

         return count == 0
            ? PacketStatsRecorder.RecentStats.EMPTY
            : new PacketStatsRecorder.RecentStats(count, uncompressedTotal, compressedTotal, uncompressedMin, uncompressedMax, compressedMin, compressedMax);
      }

      public void reset() {
         this.sentCount.set(0);
         this.sentUncompressedTotal.set(0L);
         this.sentCompressedTotal.set(0L);
         this.sentUncompressedMin.set(Long.MAX_VALUE);
         this.sentUncompressedMax.set(0L);
         this.sentCompressedMin.set(Long.MAX_VALUE);
         this.sentCompressedMax.set(0L);
         this.sentUncompressedAvg.clear();
         this.sentCompressedAvg.clear();
         this.sentRecently.clear();
         this.receivedCount.set(0);
         this.receivedUncompressedTotal.set(0L);
         this.receivedCompressedTotal.set(0L);
         this.receivedUncompressedMin.set(Long.MAX_VALUE);
         this.receivedUncompressedMax.set(0L);
         this.receivedCompressedMin.set(Long.MAX_VALUE);
         this.receivedCompressedMax.set(0L);
         this.receivedUncompressedAvg.clear();
         this.receivedCompressedAvg.clear();
         this.receivedRecently.clear();
      }

      public record SizeRecord(long nanos, int uncompressedSize, int compressedSize) {
      }
   }
}
