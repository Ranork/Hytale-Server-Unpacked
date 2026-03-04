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

public class BlockTypeFarmingStageData extends FarmingStageData {
   @Nonnull
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   @Nonnull
   public static final BuilderCodec<BlockTypeFarmingStageData> CODEC = BuilderCodec.builder(
         BlockTypeFarmingStageData.class, BlockTypeFarmingStageData::new, FarmingStageData.BASE_CODEC
      )
      .append(new KeyedCodec<>("Block", Codec.STRING), (stage, block) -> stage.block = block, stage -> stage.block)
      .addValidatorLate(() -> BlockType.VALIDATOR_CACHE.getValidator().late())
      .add()
      .build();
   protected String block;

   public String getBlock() {
      return this.block;
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
            int blockId = BlockType.getAssetMap().getIndex(this.block);
            if (blockId != worldChunkComponent.getBlock(x, y, z)) {
               BlockType blockType = BlockType.getAssetMap().getAsset(blockId);
               if (blockType == null) {
                  ((HytaleLogger.Api)LOGGER.at(Level.WARNING).atMostEvery(1, TimeUnit.MINUTES))
                     .log("Invalid block type '%s' when applying block type farming stage at (%d, %d, %d)", this.block, x, y, z);
               } else {
                  commandBuffer.getExternalData().getWorld().execute(() -> {
                     int rotationIndex = worldChunkComponent.getRotationIndex(x, y, z);
                     worldChunkComponent.setBlock(x, y, z, blockId, blockType, rotationIndex, 0, 2);
                  });
               }
            }
         }
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "BlockTypeFarmingStageData{block=" + this.block + "} " + super.toString();
   }
}
