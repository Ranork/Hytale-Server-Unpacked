package com.hypixel.hytale.builtin.adventure.stash;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.HashUtil;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.asset.type.gameplay.GameplayConfig;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackSlotTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.ListTransaction;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.block.components.ItemContainerBlock;
import com.hypixel.hytale.server.core.modules.item.ItemModule;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortLists;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class StashPlugin extends JavaPlugin {
   @Nonnull
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

   public StashPlugin(@Nonnull JavaPluginInit init) {
      super(init);
   }

   @Override
   protected void setup() {
      this.getChunkStoreRegistry().registerSystem(new StashPlugin.StashSystem(ItemContainerBlock.getComponentType()));
      this.getCodecRegistry(GameplayConfig.PLUGIN_CODEC).register(StashGameplayConfig.class, "Stash", StashGameplayConfig.CODEC);
   }

   @Nullable
   public static ListTransaction<ItemStackTransaction> stash(
      BlockModule.BlockStateInfo blockStateInfo, @Nonnull ItemContainerBlock containerState, boolean clearDropList
   ) {
      String droplist = containerState.getDroplist();
      if (droplist == null) {
         return null;
      } else {
         List<ItemStack> stacks = ItemModule.get().getRandomItemDrops(droplist);
         if (stacks.isEmpty()) {
            return ListTransaction.getEmptyTransaction(true);
         } else {
            SimpleItemContainer itemContainer = containerState.getItemContainer();
            short capacity = itemContainer.getCapacity();
            ShortArrayList slots = new ShortArrayList(capacity);

            for (short s = 0; s < capacity; s++) {
               slots.add(s);
            }

            WorldChunk wc = blockStateInfo.getChunkRef().getStore().getComponent(blockStateInfo.getChunkRef(), WorldChunk.getComponentType());
            int x = ChunkUtil.xFromBlockInColumn(blockStateInfo.getIndex());
            int y = ChunkUtil.yFromBlockInColumn(blockStateInfo.getIndex());
            int z = ChunkUtil.zFromBlockInColumn(blockStateInfo.getIndex());
            int worldX = ChunkUtil.worldCoordFromLocalCoord(wc.getX(), x);
            int worldZ = ChunkUtil.worldCoordFromLocalCoord(wc.getZ(), z);
            long positionHash = HashUtil.hash(worldX, y, worldZ);
            Random rnd = new Random(positionHash);
            ShortLists.shuffle(slots, rnd);
            boolean anySucceeded = false;

            for (int idx = 0; idx < stacks.size() && idx < slots.size(); idx++) {
               short slot = slots.getShort(idx);
               ItemStackSlotTransaction transaction = itemContainer.addItemStackToSlot(slot, stacks.get(idx));
               if (transaction.getRemainder() != null && !transaction.getRemainder().isEmpty()) {
                  LOGGER.at(Level.WARNING).log("Could not add Item to Stash at %d, %d, %d: %s", worldX, y, worldZ, transaction.getRemainder());
               } else {
                  anySucceeded = true;
               }
            }

            if (clearDropList && anySucceeded) {
               containerState.setDroplist(null);
            }

            return new ListTransaction<>(anySucceeded, new ObjectArrayList());
         }
      }
   }

   private static class StashSystem extends RefSystem<ChunkStore> {
      @Nonnull
      private final ComponentType<ChunkStore, ItemContainerBlock> itemContainerStateComponentType;
      @Nonnull
      private final ComponentType<ChunkStore, BlockModule.BlockStateInfo> blockStateInfoComponentType = BlockModule.BlockStateInfo.getComponentType();
      @Nonnull
      private final Query<ChunkStore> query;

      public StashSystem(@Nonnull ComponentType<ChunkStore, ItemContainerBlock> itemContainerStateComponentType) {
         this.itemContainerStateComponentType = itemContainerStateComponentType;
         this.query = Query.and(itemContainerStateComponentType, this.blockStateInfoComponentType);
      }

      @Override
      public Query<ChunkStore> getQuery() {
         return this.query;
      }

      @Override
      public void onEntityAdded(
         @Nonnull Ref<ChunkStore> ref, @Nonnull AddReason reason, @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer
      ) {
         World world = store.getExternalData().getWorld();
         if (world.getWorldConfig().getGameMode() != GameMode.Creative) {
            ItemContainerBlock itemContainerStateComponent = store.getComponent(ref, this.itemContainerStateComponentType);

            assert itemContainerStateComponent != null;

            BlockModule.BlockStateInfo blockStateInfo = store.getComponent(ref, this.blockStateInfoComponentType);

            assert blockStateInfo != null;

            StashGameplayConfig stashGameplayConfig = StashGameplayConfig.getOrDefault(world.getGameplayConfig());
            boolean clearContainerDropList = stashGameplayConfig.isClearContainerDropList();
            StashPlugin.stash(blockStateInfo, itemContainerStateComponent, clearContainerDropList);
         }
      }

      @Override
      public void onEntityRemove(
         @Nonnull Ref<ChunkStore> ref, @Nonnull RemoveReason reason, @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer
      ) {
      }
   }
}
