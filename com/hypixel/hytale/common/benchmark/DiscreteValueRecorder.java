package com.hypixel.hytale.common.benchmark;

import com.hypixel.hytale.common.util.FormatUtil;
import java.util.Formatter;
import javax.annotation.Nonnull;

public class DiscreteValueRecorder {
   public static final String DEFAULT_COLUMN_SEPARATOR = "|";
   public static final String DEFAULT_COLUMN_FORMAT_HEADER = "|%-6.6s";
   public static final String DEFAULT_COLUMN_FORMAT_VALUE = "|%6.6s";
   public static final String[] DEFAULT_COLUMNS = new String[]{"AVG", "MIN", "MAX", "COUNT"};
   protected long minValue;
   protected long maxValue;
   protected long sumValues;
   protected long count;

   public DiscreteValueRecorder() {
      this.reset();
   }

   public void reset() {
      this.minValue = Long.MAX_VALUE;
      this.maxValue = Long.MIN_VALUE;
      this.sumValues = 0L;
      this.count = 0L;
   }

   public long getMinValue(long def) {
      return this.count > 0L ? this.minValue : def;
   }

   public long getMinValue() {
      return this.getMinValue(0L);
   }

   public long getMaxValue(long def) {
      return this.count > 0L ? this.maxValue : def;
   }

   public long getMaxValue() {
      return this.getMaxValue(0L);
   }

   public long getCount() {
      return this.count;
   }

   public long getAverage(long def) {
      return this.count > 0L ? (2L * this.sumValues + this.count) / (2L * this.count) : def;
   }

   public long getAverage() {
      return this.getAverage(0L);
   }

   public void record(long value) {
      if (this.minValue > value) {
         this.minValue = value;
      }

      if (this.maxValue < value) {
         this.maxValue = value;
      }

      this.count++;
      this.sumValues += value;
   }

   @Nonnull
   @Override
   public String toString() {
      return String.format("Avg=%s Min=%s Max=%s", this.getAverage(), this.getMinValue(), this.getMaxValue());
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
      FormatUtil.formatArgs(formatter, columnFormatValue, this.getAverage(), this.getMinValue(), this.getMaxValue(), this.count);
   }
}
