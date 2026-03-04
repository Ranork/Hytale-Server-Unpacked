package com.hypixel.hytale.procedurallib.logic.cell.evaluator;

import com.hypixel.hytale.math.util.HashUtil;
import com.hypixel.hytale.procedurallib.condition.IDoubleCondition;
import com.hypixel.hytale.procedurallib.condition.IIntCondition;
import com.hypixel.hytale.procedurallib.logic.ResultBuffer;
import com.hypixel.hytale.procedurallib.logic.cell.jitter.CellJitter;
import com.hypixel.hytale.procedurallib.logic.point.PointConsumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DensityPointEvaluator implements PointEvaluator {
   protected final PointEvaluator pointEvaluator;
   protected final IIntCondition density;

   public DensityPointEvaluator(PointEvaluator pointEvaluator, IDoubleCondition density) {
      this(pointEvaluator, getDensityCondition(density));
   }

   public DensityPointEvaluator(PointEvaluator pointEvaluator, IIntCondition density) {
      this.pointEvaluator = pointEvaluator;
      this.density = density;
   }

   @Override
   public CellJitter getJitter() {
      return this.pointEvaluator.getJitter();
   }

   @Override
   public void evalPoint(
      int seed, double x, double y, int cellHash, int cellX, int cellY, double cellPointX, double cellPointY, ResultBuffer.ResultBuffer2d buffer
   ) {
      if (this.density.eval(cellHash)) {
         this.pointEvaluator.evalPoint(seed, x, y, cellHash, cellX, cellY, cellPointX, cellPointY, buffer);
      }
   }

   @Override
   public void evalPoint2(
      int seed, double x, double y, int cellHash, int cellX, int cellY, double cellPointX, double cellPointY, ResultBuffer.ResultBuffer2d buffer
   ) {
      if (this.density.eval(cellHash)) {
         this.pointEvaluator.evalPoint2(seed, x, y, cellHash, cellX, cellY, cellPointX, cellPointY, buffer);
      }
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
      ResultBuffer.ResultBuffer3d buffer
   ) {
      if (this.density.eval(cellHash)) {
         this.pointEvaluator.evalPoint(seed, x, y, z, cellHash, cellX, cellY, cellZ, cellPointX, cellPointY, cellPointZ, buffer);
      }
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
      ResultBuffer.ResultBuffer3d buffer
   ) {
      if (this.density.eval(cellHash)) {
         this.pointEvaluator.evalPoint2(seed, x, y, z, cellHash, cellX, cellY, cellZ, cellPointX, cellPointY, cellPointZ, buffer);
      }
   }

   @Override
   public <T> void collectPoint(int cellHash, int cellX, int cellY, double x, double y, T t, @Nonnull PointConsumer<T> consumer) {
      if (this.density.eval(cellHash)) {
         this.pointEvaluator.collectPoint(cellHash, cellX, cellY, x, y, t, consumer);
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "DensityPointEvaluator{pointEvaluator=" + this.pointEvaluator + ", density=" + this.density + "}";
   }

   @Nonnull
   public static IIntCondition getDensityCondition(@Nullable IDoubleCondition threshold) {
      return threshold == null ? seed -> true : seed -> threshold.eval(randomDensityCondition(seed));
   }

   public static double randomDensityCondition(int seed) {
      return HashUtil.random(seed, -1694747730L);
   }
}
