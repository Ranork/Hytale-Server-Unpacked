package com.hypixel.hytale.server.worldgen.util.condition.flag;

import java.util.function.IntUnaryOperator;

@FunctionalInterface
public interface Int2FlagsCondition extends IntUnaryOperator {
   int eval(int var1);

   @Override
   default int applyAsInt(int operand) {
      return this.eval(operand);
   }
}
