package com.hypixel.hytale.math.hitdetection;

public interface LineOfSightProvider {
   LineOfSightProvider DEFAULT_TRUE = (fromX, fromY, fromZ, toX, toY, toZ) -> true;

   boolean test(double var1, double var3, double var5, double var7, double var9, double var11);
}
