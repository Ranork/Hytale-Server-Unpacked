package com.hypixel.hytale.math;

import javax.annotation.Nonnull;

public class Range {
   private float min;
   private float max;

   public Range() {
   }

   public Range(float min, float max) {
      this.min = min;
      this.max = max;
   }

   public float getMin() {
      return this.min;
   }

   public float getMax() {
      return this.max;
   }

   @Nonnull
   @Override
   public String toString() {
      return "Range{min=" + this.min + ", max='" + this.max + "'}";
   }
}
