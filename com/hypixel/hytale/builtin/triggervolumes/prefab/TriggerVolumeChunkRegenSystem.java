package com.hypixel.hytale.builtin.triggervolumes.prefab;

import com.hypixel.hytale.builtin.triggervolumes.manager.TriggerVolumeManager;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.ChunkFlag;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TriggerVolumeChunkRegenSystem extends RefSystem<ChunkStore> {
   private static final Query<ChunkStore> QUERY = WorldChunk.getComponentType();
   @Nonnull
   private final ResourceType<EntityStore, TriggerVolumeManager> managerResourceType;

   public TriggerVolumeChunkRegenSystem(@Nonnull ResourceType<EntityStore, TriggerVolumeManager> managerResourceType) {
      this.managerResourceType = managerResourceType;
   }

   @Override
   public void onEntityAdded(
      @Nonnull Ref<ChunkStore> ref, @Nonnull AddReason reason, @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer
   ) {
      if (reason == AddReason.SPAWN) {
         WorldChunk wc = commandBuffer.getComponent(ref, WorldChunk.getComponentType());
         if (wc != null && wc.is(ChunkFlag.NEWLY_GENERATED)) {
            World world = store.getExternalData().getWorld();
            if (world != null) {
               TriggerVolumeManager manager = world.getEntityStore().getStore().getResource(this.managerResourceType);
               if (manager != null) {
                  manager.markWorldGenRegenChunk(ChunkUtil.indexChunk(wc.getX(), wc.getZ()));
               }
            }
         }
      }
   }

   @Override
   public void onEntityRemove(
      @Nonnull Ref<ChunkStore> ref, @Nonnull RemoveReason reason, @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer
   ) {
   }

   @Nullable
   @Override
   public Query<ChunkStore> getQuery() {
      return QUERY;
   }
}
