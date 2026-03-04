package com.hypixel.hytale.metrics.metric;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.util.MathUtil;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;

public class HistoricMetric {
   public static final HistoricMetric[] EMPTY_ARRAY = new HistoricMetric[0];
   public static final Codec<HistoricMetric> METRICS_CODEC = BuilderCodec.builder(HistoricMetric.class, HistoricMetric::new)
      .append(new KeyedCodec<>("PeriodsNanos", Codec.LONG_ARRAY), (historicMetric, s) -> {
         throw new UnsupportedOperationException("Not supported");
      }, historicMetric -> historicMetric.periodsNanos)
      .add()
      .append(new KeyedCodec<>("Timestamps", Codec.LONG_ARRAY), (historicMetric, s) -> {
         throw new UnsupportedOperationException("Not supported");
      }, HistoricMetric::getAllTimestamps)
      .add()
      .append(new KeyedCodec<>("Values", Codec.LONG_ARRAY), (historicMetric, s) -> {
         throw new UnsupportedOperationException("Not supported");
      }, HistoricMetric::getAllValues)
      .add()
      .build();
   private final long[] periodsNanos;
   @Nonnull
   private final AverageCollector[] periodAverages;
   @Nonnull
   private final int[] startIndices;
   private final int bufferSize;
   @Nonnull
   private final long[] timestamps;
   @Nonnull
   private final long[] values;
   int nextIndex;

   private HistoricMetric() {
      throw new UnsupportedOperationException("Not supported");
   }

   private HistoricMetric(@Nonnull HistoricMetric.Builder builder) {
      this.periodsNanos = builder.periods.toLongArray();
      this.periodAverages = new AverageCollector[this.periodsNanos.length];

      for (int i = 0; i < this.periodAverages.length; i++) {
         this.periodAverages[i] = new AverageCollector();
      }

      this.startIndices = new int[this.periodsNanos.length];
      long longestPeriod = 0L;

      for (long period : this.periodsNanos) {
         if (period > longestPeriod) {
            longestPeriod = period;
         }
      }

      this.bufferSize = (int)MathUtil.fastCeil((double)longestPeriod / builder.minimumInterval);
      this.timestamps = new long[this.bufferSize];
      this.values = new long[this.bufferSize];
      Arrays.fill(this.timestamps, Long.MAX_VALUE);
   }

   public long[] getPeriodsNanos() {
      return this.periodsNanos;
   }

   public long calculateMin(int periodIndex) {
      int bufferSize = this.bufferSize;
      long[] values = this.values;
      int start = this.startIndices[periodIndex];
      int nextIndex = this.nextIndex;
      long min = Long.MAX_VALUE;
      if (start < nextIndex) {
         for (int i = start; i < nextIndex; i++) {
            long value = values[i];
            if (value < min) {
               min = value;
            }
         }
      } else {
         for (int ix = start; ix < bufferSize; ix++) {
            long value = values[ix];
            if (value < min) {
               min = value;
            }
         }

         for (int ixx = 0; ixx < nextIndex; ixx++) {
            long value = values[ixx];
            if (value < min) {
               min = value;
            }
         }
      }

      return min;
   }

   public double getAverage(int periodIndex) {
      return this.periodAverages[periodIndex].get();
   }

   public long calculateMax(int periodIndex) {
      int bufferSize = this.bufferSize;
      long[] values = this.values;
      int start = this.startIndices[periodIndex];
      int nextIndex = this.nextIndex;
      long max = Long.MIN_VALUE;
      if (start < nextIndex) {
         for (int i = start; i < nextIndex; i++) {
            long value = values[i];
            if (value > max) {
               max = value;
            }
         }
      } else {
         for (int ix = start; ix < bufferSize; ix++) {
            long value = values[ix];
            if (value > max) {
               max = value;
            }
         }

         for (int ixx = 0; ixx < nextIndex; ixx++) {
            long value = values[ixx];
            if (value > max) {
               max = value;
            }
         }
      }

      return max;
   }

   public void clear() {
      for (AverageCollector average : this.periodAverages) {
         average.clear();
      }

      Arrays.fill(this.startIndices, 0);
      Arrays.fill(this.timestamps, Long.MAX_VALUE);
      Arrays.fill(this.values, 0L);
      this.nextIndex = 0;
   }

   public void add(long timestampNanos, long value) {
      long[] periodsNanos = this.periodsNanos;
      AverageCollector[] periodAverages = this.periodAverages;
      int[] startIndices = this.startIndices;
      int bufferSize = this.bufferSize;
      long[] timestamps = this.timestamps;
      long[] values = this.values;
      int nextIndex = this.nextIndex;
      int periodLength = periodsNanos.length;

      for (int i = 0; i < periodLength; i++) {
         long oldestPossibleTimestamp = timestampNanos - periodsNanos[i];
         AverageCollector average = periodAverages[i];
         int start = startIndices[i];

         while (timestamps[start] < oldestPossibleTimestamp) {
            long oldValue = values[start];
            average.remove(oldValue);
            start = (start + 1) % bufferSize;
            if (start == nextIndex) {
               break;
            }
         }

         startIndices[i] = start;
         average.add(value);
      }

      timestamps[nextIndex] = timestampNanos;
      values[nextIndex] = value;
      this.nextIndex = (nextIndex + 1) % bufferSize;
   }

   public long[] getTimestamps(int periodIndex) {
      int start = this.startIndices[periodIndex];
      long[] timestamps = this.timestamps;
      int nextIndex = this.nextIndex;
      if (start < nextIndex) {
         return Arrays.copyOfRange(timestamps, start, nextIndex);
      } else {
         int length = timestamps.length - start;
         long[] data = new long[length + nextIndex];
         System.arraycopy(timestamps, start, data, 0, length);
         System.arraycopy(timestamps, 0, data, length, nextIndex);
         return data;
      }
   }

   public long[] getValues(int periodIndex) {
      int start = this.startIndices[periodIndex];
      long[] values = this.values;
      int nextIndex = this.nextIndex;
      if (start < nextIndex) {
         return Arrays.copyOfRange(values, start, nextIndex);
      } else {
         int length = this.bufferSize - start;
         long[] data = new long[length + nextIndex];
         System.arraycopy(values, start, data, 0, length);
         System.arraycopy(values, 0, data, length, nextIndex);
         return data;
      }
   }

   public long[] getAllTimestamps() {
      return this.getTimestamps(this.periodsNanos.length - 1);
   }

   public long[] getAllValues() {
      return this.getValues(this.periodsNanos.length - 1);
   }

   public void setAllTimestamps(@Nonnull long[] timestamps) {
      int length = timestamps.length;
      System.arraycopy(timestamps, 0, this.timestamps, 0, length);
      int periodIndex = 0;
      long last = timestamps[length - 1];

      for (int i = length - 2; i >= 0; i--) {
         if (last - timestamps[i] >= this.periodsNanos[periodIndex]) {
            this.startIndices[periodIndex] = i + 1;
            if (++periodIndex >= this.periodsNanos.length) {
               break;
            }
         }
      }

      if (periodIndex < this.periodsNanos.length) {
         while (periodIndex < this.periodsNanos.length) {
            this.periodsNanos[periodIndex] = 0L;
            periodIndex++;
         }
      }

      this.nextIndex = length;
   }

   public void setAllValues(@Nonnull long[] values) {
      System.arraycopy(values, 0, this.values, 0, values.length);
   }

   public long getLastValue() {
      return this.nextIndex == 0 ? this.values[this.bufferSize - 1] : this.values[this.nextIndex - 1];
   }

   @Nonnull
   public static HistoricMetric.Builder builder(long minimumInterval, @Nonnull TimeUnit unit) {
      return new HistoricMetric.Builder(minimumInterval, unit);
   }

   public static class Builder {
      private final long minimumInterval;
      private final LongList periods = new LongArrayList();

      private Builder(long minimumInterval, @Nonnull TimeUnit unit) {
         this.minimumInterval = unit.toNanos(minimumInterval);
      }

      @Nonnull
      public HistoricMetric.Builder addPeriod(long period, @Nonnull TimeUnit unit) {
         long nanos = unit.toNanos(period);

         for (int i = 0; i < this.periods.size(); i++) {
            if (this.periods.getLong(i) > nanos) {
               throw new IllegalArgumentException("Period's must be increasing in length");
            }
         }

         this.periods.add(nanos);
         return this;
      }

      @Nonnull
      public HistoricMetric build() {
         return new HistoricMetric(this);
      }
   }
}
