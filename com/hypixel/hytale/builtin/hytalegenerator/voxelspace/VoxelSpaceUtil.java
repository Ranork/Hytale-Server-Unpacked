package com.hypixel.hytale.builtin.hytalegenerator.voxelspace;

import com.hypixel.hytale.builtin.hytalegenerator.LoggerUtil;
import com.hypixel.hytale.common.util.ExceptionUtil;

public class VoxelSpaceUtil {
   private static class BatchTransfer<T> implements Runnable {
      private final VoxelSpace<T> source;
      private final VoxelSpace<T> destination;
      private final int minX;
      private final int minY;
      private final int minZ;
      private final int maxX;
      private final int maxY;
      private final int maxZ;

      private BatchTransfer(VoxelSpace<T> source, VoxelSpace<T> destination, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
         this.source = source;
         this.destination = destination;
         this.minX = minX;
         this.minY = minY;
         this.minZ = minZ;
         this.maxX = maxX;
         this.maxY = maxY;
         this.maxZ = maxZ;
      }

      @Override
      public void run() {
         try {
            for (int x = this.minX; x < this.maxX; x++) {
               for (int y = this.minY; y < this.maxY; y++) {
                  for (int z = this.minZ; z < this.maxZ; z++) {
                     if (this.destination.getBounds().contains(x, y, z)) {
                        this.destination.set(this.source.get(x, y, z), x, y, z);
                     }
                  }
               }
            }
         } catch (Exception var4) {
            String msg = "Exception thrown by HytaleGenerator while attempting a BatchTransfer operation:\n";
            msg = msg + ExceptionUtil.toStringWithStack(var4);
            LoggerUtil.getLogger().severe(msg);
         }
      }
   }
}
