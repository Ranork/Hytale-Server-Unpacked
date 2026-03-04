package com.hypixel.hytale.builtin.hytalegenerator.delimiters;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DelimiterInt<V> {
   @Nonnull
   private final RangeInt range;
   @Nullable
   private final V value;

   public DelimiterInt(@Nonnull RangeInt range, @Nullable V value) {
      this.range = range;
      this.value = value;
   }

   @Nonnull
   public RangeInt getRange() {
      return this.range;
   }

   @Nullable
   public V getValue() {
      return this.value;
   }
}
