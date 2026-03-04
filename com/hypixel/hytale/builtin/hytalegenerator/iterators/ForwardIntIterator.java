package com.hypixel.hytale.builtin.hytalegenerator.iterators;

import it.unimi.dsi.fastutil.ints.IntIterator;
import java.util.Iterator;
import javax.annotation.Nonnull;

public class ForwardIntIterator implements IntIterator, Iterator<Integer> {
   private int max;
   private int current;

   public ForwardIntIterator(int min, int maxExclusive) {
      if (min > maxExclusive) {
         throw new IllegalArgumentException("Start greater than end.");
      } else {
         this.max = maxExclusive - 1;
         this.current = min - 1;
      }
   }

   private ForwardIntIterator() {
   }

   @Override
   public boolean hasNext() {
      return this.current < this.max;
   }

   public int nextInt() {
      return ++this.current;
   }

   @Nonnull
   public Integer next() {
      return ++this.current;
   }

   @Nonnull
   public Integer getCurrent() {
      return this.current;
   }

   @Nonnull
   public ForwardIntIterator clone() {
      ForwardIntIterator clone = new ForwardIntIterator();
      clone.current = this.current;
      clone.max = this.max;
      return clone;
   }
}
