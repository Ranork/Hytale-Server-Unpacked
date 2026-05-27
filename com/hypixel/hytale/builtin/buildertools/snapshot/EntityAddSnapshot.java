package com.hypixel.hytale.builtin.buildertools.snapshot;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EntityAddSnapshot implements EntitySnapshot<EntityRemoveSnapshot> {
   private final Ref<EntityStore> entityRef;

   public EntityAddSnapshot(Ref<EntityStore> entityRef) {
      this.entityRef = entityRef;
   }

   public Ref<EntityStore> getEntityRef() {
      return this.entityRef;
   }

   @Nullable
   public EntityRemoveSnapshot restoreEntity(@Nonnull PlayerRef playerRef, @Nonnull World world, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      if (!this.entityRef.isValid()) {
         return null;
      } else {
         EntityRemoveSnapshot snapshot = new EntityRemoveSnapshot(this.entityRef);
         world.getEntityStore().getStore().removeEntity(this.entityRef, RemoveReason.BUILDER_TOOLS_UNDO);
         return snapshot;
      }
   }
}
