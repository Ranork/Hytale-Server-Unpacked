package com.hypixel.hytale.procedurallib.supplier;

import javax.annotation.Nonnull;

public class ConstantDoubleCoordinateHashSupplier implements IDoubleCoordinateHashSupplier {
   public static final ConstantDoubleCoordinateHashSupplier ZERO = new ConstantDoubleCoordinateHashSupplier(0.0);
   public static final ConstantDoubleCoordinateHashSupplier ONE = new ConstantDoubleCoordinateHashSupplier(1.0);
   protected final double result;

   public ConstantDoubleCoordinateHashSupplier(double result) {
      this.result = result;
   }

   public double getResult() {
      return this.result;
   }

   @Override
   public double get(int seed, int x, int y, long hash) {
      return this.result;
   }

   @Nonnull
   @Override
   public String toString() {
      return "ConstantDoubleCoordinateHashSupplier{result=" + this.result + "}";
   }
}
