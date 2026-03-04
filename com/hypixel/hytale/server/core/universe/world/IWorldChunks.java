package com.hypixel.hytale.server.core.universe.world;

import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.server.core.universe.world.accessor.IChunkAccessorSync;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;

@Deprecated
public interface IWorldChunks extends IChunkAccessorSync<WorldChunk>, IWorldChunksAsync {
   @Deprecated
   void consumeTaskQueue();

   boolean isInThread();

   default WorldChunk getChunk(long index) {
      WorldChunk worldChunk = this.loadChunkIfInMemory(index);
      if (worldChunk != null) {
         return worldChunk;
      } else {
         CompletableFuture<WorldChunk> future = this.getChunkAsync(index);
         return this.waitForFutureWithoutLock(future);
      }
   }

   default WorldChunk getNonTickingChunk(long index) {
      WorldChunk worldChunk = this.getChunkIfInMemory(index);
      if (worldChunk != null) {
         return worldChunk;
      } else {
         CompletableFuture<WorldChunk> future = this.getNonTickingChunkAsync(index);
         return this.waitForFutureWithoutLock(future);
      }
   }

   default <T> T waitForFutureWithoutLock(@Nonnull CompletableFuture<T> future) {
      if (!this.isInThread()) {
         return future.join();
      } else {
         AssetRegistry.ASSET_LOCK.readLock().unlock();

         for (; !future.isDone(); Thread.yield()) {
            AssetRegistry.ASSET_LOCK.readLock().lock();

            try {
               this.consumeTaskQueue();
            } finally {
               AssetRegistry.ASSET_LOCK.readLock().unlock();
            }
         }

         AssetRegistry.ASSET_LOCK.readLock().lock();
         return future.join();
      }
   }
}
