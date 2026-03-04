package com.hypixel.hytale.procedurallib.json;

import com.google.gson.JsonElement;
import com.hypixel.hytale.procedurallib.logic.cell.CellDistanceFunction;
import com.hypixel.hytale.procedurallib.logic.cell.DistanceCalculationMode;
import com.hypixel.hytale.procedurallib.logic.cell.PointDistanceFunction;
import com.hypixel.hytale.procedurallib.logic.cell.evaluator.PointEvaluator;
import com.hypixel.hytale.procedurallib.logic.point.DistortedPointGenerator;
import com.hypixel.hytale.procedurallib.logic.point.IPointGenerator;
import com.hypixel.hytale.procedurallib.logic.point.OffsetPointGenerator;
import com.hypixel.hytale.procedurallib.logic.point.PointGenerator;
import com.hypixel.hytale.procedurallib.logic.point.ScaledPointGenerator;
import com.hypixel.hytale.procedurallib.random.CoordinateRotator;
import java.nio.file.Path;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PointGeneratorJsonLoader<K extends SeedResource> extends JsonLoader<K, IPointGenerator> {
   public PointGeneratorJsonLoader(SeedString<K> seed, Path dataFolder, JsonElement json) {
      super(seed, dataFolder, json);
   }

   public IPointGenerator load() {
      PointGenerator pointGenerator = this.newPointGenerator(this.loadSeed(), this.loadCellDistanceFunction());
      IPointGenerator generator = pointGenerator;
      if (this.has("Scale")) {
         generator = new ScaledPointGenerator(pointGenerator, this.get("Scale").getAsDouble());
      }

      if (this.has("Randomizer")) {
         generator = new DistortedPointGenerator(generator, new CoordinateRandomizerJsonLoader<>(this.seed, this.dataFolder, this.get("Randomizer")).load());
      }

      double offsetX = Double.NEGATIVE_INFINITY;
      double offsetY = Double.NEGATIVE_INFINITY;
      double offsetZ = Double.NEGATIVE_INFINITY;
      if (this.has("Offset")) {
         offsetX = offsetY = offsetZ = this.get("Offset").getAsDouble();
      }

      if (this.has("OffsetX")) {
         offsetX = this.get("OffsetX").getAsDouble();
      }

      if (this.has("OffsetY")) {
         offsetY = this.get("OffsetY").getAsDouble();
      }

      if (this.has("OffsetZ")) {
         offsetZ = this.get("OffsetZ").getAsDouble();
      }

      if (offsetX != Double.NEGATIVE_INFINITY || offsetY != Double.NEGATIVE_INFINITY || offsetZ != Double.NEGATIVE_INFINITY) {
         if (offsetX == Double.NEGATIVE_INFINITY) {
            offsetX = 0.0;
         }

         if (offsetY == Double.NEGATIVE_INFINITY) {
            offsetY = 0.0;
         }

         if (offsetZ == Double.NEGATIVE_INFINITY) {
            offsetZ = 0.0;
         }

         generator = new OffsetPointGenerator(generator, offsetX, offsetY, offsetZ);
      }

      if (this.has("Rotate")) {
         CoordinateRotator rotation = new CoordinateRotatorJsonLoader<>(this.seed, this.dataFolder, this.get("Rotate")).load();
         if (rotation != CoordinateRotator.NONE) {
            generator = new DistortedPointGenerator(generator, rotation);
         }
      }

      return generator;
   }

   protected int loadSeed() {
      int seedVal = this.seed.hashCode();
      if (this.has("Seed")) {
         SeedString<?> overwritten = this.seed.appendToOriginal(this.get("Seed").getAsString());
         seedVal = overwritten.hashCode();
         this.seed.get().reportSeeds(seedVal, this.seed.original, this.seed.seed, overwritten.seed);
      } else {
         this.seed.get().reportSeeds(seedVal, this.seed.original, this.seed.seed, null);
      }

      return seedVal;
   }

   @Nonnull
   protected PointGenerator newPointGenerator(int seedOffset, CellDistanceFunction cellDistanceFunction) {
      K seedResource = this.seed.get();
      PointEvaluator pointEvaluator = this.loadPointEvaluator();
      return new SeedResourcePointGenerator(seedOffset, cellDistanceFunction, pointEvaluator, seedResource);
   }

   @Nullable
   protected CellDistanceFunction loadCellDistanceFunction() {
      return new CellDistanceFunctionJsonLoader<>(this.seed, this.dataFolder, this.json, this.loadPointDistanceFunction()).load();
   }

   @Nullable
   protected PointEvaluator loadPointEvaluator() {
      return new PointEvaluatorJsonLoader<>(this.seed, this.dataFolder, this.json).load();
   }

   protected PointDistanceFunction loadPointDistanceFunction() {
      DistanceCalculationMode distanceCalculationMode = CellNoiseJsonLoader.Constants.DEFAULT_DISTANCE_MODE;
      if (this.has("DistanceMode")) {
         distanceCalculationMode = DistanceCalculationMode.valueOf(this.get("DistanceMode").getAsString());
      }

      return distanceCalculationMode.getFunction();
   }

   public interface Constants {
      String KEY_SEED = "Seed";
      String KEY_SCALE = "Scale";
      String KEY_RANDOMIZER = "Randomizer";
      String KEY_OFFSET = "Offset";
      String KEY_OFFSET_X = "OffsetX";
      String KEY_OFFSET_Y = "OffsetY";
      String KEY_OFFSET_Z = "OffsetZ";
   }
}
