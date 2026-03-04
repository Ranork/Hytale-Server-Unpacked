package com.hypixel.hytale.component.metric;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.metrics.MetricResults;
import com.hypixel.hytale.metrics.metric.HistoricMetric;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SystemMetricData {
   @Nonnull
   public static final Codec<SystemMetricData> CODEC = BuilderCodec.builder(SystemMetricData.class, SystemMetricData::new)
      .append(new KeyedCodec<>("Name", Codec.STRING), (systemMetricData, o) -> systemMetricData.name = o, systemMetricData -> systemMetricData.name)
      .add()
      .append(
         new KeyedCodec<>("ArchetypeChunkCount", Codec.INTEGER),
         (systemMetricData, o) -> systemMetricData.archetypeChunkCount = o,
         systemMetricData -> systemMetricData.archetypeChunkCount
      )
      .add()
      .append(
         new KeyedCodec<>("EntityCount", Codec.INTEGER),
         (systemMetricData, o) -> systemMetricData.entityCount = o,
         systemMetricData -> systemMetricData.entityCount
      )
      .add()
      .append(
         new KeyedCodec<>("HistoricMetric", HistoricMetric.METRICS_CODEC),
         (systemMetricData, o) -> systemMetricData.historicMetric = o,
         systemMetricData -> systemMetricData.historicMetric
      )
      .add()
      .append(
         new KeyedCodec<>("Metrics", MetricResults.CODEC), (systemMetricData, o) -> systemMetricData.metrics = o, systemMetricData -> systemMetricData.metrics
      )
      .add()
      .build();
   private String name;
   private int archetypeChunkCount;
   private int entityCount;
   @Nullable
   private HistoricMetric historicMetric;
   private MetricResults metrics;

   public SystemMetricData() {
   }

   public SystemMetricData(
      @Nonnull String name, int archetypeChunkCount, int entityCount, @Nullable HistoricMetric historicMetric, @Nonnull MetricResults metrics
   ) {
      this.name = name;
      this.archetypeChunkCount = archetypeChunkCount;
      this.entityCount = entityCount;
      this.historicMetric = historicMetric;
      this.metrics = metrics;
   }
}
