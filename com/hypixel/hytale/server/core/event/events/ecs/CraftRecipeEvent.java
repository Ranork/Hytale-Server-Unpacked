package com.hypixel.hytale.server.core.event.events.ecs;

import com.hypixel.hytale.component.system.CancellableEcsEvent;
import com.hypixel.hytale.server.core.asset.type.item.config.CraftingRecipe;
import javax.annotation.Nonnull;

public abstract class CraftRecipeEvent extends CancellableEcsEvent {
   @Nonnull
   private final CraftingRecipe craftedRecipe;
   private final int quantity;

   public CraftRecipeEvent(@Nonnull CraftingRecipe craftedRecipe, int quantity) {
      this.craftedRecipe = craftedRecipe;
      this.quantity = quantity;
   }

   @Nonnull
   public CraftingRecipe getCraftedRecipe() {
      return this.craftedRecipe;
   }

   public int getQuantity() {
      return this.quantity;
   }

   public static final class Post extends CraftRecipeEvent {
      public Post(@Nonnull CraftingRecipe craftedRecipe, int quantity) {
         super(craftedRecipe, quantity);
      }
   }

   public static final class Pre extends CraftRecipeEvent {
      public Pre(@Nonnull CraftingRecipe craftedRecipe, int quantity) {
         super(craftedRecipe, quantity);
      }
   }
}
