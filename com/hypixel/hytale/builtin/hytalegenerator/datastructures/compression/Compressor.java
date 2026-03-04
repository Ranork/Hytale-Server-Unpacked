package com.hypixel.hytale.builtin.hytalegenerator.datastructures.compression;

import javax.annotation.Nonnull;

public class Compressor {
   private final int MIN_RUN = 7;

   @Nonnull
   public <T> Compressor.CompressedArray<T> compressOnReference(@Nonnull T[] in) {
      int currentRun = 0;
      int resultIndex = 0;
      Object runObj = null;
      Object[] result = new Object[in.length];

      for (int i = 0; i < result.length; i++) {
         if (in[i] != runObj && currentRun >= 7) {
            result[resultIndex] = new Compressor.Run(runObj, currentRun);
            currentRun = 0;
            resultIndex++;
            runObj = in[i];
         } else if (in[i] != runObj && currentRun < 7) {
            while (currentRun > 0) {
               result[resultIndex] = runObj;
               resultIndex++;
               currentRun--;
            }

            currentRun = 0;
            runObj = in[i];
         } else {
            currentRun++;
         }
      }

      if (currentRun >= 7) {
         result[resultIndex] = new Compressor.Run(runObj, currentRun);
      } else {
         while (currentRun > 0) {
            result[resultIndex] = runObj;
            resultIndex++;
            currentRun--;
         }
      }

      Object[] trimmedResult = new Object[resultIndex];
      System.arraycopy(result, 0, trimmedResult, 0, trimmedResult.length);
      return new Compressor.CompressedArray<>(trimmedResult, in.length);
   }

   @Nonnull
   public <T> T[] decompress(@Nonnull Compressor.CompressedArray<T> compressedArray) {
      int caIndex = 0;
      int runIndex = 0;
      int outIndex = 0;
      Object[] ca = compressedArray.data;

      Object[] out;
      for (out = new Object[compressedArray.initialLength]; caIndex < ca.length; caIndex++) {
         if (ca[caIndex] instanceof Compressor.Run run) {
            for (int var8 = 0; var8 < run.length; var8++) {
               out[outIndex++] = run.obj;
            }
         } else {
            out[outIndex++] = ca[caIndex];
         }
      }

      return (T[])out;
   }

   public static class CompressedArray<T> {
      private final Object[] data;
      private final int initialLength;

      private CompressedArray(Object[] data, int initialLength) {
         this.data = data;
         this.initialLength = initialLength;
      }
   }

   public static class Run {
      Object obj;
      int length;

      private Run(Object obj, int length) {
         this.obj = obj;
         this.length = length;
      }
   }
}
