package com.hypixel.hytale.component.system;

import javax.annotation.Nonnull;

public abstract class EventSystem<EventType extends EcsEvent> {
   @Nonnull
   private final Class<EventType> eventType;

   protected EventSystem(@Nonnull Class<EventType> eventType) {
      this.eventType = eventType;
   }

   protected boolean shouldProcessEvent(@Nonnull EventType event) {
      return !(event instanceof ICancellableEcsEvent cancellable && cancellable.isCancelled());
   }

   @Nonnull
   public Class<EventType> getEventType() {
      return this.eventType;
   }
}
