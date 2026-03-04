package com.hypixel.hytale.component.event;

import com.hypixel.hytale.component.ComponentRegistry;
import com.hypixel.hytale.component.system.EcsEvent;
import com.hypixel.hytale.component.system.WorldEventSystem;
import javax.annotation.Nonnull;

public class WorldEventType<ECS_TYPE, Event extends EcsEvent> extends EventSystemType<ECS_TYPE, Event, WorldEventSystem<ECS_TYPE, Event>> {
   public WorldEventType(
      @Nonnull ComponentRegistry<ECS_TYPE> registry, @Nonnull Class<? super WorldEventSystem<ECS_TYPE, Event>> tClass, @Nonnull Class<Event> eClass, int index
   ) {
      super(registry, tClass, eClass, index);
   }
}
