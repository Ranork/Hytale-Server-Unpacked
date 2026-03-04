package com.hypixel.hytale.procedurallib.logic.point;

import com.hypixel.hytale.procedurallib.logic.ResultBuffer;
import com.hypixel.hytale.procedurallib.logic.cell.CellDistanceFunction;
import com.hypixel.hytale.procedurallib.logic.cell.evaluator.PointEvaluator;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PointGenerator implements IPointGenerator {
   protected final int seedOffset;
   protected final CellDistanceFunction cellDistanceFunction;
   protected final PointEvaluator pointEvaluator;

   public PointGenerator(int seedOffset, CellDistanceFunction cellDistanceFunction, PointEvaluator pointEvaluator) {
      this.seedOffset = seedOffset;
      this.cellDistanceFunction = cellDistanceFunction;
      this.pointEvaluator = pointEvaluator;
   }

   @Nonnull
   protected ResultBuffer.Bounds2d localBounds2d() {
      return ResultBuffer.bounds2d;
   }

   @Nonnull
   protected ResultBuffer.ResultBuffer2d localBuffer2d() {
      return ResultBuffer.buffer2d;
   }

   @Nonnull
   protected ResultBuffer.ResultBuffer3d localBuffer3d() {
      return ResultBuffer.buffer3d;
   }

   @Nonnull
   @Override
   public ResultBuffer.ResultBuffer2d nearest2D(int seed, double x, double y) {
      x = this.cellDistanceFunction.scale(x);
      y = this.cellDistanceFunction.scale(y);
      int xr = this.cellDistanceFunction.getCellX(x, y);
      int yr = this.cellDistanceFunction.getCellY(x, y);
      ResultBuffer.ResultBuffer2d buffer = this.localBuffer2d();
      buffer.distance = Double.POSITIVE_INFINITY;
      this.cellDistanceFunction.nearest2D(seed + this.seedOffset, x, y, xr, yr, buffer, this.pointEvaluator);
      return buffer;
   }

   @Nonnull
   @Override
   public ResultBuffer.ResultBuffer3d nearest3D(int seed, double x, double y, double z) {
      x = this.cellDistanceFunction.scale(x);
      y = this.cellDistanceFunction.scale(y);
      z = this.cellDistanceFunction.scale(z);
      int xr = this.cellDistanceFunction.getCellX(x, y, z);
      int yr = this.cellDistanceFunction.getCellY(x, y, z);
      int zr = this.cellDistanceFunction.getCellZ(x, y, z);
      ResultBuffer.ResultBuffer3d buffer = this.localBuffer3d();
      buffer.distance = Double.POSITIVE_INFINITY;
      this.cellDistanceFunction.nearest3D(seed + this.seedOffset, x, y, z, xr, yr, zr, buffer, this.pointEvaluator);
      return buffer;
   }

   @Nonnull
   @Override
   public ResultBuffer.ResultBuffer2d transition2D(int seed, double x, double y) {
      x = this.cellDistanceFunction.scale(x);
      y = this.cellDistanceFunction.scale(y);
      int xr = this.cellDistanceFunction.getCellX(x, y);
      int yr = this.cellDistanceFunction.getCellY(x, y);
      ResultBuffer.ResultBuffer2d buffer = this.localBuffer2d();
      buffer.distance = Double.POSITIVE_INFINITY;
      buffer.distance2 = Double.POSITIVE_INFINITY;
      this.cellDistanceFunction.transition2D(seed + this.seedOffset, x, y, xr, yr, buffer, this.pointEvaluator);
      return buffer;
   }

   @Nonnull
   @Override
   public ResultBuffer.ResultBuffer3d transition3D(int seed, double x, double y, double z) {
      x = this.cellDistanceFunction.scale(x);
      y = this.cellDistanceFunction.scale(y);
      z = this.cellDistanceFunction.scale(z);
      int xr = this.cellDistanceFunction.getCellX(x, y, z);
      int yr = this.cellDistanceFunction.getCellY(x, y, z);
      int zr = this.cellDistanceFunction.getCellZ(x, y, z);
      ResultBuffer.ResultBuffer3d buffer = this.localBuffer3d();
      buffer.distance = Double.POSITIVE_INFINITY;
      buffer.distance2 = Double.POSITIVE_INFINITY;
      this.cellDistanceFunction.transition3D(seed + this.seedOffset, x, y, z, xr, yr, zr, buffer, this.pointEvaluator);
      return buffer;
   }

   @Override
   public double getInterval() {
      return 1.0;
   }

   @Override
   public void collect(int seed, double minX, double minY, double maxX, double maxY, IPointGenerator.PointConsumer2d consumer) {
      this.collect0(seed, minX, minY, maxX, maxY, (x, y, t) -> t.accept(x, y), consumer);
   }

   public void collect0(
      int seed,
      double minX,
      double minY,
      double maxX,
      double maxY,
      PointConsumer<IPointGenerator.PointConsumer2d> pointConsumer,
      IPointGenerator.PointConsumer2d consumer
   ) {
      minX = this.cellDistanceFunction.scale(minX);
      minY = this.cellDistanceFunction.scale(minY);
      maxX = this.cellDistanceFunction.scale(maxX);
      maxY = this.cellDistanceFunction.scale(maxY);
      int x0 = this.cellDistanceFunction.getCellX(minX, minY);
      int y0 = this.cellDistanceFunction.getCellY(minX, minY);
      int x1 = this.cellDistanceFunction.getCellX(maxX, maxY);
      int y1 = this.cellDistanceFunction.getCellY(maxX, maxY);
      ResultBuffer.Bounds2d bounds = this.localBounds2d();
      bounds.assign(minX, minY, maxX, maxY);
      this.cellDistanceFunction.collect(seed, seed + this.seedOffset, x0, y0, x1, y1, bounds, consumer, pointConsumer, this.pointEvaluator);
   }

   @Override
   public boolean equals(@Nullable Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         PointGenerator that = (PointGenerator)o;
         if (this.seedOffset != that.seedOffset) {
            return false;
         } else {
            return !this.cellDistanceFunction.equals(that.cellDistanceFunction) ? false : this.pointEvaluator.equals(that.pointEvaluator);
         }
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      int result = this.seedOffset;
      result = 31 * result + this.cellDistanceFunction.hashCode();
      return 31 * result + this.pointEvaluator.hashCode();
   }

   @Nonnull
   @Override
   public String toString() {
      return "PointGenerator{seedOffset="
         + this.seedOffset
         + ", cellDistanceFunction="
         + this.cellDistanceFunction
         + ", pointEvaluator="
         + this.pointEvaluator
         + "}";
   }
}
