package com.hypixel.hytale.builtin.blocktick;

import com.hypixel.hytale.assetstore.map.BlockTypeAssetMap;
import com.hypixel.hytale.builtin.blocktick.procedure.BasicChanceBlockGrowthProcedure;
import com.hypixel.hytale.builtin.blocktick.procedure.SplitChanceBlockGrowthProcedure;
import com.hypixel.hytale.builtin.blocktick.system.ChunkBlockTickSystem;
import com.hypixel.hytale.builtin.blocktick.system.MergeWaitingBlocksSystem;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.event.EventPriority;
import com.hypixel.hytale.server.core.asset.type.blocktick.BlockTickManager;
import com.hypixel.hytale.server.core.asset.type.blocktick.IBlockTickProvider;
import com.hypixel.hytale.server.core.asset.type.blocktick.config.TickProcedure;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.ChunkColumn;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.events.ChunkPreLoadProcessEvent;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import javax.annotation.Nonnull;

public class BlockTickPlugin extends JavaPlugin implements IBlockTickProvider {
   private static BlockTickPlugin instance;

   public static BlockTickPlugin get() {
      return instance;
   }

   public BlockTickPlugin(@Nonnull JavaPluginInit init) {
      super(init);
      instance = this;
   }

   @Override
   protected void setup() {
      TickProcedure.CODEC.register("BasicChance", BasicChanceBlockGrowthProcedure.class, BasicChanceBlockGrowthProcedure.CODEC);
      TickProcedure.CODEC.register("SplitChance", SplitChanceBlockGrowthProcedure.class, SplitChanceBlockGrowthProcedure.CODEC);
      this.getEventRegistry().registerGlobal(EventPriority.EARLY, ChunkPreLoadProcessEvent.class, this::discoverTickingBlocks);
      ChunkStore.REGISTRY.registerSystem(new ChunkBlockTickSystem.PreTick());
      ChunkStore.REGISTRY.registerSystem(new ChunkBlockTickSystem.Ticking());
      ChunkStore.REGISTRY.registerSystem(new MergeWaitingBlocksSystem());
      BlockTickManager.setBlockTickProvider(this);
   }

   @Override
   public TickProcedure getTickProcedure(int blockId) {
      return BlockType.getAssetMap().getAsset(blockId).getTickProcedure();
   }

   private void discoverTickingBlocks(@Nonnull ChunkPreLoadProcessEvent event) {
      if (event.isNewlyGenerated()) {
         this.discoverTickingBlocks(event.getHolder(), event.getChunk());
      }
   }

   public int discoverTickingBlocks(@Nonnull Holder<ChunkStore> holder, @Nonnull WorldChunk worldChunk) {
      if (!this.isEnabled()) {
         return 0;
      } else {
         BlockChunk blockChunkComponent = worldChunk.getBlockChunk();
         if (blockChunkComponent != null && blockChunkComponent.consumeNeedsPhysics()) {
            ChunkColumn chunkColumnComponent = holder.getComponent(ChunkColumn.getComponentType());
            if (chunkColumnComponent == null) {
               return 0;
            } else {
               Holder<ChunkStore>[] sections = chunkColumnComponent.getSectionHolders();
               if (sections == null) {
                  return 0;
               } else {
                  BlockTypeAssetMap<String, BlockType> assetMap = BlockType.getAssetMap();
                  int count = 0;

                  for (int i = 0; i < sections.length; i++) {
                     Holder<ChunkStore> sectionHolder = sections[i];
                     BlockSection blockSectionComponent = sectionHolder.ensureAndGetComponent(BlockSection.getComponentType());
                     if (!blockSectionComponent.isSolidAir()) {
                        for (int blockIdx = 0; blockIdx < 32768; blockIdx++) {
                           int blockId = blockSectionComponent.get(blockIdx);
                           BlockType blockType = assetMap.getAsset(blockId);
                           if (blockType != null && blockType.getTickProcedure() != null) {
                              blockSectionComponent.setTicking(blockIdx, true);
                              blockChunkComponent.markNeedsSaving();
                              count++;
                           }
                        }
                     }
                  }

                  return count;
               }
            }
         } else {
            return 0;
         }
      }
   }
}
