package com.hypixel.hytale.builtin.hytalegenerator.newsystem;

import com.hypixel.hytale.builtin.hytalegenerator.LoggerUtil;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;

public class NViewport {
   @Nonnull
   private final World world;
   @Nonnull
   private final CommandSender sender;
   @Nonnull
   private final LongSet affectedChunkIndices;

   public NViewport(@Nonnull Bounds3i viewportBounds_voxelGrid, @Nonnull World world, @Nonnull CommandSender sender) {
      this.world = world;
      this.sender = sender;
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

   public void refresh() {
      LoggerUtil.getLogger().info("Refreshing viewport...");
      CompletableFuture<?>[] futures = new CompletableFuture[this.affectedChunkIndices.size()];
      int i = 0;
      LongIterator var3 = this.affectedChunkIndices.iterator();

      while (var3.hasNext()) {
         long chunkIndex = (Long)var3.next();
         ChunkStore chunkStore = this.world.getChunkStore();
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
