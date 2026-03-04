package com.hypixel.hytale.procedurallib.condition;

import javax.annotation.Nonnull;

public class ConstantIntCondition implements IIntCondition {
   public static final ConstantIntCondition DEFAULT_TRUE = new ConstantIntCondition(true);
   public static final ConstantIntCondition DEFAULT_FALSE = new ConstantIntCondition(false);
   protected final boolean result;

   public ConstantIntCondition(boolean result) {
      this.result = result;
   }

   public boolean getResult() {
      return this.result;
   }

   @Override
   public boolean eval(int value) {
      return this.result;
   }

   @Nonnull
   @Override
   public String toString() {
      return "ConstantIntCondition{result=" + this.result + "}";
   }
}
