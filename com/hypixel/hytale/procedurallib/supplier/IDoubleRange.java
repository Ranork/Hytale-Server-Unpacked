package com.hypixel.hytale.procedurallib.supplier;

import java.util.Random;
import java.util.function.DoubleSupplier;

public interface IDoubleRange {
   double getValue(double var1);

   double getValue(DoubleSupplier var1);

   double getValue(Random var1);

   double getValue(int var1, double var2, double var4, IDoubleCoordinateSupplier2d var6);

   double getValue(int var1, double var2, double var4, double var6, IDoubleCoordinateSupplier3d var8);
}
