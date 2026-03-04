package com.hypixel.hytale.builtin.hytalegenerator.rangemaps;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DoubleRangeMap<T> {
   private ArrayList<DoubleRange> ranges = new ArrayList<>(1);
   private ArrayList<T> values = new ArrayList<>(1);

   @Nullable
   public T get(double k) {
      for (int i = 0; i < this.ranges.size(); i++) {
         if (this.ranges.get(i).includes(k)) {
            return this.values.get(i);
         }
      }

      return null;
   }

   @Nonnull
   public List<DoubleRange> ranges() {
      return new ArrayList<>(this.ranges);
   }

   @Nonnull
   public List<T> values() {
      return new ArrayList<>(this.values);
   }

   public void put(@Nonnull DoubleRange range, T value) {
      this.ranges.add(range);
      this.values.add(value);
   }

   public int size() {
      return this.ranges.size();
   }
}
