package com.hypixel.hytale.server.core.modules.block.system;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.component.system.EntityHolderEventSystem;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.event.EventPriority;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.entities.player.windows.WindowManager;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.block.BlockReplaceEvent;
import com.hypixel.hytale.server.core.modules.block.components.ItemContainerBlock;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemContainerSystems {
   public static class OnAddedOrRemoved extends RefSystem<ChunkStore> {
      private final ComponentType<ChunkStore, BlockModule.BlockStateInfo> blockStateInfoComponentType = BlockModule.BlockStateInfo.getComponentType();
      private final ComponentType<ChunkStore, ItemContainerBlock> itemContainerBlockComponentType = ItemContainerBlock.getComponentType();
      private final Query<ChunkStore> query = Query.and(this.itemContainerBlockComponentType, this.blockStateInfoComponentType);

      @Override
      public void onEntityAdded(
         @Nonnull Ref<ChunkStore> ref, @Nonnull AddReason reason, @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer
      ) {
         BlockModule.BlockStateInfo blockStateInfoComponent = commandBuffer.getComponent(ref, this.blockStateInfoComponentType);

         assert blockStateInfoComponent != null;

         ItemContainerBlock itemContainerComponent = commandBuffer.getComponent(ref, this.itemContainerBlockComponentType);

         assert itemContainerComponent != null;

         Ref<ChunkStore> chunkRef = blockStateInfoComponent.getChunkRef();
         if (chunkRef.isValid()) {
            int index = blockStateInfoComponent.getIndex();
            int x = ChunkUtil.xFromBlockInColumn(index);
            int y = ChunkUtil.yFromBlockInColumn(index);
            int z = ChunkUtil.zFromBlockInColumn(index);
            BlockChunk blockChunkComponent = commandBuffer.getComponent(chunkRef, BlockChunk.getComponentType());

            assert blockChunkComponent != null;

            BlockSection blockSection = blockChunkComponent.getSectionAtBlockY(y);
            int blockId = blockSection.get(x, y, z);
            BlockType blockType = BlockType.getAssetMap().getAsset(blockId);
            if (blockType != null && blockType.getBlockEntity() != null) {
               short capacity = itemContainerComponent.getCapacity();
               ItemContainerBlock assetConfig = blockType.getBlockEntity().getComponent(this.itemContainerBlockComponentType);
               if (assetConfig != null) {
                  capacity = assetConfig.getCapacity();
               }

               List<ItemStack> remainder = new ObjectArrayList();
               SimpleItemContainer itemContainer = itemContainerComponent.getItemContainer();
               itemContainer = ItemContainer.ensureContainerCapacity(itemContainer, capacity, SimpleItemContainer::new, remainder);
               World world = store.getExternalData().getWorld();
               itemContainer.registerChangeEvent(EventPriority.LAST, itemContainerChangeEvent -> {
                  if (world.isInThread()) {
                     blockStateInfoComponent.markNeedsSaving();
                  } else {
                     world.execute(blockStateInfoComponent::markNeedsSaving);
                  }
               });
               itemContainerComponent.setItemContainer(itemContainer);
               if (!remainder.isEmpty()) {
                  Store<EntityStore> entityStore = world.getEntityStore().getStore();
                  Vector3d blockPosition = new Vector3d(
                     ChunkUtil.worldCoordFromLocalCoord(blockChunkComponent.getX(), x), y, ChunkUtil.worldCoordFromLocalCoord(blockChunkComponent.getZ(), z)
                  );
                  Holder<EntityStore>[] itemEntityHolders = ItemComponent.generateItemDrops(entityStore, remainder, blockPosition, Vector3f.ZERO);
                  entityStore.addEntities(itemEntityHolders, AddReason.SPAWN);
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

            ItemContainerBlock itemContainerComponent = commandBuffer.getComponent(ref, this.itemContainerBlockComponentType);

            assert itemContainerComponent != null;

            WindowManager.closeAndRemoveAll(itemContainerComponent.getWindows());
            Ref<ChunkStore> chunkRef = blockStateInfoComponent.getChunkRef();
            if (chunkRef.isValid()) {
               int index = blockStateInfoComponent.getIndex();
               int x = ChunkUtil.xFromBlockInColumn(index);
               int y = ChunkUtil.yFromBlockInColumn(index);
               int z = ChunkUtil.zFromBlockInColumn(index);
               BlockChunk blockChunkComponent = commandBuffer.getComponent(chunkRef, BlockChunk.getComponentType());

               assert blockChunkComponent != null;

               World world = store.getExternalData().getWorld();
               Store<EntityStore> entityStore = world.getEntityStore().getStore();
               Vector3d blockPosition = new Vector3d(
                  ChunkUtil.worldCoordFromLocalCoord(blockChunkComponent.getX(), x), y, ChunkUtil.worldCoordFromLocalCoord(blockChunkComponent.getZ(), z)
               );
               List<ItemStack> allItemStacks = itemContainerComponent.getItemContainer().dropAllItemStacks();
               Vector3d dropPosition = blockPosition.add(0.5, 0.0, 0.5);
               Holder<EntityStore>[] itemEntityHolders = ItemComponent.generateItemDrops(entityStore, allItemStacks, dropPosition, Vector3f.ZERO);
               if (itemEntityHolders.length > 0) {
                  world.execute(() -> entityStore.addEntities(itemEntityHolders, AddReason.SPAWN));
               }
            }
         }
      }

      @Nullable
      @Override
      public Query<ChunkStore> getQuery() {
         return this.query;
      }
   }

   public static class OnReplaced extends EntityEventSystem<ChunkStore, BlockReplaceEvent> {
      private final ComponentType<ChunkStore, ItemContainerBlock> itemContainerBlockComponentType = ItemContainerBlock.getComponentType();

      public OnReplaced() {
         super(BlockReplaceEvent.class);
      }

      public void handle(
         int index,
         @Nonnull ArchetypeChunk<ChunkStore> archetypeChunk,
         @Nonnull Store<ChunkStore> store,
         @Nonnull CommandBuffer<ChunkStore> commandBuffer,
         @Nonnull BlockReplaceEvent event
      ) {
         ItemContainerBlock otherContainer = event.getNewEntity().getComponent(this.itemContainerBlockComponentType);
         if (otherContainer != null) {
            ItemContainerBlock selfContainer = archetypeChunk.getComponent(index, this.itemContainerBlockComponentType);

            assert selfContainer != null;

            selfContainer.getItemContainer().moveAllItemStacksTo(otherContainer.getItemContainer());
         }
      }

      @Nullable
      @Override
      public Query<ChunkStore> getQuery() {
         return this.itemContainerBlockComponentType;
      }
   }

   public static class OnReplacedHolder extends EntityHolderEventSystem<ChunkStore, BlockReplaceEvent> {
      private final ComponentType<ChunkStore, ItemContainerBlock> itemContainerBlockComponentType = ItemContainerBlock.getComponentType();

      public OnReplacedHolder() {
         super(BlockReplaceEvent.class);
      }

      public void handle(
         @Nonnull Holder<ChunkStore> holder,
         @Nonnull Store<ChunkStore> store,
         @Nonnull CommandBuffer<ChunkStore> commandBuffer,
         @Nonnull BlockReplaceEvent event
      ) {
         ItemContainerBlock otherContainer = event.getNewEntity().getComponent(this.itemContainerBlockComponentType);
         if (otherContainer != null) {
            ItemContainerBlock selfContainer = holder.getComponent(this.itemContainerBlockComponentType);

            assert selfContainer != null;

            selfContainer.getItemContainer().moveAllItemStacksTo(otherContainer.getItemContainer());
         }
      }

      @Nullable
      @Override
      public Query<ChunkStore> getQuery() {
         return this.itemContainerBlockComponentType;
      }
   }
}
