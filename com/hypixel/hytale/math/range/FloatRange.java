package com.hypixel.hytale.math.range;

import com.hypixel.hytale.math.codec.FloatRangeArrayCodec;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FloatRange {
   public static final FloatRangeArrayCodec CODEC = new FloatRangeArrayCodec();
   private float inclusiveMin;
   private float inclusiveMax;
   private float range;

   public FloatRange() {
      this(0.0F, 0.0F);
   }

   public FloatRange(float inclusiveMin, float inclusiveMax) {
      this.inclusiveMin = inclusiveMin;
      this.inclusiveMax = inclusiveMax;
      this.range = inclusiveMax - inclusiveMin + 1.0F;
   }

   public float getInclusiveMin() {
      return this.inclusiveMin;
   }

   public float getInclusiveMax() {
      return this.inclusiveMax;
   }

   public void setInclusiveMin(float inclusiveMin) {
      this.inclusiveMin = inclusiveMin;
      this.range = this.inclusiveMax - inclusiveMin + 1.0F;
   }

   public void setInclusiveMax(float inclusiveMax) {
      this.inclusiveMax = inclusiveMax;
      this.range = inclusiveMax - this.inclusiveMin + 1.0F;
   }

   public float getFloat(float factor) {
      float value = this.inclusiveMin + this.range * factor;
      return Float.min(this.inclusiveMax, value);
   }

   public float getFloat(double factor) {
      float value = this.inclusiveMin + (float)(this.range * factor);
      return Float.min(this.inclusiveMax, value);
   }

   public boolean includes(float value) {
      return value >= this.inclusiveMin && value <= this.inclusiveMax;
   }

   @Override
   public boolean equals(@Nullable Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         FloatRange that = (FloatRange)o;
         if (Float.compare(that.inclusiveMin, this.inclusiveMin) != 0) {
            return false;
         } else {
            return Float.compare(that.inclusiveMax, this.inclusiveMax) != 0 ? false : Float.compare(that.range, this.range) == 0;
         }
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      int result = this.inclusiveMin != 0.0F ? Float.floatToIntBits(this.inclusiveMin) : 0;
      result = 31 * result + (this.inclusiveMax != 0.0F ? Float.floatToIntBits(this.inclusiveMax) : 0);
      return 31 * result + (this.range != 0.0F ? Float.floatToIntBits(this.range) : 0);
   }

   @Nonnull
   @Override
   public String toString() {
      return "FloatRange{inclusiveMin=" + this.inclusiveMin + ", inclusiveMax=" + this.inclusiveMax + "}";
   }
}
