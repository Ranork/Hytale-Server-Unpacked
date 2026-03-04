package com.hypixel.hytale.server.npc.systems;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.reference.InvalidatablePersistentRef;
import com.hypixel.hytale.server.core.modules.entity.component.WorldGenId;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathSystems;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.flock.StoredFlock;
import com.hypixel.hytale.server.npc.components.SpawnBeaconReference;
import com.hypixel.hytale.server.npc.components.SpawnMarkerReference;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.spawning.SpawningPlugin;
import com.hypixel.hytale.server.spawning.assets.spawnmarker.config.SpawnMarker;
import com.hypixel.hytale.server.spawning.beacons.LegacySpawnBeaconEntity;
import com.hypixel.hytale.server.spawning.controllers.BeaconSpawnController;
import com.hypixel.hytale.server.spawning.spawnmarkers.SpawnMarkerEntity;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class SpawnReferenceSystems {
   public static class BeaconAddRemoveSystem extends RefSystem<EntityStore> {
      @Nonnull
      private final ComponentType<EntityStore, SpawnBeaconReference> spawnReferenceComponentType;
      @Nonnull
      private final ComponentType<EntityStore, LegacySpawnBeaconEntity> legacySpawnBeaconComponent;
      @Nonnull
      private final ComponentType<EntityStore, NPCEntity> npcComponentType;
      @Nonnull
      private final Query<EntityStore> query;

      public BeaconAddRemoveSystem(
         @Nonnull ComponentType<EntityStore, SpawnBeaconReference> spawnReferenceComponentType,
         @Nonnull ComponentType<EntityStore, LegacySpawnBeaconEntity> legacySpawnBeaconComponent
      ) {
         this.spawnReferenceComponentType = spawnReferenceComponentType;
         this.legacySpawnBeaconComponent = legacySpawnBeaconComponent;
         this.npcComponentType = NPCEntity.getComponentType();
         this.query = Archetype.of(spawnReferenceComponentType, this.npcComponentType);
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
         switch (reason) {
            case LOAD:
               SpawnBeaconReference spawnReferenceComponent = store.getComponent(ref, this.spawnReferenceComponentType);

               assert spawnReferenceComponent != null;

               Ref<EntityStore> markerReference = spawnReferenceComponent.getReference().getEntity(store);
               if (markerReference == null) {
                  return;
               } else {
                  LegacySpawnBeaconEntity legacySpawnBeaconComponent = store.getComponent(markerReference, this.legacySpawnBeaconComponent);

                  assert legacySpawnBeaconComponent != null;

                  NPCEntity npcComponent = store.getComponent(ref, this.npcComponentType);

                  assert npcComponent != null;

                  spawnReferenceComponent.getReference().setEntity(markerReference, store);
                  spawnReferenceComponent.refreshTimeoutCounter();
                  BeaconSpawnController spawnController = legacySpawnBeaconComponent.getSpawnController();
                  if (!spawnController.hasSlots()) {
                     npcComponent.setToDespawn();
                     return;
                  } else {
                     spawnController.notifySpawnedEntityExists(markerReference, commandBuffer);
                  }
               }
            case SPAWN:
         }
      }

      @Override
      public void onEntityRemove(
         @Nonnull Ref<EntityStore> ref, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         switch (reason) {
            case REMOVE:
               SpawnBeaconReference spawnReference = store.getComponent(ref, this.spawnReferenceComponentType);
               if (spawnReference == null) {
                  return;
               } else {
                  Ref<EntityStore> spawnBeaconRef = spawnReference.getReference().getEntity(store);
                  if (spawnBeaconRef == null) {
                     return;
                  } else {
                     LegacySpawnBeaconEntity legacySpawnBeaconComponent = store.getComponent(spawnBeaconRef, this.legacySpawnBeaconComponent);
                     if (legacySpawnBeaconComponent == null) {
                        return;
                     } else {
                        legacySpawnBeaconComponent.getSpawnController().notifyNPCRemoval(ref, store);
                     }
                  }
               }
            case UNLOAD:
         }
      }
   }

   public static class MarkerAddRemoveSystem extends RefSystem<EntityStore> {
      @Nonnull
      private final ComponentType<EntityStore, SpawnMarkerReference> spawnReferenceComponentType;
      @Nonnull
      private final ComponentType<EntityStore, SpawnMarkerEntity> spawnMarkerEntityComponentType;
      @Nonnull
      private final ComponentType<EntityStore, NPCEntity> npcComponentType;
      @Nonnull
      private final ComponentType<EntityStore, WorldGenId> worldGenIdComponentType;
      @Nonnull
      private final ComponentType<EntityStore, UUIDComponent> uuidComponentComponentType;
      @Nonnull
      private final ResourceType<EntityStore, WorldTimeResource> worldTimeResourceResourceType;
      @Nonnull
      private final Query<EntityStore> query;

      public MarkerAddRemoveSystem(
         @Nonnull ComponentType<EntityStore, SpawnMarkerReference> spawnReferenceComponentType,
         @Nonnull ComponentType<EntityStore, SpawnMarkerEntity> spawnMarkerEntityComponentType
      ) {
         this.spawnReferenceComponentType = spawnReferenceComponentType;
         this.spawnMarkerEntityComponentType = spawnMarkerEntityComponentType;
         this.npcComponentType = NPCEntity.getComponentType();
         this.worldGenIdComponentType = WorldGenId.getComponentType();
         this.uuidComponentComponentType = UUIDComponent.getComponentType();
         this.worldTimeResourceResourceType = WorldTimeResource.getResourceType();
         this.query = Archetype.of(spawnReferenceComponentType, this.npcComponentType, this.uuidComponentComponentType);
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
         switch (reason) {
            case LOAD:
               SpawnMarkerReference spawnReferenceComponent = store.getComponent(ref, this.spawnReferenceComponentType);

               assert spawnReferenceComponent != null;

               Ref<EntityStore> markerReference = spawnReferenceComponent.getReference().getEntity(store);
               if (markerReference == null) {
                  return;
               } else {
                  SpawnMarkerEntity markerTypeComponent = store.getComponent(markerReference, this.spawnMarkerEntityComponentType);

                  assert markerTypeComponent != null;

                  NPCEntity npcComponent = store.getComponent(ref, this.npcComponentType);

                  assert npcComponent != null;

                  spawnReferenceComponent.getReference().setEntity(markerReference, store);
                  spawnReferenceComponent.refreshTimeoutCounter();
                  markerTypeComponent.refreshTimeout();
                  WorldGenId worldGenIdComponent = commandBuffer.getComponent(markerReference, this.worldGenIdComponentType);
                  int worldGenId = worldGenIdComponent != null ? worldGenIdComponent.getWorldGenId() : 0;
                  commandBuffer.putComponent(markerReference, WorldGenId.getComponentType(), new WorldGenId(worldGenId));
                  HytaleLogger.Api context = SpawningPlugin.get().getLogger().at(Level.FINE);
                  if (context.isEnabled()) {
                     UUIDComponent uuidComponent = commandBuffer.getComponent(markerReference, this.uuidComponentComponentType);

                     assert uuidComponent != null;

                     UUID uuid = uuidComponent.getUuid();
                     context.log("%s synced up with marker %s", npcComponent.getRoleName(), uuid);
                  }
               }
            case SPAWN:
         }
      }

      @Override
      public void onEntityRemove(
         @Nonnull Ref<EntityStore> ref, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         switch (reason) {
            case REMOVE:
               SpawnMarkerReference spawnReferenceComponent = store.getComponent(ref, this.spawnReferenceComponentType);
               if (spawnReferenceComponent == null) {
                  return;
               } else {
                  Ref<EntityStore> spawnMarkerRef = spawnReferenceComponent.getReference().getEntity(store);
                  if (spawnMarkerRef == null) {
                     return;
                  } else {
                     SpawnMarkerEntity spawnMarkerComponent = store.getComponent(spawnMarkerRef, this.spawnMarkerEntityComponentType);

                     assert spawnMarkerComponent != null;

                     UUIDComponent uuidComponent = store.getComponent(ref, this.uuidComponentComponentType);

                     assert uuidComponent != null;

                     UUID uuid = uuidComponent.getUuid();
                     int spawnCount = spawnMarkerComponent.decrementAndGetSpawnCount();
                     SpawnMarker cachedMarker = spawnMarkerComponent.getCachedMarker();
                     if (spawnCount > 0 && cachedMarker.getDeactivationDistance() > 0.0) {
                        InvalidatablePersistentRef[] newReferences = new InvalidatablePersistentRef[spawnCount];
                        int pos = 0;
                        InvalidatablePersistentRef[] npcReferences = spawnMarkerComponent.getNpcReferences();

                        for (InvalidatablePersistentRef npcRef : npcReferences) {
                           if (!npcRef.getUuid().equals(uuid)) {
                              newReferences[pos++] = npcRef;
                           }
                        }

                        spawnMarkerComponent.setNpcReferences(newReferences);
                     }

                     if (spawnCount <= 0 && !cachedMarker.isRealtimeRespawn()) {
                        Instant instant = store.getResource(this.worldTimeResourceResourceType).getGameTime();
                        Duration gameTimeRespawn = spawnMarkerComponent.pollGameTimeRespawn();
                        if (gameTimeRespawn != null) {
                           instant = instant.plus(gameTimeRespawn);
                        }

                        spawnMarkerComponent.setSpawnAfter(instant);
                        spawnMarkerComponent.setNpcReferences(null);
                        StoredFlock storedFlock = spawnMarkerComponent.getStoredFlock();
                        if (storedFlock != null) {
                           storedFlock.clear();
                        }
                     }
                  }
               }
            case UNLOAD:
         }
      }
   }

   public static class TickingSpawnBeaconSystem extends EntityTickingSystem<EntityStore> {
      @Nonnull
      private static final Set<Dependency<EntityStore>> DEPENDENCIES = Set.of(
         new SystemDependency<>(Order.AFTER, NPCPreTickSystem.class), new SystemDependency<>(Order.BEFORE, DeathSystems.CorpseRemoval.class)
      );
      @Nonnull
      private final ComponentType<EntityStore, SpawnBeaconReference> spawnReferenceComponentType;
      @Nonnull
      private final ComponentType<EntityStore, NPCEntity> npcEntityComponentType;
      @Nonnull
      private final Query<EntityStore> query;

      public TickingSpawnBeaconSystem(@Nonnull ComponentType<EntityStore, SpawnBeaconReference> spawnReferenceComponentType) {
         this.spawnReferenceComponentType = spawnReferenceComponentType;
         this.npcEntityComponentType = NPCEntity.getComponentType();
         this.query = Archetype.of(spawnReferenceComponentType, this.npcEntityComponentType);
      }

      @Nonnull
      @Override
      public Set<Dependency<EntityStore>> getDependencies() {
         return DEPENDENCIES;
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
         NPCEntity npcComponent = archetypeChunk.getComponent(index, this.npcEntityComponentType);

         assert npcComponent != null;

         if (!npcComponent.isDespawning() && !npcComponent.isPlayingDespawnAnim()) {
            SpawnBeaconReference spawnReferenceComponent = archetypeChunk.getComponent(index, this.spawnReferenceComponentType);

            assert spawnReferenceComponent != null;

            if (spawnReferenceComponent.tickMarkerLostTimeoutCounter(dt)) {
               Ref<EntityStore> spawnBeaconRef = spawnReferenceComponent.getReference().getEntity(commandBuffer);
               if (spawnBeaconRef != null) {
                  spawnReferenceComponent.refreshTimeoutCounter();
               } else if (npcComponent.getRole().getStateSupport().isInBusyState()) {
                  spawnReferenceComponent.refreshTimeoutCounter();
               } else {
                  npcComponent.setToDespawn();
                  HytaleLogger.Api context = SpawningPlugin.get().getLogger().at(Level.WARNING);
                  if (context.isEnabled()) {
                     context.log("NPCEntity despawning due to lost marker: %s", archetypeChunk.getReferenceTo(index));
                  }
               }
            }
         }
      }
   }

   public static class TickingSpawnMarkerSystem extends EntityTickingSystem<EntityStore> {
      @Nonnull
      private static final Set<Dependency<EntityStore>> DEPENDENCIES = Set.of(
         new SystemDependency<>(Order.AFTER, NPCPreTickSystem.class), new SystemDependency<>(Order.BEFORE, DeathSystems.CorpseRemoval.class)
      );
      @Nonnull
      private final ComponentType<EntityStore, SpawnMarkerReference> spawnReferenceComponentType;
      @Nonnull
      private final ComponentType<EntityStore, SpawnMarkerEntity> markerTypeComponentType;
      @Nonnull
      private final ComponentType<EntityStore, NPCEntity> npcEntityComponentType;
      @Nonnull
      private final Query<EntityStore> query;

      public TickingSpawnMarkerSystem(
         @Nonnull ComponentType<EntityStore, SpawnMarkerReference> spawnReferenceComponentType,
         @Nonnull ComponentType<EntityStore, SpawnMarkerEntity> markerTypeComponentType
      ) {
         this.spawnReferenceComponentType = spawnReferenceComponentType;
         this.markerTypeComponentType = markerTypeComponentType;
         this.npcEntityComponentType = NPCEntity.getComponentType();
         this.query = Archetype.of(spawnReferenceComponentType, this.npcEntityComponentType);
      }

      @Nonnull
      @Override
      public Set<Dependency<EntityStore>> getDependencies() {
         return DEPENDENCIES;
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
         NPCEntity npcComponent = archetypeChunk.getComponent(index, this.npcEntityComponentType);

         assert npcComponent != null;

         if (!npcComponent.isDespawning() && !npcComponent.isPlayingDespawnAnim()) {
            SpawnMarkerReference spawnReferenceComponent = archetypeChunk.getComponent(index, this.spawnReferenceComponentType);

            assert spawnReferenceComponent != null;

            if (spawnReferenceComponent.tickMarkerLostTimeoutCounter(dt)) {
               Ref<EntityStore> spawnMarkerRef = spawnReferenceComponent.getReference().getEntity(commandBuffer);
               if (spawnMarkerRef != null) {
                  SpawnMarkerEntity spawnMarkerComponent = commandBuffer.getComponent(spawnMarkerRef, this.markerTypeComponentType);

                  assert spawnMarkerComponent != null;

                  spawnReferenceComponent.refreshTimeoutCounter();
                  spawnMarkerComponent.refreshTimeout();
               } else if (npcComponent.getRole().getStateSupport().isInBusyState()) {
                  spawnReferenceComponent.refreshTimeoutCounter();
               } else {
                  npcComponent.setToDespawn();
                  HytaleLogger.Api context = SpawningPlugin.get().getLogger().at(Level.WARNING);
                  if (context.isEnabled()) {
                     context.log("NPCEntity despawning due to lost marker: %s", archetypeChunk.getReferenceTo(index));
                  }
               }
            }
         }
      }
   }
}
