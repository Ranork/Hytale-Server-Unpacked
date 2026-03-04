package com.hypixel.hytale.metrics;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import javax.annotation.Nonnull;
import org.bson.BsonValue;

public class ExecutorMetricsRegistry<T extends ExecutorMetricsRegistry.ExecutorMetric> extends MetricsRegistry<T> {
   public BsonValue encode(@Nonnull T t, ExtraInfo extraInfo) {
      return t.isInThread() ? super.encode(t, extraInfo) : CompletableFuture.<BsonValue>supplyAsync(() -> super.encode(t, extraInfo), t).join();
   }

   public <R extends MetricProvider> ExecutorMetricsRegistry<T> register(String id, @Nonnull Function<T, R> func) {
      return (ExecutorMetricsRegistry<T>)super.register(id, func);
   }

   public <R> ExecutorMetricsRegistry<T> register(String id, Function<T, R> func, Codec<R> codec) {
      return (ExecutorMetricsRegistry<T>)super.register(id, func, codec);
   }

   public ExecutorMetricsRegistry<T> register(String id, MetricsRegistry<Void> metricsRegistry) {
      return (ExecutorMetricsRegistry<T>)super.register(id, metricsRegistry);
   }

   public <R> ExecutorMetricsRegistry<T> register(String id, Function<T, R> func, Function<R, MetricsRegistry<R>> codecFunc) {
      return (ExecutorMetricsRegistry<T>)super.register(id, func, codecFunc);
   }

   public interface ExecutorMetric extends Executor {
      boolean isInThread();
   }
}
