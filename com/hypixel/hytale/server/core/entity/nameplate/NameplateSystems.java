package com.hypixel.hytale.server.core.entity.nameplate;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefChangeSystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.protocol.ComponentUpdateType;
import com.hypixel.hytale.protocol.NameplateUpdate;
import com.hypixel.hytale.server.core.modules.entity.tracker.EntityTrackerSystems;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NameplateSystems {
   public static class EntityTrackerRemove extends RefChangeSystem<EntityStore, Nameplate> {
      @Nonnull
      private final ComponentType<EntityStore, Nameplate> componentType;
      @Nonnull
      private final ComponentType<EntityStore, EntityTrackerSystems.Visible> visibleComponentType;
      @Nonnull
      private final Query<EntityStore> query;

      public EntityTrackerRemove(
         @Nonnull ComponentType<EntityStore, EntityTrackerSystems.Visible> visibleComponentType, @Nonnull ComponentType<EntityStore, Nameplate> componentType
      ) {
         this.visibleComponentType = visibleComponentType;
         this.componentType = componentType;
         this.query = Query.and(visibleComponentType, componentType);
      }

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return this.query;
      }

      @Nonnull
      @Override
      public ComponentType<EntityStore, Nameplate> componentType() {
         return this.componentType;
      }

      public void onComponentAdded(
         @Nonnull Ref<EntityStore> ref, @Nonnull Nameplate component, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
      }

      public void onComponentSet(
         @Nonnull Ref<EntityStore> ref,
         Nameplate oldComponent,
         @Nonnull Nameplate newComponent,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
      }

      public void onComponentRemoved(
         @Nonnull Ref<EntityStore> ref, @Nonnull Nameplate component, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         EntityTrackerSystems.Visible visibleComponent = store.getComponent(ref, this.visibleComponentType);

         assert visibleComponent != null;

         for (EntityTrackerSystems.EntityViewer viewer : visibleComponent.visibleTo.values()) {
            viewer.queueRemove(ref, ComponentUpdateType.Nameplate);
         }
      }
   }

   public static class EntityTrackerUpdate extends EntityTickingSystem<EntityStore> {
      @Nonnull
      private final ComponentType<EntityStore, EntityTrackerSystems.Visible> visibleComponentType;
      @Nonnull
      private final ComponentType<EntityStore, Nameplate> componentType;
      @Nonnull
      private final Query<EntityStore> query;

      public EntityTrackerUpdate(
         @Nonnull ComponentType<EntityStore, EntityTrackerSystems.Visible> visibleComponentType, @Nonnull ComponentType<EntityStore, Nameplate> componentType
      ) {
         this.visibleComponentType = visibleComponentType;
         this.componentType = componentType;
         this.query = Query.and(visibleComponentType, componentType);
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
      public void tick(
         float dt,
         int index,
         @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         EntityTrackerSystems.Visible visibleComponent = archetypeChunk.getComponent(index, this.visibleComponentType);

         assert visibleComponent != null;

         Nameplate nameplateComponent = archetypeChunk.getComponent(index, this.componentType);

         assert nameplateComponent != null;

         if (nameplateComponent.consumeNetworkOutdated()) {
            queueUpdatesFor(archetypeChunk.getReferenceTo(index), nameplateComponent, visibleComponent.visibleTo);
         } else if (!visibleComponent.newlyVisibleTo.isEmpty()) {
            queueUpdatesFor(archetypeChunk.getReferenceTo(index), nameplateComponent, visibleComponent.newlyVisibleTo);
         }
      }

      private static void queueUpdatesFor(
         @Nonnull Ref<EntityStore> ref, @Nonnull Nameplate nameplateComponent, @Nonnull Map<Ref<EntityStore>, EntityTrackerSystems.EntityViewer> visibleTo
      ) {
         NameplateUpdate update = new NameplateUpdate(nameplateComponent.getText());

         for (EntityTrackerSystems.EntityViewer viewer : visibleTo.values()) {
            viewer.queueUpdate(ref, update);
         }
      }
   }
}
