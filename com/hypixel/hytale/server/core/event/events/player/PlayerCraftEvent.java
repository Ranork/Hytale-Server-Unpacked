package com.hypixel.hytale.server.core.event.events.player;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.asset.type.item.config.CraftingRecipe;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

@Deprecated(forRemoval = true)
public class PlayerCraftEvent extends PlayerEvent<String> {
   private final CraftingRecipe craftedRecipe;
   private final int quantity;

   public PlayerCraftEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Player player, CraftingRecipe craftedRecipe, int quantity) {
      super(ref, player);
      this.craftedRecipe = craftedRecipe;
      this.quantity = quantity;
   }

   public CraftingRecipe getCraftedRecipe() {
      return this.craftedRecipe;
   }

   public int getQuantity() {
      return this.quantity;
   }

   @Nonnull
   @Override
   public String toString() {
      return "PlayerCraftEvent{craftingRecipe=" + this.craftedRecipe + ", quantity=" + this.quantity + "}";
   }
}
