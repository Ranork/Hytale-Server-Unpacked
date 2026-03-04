package com.hypixel.hytale.builtin.hytalegenerator.framework.math;

public class MultipliedIteration {
   public static double calculateMultiplier(double startValue, double endValue, int numberOfIterations, double precision) {
      if (startValue < endValue) {
         throw new IllegalArgumentException("start smaller than end");
      } else if (numberOfIterations <= 0) {
         throw new IllegalArgumentException("number of iterations must be greater than 0");
      } else if (precision <= 0.0) {
         throw new IllegalArgumentException("precision must be greater than 0");
      } else {
         double candidate = 0.0;

         for (int result = 0; candidate < 1.0; candidate += precision) {
            result = calculateIterations(candidate, startValue, endValue);
            if (result >= numberOfIterations) {
               break;
            }
         }

         return Math.min(candidate, 0.99999);
      }
   }

   public static int calculateIterations(double multiplier, double startValue, double endValue) {
      double currentSize = startValue;

      int iterations;
      for (iterations = 0; currentSize > endValue; iterations++) {
         currentSize *= multiplier;
      }

      return iterations;
   }
}
