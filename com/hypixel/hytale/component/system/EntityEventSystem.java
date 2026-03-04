package com.hypixel.hytale.component.system;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import javax.annotation.Nonnull;

public abstract class EntityEventSystem<ECS_TYPE, EventType extends EcsEvent> extends EventSystem<EventType> implements QuerySystem<ECS_TYPE> {
   protected EntityEventSystem(@Nonnull Class<EventType> eventType) {
      super(eventType);
   }

   public abstract void handle(
      int var1, @Nonnull ArchetypeChunk<ECS_TYPE> var2, @Nonnull Store<ECS_TYPE> var3, @Nonnull CommandBuffer<ECS_TYPE> var4, @Nonnull EventType var5
   );

   public void handleInternal(
      int index,
      @Nonnull ArchetypeChunk<ECS_TYPE> archetypeChunk,
      @Nonnull Store<ECS_TYPE> store,
      @Nonnull CommandBuffer<ECS_TYPE> commandBuffer,
      @Nonnull EventType event
   ) {
      if (this.shouldProcessEvent(event)) {
         this.handle(index, archetypeChunk, store, commandBuffer, event);
      }
   }
}
