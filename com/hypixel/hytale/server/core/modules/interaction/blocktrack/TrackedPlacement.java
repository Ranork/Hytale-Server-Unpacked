package com.hypixel.hytale.server.core.modules.interaction.blocktrack;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.interaction.InteractionModule;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TrackedPlacement implements Component<ChunkStore> {
   public static final BuilderCodec<TrackedPlacement> CODEC = BuilderCodec.builder(TrackedPlacement.class, TrackedPlacement::new)
      .append(new KeyedCodec<>("BlockName", Codec.STRING), (o, v) -> o.blockName = v, o -> o.blockName)
      .add()
      .build();
   private String blockName;

   public static ComponentType<ChunkStore, TrackedPlacement> getComponentType() {
      return InteractionModule.get().getTrackedPlacementComponentType();
   }

   public TrackedPlacement() {
   }

   public TrackedPlacement(String blockName) {
      this.blockName = blockName;
   }

   @Nullable
   @Override
   public Component<ChunkStore> clone() {
      return new TrackedPlacement(this.blockName);
   }

   public static class OnAddRemove extends RefSystem<ChunkStore> {
      private static final ComponentType<ChunkStore, TrackedPlacement> COMPONENT_TYPE = TrackedPlacement.getComponentType();
      private static final ResourceType<ChunkStore, BlockCounter> BLOCK_COUNTER_RESOURCE_TYPE = BlockCounter.getResourceType();

      @Override
      public void onEntityAdded(
         @Nonnull Ref<ChunkStore> ref, @Nonnull AddReason reason, @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer
      ) {
         if (reason == AddReason.SPAWN) {
            BlockModule.BlockStateInfo blockInfo = commandBuffer.getComponent(ref, BlockModule.BlockStateInfo.getComponentType());

            assert blockInfo != null;

            Ref<ChunkStore> chunkRef = blockInfo.getChunkRef();
            if (chunkRef != null && chunkRef.isValid()) {
               BlockChunk blockChunk = commandBuffer.getComponent(chunkRef, BlockChunk.getComponentType());

               assert blockChunk != null;

               int blockId = blockChunk.getBlock(
                  ChunkUtil.xFromBlockInColumn(blockInfo.getIndex()),
                  ChunkUtil.yFromBlockInColumn(blockInfo.getIndex()),
                  ChunkUtil.zFromBlockInColumn(blockInfo.getIndex())
               );
               BlockType blockType = BlockType.getAssetMap().getAsset(blockId);
               if (blockType != null) {
                  BlockCounter counter = commandBuffer.getResource(BLOCK_COUNTER_RESOURCE_TYPE);
                  String blockName = blockType.getId();
                  counter.trackBlock(blockName);
                  TrackedPlacement tracked = commandBuffer.getComponent(ref, COMPONENT_TYPE);

                  assert tracked != null;

                  tracked.blockName = blockName;
               }
            }
         }
      }

      @Override
      public void onEntityRemove(
         @Nonnull Ref<ChunkStore> ref, @Nonnull RemoveReason reason, @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer
      ) {
         if (reason == RemoveReason.REMOVE) {
            TrackedPlacement tracked = commandBuffer.getComponent(ref, COMPONENT_TYPE);

            assert tracked != null;

            BlockCounter counter = commandBuffer.getResource(BLOCK_COUNTER_RESOURCE_TYPE);
            counter.untrackBlock(tracked.blockName);
         }
      }

      @Nullable
      @Override
      public Query<ChunkStore> getQuery() {
         return COMPONENT_TYPE;
      }
   }
}
