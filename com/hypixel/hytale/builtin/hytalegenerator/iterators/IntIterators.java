package com.hypixel.hytale.builtin.hytalegenerator.iterators;

import it.unimi.dsi.fastutil.ints.IntIterator;
import javax.annotation.Nonnull;

public class IntIterators {
   @Nonnull
   public static IntIterator range(int start, int end) {
      return (IntIterator)(start <= end ? new ForwardIntIterator(start, end) : new BackwardIntIterator(end, start));
   }
}
