package com.hypixel.hytale.builtin.hytalegenerator.iterators;

import it.unimi.dsi.fastutil.ints.IntIterator;
import java.util.Iterator;
import javax.annotation.Nonnull;

public class BackwardIntIterator implements IntIterator, Iterator<Integer> {
   private int min;
   private int current;

   public BackwardIntIterator(int min, int maxExclusive) {
      if (min > maxExclusive) {
         throw new IllegalArgumentException("Start greater than end.");
      } else {
         this.min = min;
         this.current = maxExclusive;
      }
   }

   private BackwardIntIterator() {
   }

   @Override
   public boolean hasNext() {
      return this.current > this.min;
   }

   public int nextInt() {
      return --this.current;
   }

   @Nonnull
   public Integer next() {
      return --this.current;
   }

   @Nonnull
   public Integer getCurrent() {
      return this.current;
   }

   @Nonnull
   public BackwardIntIterator clone() {
      BackwardIntIterator clone = new BackwardIntIterator();
      clone.current = this.current;
      clone.min = this.min;
      return clone;
   }
}
