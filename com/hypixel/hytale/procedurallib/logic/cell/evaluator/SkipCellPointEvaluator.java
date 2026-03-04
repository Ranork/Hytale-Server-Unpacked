package com.hypixel.hytale.procedurallib.logic.cell.evaluator;

import com.hypixel.hytale.procedurallib.logic.ResultBuffer;
import com.hypixel.hytale.procedurallib.logic.cell.jitter.CellJitter;
import com.hypixel.hytale.procedurallib.logic.point.PointConsumer;
import it.unimi.dsi.fastutil.HashCommon;
import javax.annotation.Nonnull;

public class SkipCellPointEvaluator implements PointEvaluator {
   @Nonnull
   protected final PointEvaluator pointEvaluator;
   @Nonnull
   protected final SkipCellPointEvaluator.Mode mode;
   protected final int mask;
   protected final int mid;
   public static final int DEFAULT_NO_SKIP = 0;
   public static final SkipCellPointEvaluator.Mode DEFAULT_MODE = SkipCellPointEvaluator.Mode.CHECKERBOARD;

   public SkipCellPointEvaluator(@Nonnull PointEvaluator pointEvaluator, @Nonnull SkipCellPointEvaluator.Mode mode, int period) {
      int interval = HashCommon.nextPowerOfTwo(Math.max(0, period) + 1);
      this.pointEvaluator = pointEvaluator;
      this.mode = mode;
      this.mask = interval - 1;
      this.mid = interval >> 1;
   }

   @Override
   public CellJitter getJitter() {
      return this.pointEvaluator.getJitter();
   }

   @Override
   public void evalPoint(
      int seed, double x, double y, int cellHash, int cellX, int cellY, double cellPointX, double cellPointY, ResultBuffer.ResultBuffer2d buffer
   ) {
      if (!this.skip(this.mode, cellX, cellY)) {
         this.pointEvaluator.evalPoint(seed, x, y, cellHash, cellX, cellY, cellPointX, cellPointY, buffer);
      }
   }

   @Override
   public void evalPoint2(
      int seed, double x, double y, int cellHash, int cellX, int cellY, double cellPointX, double cellPointY, ResultBuffer.ResultBuffer2d buffer
   ) {
      if (!this.skip(this.mode, cellX, cellY)) {
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

   @Override
   public <T> void collectPoint(int cellHash, int cellX, int cellY, double cellCentreX, double cellCentreY, T ctx, @Nonnull PointConsumer<T> consumer) {
      if (!this.skip(this.mode, cellX, cellY)) {
         this.pointEvaluator.collectPoint(cellHash, cellX, cellY, cellCentreX, cellCentreY, ctx, consumer);
      }
   }

   protected boolean skip(SkipCellPointEvaluator.Mode mode, int cx, int cy) {
      int x0 = cx & this.mask;
      int y0 = cy & this.mask;
      boolean result = x0 == 0 && y0 == 0 || mode == SkipCellPointEvaluator.Mode.CHECKERBOARD && x0 == this.mid && y0 == this.mid;
      return !result;
   }

   public static enum Mode {
      CHECKERBOARD,
      GRID;
   }
}
