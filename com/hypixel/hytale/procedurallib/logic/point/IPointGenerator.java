package com.hypixel.hytale.procedurallib.logic.point;

import com.hypixel.hytale.procedurallib.logic.ResultBuffer;

public interface IPointGenerator {
   ResultBuffer.ResultBuffer2d nearest2D(int var1, double var2, double var4);

   ResultBuffer.ResultBuffer3d nearest3D(int var1, double var2, double var4, double var6);

   ResultBuffer.ResultBuffer2d transition2D(int var1, double var2, double var4);

   ResultBuffer.ResultBuffer3d transition3D(int var1, double var2, double var4, double var6);

   void collect(int var1, double var2, double var4, double var6, double var8, IPointGenerator.PointConsumer2d var10);

   double getInterval();

   @FunctionalInterface
   public interface PointConsumer2d {
      void accept(double var1, double var3);
   }
}
