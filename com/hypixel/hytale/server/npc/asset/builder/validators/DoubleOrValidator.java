package com.hypixel.hytale.server.npc.asset.builder.validators;

import javax.annotation.Nonnull;

public class DoubleOrValidator extends DoubleValidator {
   private static final DoubleOrValidator GREATER_EQUAL_0_OR_MINUS_1 = new DoubleOrValidator(
      RelationalOperator.GreaterEqual, 0.0, RelationalOperator.Equal, -1.0
   );
   private final RelationalOperator relationOne;
   private final RelationalOperator relationTwo;
   private final double valueOne;
   private final double valueTwo;

   private DoubleOrValidator(RelationalOperator relationOne, double valueOne, RelationalOperator relationTwo, double valueTwo) {
      this.relationOne = relationOne;
      this.valueOne = valueOne;
      this.relationTwo = relationTwo;
      this.valueTwo = valueTwo;
   }

   @Override
   public boolean test(double value) {
      return compare(value, this.relationOne, this.valueOne) || compare(value, this.relationTwo, this.valueTwo);
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
         + this.relationOne.asText()
         + " "
         + this.valueOne
         + " or "
         + this.relationTwo.asText()
         + " "
         + this.valueTwo
         + ", but is "
         + value;
   }

   public static DoubleOrValidator greaterEqual0OrMinus1() {
      return GREATER_EQUAL_0_OR_MINUS_1;
   }
}
