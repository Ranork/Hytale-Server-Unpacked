package com.hypixel.hytale.server.npc.systems;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.interactions.SpawnNPCInteractionFailureTracker;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class SpawnNPCInteractionFailureTrackerSystems {
   private SpawnNPCInteractionFailureTrackerSystems() {
   }

   public static class BreakBlockClearSystem extends EntityEventSystem<EntityStore, BreakBlockEvent> {
      @Nonnull
      private final ResourceType<EntityStore, SpawnNPCInteractionFailureTracker> trackerResourceType;

      public BreakBlockClearSystem(@Nonnull ResourceType<EntityStore, SpawnNPCInteractionFailureTracker> trackerResourceType) {
         super(BreakBlockEvent.class);
         this.trackerResourceType = trackerResourceType;
      }

      public void handle(
         int index,
         @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer,
         @Nonnull BreakBlockEvent event
      ) {
         store.getResource(this.trackerResourceType).clearFailureForBlock(event.getTargetBlock());
      }

      @Nullable
      @Override
      public Query<EntityStore> getQuery() {
         return Archetype.empty();
      }
   }
}
