package com.hypixel.hytale.builtin.adventure.teleporter.system;

import com.hypixel.hytale.builtin.adventure.teleporter.component.Teleporter;
import com.hypixel.hytale.builtin.teleport.TeleportPlugin;
import com.hypixel.hytale.builtin.teleport.Warp;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.AndQuery;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import javax.annotation.Nonnull;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class TurnOffTeleportersSystem extends RefSystem<ChunkStore> {
   public static final Query<ChunkStore> QUERY = Query.and(Teleporter.getComponentType(), BlockModule.BlockStateInfo.getComponentType());

   @Override
   public void onEntityAdded(
      @Nonnull Ref<ChunkStore> ref, @Nonnull AddReason reason, @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer
   ) {
      if (reason == AddReason.LOAD) {
         updatePortalBlocksInWorld(store.getExternalData().getWorld());
      }
   }

   @Override
   public void onEntityRemove(
      @Nonnull Ref<ChunkStore> ref, @Nonnull RemoveReason reason, @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer
   ) {
      if (reason == RemoveReason.REMOVE) {
         updatePortalBlocksInWorld(store.getExternalData().getWorld());
      }
   }

   public static void updatePortalBlocksInWorld(World world) {
      Store<ChunkStore> store = world.getChunkStore().getStore();
      AndQuery<ChunkStore> entityQuery = Query.and(Teleporter.getComponentType(), BlockModule.BlockStateInfo.getComponentType());
      store.forEachChunk(entityQuery, (archetypeChunk, commandBuffer) -> {
         for (int i = 0; i < archetypeChunk.size(); i++) {
            Ref<ChunkStore> ref = archetypeChunk.getReferenceTo(i);
            updatePortalBlockInWorld(ref, commandBuffer);
         }
      });
   }

   private static void updatePortalBlockInWorld(Ref<ChunkStore> ref, ComponentAccessor<ChunkStore> store) {
      if (ref.isValid()) {
         Teleporter teleporter = store.getComponent(ref, Teleporter.getComponentType());
         BlockModule.BlockStateInfo blockState = store.getComponent(ref, BlockModule.BlockStateInfo.getComponentType());
         updatePortalBlockInWorld(store, teleporter, blockState);
      }
   }

   public static void updatePortalBlockInWorld(ComponentAccessor<ChunkStore> store, Teleporter teleporter, BlockModule.BlockStateInfo blockStateInfo) {
      Ref<ChunkStore> chunkRef = blockStateInfo.getChunkRef();
      if (chunkRef.isValid()) {
         WorldChunk worldChunkComponent = store.getComponent(chunkRef, WorldChunk.getComponentType());
         if (worldChunkComponent != null) {
            int index = blockStateInfo.getIndex();
            int x = ChunkUtil.xFromBlockInColumn(index);
            int y = ChunkUtil.yFromBlockInColumn(index);
            int z = ChunkUtil.zFromBlockInColumn(index);
            BlockType blockType = worldChunkComponent.getBlockType(x, y, z);
            if (blockType != null) {
               String warpId = teleporter.getWarp();
               Warp destinationWarp = warpId == null ? null : TeleportPlugin.get().getWarps().get(warpId);
               String currentState = blockType.getStateForBlock(blockType);
               String desiredState = destinationWarp == null ? "default" : "Active";
               if (!desiredState.equals(currentState)) {
                  worldChunkComponent.setBlockInteractionState(x, y, z, blockType, desiredState, false);
                  blockStateInfo.markNeedsSaving(store);
               }

               if (destinationWarp == null) {
                  teleporter.setWarp(null);
                  blockStateInfo.markNeedsSaving(store);
               }
            }
         }
      }
   }

   @NullableDecl
   @Override
   public Query<ChunkStore> getQuery() {
      return QUERY;
   }
}
