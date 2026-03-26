package com.hypixel.hytale.component.event;

import com.hypixel.hytale.component.ComponentRegistry;
import com.hypixel.hytale.component.system.EcsEvent;
import com.hypixel.hytale.component.system.EntityHolderEventSystem;
import javax.annotation.Nonnull;

public class EntityHolderEventType<ECS_TYPE, Event extends EcsEvent> extends EventSystemType<ECS_TYPE, Event, EntityHolderEventSystem<ECS_TYPE, Event>> {
   public EntityHolderEventType(
      @Nonnull ComponentRegistry<ECS_TYPE> registry,
      @Nonnull Class<? super EntityHolderEventSystem<ECS_TYPE, Event>> tClass,
      @Nonnull Class<Event> eClass,
      int index
   ) {
      super(registry, tClass, eClass, index);
   }
}
