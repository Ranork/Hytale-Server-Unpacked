package com.hypixel.hytale.builtin.crafting.window;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hypixel.hytale.builtin.adventure.memories.MemoriesPlugin;
import com.hypixel.hytale.builtin.crafting.CraftingPlugin;
import com.hypixel.hytale.builtin.crafting.component.CraftingManager;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.BenchType;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.protocol.packets.window.CraftRecipeAction;
import com.hypixel.hytale.protocol.packets.window.WindowAction;
import com.hypixel.hytale.protocol.packets.window.WindowType;
import com.hypixel.hytale.server.core.asset.type.item.config.FieldcraftCategory;
import com.hypixel.hytale.server.core.entity.entities.player.windows.Window;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TempAssetIdUtil;
import java.util.Set;
import javax.annotation.Nonnull;

public class FieldCraftingWindow extends Window {
   @Nonnull
   private final JsonObject windowData = new JsonObject();

   public FieldCraftingWindow() {
      super(WindowType.PocketCrafting);
      this.windowData.addProperty("type", BenchType.Crafting.ordinal());
      this.windowData.addProperty("id", "Fieldcraft");
      this.windowData.addProperty("name", "server.ui.inventory.fieldcraft.title");
      JsonArray categories = new JsonArray();

      for (FieldcraftCategory fieldcraftCategory : FieldcraftCategory.getAssetMap().getAssetMap().values()) {
         JsonObject category = new JsonObject();
         category.addProperty("id", fieldcraftCategory.getId());
         category.addProperty("icon", fieldcraftCategory.getIcon());
         category.addProperty("name", fieldcraftCategory.getName());
         Set<String> recipes = CraftingPlugin.getAvailableRecipesForCategory("Fieldcraft", fieldcraftCategory.getId());
         if (recipes != null) {
            JsonArray itemsArray = new JsonArray();

            for (String recipeId : recipes) {
               itemsArray.add(recipeId);
            }

            category.add("craftableRecipes", itemsArray);
         }
      }

      this.windowData.add("categories", categories);
   }

   @Nonnull
   @Override
   public JsonObject getData() {
      return this.windowData;
   }

   @Override
   public boolean onOpen0(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
      World world = store.getExternalData().getWorld();
      this.windowData.addProperty("worldMemoriesLevel", MemoriesPlugin.get().getMemoriesLevel(world.getGameplayConfig()));
      this.invalidate();
      return true;
   }

   @Override
   public void onClose0(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
   }

   @Override
   public void handleAction(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull WindowAction action) {
      if (action instanceof CraftRecipeAction craftAction) {
         CraftingManager craftingManager = store.getComponent(ref, CraftingManager.getComponentType());
         if (CraftingWindow.craftSimpleItem(store, ref, craftingManager, craftAction)) {
            SoundUtil.playSoundEvent2d(ref, TempAssetIdUtil.getSoundEventIndex("SFX_Player_Craft_Item_Inventory"), SoundCategory.UI, store);
         }
      }
   }
}
