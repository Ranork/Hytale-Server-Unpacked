package com.hypixel.hytale.builtin.buildertools.snapshot;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface EntitySnapshot<T extends SelectionSnapshot<?>> extends SelectionSnapshot<T> {
   @Nullable
   T restoreEntity(@Nonnull Player var1, @Nonnull World var2, @Nonnull ComponentAccessor<EntityStore> var3);

   @Override
   default T restore(Ref<EntityStore> ref, Player player, @Nonnull World world, ComponentAccessor<EntityStore> componentAccessor) {
      Store<EntityStore> store = world.getEntityStore().getStore();
      return !world.isInThread()
         ? CompletableFuture.<T>supplyAsync(() -> this.restoreEntity(player, world, store), world).join()
         : this.restoreEntity(player, world, store);
   }
}
