package com.hypixel.hytale.procedurallib.condition;

import java.util.function.IntToDoubleFunction;
import javax.annotation.Nonnull;

public interface IDoubleCondition {
   boolean eval(double var1);

   default boolean eval(int seed, @Nonnull IntToDoubleFunction seedFunction) {
      return this.eval(seedFunction.applyAsDouble(seed));
   }
}
