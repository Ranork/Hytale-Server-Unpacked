package com.hypixel.hytale.builtin.hytalegenerator.rangemaps;

import javax.annotation.Nonnull;

public class DoubleRange {
   private double min;
   private double max;
   private boolean inclusiveMin;
   private boolean inclusiveMax;

   public DoubleRange(double min, boolean inclusiveMin, double max, boolean inclusiveMax) {
      if (min > max) {
         throw new IllegalArgumentException("min greater than max");
      } else {
         this.min = min;
         this.max = max;
         this.inclusiveMin = inclusiveMin;
         this.inclusiveMax = inclusiveMax;
      }
   }

   public double getMin() {
      return this.min;
   }

   public boolean isInclusiveMin() {
      return this.inclusiveMin;
   }

   public double getMax() {
      return this.max;
   }

   public boolean isInclusiveMax() {
      return this.inclusiveMax;
   }

   public boolean includes(double v) {
      return (this.inclusiveMin ? v >= this.min : v > this.min) && (this.inclusiveMax ? v <= this.max : v < this.max);
   }

   @Nonnull
   public static DoubleRange inclusive(double min, double max) {
      return new DoubleRange(min, true, max, true);
   }

   @Nonnull
   public static DoubleRange exclusive(double min, double max) {
      return new DoubleRange(min, false, max, false);
   }
}
