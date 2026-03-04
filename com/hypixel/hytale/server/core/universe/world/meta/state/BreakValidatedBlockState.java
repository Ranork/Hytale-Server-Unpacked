package com.hypixel.hytale.server.core.universe.world.meta.state;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public interface BreakValidatedBlockState {
   boolean canDestroy(@Nonnull Ref<EntityStore> var1, @Nonnull ComponentAccessor<EntityStore> var2);
}
