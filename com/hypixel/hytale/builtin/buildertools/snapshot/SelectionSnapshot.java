package com.hypixel.hytale.builtin.buildertools.snapshot;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nullable;

public interface SelectionSnapshot<T extends SelectionSnapshot<?>> {
   @Nullable
   T restore(Ref<EntityStore> var1, Player var2, World var3, ComponentAccessor<EntityStore> var4);
}
