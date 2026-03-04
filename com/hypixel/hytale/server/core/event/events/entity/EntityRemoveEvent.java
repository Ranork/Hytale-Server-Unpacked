package com.hypixel.hytale.server.core.event.events.entity;

import com.hypixel.hytale.server.core.entity.Entity;
import javax.annotation.Nonnull;

public class EntityRemoveEvent extends EntityEvent<Entity, String> {
   public EntityRemoveEvent(Entity entity) {
      super(entity);
   }

   @Nonnull
   @Override
   public String toString() {
      return "EntityRemoveEvent{} " + super.toString();
   }
}
