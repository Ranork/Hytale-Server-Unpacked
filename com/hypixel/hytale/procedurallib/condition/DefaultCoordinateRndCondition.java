package com.hypixel.hytale.procedurallib.condition;

import java.util.Random;
import javax.annotation.Nonnull;

public class DefaultCoordinateRndCondition implements ICoordinateRndCondition {
   public static final DefaultCoordinateRndCondition DEFAULT_TRUE = new DefaultCoordinateRndCondition(true);
   public static final DefaultCoordinateRndCondition DEFAULT_FALSE = new DefaultCoordinateRndCondition(false);
   protected final boolean result;

   public DefaultCoordinateRndCondition(boolean result) {
      this.result = result;
   }

   public boolean getResult() {
      return this.result;
   }

   @Override
   public boolean eval(int seed, int x, int z, int y, Random random) {
      return this.result;
   }

   @Nonnull
   @Override
   public String toString() {
      return "DefaultCoordinateRndCondition{result=" + this.result + "}";
   }
}
