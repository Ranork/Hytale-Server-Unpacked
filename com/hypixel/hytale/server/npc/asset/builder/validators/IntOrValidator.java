package com.hypixel.hytale.server.npc.asset.builder.validators;

import javax.annotation.Nonnull;

public class IntOrValidator extends IntValidator {
   private final RelationalOperator relationOne;
   private final RelationalOperator relationTwo;
   private final int valueOne;
   private final int valueTwo;

   private IntOrValidator(RelationalOperator relationOne, int valueOne, RelationalOperator relationTwo, int valueTwo) {
      this.relationOne = relationOne;
      this.valueOne = valueOne;
      this.relationTwo = relationTwo;
      this.valueTwo = valueTwo;
   }

   @Override
   public boolean test(int value) {
      return compare(value, this.relationOne, this.valueOne) || compare(value, this.relationTwo, this.valueTwo);
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

   @Nonnull
   public static IntOrValidator greater0OrMinus1() {
      return new IntOrValidator(RelationalOperator.Greater, 0, RelationalOperator.Equal, -1);
   }
}
