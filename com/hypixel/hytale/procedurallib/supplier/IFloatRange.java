package com.hypixel.hytale.procedurallib.supplier;

import java.util.Random;

public interface IFloatRange {
   float getValue(float var1);

   float getValue(FloatSupplier var1);

   float getValue(Random var1);

   float getValue(int var1, double var2, double var4, IDoubleCoordinateSupplier2d var6);

   float getValue(int var1, double var2, double var4, double var6, IDoubleCoordinateSupplier3d var8);
}
