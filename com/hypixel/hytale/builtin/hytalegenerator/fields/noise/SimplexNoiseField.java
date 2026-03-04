package com.hypixel.hytale.builtin.hytalegenerator.fields.noise;

import java.util.Random;
import javax.annotation.Nonnull;

public class SimplexNoiseField extends NoiseField {
   private final long seed;
   @Nonnull
   private final double[] offsetX;
   @Nonnull
   private final double[] offsetY;
   @Nonnull
   private final double[] offsetZ;
   @Nonnull
   private final double[] offsetW;
   private final int numberOfOctaves;
   @Nonnull
   private final double[] octaveFrequency;
   @Nonnull
   private final double[] octaveAmplitude;
   private final double normalizer;

   public SimplexNoiseField(long seed, double octaveAmplitudeMultiplier, double octaveFrequencyMultiplier, int numberOfOctaves) {
      if (numberOfOctaves <= 0) {
         throw new IllegalArgumentException("octaves can't be smaller than 1");
      } else {
         this.seed = seed;
         this.numberOfOctaves = numberOfOctaves;
         Random rand = new Random(seed);
         this.offsetX = new double[numberOfOctaves];
         this.offsetY = new double[numberOfOctaves];
         this.offsetZ = new double[numberOfOctaves];
         this.offsetW = new double[numberOfOctaves];

         for (int i = 0; i < numberOfOctaves; i++) {
            this.offsetX[i] = rand.nextDouble() * 256.0;
            this.offsetY[i] = rand.nextDouble() * 256.0;
            this.offsetZ[i] = rand.nextDouble() * 256.0;
            this.offsetW[i] = rand.nextDouble() * 256.0;
         }

         this.octaveAmplitude = new double[numberOfOctaves];
         this.octaveFrequency = new double[numberOfOctaves];
         double frequency = 1.0;
         double amplitude = 1.0;
         double maxAmplitude = 0.0;

         for (int i = 0; i < numberOfOctaves; i++) {
            this.octaveAmplitude[i] = amplitude;
            this.octaveFrequency[i] = frequency;
            maxAmplitude += amplitude;
            amplitude *= octaveAmplitudeMultiplier;
            frequency *= octaveFrequencyMultiplier;
         }

         this.normalizer = 1.0 / maxAmplitude;
      }
   }

   @Nonnull
   public static SimplexNoiseField.Builder builder() {
      return new SimplexNoiseField.Builder();
   }

   @Override
   public double valueAt(double x, double y, double z, double w) {
      x /= this.scaleX;
      y /= this.scaleY;
      z /= this.scaleZ;
      w /= this.scaleW;
      double octaveX = 0.0;
      double octaveY = 0.0;
      double octaveZ = 0.0;
      double octaveW = 0.0;
      double value = 0.0;

      for (int i = 0; i < this.numberOfOctaves; i++) {
         octaveX = x + this.offsetX[i];
         octaveY = y + this.offsetY[i];
         octaveZ = z + this.offsetZ[i];
         octaveW = w + this.offsetW[i];
         value += Simplex.noise(
               octaveX * this.octaveFrequency[i], octaveY * this.octaveFrequency[i], octaveZ * this.octaveFrequency[i], octaveW * this.octaveFrequency[i]
            )
            * this.octaveAmplitude[i];
      }

      return value * this.normalizer;
   }

   @Override
   public double valueAt(double x, double y, double z) {
      x /= this.scaleX;
      y /= this.scaleY;
      z /= this.scaleZ;
      double octaveX = 0.0;
      double octaveY = 0.0;
      double octaveZ = 0.0;
      double value = 0.0;

      for (int i = 0; i < this.numberOfOctaves; i++) {
         octaveX = x + this.offsetX[i];
         octaveY = y + this.offsetY[i];
         octaveZ = z + this.offsetZ[i];
         value += Simplex.noise(octaveX * this.octaveFrequency[i], octaveY * this.octaveFrequency[i], octaveZ * this.octaveFrequency[i])
            * this.octaveAmplitude[i];
      }

      return value * this.normalizer;
   }

   @Override
   public double valueAt(double x, double y) {
      x /= this.scaleX;
      y /= this.scaleY;
      double octaveX = 0.0;
      double octaveY = 0.0;
      double value = 0.0;

      for (int i = 0; i < this.numberOfOctaves; i++) {
         octaveX = x + this.offsetX[i];
         octaveY = y + this.offsetY[i];
         value += Simplex.noise(octaveX * this.octaveFrequency[i], octaveY * this.octaveFrequency[i]) * this.octaveAmplitude[i];
      }

      return value * this.normalizer;
   }

   @Override
   public double valueAt(double x) {
      x /= this.scaleX;
      double octaveX = 0.0;
      double value = 0.0;

      for (int i = 0; i < this.numberOfOctaves; i++) {
         octaveX = x + this.offsetX[i];
         value += Simplex.noise(octaveX * this.octaveFrequency[i], 0.0) * this.octaveAmplitude[i];
      }

      return value * this.normalizer;
   }

   public long getSeed() {
      return this.seed;
   }

   public static class Builder {
      private long seed = 1L;
      private double octaveAmplitudeMultiplier = 0.5;
      private double octaveFrequencyMultiplier = 2.0;
      private int numberOfOctaves = 4;
      private double scaleX;
      private double scaleY;
      private double scaleZ;
      private double scaleW;

      private Builder() {
      }

      @Nonnull
      public SimplexNoiseField build() {
         SimplexNoiseField g = new SimplexNoiseField(this.seed, this.octaveAmplitudeMultiplier, this.octaveFrequencyMultiplier, this.numberOfOctaves);
         g.setScale(this.scaleX, this.scaleY, this.scaleZ, this.scaleW);
         return g;
      }

      @Nonnull
      public SimplexNoiseField.Builder withScale(double s) {
         this.scaleX = s;
         this.scaleY = s;
         this.scaleZ = s;
         this.scaleW = s;
         return this;
      }

      @Nonnull
      public SimplexNoiseField.Builder withScale(double x, double y, double z, double w) {
         this.scaleX = x;
         this.scaleY = y;
         this.scaleZ = z;
         this.scaleW = w;
         return this;
      }

      @Nonnull
      public SimplexNoiseField.Builder withNumberOfOctaves(int n) {
         if (n <= 0) {
            throw new IllegalArgumentException("invalid number");
         } else {
            this.numberOfOctaves = n;
            return this;
         }
      }

      @Nonnull
      public SimplexNoiseField.Builder withFrequencyMultiplier(double f) {
         this.octaveFrequencyMultiplier = f;
         return this;
      }

      @Nonnull
      public SimplexNoiseField.Builder withAmplitudeMultiplier(double a) {
         this.octaveAmplitudeMultiplier = a;
         return this;
      }

      @Nonnull
      public SimplexNoiseField.Builder withSeed(long s) {
         this.seed = s;
         return this;
      }
   }
}
