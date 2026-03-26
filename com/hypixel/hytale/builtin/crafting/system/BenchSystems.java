package com.hypixel.hytale.builtin.crafting.system;

import com.hypixel.hytale.builtin.crafting.component.BenchBlock;
import com.hypixel.hytale.builtin.crafting.component.CraftingManager;
import com.hypixel.hytale.builtin.crafting.component.ProcessingBenchBlock;
import com.hypixel.hytale.builtin.crafting.window.BenchWindow;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.bench.Bench;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.bench.ProcessingBench;
import com.hypixel.hytale.server.core.asset.type.item.config.CraftingRecipe;
import com.hypixel.hytale.server.core.entity.entities.player.windows.WindowManager;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.MaterialQuantity;
import com.hypixel.hytale.server.core.inventory.container.CombinedItemContainer;
import com.hypixel.hytale.server.core.inventory.container.TestRemoveItemSlotResult;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.accessor.BlockAccessor;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BenchSystems {
   private static void dropUpgradeItems(
      CommandBuffer<ChunkStore> commandBuffer, @Nullable BlockType type, BenchBlock benchBlock, int rotation, int x, int y, int z
   ) {
      ItemStack[] upgradeItems = benchBlock.getUpgradeItems();
      if (upgradeItems.length != 0) {
         World world = commandBuffer.getExternalData().getWorld();
         Store<EntityStore> entityStore = world.getEntityStore().getStore();
         Vector3d dropPosition = new Vector3d();
         if (type != null) {
            type.getBlockCenter(rotation, dropPosition);
         } else {
            dropPosition.assign(0.5, 0.5, 0.5);
         }

         dropPosition.add(x, y, z);
         Holder<EntityStore>[] itemEntityHolders = ItemComponent.generateItemDrops(entityStore, List.of(upgradeItems), dropPosition, Vector3f.ZERO);
         if (itemEntityHolders.length > 0) {
            world.execute(() -> entityStore.addEntities(itemEntityHolders, AddReason.SPAWN));
         }

         benchBlock.setUpgradeItems(ItemStack.EMPTY_ARRAY);
      }
   }

   public static class OnAddOrRemoved extends RefSystem<ChunkStore> {
      private final ComponentType<ChunkStore, BlockModule.BlockStateInfo> blockStateInfoComponentType = BlockModule.BlockStateInfo.getComponentType();
      private final ComponentType<ChunkStore, BenchBlock> benchBlockComponentType = BenchBlock.getComponentType();
      private final Query<ChunkStore> query = Query.and(this.benchBlockComponentType, this.blockStateInfoComponentType);

      @Override
      public void onEntityAdded(
         @Nonnull Ref<ChunkStore> ref, @Nonnull AddReason reason, @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer
      ) {
         BlockModule.BlockStateInfo blockStateInfoComponent = commandBuffer.getComponent(ref, this.blockStateInfoComponentType);

         assert blockStateInfoComponent != null;

         BenchBlock benchBlockComponent = commandBuffer.getComponent(ref, this.benchBlockComponentType);

         assert benchBlockComponent != null;

         Ref<ChunkStore> chunkRef = blockStateInfoComponent.getChunkRef();
         if (chunkRef.isValid()) {
            int index = blockStateInfoComponent.getIndex();
            int x = ChunkUtil.xFromBlockInColumn(index);
            int y = ChunkUtil.yFromBlockInColumn(index);
            int z = ChunkUtil.zFromBlockInColumn(index);
            BlockChunk blockChunkComponent = commandBuffer.getComponent(chunkRef, BlockChunk.getComponentType());

            assert blockChunkComponent != null;

            BlockChunk blockChunk = commandBuffer.getComponent(chunkRef, BlockChunk.getComponentType());

            assert blockChunk != null;

            BlockSection blockSection = blockChunkComponent.getSectionAtBlockY(y);
            int blockId = blockSection.get(x, y, z);
            BlockType blockType = BlockType.getAssetMap().getAsset(blockId);
            if (blockType != null && blockType.getBlockEntity() != null) {
               Bench bench = blockType.getBench();
               if (bench == null) {
                  BenchSystems.dropUpgradeItems(
                     commandBuffer,
                     blockType,
                     benchBlockComponent,
                     blockChunk.getSectionAtBlockY(y).getRotationIndex(x, y, z),
                     ChunkUtil.worldCoordFromLocalCoord(blockChunk.getX(), x),
                     y,
                     ChunkUtil.worldCoordFromLocalCoord(blockChunk.getZ(), z)
                  );
               }
            }
         }
      }

      @Override
      public void onEntityRemove(
         @Nonnull Ref<ChunkStore> ref, @Nonnull RemoveReason reason, @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer
      ) {
         if (reason != RemoveReason.UNLOAD) {
            BlockModule.BlockStateInfo blockStateInfoComponent = commandBuffer.getComponent(ref, this.blockStateInfoComponentType);

            assert blockStateInfoComponent != null;

            BenchBlock benchBlockComponent = commandBuffer.getComponent(ref, this.benchBlockComponentType);

            assert benchBlockComponent != null;

            Ref<ChunkStore> chunkRef = blockStateInfoComponent.getChunkRef();
            if (chunkRef.isValid()) {
               int index = blockStateInfoComponent.getIndex();
               int x = ChunkUtil.xFromBlockInColumn(index);
               int y = ChunkUtil.yFromBlockInColumn(index);
               int z = ChunkUtil.zFromBlockInColumn(index);
               BlockChunk blockChunkComponent = commandBuffer.getComponent(chunkRef, BlockChunk.getComponentType());

               assert blockChunkComponent != null;

               BlockChunk blockChunk = commandBuffer.getComponent(chunkRef, BlockChunk.getComponentType());

               assert blockChunk != null;

               WindowManager.closeAndRemoveAll(benchBlockComponent.getWindows());
               BenchSystems.dropUpgradeItems(
                  commandBuffer,
                  null,
                  benchBlockComponent,
                  0,
                  ChunkUtil.worldCoordFromLocalCoord(blockChunk.getX(), x),
                  y,
                  ChunkUtil.worldCoordFromLocalCoord(blockChunk.getZ(), z)
               );
            }
         }
      }

      @Nullable
      @Override
      public Query<ChunkStore> getQuery() {
         return this.query;
      }
   }

   public static class ProcessingBenchLifecycle extends RefSystem<ChunkStore> {
      private final ComponentType<ChunkStore, ProcessingBenchBlock> componentType;
      private final ComponentType<ChunkStore, BenchBlock> benchBlockComponentType;
      private final ComponentType<ChunkStore, BlockModule.BlockStateInfo> blockStateInfoComponentType = BlockModule.BlockStateInfo.getComponentType();

      public ProcessingBenchLifecycle(
         @Nonnull ComponentType<ChunkStore, ProcessingBenchBlock> componentType, @Nonnull ComponentType<ChunkStore, BenchBlock> benchBlockComponentType
      ) {
         this.componentType = componentType;
         this.benchBlockComponentType = benchBlockComponentType;
      }

      @Override
      public Query<ChunkStore> getQuery() {
         return this.componentType;
      }

      @Override
      public void onEntityAdded(
         @Nonnull Ref<ChunkStore> ref, @Nonnull AddReason reason, @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer
      ) {
         ProcessingBenchBlock processingBenchBlock = commandBuffer.getComponent(ref, this.componentType);
         BenchBlock benchBlock = commandBuffer.getComponent(ref, this.benchBlockComponentType);
         BlockModule.BlockStateInfo blockStateInfo = commandBuffer.getComponent(ref, this.blockStateInfoComponentType);
         if (processingBenchBlock != null && benchBlock != null && blockStateInfo != null) {
            Ref<ChunkStore> chunkRef = blockStateInfo.getChunkRef();
            if (chunkRef.isValid()) {
               BlockChunk blockChunk = commandBuffer.getComponent(chunkRef, BlockChunk.getComponentType());
               if (blockChunk != null) {
                  int blockIndex = blockStateInfo.getIndex();
                  int localX = ChunkUtil.xFromBlockInColumn(blockIndex);
                  int localY = ChunkUtil.yFromBlockInColumn(blockIndex);
                  int localZ = ChunkUtil.zFromBlockInColumn(blockIndex);
                  int blockX = ChunkUtil.worldCoordFromLocalCoord(blockChunk.getX(), localX);
                  int blockZ = ChunkUtil.worldCoordFromLocalCoord(blockChunk.getZ(), localZ);
                  BlockSection blockSection = blockChunk.getSectionAtBlockY(localY);
                  int blockId = blockSection.get(localX, localY, localZ);
                  BlockType blockType = BlockType.getAssetMap().getAsset(blockId);
                  if (blockType != null) {
                     int rotationIndex = blockSection.getRotationIndex(localX, localY, localZ);
                     if (processingBenchBlock.initializeBenchConfig(blockType)) {
                        World world = commandBuffer.getExternalData().getWorld();
                        processingBenchBlock.setupSlots(world, benchBlock, blockStateInfo, blockX, localY, blockZ, blockType, rotationIndex);
                     }
                  }
               }
            }
         }
      }

      @Override
      public void onEntityRemove(
         @Nonnull Ref<ChunkStore> ref, @Nonnull RemoveReason reason, @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer
      ) {
         if (reason == RemoveReason.UNLOAD) {
            BlockModule.BlockStateInfo blockStateInfo = commandBuffer.getComponent(ref, this.blockStateInfoComponentType);
            if (blockStateInfo != null) {
               blockStateInfo.markNeedsSaving();
            }
         } else {
            ProcessingBenchBlock processingBenchBlock = commandBuffer.getComponent(ref, this.componentType);
            if (processingBenchBlock != null) {
               BlockModule.BlockStateInfo blockStateInfo = commandBuffer.getComponent(ref, this.blockStateInfoComponentType);
               if (blockStateInfo != null) {
                  Ref<ChunkStore> chunkRef = blockStateInfo.getChunkRef();
                  if (chunkRef.isValid()) {
                     BlockChunk blockChunk = commandBuffer.getComponent(chunkRef, BlockChunk.getComponentType());
                     if (blockChunk != null) {
                        int blockIndex = blockStateInfo.getIndex();
                        int localX = ChunkUtil.xFromBlockInColumn(blockIndex);
                        int localY = ChunkUtil.yFromBlockInColumn(blockIndex);
                        int localZ = ChunkUtil.zFromBlockInColumn(blockIndex);
                        int blockX = ChunkUtil.worldCoordFromLocalCoord(blockChunk.getX(), localX);
                        int blockZ = ChunkUtil.worldCoordFromLocalCoord(blockChunk.getZ(), localZ);
                        CombinedItemContainer combinedItemContainer = processingBenchBlock.getItemContainer();
                        if (combinedItemContainer != null) {
                           List<ItemStack> itemStacks = combinedItemContainer.dropAllItemStacks();
                           processingBenchBlock.dropFuelItems(itemStacks);
                           World world = commandBuffer.getExternalData().getWorld();
                           Store<EntityStore> entityStore = world.getEntityStore().getStore();
                           Vector3d dropPosition = new Vector3d(blockX + 0.5, localY, blockZ + 0.5);
                           Holder<EntityStore>[] itemEntityHolders = ItemComponent.generateItemDrops(entityStore, itemStacks, dropPosition, Vector3f.ZERO);
                           if (itemEntityHolders.length > 0) {
                              world.execute(() -> entityStore.addEntities(itemEntityHolders, AddReason.SPAWN));
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public static class ProcessingBenchTick extends EntityTickingSystem<ChunkStore> {
      private final ComponentType<ChunkStore, ProcessingBenchBlock> componentType;
      private final ComponentType<ChunkStore, BenchBlock> benchBlockComponentType;
      private final ComponentType<ChunkStore, BlockModule.BlockStateInfo> blockStateInfoComponentType = BlockModule.BlockStateInfo.getComponentType();

      public ProcessingBenchTick(
         @Nonnull ComponentType<ChunkStore, ProcessingBenchBlock> componentType, @Nonnull ComponentType<ChunkStore, BenchBlock> benchBlockComponentType
      ) {
         this.componentType = componentType;
         this.benchBlockComponentType = benchBlockComponentType;
      }

      @Override
      public Query<ChunkStore> getQuery() {
         return this.componentType;
      }

      @Override
      public void tick(
         float dt,
         int index,
         @Nonnull ArchetypeChunk<ChunkStore> archetypeChunk,
         @Nonnull Store<ChunkStore> store,
         @Nonnull CommandBuffer<ChunkStore> commandBuffer
      ) {
         ProcessingBenchBlock processingBenchBlock = archetypeChunk.getComponent(index, this.componentType);
         BenchBlock benchBlock = archetypeChunk.getComponent(index, this.benchBlockComponentType);
         BlockModule.BlockStateInfo blockStateInfo = archetypeChunk.getComponent(index, this.blockStateInfoComponentType);
         if (benchBlock != null && blockStateInfo != null) {
            Ref<ChunkStore> chunkRef = blockStateInfo.getChunkRef();
            if (chunkRef.isValid()) {
               BlockChunk blockChunk = store.getComponent(chunkRef, BlockChunk.getComponentType());
               if (blockChunk != null) {
                  int blockIndex = blockStateInfo.getIndex();
                  int localX = ChunkUtil.xFromBlockInColumn(blockIndex);
                  int localY = ChunkUtil.yFromBlockInColumn(blockIndex);
                  int localZ = ChunkUtil.zFromBlockInColumn(blockIndex);
                  int blockX = ChunkUtil.worldCoordFromLocalCoord(blockChunk.getX(), localX);
                  int blockZ = ChunkUtil.worldCoordFromLocalCoord(blockChunk.getZ(), localZ);
                  BlockSection blockSection = blockChunk.getSectionAtBlockY(localY);
                  int blockId = blockSection.get(localX, localY, localZ);
                  BlockType blockType = BlockType.getAssetMap().getAsset(blockId);
                  if (blockType != null) {
                     int rotationIndex = blockSection.getRotationIndex(localX, localY, localZ);
                     World world = store.getExternalData().getWorld();
                     Store<EntityStore> entityStore = world.getEntityStore().getStore();
                     ProcessingBench processingBench = processingBenchBlock.getProcessingBench();
                     if (processingBench != null) {
                        Instant currentGameTime = entityStore.getResource(WorldTimeResource.getResourceType()).getGameTime();
                        float effectiveDt;
                        if (processingBenchBlock.getLastTickGameTime() != null
                           && currentGameTime != null
                           && !currentGameTime.equals(processingBenchBlock.getLastTickGameTime())) {
                           float gameElapsedSeconds = (float)Math.max(
                                 0L, currentGameTime.toEpochMilli() - processingBenchBlock.getLastTickGameTime().toEpochMilli()
                              )
                              / 1000.0F;
                           effectiveDt = (float)(gameElapsedSeconds / WorldTimeResource.getSecondsPerTick(world));
                        } else {
                           effectiveDt = 0.0F;
                        }

                        String currentState = BlockAccessor.getCurrentInteractionState(blockType);
                        Map<UUID, BenchWindow> windows = benchBlock.getWindows();
                        processingBenchBlock.getProcessingSlots().clear();
                        processingBenchBlock.checkForRecipeUpdate(benchBlock);
                        boolean hasFuelSlots = processingBench.getFuel() != null;
                        boolean canProcess = false;
                        CraftingRecipe recipe = processingBenchBlock.getRecipe();
                        if (recipe != null) {
                           List<ItemStack> outputItemStacks = CraftingManager.getOutputItemStacks(recipe);
                           if (!processingBenchBlock.getOutputContainer().canAddItemStacks(outputItemStacks, false, false)) {
                              if ("Processing".equals(currentState)) {
                                 processingBenchBlock.setBlockInteractionState("default", blockType, world, blockX, localY, blockZ);
                                 processingBenchBlock.playSound(
                                    processingBench.getFailedSoundEventIndex(), entityStore, blockType, rotationIndex, blockX, localY, blockZ
                                 );
                              } else if ("ProcessCompleted".equals(currentState)) {
                                 processingBenchBlock.setBlockInteractionState("default", blockType, world, blockX, localY, blockZ);
                                 processingBenchBlock.playSound(
                                    processingBench.getEndSoundEventIndex(), entityStore, blockType, rotationIndex, blockX, localY, blockZ
                                 );
                              }

                              processingBenchBlock.setActive(false, benchBlock, blockStateInfo);
                              processingBenchBlock.setLastTickGameTime(currentGameTime);
                              return;
                           }

                           List<MaterialQuantity> inputMaterials = CraftingManager.getInputMaterials(recipe);
                           List<TestRemoveItemSlotResult> result = processingBenchBlock.getInputContainer()
                              .getSlotMaterialsToRemove(inputMaterials, true, true);
                           if (result.isEmpty()) {
                              if ("Processing".equals(currentState)) {
                                 processingBenchBlock.setBlockInteractionState("default", blockType, world, blockX, localY, blockZ);
                                 processingBenchBlock.playSound(
                                    processingBench.getFailedSoundEventIndex(), entityStore, blockType, rotationIndex, blockX, localY, blockZ
                                 );
                              } else if ("ProcessCompleted".equals(currentState)) {
                                 processingBenchBlock.setBlockInteractionState("default", blockType, world, blockX, localY, blockZ);
                                 processingBenchBlock.playSound(
                                    processingBench.getEndSoundEventIndex(), entityStore, blockType, rotationIndex, blockX, localY, blockZ
                                 );
                              }

                              processingBenchBlock.setInputProgress(0.0F);
                              processingBenchBlock.setActive(false, benchBlock, blockStateInfo);
                              processingBenchBlock.clearCurrentRecipe();
                              processingBenchBlock.setLastTickGameTime(currentGameTime);
                              return;
                           }

                           for (TestRemoveItemSlotResult item : result) {
                              processingBenchBlock.getProcessingSlots().addAll(item.getPickedSlots());
                           }

                           processingBenchBlock.sendProcessingSlots(windows);
                           canProcess = true;
                        } else {
                           if (!hasFuelSlots) {
                              if ("Processing".equals(currentState)) {
                                 processingBenchBlock.setBlockInteractionState("default", blockType, world, blockX, localY, blockZ);
                                 processingBenchBlock.playSound(
                                    processingBench.getFailedSoundEventIndex(), entityStore, blockType, rotationIndex, blockX, localY, blockZ
                                 );
                              } else if ("ProcessCompleted".equals(currentState)) {
                                 processingBenchBlock.setBlockInteractionState("default", blockType, world, blockX, localY, blockZ);
                                 processingBenchBlock.playSound(
                                    processingBench.getEndSoundEventIndex(), entityStore, blockType, rotationIndex, blockX, localY, blockZ
                                 );
                              }

                              processingBenchBlock.setLastTickGameTime(currentGameTime);
                              return;
                           }

                           boolean allowNoInputProcessing = processingBench.shouldAllowNoInputProcessing();
                           if (!allowNoInputProcessing && "Processing".equals(currentState)) {
                              processingBenchBlock.setBlockInteractionState("default", blockType, world, blockX, localY, blockZ);
                              processingBenchBlock.playSound(
                                 processingBench.getFailedSoundEventIndex(), entityStore, blockType, rotationIndex, blockX, localY, blockZ
                              );
                           } else if ("ProcessCompleted".equals(currentState)) {
                              processingBenchBlock.setBlockInteractionState("default", blockType, world, blockX, localY, blockZ);
                              processingBenchBlock.playSound(
                                 processingBench.getEndSoundEventIndex(), entityStore, blockType, rotationIndex, blockX, localY, blockZ
                              );
                              processingBenchBlock.setActive(false, benchBlock, blockStateInfo);
                              processingBenchBlock.sendProgress(0.0F, windows);
                              processingBenchBlock.setLastTickGameTime(currentGameTime);
                              return;
                           }

                           processingBenchBlock.sendProgress(0.0F, windows);
                           if (!allowNoInputProcessing) {
                              processingBenchBlock.setActive(false, benchBlock, blockStateInfo);
                              processingBenchBlock.setLastTickGameTime(currentGameTime);
                              return;
                           }
                        }

                        int completions = processingBenchBlock.advanceProcessing(
                           effectiveDt, entityStore, benchBlock, blockStateInfo, blockX, localY, blockZ, blockType, rotationIndex
                        );
                        if (!canProcess && processingBenchBlock.isActive()) {
                           processingBenchBlock.consumeFuelForDuration(effectiveDt, entityStore, blockX, localY, blockZ, blockType, rotationIndex);
                        }

                        processingBenchBlock.getProcessingFuelSlots().clear();
                        if (hasFuelSlots) {
                           ProcessingBench.ProcessingSlot[] fuelSlots = processingBench.getFuel();
                           if (processingBenchBlock.isActive() && processingBenchBlock.getFuelTime() > 0.0F && fuelSlots != null) {
                              for (int i = 0; i < fuelSlots.length; i++) {
                                 if (processingBenchBlock.getFuelContainer().getItemStack((short)i) != null) {
                                    processingBenchBlock.getProcessingFuelSlots().add((short)i);
                                    break;
                                 }
                              }
                           }

                           if (!processingBenchBlock.isActive() || processingBenchBlock.getFuelTime() <= 0.0F) {
                              processingBenchBlock.setLastConsumedFuelTotal(0);
                              if ("Processing".equals(currentState) || "ProcessCompleted".equals(currentState)) {
                                 processingBenchBlock.setBlockInteractionState("default", blockType, world, blockX, localY, blockZ);
                                 processingBenchBlock.playSound(
                                    processingBench.getFailedSoundEventIndex(), entityStore, blockType, rotationIndex, blockX, localY, blockZ
                                 );
                                 processingBenchBlock.setActive(false, benchBlock, blockStateInfo);
                              }

                              processingBenchBlock.setLastTickGameTime(currentGameTime);
                              return;
                           }

                           processingBenchBlock.updateFuelValues(windows);
                        }

                        if (completions > 0) {
                           processingBenchBlock.setBlockInteractionState("ProcessCompleted", blockType, world, blockX, localY, blockZ);
                           processingBenchBlock.playSound(
                              processingBenchBlock.getBench().getCompletedSoundEventIndex(), entityStore, blockType, rotationIndex, blockX, localY, blockZ
                           );
                        } else {
                           if (!"Processing".equals(currentState)) {
                              processingBenchBlock.setBlockInteractionState("Processing", blockType, world, blockX, localY, blockZ);
                           }

                           if (canProcess) {
                              int tierLevel = benchBlock.getTierLevel();
                              float recipeTime = processingBenchBlock.getRecipeTimeSeconds(tierLevel);
                              if (processingBenchBlock.getRecipe() != null && recipeTime > 0.0F) {
                                 processingBenchBlock.sendProgress(processingBenchBlock.getInputProgress() / recipeTime, windows);
                              } else {
                                 processingBenchBlock.sendProgress(0.0F, windows);
                              }
                           }
                        }

                        processingBenchBlock.setLastTickGameTime(currentGameTime);
                     }
                  }
               }
            }
         }
      }
   }
}
