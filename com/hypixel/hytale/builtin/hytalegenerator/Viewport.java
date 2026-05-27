package com.hypixel.hytale.builtin.hytalegenerator;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.lang.ref.WeakReference;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;

public class Viewport {
   @Nonnull
   private final WeakReference<World> world;
   @Nonnull
   private final LongSet affectedChunkIndices;

   public Viewport(@Nonnull Bounds3i viewportBounds_voxelGrid, @Nonnull World world) {
      this.world = new WeakReference<>(world);
      int minCX = ChunkUtil.chunkCoordinate(viewportBounds_voxelGrid.min.x);
      int minCZ = ChunkUtil.chunkCoordinate(viewportBounds_voxelGrid.min.z);
      int maxCX = ChunkUtil.chunkCoordinate(viewportBounds_voxelGrid.max.x);
      int maxCZ = ChunkUtil.chunkCoordinate(viewportBounds_voxelGrid.max.z);
      this.affectedChunkIndices = new LongArraySet();

      for (int x = minCX; x <= maxCX; x++) {
         for (int z = minCZ; z <= maxCZ; z++) {
            long chunkIndex = ChunkUtil.indexChunk(x, z);
            this.affectedChunkIndices.add(chunkIndex);
         }
      }
   }

   public void submitRefresh() {
      World world = this.world.get();
      if (world != null) {
         world.execute(this::refresh);
      }
   }

   public void refresh() {
      World world = this.world.get();
      if (world != null) {
         LoggerUtil.getLogger().info("Refreshing viewport...");
         CompletableFuture<?>[] futures = new CompletableFuture[this.affectedChunkIndices.size()];
         int i = 0;
         LongIterator var4 = this.affectedChunkIndices.iterator();

         while (var4.hasNext()) {
            long chunkIndex = (Long)var4.next();
            ChunkStore chunkStore = world.getChunkStore();
            CompletableFuture<?> future = chunkStore.getChunkReferenceAsync(chunkIndex, 9);
            futures[i++] = future;
         }

         CompletableFuture.allOf(futures).handle((r, e) -> {
            if (e == null) {
               return (Void)r;
            } else {
               LoggerUtil.logException("viewport refresh", e);
               return null;
            }
         }).thenRun(() -> LoggerUtil.getLogger().info("Viewport refresh complete."));
      }
   }
}
