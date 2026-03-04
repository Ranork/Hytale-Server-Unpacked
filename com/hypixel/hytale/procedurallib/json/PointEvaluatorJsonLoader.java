package com.hypixel.hytale.procedurallib.json;

import com.google.gson.JsonElement;
import com.hypixel.hytale.procedurallib.condition.DoubleThresholdCondition;
import com.hypixel.hytale.procedurallib.condition.IDoubleCondition;
import com.hypixel.hytale.procedurallib.condition.IDoubleThreshold;
import com.hypixel.hytale.procedurallib.logic.cell.DistanceCalculationMode;
import com.hypixel.hytale.procedurallib.logic.cell.MeasurementMode;
import com.hypixel.hytale.procedurallib.logic.cell.PointDistanceFunction;
import com.hypixel.hytale.procedurallib.logic.cell.evaluator.BorderPointEvaluator;
import com.hypixel.hytale.procedurallib.logic.cell.evaluator.JitterPointEvaluator;
import com.hypixel.hytale.procedurallib.logic.cell.evaluator.PointEvaluator;
import com.hypixel.hytale.procedurallib.logic.cell.evaluator.SkipCellPointEvaluator;
import com.hypixel.hytale.procedurallib.logic.cell.jitter.CellJitter;
import com.hypixel.hytale.procedurallib.logic.cell.jitter.DefaultCellJitter;
import com.hypixel.hytale.procedurallib.supplier.IDoubleRange;
import java.nio.file.Path;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PointEvaluatorJsonLoader<T extends SeedResource> extends AbstractCellJitterJsonLoader<T, PointEvaluator> {
   @Nonnull
   protected final MeasurementMode measurementMode;
   protected final PointDistanceFunction pointDistanceFunction;

   public PointEvaluatorJsonLoader(@Nonnull SeedString<T> seed, Path dataFolder, JsonElement json) {
      this(seed, dataFolder, json, null);
   }

   public PointEvaluatorJsonLoader(@Nonnull SeedString<T> seed, Path dataFolder, JsonElement json, @Nullable PointDistanceFunction pointDistanceFunction) {
      this(seed, dataFolder, json, MeasurementMode.CENTRE_DISTANCE, pointDistanceFunction);
   }

   public PointEvaluatorJsonLoader(
      @Nonnull SeedString<T> seed,
      Path dataFolder,
      JsonElement json,
      @Nonnull MeasurementMode measurementMode,
      @Nullable PointDistanceFunction pointDistanceFunction
   ) {
      super(seed.append(".PointEvaluator"), dataFolder, json);
      this.measurementMode = measurementMode;
      this.pointDistanceFunction = pointDistanceFunction;
   }

   public PointEvaluator load() {
      return switch (this.measurementMode) {
         case CENTRE_DISTANCE -> this.loadCentrePointEvaluator();
         case BORDER_DISTANCE -> this.loadBorderPointEvaluator();
      };
   }

   public PointEvaluator loadCentrePointEvaluator() {
      return PointEvaluator.of(
         this.loadPointDistanceFunction(), this.loadDensity(), this.loadDistanceRange(), this.loadSkipCount(), this.loadSkipMode(), this.loadJitter()
      );
   }

   @Nonnull
   public PointEvaluator loadBorderPointEvaluator() {
      BorderPointEvaluator pointEvaluator = BorderPointEvaluator.INSTANCE;
      CellJitter jitter = this.loadJitter();
      return (PointEvaluator)(jitter == DefaultCellJitter.DEFAULT_ONE ? pointEvaluator : new JitterPointEvaluator(pointEvaluator, jitter));
   }

   public PointDistanceFunction loadPointDistanceFunction() {
      if (this.pointDistanceFunction != null) {
         return this.pointDistanceFunction;
      } else {
         DistanceCalculationMode distanceCalculationMode = CellNoiseJsonLoader.Constants.DEFAULT_DISTANCE_MODE;
         if (this.has("DistanceMode")) {
            distanceCalculationMode = DistanceCalculationMode.valueOf(this.get("DistanceMode").getAsString());
         }

         return distanceCalculationMode.getFunction();
      }
   }

   @Nullable
   public IDoubleRange loadDistanceRange() {
      return !this.has("DistanceRange") ? null : new DoubleRangeJsonLoader<>(this.seed, this.dataFolder, this.get("DistanceRange")).load();
   }

   @Nullable
   public IDoubleCondition loadDensity() {
      if (!this.has("Density")) {
         return null;
      } else {
         IDoubleThreshold threshold = new DoubleThresholdJsonLoader<>(this.seed, this.dataFolder, this.get("Density")).load();
         return new DoubleThresholdCondition(threshold);
      }
   }

   public int loadSkipCount() {
      return this.mustGetNumber("Skip", 0).intValue();
   }

   public SkipCellPointEvaluator.Mode loadSkipMode() {
      String name = this.mustGetString("SkipMode", SkipCellPointEvaluator.DEFAULT_MODE.name());
      return SkipCellPointEvaluator.Mode.valueOf(name);
   }
}
