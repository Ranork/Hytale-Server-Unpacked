package com.hypixel.hytale.server.npc.asset.builder.validators;

import javax.annotation.Nonnull;

public class DoubleRangeValidator extends DoubleValidator {
   private static final DoubleRangeValidator VALIDATOR_BETWEEN_01 = new DoubleRangeValidator(
      RelationalOperator.GreaterEqual, 0.0, RelationalOperator.LessEqual, 1.0
   );
   private final RelationalOperator relationLower;
   private final double lower;
   private final RelationalOperator relationUpper;
   private final double upper;

   private DoubleRangeValidator(RelationalOperator relationLower, double lower, RelationalOperator relationUpper, double upper) {
      this.lower = lower;
      this.upper = upper;
      this.relationLower = relationLower;
      this.relationUpper = relationUpper;
   }

   public static DoubleRangeValidator between01() {
      return VALIDATOR_BETWEEN_01;
   }

   @Nonnull
   public static DoubleRangeValidator between(double lower, double upper) {
      return new DoubleRangeValidator(RelationalOperator.GreaterEqual, lower, RelationalOperator.LessEqual, upper);
   }

   @Nonnull
   public static DoubleRangeValidator fromExclToIncl(double lower, double upper) {
      return new DoubleRangeValidator(RelationalOperator.Greater, lower, RelationalOperator.LessEqual, upper);
   }

   @Nonnull
   public static DoubleRangeValidator fromExclToExcl(double lower, double upper) {
      return new DoubleRangeValidator(RelationalOperator.Greater, lower, RelationalOperator.Less, upper);
   }

   @Override
   public boolean test(double value) {
      return compare(value, this.relationLower, this.lower) && compare(value, this.relationUpper, this.upper);
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
      return name
         + " should be "
         + this.relationLower.asText()
         + " "
         + this.lower
         + " and "
         + this.relationUpper.asText()
         + " "
         + this.upper
         + " but is "
         + value;
   }
}
