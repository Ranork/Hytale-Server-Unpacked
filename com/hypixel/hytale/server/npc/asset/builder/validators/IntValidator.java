package com.hypixel.hytale.server.npc.asset.builder.validators;

import javax.annotation.Nonnull;

public abstract class IntValidator extends Validator {
   public abstract boolean test(int var1);

   public static boolean compare(int value, @Nonnull RelationalOperator op, int c) {
      return switch (op) {
         case NotEqual -> value != c;
         case Less -> value < c;
         case LessEqual -> value <= c;
         case Greater -> value > c;
         case GreaterEqual -> value >= c;
         case Equal -> value == c;
      };
   }

   public abstract String errorMessage(int var1);

   public abstract String errorMessage(int var1, String var2);
}
