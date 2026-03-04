package com.hypixel.hytale.server.core.modules.entityui;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.HolderSystem;
import com.hypixel.hytale.component.system.RefChangeSystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.protocol.ComponentUpdateType;
import com.hypixel.hytale.protocol.UIComponentsUpdate;
import com.hypixel.hytale.server.core.modules.entity.AllLegacyLivingEntityTypesQuery;
import com.hypixel.hytale.server.core.modules.entity.tracker.EntityTrackerSystems;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class UIComponentSystems {
   public static class Remove extends RefChangeSystem<EntityStore, UIComponentList> {
      private final ComponentType<EntityStore, UIComponentList> uiComponentListComponentType;
      private final ComponentType<EntityStore, EntityTrackerSystems.Visible> visibleComponentType;
      @Nonnull
      private final Query<EntityStore> query;

      public Remove(ComponentType<EntityStore, EntityTrackerSystems.Visible> visibleComponentType, ComponentType<EntityStore, UIComponentList> componentType) {
         this.visibleComponentType = visibleComponentType;
         this.uiComponentListComponentType = componentType;
         this.query = Query.and(visibleComponentType, componentType);
      }

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return this.query;
      }

      @Nonnull
      @Override
      public ComponentType<EntityStore, UIComponentList> componentType() {
         return this.uiComponentListComponentType;
      }

      public void onComponentAdded(
         @Nonnull Ref<EntityStore> ref,
         @Nonnull UIComponentList component,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
      }

      public void onComponentSet(
         @Nonnull Ref<EntityStore> ref,
         UIComponentList oldComponent,
         @Nonnull UIComponentList newComponent,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
      }

      public void onComponentRemoved(
         @Nonnull Ref<EntityStore> ref,
         @Nonnull UIComponentList component,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         for (EntityTrackerSystems.EntityViewer viewer : store.getComponent(ref, this.visibleComponentType).visibleTo.values()) {
            viewer.queueRemove(ref, ComponentUpdateType.UIComponents);
         }
      }
   }

   public static class Setup extends HolderSystem<EntityStore> {
      private final ComponentType<EntityStore, UIComponentList> uiComponentListComponentType;

      public Setup(ComponentType<EntityStore, UIComponentList> uiComponentListType) {
         this.uiComponentListComponentType = uiComponentListType;
      }

      @Override
      public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
         UIComponentList components = holder.getComponent(this.uiComponentListComponentType);
         if (components == null) {
            components = holder.ensureAndGetComponent(this.uiComponentListComponentType);
            components.update();
         }
      }

      @Override
      public void onEntityRemoved(@Nonnull Holder<EntityStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store) {
      }

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return AllLegacyLivingEntityTypesQuery.INSTANCE;
      }
   }

   public static class Update extends EntityTickingSystem<EntityStore> {
      private final ComponentType<EntityStore, EntityTrackerSystems.Visible> visibleComponentType;
      private final ComponentType<EntityStore, UIComponentList> uiComponentListComponentType;
      @Nonnull
      private final Query<EntityStore> query;

      public Update(
         ComponentType<EntityStore, EntityTrackerSystems.Visible> visibleComponentType,
         ComponentType<EntityStore, UIComponentList> uiComponentListComponentType
      ) {
         this.visibleComponentType = visibleComponentType;
         this.uiComponentListComponentType = uiComponentListComponentType;
         this.query = Query.and(visibleComponentType, uiComponentListComponentType);
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
         EntityTrackerSystems.Visible visible = archetypeChunk.getComponent(index, this.visibleComponentType);
         UIComponentList uiComponentList = archetypeChunk.getComponent(index, this.uiComponentListComponentType);
         if (!visible.newlyVisibleTo.isEmpty()) {
            queueUpdatesFor(archetypeChunk.getReferenceTo(index), uiComponentList, visible.newlyVisibleTo);
         }
      }

      private static void queueUpdatesFor(
         Ref<EntityStore> ref, @Nonnull UIComponentList uiComponentList, @Nonnull Map<Ref<EntityStore>, EntityTrackerSystems.EntityViewer> visibleTo
      ) {
         UIComponentsUpdate update = new UIComponentsUpdate(uiComponentList.getComponentIds());

         for (EntityTrackerSystems.EntityViewer viewer : visibleTo.values()) {
            viewer.queueUpdate(ref, update);
         }
      }
   }
}
