package com.hypixel.hytale.procedurallib.logic.cell;

import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.procedurallib.logic.CellularNoise;
import com.hypixel.hytale.procedurallib.logic.DoubleArray;
import com.hypixel.hytale.procedurallib.logic.ResultBuffer;
import com.hypixel.hytale.procedurallib.logic.cell.evaluator.PointEvaluator;
import com.hypixel.hytale.procedurallib.logic.cell.jitter.CellJitter;
import com.hypixel.hytale.procedurallib.logic.point.PointConsumer;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

public class HexCellDistanceFunction implements CellDistanceFunction {
   public static final HexCellDistanceFunction DISTANCE_FUNCTION = new HexCellDistanceFunction();
   public static final CellPointFunction POINT_FUNCTION = new CellPointFunction() {
      @Override
      public double scale(double value) {
         return value * HexCellDistanceFunction.SCALE;
      }

      @Override
      public double normalize(double value) {
         return value * 0.3333333333333333;
      }

      @Override
      public int getHash(int seed, int cellX, int cellY) {
         return HexCellDistanceFunction.getHash(seed, cellX, cellY);
      }

      @Override
      public double getX(double x, double y) {
         return HexCellDistanceFunction.toHexX(x, y);
      }

      @Override
      public double getY(double x, double y) {
         return HexCellDistanceFunction.toHexY(x, y);
      }

      @Override
      public DoubleArray.Double2 getOffsets(int hash) {
         return HexCellDistanceFunction.HEX_CELL_2D[hash & 0xFF];
      }
   };
   protected static final double X_TO_GRID_X = Math.sqrt(3.0) / 3.0;
   protected static final double Y_TO_GRID_X = -0.3333333333333333;
   protected static final double Y_TO_GRID_Y = 0.6666666666666666;
   protected static final double X_TO_HEX_X = Math.sqrt(3.0);
   protected static final double Y_TO_HEX_X = Math.sqrt(3.0) / 2.0;
   protected static final double Y_TO_HEX_Y = 1.5;
   protected static final double NORMALIZATION = 0.3333333333333333;
   protected static final double SCALE = (X_TO_HEX_X + 1.5) / 2.0;
   public static final DoubleArray.Double2[] HEX_CELL_2D = Stream.of(CellularNoise.CELL_2D)
      .map(d -> new DoubleArray.Double2(d.x - 0.5, d.y - 0.5))
      .toArray(DoubleArray.Double2[]::new);

   @Override
   public double scale(double value) {
      return value * SCALE;
   }

   @Override
   public double invScale(double value) {
      return value / SCALE;
   }

   @Override
   public int getCellX(double x, double y) {
      return toGridX(x, y);
   }

   @Override
   public int getCellY(double x, double y) {
      return toGridY(x, y);
   }

   @Override
   public void nearest2D(
      int seed, double x, double y, int cellX, int cellY, @Nonnull ResultBuffer.ResultBuffer2d buffer, @Nonnull PointEvaluator pointEvaluator
   ) {
      this.evalPoint(seed, x, y, cellX - 1, cellY - 1, buffer, pointEvaluator);
      this.evalPoint(seed, x, y, cellX + 0, cellY - 1, buffer, pointEvaluator);
      this.evalPoint(seed, x, y, cellX + 1, cellY - 1, buffer, pointEvaluator);
      this.evalPoint(seed, x, y, cellX - 1, cellY + 0, buffer, pointEvaluator);
      this.evalPoint(seed, x, y, cellX + 0, cellY + 0, buffer, pointEvaluator);
      this.evalPoint(seed, x, y, cellX + 1, cellY + 0, buffer, pointEvaluator);
      this.evalPoint(seed, x, y, cellX - 1, cellY + 1, buffer, pointEvaluator);
      this.evalPoint(seed, x, y, cellX + 0, cellY + 1, buffer, pointEvaluator);
      this.evalPoint(seed, x, y, cellX + 1, cellY + 1, buffer, pointEvaluator);
      buffer.distance *= 0.3333333333333333;
   }

   @Override
   public void nearest3D(
      int seed, double x, double y, double z, int cellX, int cellY, int cellZ, ResultBuffer.ResultBuffer3d buffer, PointEvaluator pointEvaluator
   ) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void transition2D(
      int seed, double x, double y, int cellX, int cellY, @Nonnull ResultBuffer.ResultBuffer2d buffer, @Nonnull PointEvaluator pointEvaluator
   ) {
      this.evalPoint2(seed, x, y, cellX - 1, cellY - 1, buffer, pointEvaluator);
      this.evalPoint2(seed, x, y, cellX + 0, cellY - 1, buffer, pointEvaluator);
      this.evalPoint2(seed, x, y, cellX + 1, cellY - 1, buffer, pointEvaluator);
      this.evalPoint2(seed, x, y, cellX - 1, cellY + 0, buffer, pointEvaluator);
      this.evalPoint2(seed, x, y, cellX + 0, cellY + 0, buffer, pointEvaluator);
      this.evalPoint2(seed, x, y, cellX + 1, cellY + 0, buffer, pointEvaluator);
      this.evalPoint2(seed, x, y, cellX - 1, cellY + 1, buffer, pointEvaluator);
      this.evalPoint2(seed, x, y, cellX + 0, cellY + 1, buffer, pointEvaluator);
      this.evalPoint2(seed, x, y, cellX + 1, cellY + 1, buffer, pointEvaluator);
      CellJitter jitter = pointEvaluator.getJitter();
      if (jitter.getMaxX() > 0.5) {
         this.evalPoint2(seed, x, y, cellX - 2, cellY - 1, buffer, pointEvaluator);
         this.evalPoint2(seed, x, y, cellX - 2, cellY + 0, buffer, pointEvaluator);
         this.evalPoint2(seed, x, y, cellX - 2, cellY + 1, buffer, pointEvaluator);
         this.evalPoint2(seed, x, y, cellX + 2, cellY + 0, buffer, pointEvaluator);
         this.evalPoint2(seed, x, y, cellX + 2, cellY - 1, buffer, pointEvaluator);
         this.evalPoint2(seed, x, y, cellX + 2, cellY + 1, buffer, pointEvaluator);
      }

      if (jitter.getMaxY() > 0.5) {
         this.evalPoint2(seed, x, y, cellX - 1, cellY - 2, buffer, pointEvaluator);
         this.evalPoint2(seed, x, y, cellX + 0, cellY - 2, buffer, pointEvaluator);
         this.evalPoint2(seed, x, y, cellX + 1, cellY - 2, buffer, pointEvaluator);
         this.evalPoint2(seed, x, y, cellX - 1, cellY + 2, buffer, pointEvaluator);
         this.evalPoint2(seed, x, y, cellX + 0, cellY + 2, buffer, pointEvaluator);
         this.evalPoint2(seed, x, y, cellX + 1, cellY + 2, buffer, pointEvaluator);
      }

      buffer.distance *= 0.3333333333333333;
      buffer.distance2 *= 0.3333333333333333;
   }

   @Override
   public void transition3D(
      int seed, double x, double y, double z, int cellX, int cellY, int cellZ, ResultBuffer.ResultBuffer3d buffer, PointEvaluator pointEvaluator
   ) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void evalPoint(int seed, double x, double y, int cellX, int cellY, ResultBuffer.ResultBuffer2d buffer, @Nonnull PointEvaluator pointEvaluator) {
      int cellHash = getHash(seed, cellX, cellY);
      DoubleArray.Double2 vec = HEX_CELL_2D[cellHash & 0xFF];
      CellJitter jitter = pointEvaluator.getJitter();
      double px = jitter.getPointX(cellX, vec);
      double py = jitter.getPointY(cellY, vec);
      double hx = toHexX(px, py);
      double hy = toHexY(px, py);
      pointEvaluator.evalPoint(seed, x, y, cellHash, cellX, cellY, hx, hy, buffer);
   }

   @Override
   public void evalPoint(
      int seed, double x, double y, double z, int cellX, int cellY, int cellZ, ResultBuffer.ResultBuffer3d buffer, PointEvaluator pointEvaluator
   ) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void evalPoint2(int seed, double x, double y, int cellX, int cellY, ResultBuffer.ResultBuffer2d buffer, @Nonnull PointEvaluator pointEvaluator) {
      int cellHash = getHash(seed, cellX, cellY);
      DoubleArray.Double2 vec = HEX_CELL_2D[cellHash & 0xFF];
      CellJitter jitter = pointEvaluator.getJitter();
      double px = jitter.getPointX(cellX, vec);
      double py = jitter.getPointY(cellY, vec);
      double hx = toHexX(px, py);
      double hy = toHexY(px, py);
      pointEvaluator.evalPoint2(seed, x, y, cellHash, cellX, cellY, hx, hy, buffer);
   }

   @Override
   public void evalPoint2(
      int seed, double x, double y, double z, int cellX, int cellY, int cellZ, ResultBuffer.ResultBuffer3d buffer, PointEvaluator pointEvaluator
   ) {
      throw new UnsupportedOperationException();
   }

   @Override
   public <T> void collect(
      int originalSeed,
      int seed,
      int minX,
      int minY,
      int maxX,
      int maxY,
      @Nonnull ResultBuffer.Bounds2d bounds,
      T ctx,
      @Nonnull PointConsumer<T> collector,
      @Nonnull PointEvaluator pointEvaluator
   ) {
      minX--;
      minY--;
      maxX++;
      maxY++;
      int height = maxY - minY;
      int width = maxX - minX + (height >> 1);
      CellJitter jitter = pointEvaluator.getJitter();

      for (int dy = 0; dy <= height; dy++) {
         int cy = minY + dy;
         int startX = minX - (dy >> 1);

         for (int dx = 0; dx <= width; dx++) {
            int cx = startX + dx;
            int cellHash = getHash(seed, cx, cy);
            DoubleArray.Double2 vec = HEX_CELL_2D[cellHash & 0xFF];
            double px = jitter.getPointX(cx, vec);
            double py = jitter.getPointY(cy, vec);
            double hx = toHexX(px, py);
            double hy = toHexY(px, py);
            if (bounds.contains(hx, hy)) {
               hx /= SCALE;
               hy /= SCALE;
               pointEvaluator.collectPoint(cellHash, cx, cy, hx, hy, ctx, collector);
            }
         }
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "HexCellDistanceFunction{}";
   }

   public static int getHash(int seed, int x, int y) {
      return HexCellDistanceFunction.SquirrelHash.hash(seed, x, y);
   }

   public static int toGridX(double x, double y) {
      return (int)MathUtil.fastRound(X_TO_GRID_X * x + -0.3333333333333333 * y);
   }

   public static int toGridY(double x, double y) {
      return (int)MathUtil.fastRound(0.6666666666666666 * y);
   }

   public static double toHexX(double hx, double hy) {
      return X_TO_HEX_X * hx + Y_TO_HEX_X * hy;
   }

   public static double toHexY(double hx, double hy) {
      return 1.5 * hy;
   }

   public static class SquirrelHash {
      protected static final int HASH0 = 198491317;
      protected static final int BIT_NOISE1 = -1255572915;
      protected static final int BIT_NOISE2 = -1255572915;
      protected static final int BIT_NOISE3 = -1255572915;

      public static int hash(int seed, int x, int y) {
         int hash = x + y * 198491317;
         hash *= -1255572915;
         hash += seed;
         hash ^= hash >> 8;
         hash -= 1255572915;
         hash ^= hash << 8;
         hash *= -1255572915;
         return hash ^ hash >> 8;
      }
   }
}
