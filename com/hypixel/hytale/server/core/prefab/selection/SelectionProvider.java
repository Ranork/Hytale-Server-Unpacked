package com.hypixel.hytale.server.core.prefab.selection;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.sneakythrow.consumer.ThrowableConsumer;
import javax.annotation.Nonnull;

public interface SelectionProvider {
   <T extends Throwable> void computeSelectionCopy(
      @Nonnull Ref<EntityStore> var1, @Nonnull Player var2, @Nonnull ThrowableConsumer<BlockSelection, T> var3, @Nonnull ComponentAccessor<EntityStore> var4
   );
}
