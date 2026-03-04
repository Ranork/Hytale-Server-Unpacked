package com.hypixel.hytale.server.worldgen.util.condition;

import com.hypixel.hytale.math.util.HashUtil;
import com.hypixel.hytale.procedurallib.condition.ICoordinateCondition;
import javax.annotation.Nonnull;

public class RandomCoordinateCondition implements ICoordinateCondition {
   private final double chance;

   public RandomCoordinateCondition(double chance) {
      this.chance = chance;
   }

   @Override
   public boolean eval(int seed, int x, int y) {
      return HashUtil.random(seed, x, y) <= this.chance;
   }

   @Override
   public boolean eval(int seed, int x, int y, int z) {
      return HashUtil.random(seed, x, y, z) <= this.chance;
   }

   @Nonnull
   @Override
   public String toString() {
      return "RandomCoordinateCondition{chance=" + this.chance + "}";
   }
}
