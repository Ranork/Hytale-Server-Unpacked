package com.hypixel.hytale.builtin.crafting.window;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hypixel.hytale.builtin.adventure.memories.MemoriesGameplayConfig;
import com.hypixel.hytale.builtin.crafting.CraftingPlugin;
import com.hypixel.hytale.builtin.crafting.component.CraftingManager;
import com.hypixel.hytale.builtin.crafting.state.BenchState;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.protocol.packets.window.CraftRecipeAction;
import com.hypixel.hytale.protocol.packets.window.WindowType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.bench.CraftingBench;
import com.hypixel.hytale.server.core.asset.type.gameplay.GameplayConfig;
import com.hypixel.hytale.server.core.asset.type.item.config.CraftingRecipe;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Set;
import javax.annotation.Nonnull;

public abstract class CraftingWindow extends BenchWindow {
   @Nonnull
   protected static final String CRAFT_COMPLETED = "CraftCompleted";
   @Nonnull
   protected static final String CRAFT_COMPLETED_INSTANT = "CraftCompletedInstant";

   public CraftingWindow(@Nonnull WindowType windowType, @Nonnull BenchState benchState) {
      super(windowType, benchState);
      JsonArray categories = new JsonArray();
      if (this.bench instanceof CraftingBench craftingBench) {
         for (CraftingBench.BenchCategory benchCategory : craftingBench.getCategories()) {
            JsonObject category = new JsonObject();
            categories.add(category);
            category.addProperty("id", benchCategory.getId());
            category.addProperty("name", benchCategory.getName());
            category.addProperty("icon", benchCategory.getIcon());
            Set<String> recipes = CraftingPlugin.getAvailableRecipesForCategory(this.bench.getId(), benchCategory.getId());
            if (recipes != null) {
               JsonArray recipesArray = new JsonArray();

               for (String recipeId : recipes) {
                  recipesArray.add(recipeId);
               }

               category.add("craftableRecipes", recipesArray);
            }

            if (benchCategory.getItemCategories() != null) {
               JsonArray itemCategories = new JsonArray();

               for (CraftingBench.BenchItemCategory benchItemCategory : benchCategory.getItemCategories()) {
                  JsonObject itemCategory = new JsonObject();
                  itemCategory.addProperty("id", benchItemCategory.getId());
                  itemCategory.addProperty("icon", benchItemCategory.getIcon());
                  itemCategory.addProperty("diagram", benchItemCategory.getDiagram());
                  itemCategory.addProperty("slots", benchItemCategory.getSlots());
                  itemCategory.addProperty("specialSlot", benchItemCategory.isSpecialSlot());
                  itemCategories.add(itemCategory);
               }

               category.add("itemCategories", itemCategories);
            }
         }

         this.windowData.add("categories", categories);
      }
   }

   @Override
   protected boolean onOpen0(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
      super.onOpen0(ref, store);
      GameplayConfig gameplayConfig = store.getExternalData().getWorld().getGameplayConfig();
      MemoriesGameplayConfig memoriesConfig = MemoriesGameplayConfig.get(gameplayConfig);
      if (memoriesConfig != null) {
         int[] memoriesAmountPerLevel = memoriesConfig.getMemoriesAmountPerLevel();
         if (memoriesAmountPerLevel != null && memoriesAmountPerLevel.length > 1) {
            JsonArray memoriesPerLevel = new JsonArray();

            for (int i = 0; i < memoriesAmountPerLevel.length; i++) {
               memoriesPerLevel.add(memoriesAmountPerLevel[i]);
            }

            this.windowData.add("memoriesPerLevel", memoriesPerLevel);
         }
      }

      if (this.bench.getLocalOpenSoundEventIndex() != 0) {
         SoundUtil.playSoundEvent2d(ref, this.bench.getLocalOpenSoundEventIndex(), SoundCategory.UI, store);
      }

      return true;
   }

   @Override
   public void onClose0(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      super.onClose0(ref, componentAccessor);
      if (this.bench.getLocalCloseSoundEventIndex() != 0) {
         SoundUtil.playSoundEvent2d(ref, this.bench.getLocalCloseSoundEventIndex(), SoundCategory.UI, componentAccessor);
      }
   }

   public static boolean craftSimpleItem(
      @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull CraftingManager craftingManager, @Nonnull CraftRecipeAction action
   ) {
      String recipeId = action.recipeId;
      int quantity = action.quantity;
      if (recipeId == null) {
         return false;
      } else {
         CraftingRecipe recipe = CraftingRecipe.getAssetMap().getAsset(recipeId);
         if (recipe == null) {
            PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());

            assert playerRef != null;

            playerRef.getPacketHandler().disconnect("Attempted to craft unknown recipe!");
            return false;
         } else {
            Player playerComponent = store.getComponent(ref, Player.getComponentType());

            assert playerComponent != null;

            craftingManager.craftItem(ref, store, recipe, quantity, playerComponent.getInventory().getCombinedBackpackStorageHotbar());
            return true;
         }
      }
   }
}
