package com.hypixel.hytale.server.npc.asset.builder.validators;

import javax.annotation.Nonnull;

public class IntRangeValidator extends IntValidator {
   private final RelationalOperator relationLower;
   private final int lower;
   private final RelationalOperator relationUpper;
   private final int upper;

   public IntRangeValidator(RelationalOperator relationLower, int lower, RelationalOperator relationUpper, int upper) {
      this.lower = lower;
      this.upper = upper;
      this.relationLower = relationLower;
      this.relationUpper = relationUpper;
   }

   @Override
   public boolean test(int value) {
      return compare(value, this.relationLower, this.lower) && compare(value, this.relationUpper, this.upper);
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

   @Nonnull
   public static IntRangeValidator fromInclToExcl(int lower, int upper) {
      return new IntRangeValidator(RelationalOperator.GreaterEqual, lower, RelationalOperator.Less, upper);
   }

   @Nonnull
   public static IntRangeValidator fromExclToIncl(int lower, int upper) {
      return new IntRangeValidator(RelationalOperator.Greater, lower, RelationalOperator.LessEqual, upper);
   }

   @Nonnull
   public static IntRangeValidator between(int lower, int upper) {
      return new IntRangeValidator(RelationalOperator.GreaterEqual, lower, RelationalOperator.LessEqual, upper);
   }
}
