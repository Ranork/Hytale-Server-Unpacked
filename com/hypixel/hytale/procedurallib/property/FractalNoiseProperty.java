package com.hypixel.hytale.procedurallib.property;

import com.hypixel.hytale.procedurallib.NoiseFunction;
import com.hypixel.hytale.procedurallib.NoiseFunction2d;
import com.hypixel.hytale.procedurallib.NoiseFunction3d;
import com.hypixel.hytale.procedurallib.logic.GeneralNoise;
import javax.annotation.Nonnull;

public class FractalNoiseProperty implements NoiseProperty {
   protected final int seedOffset;
   protected final NoiseFunction function;
   protected final FractalNoiseProperty.FractalFunction fractalFunction;
   protected final int octaves;
   protected final double lacunarity;
   protected final double persistence;

   public FractalNoiseProperty(
      int seedOffset, NoiseFunction function, FractalNoiseProperty.FractalFunction fractalFunction, int octaves, double lacunarity, double persistence
   ) {
      this.seedOffset = seedOffset;
      this.function = function;
      this.fractalFunction = fractalFunction;
      this.octaves = octaves;
      this.lacunarity = lacunarity;
      this.persistence = persistence;
   }

   public int getSeedOffset() {
      return this.seedOffset;
   }

   public NoiseFunction getFunction() {
      return this.function;
   }

   public FractalNoiseProperty.FractalFunction getFractalFunction() {
      return this.fractalFunction;
   }

   public int getOctaves() {
      return this.octaves;
   }

   public double getLacunarity() {
      return this.lacunarity;
   }

   public double getPersistence() {
      return this.persistence;
   }

   @Override
   public double get(int seed, double x, double y) {
      return this.fractalFunction.get(seed, seed + this.seedOffset, x, y, this.octaves, this.lacunarity, this.persistence, this.function);
   }

   @Override
   public double get(int seed, double x, double y, double z) {
      return this.fractalFunction.get(seed, seed + this.seedOffset, x, y, z, this.octaves, this.lacunarity, this.persistence, this.function);
   }

   @Nonnull
   @Override
   public String toString() {
      return "FractalNoiseProperty{seedOffset="
         + this.seedOffset
         + ", function="
         + this.function
         + ", fractalFunction="
         + this.fractalFunction
         + ", octaves="
         + this.octaves
         + ", lacunarity="
         + this.lacunarity
         + ", persistence="
         + this.persistence
         + "}";
   }

   private interface FractalFunction {
      double get(int var1, int var2, double var3, double var5, int var7, double var8, double var10, NoiseFunction2d var12);

      double get(int var1, int var2, double var3, double var5, double var7, int var9, double var10, double var12, NoiseFunction3d var14);
   }

   public static enum FractalMode {
      FBM(
         new FractalNoiseProperty.FractalFunction() {
            @Override
            public double get(int seed, int offsetSeed, double x, double y, int octaves, double lacunarity, double persistence, @Nonnull NoiseFunction2d noise) {
               double sum = noise.get(seed, offsetSeed, x, y);
               double amp = 1.0;

               for (int i = 1; i < octaves; i++) {
                  x *= lacunarity;
                  y *= lacunarity;
                  amp *= persistence;
                  sum += noise.get(seed, ++offsetSeed, x, y) * amp;
               }

               return GeneralNoise.limit(sum * 0.5 + 0.5);
            }

            @Override
            public double get(
               int seed, int offsetSeed, double x, double y, double z, int octaves, double lacunarity, double persistence, @Nonnull NoiseFunction3d noise
            ) {
               double sum = noise.get(seed, offsetSeed, x, y, z);
               double amp = 1.0;

               for (int i = 1; i < octaves; i++) {
                  x *= lacunarity;
                  y *= lacunarity;
                  z *= lacunarity;
                  amp *= persistence;
                  sum += noise.get(seed, ++offsetSeed, x, y, z) * amp;
               }

               return GeneralNoise.limit(sum * 0.5 + 0.5);
            }

            @Nonnull
            @Override
            public String toString() {
               return "FbmFractalFunction{}";
            }
         }
      ),
      BILLOW(
         new FractalNoiseProperty.FractalFunction() {
            @Override
            public double get(int seed, int offsetSeed, double x, double y, int octaves, double lacunarity, double persistence, @Nonnull NoiseFunction2d noise) {
               double sum = Math.abs(noise.get(seed, offsetSeed, x, y)) * 2.0 - 1.0;
               double amp = 1.0;

               for (int i = 1; i < octaves; i++) {
                  x *= lacunarity;
                  y *= lacunarity;
                  amp *= persistence;
                  offsetSeed++;
                  sum += (Math.abs(noise.get(seed, offsetSeed, x, y)) * 2.0 - 1.0) * amp;
               }

               return GeneralNoise.limit(sum * 0.5 + 0.5);
            }

            @Override
            public double get(
               int seed, int offsetSeed, double x, double y, double z, int octaves, double lacunarity, double persistence, @Nonnull NoiseFunction3d noise
            ) {
               double sum = Math.abs(noise.get(seed, offsetSeed, x, y, z)) * 2.0 - 1.0;
               double amp = 1.0;

               for (int i = 1; i < octaves; i++) {
                  x *= lacunarity;
                  y *= lacunarity;
                  z *= lacunarity;
                  amp *= persistence;
                  offsetSeed++;
                  sum += (Math.abs(noise.get(seed, offsetSeed, x, y, z)) * 2.0 - 1.0) * amp;
               }

               return GeneralNoise.limit(sum * 0.5 + 0.5);
            }

            @Nonnull
            @Override
            public String toString() {
               return "BillowFractalFunction{}";
            }
         }
      ),
      MULTI_RIGID(
         new FractalNoiseProperty.FractalFunction() {
            @Override
            public double get(int seed, int offsetSeed, double x, double y, int octaves, double lacunarity, double persistence, @Nonnull NoiseFunction2d noise) {
               double sum = 1.0 - Math.abs(noise.get(seed, offsetSeed, x, y));
               double amp = 1.0;

               for (int i = 1; i < octaves; i++) {
                  x *= lacunarity;
                  y *= lacunarity;
                  amp *= persistence;
                  offsetSeed++;
                  sum -= (1.0 - Math.abs(noise.get(seed, offsetSeed, x, y))) * amp;
               }

               return GeneralNoise.limit(sum * 0.5 + 0.5);
            }

            @Override
            public double get(
               int seed, int offsetSeed, double x, double y, double z, int octaves, double lacunarity, double persistence, @Nonnull NoiseFunction3d noise
            ) {
               double sum = 1.0 - Math.abs(noise.get(seed, offsetSeed, x, y, z));
               float amp = 1.0F;

               for (int i = 1; i < octaves; i++) {
                  x *= lacunarity;
                  y *= lacunarity;
                  z *= lacunarity;
                  amp = (float)(amp * persistence);
                  offsetSeed++;
                  sum -= (1.0 - Math.abs(noise.get(seed, offsetSeed, x, y, z))) * amp;
               }

               return GeneralNoise.limit(sum * 0.5 + 0.5);
            }

            @Nonnull
            @Override
            public String toString() {
               return "MultiRigidFractalFunction{}";
            }
         }
      ),
      OLDSCHOOL(
         new FractalNoiseProperty.FractalFunction() {
            @Override
            public double get(int seed, int offsetSeed, double x, double y, int octaves, double lacunarity, double persistence, @Nonnull NoiseFunction2d noise) {
               double maxAmp = 0.0;
               double amp = 1.0;
               int freq = 1;
               double sum = 0.0;
               seed--;

               for (int i = 0; i < octaves; i++) {
                  sum += noise.get(seed, offsetSeed++, x * freq, y * freq) * amp;
                  maxAmp += amp;
                  amp *= persistence;
                  freq <<= 1;
               }

               sum /= maxAmp;
               sum *= 0.5;
               return sum + 0.5;
            }

            @Override
            public double get(
               int seed, int offsetSeed, double x, double y, double z, int octaves, double lacunarity, double persistence, @Nonnull NoiseFunction3d noise
            ) {
               double maxAmp = 0.0;
               double amp = 1.0;
               int freq = 1;
               double sum = 0.0;
               seed--;

               for (int i = 0; i < octaves; i++) {
                  sum += noise.get(seed, offsetSeed++, x * freq, y * freq, z * freq) * amp;
                  maxAmp += amp;
                  amp *= persistence;
                  freq <<= 1;
               }

               sum /= maxAmp;
               sum *= 0.5;
               return sum + 0.5;
            }

            @Nonnull
            @Override
            public String toString() {
               return "OldschoolFractalFunction{}";
            }
         }
      ),
      MIN(
         new FractalNoiseProperty.FractalFunction() {
            @Override
            public double get(int seed, int offsetSeed, double x, double y, int octaves, double lacunarity, double persistence, @Nonnull NoiseFunction2d noise) {
               double min = noise.get(seed, offsetSeed, x, y);

               for (int i = 0; i < octaves; i++) {
                  x *= lacunarity;
                  y *= lacunarity;
                  double d = noise.get(seed, ++offsetSeed, x, y);
                  if (d < min) {
                     min = d;
                  }
               }

               return GeneralNoise.limit(min * 0.5 + 0.5);
            }

            @Override
            public double get(
               int seed, int offsetSeed, double x, double y, double z, int octaves, double lacunarity, double persistence, @Nonnull NoiseFunction3d noise
            ) {
               double min = noise.get(seed, offsetSeed, x, y, z);

               for (int i = 0; i < octaves; i++) {
                  x *= lacunarity;
                  y *= lacunarity;
                  double d = noise.get(seed, ++offsetSeed, x, y, z);
                  if (d < min) {
                     min = d;
                  }
               }

               return GeneralNoise.limit(min * 0.5 + 0.5);
            }

            @Nonnull
            @Override
            public String toString() {
               return "MinFractalFunction{}";
            }
         }
      ),
      MAX(
         new FractalNoiseProperty.FractalFunction() {
            @Override
            public double get(int seed, int offsetSeed, double x, double y, int octaves, double lacunarity, double persistence, @Nonnull NoiseFunction2d noise) {
               double max = noise.get(seed, offsetSeed, x, y);

               for (int i = 0; i < octaves; i++) {
                  x *= lacunarity;
                  y *= lacunarity;
                  double d = noise.get(seed, ++offsetSeed, x, y);
                  if (d > max) {
                     max = d;
                  }
               }

               return GeneralNoise.limit(max * 0.5 + 0.5);
            }

            @Override
            public double get(
               int seed, int offsetSeed, double x, double y, double z, int octaves, double lacunarity, double persistence, @Nonnull NoiseFunction3d noise
            ) {
               double max = noise.get(seed, offsetSeed, x, y, z);

               for (int i = 0; i < octaves; i++) {
                  x *= lacunarity;
                  y *= lacunarity;
                  double d = noise.get(seed, ++offsetSeed, x, y, z);
                  if (d > max) {
                     max = d;
                  }
               }

               return GeneralNoise.limit(max * 0.5 + 0.5);
            }

            @Nonnull
            @Override
            public String toString() {
               return "MaxFractalFunction{}";
            }
         }
      );

      private final FractalNoiseProperty.FractalFunction function;

      private FractalMode(FractalNoiseProperty.FractalFunction function) {
         this.function = function;
      }

      public FractalNoiseProperty.FractalFunction getFunction() {
         return this.function;
      }
   }
}
