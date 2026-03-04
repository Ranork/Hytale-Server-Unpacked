package com.hypixel.hytale.procedurallib.logic.cell.evaluator;

import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.procedurallib.logic.ResultBuffer;
import javax.annotation.Nonnull;

public class BorderPointEvaluator implements PointEvaluator {
   public static final BorderPointEvaluator INSTANCE = new BorderPointEvaluator();

   @Override
   public void evalPoint(
      int seed, double x, double y, int cellHash, int cellX, int cellY, double cellPointX, double cellPointY, @Nonnull ResultBuffer.ResultBuffer2d buffer
   ) {
      if (!isOrigin(cellX, cellY, buffer)) {
         double distance = getBorderDistance(x, y, buffer.x2, buffer.y2, cellPointX, cellPointY);
         if (distance < buffer.distance) {
            buffer.distance = distance;
         }
      }
   }

   @Override
   public void evalPoint2(
      int seed, double x, double y, int cellHash, int cellX, int cellY, double cellPointX, double cellPointY, @Nonnull ResultBuffer.ResultBuffer2d buffer
   ) {
      if (!isOrigin(cellX, cellY, buffer)) {
         double distance = getBorderDistance(x, y, buffer.x2, buffer.y2, cellPointX, cellPointY);
         if (distance < buffer.distance) {
            buffer.distance2 = buffer.distance;
            buffer.distance = distance;
         } else if (distance < buffer.distance2) {
            buffer.distance2 = distance;
         }
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
      throw new UnsupportedOperationException();
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
      throw new UnsupportedOperationException();
   }

   @Nonnull
   @Override
   public String toString() {
      return "BorderPointEvaluator{}";
   }

   protected static boolean isOrigin(int cellX, int cellY, @Nonnull ResultBuffer.ResultBuffer2d buffer) {
      return cellX == buffer.ix2 && cellY == buffer.iy2;
   }

   protected static double getBorderDistance(double x, double y, double originX, double originY, double cellPointX, double cellPointY) {
      double ax = (cellPointX + originX) * 0.5;
      double ay = (cellPointY + originY) * 0.5;
      double normX = -(cellPointY - originY);
      double normY = cellPointX - originX;
      double bx = ax + normX;
      double by = ay + normY;
      return MathUtil.distanceToInfLineSq(x, y, ax, ay, bx, by);
   }
}
