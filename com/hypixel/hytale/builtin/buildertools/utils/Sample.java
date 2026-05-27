package com.hypixel.hytale.builtin.buildertools.utils;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

public class Sample {
   private static final int[] BLOCK_TEXTURE_KERNEL = new int[]{0, 1, 0, 1, 3, 1, 0, 1, 0, 1, 3, 1, 3, 4, 3, 1, 3, 1, 0, 1, 0, 1, 3, 1, 0, 1, 0};

   public static Sample.Result3D calculate3D(int x, int y, int z, int[][][] buffer, int[] kernelWeights) {
      Int2IntOpenHashMap counts = new Int2IntOpenHashMap();
      int countAirLike = 0;
      int index = 0;

      for (int deltaX = -1; deltaX <= 1; deltaX++) {
         for (int deltaY = -1; deltaY <= 1; deltaY++) {
            for (int deltaZ = -1; deltaZ <= 1; deltaZ++) {
               int weight = kernelWeights[index];
               index++;
               if (weight != 0) {
                  int blockID = buffer[x + deltaX][y + deltaY][z + deltaZ];
                  if (blockID <= 0) {
                     countAirLike += weight;
                  } else {
                     counts.addTo(blockID, BLOCK_TEXTURE_KERNEL[index - 1]);
                  }
               }
            }
         }
      }

      return new Sample.Result3D(countAirLike, sampleEntries(counts), buffer[x][y][z]);
   }

   public static int calculateAirWeight3D(int x, int y, int z, int[][][] buffer, int[] kernelWeights) {
      int countAirLike = 0;
      int index = 0;

      for (int deltaX = -1; deltaX <= 1; deltaX++) {
         for (int deltaY = -1; deltaY <= 1; deltaY++) {
            for (int deltaZ = -1; deltaZ <= 1; deltaZ++) {
               int weight = kernelWeights[index++];
               if (weight != 0 && buffer[x + deltaX][y + deltaY][z + deltaZ] <= 0) {
                  countAirLike += weight;
               }
            }
         }
      }

      return countAirLike;
   }

   public static Sample.Result2D calcDeltaHeights(
      int x, int y, int z, int[][] heightMap, int[][][] buffer, int[] kernelWeights, int kernelTotal, int hitY, boolean sampleNearby
   ) {
      int currentHeight = heightMap[x][z];
      if (currentHeight == -1) {
         return new Sample.Result2D(0.0, 0, -1, 0);
      } else {
         Int2IntOpenHashMap counts = sampleNearby ? new Int2IntOpenHashMap() : null;
         int bufferHeight = buffer[0].length;
         int countAllHeights = 0;
         int ignoredWeights = 0;
         int index = 0;

         for (int deltaX = -2; deltaX <= 2; deltaX++) {
            for (int deltaZ = -2; deltaZ <= 2; deltaZ++) {
               int bufferX = x + deltaX;
               int bufferZ = z + deltaZ;
               int weight = kernelWeights[index++];
               if (weight != 0) {
                  int height = heightMap[bufferX][bufferZ];
                  if (height == -1) {
                     ignoredWeights += weight;
                  } else {
                     if (counts != null) {
                        int by = height - hitY + bufferHeight / 2;
                        if (by >= 0 && by < bufferHeight) {
                           int blockID = buffer[bufferX][by][bufferZ];
                           if (blockID > 0) {
                              counts.addTo(blockID, weight);
                           }
                        }
                     }

                     countAllHeights += weight * height;
                  }
               }
            }
         }

         int effectiveTotal = kernelTotal - ignoredWeights;
         double delta = effectiveTotal > 0 ? (double)countAllHeights / effectiveTotal - currentHeight : 0.0;
         int sampledBlock = counts != null ? sampleEntries(counts) : 0;
         return new Sample.Result2D(delta, sampledBlock, currentHeight, buffer[x][y][z]);
      }
   }

   private static int sampleEntries(Int2IntOpenHashMap counts) {
      int sampledBlock = 0;
      int maxWeight = 0;
      ObjectIterator var3 = counts.int2IntEntrySet().iterator();

      while (var3.hasNext()) {
         Entry entry = (Entry)var3.next();
         if (entry.getIntValue() > maxWeight) {
            maxWeight = entry.getIntValue();
            sampledBlock = entry.getIntKey();
         }
      }

      return sampledBlock;
   }

   public record Result2D(double deltaHeight, int sampledBlock, int currentHeight, int originalBlock) {
   }

   public record Result3D(int airWeight, int sampledBlock, int originalBlock) {
   }
}
