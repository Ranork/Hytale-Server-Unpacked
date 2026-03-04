package com.hypixel.hytale.procedurallib.condition;

import javax.annotation.Nonnull;

public class DefaultDoubleCondition implements IDoubleCondition {
   public static final DefaultDoubleCondition DEFAULT_TRUE = new DefaultDoubleCondition(true);
   public static final DefaultDoubleCondition DEFAULT_FALSE = new DefaultDoubleCondition(false);
   protected final boolean result;

   public DefaultDoubleCondition(boolean result) {
      this.result = result;
   }

   public boolean getResult() {
      return this.result;
   }

   @Override
   public boolean eval(double value) {
      return this.result;
   }

   @Nonnull
   @Override
   public String toString() {
      return "DefaultDoubleCondition{result=" + this.result + "}";
   }
}
