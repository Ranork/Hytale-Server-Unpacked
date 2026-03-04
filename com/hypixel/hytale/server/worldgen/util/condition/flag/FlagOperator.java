package com.hypixel.hytale.server.worldgen.util.condition.flag;

import java.util.function.IntBinaryOperator;

public enum FlagOperator implements IntBinaryOperator {
   And {
      @Override
      public int apply(int output, int flags) {
         return output & flags;
      }
   },
   Or {
      @Override
      public int apply(int output, int flags) {
         return output | flags;
      }
   },
   Xor {
      @Override
      public int apply(int output, int flags) {
         return output ^ flags;
      }
   };

   public abstract int apply(int var1, int var2);

   @Override
   public int applyAsInt(int left, int right) {
      return this.apply(left, right);
   }
}
