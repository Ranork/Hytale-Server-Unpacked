package com.hypixel.hytale.procedurallib.condition;

public interface IHeightThresholdInterpreter {
   int getLowestNonOne();

   int getHighestNonZero();

   float getThreshold(int var1, double var2, double var4, int var6);

   float getThreshold(int var1, double var2, double var4, int var6, double var7);

   double getContext(int var1, double var2, double var4);

   int getLength();

   default boolean isSpawnable(int height) {
      return height >= this.getLowestNonOne() && height <= this.getHighestNonZero();
   }

   static float lerp(float from, float to, float t) {
      return from + (to - from) * t;
   }
}
