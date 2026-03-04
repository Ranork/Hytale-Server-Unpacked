package com.hypixel.hytale.server.core.universe.world;

import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import java.util.concurrent.CompletableFuture;

@Deprecated
public interface IWorldChunksAsync {
   CompletableFuture<WorldChunk> getChunkAsync(long var1);

   CompletableFuture<WorldChunk> getNonTickingChunkAsync(long var1);

   default CompletableFuture<WorldChunk> getChunkAsync(int x, int z) {
      return this.getChunkAsync(ChunkUtil.indexChunk(x, z));
   }

   default CompletableFuture<WorldChunk> getNonTickingChunkAsync(int x, int z) {
      return this.getNonTickingChunkAsync(ChunkUtil.indexChunk(x, z));
   }
}
