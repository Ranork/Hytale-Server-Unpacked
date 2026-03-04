package com.hypixel.hytale.server.core.universe.world.meta.state;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.meta.BlockState;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public interface PlacedByBlockState {
   void placedBy(@Nonnull Ref<EntityStore> var1, @Nonnull String var2, @Nonnull BlockState var3, @Nonnull ComponentAccessor<EntityStore> var4);
}
