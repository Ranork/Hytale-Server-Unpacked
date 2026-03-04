package com.hypixel.hytale.procedurallib.logic.cell;

import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.procedurallib.logic.ResultBuffer;
import com.hypixel.hytale.procedurallib.logic.cell.evaluator.PointEvaluator;
import com.hypixel.hytale.procedurallib.logic.point.PointConsumer;

public interface CellDistanceFunction {
   default double scale(double value) {
      return value;
   }

   default double invScale(double value) {
      return value;
   }

   default int getCellX(double x, double y) {
      return MathUtil.floor(x);
   }

   default int getCellY(double x, double y) {
      return MathUtil.floor(y);
   }

   default int getCellX(double x, double y, double z) {
      return MathUtil.floor(x);
   }

   default int getCellY(double x, double y, double z) {
      return MathUtil.floor(y);
   }

   default int getCellZ(double x, double y, double z) {
      return MathUtil.floor(z);
   }

   void nearest2D(int var1, double var2, double var4, int var6, int var7, ResultBuffer.ResultBuffer2d var8, PointEvaluator var9);

   void nearest3D(int var1, double var2, double var4, double var6, int var8, int var9, int var10, ResultBuffer.ResultBuffer3d var11, PointEvaluator var12);

   void transition2D(int var1, double var2, double var4, int var6, int var7, ResultBuffer.ResultBuffer2d var8, PointEvaluator var9);

   void transition3D(int var1, double var2, double var4, double var6, int var8, int var9, int var10, ResultBuffer.ResultBuffer3d var11, PointEvaluator var12);

   void evalPoint(int var1, double var2, double var4, int var6, int var7, ResultBuffer.ResultBuffer2d var8, PointEvaluator var9);

   void evalPoint(int var1, double var2, double var4, double var6, int var8, int var9, int var10, ResultBuffer.ResultBuffer3d var11, PointEvaluator var12);

   void evalPoint2(int var1, double var2, double var4, int var6, int var7, ResultBuffer.ResultBuffer2d var8, PointEvaluator var9);

   void evalPoint2(int var1, double var2, double var4, double var6, int var8, int var9, int var10, ResultBuffer.ResultBuffer3d var11, PointEvaluator var12);

   <T> void collect(int var1, int var2, int var3, int var4, int var5, int var6, ResultBuffer.Bounds2d var7, T var8, PointConsumer<T> var9, PointEvaluator var10);
}
