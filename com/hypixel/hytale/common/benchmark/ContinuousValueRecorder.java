package com.hypixel.hytale.common.benchmark;

public class ContinuousValueRecorder {
   protected double minValue = Double.MAX_VALUE;
   protected double maxValue = -Double.MAX_VALUE;
   protected double sumValues = 0.0;
   protected long count = 0L;

   public void reset() {
      this.minValue = Double.MAX_VALUE;
      this.maxValue = -Double.MAX_VALUE;
      this.sumValues = 0.0;
      this.count = 0L;
   }

   public double getMinValue(double def) {
      return this.count > 0L ? this.minValue : def;
   }

   public double getMinValue() {
      return this.getMinValue(0.0);
   }

   public double getMaxValue(double def) {
      return this.count > 0L ? this.maxValue : def;
   }

   public double getMaxValue() {
      return this.getMaxValue(0.0);
   }

   public long getCount() {
      return this.count;
   }

   public double getAverage(double def) {
      return this.count > 0L ? this.sumValues / this.count : def;
   }

   public double getAverage() {
      return this.getAverage(0.0);
   }

   public double record(double value) {
      if (this.minValue > value) {
         this.minValue = value;
      }

      if (this.maxValue < value) {
         this.maxValue = value;
      }

      this.count++;
      this.sumValues += value;
      return value;
   }
}
