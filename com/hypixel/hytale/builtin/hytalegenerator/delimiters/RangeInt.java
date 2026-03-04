package com.hypixel.hytale.builtin.hytalegenerator.delimiters;

public class RangeInt {
   private final int minInclusive;
   private final int maxExclusive;

   public RangeInt(int minInclusive, int maxExclusive) {
      this.minInclusive = minInclusive;
      this.maxExclusive = maxExclusive;
   }

   public boolean contains(int value) {
      return this.minInclusive <= value && this.maxExclusive > value;
   }

   public int getMinInclusive() {
      return this.minInclusive;
   }

   public int getMaxExclusive() {
      return this.maxExclusive;
   }
}
