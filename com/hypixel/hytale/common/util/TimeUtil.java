package com.hypixel.hytale.common.util;

import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import javax.annotation.Nonnull;

public class TimeUtil {
   public static int compareDifference(@Nonnull Instant from, @Nonnull Instant to, @Nonnull Duration duration) {
      if (from.equals(Instant.MIN) && !to.equals(Instant.MIN) && !duration.isZero()) {
         return 1;
      } else {
         try {
            long diff = from.until(to, ChronoUnit.NANOS);
            return Long.compare(diff, duration.toNanos());
         } catch (ArithmeticException | DateTimeException var13) {
            long seconds = from.until(to, ChronoUnit.SECONDS);

            long nanos;
            try {
               nanos = to.getLong(ChronoField.NANO_OF_SECOND) - from.getLong(ChronoField.NANO_OF_SECOND);
               if (seconds > 0L && nanos < 0L) {
                  seconds++;
               } else if (seconds < 0L && nanos > 0L) {
                  seconds--;
               }
            } catch (DateTimeException var12) {
               nanos = 0L;
            }

            long durSeconds = duration.getSeconds();
            int durNanos = duration.getNano();
            int res = Long.compare(seconds, durSeconds);
            if (res == 0) {
               res = Integer.compare((int)nanos, durNanos);
            }

            return res;
         }
      }
   }
}
