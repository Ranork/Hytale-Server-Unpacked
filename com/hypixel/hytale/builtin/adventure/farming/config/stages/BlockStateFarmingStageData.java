package com.hypixel.hytale.builtin.adventure.farming.config.stages;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.farming.FarmingStageData;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.ChunkSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockStateFarmingStageData extends FarmingStageData {
   @Nonnull
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   @Nonnull
   public static final BuilderCodec<BlockStateFarmingStageData> CODEC = BuilderCodec.builder(
         BlockStateFarmingStageData.class, BlockStateFarmingStageData::new, FarmingStageData.BASE_CODEC
      )
      .append(new KeyedCodec<>("State", Codec.STRING), (stage, block) -> stage.state = block, stage -> stage.state)
      .add()
      .build();
   protected String state;

   public String getState() {
      return this.state;
   }

   @Override
   public void apply(
      @Nonnull ComponentAccessor<ChunkStore> commandBuffer,
      @Nonnull Ref<ChunkStore> sectionRef,
      @Nonnull Ref<ChunkStore> blockRef,
      int x,
      int y,
      int z,
      @Nullable FarmingStageData previousStage
   ) {
      super.apply(commandBuffer, sectionRef, blockRef, x, y, z, previousStage);
      ChunkSection chunkSectionComponent = commandBuffer.getComponent(sectionRef, ChunkSection.getComponentType());
      if (chunkSectionComponent == null) {
         ((HytaleLogger.Api)LOGGER.at(Level.WARNING).atMostEvery(1, TimeUnit.MINUTES))
            .log("Missing chunk section component when applying state farming stage at (%d, %d, %d)", x, y, z);
      } else {
         WorldChunk worldChunkComponent = commandBuffer.getComponent(chunkSectionComponent.getChunkColumnReference(), WorldChunk.getComponentType());
         if (worldChunkComponent == null) {
            ((HytaleLogger.Api)LOGGER.at(Level.WARNING).atMostEvery(1, TimeUnit.MINUTES))
               .log("Missing world chunk component when applying state farming stage at (%d, %d, %d)", x, y, z);
         } else {
            int originBlockId = worldChunkComponent.getBlock(x, y, z);
            BlockType originBlockType = BlockType.getAssetMap().getAsset(originBlockId);
            if (originBlockType == null) {
               ((HytaleLogger.Api)LOGGER.at(Level.WARNING).atMostEvery(1, TimeUnit.MINUTES))
                  .log("Missing origin block type for block ID '%s' when applying state farming stage at (%d, %d, %d)", String.valueOf(originBlockId), x, y, z);
            } else {
               BlockType blockType = originBlockType.getBlockForState(this.state);
               if (blockType == null) {
                  ((HytaleLogger.Api)LOGGER.at(Level.WARNING).atMostEvery(1, TimeUnit.MINUTES))
                     .log("Missing new block type '%s' when applying state farming stage at (%d, %d, %d)", this.state, x, y, z);
               } else {
                  int newBlockId = BlockType.getAssetMap().getIndex(blockType.getId());
                  if (originBlockId != newBlockId) {
                     int rotationIndex = worldChunkComponent.getRotationIndex(x, y, z);
                     commandBuffer.getExternalData()
                        .getWorld()
                        .execute(() -> worldChunkComponent.setBlock(x, y, z, newBlockId, blockType, rotationIndex, 0, 2));
                  }
               }
            }
         }
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "BlockStateFarmingStageData{state='" + this.state + "'} " + super.toString();
   }
}
