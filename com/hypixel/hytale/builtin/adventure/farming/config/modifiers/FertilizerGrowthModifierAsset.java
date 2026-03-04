package com.hypixel.hytale.builtin.adventure.farming.config.modifiers;

import com.hypixel.hytale.builtin.adventure.farming.states.TilledSoilBlock;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.farming.GrowthModifierAsset;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockComponentChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.ChunkSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import javax.annotation.Nonnull;

public class FertilizerGrowthModifierAsset extends GrowthModifierAsset {
   @Nonnull
   public static final BuilderCodec<FertilizerGrowthModifierAsset> CODEC = BuilderCodec.builder(
         FertilizerGrowthModifierAsset.class, FertilizerGrowthModifierAsset::new, ABSTRACT_CODEC
      )
      .build();

   @Override
   public double getCurrentGrowthMultiplier(
      @Nonnull CommandBuffer<ChunkStore> commandBuffer,
      @Nonnull Ref<ChunkStore> sectionRef,
      @Nonnull Ref<ChunkStore> blockRef,
      int x,
      int y,
      int z,
      boolean initialTick
   ) {
      ChunkSection chunkSectionComponent = commandBuffer.getComponent(sectionRef, ChunkSection.getComponentType());

      assert chunkSectionComponent != null;

      Ref<ChunkStore> chunkRef = chunkSectionComponent.getChunkColumnReference();
      if (chunkRef != null && chunkRef.isValid()) {
         BlockComponentChunk blockComponentChunk = commandBuffer.getComponent(chunkRef, BlockComponentChunk.getComponentType());
         if (blockComponentChunk == null) {
            return 1.0;
         } else {
            int chunkIndexBelow = ChunkUtil.indexBlockInColumn(x, y - 1, z);
            Ref<ChunkStore> blockBelowRef = blockComponentChunk.getEntityReference(chunkIndexBelow);
            if (blockBelowRef != null && blockBelowRef.isValid()) {
               TilledSoilBlock belowTilledSoilComponent = commandBuffer.getComponent(blockBelowRef, TilledSoilBlock.getComponentType());
               return belowTilledSoilComponent != null && belowTilledSoilComponent.isFertilized()
                  ? super.getCurrentGrowthMultiplier(commandBuffer, sectionRef, blockRef, x, y, z, initialTick)
                  : 1.0;
            } else {
               return 1.0;
            }
         }
      } else {
         return 1.0;
      }
   }
}
