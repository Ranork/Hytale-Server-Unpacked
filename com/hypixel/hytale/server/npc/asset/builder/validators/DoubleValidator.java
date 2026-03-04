package com.hypixel.hytale.server.npc.asset.builder.validators;

import javax.annotation.Nonnull;

public abstract class DoubleValidator extends Validator {
   public abstract boolean test(double var1);

   public static boolean compare(double value, @Nonnull RelationalOperator predicate, double c) {
      return switch (predicate) {
         case NotEqual -> value != c;
         case Less -> value < c;
         case LessEqual -> value <= c;
         case Greater -> value > c;
         case GreaterEqual -> value >= c;
         case Equal -> value == c;
      };
   }

   public abstract String errorMessage(double var1);

   public abstract String errorMessage(double var1, String var3);
}
