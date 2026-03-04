package com.hypixel.hytale.builtin.blocktick.system;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import javax.annotation.Nonnull;

public class MergeWaitingBlocksSystem extends RefSystem<ChunkStore> {
   @Nonnull
   private static final ComponentType<ChunkStore, WorldChunk> COMPONENT_TYPE = WorldChunk.getComponentType();

   @Override
   public Query<ChunkStore> getQuery() {
      return COMPONENT_TYPE;
   }

   @Override
   public void onEntityAdded(
      @Nonnull Ref<ChunkStore> ref, @Nonnull AddReason reason, @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer
   ) {
      ChunkStore chunkStore = store.getExternalData();
      WorldChunk worldChunkComponent = store.getComponent(ref, COMPONENT_TYPE);

      assert worldChunkComponent != null;

      int x = worldChunkComponent.getX();
      int z = worldChunkComponent.getZ();
      mergeTickingBlocks(chunkStore, x - 1, z);
      mergeTickingBlocks(chunkStore, x + 1, z);
      mergeTickingBlocks(chunkStore, x, z - 1);
      mergeTickingBlocks(chunkStore, x, z + 1);
   }

   @Override
   public void onEntityRemove(
      @Nonnull Ref<ChunkStore> ref, @Nonnull RemoveReason reason, @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer
   ) {
   }

   public static void mergeTickingBlocks(@Nonnull ChunkStore store, int x, int z) {
      long chunkIndex = ChunkUtil.indexChunk(x, z);
      BlockChunk blockChunkComponent = store.getChunkComponent(chunkIndex, BlockChunk.getComponentType());
      if (blockChunkComponent != null) {
         blockChunkComponent.mergeTickingBlocks();
      }
   }
}
