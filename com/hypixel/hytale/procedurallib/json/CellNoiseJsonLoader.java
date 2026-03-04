package com.hypixel.hytale.procedurallib.json;

import com.google.gson.JsonElement;
import com.hypixel.hytale.procedurallib.NoiseFunction;
import com.hypixel.hytale.procedurallib.logic.CellNoise;
import com.hypixel.hytale.procedurallib.logic.ResultBuffer;
import com.hypixel.hytale.procedurallib.logic.cell.CellDistanceFunction;
import com.hypixel.hytale.procedurallib.logic.cell.CellType;
import com.hypixel.hytale.procedurallib.logic.cell.DistanceCalculationMode;
import com.hypixel.hytale.procedurallib.logic.cell.evaluator.PointEvaluator;
import com.hypixel.hytale.procedurallib.property.NoiseProperty;
import java.nio.file.Path;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CellNoiseJsonLoader<K extends SeedResource> extends JsonLoader<K, NoiseFunction> {
   public CellNoiseJsonLoader(@Nonnull SeedString<K> seed, Path dataFolder, JsonElement json) {
      super(seed.append(".CellNoise"), dataFolder, json);
   }

   @Nonnull
   public NoiseFunction load() {
      CellDistanceFunction cellDistanceFunction = this.loadCellDistanceFunction();
      PointEvaluator pointEvaluator = this.loadPointEvaluator();
      CellNoise.CellFunction cellFunction = this.loadCellFunction();
      NoiseProperty noiseLookup = this.loadNoiseLookup();
      return new CellNoiseJsonLoader.LoadedCellNoise(cellDistanceFunction, pointEvaluator, cellFunction, noiseLookup, this.seed.get());
   }

   @Nullable
   protected CellDistanceFunction loadCellDistanceFunction() {
      return new CellDistanceFunctionJsonLoader<>(this.seed, this.dataFolder, this.json, null).load();
   }

   @Nullable
   protected PointEvaluator loadPointEvaluator() {
      return new PointEvaluatorJsonLoader<>(this.seed, this.dataFolder, this.json).load();
   }

   protected CellNoise.CellFunction loadCellFunction() {
      CellNoise.CellMode cellMode = CellNoiseJsonLoader.Constants.DEFAULT_CELL_MODE;
      if (this.has("CellMode")) {
         cellMode = CellNoise.CellMode.valueOf(this.get("CellMode").getAsString());
      }

      return cellMode.getFunction();
   }

   @Nullable
   protected NoiseProperty loadNoiseLookup() {
      NoiseProperty noiseProperty = null;
      if (this.has("NoiseLookup")) {
         noiseProperty = new NoisePropertyJsonLoader<>(this.seed, this.dataFolder, this.get("NoiseLookup")).load();
      }

      return noiseProperty;
   }

   public interface Constants {
      String KEY_JITTER = "Jitter";
      String KEY_JITTER_X = "JitterX";
      String KEY_JITTER_Y = "JitterY";
      String KEY_JITTER_Z = "JitterZ";
      String KEY_DENSITY = "Density";
      String KEY_CELL_MODE = "CellMode";
      String KEY_NOISE_LOOKUP = "NoiseLookup";
      String KEY_DISTANCE_MODE = "DistanceMode";
      String KEY_DISTANCE_RANGE = "DistanceRange";
      String KEY_CELL_TYPE = "CellType";
      String KEY_SKIP_CELLS = "Skip";
      String KEY_SKIP_MODE = "SkipMode";
      double DEFAULT_JITTER = 1.0;
      double DEFAULT_DISTANCE_RANGE = 1.0;
      double DEFAULT_DENSITY_LOWER = 0.0;
      double DEFAULT_DENSITY_UPPER = 1.0;
      DistanceCalculationMode DEFAULT_DISTANCE_MODE = DistanceCalculationMode.EUCLIDEAN;
      CellNoise.CellMode DEFAULT_CELL_MODE = CellNoise.CellMode.CELL_VALUE;
      CellType DEFAULT_CELL_TYPE = CellType.SQUARE;
   }

   private static class LoadedCellNoise extends CellNoise {
      private final SeedResource seedResource;

      public LoadedCellNoise(
         CellDistanceFunction cellDistanceFunction,
         PointEvaluator pointEvaluator,
         CellNoise.CellFunction cellFunction,
         @Nullable NoiseProperty noiseLookup,
         SeedResource seedResource
      ) {
         super(cellDistanceFunction, pointEvaluator, cellFunction, noiseLookup);
         this.seedResource = seedResource;
      }

      @Nonnull
      @Override
      protected ResultBuffer.ResultBuffer2d localBuffer2d() {
         return this.seedResource.localBuffer2d();
      }

      @Nonnull
      @Override
      protected ResultBuffer.ResultBuffer3d localBuffer3d() {
         return this.seedResource.localBuffer3d();
      }
   }
}
