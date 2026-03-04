package com.hypixel.hytale.builtin.hytalegenerator.delimiters;

public class RangeDouble {
   private final double minInclusive;
   private final double maxExclusive;

   public RangeDouble(double minInclusive, double maxExclusive) {
      this.minInclusive = minInclusive;
      this.maxExclusive = maxExclusive;
   }

   public boolean contains(double value) {
      return this.minInclusive <= value && this.maxExclusive > value;
   }

   public double min() {
      return this.minInclusive;
   }

   public double max() {
      return this.maxExclusive;
   }
}
