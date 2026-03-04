package com.hypixel.hytale.math.range;

import com.hypixel.hytale.math.codec.IntRangeArrayCodec;
import com.hypixel.hytale.math.util.MathUtil;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class IntRange {
   public static final IntRangeArrayCodec CODEC = new IntRangeArrayCodec();
   private int inclusiveMin;
   private int inclusiveMax;
   private int range;

   public IntRange() {
      this(0, 0);
   }

   public IntRange(int inclusiveMin, int inclusiveMax) {
      this.inclusiveMin = inclusiveMin;
      this.inclusiveMax = inclusiveMax;
      this.range = inclusiveMax - inclusiveMin + 1;
   }

   public int getInclusiveMin() {
      return this.inclusiveMin;
   }

   public int getInclusiveMax() {
      return this.inclusiveMax;
   }

   public void setInclusiveMin(int inclusiveMin) {
      this.inclusiveMin = inclusiveMin;
      this.range = this.inclusiveMax - inclusiveMin + 1;
   }

   public void setInclusiveMax(int inclusiveMax) {
      this.inclusiveMax = inclusiveMax;
      this.range = inclusiveMax - this.inclusiveMin + 1;
   }

   public int getInt(float factor) {
      int value = this.inclusiveMin + MathUtil.fastFloor(this.range * factor);
      return Integer.min(this.inclusiveMax, value);
   }

   public int getInt(double factor) {
      int value = this.inclusiveMin + MathUtil.floor(this.range * factor);
      return Integer.min(this.inclusiveMax, value);
   }

   public boolean includes(int value) {
      return value >= this.inclusiveMin && value <= this.inclusiveMax;
   }

   @Override
   public boolean equals(@Nullable Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         IntRange intRange = (IntRange)o;
         return this.inclusiveMin != intRange.inclusiveMin ? false : this.inclusiveMax == intRange.inclusiveMax;
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      int result = this.inclusiveMin;
      return 31 * result + this.inclusiveMax;
   }

   @Nonnull
   @Override
   public String toString() {
      return "IntRange{inclusiveMin=" + this.inclusiveMin + ", inclusiveMax=" + this.inclusiveMax + "}";
   }
}
