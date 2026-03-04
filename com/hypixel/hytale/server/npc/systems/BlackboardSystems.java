package com.hypixel.hytale.server.npc.systems;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.DelayedSystem;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.component.system.StoreSystem;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.event.events.ecs.DamageBlockEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.blackboard.Blackboard;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlackboardSystems {
   public static class BreakBlockEventSystem extends EntityEventSystem<EntityStore, BreakBlockEvent> {
      public BreakBlockEventSystem() {
         super(BreakBlockEvent.class);
      }

      public void handle(
         int index,
         @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer,
         @Nonnull BreakBlockEvent event
      ) {
         Blackboard blackBoardResource = store.getResource(Blackboard.getResourceType());
         Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
         blackBoardResource.onEntityBreakBlock(ref, event);
      }

      @Nullable
      @Override
      public Query<EntityStore> getQuery() {
         return Archetype.empty();
      }
   }

   public static class DamageBlockEventSystem extends EntityEventSystem<EntityStore, DamageBlockEvent> {
      public DamageBlockEventSystem() {
         super(DamageBlockEvent.class);
      }

      public void handle(
         int index,
         @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer,
         @Nonnull DamageBlockEvent event
      ) {
         Blackboard blackBoardResource = store.getResource(Blackboard.getResourceType());
         Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
         blackBoardResource.onEntityDamageBlock(ref, event);
      }

      @Nullable
      @Override
      public Query<EntityStore> getQuery() {
         return Archetype.empty();
      }
   }

   public static class InitSystem extends StoreSystem<EntityStore> {
      @Nonnull
      private final ResourceType<EntityStore, Blackboard> resourceType;

      public InitSystem(@Nonnull ResourceType<EntityStore, Blackboard> resourceType) {
         this.resourceType = resourceType;
      }

      @Override
      public void onSystemAddedToStore(@Nonnull Store<EntityStore> store) {
         store.getResource(this.resourceType).init(store.getExternalData().getWorld());
      }

      @Override
      public void onSystemRemovedFromStore(@Nonnull Store<EntityStore> store) {
         store.getResource(this.resourceType).onWorldRemoved();
      }
   }

   public static class TickingSystem extends DelayedSystem<EntityStore> {
      private static final float SYSTEM_INTERVAL = 5.0F;
      @Nonnull
      private final ResourceType<EntityStore, Blackboard> resourceType;

      public TickingSystem(@Nonnull ResourceType<EntityStore, Blackboard> resourceType) {
         super(5.0F);
         this.resourceType = resourceType;
      }

      @Override
      public void delayedTick(float dt, int systemIndex, @Nonnull Store<EntityStore> store) {
         store.getResource(this.resourceType).cleanupViews();
      }
   }
}
