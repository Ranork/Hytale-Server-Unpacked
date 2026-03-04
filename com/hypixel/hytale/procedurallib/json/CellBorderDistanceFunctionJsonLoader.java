package com.hypixel.hytale.procedurallib.json;

import com.google.gson.JsonElement;
import com.hypixel.hytale.procedurallib.condition.IDoubleCondition;
import com.hypixel.hytale.procedurallib.logic.cell.BorderDistanceFunction;
import com.hypixel.hytale.procedurallib.logic.cell.CellDistanceFunction;
import com.hypixel.hytale.procedurallib.logic.cell.MeasurementMode;
import com.hypixel.hytale.procedurallib.logic.cell.evaluator.PointEvaluator;
import java.nio.file.Path;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CellBorderDistanceFunctionJsonLoader<K extends SeedResource> extends JsonLoader<K, BorderDistanceFunction> {
   protected final CellDistanceFunction distanceFunction;

   public CellBorderDistanceFunctionJsonLoader(SeedString<K> seed, Path dataFolder, JsonElement json, CellDistanceFunction distanceFunction) {
      super(seed, dataFolder, json);
      this.distanceFunction = distanceFunction;
   }

   @Nonnull
   public BorderDistanceFunction load() {
      return new BorderDistanceFunction(this.distanceFunction, this.loadPointEvaluator(), this.loadDensity());
   }

   @Nullable
   protected PointEvaluator loadPointEvaluator() {
      return new PointEvaluatorJsonLoader<>(this.seed, this.dataFolder, this.json, MeasurementMode.BORDER_DISTANCE, null).load();
   }

   @Nullable
   protected IDoubleCondition loadDensity() {
      return new PointEvaluatorJsonLoader<>(this.seed, this.dataFolder, this.json).loadDensity();
   }
}
