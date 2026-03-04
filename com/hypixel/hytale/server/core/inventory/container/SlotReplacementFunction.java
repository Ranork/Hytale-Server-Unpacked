package com.hypixel.hytale.server.core.inventory.container;

import com.hypixel.hytale.server.core.inventory.ItemStack;

public interface SlotReplacementFunction {
   ItemStack replace(short var1, ItemStack var2);
}
