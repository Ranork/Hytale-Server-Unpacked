package com.hypixel.hytale.server.core.modules.entity.dynamiclight;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.HolderSystem;
import com.hypixel.hytale.component.system.RefChangeSystem;
import com.hypixel.hytale.protocol.ComponentUpdateType;
import com.hypixel.hytale.server.core.modules.entity.component.DynamicLight;
import com.hypixel.hytale.server.core.modules.entity.component.PersistentDynamicLight;
import com.hypixel.hytale.server.core.modules.entity.tracker.EntityTrackerSystems;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class DynamicLightSystems {
   public static class EntityTrackerRemove extends RefChangeSystem<EntityStore, DynamicLight> {
      private final ComponentType<EntityStore, EntityTrackerSystems.Visible> visibleComponentType;

      public EntityTrackerRemove(ComponentType<EntityStore, EntityTrackerSystems.Visible> visibleComponentType) {
         this.visibleComponentType = visibleComponentType;
      }

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return this.visibleComponentType;
      }

      @Nonnull
      @Override
      public ComponentType<EntityStore, DynamicLight> componentType() {
         return DynamicLight.getComponentType();
      }

      public void onComponentAdded(
         @Nonnull Ref<EntityStore> ref, @Nonnull DynamicLight component, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
      }

      public void onComponentSet(
         @Nonnull Ref<EntityStore> ref,
         DynamicLight oldComponent,
         @Nonnull DynamicLight newComponent,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
      }

      public void onComponentRemoved(
         @Nonnull Ref<EntityStore> ref, @Nonnull DynamicLight component, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         EntityTrackerSystems.Visible visible = commandBuffer.getComponent(ref, this.visibleComponentType);
         if (visible != null) {
            for (EntityTrackerSystems.EntityViewer viewer : visible.visibleTo.values()) {
               viewer.queueRemove(ref, ComponentUpdateType.DynamicLight);
            }
         }
      }
   }

   public static class Setup extends HolderSystem<EntityStore> {
      private final ComponentType<EntityStore, DynamicLight> dynamicLightComponentType = DynamicLight.getComponentType();
      private final ComponentType<EntityStore, PersistentDynamicLight> persistentDynamicLightComponentType = PersistentDynamicLight.getComponentType();
      private final Query<EntityStore> query = Query.and(this.persistentDynamicLightComponentType, Query.not(this.dynamicLightComponentType));

      @Override
      public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
         PersistentDynamicLight persistentLight = holder.getComponent(this.persistentDynamicLightComponentType);
         holder.putComponent(this.dynamicLightComponentType, new DynamicLight(persistentLight.getColorLight()));
      }

      @Override
      public void onEntityRemoved(@Nonnull Holder<EntityStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store) {
      }

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return this.query;
      }
   }
}
