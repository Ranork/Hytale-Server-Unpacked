package com.hypixel.hytale.procedurallib.condition;

import javax.annotation.Nonnull;

public class DefaultCoordinateCondition implements ICoordinateCondition {
   public static final DefaultCoordinateCondition DEFAULT_TRUE = new DefaultCoordinateCondition(true);
   public static final DefaultCoordinateCondition DEFAULT_FALSE = new DefaultCoordinateCondition(false);
   protected final boolean result;

   private DefaultCoordinateCondition(boolean result) {
      this.result = result;
   }

   public boolean getResult() {
      return this.result;
   }

   @Override
   public boolean eval(int seed, int x, int y) {
      return this.result;
   }

   @Override
   public boolean eval(int seed, int x, int y, int z) {
      return this.result;
   }

   @Nonnull
   @Override
   public String toString() {
      return "DefaultCoordinateCondition{result=" + this.result + "}";
   }
}
