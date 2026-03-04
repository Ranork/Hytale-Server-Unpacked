package com.hypixel.hytale.server.core.entity.entities.player.windows;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public interface ValidatedWindow {
   boolean validate(@Nonnull Ref<EntityStore> var1, @Nonnull ComponentAccessor<EntityStore> var2);
}
