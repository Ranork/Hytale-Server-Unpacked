package com.hypixel.hytale.builtin.buildertools.snapshot;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class EntityRemoveSnapshot implements EntitySnapshot<EntityAddSnapshot> {
   @Nonnull
   private final Holder<EntityStore> holder;

   public EntityRemoveSnapshot(@Nonnull Ref<EntityStore> ref) {
      this.holder = ref.getStore().copyEntity(ref);
   }

   @Nonnull
   public Holder<EntityStore> getHolder() {
      return this.holder;
   }

   public EntityAddSnapshot restoreEntity(@Nonnull Player player, @Nonnull World world, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      Ref<EntityStore> entityRef = componentAccessor.addEntity(this.holder, AddReason.LOAD);
      return new EntityAddSnapshot(entityRef);
   }
}
