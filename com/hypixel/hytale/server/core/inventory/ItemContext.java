package com.hypixel.hytale.server.core.inventory;

import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import javax.annotation.Nonnull;

public class ItemContext {
   @Nonnull
   private final ItemContainer container;
   private final short slot;
   @Nonnull
   private final ItemStack itemStack;

   public ItemContext(@Nonnull ItemContainer container, short slot, @Nonnull ItemStack itemStack) {
      this.container = container;
      this.slot = slot;
      this.itemStack = itemStack;
   }

   @Nonnull
   public ItemContainer getContainer() {
      return this.container;
   }

   public short getSlot() {
      return this.slot;
   }

   @Nonnull
   public ItemStack getItemStack() {
      return this.itemStack;
   }

   @Nonnull
   @Override
   public String toString() {
      return "ItemContext{container=" + this.container + ", slot=" + this.slot + ", itemStack=" + this.itemStack + "}";
   }
}
