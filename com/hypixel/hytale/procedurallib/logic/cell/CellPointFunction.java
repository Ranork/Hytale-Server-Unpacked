package com.hypixel.hytale.procedurallib.logic.cell;

import com.hypixel.hytale.procedurallib.logic.DoubleArray;

public interface CellPointFunction {
   default double scale(double value) {
      return value;
   }

   default double normalize(double value) {
      return value;
   }

   int getHash(int var1, int var2, int var3);

   double getX(double var1, double var3);

   double getY(double var1, double var3);

   DoubleArray.Double2 getOffsets(int var1);
}
