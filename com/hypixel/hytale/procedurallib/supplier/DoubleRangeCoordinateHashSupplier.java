package com.hypixel.hytale.procedurallib.supplier;

import com.hypixel.hytale.math.util.HashUtil;
import javax.annotation.Nonnull;

public class DoubleRangeCoordinateHashSupplier implements IDoubleCoordinateHashSupplier {
   protected final IDoubleRange range;

   public DoubleRangeCoordinateHashSupplier(IDoubleRange range) {
      this.range = range;
   }

   @Override
   public double get(int seed, int x, int y, long hash) {
      return this.range.getValue(HashUtil.random(seed, x, y, hash));
   }

   @Nonnull
   @Override
   public String toString() {
      return "DoubleRangeCoordinateHashSupplier{range=" + this.range + "}";
   }
}
