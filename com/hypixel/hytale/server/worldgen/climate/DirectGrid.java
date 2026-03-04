package com.hypixel.hytale.server.worldgen.climate;

import com.hypixel.hytale.procedurallib.logic.ResultBuffer;
import com.hypixel.hytale.procedurallib.logic.cell.CellDistanceFunction;
import com.hypixel.hytale.procedurallib.logic.cell.evaluator.PointEvaluator;
import com.hypixel.hytale.procedurallib.logic.point.PointConsumer;

public class DirectGrid implements CellDistanceFunction {
   public static final DirectGrid INSTANCE = new DirectGrid();

   @Override
   public void nearest2D(int seed, double x, double y, int cellX, int cellY, ResultBuffer.ResultBuffer2d buffer, PointEvaluator pointEvaluator) {
      buffer.x = x;
      buffer.y = y;
   }

   @Override
   public void nearest3D(
      int seed, double x, double y, double z, int cellX, int cellY, int cellZ, ResultBuffer.ResultBuffer3d buffer, PointEvaluator pointEvaluator
   ) {
      buffer.x = x;
      buffer.y = y;
      buffer.z = z;
   }

   @Override
   public void transition2D(int seed, double x, double y, int cellX, int cellY, ResultBuffer.ResultBuffer2d buffer, PointEvaluator pointEvaluator) {
   }

   @Override
   public void transition3D(
      int seed, double x, double y, double z, int cellX, int cellY, int cellZ, ResultBuffer.ResultBuffer3d buffer, PointEvaluator pointEvaluator
   ) {
   }

   @Override
   public void evalPoint(int seed, double x, double y, int cellX, int cellY, ResultBuffer.ResultBuffer2d buffer, PointEvaluator pointEvaluator) {
   }

   @Override
   public void evalPoint(
      int seed, double x, double y, double z, int cellX, int cellY, int cellZ, ResultBuffer.ResultBuffer3d buffer, PointEvaluator pointEvaluator
   ) {
   }

   @Override
   public void evalPoint2(int seed, double x, double y, int cellX, int cellY, ResultBuffer.ResultBuffer2d buffer, PointEvaluator pointEvaluator) {
   }

   @Override
   public void evalPoint2(
      int seed, double x, double y, double z, int cellX, int cellY, int cellZ, ResultBuffer.ResultBuffer3d buffer, PointEvaluator pointEvaluator
   ) {
   }

   @Override
   public <T> void collect(
      int originalSeed,
      int seed,
      int minX,
      int minY,
      int maxX,
      int maxY,
      ResultBuffer.Bounds2d bounds,
      T ctx,
      PointConsumer<T> collector,
      PointEvaluator pointEvaluator
   ) {
   }
}
