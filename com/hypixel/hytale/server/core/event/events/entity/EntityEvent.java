package com.hypixel.hytale.server.core.event.events.entity;

import com.hypixel.hytale.event.IEvent;
import com.hypixel.hytale.server.core.entity.Entity;
import javax.annotation.Nonnull;

public abstract class EntityEvent<EntityType extends Entity, KeyType> implements IEvent<KeyType> {
   private final EntityType entity;

   public EntityEvent(EntityType entity) {
      this.entity = entity;
   }

   public EntityType getEntity() {
      return this.entity;
   }

   @Nonnull
   @Override
   public String toString() {
      return "EntityEvent{entity=" + this.entity + "}";
   }
}
