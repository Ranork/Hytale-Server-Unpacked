package com.hypixel.hytale.server.core.inventory.container.filter;

import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ItemSlotFilter extends SlotFilter {
   @Override
   default boolean test(@Nonnull FilterActionType actionType, @Nonnull ItemContainer container, short slot, @Nullable ItemStack itemStack) {
      return switch (actionType) {
         case ADD -> this.test(itemStack != null ? itemStack.getItem() : null);
         case REMOVE, DROP -> {
            itemStack = container.getItemStack(slot);
            yield this.test(itemStack != null ? itemStack.getItem() : null);
         }
      };
   }

   boolean test(@Nullable Item var1);
}
