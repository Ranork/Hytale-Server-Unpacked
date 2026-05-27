package com.hypixel.hytale.builtin.triggervolumes.manager;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.ProjectileComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public enum ProjectileSource {
   SHOOTER,
   PROJECTILE;

   @Nonnull
   public Ref<EntityStore> resolveActorRef(@Nonnull Ref<EntityStore> entityRef, @Nonnull Store<EntityStore> store) {
      if (this == PROJECTILE) {
         return entityRef;
      } else {
         ProjectileComponent projectileComponent = store.getComponent(entityRef, ProjectileComponent.getComponentType());
         if (projectileComponent != null && projectileComponent.getCreatorUuid() != null) {
            Ref<EntityStore> creatorRef = store.getExternalData().getRefFromUUID(projectileComponent.getCreatorUuid());
            return creatorRef != null && creatorRef.isValid() ? creatorRef : entityRef;
         } else {
            return entityRef;
         }
      }
   }
}
