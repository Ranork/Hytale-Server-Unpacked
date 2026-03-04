package com.hypixel.hytale.procedurallib.logic.cell.evaluator;

import com.hypixel.hytale.procedurallib.logic.ResultBuffer;
import com.hypixel.hytale.procedurallib.logic.cell.DistanceCalculationMode;
import com.hypixel.hytale.procedurallib.logic.cell.PointDistanceFunction;
import javax.annotation.Nonnull;

public class NormalPointEvaluator implements PointEvaluator {
   public static final PointEvaluator EUCLIDEAN = new NormalPointEvaluator(DistanceCalculationMode.EUCLIDEAN.getFunction());
   public static final PointEvaluator MANHATTAN = new NormalPointEvaluator(DistanceCalculationMode.MANHATTAN.getFunction());
   public static final PointEvaluator NATURAL = new NormalPointEvaluator(DistanceCalculationMode.NATURAL.getFunction());
   public static final PointEvaluator MAX = new NormalPointEvaluator(DistanceCalculationMode.MAX.getFunction());
   protected final PointDistanceFunction distanceFunction;

   public NormalPointEvaluator(PointDistanceFunction distanceFunction) {
      this.distanceFunction = distanceFunction;
   }

   @Override
   public void evalPoint(
      int seed, double x, double y, int cellHash, int cellX, int cellY, double cellPointX, double cellPointY, @Nonnull ResultBuffer.ResultBuffer2d buffer
   ) {
      double distance = this.distanceFunction.distance2D(seed, cellX, cellY, cellPointX, cellPointY, cellPointX - x, cellPointY - y);
      buffer.register(cellHash, cellX, cellY, distance, cellPointX, cellPointY);
   }

   @Override
   public void evalPoint2(
      int seed, double x, double y, int cellHash, int cellX, int cellY, double cellPointX, double cellPointY, @Nonnull ResultBuffer.ResultBuffer2d buffer
   ) {
      double distance = this.distanceFunction.distance2D(seed, cellX, cellY, cellPointX, cellPointY, cellPointX - x, cellPointY - y);
      buffer.register2(cellHash, cellX, cellY, distance, cellPointX, cellPointY);
   }

   @Override
   public void evalPoint(
      int seed,
      double x,
      double y,
      double z,
      int cellHash,
      int cellX,
      int cellY,
      int cellZ,
      double cellPointX,
      double cellPointY,
      double cellPointZ,
      @Nonnull ResultBuffer.ResultBuffer3d buffer
   ) {
      double distance = this.distanceFunction
         .distance3D(seed, cellX, cellY, cellZ, cellPointX, cellPointY, cellPointZ, cellPointX - x, cellPointY - y, cellPointZ - z);
      buffer.register(cellHash, cellX, cellY, cellZ, distance, cellPointX, cellPointY, cellPointZ);
   }

   @Override
   public void evalPoint2(
      int seed,
      double x,
      double y,
      double z,
      int cellHash,
      int cellX,
      int cellY,
      int cellZ,
      double cellPointX,
      double cellPointY,
      double cellPointZ,
      @Nonnull ResultBuffer.ResultBuffer3d buffer
   ) {
      double distance = this.distanceFunction
         .distance3D(seed, cellX, cellY, cellZ, cellPointX, cellPointY, cellPointZ, cellPointX - x, cellPointY - y, cellPointZ - z);
      buffer.register2(cellHash, cellX, cellY, cellZ, distance, cellPointX, cellPointY, cellPointZ);
   }

   @Nonnull
   @Override
   public String toString() {
      return "NormalPointEvaluator{distanceFunction=" + this.distanceFunction + "}";
   }

   public static PointEvaluator of(PointDistanceFunction distanceFunction) {
      DistanceCalculationMode mode = DistanceCalculationMode.from(distanceFunction);
      if (mode == null) {
         return new NormalPointEvaluator(distanceFunction);
      } else {
         return switch (mode) {
            case EUCLIDEAN -> EUCLIDEAN;
            case MANHATTAN -> MANHATTAN;
            case NATURAL -> NATURAL;
            case MAX -> MAX;
         };
      }
   }
}
