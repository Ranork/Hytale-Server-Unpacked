package com.hypixel.hytale.server.npc.asset.builder.validators;

import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.TemporalAmount;
import java.util.Arrays;
import javax.annotation.Nonnull;

public class TemporalSequenceValidator extends TemporalArrayValidator {
   private final RelationalOperator relationLower;
   private final TemporalAmount lower;
   private final RelationalOperator relationUpper;
   private final TemporalAmount upper;
   private final RelationalOperator relationSequence;

   private TemporalSequenceValidator(
      RelationalOperator relationLower, TemporalAmount lower, RelationalOperator relationUpper, TemporalAmount upper, RelationalOperator relationSequence
   ) {
      this.lower = lower;
      this.upper = upper;
      this.relationLower = relationLower;
      this.relationUpper = relationUpper;
      this.relationSequence = relationSequence;
   }

   @Nonnull
   public static TemporalSequenceValidator betweenMonotonic(TemporalAmount lower, TemporalAmount upper) {
      return new TemporalSequenceValidator(RelationalOperator.GreaterEqual, lower, RelationalOperator.LessEqual, upper, RelationalOperator.Less);
   }

   @Nonnull
   public static TemporalSequenceValidator betweenWeaklyMonotonic(TemporalAmount lower, TemporalAmount upper) {
      return new TemporalSequenceValidator(RelationalOperator.GreaterEqual, lower, RelationalOperator.LessEqual, upper, RelationalOperator.LessEqual);
   }

   public static boolean compare(@Nonnull LocalDateTime value, @Nonnull RelationalOperator op, LocalDateTime c) {
      return switch (op) {
         case NotEqual -> !value.equals(c);
         case Less -> value.isBefore(c);
         case LessEqual -> !value.isAfter(c);
         case Greater -> value.isAfter(c);
         case GreaterEqual -> !value.isBefore(c);
         case Equal -> value.equals(c);
      };
   }

   @Override
   public boolean test(@Nonnull TemporalAmount[] values) {
      LocalDateTime zeroDate = LocalDateTime.ofInstant(WorldTimeResource.ZERO_YEAR, WorldTimeResource.ZONE_OFFSET);
      LocalDateTime min = zeroDate.plus(this.lower);
      LocalDateTime max = zeroDate.plus(this.upper);
      boolean expectPeriod = values[0] instanceof Period;

      for (int i = 0; i < values.length; i++) {
         TemporalAmount value = values[i];
         if (value instanceof Period && !expectPeriod) {
            return false;
         }

         if (value instanceof Duration && expectPeriod) {
            return false;
         }

         LocalDateTime dateValue = zeroDate.plus(values[i]);
         if (!compare(dateValue, this.relationLower, min) && compare(dateValue, this.relationUpper, max)) {
            return false;
         }

         if (i > 0 && this.relationSequence != null) {
            LocalDateTime previousValue = zeroDate.plus(values[i - 1]);
            if (!compare(previousValue, this.relationSequence, dateValue)) {
               return false;
            }
         }
      }

      return true;
   }

   @Nonnull
   @Override
   public String errorMessage(String name, TemporalAmount[] value) {
      return name
         + (this.relationLower == null ? "" : " values should be " + this.relationLower.asText() + " " + this.lower + " and")
         + (this.relationUpper == null ? "" : " values should be " + this.relationUpper.asText() + " " + this.upper + " and")
         + (this.relationSequence == null ? "" : " succeeding values should be " + this.relationSequence.asText() + " preceding values and")
         + " values must all either be periods or durations but is "
         + Arrays.toString((Object[])value);
   }
}
