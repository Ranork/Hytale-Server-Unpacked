package com.hypixel.hytale.server.npc.blackboard.view.blocktype;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.random.RandomExtra;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.modules.blockset.BlockSetModule;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.blockpositions.BlockPositionProvider;
import com.hypixel.hytale.server.core.universe.world.chunk.section.blockpositions.IBlockPositionData;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.blackboard.Blackboard;
import com.hypixel.hytale.server.npc.blackboard.view.BlockRegionView;
import com.hypixel.hytale.server.npc.blackboard.view.resource.ResourceView;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockTypeView extends BlockRegionView<BlockTypeView> {
   private final long index;
   private final Blackboard blackboard;
   private final BitSet allBlockSets = new BitSet();
   private final Set<Ref<EntityStore>> entities = new HashSet<>();
   private final IntArrayList blockSetAggregate = new IntArrayList();
   private final IntArrayList crossViewBlockSetAggregate = new IntArrayList();
   private boolean aggregateNeedsRebuild;
   private final Int2IntMap blockSetCounts = new Int2IntOpenHashMap();
   private final List<IBlockPositionData> foundBlocks = new ObjectArrayList();
   private final BlockPositionEntryGenerator generator;
   private final BiPredicate<IBlockPositionData, ResourceView> reservedBlockFilter = (data, view) -> view.isBlockReserved(data.getX(), data.getY(), data.getZ());

   public BlockTypeView(long index, Blackboard blackboard, BlockPositionEntryGenerator generator) {
      this.index = index;
      this.blackboard = blackboard;
      this.generator = generator;
   }

   public long getIndex() {
      return this.index;
   }

   @Override
   public boolean isOutdated(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
      TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());

      assert transformComponent != null;

      return indexViewFromWorldPosition(transformComponent.getPosition()) != this.index;
   }

   @Nonnull
   public BlockTypeView getUpdatedView(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      Blackboard blackBoardResource = componentAccessor.getResource(Blackboard.getResourceType());
      BlockTypeView blockTypeView = blackBoardResource.getView(BlockTypeView.class, ref, componentAccessor);
      NPCEntity npcComponent = componentAccessor.getComponent(ref, NPCEntity.getComponentType());

      assert npcComponent != null;

      IntList blockSets = npcComponent.getBlackboardBlockTypeSets();
      this.removeSearchedBlockSets(ref, npcComponent, blockSets);
      blockTypeView.addSearchedBlockSets(ref, npcComponent, blockSets);
      return blockTypeView;
   }

   @Override
   public void initialiseEntity(@Nonnull Ref<EntityStore> ref, @Nonnull NPCEntity npcComponent) {
      this.addSearchedBlockSets(ref, npcComponent, npcComponent.getBlackboardBlockTypeSets());
   }

   @Override
   public void cleanup() {
   }

   @Override
   public void onWorldRemoved() {
   }

   public void addSearchedBlockSets(@Nonnull Ref<EntityStore> ref, @Nonnull NPCEntity entity, @Nonnull IntList blockSets) {
      HytaleLogger.Api context = Blackboard.LOGGER.at(Level.FINEST);
      if (context.isEnabled()) {
         context.log(
            "Registering new entity %s (reference:%s) with partial blackboard view %s, %s",
            entity.getRoleName(),
            ref,
            xOfViewIndex(this.index),
            zOfViewIndex(this.index)
         );
      }

      this.entities.add(ref);

      for (int i = 0; i < blockSets.size(); i++) {
         this.addSearchedBlockSet(blockSets.getInt(i));
      }
   }

   private void addSearchedBlockSet(int blockSet) {
      int existingCount = this.blockSetCounts.getOrDefault(blockSet, 0);
      if (existingCount == 0) {
         this.allBlockSets.set(blockSet);
         this.aggregateNeedsRebuild = true;
      }

      this.blockSetCounts.put(blockSet, existingCount + 1);
   }

   public void removeSearchedBlockSets(@Nonnull Ref<EntityStore> ref, @Nonnull NPCEntity npcComponent, @Nonnull IntList blockSets) {
      if (!this.entities.remove(ref)) {
         throw new IllegalStateException(
            String.format(
               "Attempting to unregister entity %s (reference:%s) from partial blackboard view %s at %s, %s when not registered",
               npcComponent.getRoleName(),
               ref,
               this.index,
               xOfViewIndex(this.index),
               zOfViewIndex(this.index)
            )
         );
      } else {
         HytaleLogger.Api context = Blackboard.LOGGER.at(Level.FINEST);
         if (context.isEnabled()) {
            context.log(
               "Unregistering entity %s (reference:%s) from partial blackboard view %s, %s",
               npcComponent.getRoleName(),
               ref,
               xOfViewIndex(this.index),
               zOfViewIndex(this.index)
            );
         }

         for (int i = 0; i < blockSets.size(); i++) {
            this.removeSearchedBlockSet(blockSets.getInt(i));
         }
      }
   }

   private void removeSearchedBlockSet(int blockSet) {
      int newCount = this.blockSetCounts.getOrDefault(blockSet, 0) - 1;
      if (newCount < 0) {
         throw new IllegalStateException(
            String.format(
               "Attempting to unregister blockset %s from partial blackboard view %s at %s, %s when not registered",
               blockSet,
               this.index,
               xOfViewIndex(this.index),
               zOfViewIndex(this.index)
            )
         );
      } else {
         if (newCount == 0) {
            this.allBlockSets.clear(blockSet);
            this.aggregateNeedsRebuild = true;
         }

         this.blockSetCounts.put(blockSet, newCount);
      }
   }

   @Nullable
   public IBlockPositionData findBlock(
      int blockSet, double range, double yMax, boolean pickRandom, @Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      TransformComponent transformComponent = componentAccessor.getComponent(ref, TransformComponent.getComponentType());

      assert transformComponent != null;

      Vector3d entityPos = transformComponent.getPosition();
      NPCEntity npcComponent = componentAccessor.getComponent(ref, NPCEntity.getComponentType());

      assert npcComponent != null;

      String roleName = npcComponent.getRoleName();
      int entityX = MathUtil.floor(entityPos.x);
      int entityZ = MathUtil.floor(entityPos.z);
      int entityY = MathUtil.floor(entityPos.y);
      World world = componentAccessor.getExternalData().getWorld();
      int maxRange = MathUtil.ceil(range);
      int yRange = MathUtil.ceil(yMax);
      int minY = MathUtil.clamp(entityY - yRange & -32, 0, 320);
      int maxY = MathUtil.clamp(entityY + yRange, 0, 320);
      BitSet clonedBitSet = null;
      ChunkStore chunkStore = componentAccessor.getExternalData().getWorld().getChunkStore();
      Store<ChunkStore> chunkStoreStore = chunkStore.getStore();

      for (int x = entityX - maxRange & -32; x < entityX + maxRange; x += 32) {
         for (int z = entityZ - maxRange & -32; z < entityZ + maxRange; z += 32) {
            WorldChunk chunk = world.getChunkIfInMemory(ChunkUtil.indexChunkFromBlock(x, z));
            if (chunk != null) {
               long chunkIndex = chunk.getIndex();
               BlockChunk blockChunk = chunk.getBlockChunk();

               for (int y = minY; y < maxY; y += 32) {
                  int sectionIndex = ChunkUtil.indexSection(y);
                  BlockSection section = blockChunk.getSectionAtIndex(sectionIndex);
                  Ref<ChunkStore> sectionRef = chunkStore.getChunkSectionReference(ChunkUtil.chunkCoordinate(x), sectionIndex, ChunkUtil.chunkCoordinate(z));
                  if (sectionRef != null) {
                     BlockPositionProvider entry = chunkStoreStore.getComponent(sectionRef, BlockPositionProvider.getComponentType());
                     if (entry == null || entry.isStale(blockSet, section)) {
                        short changeCounter = section.getLocalChangeCounter();
                        if (indexViewFromChunkCoordinates(x, z) == this.index) {
                           if (this.aggregateNeedsRebuild) {
                              HytaleLogger.Api context = Blackboard.LOGGER.at(Level.FINEST);
                              if (context.isEnabled()) {
                                 context.log(
                                    "Rebuilding blocktype aggregate in partial blackboard view %s, %s with %s blocksets",
                                    xOfViewIndex(this.index),
                                    zOfViewIndex(this.index),
                                    this.allBlockSets.cardinality()
                                 );
                              }

                              this.aggregateNeedsRebuild = false;
                              rebuildBlockTypeAggregate(this.blockSetAggregate, this.allBlockSets);
                           }

                           HytaleLogger.Api context = Blackboard.LOGGER.at(Level.FINEST);
                           if (context.isEnabled()) {
                              context.log(
                                 "Entity %s (reference:%s) generating new entry for chunk %s section %s in view %s, %s",
                                 roleName,
                                 ref,
                                 chunkIndex,
                                 sectionIndex,
                                 xOfViewIndex(this.index),
                                 zOfViewIndex(this.index)
                              );
                           }

                           if (clonedBitSet == null) {
                              clonedBitSet = (BitSet)this.allBlockSets.clone();
                           }

                           entry = this.generator.generate(changeCounter, sectionIndex, blockChunk, this.blockSetAggregate, clonedBitSet);
                        } else {
                           HytaleLogger.Api contextx = Blackboard.LOGGER.at(Level.FINEST);
                           BitSet combinedClonedBlockSets;
                           if (entry != null) {
                              if (contextx.isEnabled()) {
                                 contextx.log(
                                    "Entity %s (reference:%s) generating new entry for chunk %s section %s across border using existing entry",
                                    roleName,
                                    ref,
                                    chunkIndex,
                                    sectionIndex
                                 );
                              }

                              combinedClonedBlockSets = entry.getSearchedBlockSets();
                           } else {
                              if (contextx.isEnabled()) {
                                 contextx.log(
                                    "Entity %s (reference:%s) generating new entry for chunk %s section %s across border",
                                    roleName,
                                    ref,
                                    chunkIndex,
                                    sectionIndex
                                 );
                              }

                              BlockTypeView otherView = this.blackboard.getView(BlockTypeView.class, x, z);
                              if (otherView != null) {
                                 combinedClonedBlockSets = (BitSet)otherView.allBlockSets.clone();
                              } else {
                                 combinedClonedBlockSets = new BitSet();
                              }
                           }

                           combinedClonedBlockSets.or(this.allBlockSets);
                           rebuildBlockTypeAggregate(this.crossViewBlockSetAggregate, combinedClonedBlockSets);
                           entry = this.generator.generate(changeCounter, sectionIndex, blockChunk, this.crossViewBlockSetAggregate, combinedClonedBlockSets);
                        }

                        chunkStoreStore.putComponent(sectionRef, BlockPositionProvider.getComponentType(), entry);
                     }

                     ResourceView resourceView = this.blackboard.getIfExists(ResourceView.class, indexViewFromChunkCoordinates(x, z));
                     entry.findBlocks(
                        this.foundBlocks, blockSet, range, yRange, ref, resourceView != null ? this.reservedBlockFilter : null, resourceView, componentAccessor
                     );
                  }
               }
            }
         }
      }

      if (this.foundBlocks.isEmpty()) {
         return null;
      } else {
         IBlockPositionData data = null;
         if (pickRandom) {
            data = RandomExtra.randomElement(this.foundBlocks);
         } else {
            double minDist2 = Double.MAX_VALUE;

            for (int i = 0; i < this.foundBlocks.size(); i++) {
               IBlockPositionData block = this.foundBlocks.get(i);
               double dist2 = entityPos.distanceSquaredTo(block.getXCentre(), block.getYCentre(), block.getZCentre());
               if (dist2 < minDist2) {
                  minDist2 = dist2;
                  data = block;
               }
            }
         }

         this.foundBlocks.clear();
         return data;
      }
   }

   @Nonnull
   public Set<Ref<EntityStore>> getEntities() {
      return this.entities;
   }

   @Nonnull
   public BitSet getAllBlockSets() {
      return this.allBlockSets;
   }

   @Nonnull
   public Int2IntMap getBlockSetCounts() {
      return this.blockSetCounts;
   }

   private static void rebuildBlockTypeAggregate(@Nonnull IntArrayList aggregate, @Nonnull BitSet searchedBlockSets) {
      aggregate.clear();
      Int2ObjectMap<IntSet> blockSets = BlockSetModule.getInstance().getBlockSets();

      for (int i = searchedBlockSets.nextSetBit(0); i >= 0; i = searchedBlockSets.nextSetBit(i + 1)) {
         IntSet set = (IntSet)blockSets.get(i);
         IntIterator iterator = set.iterator();

         while (iterator.hasNext()) {
            int next = iterator.nextInt();
            aggregate.add(next);
         }

         if (i == Integer.MAX_VALUE) {
            break;
         }
      }
   }
}
