package com.hypixel.hytale.procedurallib.logic;

import com.hypixel.hytale.procedurallib.NoiseFunction;
import javax.annotation.Nonnull;

public class ConstantNoise implements NoiseFunction {
   protected final double value;

   public ConstantNoise(double value) {
      this.value = value;
   }

   public double getValue() {
      return this.value;
   }

   @Override
   public double get(int seed, int offsetSeed, double x, double y) {
      return this.value;
   }

   @Override
   public double get(int seed, int offsetSeed, double x, double y, double z) {
      return this.value;
   }

   @Nonnull
   @Override
   public String toString() {
      return "ConstantNoise{value=" + this.value + "}";
   }
}
