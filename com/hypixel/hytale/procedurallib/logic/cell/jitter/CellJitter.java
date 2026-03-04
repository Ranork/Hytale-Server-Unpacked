package com.hypixel.hytale.procedurallib.logic.cell.jitter;

import com.hypixel.hytale.procedurallib.logic.DoubleArray;
import javax.annotation.Nonnull;

public interface CellJitter {
   double getMaxX();

   double getMaxY();

   double getMaxZ();

   double getPointX(int var1, DoubleArray.Double2 var2);

   double getPointY(int var1, DoubleArray.Double2 var2);

   double getPointX(int var1, DoubleArray.Double3 var2);

   double getPointY(int var1, DoubleArray.Double3 var2);

   double getPointZ(int var1, DoubleArray.Double3 var2);

   @Nonnull
   static CellJitter of(double x, double y, double z) {
      return (CellJitter)(x == 1.0 && y == 1.0 && z == 1.0 ? DefaultCellJitter.DEFAULT_ONE : new ConstantCellJitter(x, y, z));
   }
}
