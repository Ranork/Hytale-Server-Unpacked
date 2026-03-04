package com.hypixel.hytale.procedurallib.logic;

import com.hypixel.hytale.procedurallib.NoiseFunction;
import javax.annotation.Nonnull;

public class PerlinNoise implements NoiseFunction {
   protected final GeneralNoise.InterpolationFunction interpolationFunction;

   public PerlinNoise(GeneralNoise.InterpolationFunction interpolationFunction) {
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
      double xd0 = x - x0;
      double yd0 = y - y0;
      double xd1 = xd0 - 1.0;
      double yd1 = yd0 - 1.0;
      double xf0 = GeneralNoise.lerp(GeneralNoise.gradCoord2D(offsetSeed, x0, y0, xd0, yd0), GeneralNoise.gradCoord2D(offsetSeed, x1, y0, xd1, yd0), xs);
      double xf1 = GeneralNoise.lerp(GeneralNoise.gradCoord2D(offsetSeed, x0, y1, xd0, yd1), GeneralNoise.gradCoord2D(offsetSeed, x1, y1, xd1, yd1), xs);
      return GeneralNoise.lerp(xf0, xf1, ys);
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
      double xd0 = x - x0;
      double yd0 = y - y0;
      double zd0 = z - z0;
      double xd1 = xd0 - 1.0;
      double yd1 = yd0 - 1.0;
      double zd1 = zd0 - 1.0;
      double xf00 = GeneralNoise.lerp(
         GeneralNoise.gradCoord3D(offsetSeed, x0, y0, z0, xd0, yd0, zd0), GeneralNoise.gradCoord3D(offsetSeed, x1, y0, z0, xd1, yd0, zd0), xs
      );
      double xf10 = GeneralNoise.lerp(
         GeneralNoise.gradCoord3D(offsetSeed, x0, y1, z0, xd0, yd1, zd0), GeneralNoise.gradCoord3D(offsetSeed, x1, y1, z0, xd1, yd1, zd0), xs
      );
      double xf01 = GeneralNoise.lerp(
         GeneralNoise.gradCoord3D(offsetSeed, x0, y0, z1, xd0, yd0, zd1), GeneralNoise.gradCoord3D(offsetSeed, x1, y0, z1, xd1, yd0, zd1), xs
      );
      double xf11 = GeneralNoise.lerp(
         GeneralNoise.gradCoord3D(offsetSeed, x0, y1, z1, xd0, yd1, zd1), GeneralNoise.gradCoord3D(offsetSeed, x1, y1, z1, xd1, yd1, zd1), xs
      );
      double yf0 = GeneralNoise.lerp(xf00, xf10, ys);
      double yf1 = GeneralNoise.lerp(xf01, xf11, ys);
      return GeneralNoise.lerp(yf0, yf1, zs);
   }

   @Nonnull
   @Override
   public String toString() {
      return "PerlinNoise{interpolationFunction=" + this.interpolationFunction + "}";
   }
}
