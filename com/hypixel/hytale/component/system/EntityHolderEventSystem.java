package com.hypixel.hytale.component.system;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Store;
import javax.annotation.Nonnull;

public abstract class EntityHolderEventSystem<ECS_TYPE, EventType extends EcsEvent> extends EventSystem<EventType> implements QuerySystem<ECS_TYPE> {
   protected EntityHolderEventSystem(@Nonnull Class<EventType> eventType) {
      super(eventType);
   }

   public abstract void handle(@Nonnull Holder<ECS_TYPE> var1, @Nonnull Store<ECS_TYPE> var2, @Nonnull CommandBuffer<ECS_TYPE> var3, @Nonnull EventType var4);

   public void handleInternal(
      @Nonnull Holder<ECS_TYPE> holder, @Nonnull Store<ECS_TYPE> store, @Nonnull CommandBuffer<ECS_TYPE> commandBuffer, @Nonnull EventType event
   ) {
      if (this.shouldProcessEvent(event)) {
         this.handle(holder, store, commandBuffer, event);
      }
   }
}
