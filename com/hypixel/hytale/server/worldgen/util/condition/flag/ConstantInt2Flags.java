package com.hypixel.hytale.server.worldgen.util.condition.flag;

import javax.annotation.Nonnull;

public class ConstantInt2Flags implements Int2FlagsCondition {
   private final int result;

   public ConstantInt2Flags(int result) {
      this.result = result;
   }

   @Override
   public int eval(int input) {
      return this.result;
   }

   @Nonnull
   @Override
   public String toString() {
      return "ConstantInt2Flags{result=" + this.result + "}";
   }
}
