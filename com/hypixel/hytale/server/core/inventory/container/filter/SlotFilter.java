package com.hypixel.hytale.server.core.inventory.container.filter;

import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import javax.annotation.Nullable;

public interface SlotFilter {
   SlotFilter ALLOW = (actionType, container, slot, itemStack) -> true;
   SlotFilter DENY = (actionType, container, slot, itemStack) -> false;

   boolean test(FilterActionType var1, ItemContainer var2, short var3, @Nullable ItemStack var4);
}
