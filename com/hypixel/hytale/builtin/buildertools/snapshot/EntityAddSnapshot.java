package com.hypixel.hytale.builtin.buildertools.snapshot;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class EntityAddSnapshot implements EntitySnapshot<EntityRemoveSnapshot> {
   private final Ref<EntityStore> entityRef;

   public EntityAddSnapshot(Ref<EntityStore> entityRef) {
      this.entityRef = entityRef;
   }

   public Ref<EntityStore> getEntityRef() {
      return this.entityRef;
   }

   public EntityRemoveSnapshot restoreEntity(@Nonnull Player player, @Nonnull World world, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      if (!this.entityRef.isValid()) {
         return null;
      } else {
         EntityRemoveSnapshot snapshot = new EntityRemoveSnapshot(this.entityRef);
         world.getEntityStore().getStore().removeEntity(this.entityRef, RemoveReason.UNLOAD);
         return snapshot;
      }
   }
}
