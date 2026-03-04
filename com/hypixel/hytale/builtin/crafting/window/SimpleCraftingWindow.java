package com.hypixel.hytale.builtin.crafting.window;

import com.hypixel.hytale.builtin.crafting.component.CraftingManager;
import com.hypixel.hytale.builtin.crafting.state.BenchState;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.protocol.packets.window.CraftRecipeAction;
import com.hypixel.hytale.protocol.packets.window.TierUpgradeAction;
import com.hypixel.hytale.protocol.packets.window.WindowAction;
import com.hypixel.hytale.protocol.packets.window.WindowType;
import com.hypixel.hytale.server.core.asset.type.item.config.CraftingRecipe;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.windows.MaterialContainerWindow;
import com.hypixel.hytale.server.core.entity.entities.player.windows.WindowManager;
import com.hypixel.hytale.server.core.inventory.container.CombinedItemContainer;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class SimpleCraftingWindow extends CraftingWindow implements MaterialContainerWindow {
   public SimpleCraftingWindow(@Nonnull BenchState benchState) {
      super(WindowType.BasicCrafting, benchState);
   }

   @Override
   public void init(@Nonnull PlayerRef playerRef, @Nonnull WindowManager manager) {
      super.init(playerRef, manager);
   }

   @Override
   public void handleAction(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull WindowAction action) {
      CraftingManager craftingManager = store.getComponent(ref, CraftingManager.getComponentType());
      if (craftingManager != null) {
         World world = store.getExternalData().getWorld();
         if (action instanceof CraftRecipeAction craftAction) {
            String recipeId = craftAction.recipeId;
            int quantity = craftAction.quantity;
            CraftingRecipe craftRecipe = CraftingRecipe.getAssetMap().getAsset(recipeId);
            if (craftRecipe == null) {
               PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
               if (playerRef != null) {
                  playerRef.getPacketHandler().disconnect("Attempted to craft unknown recipe!");
               }

               return;
            }

            Player playerComponent = store.getComponent(ref, Player.getComponentType());
            if (playerComponent == null) {
               return;
            }

            CombinedItemContainer combined = playerComponent.getInventory().getCombinedBackpackStorageHotbar();
            CombinedItemContainer playerAndContainerInventory = new CombinedItemContainer(combined, this.getExtraResourcesSection().getItemContainer());
            boolean accepted;
            if (craftRecipe.getTimeSeconds() > 0.0F) {
               accepted = craftingManager.queueCraft(
                  ref, store, this, 0, craftRecipe, quantity, playerAndContainerInventory, CraftingManager.InputRemovalType.NORMAL
               );
            } else {
               accepted = craftingManager.craftItem(ref, store, craftRecipe, quantity, playerAndContainerInventory);
            }

            this.invalidateExtraResources();
            if (accepted) {
               String completedState = craftRecipe.getTimeSeconds() > 0.0F ? "CraftCompleted" : "CraftCompletedInstant";
               this.setBlockInteractionState(completedState, world);
               if (this.bench.getCompletedSoundEventIndex() != 0) {
                  Vector3d pos = new Vector3d();
                  this.blockType.getBlockCenter(this.rotationIndex, pos);
                  pos.add(this.x, this.y, this.z);
                  SoundUtil.playSoundEvent3d(this.bench.getCompletedSoundEventIndex(), SoundCategory.SFX, pos, store);
               }
            }
         } else if (action instanceof TierUpgradeAction && craftingManager.startTierUpgrade(ref, store, this)) {
            this.setBlockInteractionState("BenchUpgrading", world);
            if (this.bench.getBenchUpgradeSoundEventIndex() != 0) {
               Vector3d pos = new Vector3d();
               this.blockType.getBlockCenter(this.rotationIndex, pos);
               pos.add(this.x, this.y, this.z);
               SoundUtil.playSoundEvent3d(this.bench.getBenchUpgradeSoundEventIndex(), SoundCategory.SFX, pos, store);
            }
         }
      }
   }
}
