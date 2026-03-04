package com.hypixel.hytale.procedurallib.random;

import com.hypixel.hytale.procedurallib.property.NoiseProperty;
import java.util.Arrays;
import javax.annotation.Nonnull;

public class CoordinateRandomizer implements ICoordinateRandomizer {
   public static final ICoordinateRandomizer EMPTY_RANDOMIZER = new CoordinateRandomizer.EmptyCoordinateRandomizer();
   protected final CoordinateRandomizer.AmplitudeNoiseProperty[] xNoise;
   protected final CoordinateRandomizer.AmplitudeNoiseProperty[] yNoise;
   protected final CoordinateRandomizer.AmplitudeNoiseProperty[] zNoise;

   public CoordinateRandomizer(
      CoordinateRandomizer.AmplitudeNoiseProperty[] xNoise,
      CoordinateRandomizer.AmplitudeNoiseProperty[] yNoise,
      CoordinateRandomizer.AmplitudeNoiseProperty[] zNoise
   ) {
      this.xNoise = xNoise;
      this.yNoise = yNoise;
      this.zNoise = zNoise;
   }

   public CoordinateRandomizer.AmplitudeNoiseProperty[] getXNoise() {
      return this.xNoise;
   }

   public CoordinateRandomizer.AmplitudeNoiseProperty[] getYNoise() {
      return this.yNoise;
   }

   public CoordinateRandomizer.AmplitudeNoiseProperty[] getZNoise() {
      return this.zNoise;
   }

   @Override
   public double randomDoubleX(int seed, double x, double y) {
      double offsetX = 0.0;

      for (CoordinateRandomizer.AmplitudeNoiseProperty property : this.xNoise) {
         offsetX += (property.property.get(seed, x, y) * 2.0 - 1.0) * property.amplitude;
      }

      return x + offsetX;
   }

   @Override
   public double randomDoubleY(int seed, double x, double y) {
      double offsetY = 0.0;

      for (CoordinateRandomizer.AmplitudeNoiseProperty property : this.yNoise) {
         offsetY += (property.property.get(seed, x, y) * 2.0 - 1.0) * property.amplitude;
      }

      return y + offsetY;
   }

   @Override
   public double randomDoubleX(int seed, double x, double y, double z) {
      double offsetX = 0.0;

      for (CoordinateRandomizer.AmplitudeNoiseProperty property : this.xNoise) {
         offsetX += (property.property.get(seed, x, y, z) * 2.0 - 1.0) * property.amplitude;
      }

      return x + offsetX;
   }

   @Override
   public double randomDoubleY(int seed, double x, double y, double z) {
      double offsetY = 0.0;

      for (CoordinateRandomizer.AmplitudeNoiseProperty property : this.yNoise) {
         offsetY += (property.property.get(seed, x, y, z) * 2.0 - 1.0) * property.amplitude;
      }

      return y + offsetY;
   }

   @Override
   public double randomDoubleZ(int seed, double x, double y, double z) {
      double offsetZ = 0.0;

      for (CoordinateRandomizer.AmplitudeNoiseProperty property : this.zNoise) {
         offsetZ += (property.property.get(seed, x, y, z) * 2.0 - 1.0) * property.amplitude;
      }

      return z + offsetZ;
   }

   @Nonnull
   @Override
   public String toString() {
      return "CoordinateRandomizer{xNoise="
         + Arrays.toString((Object[])this.xNoise)
         + ", yNoise="
         + Arrays.toString((Object[])this.yNoise)
         + ", zNoise="
         + Arrays.toString((Object[])this.zNoise)
         + "}";
   }

   public static class AmplitudeNoiseProperty {
      protected NoiseProperty property;
      protected double amplitude;

      public AmplitudeNoiseProperty(NoiseProperty property, double amplitude) {
         this.property = property;
         this.amplitude = amplitude;
      }

      public NoiseProperty getProperty() {
         return this.property;
      }

      public void setProperty(NoiseProperty property) {
         this.property = property;
      }

      public double getAmplitude() {
         return this.amplitude;
      }

      public void setAmplitude(double amplitude) {
         this.amplitude = amplitude;
      }

      @Nonnull
      @Override
      public String toString() {
         return "AmplitudeNoiseProperty{property=" + this.property + ", amplitude=" + this.amplitude + "}";
      }
   }

   private static class EmptyCoordinateRandomizer implements ICoordinateRandomizer {
      @Override
      public double randomDoubleX(int seed, double x, double y) {
         return x;
      }

      @Override
      public double randomDoubleY(int seed, double x, double y) {
         return y;
      }

      @Override
      public double randomDoubleX(int seed, double x, double y, double z) {
         return x;
      }

      @Override
      public double randomDoubleY(int seed, double x, double y, double z) {
         return y;
      }

      @Override
      public double randomDoubleZ(int seed, double x, double y, double z) {
         return z;
      }

      @Nonnull
      @Override
      public String toString() {
         return "EmptyCoordinateRandomizer{}";
      }
   }
}
