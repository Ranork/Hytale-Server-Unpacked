package com.hypixel.hytale.procedurallib.logic;

import com.hypixel.hytale.math.util.HashUtil;
import com.hypixel.hytale.procedurallib.NoiseFunction;
import javax.annotation.Nonnull;

public class ValueNoise implements NoiseFunction {
   protected final GeneralNoise.InterpolationFunction interpolationFunction;

   public ValueNoise(GeneralNoise.InterpolationFunction interpolationFunction) {
      this.interpolationFunction = interpolationFunction;
   }

   public GeneralNoise.InterpolationFunction getInterpolationFunction() {
      return this.interpolationFunction;
   }

   @Override
   public double get(int seed, int offsetSeed, double x, double y) {
      int x0 = GeneralNoise.fastFloor(x);
      int y0 = GeneralNoise.fastFloor(y);
      int x1 = x0 + 1;
      int y1 = y0 + 1;
      double xs = this.interpolationFunction.interpolate(x - x0);
      double ys = this.interpolationFunction.interpolate(y - y0);
      double xf0 = GeneralNoise.lerp(HashUtil.random(offsetSeed, x0, y0), HashUtil.random(offsetSeed, x1, y0), xs);
      double xf1 = GeneralNoise.lerp(HashUtil.random(offsetSeed, x0, y1), HashUtil.random(offsetSeed, x1, y1), xs);
      return GeneralNoise.lerp(xf0, xf1, ys) * 2.0 - 1.0;
   }

   @Override
   public double get(int seed, int offsetSeed, double x, double y, double z) {
      int x0 = GeneralNoise.fastFloor(x);
      int y0 = GeneralNoise.fastFloor(y);
      int z0 = GeneralNoise.fastFloor(z);
      int x1 = x0 + 1;
      int y1 = y0 + 1;
      int z1 = z0 + 1;
      double xs = this.interpolationFunction.interpolate(x - x0);
      double ys = this.interpolationFunction.interpolate(y - y0);
      double zs = this.interpolationFunction.interpolate(z - z0);
      double xf00 = GeneralNoise.lerp(HashUtil.random(offsetSeed, x0, y0, z0), HashUtil.random(offsetSeed, x1, y0, z0), xs);
      double xf10 = GeneralNoise.lerp(HashUtil.random(offsetSeed, x0, y1, z0), HashUtil.random(offsetSeed, x1, y1, z0), xs);
      double xf01 = GeneralNoise.lerp(HashUtil.random(offsetSeed, x0, y0, z1), HashUtil.random(offsetSeed, x1, y0, z1), xs);
      double xf11 = GeneralNoise.lerp(HashUtil.random(offsetSeed, x0, y1, z1), HashUtil.random(offsetSeed, x1, y1, z1), xs);
      double yf0 = GeneralNoise.lerp(xf00, xf10, ys);
      double yf1 = GeneralNoise.lerp(xf01, xf11, ys);
      return GeneralNoise.lerp(yf0, yf1, zs) * 2.0 - 1.0;
   }

   @Nonnull
   @Override
   public String toString() {
      return "ValueNoise{interpolationFunction=" + this.interpolationFunction + "}";
   }
}
