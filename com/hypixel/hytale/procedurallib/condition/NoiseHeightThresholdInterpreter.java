package com.hypixel.hytale.procedurallib.condition;

import com.hypixel.hytale.procedurallib.property.NoiseProperty;
import java.util.Arrays;
import javax.annotation.Nonnull;

public class NoiseHeightThresholdInterpreter implements IHeightThresholdInterpreter {
   protected final NoiseProperty noise;
   @Nonnull
   protected final float[] keys;
   @Nonnull
   protected final IHeightThresholdInterpreter[] values;
   protected final int length;
   protected final int lowestNonOne;
   protected final int highestNonZero;

   public NoiseHeightThresholdInterpreter(NoiseProperty noise, @Nonnull float[] keys, @Nonnull IHeightThresholdInterpreter[] values) {
      if (keys.length != values.length) {
         throw new IllegalStateException("Length of keys and values are different!");
      } else {
         checkInterpreterLength(values);
         this.noise = noise;
         this.keys = keys;
         this.values = values;
         this.length = values[0].getLength();
         int lowestNonOne = 0;

         for (IHeightThresholdInterpreter value : values) {
            if (value.getLowestNonOne() < lowestNonOne) {
               lowestNonOne = value.getLowestNonOne();
            }
         }

         this.lowestNonOne = lowestNonOne;
         int highestNonZero = this.length - 1;

         for (IHeightThresholdInterpreter valuex : values) {
            if (valuex.getHighestNonZero() > highestNonZero) {
               highestNonZero = valuex.getHighestNonZero();
            }
         }

         this.highestNonZero = highestNonZero;
      }
   }

   @Override
   public int getLowestNonOne() {
      return this.lowestNonOne;
   }

   @Override
   public int getHighestNonZero() {
      return this.highestNonZero;
   }

   protected double noise(int seed, double x, double y) {
      return this.noise.get(seed, x, y);
   }

   @Override
   public double getContext(int seed, double x, double y) {
      return this.noise(seed, x, y);
   }

   @Override
   public int getLength() {
      return this.length;
   }

   @Override
   public float getThreshold(int seed, double x, double z, int height) {
      return this.getThreshold(seed, x, z, height, this.getContext(seed, x, z));
   }

   @Override
   public float getThreshold(int seed, double x, double z, int height, double context) {
      if (height > this.highestNonZero) {
         return 0.0F;
      } else {
         int length = this.keys.length;

         for (int i = 0; i < length; i++) {
            if (context <= this.keys[i]) {
               if (i == 0) {
                  return this.values[0].getThreshold(seed, x, z, height);
               }

               float distance = ((float)context - this.keys[i - 1]) / (this.keys[i] - this.keys[i - 1]);
               return IHeightThresholdInterpreter.lerp(
                  this.values[i - 1].getThreshold(seed, x, z, height), this.values[i].getThreshold(seed, x, z, height), distance
               );
            }
         }

         return this.values[length - 1].getThreshold(seed, x, z, height);
      }
   }

   static float lerp(float from, float to, float t) {
      return from + (to - from) * t;
   }

   private static void checkInterpreterLength(@Nonnull IHeightThresholdInterpreter[] values) {
      int length = values[0].getLength();

      for (int i = 1; i < values.length; i++) {
         if (values[i].getLength() != length) {
            throw new IllegalStateException("ThresholdKeyInterpreter have different size!");
         }
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "NoiseHeightThresholdInterpreter{noise="
         + this.noise
         + ", keys="
         + Arrays.toString(this.keys)
         + ", values="
         + Arrays.toString((Object[])this.values)
         + ", length="
         + this.length
         + ", lowestNonOne="
         + this.lowestNonOne
         + ", highestNonZero="
         + this.highestNonZero
         + "}";
   }
}
