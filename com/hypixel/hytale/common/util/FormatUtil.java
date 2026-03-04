package com.hypixel.hytale.common.util;

import com.hypixel.hytale.metrics.metric.Metric;
import java.util.EnumMap;
import java.util.Formatter;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.DoubleUnaryOperator;
import javax.annotation.Nonnull;

public class FormatUtil {
   private static final String[] NUMBER_SUFFIXES = new String[]{"th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th"};
   private static final EnumMap<TimeUnit, String> timeUnitToShortString = new EnumMap<TimeUnit, String>(TimeUnit.class) {
      {
         this.put(TimeUnit.DAYS, "days");
         this.put(TimeUnit.HOURS, "hours");
         this.put(TimeUnit.MINUTES, "min");
         this.put(TimeUnit.SECONDS, "s");
         this.put(TimeUnit.MILLISECONDS, "ms");
         this.put(TimeUnit.MICROSECONDS, "us");
         this.put(TimeUnit.NANOSECONDS, "ns");
      }
   };
   public static final long DAY_AS_NANOS = TimeUnit.DAYS.toNanos(1L);
   public static final long HOUR_AS_NANOS = TimeUnit.HOURS.toNanos(1L);
   public static final long MINUTE_AS_NANOS = TimeUnit.MINUTES.toNanos(1L);
   public static final long SECOND_AS_NANOS = TimeUnit.SECONDS.toNanos(1L);
   public static final long MILLISECOND_AS_NANOS = TimeUnit.MILLISECONDS.toNanos(1L);
   public static final long MICOSECOND_AS_NANOS = TimeUnit.MICROSECONDS.toNanos(1L);

   @Nonnull
   public static TimeUnit largestUnit(long value, @Nonnull TimeUnit unit) {
      long nanos = unit.toNanos(value);
      if (nanos > DAY_AS_NANOS) {
         return TimeUnit.DAYS;
      } else if (nanos > HOUR_AS_NANOS) {
         return TimeUnit.HOURS;
      } else if (nanos > MINUTE_AS_NANOS) {
         return TimeUnit.MINUTES;
      } else if (nanos > SECOND_AS_NANOS) {
         return TimeUnit.SECONDS;
      } else if (nanos > MILLISECOND_AS_NANOS) {
         return TimeUnit.MILLISECONDS;
      } else {
         return nanos > MICOSECOND_AS_NANOS ? TimeUnit.MICROSECONDS : TimeUnit.NANOSECONDS;
      }
   }

   @Nonnull
   public static String simpleTimeUnitFormat(@Nonnull Metric metric, @Nonnull TimeUnit timeUnit, int rounding) {
      TimeUnit largestUnit = largestUnit(Math.round(metric.getAverage()), timeUnit);
      return simpleTimeUnitFormat(metric, timeUnit, largestUnit, rounding);
   }

   @Nonnull
   public static String simpleTimeUnitFormat(@Nonnull Metric metric, TimeUnit timeUnit, @Nonnull TimeUnit largestUnit, int rounding) {
      long min = metric.getMin();
      double average = metric.getAverage();
      long max = metric.getMax();
      return simpleTimeUnitFormat(min, average, max, timeUnit, largestUnit, rounding);
   }

   @Nonnull
   public static String simpleTimeUnitFormat(long min, double average, long max, TimeUnit timeUnit, @Nonnull TimeUnit largestUnit, int rounding) {
      int roundValue = (int)Math.pow(10.0, rounding);
      long range = Math.round(Math.max(Math.abs(average - min), Math.abs(max - average)));
      long averageNanos = largestUnit.convert(Math.round(average * roundValue), timeUnit);
      long rangeNanos = largestUnit.convert(range * roundValue, timeUnit);
      String unitStr = timeUnitToShortString.get(largestUnit);
      return (double)averageNanos / roundValue + unitStr + "  +/-" + (double)rangeNanos / roundValue + unitStr;
   }

   @Nonnull
   public static String simpleTimeUnitFormat(long value, @Nonnull TimeUnit timeUnit, int rounding) {
      int roundValue = (int)Math.pow(10.0, rounding);
      TimeUnit largestUnit = largestUnit(value, timeUnit);
      long averageNanos = largestUnit.convert(value * roundValue, timeUnit);
      String unitStr = timeUnitToShortString.get(largestUnit);
      return (double)averageNanos / roundValue + unitStr;
   }

   @Nonnull
   public static String simpleFormat(long min1, double average1, long max1, @Nonnull DoubleUnaryOperator doubleFunction, int rounding) {
      double average = doubleFunction.applyAsDouble(average1);
      double min = Math.abs(average - doubleFunction.applyAsDouble(min1));
      double max = Math.abs(doubleFunction.applyAsDouble(max1) - average);
      double range = Math.max(min, max);
      return simpleFormat(rounding, average, range);
   }

   @Nonnull
   public static String simpleFormat(@Nonnull Metric metric) {
      return simpleFormat(metric, 2);
   }

   @Nonnull
   public static String simpleFormat(@Nonnull Metric metric, int rounding) {
      double average = metric.getAverage();
      double min = Math.abs(average - metric.getMin());
      double max = Math.abs(metric.getMax() - average);
      double range = Math.max(min, max);
      return simpleFormat(rounding, average, range);
   }

   @Nonnull
   public static String simpleFormat(int rounding, double average, double range) {
      int roundValue = (int)Math.pow(10.0, rounding);
      return (double)((long)(average * roundValue)) / roundValue + "  +/-" + (double)((long)(range * roundValue)) / roundValue;
   }

   @Nonnull
   public static String timeUnitToString(@Nonnull Metric metric, @Nonnull TimeUnit timeUnit) {
      double min = Math.abs(metric.getAverage() - metric.getMin());
      double max = Math.abs(metric.getMax() - metric.getAverage());
      long range = Math.round(Math.max(min, max));
      return timeUnitToString(Math.round(metric.getAverage()), timeUnit) + "  +/-" + timeUnitToString(range, timeUnit);
   }

   @Nonnull
   public static String timeUnitToString(long value, @Nonnull TimeUnit timeUnit) {
      return timeUnitToString(value, timeUnit, false);
   }

   @Nonnull
   public static String timeUnitToString(long value, @Nonnull TimeUnit timeUnit, boolean paddingBetween) {
      boolean p = false;
      StringBuilder sb = new StringBuilder();
      AtomicLong time = new AtomicLong(value);
      p |= timeToStringPart(time, sb, p, timeUnit, TimeUnit.DAYS, "days", false, paddingBetween);
      boolean hasHours = timeToStringPart(time, sb, p, timeUnit, TimeUnit.HOURS, ":", true, paddingBetween);
      p |= hasHours;
      p |= timeToStringPart(time, sb, p, timeUnit, TimeUnit.MINUTES, hasHours ? ":" : "min", false, paddingBetween);
      p |= timeToStringPart(time, sb, p, timeUnit, TimeUnit.SECONDS, hasHours ? "" : "sec", !hasHours, paddingBetween);
      p |= timeToStringPart(time, sb, p, timeUnit, TimeUnit.MILLISECONDS, "ms", true, paddingBetween);
      p |= timeToStringPart(time, sb, p, timeUnit, TimeUnit.MICROSECONDS, "us", true, paddingBetween);
      p |= timeToStringPart(time, sb, p, timeUnit, TimeUnit.NANOSECONDS, "ns", true, paddingBetween);
      return sb.toString();
   }

   @Nonnull
   public static String nanosToString(long nanos) {
      return timeUnitToString(nanos, TimeUnit.NANOSECONDS);
   }

   private static boolean timeToStringPart(
      @Nonnull AtomicLong time,
      @Nonnull StringBuilder sb,
      boolean previous,
      @Nonnull TimeUnit timeUnitFrom,
      @Nonnull TimeUnit timeUnitTo,
      String after,
      boolean paddingBefore,
      boolean paddingBetween
   ) {
      if (timeUnitFrom.ordinal() > timeUnitTo.ordinal()) {
         return false;
      } else {
         long timeInUnitTo = timeUnitTo.convert(time.get(), timeUnitFrom);
         time.getAndAdd(-timeUnitFrom.convert(timeInUnitTo, timeUnitTo));
         if (timeInUnitTo > 0L || previous && time.get() > 0L || !previous && timeUnitFrom == timeUnitTo) {
            if (paddingBefore && previous) {
               sb.append(' ');
            }

            sb.append(timeInUnitTo);
            if (paddingBetween) {
               sb.append(' ');
            }

            sb.append(after);
            return true;
         } else {
            return false;
         }
      }
   }

   @Nonnull
   public static String bytesToString(long bytes) {
      return bytesToString(bytes, false);
   }

   @Nonnull
   public static String bytesToString(long bytes, boolean si) {
      int unit = si ? 1000 : 1024;
      if (bytes < unit) {
         return bytes + " B";
      } else {
         int exp = (int)(Math.log(bytes) / Math.log(unit));
         return String.format("%.1f %sB", bytes / Math.pow(unit, exp), (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i"));
      }
   }

   @Nonnull
   public static String addNumberSuffix(int i) {
      return switch (i % 100) {
         case 11, 12, 13 -> i + "th";
         default -> i + NUMBER_SUFFIXES[i % 10];
      };
   }

   public static void formatArray(@Nonnull Formatter formatter, @Nonnull String format, @Nonnull Object[] args) {
      for (Object arg : args) {
         formatter.format(format, arg);
      }
   }

   public static void formatArgs(@Nonnull Formatter formatter, @Nonnull String format, @Nonnull Object... args) {
      formatArray(formatter, format, args);
   }
}
