package com.hypixel.hytale.server.npc.blackboard.view.blocktype;

import com.hypixel.hytale.math.random.RandomExtra;
import com.hypixel.hytale.server.core.modules.blockset.BlockSetModule;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.ChunkSectionReference;
import com.hypixel.hytale.server.core.universe.world.chunk.section.blockpositions.BlockPositionData;
import com.hypixel.hytale.server.core.universe.world.chunk.section.blockpositions.BlockPositionProvider;
import com.hypixel.hytale.server.core.universe.world.chunk.section.blockpositions.IBlockPositionData;
import com.hypixel.hytale.server.npc.NPCPlugin;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntConsumer;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.BitSet;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockPositionEntryGenerator {
   private final BlockPositionEntryGenerator.FoundBlockConsumer foundBlockConsumer = new BlockPositionEntryGenerator.FoundBlockConsumer();
   private final IntSet internalIdHolder = new IntOpenHashSet();

   @Nonnull
   public BlockPositionProvider generate(
      short changeCounter, int sectionIndex, @Nonnull BlockChunk chunk, IntList unifiedBlocksOfInterest, @Nonnull BitSet searchedBlockSets
   ) {
      BlockSection section = chunk.getSectionAtIndex(sectionIndex);
      if (section.isSolidAir()) {
         return new BlockPositionProvider(searchedBlockSets, null, changeCounter);
      } else if (!section.containsAny(unifiedBlocksOfInterest)) {
         return new BlockPositionProvider(searchedBlockSets, null, changeCounter);
      } else {
         ChunkSectionReference chunkSectionPointer = new ChunkSectionReference(chunk, section, sectionIndex);
         this.foundBlockConsumer.init(chunkSectionPointer, searchedBlockSets);
         section.find(unifiedBlocksOfInterest, this.internalIdHolder, this.foundBlockConsumer);
         this.internalIdHolder.clear();
         Int2ObjectOpenHashMap<List<IBlockPositionData>> blockData = this.foundBlockConsumer.getBlockData();
         this.foundBlockConsumer.release();
         return new BlockPositionProvider(searchedBlockSets, blockData, changeCounter);
      }
   }

   private static class FoundBlockConsumer implements IntConsumer {
      private final Int2IntMap blockSetCounts = new Int2IntOpenHashMap();
      @Nullable
      private ChunkSectionReference sectionPointer;
      @Nullable
      private BitSet searchedBlockSets;
      private int maxBlockType;
      @Nullable
      private Int2ObjectOpenHashMap<List<IBlockPositionData>> blockData;

      public void init(ChunkSectionReference sectionPointer, BitSet searchedBlockSets) {
         this.sectionPointer = sectionPointer;
         this.searchedBlockSets = searchedBlockSets;
         this.maxBlockType = NPCPlugin.get().getMaxBlackboardBlockCountPerType();
         this.blockData = new Int2ObjectOpenHashMap();
      }

      public void release() {
         this.blockSetCounts.clear();
         this.sectionPointer = null;
         this.searchedBlockSets = null;
         this.blockData = null;
      }

      public void accept(int blockIndex) {
         BlockSetModule blockSetModule = BlockSetModule.getInstance();
         int type = this.sectionPointer.getSection().get(blockIndex);
         BlockPositionData data = null;

         for (int i = this.searchedBlockSets.nextSetBit(0); i >= 0; i = this.searchedBlockSets.nextSetBit(i + 1)) {
            if (blockSetModule.blockInSet(i, type)) {
               List<IBlockPositionData> entry = (List<IBlockPositionData>)this.blockData.getOrDefault(i, null);
               if (entry == null) {
                  entry = new ObjectArrayList();
                  this.blockData.put(i, entry);
               }

               int count = this.blockSetCounts.getOrDefault(i, 0);
               if (count < this.maxBlockType) {
                  if (data == null) {
                     data = new BlockPositionData(blockIndex, this.sectionPointer, type);
                  }

                  entry.add(data);
               } else {
                  int j = RandomExtra.randomRange(count + 1);
                  if (j < this.maxBlockType) {
                     if (data == null) {
                        data = new BlockPositionData(blockIndex, this.sectionPointer, type);
                     }

                     entry.set(j, data);
                  }
               }

               this.blockSetCounts.put(i, count + 1);
            }

            if (i == Integer.MAX_VALUE) {
               break;
            }
         }
      }

      public Int2ObjectOpenHashMap<List<IBlockPositionData>> getBlockData() {
         this.blockData.trim();
         return this.blockData;
      }
   }
}
