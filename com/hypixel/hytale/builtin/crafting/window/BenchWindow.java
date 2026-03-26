package com.hypixel.hytale.builtin.crafting.window;

import com.google.gson.JsonObject;
import com.hypixel.hytale.builtin.adventure.memories.MemoriesPlugin;
import com.hypixel.hytale.builtin.crafting.component.BenchBlock;
import com.hypixel.hytale.builtin.crafting.component.CraftingManager;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.protocol.packets.window.WindowType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.bench.Bench;
import com.hypixel.hytale.server.core.asset.type.gameplay.CraftingConfig;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.entities.player.windows.BlockWindow;
import com.hypixel.hytale.server.core.entity.entities.player.windows.MaterialContainerWindow;
import com.hypixel.hytale.server.core.entity.entities.player.windows.MaterialExtraResourcesSection;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public abstract class BenchWindow extends BlockWindow implements MaterialContainerWindow {
   private static final float CRAFTING_UPDATE_MIN_PERCENT = 0.05F;
   private static final long CRAFTING_UPDATE_INTERVAL_MS = 500L;
   @Nonnull
   protected static final String BENCH_UPGRADING = "BenchUpgrading";
   private float lastUpdatePercent;
   private long lastUpdateTimeMs;
   protected final Bench bench;
   @Nonnull
   protected final BenchBlock benchBlock;
   @Nonnull
   protected final JsonObject windowData = new JsonObject();
   @Nonnull
   private final MaterialExtraResourcesSection extraResourcesSection = new MaterialExtraResourcesSection();

   public BenchWindow(@Nonnull WindowType windowType, int x, int y, int z, int rotationIndex, @Nonnull BlockType blockType, @Nonnull BenchBlock benchBlock) {
      super(windowType, x, y, z, rotationIndex, blockType);
      this.bench = blockType.getBench();
      this.benchBlock = benchBlock;
      Item item = blockType.getItem();
      if (item == null) {
         throw new IllegalStateException("Bench block type " + blockType.getId() + " does not have an associated item!");
      } else {
         this.windowData.addProperty("type", this.bench.getType().ordinal());
         this.windowData.addProperty("id", this.bench.getId());
         this.windowData.addProperty("name", item.getTranslationKey());
         this.windowData.addProperty("blockItemId", item.getId());
         this.windowData.addProperty("tierLevel", this.getBenchTierLevel());
      }
   }

   @Nonnull
   @Override
   public JsonObject getData() {
      return this.windowData;
   }

   @Override
   protected boolean onOpen0(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
      CraftingManager craftingManagerComponent = store.getComponent(ref, CraftingManager.getComponentType());
      if (craftingManagerComponent == null) {
         return false;
      } else {
         craftingManagerComponent.setBench(this.x, this.y, this.z, this.blockType);
         World world = store.getExternalData().getWorld();
         int memoriesLevel = MemoriesPlugin.get().getMemoriesLevel(world.getGameplayConfig());
         this.windowData.addProperty("worldMemoriesLevel", memoriesLevel);
         int chestCount = CraftingManager.feedExtraResourcesSection(
            world, this.x, this.y, this.z, this.blockType, this.rotationIndex, this.bench, this.getBenchTierLevel(), this.extraResourcesSection
         );
         CraftingConfig craftingConfig = world.getGameplayConfig().getCraftingConfig();
         int maxChestCount = craftingConfig.getBenchMaterialChestLimit();
         int horizontalRadius = craftingConfig.getBenchMaterialHorizontalChestSearchRadius();
         int verticalRadius = craftingConfig.getBenchMaterialVerticalChestSearchRadius();
         this.windowData.addProperty("nearbyChestCount", chestCount);
         this.windowData.addProperty("maxChestCount", maxChestCount);
         this.windowData.addProperty("chestHorizontalRadius", horizontalRadius);
         this.windowData.addProperty("chestVerticalRadius", verticalRadius);
         return true;
      }
   }

   protected int getBenchTierLevel() {
      return this.benchBlock != null ? this.benchBlock.getTierLevel() : 1;
   }

   @Override
   public void onClose0(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      World world = componentAccessor.getExternalData().getWorld();
      this.setBlockInteractionState(this.benchBlock.getTierStateName(), world);
      CraftingManager craftingManagerComponent = componentAccessor.getComponent(ref, CraftingManager.getComponentType());
      if (craftingManagerComponent != null) {
         if (craftingManagerComponent.clearBench(ref, componentAccessor) && this.bench.getFailedSoundEventIndex() != 0) {
            SoundUtil.playSoundEvent2d(ref, this.bench.getFailedSoundEventIndex(), SoundCategory.UI, componentAccessor);
         }
      }
   }

   public void setBlockInteractionState(@Nonnull String state, @Nonnull World world) {
      WorldChunk worldChunk = world.getChunk(ChunkUtil.indexChunkFromBlock(this.x, this.z));
      if (worldChunk != null) {
         BlockType blockType = worldChunk.getBlockType(this.x, this.y, this.z);
         if (blockType != null) {
            worldChunk.setBlockInteractionState(this.x, this.y, this.z, blockType, state, true);
         }
      }
   }

   public void updateQueueSize(int size) {
      this.windowData.addProperty("queueSize", size);
   }

   public void updateCraftingJob(float percent) {
      this.windowData.addProperty("progress", percent);
      this.checkProgressInvalidate(percent);
   }

   public void updateBenchUpgradeJob(float percent) {
      this.windowData.addProperty("tierUpgradeProgress", percent);
      this.checkProgressInvalidate(percent);
   }

   private void checkProgressInvalidate(float percent) {
      if (this.lastUpdatePercent != percent) {
         long time = System.currentTimeMillis();
         if (percent >= 1.0F
            || percent < this.lastUpdatePercent
            || percent - this.lastUpdatePercent > 0.05F
            || time - this.lastUpdateTimeMs > 500L
            || this.lastUpdateTimeMs == 0L) {
            this.lastUpdatePercent = percent;
            this.lastUpdateTimeMs = time;
            this.invalidate();
         }
      }
   }

   public void updateBenchTierLevel(int newValue) {
      this.windowData.addProperty("tierLevel", newValue);
      this.updateBenchUpgradeJob(0.0F);
      this.setNeedRebuild();
      this.invalidate();
   }

   @Nonnull
   @Override
   public MaterialExtraResourcesSection getExtraResourcesSection() {
      if (!this.extraResourcesSection.isValid()) {
         PlayerRef playerRef = this.getPlayerRef();
         if (playerRef != null) {
            Ref<EntityStore> ref = playerRef.getReference();
            if (ref != null && ref.isValid()) {
               World world = ref.getStore().getExternalData().getWorld();
               CraftingManager.feedExtraResourcesSection(
                  world, this.x, this.y, this.z, this.blockType, this.rotationIndex, this.bench, this.getBenchTierLevel(), this.extraResourcesSection
               );
            }
         }
      }

      return this.extraResourcesSection;
   }

   @Override
   public void invalidateExtraResources() {
      this.extraResourcesSection.setValid(false);
      this.invalidate();
   }

   @Override
   public boolean isValid() {
      return this.extraResourcesSection.isValid();
   }
}
