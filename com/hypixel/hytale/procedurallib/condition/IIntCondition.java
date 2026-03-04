package com.hypixel.hytale.procedurallib.condition;

import com.hypixel.hytale.procedurallib.util.IntToIntFunction;
import javax.annotation.Nonnull;

public interface IIntCondition {
   boolean eval(int var1);

   default boolean eval(int seed, @Nonnull IntToIntFunction seedFunction) {
      return this.eval(seedFunction.applyAsInt(seed));
   }
}
