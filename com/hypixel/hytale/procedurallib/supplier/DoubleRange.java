package com.hypixel.hytale.procedurallib.supplier;

import java.util.Arrays;
import java.util.Random;
import java.util.function.DoubleSupplier;
import javax.annotation.Nonnull;

public class DoubleRange {
   public static final DoubleRange.Constant ZERO = new DoubleRange.Constant(0.0);
   public static final DoubleRange.Constant ONE = new DoubleRange.Constant(1.0);

   public static class Constant implements IDoubleRange {
      protected final double result;

      public Constant(double result) {
         this.result = result;
      }

      public double getResult() {
         return this.result;
      }

      @Override
      public double getValue(double v) {
         return this.result;
      }

      @Override
      public double getValue(DoubleSupplier supplier) {
         return this.result;
      }

      @Override
      public double getValue(Random random) {
         return this.result;
      }

      @Override
      public double getValue(int seed, double x, double y, IDoubleCoordinateSupplier2d supplier) {
         return this.result;
      }

      @Override
      public double getValue(int seed, double x, double y, double z, IDoubleCoordinateSupplier3d supplier) {
         return this.result;
      }

      @Nonnull
      @Override
      public String toString() {
         return "DoubleRange.Constant{result=" + this.result + "}";
      }
   }

   public static class Multiple implements IDoubleRange {
      protected final double[] thresholds;
      protected final double[] values;

      public Multiple(double[] thresholds, double[] values) {
         this.thresholds = thresholds;
         this.values = values;
      }

      @Override
      public double getValue(double v) {
         if (v > this.thresholds[this.thresholds.length - 1]) {
            return this.values[this.values.length - 1];
         } else {
            double min = 0.0;

            for (int i = 0; i < this.thresholds.length; i++) {
               double max = this.thresholds[i];
               if (v < max) {
                  if (i == 0) {
                     return this.values[0];
                  }

                  double alpha = (v - min) / (max - min);
                  double valueMin = this.values[i - 1];
                  double valueMax = this.values[i];
                  double range = valueMax - valueMin;
                  return valueMin + alpha * range;
               }

               min = max;
            }

            return 0.0;
         }
      }

      @Override
      public double getValue(@Nonnull DoubleSupplier supplier) {
         return this.getValue(supplier.getAsDouble());
      }

      @Override
      public double getValue(@Nonnull Random random) {
         return this.getValue(random.nextDouble());
      }

      @Override
      public double getValue(int seed, double x, double y, @Nonnull IDoubleCoordinateSupplier2d supplier) {
         return this.getValue(supplier.get(seed, x, y));
      }

      @Override
      public double getValue(int seed, double x, double y, double z, @Nonnull IDoubleCoordinateSupplier3d supplier) {
         return this.getValue(supplier.get(seed, x, y, z));
      }

      @Nonnull
      @Override
      public String toString() {
         return "Multiple{thresholds=" + Arrays.toString(this.thresholds) + ", values=" + Arrays.toString(this.values) + "}";
      }
   }

   public static class Normal implements IDoubleRange {
      protected final double min;
      protected final double range;

      public Normal(double min, double max) {
         this.min = min;
         this.range = max - min;
      }

      public double getMin() {
         return this.min;
      }

      public double getRange() {
         return this.range;
      }

      @Override
      public double getValue(double v) {
         return this.min + this.range * v;
      }

      @Override
      public double getValue(@Nonnull DoubleSupplier supplier) {
         return this.min + this.range * supplier.getAsDouble();
      }

      @Override
      public double getValue(@Nonnull Random random) {
         return this.getValue(random.nextDouble());
      }

      @Override
      public double getValue(int seed, double x, double y, @Nonnull IDoubleCoordinateSupplier2d supplier) {
         return this.min + this.range * supplier.get(seed, x, y);
      }

      @Override
      public double getValue(int seed, double x, double y, double z, @Nonnull IDoubleCoordinateSupplier3d supplier) {
         return this.min + this.range * supplier.get(seed, x, y, z);
      }

      @Nonnull
      @Override
      public String toString() {
         return "DoubleRange.Normal{min=" + this.min + ", range=" + this.range + "}";
      }
   }
}
