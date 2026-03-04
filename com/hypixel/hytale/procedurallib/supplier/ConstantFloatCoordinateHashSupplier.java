package com.hypixel.hytale.procedurallib.supplier;

import javax.annotation.Nonnull;

public class ConstantFloatCoordinateHashSupplier implements IFloatCoordinateHashSupplier {
   public static final ConstantFloatCoordinateHashSupplier ZERO = new ConstantFloatCoordinateHashSupplier(0.0F);
   public static final ConstantFloatCoordinateHashSupplier ONE = new ConstantFloatCoordinateHashSupplier(1.0F);
   protected final float result;

   public ConstantFloatCoordinateHashSupplier(float result) {
      this.result = result;
   }

   public float getResult() {
      return this.result;
   }

   @Override
   public float get(int seed, double x, double y, long hash) {
      return this.result;
   }

   @Nonnull
   @Override
   public String toString() {
      return "ConstantFloatCoordinateHashSupplier{result=" + this.result + "}";
   }
}
