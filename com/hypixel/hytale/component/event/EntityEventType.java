package com.hypixel.hytale.component.event;

import com.hypixel.hytale.component.ComponentRegistry;
import com.hypixel.hytale.component.system.EcsEvent;
import com.hypixel.hytale.component.system.EntityEventSystem;
import javax.annotation.Nonnull;

public class EntityEventType<ECS_TYPE, Event extends EcsEvent> extends EventSystemType<ECS_TYPE, Event, EntityEventSystem<ECS_TYPE, Event>> {
   public EntityEventType(
      @Nonnull ComponentRegistry<ECS_TYPE> registry, @Nonnull Class<? super EntityEventSystem<ECS_TYPE, Event>> tClass, @Nonnull Class<Event> eClass, int index
   ) {
      super(registry, tClass, eClass, index);
   }
}
