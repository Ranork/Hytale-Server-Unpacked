package com.hypixel.hytale.metrics;

import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface MetricProvider {
   @Nullable
   MetricResults toMetricResults();

   @Nonnull
   static <T, R> Function<T, MetricProvider> maybe(@Nonnull Function<T, R> func) {
      return t -> {
         R r = func.apply(t);
         return r instanceof MetricProvider ? (MetricProvider)r : null;
      };
   }
}
