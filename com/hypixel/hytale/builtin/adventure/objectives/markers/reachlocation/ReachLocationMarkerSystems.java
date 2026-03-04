package com.hypixel.hytale.builtin.adventure.objectives.markers.reachlocation;

import com.hypixel.hytale.builtin.adventure.objectives.Objective;
import com.hypixel.hytale.builtin.adventure.objectives.ObjectiveDataStore;
import com.hypixel.hytale.builtin.adventure.objectives.ObjectivePlugin;
import com.hypixel.hytale.builtin.adventure.objectives.task.ObjectiveTaskRef;
import com.hypixel.hytale.builtin.adventure.objectives.task.ReachLocationTask;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.OrderPriority;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.component.system.HolderSystem;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.system.PlayerSpatialSystem;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.ObjectList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class ReachLocationMarkerSystems {
   @Nonnull
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   @Nonnull
   private static final ThreadLocal<Set<UUID>> THREAD_LOCAL_TEMP_UUIDS = ThreadLocal.withInitial(HashSet::new);

   public static class EnsureNetworkSendable extends HolderSystem<EntityStore> {
      @Nonnull
      private final ComponentType<EntityStore, NetworkId> networkIdComponentType;
      @Nonnull
      private final Query<EntityStore> query;

      public EnsureNetworkSendable(
         @Nonnull ComponentType<EntityStore, ReachLocationMarker> reachLocationMarkerComponentType,
         @Nonnull ComponentType<EntityStore, NetworkId> networkIdComponentType
      ) {
         this.networkIdComponentType = networkIdComponentType;
         this.query = Query.and(reachLocationMarkerComponentType, Query.not(networkIdComponentType));
      }

      @Override
      public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
         int nextNetworkId = store.getExternalData().takeNextNetworkId();
         holder.addComponent(this.networkIdComponentType, new NetworkId(nextNetworkId));
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

   public static class EntityAdded extends RefSystem<EntityStore> {
      @Nonnull
      private final ComponentType<EntityStore, ReachLocationMarker> reachLocationMarkerComponent;
      @Nonnull
      private final ComponentType<EntityStore, TransformComponent> transformComponentType;
      @Nonnull
      private final Query<EntityStore> query;

      public EntityAdded(
         @Nonnull ComponentType<EntityStore, ReachLocationMarker> reachLocationMarkerComponent,
         @Nonnull ComponentType<EntityStore, TransformComponent> transformComponentType
      ) {
         this.reachLocationMarkerComponent = reachLocationMarkerComponent;
         this.transformComponentType = transformComponentType;
         this.query = Query.and(reachLocationMarkerComponent, transformComponentType);
      }

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return this.query;
      }

      @Override
      public void onEntityAdded(
         @Nonnull Ref<EntityStore> ref, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         ReachLocationMarker reachLocationMarkerComponent = commandBuffer.getComponent(ref, this.reachLocationMarkerComponent);

         assert reachLocationMarkerComponent != null;

         TransformComponent transformComponent = commandBuffer.getComponent(ref, this.transformComponentType);

         assert transformComponent != null;

         Vector3d pos = transformComponent.getPosition();
         ObjectiveDataStore objectiveDataStore = ObjectivePlugin.get().getObjectiveDataStore();

         for (ObjectiveTaskRef<ReachLocationTask> taskRef : objectiveDataStore.getTaskRefsForType(ReachLocationTask.class)) {
            Objective objective = objectiveDataStore.getObjective(taskRef.getObjectiveUUID());
            if (objective != null) {
               taskRef.getObjectiveTask().setupMarker(objective, reachLocationMarkerComponent, pos, commandBuffer);
            }
         }
      }

      @Override
      public void onEntityRemove(
         @Nonnull Ref<EntityStore> ref, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
      }
   }

   public static class Ticking extends EntityTickingSystem<EntityStore> {
      @Nonnull
      private final ComponentType<EntityStore, ReachLocationMarker> reachLocationMarkerComponent;
      @Nonnull
      private final ComponentType<EntityStore, TransformComponent> transformComponentType;
      @Nonnull
      private final ResourceType<EntityStore, SpatialResource<Ref<EntityStore>, EntityStore>> playerSpatialComponent;
      @Nonnull
      private final ComponentType<EntityStore, UUIDComponent> uuidComponentType;
      @Nonnull
      private final Query<EntityStore> query;
      @Nonnull
      private final Set<Dependency<EntityStore>> dependencies;

      public Ticking(
         @Nonnull ComponentType<EntityStore, ReachLocationMarker> reachLocationMarkerComponent,
         @Nonnull ResourceType<EntityStore, SpatialResource<Ref<EntityStore>, EntityStore>> playerSpatialComponent,
         @Nonnull ComponentType<EntityStore, TransformComponent> transformComponentType,
         @Nonnull ComponentType<EntityStore, UUIDComponent> uuidComponentType
      ) {
         this.reachLocationMarkerComponent = reachLocationMarkerComponent;
         this.transformComponentType = transformComponentType;
         this.uuidComponentType = uuidComponentType;
         this.playerSpatialComponent = playerSpatialComponent;
         this.query = Query.and(reachLocationMarkerComponent, transformComponentType);
         this.dependencies = Set.of(new SystemDependency<>(Order.AFTER, PlayerSpatialSystem.class, OrderPriority.CLOSEST));
      }

      @Nonnull
      @Override
      public Set<Dependency<EntityStore>> getDependencies() {
         return this.dependencies;
      }

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return this.query;
      }

      @Override
      public boolean isParallel(int archetypeChunkSize, int taskCount) {
         return false;
      }

      @Override
      public void tick(
         float dt,
         int index,
         @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         ReachLocationMarker reachLocationMarkerComponent = archetypeChunk.getComponent(index, this.reachLocationMarkerComponent);

         assert reachLocationMarkerComponent != null;

         String markerId = reachLocationMarkerComponent.getMarkerId();
         ReachLocationMarkerAsset asset = ReachLocationMarkerAsset.getAssetMap().getAsset(markerId);
         if (asset == null) {
            ReachLocationMarkerSystems.LOGGER
               .at(Level.WARNING)
               .log("No ReachLocationMarkerAsset found for ID '%s', entity removed! %s", markerId, reachLocationMarkerComponent);
            commandBuffer.removeEntity(archetypeChunk.getReferenceTo(index), RemoveReason.REMOVE);
         } else {
            Set<UUID> previousPlayers = ReachLocationMarkerSystems.THREAD_LOCAL_TEMP_UUIDS.get();
            previousPlayers.clear();
            previousPlayers.addAll(reachLocationMarkerComponent.getPlayers());
            Set<UUID> players = reachLocationMarkerComponent.getPlayers();
            players.clear();
            SpatialResource<Ref<EntityStore>, EntityStore> spatialResource = store.getResource(this.playerSpatialComponent);
            ObjectList<Ref<EntityStore>> results = SpatialResource.getThreadLocalReferenceList();
            TransformComponent transformComponent = archetypeChunk.getComponent(index, this.transformComponentType);

            assert transformComponent != null;

            Vector3d position = transformComponent.getPosition();
            spatialResource.getSpatialStructure().ordered(position, asset.getRadius(), results);
            ObjectiveDataStore objectiveDataStore = ObjectivePlugin.get().getObjectiveDataStore();

            for (int i = 0; i < results.size(); i++) {
               Ref<EntityStore> otherEntityReference = (Ref<EntityStore>)results.get(i);
               UUIDComponent otherUuidComponent = commandBuffer.getComponent(otherEntityReference, this.uuidComponentType);
               if (otherUuidComponent != null) {
                  UUID otherUuid = otherUuidComponent.getUuid();
                  players.add(otherUuid);
                  if (!previousPlayers.contains(otherUuid)) {
                     for (ObjectiveTaskRef<ReachLocationTask> taskRef : objectiveDataStore.getTaskRefsForType(ReachLocationTask.class)) {
                        Objective objective = objectiveDataStore.getObjective(taskRef.getObjectiveUUID());
                        if (objective != null) {
                           taskRef.getObjectiveTask().onPlayerReachLocationMarker(store, otherEntityReference, markerId, objective);
                        }
                     }
                  }
               }
            }
         }
      }
   }
}
