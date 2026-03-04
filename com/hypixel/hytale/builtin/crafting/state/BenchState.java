package com.hypixel.hytale.builtin.crafting.state;

import com.hypixel.hytale.builtin.crafting.window.BenchWindow;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.bench.Bench;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.bench.BenchUpgradeRequirement;
import com.hypixel.hytale.server.core.entity.entities.player.windows.WindowManager;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.meta.BlockState;
import com.hypixel.hytale.server.core.universe.world.meta.state.DestroyableBlockState;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;

public class BenchState extends BlockState implements DestroyableBlockState {
   @Nonnull
   public static BuilderCodec<BenchState> CODEC = BuilderCodec.builder(BenchState.class, BenchState::new, BlockState.BASE_CODEC)
      .appendInherited(
         new KeyedCodec<>("TierLevel", Codec.INTEGER),
         (state, o) -> state.tierLevel = o,
         state -> state.tierLevel,
         (state, parent) -> state.tierLevel = parent.tierLevel
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("UpgradeItems", new ArrayCodec<>(ItemStack.CODEC, ItemStack[]::new)),
         (state, o) -> state.upgradeItems = o,
         state -> state.upgradeItems,
         (state, parent) -> state.upgradeItems = parent.upgradeItems
      )
      .add()
      .build();
   private int tierLevel = 1;
   protected ItemStack[] upgradeItems = ItemStack.EMPTY_ARRAY;
   protected Bench bench;
   @Nonnull
   protected final Map<UUID, BenchWindow> windows = new ConcurrentHashMap<>();

   public int getTierLevel() {
      return this.tierLevel;
   }

   @Override
   public boolean initialize(@Nonnull BlockType blockType) {
      if (!super.initialize(blockType)) {
         return false;
      } else {
         this.bench = blockType.getBench();
         if (this.bench == null) {
            if (this.upgradeItems.length > 0) {
               this.dropUpgradeItems();
            }

            return false;
         } else {
            return true;
         }
      }
   }

   public void addUpgradeItems(@Nonnull List<ItemStack> consumed) {
      consumed.addAll(Arrays.asList(this.upgradeItems));
      this.upgradeItems = consumed.toArray(ItemStack[]::new);
      this.markNeedsSave();
   }

   private void dropUpgradeItems() {
      if (this.upgradeItems.length != 0) {
         World world = this.getChunk().getWorld();
         Store<EntityStore> entityStore = world.getEntityStore().getStore();
         Vector3d dropPosition = this.getBlockPosition().toVector3d().add(0.5, 0.0, 0.5);
         Holder<EntityStore>[] itemEntityHolders = ItemComponent.generateItemDrops(entityStore, List.of(this.upgradeItems), dropPosition, Vector3f.ZERO);
         if (itemEntityHolders.length > 0) {
            world.execute(() -> entityStore.addEntities(itemEntityHolders, AddReason.SPAWN));
         }

         this.upgradeItems = ItemStack.EMPTY_ARRAY;
      }
   }

   public Bench getBench() {
      return this.bench;
   }

   public void setTierLevel(int newTierLevel) {
      if (this.tierLevel != newTierLevel) {
         this.tierLevel = newTierLevel;
         this.onTierLevelChange();
         this.markNeedsSave();
      }
   }

   public BenchUpgradeRequirement getNextLevelUpgradeMaterials() {
      return this.bench.getUpgradeRequirement(this.tierLevel);
   }

   protected void onTierLevelChange() {
      this.getChunk().setBlockInteractionState(this.getBlockPosition(), this.getBaseBlockType(), this.getTierStateName());
   }

   @Nonnull
   public BlockType getBaseBlockType() {
      BlockType currentBlockType = this.getBlockType();
      String baseBlockKey = currentBlockType.getDefaultStateKey();
      BlockType baseBlockType = BlockType.getAssetMap().getAsset(baseBlockKey);
      if (baseBlockType == null) {
         baseBlockType = currentBlockType;
      }

      return baseBlockType;
   }

   @Nonnull
   public String getTierStateName() {
      return this.tierLevel > 1 ? "Tier" + this.tierLevel : "default";
   }

   @Override
   public void onDestroy() {
      WindowManager.closeAndRemoveAll(this.windows);
      this.dropUpgradeItems();
   }

   @Nonnull
   public Map<UUID, BenchWindow> getWindows() {
      return this.windows;
   }
}
