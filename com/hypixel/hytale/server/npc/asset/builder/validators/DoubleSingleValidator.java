package com.hypixel.hytale.server.npc.asset.builder.validators;

import javax.annotation.Nonnull;

public class DoubleSingleValidator extends DoubleValidator {
   private static final DoubleSingleValidator VALIDATOR_GREATER_0 = new DoubleSingleValidator(RelationalOperator.Greater, 0.0);
   private static final DoubleSingleValidator VALIDATOR_GREATER_EQUAL_0 = new DoubleSingleValidator(RelationalOperator.GreaterEqual, 0.0);
   private final RelationalOperator relation;
   private final double value;

   private DoubleSingleValidator(RelationalOperator relation, double value) {
      this.value = value;
      this.relation = relation;
   }

   public static DoubleSingleValidator greater0() {
      return VALIDATOR_GREATER_0;
   }

   @Nonnull
   public static DoubleSingleValidator greater(double threshold) {
      return new DoubleSingleValidator(RelationalOperator.Greater, threshold);
   }

   public static DoubleSingleValidator greaterEqual0() {
      return VALIDATOR_GREATER_EQUAL_0;
   }

   @Override
   public boolean test(double value) {
      return compare(value, this.relation, this.value);
   }

   @Nonnull
   @Override
   public String errorMessage(double value) {
      return this.errorMessage0(value, "Value");
   }

   @Nonnull
   @Override
   public String errorMessage(double value, String name) {
      return this.errorMessage0(value, "\"" + name + "\"");
   }

   @Nonnull
   private String errorMessage0(double value, String name) {
      return name + " should be " + this.relation.asText() + " " + this.value + " but is " + value;
   }
}
