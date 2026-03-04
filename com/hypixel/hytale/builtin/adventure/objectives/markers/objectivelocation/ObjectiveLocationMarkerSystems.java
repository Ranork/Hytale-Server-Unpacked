package com.hypixel.hytale.builtin.adventure.objectives.markers.objectivelocation;

import com.hypixel.hytale.builtin.adventure.objectives.Objective;
import com.hypixel.hytale.builtin.adventure.objectives.ObjectiveDataStore;
import com.hypixel.hytale.builtin.adventure.objectives.ObjectivePlugin;
import com.hypixel.hytale.builtin.adventure.objectives.config.ObjectiveLocationMarkerAsset;
import com.hypixel.hytale.builtin.adventure.objectives.config.triggercondition.ObjectiveLocationTriggerCondition;
import com.hypixel.hytale.builtin.adventure.objectives.task.ObjectiveTask;
import com.hypixel.hytale.builtin.adventure.objectives.task.UseEntityObjectiveTask;
import com.hypixel.hytale.builtin.weather.components.WeatherTracker;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
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
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.packets.assets.TrackOrUpdateObjective;
import com.hypixel.hytale.protocol.packets.assets.UntrackObjective;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.system.PlayerSpatialSystem;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.prefab.PrefabCopyableComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ObjectiveLocationMarkerSystems {
   public static class EnsureNetworkSendableSystem extends HolderSystem<EntityStore> {
      @Nonnull
      private final ComponentType<EntityStore, NetworkId> networkIdComponentType;
      @Nonnull
      private final Query<EntityStore> query;

      public EnsureNetworkSendableSystem(
         @Nonnull ComponentType<EntityStore, ObjectiveLocationMarker> objectiveLocationMarkerComponentType,
         @Nonnull ComponentType<EntityStore, NetworkId> networkIdComponentType
      ) {
         this.networkIdComponentType = networkIdComponentType;
         this.query = Query.and(objectiveLocationMarkerComponentType, Query.not(networkIdComponentType));
      }

      @Override
      public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
         holder.addComponent(this.networkIdComponentType, new NetworkId(store.getExternalData().takeNextNetworkId()));
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

   public static class InitSystem extends RefSystem<EntityStore> {
      @Nonnull
      private final ComponentType<EntityStore, ObjectiveLocationMarker> objectiveLocationMarkerComponent;
      @Nonnull
      private final ComponentType<EntityStore, ModelComponent> modelComponentType;
      @Nonnull
      private final ComponentType<EntityStore, TransformComponent> transformComponentType;
      @Nonnull
      private final ComponentType<EntityStore, PrefabCopyableComponent> prefabCopyableComponentType;
      @Nonnull
      private final Query<EntityStore> query;

      public InitSystem(
         @Nonnull ComponentType<EntityStore, ObjectiveLocationMarker> objectiveLocationMarkerComponent,
         @Nonnull ComponentType<EntityStore, ModelComponent> modelComponentType,
         @Nonnull ComponentType<EntityStore, TransformComponent> transformComponentType,
         @Nonnull ComponentType<EntityStore, PrefabCopyableComponent> prefabCopyableComponentType
      ) {
         this.objectiveLocationMarkerComponent = objectiveLocationMarkerComponent;
         this.modelComponentType = modelComponentType;
         this.transformComponentType = transformComponentType;
         this.prefabCopyableComponentType = prefabCopyableComponentType;
         this.query = Query.and(objectiveLocationMarkerComponent, modelComponentType, transformComponentType);
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
         ObjectiveLocationMarker objectiveLocationMarkerComponent = store.getComponent(ref, this.objectiveLocationMarkerComponent);

         assert objectiveLocationMarkerComponent != null;

         ObjectiveLocationMarkerAsset markerAsset = ObjectiveLocationMarkerAsset.getAssetMap()
            .getAsset(objectiveLocationMarkerComponent.objectiveLocationMarkerId);
         if (markerAsset == null) {
            ObjectivePlugin.get()
               .getLogger()
               .at(Level.WARNING)
               .log("Failed to find ObjectiveLocationMarker '%s'. Entity removed!", objectiveLocationMarkerComponent.objectiveLocationMarkerId);
            commandBuffer.removeEntity(ref, RemoveReason.REMOVE);
         } else {
            if (objectiveLocationMarkerComponent.activeObjectiveUUID != null) {
               Objective activeObjective = ObjectivePlugin.get()
                  .getObjectiveDataStore()
                  .loadObjective(objectiveLocationMarkerComponent.activeObjectiveUUID, store);
               if (activeObjective == null) {
                  ObjectivePlugin.get()
                     .getLogger()
                     .at(Level.WARNING)
                     .log("Failed to load Objective with UUID '%s'. Entity removed!", objectiveLocationMarkerComponent.activeObjectiveUUID);
                  commandBuffer.removeEntity(ref, RemoveReason.REMOVE);
                  return;
               }

               objectiveLocationMarkerComponent.setActiveObjective(activeObjective);
               objectiveLocationMarkerComponent.setUntrackPacket(new UntrackObjective(objectiveLocationMarkerComponent.activeObjectiveUUID));
            }

            TransformComponent transformComponent = store.getComponent(ref, this.transformComponentType);

            assert transformComponent != null;

            Vector3f rotation = transformComponent.getRotation();
            objectiveLocationMarkerComponent.updateLocationMarkerValues(markerAsset, rotation.getYaw(), store);
            ModelComponent modelComponent = store.getComponent(ref, this.modelComponentType);

            assert modelComponent != null;

            Model model = modelComponent.getModel();
            commandBuffer.putComponent(
               ref,
               this.modelComponentType,
               new ModelComponent(
                  new Model(
                     model.getModelAssetId(),
                     model.getScale(),
                     model.getRandomAttachmentIds(),
                     model.getAttachments(),
                     objectiveLocationMarkerComponent.getArea().getBoxForEntryArea(),
                     model.getModel(),
                     model.getTexture(),
                     model.getGradientSet(),
                     model.getGradientId(),
                     model.getEyeHeight(),
                     model.getCrouchOffset(),
                     model.getSittingOffset(),
                     model.getSleepingOffset(),
                     model.getAnimationSetMap(),
                     model.getCamera(),
                     model.getLight(),
                     model.getParticles(),
                     model.getTrails(),
                     model.getPhysicsValues(),
                     model.getDetailBoxes(),
                     model.getPhobia(),
                     model.getPhobiaModelAssetId()
                  )
               )
            );
            commandBuffer.ensureComponent(ref, this.prefabCopyableComponentType);
         }
      }

      @Override
      public void onEntityRemove(
         @Nonnull Ref<EntityStore> ref, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         ObjectiveLocationMarker objectLocationMarkerComponent = store.getComponent(ref, this.objectiveLocationMarkerComponent);

         assert objectLocationMarkerComponent != null;

         Objective activeObjective = objectLocationMarkerComponent.getActiveObjective();
         if (activeObjective != null) {
            ObjectiveDataStore objectiveDataStore = ObjectivePlugin.get().getObjectiveDataStore();
            objectiveDataStore.saveToDisk(objectLocationMarkerComponent.activeObjectiveUUID.toString(), activeObjective);
            objectiveDataStore.unloadObjective(objectLocationMarkerComponent.activeObjectiveUUID);
            if (reason == RemoveReason.REMOVE) {
               commandBuffer.run(theStore -> ObjectivePlugin.get().cancelObjective(objectLocationMarkerComponent.activeObjectiveUUID, theStore));
            }
         }
      }
   }

   public static class TickingSystem extends EntityTickingSystem<EntityStore> {
      @Nonnull
      private final ComponentType<EntityStore, ObjectiveLocationMarker> objectiveLocationMarkerComponentType;
      @Nonnull
      private final ComponentType<EntityStore, PlayerRef> playerRefComponentType;
      @Nonnull
      private final ComponentType<EntityStore, TransformComponent> transformComponentType;
      @Nonnull
      private final ComponentType<EntityStore, WeatherTracker> weatherTrackerComponentType;
      @Nonnull
      private final ComponentType<EntityStore, UUIDComponent> uuidComponentType;
      @Nonnull
      private final ResourceType<EntityStore, SpatialResource<Ref<EntityStore>, EntityStore>> playerSpatialComponent;
      @Nonnull
      private final Query<EntityStore> query;
      @Nonnull
      private final Set<Dependency<EntityStore>> dependencies;

      public TickingSystem(
         @Nonnull ComponentType<EntityStore, ObjectiveLocationMarker> objectiveLocationMarkerComponentType,
         @Nonnull ComponentType<EntityStore, PlayerRef> playerRefComponentType,
         @Nonnull ResourceType<EntityStore, SpatialResource<Ref<EntityStore>, EntityStore>> playerSpatialComponent,
         @Nonnull ComponentType<EntityStore, TransformComponent> transformComponentType,
         @Nonnull ComponentType<EntityStore, WeatherTracker> weatherTrackerComponentType,
         @Nonnull ComponentType<EntityStore, UUIDComponent> uuidComponentType
      ) {
         this.objectiveLocationMarkerComponentType = objectiveLocationMarkerComponentType;
         this.playerRefComponentType = playerRefComponentType;
         this.transformComponentType = transformComponentType;
         this.weatherTrackerComponentType = weatherTrackerComponentType;
         this.uuidComponentType = uuidComponentType;
         this.playerSpatialComponent = playerSpatialComponent;
         this.query = Archetype.of(objectiveLocationMarkerComponentType, transformComponentType, uuidComponentType);
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
      public void tick(float dt, int systemIndex, @Nonnull Store<EntityStore> store) {
         World world = store.getExternalData().getWorld();
         if (world.getWorldConfig().isObjectiveMarkersEnabled()) {
            store.tick(this, dt, systemIndex);
         }
      }

      @Override
      public void tick(
         float dt,
         int index,
         @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         ObjectiveLocationMarker objectiveLocationMarkerComponent = archetypeChunk.getComponent(index, this.objectiveLocationMarkerComponentType);

         assert objectiveLocationMarkerComponent != null;

         TransformComponent transformComponent = archetypeChunk.getComponent(index, this.transformComponentType);

         assert transformComponent != null;

         Ref<EntityStore> entityReference = archetypeChunk.getReferenceTo(index);
         Vector3d position = transformComponent.getPosition();
         Objective activeObjective = objectiveLocationMarkerComponent.getActiveObjective();
         if (activeObjective == null) {
            UUIDComponent uuidComponent = archetypeChunk.getComponent(index, this.uuidComponentType);

            assert uuidComponent != null;

            UUID uuid = uuidComponent.getUuid();
            this.setupMarker(store, objectiveLocationMarkerComponent, entityReference, position, uuid, commandBuffer);
         } else if (!activeObjective.isCompleted()) {
            SpatialResource<Ref<EntityStore>, EntityStore> spatialResource = store.getResource(this.playerSpatialComponent);
            ObjectList<Ref<EntityStore>> playerRefs = SpatialResource.getThreadLocalReferenceList();
            objectiveLocationMarkerComponent.area.getPlayersInExitArea(spatialResource, playerRefs, position);
            HashSet<UUID> playersInExitArea = new HashSet<>(playerRefs.size());
            PlayerRef[] playersInEntryArea = new PlayerRef[playerRefs.size()];
            int playersInEntryAreaSize = 0;
            ObjectListIterator playerUUIDs = playerRefs.iterator();

            while (playerUUIDs.hasNext()) {
               Ref<EntityStore> playerRef = (Ref<EntityStore>)playerUUIDs.next();
               PlayerRef playerRefComponent = commandBuffer.getComponent(playerRef, this.playerRefComponentType);
               if (playerRefComponent != null) {
                  UUIDComponent playerUuidComponent = commandBuffer.getComponent(playerRef, this.uuidComponentType);
                  if (playerUuidComponent != null) {
                     TransformComponent playerTransformComponent = commandBuffer.getComponent(playerRef, this.transformComponentType);
                     if (playerTransformComponent != null) {
                        WeatherTracker playerWeatherTrackerComponent = commandBuffer.getComponent(playerRef, this.weatherTrackerComponentType);
                        if (playerWeatherTrackerComponent != null
                           && isPlayerInSpecificEnvironment(
                              objectiveLocationMarkerComponent, playerWeatherTrackerComponent, playerTransformComponent, commandBuffer
                           )) {
                           playersInExitArea.add(playerUuidComponent.getUuid());
                           if (objectiveLocationMarkerComponent.area.isPlayerInEntryArea(playerTransformComponent.getPosition(), position)) {
                              playersInEntryArea[playersInEntryAreaSize++] = playerRefComponent;
                           }
                        }
                     }
                  }
               }
            }

            Set<UUID> playerUUIDsx = activeObjective.getPlayerUUIDs();
            Set<UUID> activePlayerUUIDs = activeObjective.getActivePlayerUUIDs();
            String objectiveId = activeObjective.getObjectiveId();
            updateIncomingPlayers(playersInEntryArea, playersInEntryAreaSize, objectiveLocationMarkerComponent, playerUUIDsx, activePlayerUUIDs, objectiveId);
            updateOutgoingPlayers(playersInExitArea, objectiveLocationMarkerComponent, activePlayerUUIDs, objectiveId);
         } else {
            commandBuffer.removeEntity(entityReference, RemoveReason.REMOVE);
         }
      }

      private void setupMarker(
         @Nonnull Store<EntityStore> store,
         @Nonnull ObjectiveLocationMarker entity,
         @Nonnull Ref<EntityStore> entityReference,
         @Nonnull Vector3d position,
         @Nonnull UUID uuid,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         if (entity.triggerConditions != null && entity.triggerConditions.length > 0) {
            SpatialResource<Ref<EntityStore>, EntityStore> spatialResource = store.getResource(this.playerSpatialComponent);
            if (!entity.area.hasPlayerInExitArea(spatialResource, this.playerRefComponentType, position, commandBuffer)) {
               return;
            }

            for (ObjectiveLocationTriggerCondition triggerCondition : entity.triggerConditions) {
               if (!triggerCondition.isConditionMet(commandBuffer, entityReference, entity)) {
                  return;
               }
            }
         }

         ObjectiveLocationMarkerAsset objectiveLocationMarkerAsset = ObjectiveLocationMarkerAsset.getAssetMap().getAsset(entity.objectiveLocationMarkerId);
         if (objectiveLocationMarkerAsset == null) {
            ObjectivePlugin.get()
               .getLogger()
               .at(Level.WARNING)
               .log("Could not find ObjectiveLocationMarker '%s'. Entity removed!", entity.objectiveLocationMarkerId);
            commandBuffer.removeEntity(entityReference, RemoveReason.REMOVE);
         } else {
            World world = store.getExternalData().getWorld();
            Objective objective = objectiveLocationMarkerAsset.getObjectiveTypeSetup().setup(new HashSet<>(), world.getWorldConfig().getUuid(), uuid, store);
            if (objective == null) {
               ObjectivePlugin.get()
                  .getLogger()
                  .at(Level.WARNING)
                  .log("Objective failed to setup for ObjectiveLocationMarker '%s'. Entity removed!", entity.objectiveLocationMarkerId);
               commandBuffer.removeEntity(entityReference, RemoveReason.REMOVE);
            } else {
               entity.setActiveObjective(objective);
               entity.activeObjectiveUUID = objective.getObjectiveUUID();
               entity.setUntrackPacket(new UntrackObjective(entity.activeObjectiveUUID));
            }
         }
      }

      private static void updateIncomingPlayers(
         @Nonnull PlayerRef[] playersInArea,
         int playersInAreaSize,
         @Nonnull ObjectiveLocationMarker entity,
         @Nonnull Set<UUID> playerUUIDs,
         @Nonnull Set<UUID> activePlayerUUIDs,
         @Nonnull String objectiveId
      ) {
         if (playersInArea.length != 0) {
            TrackOrUpdateObjective trackPacket = null;
            ObjectivePlugin objectiveModule = ObjectivePlugin.get();
            HytaleLogger logger = objectiveModule.getLogger();
            ObjectiveDataStore objectiveDataStore = objectiveModule.getObjectiveDataStore();

            for (int i = 0; i < playersInAreaSize; i++) {
               PlayerRef playerRef = playersInArea[i];
               UUID playerUUID = playerRef.getUuid();
               if (activePlayerUUIDs.add(playerUUID)) {
                  playerUUIDs.add(playerUUID);
                  logger.at(Level.FINE)
                     .log(
                        "Player '%s' joined the objective area for marker '%s', current objective '%s' with UUID '%s'",
                        playerRef.getUsername(),
                        entity.objectiveLocationMarkerId,
                        objectiveId,
                        entity.activeObjectiveUUID
                     );
                  if (trackPacket == null) {
                     trackPacket = new TrackOrUpdateObjective(entity.getActiveObjective().toPacket());
                  }

                  playerRef.getPacketHandler().writeNoCache(trackPacket);

                  for (ObjectiveTask task : entity.getActiveObjective().getCurrentTasks()) {
                     if (task instanceof UseEntityObjectiveTask) {
                        objectiveDataStore.addEntityTaskForPlayer(playerUUID, ((UseEntityObjectiveTask)task).getAsset().getTaskId(), entity.activeObjectiveUUID);
                     }
                  }
               }
            }
         }
      }

      private static void updateOutgoingPlayers(
         @Nonnull Set<UUID> playersInArea, @Nonnull ObjectiveLocationMarker entity, @Nullable Set<UUID> activePlayerUUIDs, @Nonnull String objectiveId
      ) {
         if (activePlayerUUIDs != null && !activePlayerUUIDs.isEmpty()) {
            HytaleLogger logger = ObjectivePlugin.get().getLogger();
            Iterator<UUID> iterator = activePlayerUUIDs.iterator();
            Universe universe = Universe.get();

            while (iterator.hasNext()) {
               UUID uuid = iterator.next();
               if (!playersInArea.contains(uuid)) {
                  iterator.remove();
                  untrackEntityObjectiveForPlayer(entity, uuid);
                  PlayerRef playerRef = universe.getPlayer(uuid);
                  logger.at(Level.FINE)
                     .log(
                        "Player '%s' left the objective area for marker '%s', current objective '%s' with UUID '%s'",
                        playerRef == null ? uuid : playerRef.getUsername(),
                        entity.objectiveLocationMarkerId,
                        objectiveId,
                        entity.activeObjectiveUUID
                     );
                  if (playerRef != null) {
                     playerRef.getPacketHandler().write(entity.getUntrackPacket());
                  }
               }
            }
         }
      }

      private static boolean isPlayerInSpecificEnvironment(
         @Nonnull ObjectiveLocationMarker entity,
         @Nonnull WeatherTracker weatherTracker,
         @Nonnull TransformComponent transformComponent,
         @Nonnull ComponentAccessor<EntityStore> componentAccessor
      ) {
         if (entity.environmentIndexes == null) {
            return true;
         } else {
            weatherTracker.updateEnvironment(transformComponent, componentAccessor);
            int environmentIndex = weatherTracker.getEnvironmentId();
            return Arrays.binarySearch(entity.environmentIndexes, environmentIndex) >= 0;
         }
      }

      private static void untrackEntityObjectiveForPlayer(@Nonnull ObjectiveLocationMarker entity, @Nonnull UUID playerUUID) {
         ObjectiveDataStore objectiveDataStore = ObjectivePlugin.get().getObjectiveDataStore();

         for (ObjectiveTask task : entity.getActiveObjective().getCurrentTasks()) {
            if (task instanceof UseEntityObjectiveTask useEntityObjectiveTask) {
               String taskId = useEntityObjectiveTask.getAsset().getTaskId();
               objectiveDataStore.removeEntityTaskForPlayer(entity.activeObjectiveUUID, taskId, playerUUID);
            }
         }
      }
   }
}
