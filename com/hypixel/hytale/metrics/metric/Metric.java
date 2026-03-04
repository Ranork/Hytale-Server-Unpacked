package com.hypixel.hytale.metrics.metric;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class Metric {
   public static final Codec<Metric> CODEC = BuilderCodec.builder(Metric.class, Metric::new)
      .append(new KeyedCodec<>("Min", Codec.LONG), (metric, s) -> metric.min = s, metric -> metric.min)
      .add()
      .append(new KeyedCodec<>("Average", Codec.DOUBLE), (metric, s) -> metric.average.set(s), metric -> metric.average.get())
      .add()
      .append(new KeyedCodec<>("Max", Codec.LONG), (metric, s) -> metric.max = s, metric -> metric.max)
      .add()
      .build();
   private long min;
   private final AverageCollector average = new AverageCollector();
   private long max;

   public Metric() {
      this.clear();
   }

   public void add(long value) {
      if (value < this.min) {
         this.min = value;
      }

      this.average.add(value);
      if (value > this.max) {
         this.max = value;
      }
   }

   public void remove(long value) {
      this.average.remove(value);
   }

   public long getMin() {
      return this.min;
   }

   public double getAverage() {
      return this.average.get();
   }

   public long getMax() {
      return this.max;
   }

   public void clear() {
      this.min = Long.MAX_VALUE;
      this.average.clear();
      this.max = Long.MIN_VALUE;
   }

   public void resetMinMax() {
      this.min = Long.MAX_VALUE;
      this.max = Long.MIN_VALUE;
   }

   public void calculateMinMax(long value) {
      if (value < this.min) {
         this.min = value;
      }

      if (value > this.max) {
         this.max = value;
      }
   }

   public void addToAverage(long value) {
      this.average.add(value);
   }

   public void set(@Nonnull Metric metric) {
      this.min = metric.min;
      this.average.set(metric.average.get());
      this.max = metric.max;
   }

   @Nonnull
   @Override
   public String toString() {
      return "Metric{min=" + this.min + ", average=" + this.average.get() + ", max=" + this.max + "}";
   }
}
