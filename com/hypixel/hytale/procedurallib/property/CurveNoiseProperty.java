package com.hypixel.hytale.procedurallib.property;

import com.hypixel.hytale.math.util.MathUtil;
import java.util.function.DoubleUnaryOperator;
import javax.annotation.Nonnull;

public class CurveNoiseProperty implements NoiseProperty {
   protected final NoiseProperty noise;
   protected final DoubleUnaryOperator function;

   public CurveNoiseProperty(NoiseProperty noise, DoubleUnaryOperator function) {
      this.noise = noise;
      this.function = function;
   }

   @Override
   public double get(int seed, double x, double y) {
      double value = this.noise.get(seed, x, y);
      return this.function.applyAsDouble(value);
   }

   @Override
   public double get(int seed, double x, double y, double z) {
      double value = this.noise.get(seed, x, y, z);
      return this.function.applyAsDouble(value);
   }

   @Nonnull
   @Override
   public String toString() {
      return "CurveNoiseProperty{noise=" + this.noise + ", function=" + this.function + "}";
   }

   public static class PowerCurve implements DoubleUnaryOperator {
      protected static final double MAX = 10.0;
      protected final double a;
      protected final double b;

      public PowerCurve(double a, double b) {
         this.a = MathUtil.clamp(a, 0.0, 10.0);
         this.b = MathUtil.clamp(b, -this.a, 10.0);
      }

      @Override
      public double applyAsDouble(double operand) {
         operand = 1.0 - operand;
         return 1.0 - Math.pow(operand, this.a + this.b * operand);
      }

      @Nonnull
      @Override
      public String toString() {
         return "PowerCurve{a=" + this.a + ", b=" + this.b + "}";
      }
   }
}
