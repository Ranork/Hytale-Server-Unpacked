package com.hypixel.hytale.server.npc.asset.builder.validators;

import java.util.Arrays;
import javax.annotation.Nonnull;

public class DoubleSequenceValidator extends DoubleArrayValidator {
   private static final DoubleSequenceValidator VALIDATOR_BETWEEN_01 = new DoubleSequenceValidator(
      RelationalOperator.GreaterEqual, 0.0, RelationalOperator.LessEqual, 1.0, null
   );
   private static final DoubleSequenceValidator VALIDATOR_BETWEEN_01_WEAKLY_MONOTONIC = new DoubleSequenceValidator(
      RelationalOperator.GreaterEqual, 0.0, RelationalOperator.LessEqual, 1.0, RelationalOperator.LessEqual
   );
   private static final DoubleSequenceValidator VALIDATOR_BETWEEN_01_MONOTONIC = new DoubleSequenceValidator(
      RelationalOperator.GreaterEqual, 0.0, RelationalOperator.LessEqual, 1.0, RelationalOperator.Less
   );
   private static final DoubleSequenceValidator VALIDATOR_WEAKLY_MONOTONIC = new DoubleSequenceValidator(
      RelationalOperator.GreaterEqual, -Double.MAX_VALUE, RelationalOperator.LessEqual, Double.MAX_VALUE, RelationalOperator.LessEqual
   );
   private static final DoubleSequenceValidator VALIDATOR_MONOTONIC = new DoubleSequenceValidator(
      RelationalOperator.GreaterEqual, -Double.MAX_VALUE, RelationalOperator.LessEqual, Double.MAX_VALUE, RelationalOperator.Less
   );
   private final RelationalOperator relationLower;
   private final double lower;
   private final RelationalOperator relationUpper;
   private final double upper;
   private final RelationalOperator relationSequence;

   private DoubleSequenceValidator(
      RelationalOperator relationLower, double lower, RelationalOperator relationUpper, double upper, RelationalOperator relationSequence
   ) {
      this.lower = lower;
      this.upper = upper;
      this.relationLower = relationLower;
      this.relationUpper = relationUpper;
      this.relationSequence = relationSequence;
   }

   public static DoubleSequenceValidator between01() {
      return VALIDATOR_BETWEEN_01;
   }

   public static DoubleSequenceValidator between01WeaklyMonotonic() {
      return VALIDATOR_BETWEEN_01_WEAKLY_MONOTONIC;
   }

   public static DoubleSequenceValidator between01Monotonic() {
      return VALIDATOR_BETWEEN_01_MONOTONIC;
   }

   @Nonnull
   public static DoubleSequenceValidator between(double lower, double upper) {
      return new DoubleSequenceValidator(RelationalOperator.GreaterEqual, lower, RelationalOperator.LessEqual, upper, null);
   }

   @Nonnull
   public static DoubleSequenceValidator betweenWeaklyMonotonic(double lower, double upper) {
      return new DoubleSequenceValidator(RelationalOperator.GreaterEqual, lower, RelationalOperator.LessEqual, upper, RelationalOperator.LessEqual);
   }

   @Nonnull
   public static DoubleSequenceValidator betweenMonotonic(double lower, double upper) {
      return new DoubleSequenceValidator(RelationalOperator.GreaterEqual, lower, RelationalOperator.LessEqual, upper, RelationalOperator.Less);
   }

   @Nonnull
   public static DoubleSequenceValidator fromExclToIncl(double lower, double upper) {
      return new DoubleSequenceValidator(RelationalOperator.Greater, lower, RelationalOperator.LessEqual, upper, null);
   }

   @Nonnull
   public static DoubleSequenceValidator fromExclToInclWeaklyMonotonic(double lower, double upper) {
      return new DoubleSequenceValidator(RelationalOperator.Greater, lower, RelationalOperator.LessEqual, upper, RelationalOperator.LessEqual);
   }

   @Nonnull
   public static DoubleSequenceValidator fromExclToInclMonotonic(double lower, double upper) {
      return new DoubleSequenceValidator(RelationalOperator.Greater, lower, RelationalOperator.LessEqual, upper, RelationalOperator.Less);
   }

   public static DoubleSequenceValidator monotonic() {
      return VALIDATOR_MONOTONIC;
   }

   public static DoubleSequenceValidator weaklyMonotonic() {
      return VALIDATOR_WEAKLY_MONOTONIC;
   }

   @Override
   public boolean test(@Nonnull double[] values) {
      for (int i = 0; i < values.length; i++) {
         double value = values[i];
         if (!DoubleValidator.compare(value, this.relationLower, this.lower) && DoubleValidator.compare(value, this.relationUpper, this.upper)) {
            return false;
         }

         if (i > 0 && this.relationSequence != null && !DoubleValidator.compare(values[i - 1], this.relationSequence, value)) {
            return false;
         }
      }

      return true;
   }

   @Nonnull
   @Override
   public String errorMessage(double[] value) {
      return this.errorMessage0(value, "Array");
   }

   @Nonnull
   @Override
   public String errorMessage(double[] value, String name) {
      return this.errorMessage0(value, "\"" + name + "\"");
   }

   @Nonnull
   private String errorMessage0(double[] value, String name) {
      return name
         + (this.relationLower == null ? "" : " values should be " + this.relationLower.asText() + " " + this.lower + " and ")
         + (this.relationUpper == null ? "" : " values should be " + this.relationUpper.asText() + " " + this.upper + " and ")
         + (this.relationSequence == null ? "" : " succeeding values should be " + this.relationSequence.asText() + " preceding values ")
         + " but is "
         + Arrays.toString(value);
   }
}
