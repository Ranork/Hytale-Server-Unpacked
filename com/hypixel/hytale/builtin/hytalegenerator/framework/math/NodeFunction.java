package com.hypixel.hytale.builtin.hytalegenerator.framework.math;

import com.hypixel.hytale.builtin.hytalegenerator.ArrayUtil;
import com.hypixel.hytale.builtin.hytalegenerator.delimiters.RangeDouble;
import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import javax.annotation.Nonnull;

public class NodeFunction implements Function<Double, Double>, Double2DoubleFunction {
   private static final double FALLBACK_VALUE = 0.0;
   @Nonnull
   private final List<double[]> points = new ArrayList<>(2);
   @Nonnull
   private final List<RangeDouble> ranges = new ArrayList<>(2);

   public Double apply(@Nonnull Double input) {
      return this.get(input);
   }

   public double get(double input) {
      if (Double.isNaN(input)) {
         return 0.0;
      } else if (this.points.isEmpty()) {
         return 0.0;
      } else if (this.points.size() == 1 || input <= this.points.getFirst()[0]) {
         return this.points.getFirst()[1];
      } else if (input >= this.points.getLast()[0]) {
         return this.points.getLast()[1];
      } else {
         int indexBefore = this.indexBefore(input);
         double[] before = this.points.get(indexBefore);
         double[] after = this.points.get(indexBefore + 1);
         double differenceY = after[1] - before[1];
         double ratio = (input - before[0]) / (after[0] - before[0]);
         return before[1] + differenceY * ratio;
      }
   }

   @Nonnull
   public NodeFunction addPoint(double in, double out) {
      for (double[] point : this.points) {
         if (point[0] == in) {
            return this;
         }
      }

      this.points.add(new double[]{in, out});
      this.points.sort((a, b) -> {
         if (a[0] < b[0]) {
            return -1;
         } else {
            return a[0] == b[0] ? 0 : 1;
         }
      });
      this.initializeRanges();
      return this;
   }

   public boolean contains(double x) {
      return this.points.parallelStream().anyMatch(point -> point[0] == x);
   }

   private void initializeRanges() {
      this.ranges.clear();

      for (int i = 0; i < this.points.size() - 1; i++) {
         this.ranges.add(new RangeDouble(this.points.get(i)[0], this.points.get(i + 1)[0]));
      }
   }

   private int indexBefore(double input) {
      return ArrayUtil.sortedSearch(this.ranges, input, (gauge, range) -> {
         if (gauge < range.min()) {
            return -1;
         } else {
            return gauge >= range.max() ? 1 : 0;
         }
      });
   }
}
