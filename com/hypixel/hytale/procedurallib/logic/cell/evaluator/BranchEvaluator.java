package com.hypixel.hytale.procedurallib.logic.cell.evaluator;

import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector2i;
import com.hypixel.hytale.procedurallib.logic.DoubleArray;
import com.hypixel.hytale.procedurallib.logic.ResultBuffer;
import com.hypixel.hytale.procedurallib.logic.cell.CellDistanceFunction;
import com.hypixel.hytale.procedurallib.logic.cell.CellPointFunction;
import com.hypixel.hytale.procedurallib.logic.cell.jitter.CellJitter;
import javax.annotation.Nonnull;

public class BranchEvaluator implements PointEvaluator {
   protected static final int CARDINAL_MASK = 1;
   protected static final int CARDINAL_MASK_RESULT_X = 0;
   protected static final int CARDINAL_MASK_RESULT_Y = 1;
   protected static final int RANDOM_DIRECTION_MASK = 3;
   protected static final Vector2i[] RANDOM_DIRECTIONS = new Vector2i[]{new Vector2i(1, 1), new Vector2i(1, -1), new Vector2i(-1, 1), new Vector2i(-1, -1)};
   @Nonnull
   protected final CellPointFunction pointFunction;
   protected final BranchEvaluator.Direction direction;
   protected final CellJitter jitter;
   protected final double branch2parentScale;
   protected final double invLineNormalization;

   public BranchEvaluator(
      @Nonnull CellDistanceFunction parentFunction,
      @Nonnull CellPointFunction linePointFunction,
      BranchEvaluator.Direction direction,
      CellJitter jitter,
      double branchScale
   ) {
      this.pointFunction = linePointFunction;
      this.direction = direction;
      this.jitter = jitter;
      double inverseScalar = 1.0 / linePointFunction.scale(branchScale);
      this.branch2parentScale = parentFunction.scale(inverseScalar);
      this.invLineNormalization = 1.0 / linePointFunction.normalize(1.0);
   }

   @Override
   public CellJitter getJitter() {
      return this.jitter;
   }

   @Override
   public void evalPoint(int seed, double x, double y, int hashA, int cax, int cay, double ax, double ay, @Nonnull ResultBuffer.ResultBuffer2d buffer) {
      int dx = getConnectionX(this.direction, buffer.ix2, buffer.x2, hashA, ax * this.branch2parentScale);
      int dy = getConnectionY(this.direction, buffer.ix2, buffer.y2, hashA, ay * this.branch2parentScale);
      int cbx = cax + dx;
      int cby = cay + dy;
      int hashB = this.pointFunction.getHash(seed, cbx, cby);
      DoubleArray.Double2 offsetsB = this.pointFunction.getOffsets(hashB);
      double rawBx = this.getJitter().getPointX(cbx, offsetsB);
      double rawBy = this.getJitter().getPointY(cby, offsetsB);
      double bx = this.pointFunction.getX(rawBx, rawBy);
      double by = this.pointFunction.getY(rawBx, rawBy);
      if (checkBounds(x, y, ax, ay, bx, by, buffer.distance2)) {
         double dist2 = MathUtil.distanceToLineSq(x, y, ax, ay, bx, by);
         dist2 *= this.invLineNormalization;
         buffer.register(hashA, cax, cay, dist2, ax, ay);
      }
   }

   @Override
   public void evalPoint2(int seed, double x, double y, int cellHash, int xi, int yi, double vecX, double vecY, ResultBuffer.ResultBuffer2d buffer) {
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
   }

   protected static int getConnectionX(BranchEvaluator.Direction direction, int regionHash, double regionCoord, int cellHash, double cellCoord) {
      if ((cellHash & 1) != 0) {
         return 0;
      } else {
         return switch (direction) {
            case OUTWARD -> cellCoord < regionCoord ? -1 : 1;
            case INWARD -> cellCoord > regionCoord ? -1 : 1;
            case RANDOM -> RANDOM_DIRECTIONS[regionHash & 3].x;
         };
      }
   }

   protected static int getConnectionY(BranchEvaluator.Direction direction, int regionHash, double regionCoord, int cellHash, double cellCoord) {
      if ((cellHash & 1) != 1) {
         return 0;
      } else {
         return switch (direction) {
            case OUTWARD -> cellCoord < regionCoord ? -1 : 1;
            case INWARD -> cellCoord > regionCoord ? -1 : 1;
            case RANDOM -> RANDOM_DIRECTIONS[regionHash & 3].y;
         };
      }
   }

   protected static boolean checkBounds(double x, double y, double ax, double ay, double bx, double by, double thickness) {
      double minX = Math.min(ax, bx) - thickness;
      double minY = Math.min(ay, by) - thickness;
      double maxX = Math.max(ax, bx) + thickness;
      double maxY = Math.max(ay, by) + thickness;
      return x > minX && x < maxX && y > minY && y < maxY;
   }

   public static enum Direction {
      OUTWARD,
      INWARD,
      RANDOM;
   }
}
