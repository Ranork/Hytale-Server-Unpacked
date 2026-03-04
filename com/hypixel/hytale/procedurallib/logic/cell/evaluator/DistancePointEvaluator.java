package com.hypixel.hytale.procedurallib.logic.cell.evaluator;

import com.hypixel.hytale.math.util.HashUtil;
import com.hypixel.hytale.procedurallib.logic.ResultBuffer;
import com.hypixel.hytale.procedurallib.logic.cell.PointDistanceFunction;
import com.hypixel.hytale.procedurallib.supplier.IDoubleRange;
import com.hypixel.hytale.procedurallib.supplier.ISeedDoubleRange;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DistancePointEvaluator implements PointEvaluator {
   protected final PointDistanceFunction distanceFunction;
   protected final ISeedDoubleRange distanceMod;

   public DistancePointEvaluator(PointDistanceFunction distanceFunction, IDoubleRange distanceMod) {
      this(distanceFunction, getDistanceModifier(distanceMod));
   }

   public DistancePointEvaluator(PointDistanceFunction distanceFunction, ISeedDoubleRange distanceMod) {
      this.distanceFunction = distanceFunction;
      this.distanceMod = distanceMod;
   }

   @Override
   public void evalPoint(
      int seed, double x, double y, int cellHash, int cellX, int cellY, double cellPointX, double cellPointY, @Nonnull ResultBuffer.ResultBuffer2d buffer
   ) {
      double distance = this.distanceFunction.distance2D(seed, cellX, cellY, cellPointX, cellPointY, cellPointX - x, cellPointY - y);
      distance = this.distanceMod.getValue(cellHash, distance);
      buffer.register(cellHash, cellX, cellY, distance, cellPointX, cellPointY);
   }

   @Override
   public void evalPoint2(
      int seed, double x, double y, int cellHash, int cellX, int cellY, double cellPointX, double cellPointY, @Nonnull ResultBuffer.ResultBuffer2d buffer
   ) {
      double distance = this.distanceFunction.distance2D(seed, cellX, cellY, cellPointX, cellPointY, cellPointX - x, cellPointY - y);
      distance = this.distanceMod.getValue(cellHash, distance);
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
      distance = this.distanceMod.getValue(cellHash, distance);
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
      distance = this.distanceMod.getValue(cellHash, distance);
      buffer.register2(cellHash, cellX, cellY, cellZ, distance, cellPointX, cellPointY, cellPointZ);
   }

   @Nonnull
   @Override
   public String toString() {
      return "DistancePointEvaluator{distanceFunction=" + this.distanceFunction + ", distanceMod=" + this.distanceMod + "}";
   }

   @Nonnull
   public static ISeedDoubleRange getDistanceModifier(@Nullable IDoubleRange range) {
      return range == null ? ISeedDoubleRange.DIRECT : (seed, value) -> value * range.getValue(randomDistanceModification(seed));
   }

   public static double randomDistanceModification(int seed) {
      return HashUtil.random(seed, 1495661265L);
   }
}
