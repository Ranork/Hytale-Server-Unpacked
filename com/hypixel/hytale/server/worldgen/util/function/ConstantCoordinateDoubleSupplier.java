package com.hypixel.hytale.server.worldgen.util.function;

public class ConstantCoordinateDoubleSupplier implements ICoordinateDoubleSupplier {
   public static final ConstantCoordinateDoubleSupplier DEFAULT_ZERO = new ConstantCoordinateDoubleSupplier(0.0);
   public static final ConstantCoordinateDoubleSupplier DEFAULT_ONE = new ConstantCoordinateDoubleSupplier(1.0);
   protected final double value;

   public ConstantCoordinateDoubleSupplier(double value) {
      this.value = value;
   }

   public double getValue() {
      return this.value;
   }

   @Override
   public double apply(int seed, int x, int y) {
      return this.value;
   }

   @Override
   public double apply(int seed, int x, int y, int z) {
      return this.value;
   }
}
