package com.hypixel.hytale.builtin.adventure.objectives.task;

import com.hypixel.hytale.builtin.adventure.objectives.Objective;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.inventory.InventoryChangeEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public interface InventoryChangeAware {
   void onInventoryChange(@Nonnull Objective var1, @Nonnull Ref<EntityStore> var2, @Nonnull Store<EntityStore> var3, @Nonnull InventoryChangeEvent var4);
}
