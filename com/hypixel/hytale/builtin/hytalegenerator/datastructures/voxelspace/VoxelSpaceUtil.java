package com.hypixel.hytale.builtin.hytalegenerator.datastructures.voxelspace;

import com.hypixel.hytale.builtin.hytalegenerator.LoggerUtil;
import com.hypixel.hytale.common.util.ExceptionUtil;
import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import javax.annotation.Nonnull;

public class VoxelSpaceUtil {
   public static <V> void parallelCopy(@Nonnull VoxelSpace<V> source, @Nonnull VoxelSpace<V> destination, int concurrency) {
      if (concurrency < 1) {
         throw new IllegalArgumentException("negative concurrency");
      } else {
         int minX = source.minX();
         int minY = source.minY();
         int minZ = source.minZ();
         int sizeX = source.sizeX();
         int sizeY = source.sizeY();
         int sizeZ = source.sizeZ();
         LinkedList<CompletableFuture<Void>> tasks = new LinkedList<>();
         int bSize = source.sizeX() * source.sizeY() * source.sizeZ() / concurrency;

         for (int b = 0; b < concurrency; b++) {
            tasks.add(CompletableFuture.runAsync(() -> {
               for (int i = b * bSize; i < (b + 1) * bSize; i++) {
                  int x = i % sizeX + minX;
                  int y = i / sizeX % sizeY + minY;
                  int z = i / (sizeX * sizeY) % sizeZ + minZ;
                  if (source.isInsideSpace(x, y, z) && destination.isInsideSpace(x, y, z)) {
                     destination.set(source.getContent(x, y, z), x, y, z);
                  }
               }
            }).handle((r, ex) -> {
               if (ex == null) {
                  return (Void)r;
               } else {
                  LoggerUtil.logException("a VoxelSpace async process", ex, LoggerUtil.getLogger());
                  return null;
               }
            }));
         }

         try {
            while (!tasks.isEmpty()) {
               tasks.removeFirst().get();
            }
         } catch (ExecutionException | InterruptedException var13) {
            Thread.currentThread().interrupt();
            String msg = "Exception thrown by HytaleGenerator while attempting an asynchronous copy of a VoxelSpace:\n";
            msg = msg + ExceptionUtil.toStringWithStack(var13);
            LoggerUtil.getLogger().severe(msg);
         }
      }
   }

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
                     if (this.destination.isInsideSpace(x, y, z)) {
                        this.destination.set(this.source.getContent(x, y, z), x, y, z);
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
