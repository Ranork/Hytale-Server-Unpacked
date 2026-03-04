package com.hypixel.hytale.component.system;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.metrics.MetricResults;

public interface MetricSystem<ECS_TYPE> {
   MetricResults toMetricResults(Store<ECS_TYPE> var1);
}
