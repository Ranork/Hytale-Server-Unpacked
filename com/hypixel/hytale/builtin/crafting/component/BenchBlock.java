package com.hypixel.hytale.builtin.crafting.component;

import com.hypixel.hytale.builtin.crafting.CraftingPlugin;
import com.hypixel.hytale.builtin.crafting.window.BenchWindow;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BenchBlock implements Component<ChunkStore> {
   @Nonnull
   public static BuilderCodec<BenchBlock> CODEC = BuilderCodec.builder(BenchBlock.class, BenchBlock::new)
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
   @Nonnull
   protected final transient Map<UUID, BenchWindow> windows = new ConcurrentHashMap<>();

   public static ComponentType<ChunkStore, BenchBlock> getComponentType() {
      return CraftingPlugin.get().getBenchBlockComponentType();
   }

   public BenchBlock() {
   }

   public BenchBlock(int tierLevel, ItemStack[] upgradeItems) {
      this.tierLevel = tierLevel;
      this.upgradeItems = upgradeItems;
   }

   public void addUpgradeItems(@Nonnull List<ItemStack> consumed) {
      consumed.addAll(Arrays.asList(this.upgradeItems));
      this.upgradeItems = consumed.toArray(ItemStack[]::new);
   }

   public void setTierLevel(int newTierLevel) {
      this.tierLevel = newTierLevel;
   }

   public int getTierLevel() {
      return this.tierLevel;
   }

   public ItemStack[] getUpgradeItems() {
      return this.upgradeItems;
   }

   public void setUpgradeItems(ItemStack[] upgradeItems) {
      this.upgradeItems = upgradeItems;
   }

   @Nonnull
   public String getTierStateName() {
      return this.tierLevel > 1 ? "Tier" + this.tierLevel : "default";
   }

   @Nonnull
   public Map<UUID, BenchWindow> getWindows() {
      return this.windows;
   }

   @Nullable
   @Override
   public Component<ChunkStore> clone() {
      return new BenchBlock(this.tierLevel, this.upgradeItems);
   }

   @Nonnull
   public static BlockType getBaseBlockType(@Nonnull BlockType currentBlockType) {
      String baseBlockKey = currentBlockType.getDefaultStateKey();
      if (baseBlockKey == null) {
         return currentBlockType;
      } else {
         BlockType baseBlockType = BlockType.getAssetMap().getAsset(baseBlockKey);
         if (baseBlockType == null) {
            baseBlockType = currentBlockType;
         }

         return baseBlockType;
      }
   }
}
