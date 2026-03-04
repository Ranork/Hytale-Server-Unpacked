package com.hypixel.hytale.procedurallib.condition;

import javax.annotation.Nonnull;

public class DefaultDoubleThresholdCondition implements IDoubleThreshold {
   public static final DefaultDoubleThresholdCondition DEFAULT_TRUE = new DefaultDoubleThresholdCondition(true);
   public static final DefaultDoubleThresholdCondition DEFAULT_FALSE = new DefaultDoubleThresholdCondition(false);
   protected final boolean result;

   public DefaultDoubleThresholdCondition(boolean result) {
      this.result = result;
   }

   public boolean isResult() {
      return this.result;
   }

   @Override
   public boolean eval(double d) {
      return this.result;
   }

   @Override
   public boolean eval(double d, double factor) {
      return this.result;
   }

   @Nonnull
   @Override
   public String toString() {
      return "DefaultDoubleThresholdCondition{result=" + this.result + "}";
   }
}
