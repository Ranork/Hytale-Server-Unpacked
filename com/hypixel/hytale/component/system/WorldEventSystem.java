package com.hypixel.hytale.component.system;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import javax.annotation.Nonnull;

public abstract class WorldEventSystem<ECS_TYPE, EventType extends EcsEvent> extends EventSystem<EventType> implements ISystem<ECS_TYPE> {
   protected WorldEventSystem(@Nonnull Class<EventType> eventType) {
      super(eventType);
   }

   public abstract void handle(@Nonnull Store<ECS_TYPE> var1, @Nonnull CommandBuffer<ECS_TYPE> var2, @Nonnull EventType var3);

   public void handleInternal(@Nonnull Store<ECS_TYPE> store, @Nonnull CommandBuffer<ECS_TYPE> commandBuffer, @Nonnull EventType event) {
      if (this.shouldProcessEvent(event)) {
         this.handle(store, commandBuffer, event);
      }
   }
}
