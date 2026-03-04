package com.hypixel.hytale.server.npc.asset.builder.validators;

import java.util.Arrays;
import javax.annotation.Nonnull;

public class BooleanImplicationValidator extends Validator {
   private final String[] antecedentSet;
   private final boolean antecedentState;
   private final String[] consequentSet;
   private final boolean consequentState;
   private final boolean anyAntecedent;

   private BooleanImplicationValidator(String[] antecedentSet, boolean antecedentState, String[] consequentSet, boolean consequentState, boolean anyAntecedent) {
      this.antecedentSet = antecedentSet;
      this.antecedentState = antecedentState;
      this.consequentSet = consequentSet;
      this.consequentState = consequentState;
      this.anyAntecedent = anyAntecedent;
   }

   public boolean test(@Nonnull boolean[] antecedents, @Nonnull boolean[] consequents) {
      boolean antecedent = this.anyAntecedent ? this.anyMatch(antecedents, this.antecedentState) : this.allMatch(antecedents, this.antecedentState);
      return !antecedent || this.allMatch(consequents, this.consequentState);
   }

   private boolean allMatch(@Nonnull boolean[] values, boolean expected) {
      for (boolean value : values) {
         if (value != expected) {
            return false;
         }
      }

      return true;
   }

   private boolean anyMatch(@Nonnull boolean[] values, boolean expected) {
      for (boolean value : values) {
         if (value == expected) {
            return true;
         }
      }

      return false;
   }

   @Nonnull
   public String errorMessage() {
      return String.format(
         "If %s%s%s %s, all members of %s must be %s",
         this.anyAntecedent ? "any of " : "all members of ",
         Arrays.toString((Object[])this.antecedentSet),
         this.anyAntecedent ? " is set to" : " are set to",
         this.antecedentState,
         Arrays.toString((Object[])this.consequentSet),
         this.consequentState
      );
   }

   @Nonnull
   public static BooleanImplicationValidator withAttributes(
      String[] antecedentSet, boolean antecedentState, String[] consequentSet, boolean consequentState, boolean anyAntecedent
   ) {
      return new BooleanImplicationValidator(antecedentSet, antecedentState, consequentSet, consequentState, anyAntecedent);
   }
}
