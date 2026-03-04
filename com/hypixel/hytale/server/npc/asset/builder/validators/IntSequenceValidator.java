package com.hypixel.hytale.server.npc.asset.builder.validators;

import java.util.Arrays;
import javax.annotation.Nonnull;

public class IntSequenceValidator extends IntArrayValidator {
   private static final IntSequenceValidator VALIDATOR_BETWEEN_01 = new IntSequenceValidator(
      RelationalOperator.GreaterEqual, 0, RelationalOperator.LessEqual, 1, null
   );
   private static final IntSequenceValidator VALIDATOR_BETWEEN_01_WEAKLY_MONOTONIC = new IntSequenceValidator(
      RelationalOperator.GreaterEqual, 0, RelationalOperator.LessEqual, 1, RelationalOperator.LessEqual
   );
   private static final IntSequenceValidator VALIDATOR_BETWEEN_01_MONOTONIC = new IntSequenceValidator(
      RelationalOperator.GreaterEqual, 0, RelationalOperator.LessEqual, 1, RelationalOperator.Less
   );
   private final RelationalOperator relationLower;
   private final int lower;
   private final RelationalOperator relationUpper;
   private final int upper;
   private final RelationalOperator relationSequence;

   private IntSequenceValidator(RelationalOperator relationLower, int lower, RelationalOperator relationUpper, int upper, RelationalOperator relationSequence) {
      this.lower = lower;
      this.upper = upper;
      this.relationLower = relationLower;
      this.relationUpper = relationUpper;
      this.relationSequence = relationSequence;
   }

   public static IntSequenceValidator between01() {
      return VALIDATOR_BETWEEN_01;
   }

   public static IntSequenceValidator between01WeaklyMonotonic() {
      return VALIDATOR_BETWEEN_01_WEAKLY_MONOTONIC;
   }

   public static IntSequenceValidator between01Monotonic() {
      return VALIDATOR_BETWEEN_01_MONOTONIC;
   }

   @Nonnull
   public static IntSequenceValidator between(int lower, int upper) {
      return new IntSequenceValidator(RelationalOperator.GreaterEqual, lower, RelationalOperator.LessEqual, upper, null);
   }

   @Nonnull
   public static IntSequenceValidator betweenWeaklyMonotonic(int lower, int upper) {
      return new IntSequenceValidator(RelationalOperator.GreaterEqual, lower, RelationalOperator.LessEqual, upper, RelationalOperator.LessEqual);
   }

   @Nonnull
   public static IntSequenceValidator betweenMonotonic(int lower, int upper) {
      return new IntSequenceValidator(RelationalOperator.GreaterEqual, lower, RelationalOperator.LessEqual, upper, RelationalOperator.Less);
   }

   @Nonnull
   public static IntSequenceValidator fromExclToIncl(int lower, int upper) {
      return new IntSequenceValidator(RelationalOperator.Greater, lower, RelationalOperator.LessEqual, upper, null);
   }

   @Nonnull
   public static IntSequenceValidator fromExclToInclWeaklyMonotonic(int lower, int upper) {
      return new IntSequenceValidator(RelationalOperator.Greater, lower, RelationalOperator.LessEqual, upper, RelationalOperator.LessEqual);
   }

   @Nonnull
   public static IntSequenceValidator fromExclToInclMonotonic(int lower, int upper) {
      return new IntSequenceValidator(RelationalOperator.Greater, lower, RelationalOperator.LessEqual, upper, RelationalOperator.Less);
   }

   @Override
   public boolean test(@Nonnull int[] values) {
      for (int i = 0; i < values.length; i++) {
         int value = values[i];
         if (!IntValidator.compare(value, this.relationLower, this.lower) && IntValidator.compare(value, this.relationUpper, this.upper)) {
            return false;
         }

         if (i > 0 && this.relationSequence != null && !IntValidator.compare(values[i - 1], this.relationSequence, value)) {
            return false;
         }
      }

      return true;
   }

   @Nonnull
   @Override
   public String errorMessage(int[] value) {
      return this.errorMessage0(value, "Array");
   }

   @Nonnull
   @Override
   public String errorMessage(int[] value, String name) {
      return this.errorMessage0(value, "\"" + name + "\"");
   }

   @Nonnull
   private String errorMessage0(int[] value, String name) {
      return name
         + (this.relationLower == null ? "" : " values should be " + this.relationLower.asText() + " " + this.lower + " and ")
         + (this.relationUpper == null ? "" : " values should be " + this.relationUpper.asText() + " " + this.upper + " and ")
         + (this.relationSequence == null ? "" : " succeeding values should be " + this.relationSequence.asText() + " preceding values ")
         + " but is "
         + Arrays.toString(value);
   }
}
