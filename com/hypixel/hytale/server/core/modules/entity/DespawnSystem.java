package com.hypixel.hytale.server.core.modules.entity;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.modules.entity.component.Interactable;
import com.hypixel.hytale.server.core.modules.time.TimeResource;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.time.Instant;
import javax.annotation.Nonnull;

public class DespawnSystem extends EntityTickingSystem<EntityStore> {
   private final ComponentType<EntityStore, DespawnComponent> despawnComponentType;
   @Nonnull
   private final Query<EntityStore> query;

   public DespawnSystem(ComponentType<EntityStore, DespawnComponent> despawnComponentType) {
      this.despawnComponentType = despawnComponentType;
      this.query = Query.and(despawnComponentType, Query.not(Interactable.getComponentType()));
   }

   @Override
   public boolean isParallel(int archetypeChunkSize, int taskCount) {
      return EntityTickingSystem.maybeUseParallel(archetypeChunkSize, taskCount);
   }

   @Nonnull
   @Override
   public Query<EntityStore> getQuery() {
      return this.query;
   }

   @Override
   public void tick(
      float dt,
      int index,
      @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
      @Nonnull Store<EntityStore> store,
      @Nonnull CommandBuffer<EntityStore> commandBuffer
   ) {
      DespawnComponent despawn = archetypeChunk.getComponent(index, this.despawnComponentType);
      Instant despawnInstant = despawn.getDespawn();
      TimeResource timeResource = store.getResource(TimeResource.getResourceType());
      if (timeResource.getNow().isAfter(despawnInstant)) {
         Ref<EntityStore> entityRef = archetypeChunk.getReferenceTo(index);
         commandBuffer.removeEntity(entityRef, RemoveReason.REMOVE);
      }
   }
}
