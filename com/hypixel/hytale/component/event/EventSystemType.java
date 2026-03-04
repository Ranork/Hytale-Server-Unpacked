package com.hypixel.hytale.component.event;

import com.hypixel.hytale.component.ComponentRegistry;
import com.hypixel.hytale.component.SystemType;
import com.hypixel.hytale.component.system.EcsEvent;
import com.hypixel.hytale.component.system.EventSystem;
import com.hypixel.hytale.component.system.ISystem;
import javax.annotation.Nonnull;

public abstract class EventSystemType<ECS_TYPE, Event extends EcsEvent, SYSTEM_TYPE extends EventSystem<Event> & ISystem<ECS_TYPE>>
   extends SystemType<ECS_TYPE, SYSTEM_TYPE> {
   @Nonnull
   private final Class<Event> eClass;

   protected EventSystemType(@Nonnull ComponentRegistry<ECS_TYPE> registry, @Nonnull Class<? super SYSTEM_TYPE> tClass, @Nonnull Class<Event> eClass, int index) {
      super(registry, tClass, index);
      this.eClass = eClass;
   }

   @Nonnull
   public Class<Event> getEventClass() {
      return this.eClass;
   }

   @Override
   public boolean isType(@Nonnull ISystem<ECS_TYPE> system) {
      if (!super.isType(system)) {
         return false;
      } else {
         return system instanceof EventSystem<?> eventSystem ? this.eClass.equals(eventSystem.getEventType()) : false;
      }
   }
}
