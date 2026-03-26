package com.hypixel.hytale.builtin.blocktick;

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
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.function.IntConsumer;
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
         BlockChunk blockChunkComponent = holder.getComponent(BlockChunk.getComponentType());
         if (blockChunkComponent != null && blockChunkComponent.consumeNeedsPhysics()) {
            ChunkColumn chunkColumnComponent = holder.getComponent(ChunkColumn.getComponentType());
            if (chunkColumnComponent == null) {
               return 0;
            } else {
               Holder<ChunkStore>[] sections = chunkColumnComponent.getSectionHolders();
               if (sections == null) {
                  return 0;
               } else {
                  BlockTickPlugin.Preprocessor preprocessor = BlockTickPlugin.Preprocessor.LOCAL.get();
                  int count = 0;

                  for (int i = 0; i < sections.length; i++) {
                     Holder<ChunkStore> sectionHolder = sections[i];
                     BlockSection section = sectionHolder.ensureAndGetComponent(BlockSection.getComponentType());
                     if (!section.isSolidAir()) {
                        preprocessor.clear();
                        section.forEachValue(preprocessor.typeCollector);
                        if (!preprocessor.tickingIds.isEmpty()) {
                           section.find(preprocessor.tickingIds, preprocessor.indexCollector);
                           count += section.setTicking(preprocessor.tickingIndices, true);
                        }
                     }
                  }

                  if (count > 0) {
                     blockChunkComponent.markNeedsSaving();
                  }

                  return count;
               }
            }
         } else {
            return 0;
         }
      }
   }

   public static final class Preprocessor {
      public static final ThreadLocal<BlockTickPlugin.Preprocessor> LOCAL = ThreadLocal.withInitial(BlockTickPlugin.Preprocessor::new);
      public final IntList tickingIds = new IntArrayList();
      public final IntList tickingIndices = new IntArrayList();
      public final IntConsumer typeCollector = this::collectType;
      public final IntConsumer indexCollector = this::collectIndex;

      public void clear() {
         this.tickingIds.clear();
         this.tickingIndices.clear();
      }

      private void collectType(int value) {
         BlockType type = BlockType.getAssetMap().getAsset(value);
         if (type != null && type.getTickProcedure() != null) {
            this.tickingIds.add(value);
         }
      }

      private void collectIndex(int index) {
         this.tickingIndices.add(index);
      }
   }
}
