package com.hypixel.hytale.common.benchmark;

import com.hypixel.hytale.common.util.FormatUtil;
import java.util.Formatter;
import javax.annotation.Nonnull;

public class TimeRecorder extends ContinuousValueRecorder {
   public static final String DEFAULT_COLUMN_SEPARATOR = "|";
   public static final String DEFAULT_COLUMN_FORMAT_HEADER = "|%-6.6s";
   public static final String DEFAULT_COLUMN_FORMAT_VALUE = "|%6.6s";
   public static final String[] DEFAULT_COLUMNS = DiscreteValueRecorder.DEFAULT_COLUMNS;
   public static final double NANOS_TO_SECONDS = 1.0E-9;

   public long start() {
      return System.nanoTime();
   }

   public double end(long start) {
      return this.recordNanos(System.nanoTime() - start);
   }

   public double recordNanos(long nanos) {
      return super.record(nanos * 1.0E-9);
   }

   @Nonnull
   @Override
   public String toString() {
      return String.format("Avg=%s Min=%s Max=%s", formatTime(this.getAverage(0.0)), formatTime(this.getMinValue(0.0)), formatTime(this.getMaxValue(0.0)));
   }

   @Nonnull
   public static String formatTime(double secs) {
      if (secs <= 0.0) {
         return "0s";
      } else if (secs >= 10.0) {
         return format(secs, "s");
      } else {
         secs *= 1000.0;
         if (secs >= 10.0) {
            return format(secs, "ms");
         } else {
            secs *= 1000.0;
            if (secs >= 10.0) {
               return format(secs, "us");
            } else {
               secs *= 1000.0;
               return format(secs, "ns");
            }
         }
      }
   }

   @Nonnull
   protected static String format(double val, String suffix) {
      return (int)Math.round(val) + suffix;
   }

   public void formatHeader(@Nonnull Formatter formatter) {
      this.formatHeader(formatter, "|%-6.6s");
   }

   public void formatHeader(@Nonnull Formatter formatter, @Nonnull String columnFormatHeader) {
      FormatUtil.formatArray(formatter, columnFormatHeader, DEFAULT_COLUMNS);
   }

   public void formatValues(@Nonnull Formatter formatter) {
      this.formatValues(formatter, "|%6.6s");
   }

   public void formatValues(@Nonnull Formatter formatter, @Nonnull String columnFormatValue) {
      FormatUtil.formatArgs(
         formatter, columnFormatValue, formatTime(this.getAverage()), formatTime(this.getMinValue()), formatTime(this.getMaxValue()), this.count
      );
   }
}
