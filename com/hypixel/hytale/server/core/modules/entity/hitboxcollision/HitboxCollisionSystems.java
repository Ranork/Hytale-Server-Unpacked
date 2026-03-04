package com.hypixel.hytale.server.core.modules.entity.hitboxcollision;

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
import com.hypixel.hytale.protocol.HitboxCollisionUpdate;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.tracker.EntityTrackerSystems;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class HitboxCollisionSystems {
   public static class EntityTrackerRemove extends RefChangeSystem<EntityStore, HitboxCollision> {
      private final ComponentType<EntityStore, HitboxCollision> componentType;
      private final ComponentType<EntityStore, EntityTrackerSystems.Visible> visibleComponentType;
      @Nonnull
      private final Query<EntityStore> query;

      public EntityTrackerRemove(
         ComponentType<EntityStore, EntityTrackerSystems.Visible> visibleComponentType, ComponentType<EntityStore, HitboxCollision> componentType
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
      public ComponentType<EntityStore, HitboxCollision> componentType() {
         return this.componentType;
      }

      public void onComponentAdded(
         @Nonnull Ref<EntityStore> ref,
         @Nonnull HitboxCollision component,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
      }

      public void onComponentSet(
         @Nonnull Ref<EntityStore> ref,
         HitboxCollision oldComponent,
         @Nonnull HitboxCollision newComponent,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
      }

      public void onComponentRemoved(
         @Nonnull Ref<EntityStore> ref,
         @Nonnull HitboxCollision component,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         for (EntityTrackerSystems.EntityViewer viewer : store.getComponent(ref, this.visibleComponentType).visibleTo.values()) {
            viewer.queueRemove(ref, ComponentUpdateType.HitboxCollision);
         }
      }
   }

   public static class EntityTrackerUpdate extends EntityTickingSystem<EntityStore> {
      private final ComponentType<EntityStore, EntityTrackerSystems.Visible> visibleComponentType;
      private final ComponentType<EntityStore, HitboxCollision> componentType;
      @Nonnull
      private final Query<EntityStore> query;

      public EntityTrackerUpdate(
         ComponentType<EntityStore, EntityTrackerSystems.Visible> visibleComponentType, ComponentType<EntityStore, HitboxCollision> componentType
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
         EntityTrackerSystems.Visible visible = archetypeChunk.getComponent(index, this.visibleComponentType);
         HitboxCollision hitboxCollision = archetypeChunk.getComponent(index, this.componentType);
         if (hitboxCollision.consumeNetworkOutdated()) {
            queueUpdatesFor(archetypeChunk.getReferenceTo(index), hitboxCollision, visible.visibleTo);
         } else if (!visible.newlyVisibleTo.isEmpty()) {
            queueUpdatesFor(archetypeChunk.getReferenceTo(index), hitboxCollision, visible.newlyVisibleTo);
         }
      }

      private static void queueUpdatesFor(
         Ref<EntityStore> ref, @Nonnull HitboxCollision hitboxCollision, @Nonnull Map<Ref<EntityStore>, EntityTrackerSystems.EntityViewer> visibleTo
      ) {
         HitboxCollisionUpdate update = new HitboxCollisionUpdate(hitboxCollision.getHitboxCollisionConfigIndex());

         for (EntityTrackerSystems.EntityViewer viewer : visibleTo.values()) {
            viewer.queueUpdate(ref, update);
         }
      }
   }

   public static class Setup extends HolderSystem<EntityStore> {
      private final ComponentType<EntityStore, HitboxCollision> componentType;
      private final ComponentType<EntityStore, Player> playerComponentType;
      @Nonnull
      private final Query<EntityStore> query;

      public Setup(ComponentType<EntityStore, HitboxCollision> componentType, ComponentType<EntityStore, Player> playerComponentType) {
         this.componentType = componentType;
         this.playerComponentType = playerComponentType;
         this.query = Query.and(playerComponentType, Query.not(componentType));
      }

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return this.query;
      }

      @Override
      public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
         World world = store.getExternalData().getWorld();
         int hitboxCollisionConfigIndex = world.getGameplayConfig().getPlayerConfig().getHitboxCollisionConfigIndex();
         if (hitboxCollisionConfigIndex != -1) {
            holder.addComponent(this.componentType, new HitboxCollision(HitboxCollisionConfig.getAssetMap().getAsset(hitboxCollisionConfigIndex)));
         }
      }

      @Override
      public void onEntityRemoved(@Nonnull Holder<EntityStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store) {
      }
   }
}
