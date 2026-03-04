package com.hypixel.hytale.server.npc.asset.builder.validators;

import javax.annotation.Nonnull;

public class IntSingleValidator extends IntValidator {
   private static final IntSingleValidator VALIDATOR_GREATER_EQUAL_0 = new IntSingleValidator(RelationalOperator.GreaterEqual, 0);
   private static final IntSingleValidator VALIDATOR_GREATER_0 = new IntSingleValidator(RelationalOperator.Greater, 0);
   private final RelationalOperator relation;
   private final int value;

   private IntSingleValidator(RelationalOperator relation, int value) {
      this.value = value;
      this.relation = relation;
   }

   @Override
   public boolean test(int value) {
      return compare(value, this.relation, this.value);
   }

   @Nonnull
   @Override
   public String errorMessage(int value) {
      return this.errorMessage0(value, "Value");
   }

   @Nonnull
   @Override
   public String errorMessage(int value, String name) {
      return this.errorMessage0(value, "\"" + name + "\"");
   }

   @Nonnull
   private String errorMessage0(int value, String name) {
      return name + " should be " + this.relation.asText() + " " + this.value + " but is " + value;
   }

   public static IntValidator greaterEqual0() {
      return VALIDATOR_GREATER_EQUAL_0;
   }

   public static IntValidator greater0() {
      return VALIDATOR_GREATER_0;
   }
}
