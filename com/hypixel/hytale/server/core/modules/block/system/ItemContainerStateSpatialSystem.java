package com.hypixel.hytale.server.core.modules.block.system;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.component.spatial.SpatialSystem;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.meta.BlockStateModule;
import com.hypixel.hytale.server.core.universe.world.meta.state.ItemContainerState;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemContainerStateSpatialSystem extends SpatialSystem<ChunkStore> {
   @Nonnull
   public static final Query<ChunkStore> QUERY = (Query<ChunkStore>)Objects.requireNonNull(BlockStateModule.get().getComponentType(ItemContainerState.class));

   public ItemContainerStateSpatialSystem(ResourceType<ChunkStore, SpatialResource<Ref<ChunkStore>, ChunkStore>> resourceType) {
      super(resourceType);
   }

   @Override
   public void tick(float dt, int systemIndex, @Nonnull Store<ChunkStore> store) {
      if (store.getResource(BlockModule.BlockStateInfoNeedRebuild.getResourceType()).invalidateAndReturnIfNeedRebuild()) {
         super.tick(dt, systemIndex, store);
      }
   }

   @Override
   public Vector3d getPosition(@Nonnull ArchetypeChunk<ChunkStore> archetypeChunk, int index) {
      BlockModule.BlockStateInfo blockInfo = archetypeChunk.getComponent(index, BlockModule.BlockStateInfo.getComponentType());
      Ref<ChunkStore> chunkRef = blockInfo.getChunkRef();
      if (chunkRef != null && chunkRef.isValid()) {
         BlockChunk blockChunk = chunkRef.getStore().getComponent(chunkRef, BlockChunk.getComponentType());
         int worldX = blockChunk.getX() << 5 | ChunkUtil.xFromBlockInColumn(blockInfo.getIndex());
         int worldY = ChunkUtil.yFromBlockInColumn(blockInfo.getIndex());
         int worldZ = blockChunk.getZ() << 5 | ChunkUtil.zFromBlockInColumn(blockInfo.getIndex());
         return new Vector3d(worldX, worldY, worldZ);
      } else {
         return null;
      }
   }

   @Nullable
   @Override
   public Query<ChunkStore> getQuery() {
      return QUERY;
   }
}
