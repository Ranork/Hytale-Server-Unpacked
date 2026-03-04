package com.hypixel.hytale.server.core.event.events.ecs;

import com.hypixel.hytale.component.system.CancellableEcsEvent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import javax.annotation.Nonnull;

public class InteractivelyPickupItemEvent extends CancellableEcsEvent {
   @Nonnull
   private ItemStack itemStack;

   public InteractivelyPickupItemEvent(@Nonnull ItemStack itemStack) {
      this.itemStack = itemStack;
   }

   @Nonnull
   public ItemStack getItemStack() {
      return this.itemStack;
   }

   public void setItemStack(@Nonnull ItemStack itemStack) {
      this.itemStack = itemStack;
   }
}
