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
import com.hypixel.hytale.protocol.InteractableUpdate;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.Interactable;
import com.hypixel.hytale.server.core.modules.entity.tracker.EntityTrackerSystems;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EntityInteractableSystems {
   public static class EntityTrackerAddAndRemove extends RefChangeSystem<EntityStore, Interactable> {
      private final ComponentType<EntityStore, Interactable> interactableComponentType = Interactable.getComponentType();
      private final ComponentType<EntityStore, EntityTrackerSystems.Visible> visibleComponentType;
      @Nonnull
      private final Query<EntityStore> query;

      public EntityTrackerAddAndRemove(ComponentType<EntityStore, EntityTrackerSystems.Visible> visibleComponentType) {
         this.visibleComponentType = visibleComponentType;
         this.query = Query.and(visibleComponentType, this.interactableComponentType);
      }

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return this.query;
      }

      @Nonnull
      @Override
      public ComponentType<EntityStore, Interactable> componentType() {
         return this.interactableComponentType;
      }

      public void onComponentAdded(
         @Nonnull Ref<EntityStore> ref, @Nonnull Interactable component, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         commandBuffer.getResource(EntityInteractableSystems.QueueResource.getResourceType()).queue.add(ref);
      }

      public void onComponentSet(
         @Nonnull Ref<EntityStore> ref,
         Interactable oldComponent,
         @Nonnull Interactable newComponent,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
      }

      public void onComponentRemoved(
         @Nonnull Ref<EntityStore> ref, @Nonnull Interactable component, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         for (EntityTrackerSystems.EntityViewer viewer : store.getComponent(ref, this.visibleComponentType).visibleTo.values()) {
            viewer.queueRemove(ref, ComponentUpdateType.Interactable);
         }
      }
   }

   public static class EntityTrackerUpdate extends EntityTickingSystem<EntityStore> {
      private final ComponentType<EntityStore, EntityTrackerSystems.Visible> componentType;
      @Nonnull
      private final Query<EntityStore> query;

      public EntityTrackerUpdate(ComponentType<EntityStore, EntityTrackerSystems.Visible> componentType) {
         this.componentType = componentType;
         this.query = Query.and(componentType, Interactable.getComponentType());
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
         store.getResource(EntityInteractableSystems.QueueResource.getResourceType()).queue.clear();
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
         if (commandBuffer.getResource(EntityInteractableSystems.QueueResource.getResourceType()).queue.remove(ref)) {
            queueUpdatesFor(ref, visible.visibleTo);
         } else if (!visible.newlyVisibleTo.isEmpty()) {
            queueUpdatesFor(ref, visible.newlyVisibleTo);
         }
      }

      private static void queueUpdatesFor(Ref<EntityStore> ref, @Nonnull Map<Ref<EntityStore>, EntityTrackerSystems.EntityViewer> visibleTo) {
         InteractableUpdate update = new InteractableUpdate();

         for (EntityTrackerSystems.EntityViewer viewer : visibleTo.values()) {
            viewer.queueUpdate(ref, update);
         }
      }
   }

   public static class QueueResource implements Resource<EntityStore> {
      private final Set<Ref<EntityStore>> queue = ConcurrentHashMap.newKeySet();

      public static ResourceType<EntityStore, EntityInteractableSystems.QueueResource> getResourceType() {
         return EntityModule.get().getInteractableQueueResourceType();
      }

      @Nonnull
      @Override
      public Resource<EntityStore> clone() {
         return new EntityInteractableSystems.QueueResource();
      }
   }
}
