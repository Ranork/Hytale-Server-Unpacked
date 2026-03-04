package com.hypixel.hytale.procedurallib.condition;

import com.hypixel.hytale.math.util.HashUtil;
import com.hypixel.hytale.procedurallib.logic.GeneralNoise;
import javax.annotation.Nonnull;

public class HeightThresholdCoordinateCondition implements ICoordinateCondition {
   private final IHeightThresholdInterpreter interpreter;

   public HeightThresholdCoordinateCondition(IHeightThresholdInterpreter interpreter) {
      this.interpreter = interpreter;
   }

   @Override
   public boolean eval(int seed, int x, int y) {
      throw new UnsupportedOperationException("This needs a height to operate.");
   }

   @Override
   public boolean eval(int seed, int x, int y, int z) {
      return this.interpreter.getThreshold(seed, x, z, y)
         >= HashUtil.random(seed, GeneralNoise.fastFloor(x), GeneralNoise.fastFloor(y), GeneralNoise.fastFloor(z));
   }

   @Nonnull
   @Override
   public String toString() {
      return "HeightThresholdCoordinateCondition{interpreter=" + this.interpreter + "}";
   }
}
