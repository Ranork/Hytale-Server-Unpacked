package com.hypixel.hytale.server.worldgen.util.condition.flag;

import com.hypixel.hytale.procedurallib.condition.IIntCondition;
import java.util.Arrays;
import java.util.function.IntBinaryOperator;
import javax.annotation.Nonnull;

public class CompositeInt2Flags implements Int2FlagsCondition {
   private final CompositeInt2Flags.FlagCondition[] flags;
   private final int defaultResult;

   public CompositeInt2Flags(int defaultResult, CompositeInt2Flags.FlagCondition[] flags) {
      this.flags = flags;
      this.defaultResult = defaultResult;
   }

   @Override
   public int eval(int input) {
      int output = this.defaultResult;

      for (CompositeInt2Flags.FlagCondition flag : this.flags) {
         output = flag.eval(input, output);
      }

      return output;
   }

   @Nonnull
   @Override
   public String toString() {
      return "CompositeInt2Flags{flags=" + Arrays.toString((Object[])this.flags) + ", defaultResult=" + this.defaultResult + "}";
   }

   public static class FlagCondition implements IntBinaryOperator {
      private final IIntCondition condition;
      private final FlagOperator operator;
      private final int flags;

      public FlagCondition(IIntCondition condition, FlagOperator operator, int flags) {
         this.condition = condition;
         this.operator = operator;
         this.flags = flags;
      }

      public int eval(int input, int output) {
         if (this.condition.eval(input)) {
            output = this.operator.apply(output, this.flags);
         }

         return output;
      }

      @Override
      public int applyAsInt(int input, int output) {
         return this.eval(input, output);
      }

      @Nonnull
      @Override
      public String toString() {
         return "FlagOperator{condition=" + this.condition + ", operator=" + this.operator + ", flags=" + this.flags + "}";
      }
   }
}
