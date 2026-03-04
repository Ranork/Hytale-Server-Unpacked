package com.hypixel.hytale.server.spawning.beacons;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.OrderPriority;
import com.hypixel.hytale.component.dependency.RootDependency;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.random.RandomExtra;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.asset.type.responsecurve.ScaledXYResponseCurve;
import com.hypixel.hytale.server.core.entity.Frozen;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.system.PlayerSpatialSystem;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.prefab.PrefabCopyableComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.flock.FlockPlugin;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.spawning.SpawningContext;
import com.hypixel.hytale.server.spawning.SpawningPlugin;
import com.hypixel.hytale.server.spawning.assets.spawns.config.BeaconNPCSpawn;
import com.hypixel.hytale.server.spawning.controllers.BeaconSpawnController;
import com.hypixel.hytale.server.spawning.controllers.SpawnControllerSystem;
import com.hypixel.hytale.server.spawning.controllers.SpawnJobSystem;
import com.hypixel.hytale.server.spawning.jobs.NPCBeaconSpawnJob;
import com.hypixel.hytale.server.spawning.util.FloodFillEntryPoolProviderSimple;
import com.hypixel.hytale.server.spawning.util.FloodFillPositionSelector;
import com.hypixel.hytale.server.spawning.wrappers.BeaconSpawnWrapper;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectList;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SpawnBeaconSystems {
   public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   public static final double[] POSITION_CALCULATION_DELAY_RANGE = new double[]{0.0, 1.0};
   private static final double LOAD_TIME_SPAWN_DELAY = 15.0;

   public static class CheckDespawn extends EntityTickingSystem<EntityStore> {
      private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
      private final ComponentType<EntityStore, LegacySpawnBeaconEntity> componentType;
      @Nullable
      private final ComponentType<EntityStore, NPCEntity> npcComponentType;
      @Nonnull
      private final Query<EntityStore> query;

      public CheckDespawn(
         ComponentType<EntityStore, LegacySpawnBeaconEntity> componentType, ComponentType<EntityStore, InitialBeaconDelay> initialBeaconDelayComponentType
      ) {
         this.componentType = componentType;
         this.npcComponentType = NPCEntity.getComponentType();
         this.query = Query.and(componentType, UUIDComponent.getComponentType(), Query.not(initialBeaconDelayComponentType));
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
         LegacySpawnBeaconEntity legacySpawnBeaconComponent = archetypeChunk.getComponent(index, this.componentType);

         assert legacySpawnBeaconComponent != null;

         UUIDComponent uuidComponent = archetypeChunk.getComponent(index, UUIDComponent.getComponentType());

         assert uuidComponent != null;

         BeaconSpawnController spawnController = legacySpawnBeaconComponent.getSpawnController();
         Instant despawnSelfAfter = legacySpawnBeaconComponent.getDespawnSelfAfter();
         WorldTimeResource worldTimeResource = commandBuffer.getResource(WorldTimeResource.getResourceType());
         if (despawnSelfAfter != null && worldTimeResource.getGameTime().isAfter(despawnSelfAfter)) {
            this.despawnAllSpawns(spawnController.getSpawnedEntities(), commandBuffer);
            commandBuffer.removeEntity(archetypeChunk.getReferenceTo(index), RemoveReason.REMOVE);
         } else {
            World world = store.getExternalData().getWorld();
            BeaconSpawnWrapper spawnWrapper = legacySpawnBeaconComponent.getSpawnWrapper();
            if (spawnWrapper.shouldDespawn(world, worldTimeResource)) {
               LOGGER.at(Level.FINE).log("Removing spawn beacon %s due to matching despawn parameters", uuidComponent.getUuid());
               this.despawnAllSpawns(spawnController.getSpawnedEntities(), commandBuffer);
               commandBuffer.removeEntity(archetypeChunk.getReferenceTo(index), RemoveReason.REMOVE);
            }
         }
      }

      private void despawnAllSpawns(@Nonnull List<Ref<EntityStore>> spawnedEntities, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
         for (int i = 0; i < spawnedEntities.size(); i++) {
            Ref<EntityStore> ref = spawnedEntities.get(i);
            if (ref.isValid()) {
               NPCEntity npc = commandBuffer.getComponent(ref, this.npcComponentType);
               if (npc != null && !npc.getRole().getStateSupport().isInBusyState() && !npc.isDespawning()) {
                  npc.setToDespawn();
               }
            }
         }

         spawnedEntities.clear();
      }
   }

   public static class ControllerTick extends SpawnControllerSystem<NPCBeaconSpawnJob, BeaconSpawnController> {
      private static final ThreadLocal<List<NPCEntity>> THREAD_LOCAL_VALIDATED_ENTITIES = ThreadLocal.withInitial(ArrayList::new);
      private final ComponentType<EntityStore, LegacySpawnBeaconEntity> componentType;
      private final ComponentType<EntityStore, FloodFillPositionSelector> floodFillPositionSelectorComponentType;
      private final ComponentType<EntityStore, PlayerRef> playerRefComponentType;
      @Nullable
      private final ComponentType<EntityStore, NPCEntity> npcComponentType;
      private final ComponentType<EntityStore, TransformComponent> transformComponentType;
      private final ComponentType<EntityStore, DeathComponent> deathComponentComponentType;
      private final ComponentType<EntityStore, UUIDComponent> uuidComponentType = UUIDComponent.getComponentType();
      private final ResourceType<EntityStore, SpatialResource<Ref<EntityStore>, EntityStore>> playerSpatialResource;
      @Nonnull
      private final Query<EntityStore> query;
      @Nonnull
      private final Set<Dependency<EntityStore>> dependencies;

      public ControllerTick(
         ComponentType<EntityStore, LegacySpawnBeaconEntity> componentType,
         ComponentType<EntityStore, FloodFillPositionSelector> floodFillPositionSelectorComponentType,
         ComponentType<EntityStore, InitialBeaconDelay> initialBeaconDelayComponentType
      ) {
         this.componentType = componentType;
         this.floodFillPositionSelectorComponentType = floodFillPositionSelectorComponentType;
         this.playerRefComponentType = PlayerRef.getComponentType();
         this.npcComponentType = NPCEntity.getComponentType();
         this.transformComponentType = TransformComponent.getComponentType();
         this.deathComponentComponentType = DeathComponent.getComponentType();
         this.playerSpatialResource = EntityModule.get().getPlayerSpatialResourceType();
         this.query = Query.and(componentType, floodFillPositionSelectorComponentType, this.transformComponentType, Query.not(initialBeaconDelayComponentType));
         this.dependencies = Set.of(
            new SystemDependency<>(Order.AFTER, PlayerSpatialSystem.class, OrderPriority.CLOSEST),
            new SystemDependency<>(Order.AFTER, SpawnBeaconSystems.PositionSelectorUpdate.class)
         );
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
         FloodFillPositionSelector positionSelectorComponent = archetypeChunk.getComponent(index, this.floodFillPositionSelectorComponentType);

         assert positionSelectorComponent != null;

         if (!positionSelectorComponent.shouldRebuildCache()) {
            TransformComponent transformComponent = archetypeChunk.getComponent(index, this.transformComponentType);

            assert transformComponent != null;

            Vector3d position = transformComponent.getPosition();
            LegacySpawnBeaconEntity legacySpawnBeaconComponent = archetypeChunk.getComponent(index, this.componentType);

            assert legacySpawnBeaconComponent != null;

            legacySpawnBeaconComponent.setSpawnAttempts(0);
            List<NPCEntity> validatedEntityList = THREAD_LOCAL_VALIDATED_ENTITIES.get();
            BeaconSpawnController spawnController = legacySpawnBeaconComponent.getSpawnController();
            List<Ref<EntityStore>> spawnedEntities = spawnController.getSpawnedEntities();
            if (!spawnedEntities.isEmpty()) {
               Object2DoubleMap<Ref<EntityStore>> entityTimeoutCounter = spawnController.getEntityTimeoutCounter();
               boolean despawnNPCsIfIdle = spawnController.isDespawnNPCsIfIdle();
               double beaconRadiusSquared = spawnController.getBeaconRadiusSquared();
               double despawnNPCAfterTimeout = spawnController.getDespawnNPCAfterTimeout();

               for (int i = spawnedEntities.size() - 1; i >= 0; i--) {
                  Ref<EntityStore> spawnedEntityReference = spawnedEntities.get(i);
                  if (!spawnedEntityReference.isValid()) {
                     spawnedEntities.remove(i);
                  } else {
                     NPCEntity spawnedEntityNpcComponent = commandBuffer.getComponent(spawnedEntityReference, this.npcComponentType);
                     if (spawnedEntityNpcComponent != null && !spawnedEntityNpcComponent.isDespawning()) {
                        Role role = spawnedEntityNpcComponent.getRole();
                        if (role != null) {
                           boolean hasTarget = role.getMarkedEntitySupport()
                              .hasMarkedEntityInSlot(legacySpawnBeaconComponent.getSpawnWrapper().getSpawn().getTargetSlot());
                           TransformComponent spawnedEntityTransformComponent = commandBuffer.getComponent(spawnedEntityReference, this.transformComponentType);

                           assert spawnedEntityTransformComponent != null;

                           Vector3d npcPosition = spawnedEntityTransformComponent.getPosition();
                           double beaconDistance = npcPosition.distanceSquaredTo(position);
                           if ((despawnNPCsIfIdle && !hasTarget || beaconDistance > beaconRadiusSquared) && !role.getStateSupport().isInBusyState()) {
                              double timeout = entityTimeoutCounter.mergeDouble(spawnedEntityReference, dt, Double::sum);
                              if (timeout >= despawnNPCAfterTimeout) {
                                 spawnedEntityNpcComponent.setToDespawn();
                              }
                           } else {
                              entityTimeoutCounter.put(spawnedEntityReference, 0.0);
                              validatedEntityList.add(spawnedEntityNpcComponent);
                           }
                        }
                     }
                  }
               }
            }

            WorldTimeResource timeManager = commandBuffer.getResource(WorldTimeResource.getResourceType());
            if (!isReadyToRespawn(legacySpawnBeaconComponent, timeManager)) {
               validatedEntityList.clear();
            } else {
               int y = MathUtil.floor(position.getY());
               BeaconSpawnWrapper spawnWrapper = legacySpawnBeaconComponent.getSpawnWrapper();
               int[] yRange = spawnWrapper.getSpawn().getYRange();
               double minY = y + yRange[0];
               double maxY = y + yRange[1];
               SpatialResource<Ref<EntityStore>, EntityStore> spatialResource = store.getResource(this.playerSpatialResource);
               ObjectList<Ref<EntityStore>> results = SpatialResource.getThreadLocalReferenceList();
               spatialResource.getSpatialStructure().collect(position, spawnWrapper.getBeaconRadius(), results);
               List<PlayerRef> playersInRegion = spawnController.getPlayersInRegion();

               for (int ix = 0; ix < results.size(); ix++) {
                  Ref<EntityStore> result = (Ref<EntityStore>)results.get(ix);
                  if (result.isValid()) {
                     PlayerRef resultPlayerComponent = commandBuffer.getComponent(result, this.playerRefComponentType);

                     assert resultPlayerComponent != null;

                     TransformComponent resultTransformComponent = commandBuffer.getComponent(result, this.transformComponentType);

                     assert resultTransformComponent != null;

                     double yPos = resultTransformComponent.getPosition().getY();
                     if (!(yPos < minY) && !(yPos > maxY) && !commandBuffer.getArchetype(result).contains(this.deathComponentComponentType)) {
                        playersInRegion.add(resultPlayerComponent);
                     }
                  }
               }

               legacySpawnBeaconComponent.setLastPlayerCount(playersInRegion.size());
               Ref<EntityStore> spawnBeaconRef = archetypeChunk.getReferenceTo(index);
               if (playersInRegion.isEmpty()) {
                  LegacySpawnBeaconEntity.setToDespawnAfter(spawnBeaconRef, spawnController.getDespawnBeaconAfterTimeout(), commandBuffer);
                  validatedEntityList.clear();
               } else {
                  boolean playersInSpawnRange = false;

                  for (int ixx = 0; ixx < playersInRegion.size(); ixx++) {
                     Ref<EntityStore> playerReference = playersInRegion.get(ixx).getReference();
                     TransformComponent playerTransformComponent = commandBuffer.getComponent(playerReference, this.transformComponentType);

                     assert playerTransformComponent != null;

                     Vector3d playerPos = playerTransformComponent.getPosition();
                     if (playerPos.distanceSquaredTo(position) <= spawnController.getSpawnRadiusSquared()) {
                        playersInSpawnRange = true;
                        break;
                     }
                  }

                  if (playersInSpawnRange) {
                     LegacySpawnBeaconEntity.clearDespawnTimer(spawnBeaconRef, commandBuffer);
                  } else {
                     LegacySpawnBeaconEntity.setToDespawnAfter(spawnBeaconRef, spawnController.getDespawnBeaconAfterTimeout(), commandBuffer);
                  }

                  ScaledXYResponseCurve maxSpawnScaleCurve = spawnWrapper.getSpawn().getMaxSpawnsScalingCurve();
                  int baseMaxTotalSpawns = spawnController.getBaseMaxTotalSpawns();
                  int currentScaledMaxTotalSpawns = maxSpawnScaleCurve != null
                     ? baseMaxTotalSpawns + MathUtil.floor(maxSpawnScaleCurve.computeY(playersInRegion.size()) + 0.25)
                     : baseMaxTotalSpawns;
                  spawnController.setCurrentScaledMaxTotalSpawns(currentScaledMaxTotalSpawns);
                  if (spawnController.getSpawnedEntities().size() >= currentScaledMaxTotalSpawns) {
                     playersInRegion.clear();
                     validatedEntityList.clear();
                  } else {
                     Object2IntMap<UUID> entitiesPerPlayer = spawnController.getEntitiesPerPlayer();

                     for (int ixx = 0; ixx < validatedEntityList.size(); ixx++) {
                        NPCEntity npc = validatedEntityList.get(ixx);
                        Role role = npc.getRole();
                        if (role != null) {
                           Ref<EntityStore> lockedTargetRef = role.getMarkedEntitySupport()
                              .getMarkedEntityRef(legacySpawnBeaconComponent.getSpawnWrapper().getSpawn().getTargetSlot());
                           if (lockedTargetRef != null) {
                              UUIDComponent lockedTargetUuidComponent = commandBuffer.getComponent(lockedTargetRef, this.uuidComponentType);
                              if (lockedTargetUuidComponent != null) {
                                 entitiesPerPlayer.mergeInt(lockedTargetUuidComponent.getUuid(), 1, Integer::sum);
                              }
                           }
                        }
                     }

                     playersInRegion.sort(spawnController.getThreatComparator());
                     entitiesPerPlayer.clear();
                     this.tickController(spawnController, store);
                     playersInRegion.clear();
                     spawnController.setNextPlayerIndex(0);
                     validatedEntityList.clear();
                  }
               }
            }
         }
      }

      private static boolean isReadyToRespawn(@Nonnull LegacySpawnBeaconEntity spawnBeacon, @Nonnull WorldTimeResource worldTimeResource) {
         Instant nextSpawnAfter = spawnBeacon.getNextSpawnAfter();
         if (nextSpawnAfter == null) {
            return true;
         } else {
            Instant now = spawnBeacon.isNextSpawnAfterRealtime() ? Instant.now() : worldTimeResource.getGameTime();
            return now.isAfter(nextSpawnAfter);
         }
      }

      protected void prepareSpawnJobGeneration(@Nonnull BeaconSpawnController spawnController, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
         if (spawnController.isRoundStart()) {
            Ref<EntityStore> ownerRef = spawnController.getOwnerRef();
            LegacySpawnBeaconEntity legacySpawnBeaconComponent = componentAccessor.getComponent(ownerRef, LegacySpawnBeaconEntity.getComponentType());

            assert legacySpawnBeaconComponent != null;

            ScaledXYResponseCurve concurrentSpawnScaleCurve = legacySpawnBeaconComponent.getSpawnWrapper().getSpawn().getConcurrentSpawnsScalingCurve();
            int[] baseMaxConcurrentSpawns = spawnController.getBaseMaxConcurrentSpawns();
            List<PlayerRef> playersInRegion = spawnController.getPlayersInRegion();
            int min;
            int max;
            if (concurrentSpawnScaleCurve != null) {
               min = baseMaxConcurrentSpawns[0] + MathUtil.floor(concurrentSpawnScaleCurve.computeY(playersInRegion.size()) + 0.25);
               max = baseMaxConcurrentSpawns[1] + MathUtil.floor(concurrentSpawnScaleCurve.computeY(playersInRegion.size()) + 0.25);
            } else {
               min = baseMaxConcurrentSpawns[0];
               max = baseMaxConcurrentSpawns[1];
            }

            spawnController.setCurrentScaledMaxConcurrentSpawns(RandomExtra.randomRange(min, max));
            spawnController.setRoundStart(false);
         }

         int remainingSpawns = Math.max(0, spawnController.getCurrentScaledMaxConcurrentSpawns()) - spawnController.getSpawnsThisRound();
         spawnController.setRemainingSpawns(remainingSpawns);
         if (remainingSpawns == 0) {
            spawnController.onAllConcurrentSpawned(componentAccessor);
         }
      }

      protected void createRandomSpawnJobs(@Nonnull BeaconSpawnController spawnController, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
         while (spawnController.getActiveJobCount() < spawnController.getMaxActiveJobs()) {
            if (spawnController.createRandomSpawnJob(componentAccessor) == null) {
               spawnController.addRoundSpawn();
            }
         }
      }
   }

   public static class EntityAdded extends RefSystem<EntityStore> {
      private final ComponentType<EntityStore, SpawnBeacon> componentType;

      public EntityAdded(ComponentType<EntityStore, SpawnBeacon> componentType) {
         this.componentType = componentType;
      }

      @Override
      public Query<EntityStore> getQuery() {
         return this.componentType;
      }

      @Override
      public void onEntityAdded(
         @Nonnull Ref<EntityStore> ref, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         SpawnBeacon spawnBeaconComponent = store.getComponent(ref, this.componentType);

         assert spawnBeaconComponent != null;

         String config = spawnBeaconComponent.getSpawnConfigId();
         int index = BeaconNPCSpawn.getAssetMap().getIndex(config);
         if (index == Integer.MIN_VALUE) {
            SpawnBeaconSystems.LOGGER.at(Level.SEVERE).log("Beacon %s removed due to missing spawn beacon type: %s", ref, config);
            commandBuffer.removeEntity(ref, RemoveReason.REMOVE);
         } else {
            BeaconSpawnWrapper spawnWrapper = SpawningPlugin.get().getBeaconSpawnWrapper(index);
            spawnBeaconComponent.setSpawnWrapper(spawnWrapper);
            FloodFillPositionSelector positionSelector = new FloodFillPositionSelector(store.getExternalData().getWorld(), spawnWrapper);
            positionSelector.setCalculatePositionsAfter(
               RandomExtra.randomRange(SpawnBeaconSystems.POSITION_CALCULATION_DELAY_RANGE[0], SpawnBeaconSystems.POSITION_CALCULATION_DELAY_RANGE[1])
            );
            commandBuffer.putComponent(ref, FloodFillPositionSelector.getComponentType(), positionSelector);
            commandBuffer.ensureComponent(ref, PrefabCopyableComponent.getComponentType());
         }
      }

      @Override
      public void onEntityRemove(
         @Nonnull Ref<EntityStore> ref, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
      }
   }

   public static class LegacyEntityAdded extends RefSystem<EntityStore> {
      private final ComponentType<EntityStore, LegacySpawnBeaconEntity> componentType;

      public LegacyEntityAdded(ComponentType<EntityStore, LegacySpawnBeaconEntity> componentType) {
         this.componentType = componentType;
      }

      @Override
      public Query<EntityStore> getQuery() {
         return this.componentType;
      }

      @Override
      public void onEntityAdded(
         @Nonnull Ref<EntityStore> ref, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         LegacySpawnBeaconEntity legacySpawnBeaconComponent = store.getComponent(ref, this.componentType);

         assert legacySpawnBeaconComponent != null;

         String spawnConfigId = legacySpawnBeaconComponent.getSpawnConfigId();
         int index = BeaconNPCSpawn.getAssetMap().getIndex(spawnConfigId);
         if (index == Integer.MIN_VALUE) {
            SpawnBeaconSystems.LOGGER.at(Level.SEVERE).log("Beacon %s removed due to missing spawn beacon type: %s", ref, spawnConfigId);
            commandBuffer.removeEntity(ref, RemoveReason.REMOVE);
         } else {
            legacySpawnBeaconComponent.setSpawnWrapper(SpawningPlugin.get().getBeaconSpawnWrapper(index));
            World world = store.getExternalData().getWorld();
            BeaconSpawnController spawnController = new BeaconSpawnController(world, ref);
            legacySpawnBeaconComponent.setSpawnController(spawnController);
            BeaconSpawnWrapper spawnWrapper = legacySpawnBeaconComponent.getSpawnWrapper();
            if (spawnWrapper != null) {
               spawnController.initialise(spawnWrapper);
               FloodFillPositionSelector positionSelector = new FloodFillPositionSelector(world, spawnWrapper);
               positionSelector.setCalculatePositionsAfter(
                  RandomExtra.randomRange(SpawnBeaconSystems.POSITION_CALCULATION_DELAY_RANGE[0], SpawnBeaconSystems.POSITION_CALCULATION_DELAY_RANGE[1])
               );
               commandBuffer.putComponent(ref, FloodFillPositionSelector.getComponentType(), positionSelector);
               ScaledXYResponseCurve maxSpawnScaleCurve = spawnWrapper.getSpawn().getMaxSpawnsScalingCurve();
               int baseMaxTotalSpawns = spawnController.getBaseMaxTotalSpawns();
               int currentScaledMaxTotalSpawns = maxSpawnScaleCurve != null
                  ? baseMaxTotalSpawns + MathUtil.floor(maxSpawnScaleCurve.computeY(legacySpawnBeaconComponent.getLastPlayerCount()) + 0.25)
                  : baseMaxTotalSpawns;
               spawnController.setCurrentScaledMaxTotalSpawns(currentScaledMaxTotalSpawns);
            }

            if (reason == AddReason.LOAD) {
               InitialBeaconDelay delay = new InitialBeaconDelay();
               delay.setLoadTimeSpawnDelay(15.0);
               commandBuffer.putComponent(ref, InitialBeaconDelay.getComponentType(), delay);
            }

            commandBuffer.ensureComponent(ref, PrefabCopyableComponent.getComponentType());
         }
      }

      @Override
      public void onEntityRemove(
         @Nonnull Ref<EntityStore> ref, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
      }
   }

   public static class LoadTimeDelay extends EntityTickingSystem<EntityStore> {
      private final ComponentType<EntityStore, InitialBeaconDelay> componentType;
      private final Set<Dependency<EntityStore>> dependencies = RootDependency.lastSet();

      public LoadTimeDelay(ComponentType<EntityStore, InitialBeaconDelay> componentType) {
         this.componentType = componentType;
      }

      @Nonnull
      @Override
      public Set<Dependency<EntityStore>> getDependencies() {
         return this.dependencies;
      }

      @Override
      public Query<EntityStore> getQuery() {
         return this.componentType;
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
         InitialBeaconDelay beaconDelayComponent = archetypeChunk.getComponent(index, this.componentType);

         assert beaconDelayComponent != null;

         if (beaconDelayComponent.tickLoadTimeSpawnDelay(dt)) {
            commandBuffer.removeComponent(archetypeChunk.getReferenceTo(index), this.componentType);
         }
      }
   }

   public static class PositionSelectorUpdate extends EntityTickingSystem<EntityStore> {
      private final ComponentType<EntityStore, FloodFillPositionSelector> componentType;
      private final ComponentType<EntityStore, TransformComponent> transformComponentType;
      private final ResourceType<EntityStore, FloodFillEntryPoolProviderSimple> floodFillEntryPoolProviderSimpleResourceType;
      @Nonnull
      private final Query<EntityStore> query;
      private final Set<Dependency<EntityStore>> dependencies = Set.of(new SystemDependency<>(Order.AFTER, SpawnBeaconSystems.CheckDespawn.class));

      public PositionSelectorUpdate(
         ComponentType<EntityStore, FloodFillPositionSelector> componentType,
         ResourceType<EntityStore, FloodFillEntryPoolProviderSimple> floodFillEntryPoolProviderSimpleResourceType
      ) {
         this.componentType = componentType;
         this.transformComponentType = TransformComponent.getComponentType();
         this.floodFillEntryPoolProviderSimpleResourceType = floodFillEntryPoolProviderSimpleResourceType;
         this.query = Query.and(componentType, this.transformComponentType);
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
         FloodFillPositionSelector positionSelectorComponent = archetypeChunk.getComponent(index, this.componentType);

         assert positionSelectorComponent != null;

         if (positionSelectorComponent.shouldRebuildCache() && positionSelectorComponent.tickCalculatePositionsAfter(dt)) {
            positionSelectorComponent.init();
            TransformComponent transformComponent = archetypeChunk.getComponent(index, this.transformComponentType);

            assert transformComponent != null;

            Vector3d position = transformComponent.getPosition();
            FloodFillEntryPoolProviderSimple poolProvider = store.getResource(this.floodFillEntryPoolProviderSimpleResourceType);
            positionSelectorComponent.buildPositionCache(position, poolProvider.getPool());
         }
      }
   }

   public static class SpawnJobTick extends SpawnJobSystem<NPCBeaconSpawnJob, BeaconSpawnController> {
      private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
      private final ComponentType<EntityStore, LegacySpawnBeaconEntity> componentType;
      @Nonnull
      private final ComponentType<EntityStore, Player> playerComponentType;
      private final ComponentType<EntityStore, TransformComponent> transformComponentType;
      @Nonnull
      private final Query<EntityStore> query;
      private final Set<Dependency<EntityStore>> dependencies = Set.of(new SystemDependency<>(Order.AFTER, SpawnBeaconSystems.ControllerTick.class));

      public SpawnJobTick(
         ComponentType<EntityStore, LegacySpawnBeaconEntity> componentType, ComponentType<EntityStore, InitialBeaconDelay> initialBeaconDelayComponentType
      ) {
         this.componentType = componentType;
         this.playerComponentType = Player.getComponentType();
         this.transformComponentType = TransformComponent.getComponentType();
         this.query = Query.and(componentType, Query.not(initialBeaconDelayComponentType));
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
         LegacySpawnBeaconEntity legacySpawnBeaconComponent = archetypeChunk.getComponent(index, this.componentType);

         assert legacySpawnBeaconComponent != null;

         this.tickSpawnJobs(legacySpawnBeaconComponent.getSpawnController(), store, commandBuffer);
      }

      protected void onStartRun(NPCBeaconSpawnJob spawnJob) {
      }

      protected void onEndProbing(
         @Nonnull BeaconSpawnController spawnController,
         @Nonnull NPCBeaconSpawnJob spawnJob,
         SpawnJobSystem.Result result,
         @Nonnull ComponentAccessor<EntityStore> componentAccessor
      ) {
         Ref<EntityStore> ownerRef = spawnController.getOwnerRef();
         if (ownerRef.isValid()) {
            LegacySpawnBeaconEntity legacySpawnBeaconEntity = componentAccessor.getComponent(ownerRef, LegacySpawnBeaconEntity.getComponentType());

            assert legacySpawnBeaconEntity != null;

            if (result == SpawnJobSystem.Result.FAILED && legacySpawnBeaconEntity.getSpawnAttempts() > 5) {
               LegacySpawnBeaconEntity.prepareNextSpawnTimer(ownerRef, componentAccessor);
               spawnJob.setBudgetUsed(spawnJob.getColumnBudget());
            } else if (result == SpawnJobSystem.Result.PERMANENT_FAILURE) {
               legacySpawnBeaconEntity.remove();
            } else if (result != SpawnJobSystem.Result.SUCCESS) {
               legacySpawnBeaconEntity.notifyFailedSpawn();
            }
         }
      }

      protected boolean pickSpawnPosition(
         @Nonnull BeaconSpawnController spawnController, @Nonnull NPCBeaconSpawnJob spawnJob, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         Ref<EntityStore> playerReference = spawnJob.getPlayer();
         if (playerReference != null && playerReference.isValid()) {
            TransformComponent playerTransformComponent = commandBuffer.getComponent(playerReference, this.transformComponentType);

            assert playerTransformComponent != null;

            Vector3d playerPosition = playerTransformComponent.getPosition();
            Ref<EntityStore> ownerRef = spawnController.getOwnerRef();
            LegacySpawnBeaconEntity legacySpawnBeaconComponent = commandBuffer.getComponent(ownerRef, LegacySpawnBeaconEntity.getComponentType());

            assert legacySpawnBeaconComponent != null;

            return legacySpawnBeaconComponent.prepareSpawnContext(
               playerPosition, spawnJob.getSpawnsThisRound(), spawnJob.getRoleIndex(), spawnJob.getSpawningContext(), commandBuffer
            );
         } else {
            return false;
         }
      }

      @Nonnull
      protected SpawnJobSystem.Result trySpawn(
         @Nonnull BeaconSpawnController spawnController, @Nonnull NPCBeaconSpawnJob spawnJob, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         return this.spawn(spawnJob.getSpawningContext().world, spawnController, spawnJob, commandBuffer);
      }

      @Nonnull
      protected SpawnJobSystem.Result spawn(
         World world, @Nonnull BeaconSpawnController spawnController, @Nonnull NPCBeaconSpawnJob spawnJob, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         SpawningContext spawningContext = spawnJob.getSpawningContext();
         Vector3d position = spawningContext.newPosition();
         Vector3f rotation = spawningContext.newRotation();
         int roleIndex = spawnJob.getRoleIndex();
         commandBuffer.run(
            _store -> {
               try {
                  Pair<Ref<EntityStore>, NPCEntity> npcPair = NPCPlugin.get()
                     .spawnEntity(
                        _store,
                        roleIndex,
                        position,
                        rotation,
                        spawningContext.getModel(),
                        (npc, ref, store) -> postSpawn(npc, ref, roleIndex, spawnController.isDebugSpawnFrozen(), store)
                     );
                  Ref<EntityStore> npcRef = (Ref<EntityStore>)npcPair.first();
                  FlockPlugin.trySpawnFlock(
                     npcRef,
                     (NPCEntity)npcPair.second(),
                     roleIndex,
                     position,
                     rotation,
                     spawnJob.getFlockSize(),
                     spawnJob.getFlockAsset(),
                     null,
                     (npc, ref, store) -> postSpawn(npc, ref, roleIndex, spawnController.isDebugSpawnFrozen(), store),
                     _store
                  );
                  this.onSpawn(npcRef, spawnController, spawnJob, _store);
                  this.endProbing(spawnController, spawnJob, SpawnJobSystem.Result.SUCCESS, _store);
               } catch (RuntimeException var10) {
                  LOGGER.at(Level.WARNING)
                     .log("Spawn job %s: Failed to create %s: %s", spawnJob.getJobId(), NPCPlugin.get().getName(roleIndex), var10.getMessage());
                  this.endProbing(spawnController, spawnJob, SpawnJobSystem.Result.FAILED, _store);
               }

               spawnController.addIdleJob(spawnJob);
            }
         );
         return SpawnJobSystem.Result.PENDING_SPAWN;
      }

      private void onSpawn(
         @Nonnull Ref<EntityStore> npcReference,
         @Nonnull BeaconSpawnController spawnController,
         @Nonnull NPCBeaconSpawnJob spawnJob,
         @Nonnull Store<EntityStore> store
      ) {
         HytaleLogger.Api context = LOGGER.at(Level.FINE);
         if (context.isEnabled()) {
            TransformComponent transformComponent = store.getComponent(npcReference, this.transformComponentType);

            assert transformComponent != null;

            Vector3d pos = transformComponent.getPosition();
            context.log("Spawn job %s: Created %s at position %s", spawnJob.getJobId(), NPCPlugin.get().getName(spawnJob.getRoleIndex()), pos);
         }

         Ref<EntityStore> playerRef = spawnJob.getPlayer();

         assert playerRef != null;

         Player playerComponent = store.getComponent(spawnJob.getPlayer(), this.playerComponentType);

         assert playerComponent != null;

         Ref<EntityStore> ownerRef = spawnController.getOwnerRef();
         LegacySpawnBeaconEntity legacySpawnBeaconComponent = store.getComponent(ownerRef, LegacySpawnBeaconEntity.getComponentType());

         assert legacySpawnBeaconComponent != null;

         legacySpawnBeaconComponent.notifySpawn(playerComponent, npcReference, store);
      }

      private static void postSpawn(
         @Nonnull NPCEntity entity, @Nonnull Ref<EntityStore> ref, int roleIndex, boolean spawnFrozen, @Nonnull Store<EntityStore> store
      ) {
         entity.setSpawnRoleIndex(roleIndex);
         if (spawnFrozen) {
            store.ensureComponent(ref, Frozen.getComponentType());
         }
      }
   }
}
