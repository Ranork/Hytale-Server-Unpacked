package com.hypixel.hytale.procedurallib.condition;

import javax.annotation.Nonnull;

public class DoubleThresholdCondition implements IDoubleCondition {
   protected final IDoubleThreshold threshold;

   public DoubleThresholdCondition(IDoubleThreshold threshold) {
      this.threshold = threshold;
   }

   @Override
   public boolean eval(double value) {
      return this.threshold.eval(value);
   }

   @Nonnull
   @Override
   public String toString() {
      return "DoubleThresholdCondition{threshold=" + this.threshold + "}";
   }
}
