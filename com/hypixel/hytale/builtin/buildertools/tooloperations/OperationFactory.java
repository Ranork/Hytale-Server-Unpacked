package com.hypixel.hytale.builtin.buildertools.tooloperations;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolOnUseInteraction;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public interface OperationFactory {
   @Nonnull
   ToolOperation create(
      @Nonnull Ref<EntityStore> var1, @Nonnull Player var2, @Nonnull BuilderToolOnUseInteraction var3, @Nonnull ComponentAccessor<EntityStore> var4
   );
}
