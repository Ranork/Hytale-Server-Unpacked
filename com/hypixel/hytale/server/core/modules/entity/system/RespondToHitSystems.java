package com.hypixel.hytale.server.core.modules.entity.system;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Resource;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefChangeSystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.protocol.ComponentUpdateType;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.protocol.RespondToHitUpdate;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.RespondToHit;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSettings;
import com.hypixel.hytale.server.core.modules.entity.tracker.EntityTrackerSystems;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RespondToHitSystems {
   public static class EntityTrackerAddAndRemove extends RefChangeSystem<EntityStore, RespondToHit> {
      private final ComponentType<EntityStore, RespondToHit> respondToHitComponentType = RespondToHit.getComponentType();
      private final ComponentType<EntityStore, EntityTrackerSystems.Visible> visibleComponentType;
      @Nonnull
      private final Query<EntityStore> query;

      public EntityTrackerAddAndRemove(ComponentType<EntityStore, EntityTrackerSystems.Visible> visibleComponentType) {
         this.visibleComponentType = visibleComponentType;
         this.query = Query.and(visibleComponentType, this.respondToHitComponentType);
      }

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return this.query;
      }

      @Nonnull
      @Override
      public ComponentType<EntityStore, RespondToHit> componentType() {
         return this.respondToHitComponentType;
      }

      public void onComponentAdded(
         @Nonnull Ref<EntityStore> ref, @Nonnull RespondToHit component, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         commandBuffer.getResource(RespondToHitSystems.QueueResource.getResourceType()).queue.add(ref);
      }

      public void onComponentSet(
         @Nonnull Ref<EntityStore> ref,
         RespondToHit oldComponent,
         @Nonnull RespondToHit newComponent,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
      }

      public void onComponentRemoved(
         @Nonnull Ref<EntityStore> ref, @Nonnull RespondToHit component, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         for (EntityTrackerSystems.EntityViewer viewer : store.getComponent(ref, this.visibleComponentType).visibleTo.values()) {
            viewer.queueRemove(ref, ComponentUpdateType.RespondToHit);
         }
      }
   }

   public static class EntityTrackerUpdate extends EntityTickingSystem<EntityStore> {
      private final ComponentType<EntityStore, EntityTrackerSystems.Visible> componentType;
      @Nonnull
      private final Query<EntityStore> query;

      public EntityTrackerUpdate(ComponentType<EntityStore, EntityTrackerSystems.Visible> componentType) {
         this.componentType = componentType;
         this.query = Query.and(componentType, RespondToHit.getComponentType());
      }

      @Nullable
      @Override
      public SystemGroup<EntityStore> getGroup() {
         return EntityTrackerSystems.QUEUE_UPDATE_GROUP;
      }

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return this.query;
      }

      @Override
      public boolean isParallel(int archetypeChunkSize, int taskCount) {
         return EntityTickingSystem.maybeUseParallel(archetypeChunkSize, taskCount);
      }

      @Override
      public void tick(float dt, int systemIndex, @Nonnull Store<EntityStore> store) {
         super.tick(dt, systemIndex, store);
         store.getResource(RespondToHitSystems.QueueResource.getResourceType()).queue.clear();
      }

      @Override
      public void tick(
         float dt,
         int index,
         @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         EntityTrackerSystems.Visible visible = archetypeChunk.getComponent(index, this.componentType);
         Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
         if (commandBuffer.getResource(RespondToHitSystems.QueueResource.getResourceType()).queue.remove(ref)) {
            queueUpdatesFor(ref, visible.visibleTo);
         } else if (!visible.newlyVisibleTo.isEmpty()) {
            queueUpdatesFor(ref, visible.newlyVisibleTo);
         }
      }

      private static void queueUpdatesFor(Ref<EntityStore> ref, @Nonnull Map<Ref<EntityStore>, EntityTrackerSystems.EntityViewer> visibleTo) {
         RespondToHitUpdate update = new RespondToHitUpdate();

         for (EntityTrackerSystems.EntityViewer viewer : visibleTo.values()) {
            viewer.queueUpdate(ref, update);
         }
      }
   }

   public static class OnPlayerSettingsChange extends RefChangeSystem<EntityStore, PlayerSettings> {
      @Nonnull
      @Override
      public ComponentType<EntityStore, PlayerSettings> componentType() {
         return PlayerSettings.getComponentType();
      }

      public void onComponentAdded(
         @Nonnull Ref<EntityStore> ref, @Nonnull PlayerSettings component, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         Player player = commandBuffer.getComponent(ref, Player.getComponentType());
         if (player.getGameMode() == GameMode.Creative) {
            if (component.creativeSettings().respondToHit()) {
               commandBuffer.ensureComponent(ref, RespondToHit.getComponentType());
            } else {
               commandBuffer.tryRemoveComponent(ref, RespondToHit.getComponentType());
            }
         }
      }

      public void onComponentSet(
         @Nonnull Ref<EntityStore> ref,
         @Nullable PlayerSettings oldComponent,
         @Nonnull PlayerSettings newComponent,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         Player player = commandBuffer.getComponent(ref, Player.getComponentType());
         if (player.getGameMode() == GameMode.Creative) {
            if (newComponent.creativeSettings().respondToHit()) {
               commandBuffer.ensureComponent(ref, RespondToHit.getComponentType());
            } else {
               commandBuffer.tryRemoveComponent(ref, RespondToHit.getComponentType());
            }
         }
      }

      public void onComponentRemoved(
         @Nonnull Ref<EntityStore> ref, @Nonnull PlayerSettings component, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
      }

      @Nullable
      @Override
      public Query<EntityStore> getQuery() {
         return Player.getComponentType();
      }
   }

   public static class QueueResource implements Resource<EntityStore> {
      private final Set<Ref<EntityStore>> queue = ConcurrentHashMap.newKeySet();

      public static ResourceType<EntityStore, RespondToHitSystems.QueueResource> getResourceType() {
         return EntityModule.get().getRespondToHitQueueResourceType();
      }

      @Nonnull
      @Override
      public Resource<EntityStore> clone() {
         return new RespondToHitSystems.QueueResource();
      }
   }
}
