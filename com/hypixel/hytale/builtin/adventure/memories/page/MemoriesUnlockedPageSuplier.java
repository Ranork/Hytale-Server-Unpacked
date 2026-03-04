package com.hypixel.hytale.builtin.adventure.memories.page;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.player.pages.CustomUIPage;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenCustomUIInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MemoriesUnlockedPageSuplier implements OpenCustomUIInteraction.CustomPageSupplier {
   @Nullable
   @Override
   public CustomUIPage tryCreate(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor,
      @Nonnull PlayerRef playerRef,
      @Nonnull InteractionContext context
   ) {
      BlockPosition targetBlock = context.getTargetBlock();
      return targetBlock == null ? null : new MemoriesUnlockedPage(playerRef, targetBlock);
   }
}
