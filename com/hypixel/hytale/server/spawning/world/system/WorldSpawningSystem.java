package com.hypixel.hytale.server.spawning.world.system;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.tick.TickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.random.RandomExtra;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.server.core.asset.type.environment.config.Environment;
import com.hypixel.hytale.server.core.asset.type.gameplay.GameplayConfig;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.flock.config.FlockAsset;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.spawning.SpawningPlugin;
import com.hypixel.hytale.server.spawning.world.ChunkEnvironmentSpawnData;
import com.hypixel.hytale.server.spawning.world.WorldEnvironmentSpawnData;
import com.hypixel.hytale.server.spawning.world.WorldNPCSpawnStat;
import com.hypixel.hytale.server.spawning.world.component.ChunkSpawnData;
import com.hypixel.hytale.server.spawning.world.component.ChunkSpawnedNPCData;
import com.hypixel.hytale.server.spawning.world.component.SpawnJobData;
import com.hypixel.hytale.server.spawning.world.component.WorldSpawnData;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WorldSpawningSystem extends TickingSystem<ChunkStore> {
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   private static final long SPAWN_COOLDOWN_NANOS = Duration.ofSeconds(1L).toNanos();
   private final ResourceType<EntityStore, WorldSpawnData> worldSpawnDataResourceType;
   private final ComponentType<ChunkStore, ChunkSpawnData> chunkSpawnDataComponentType;
   private final ComponentType<ChunkStore, ChunkSpawnedNPCData> chunkSpawnedNPCDataComponentType;
   private final ComponentType<ChunkStore, SpawnJobData> spawnJobDataComponentType;
   private final ComponentType<ChunkStore, WorldChunk> worldChunkComponentType;

   public WorldSpawningSystem(
      @Nonnull ResourceType<EntityStore, WorldSpawnData> worldSpawnDataResourceType,
      @Nonnull ComponentType<ChunkStore, ChunkSpawnData> chunkSpawnDataComponentType,
      @Nonnull ComponentType<ChunkStore, ChunkSpawnedNPCData> chunkSpawnedNPCDataComponentType,
      @Nonnull ComponentType<ChunkStore, SpawnJobData> spawnJobDataComponentType
   ) {
      this.worldSpawnDataResourceType = worldSpawnDataResourceType;
      this.chunkSpawnDataComponentType = chunkSpawnDataComponentType;
      this.chunkSpawnedNPCDataComponentType = chunkSpawnedNPCDataComponentType;
      this.spawnJobDataComponentType = spawnJobDataComponentType;
      this.worldChunkComponentType = WorldChunk.getComponentType();
   }

   @Override
   public void tick(float dt, int systemIndex, @Nonnull Store<ChunkStore> store) {
      World world = store.getExternalData().getWorld();
      if (world.getWorldConfig().isSpawningNPC() && world.getPlayerCount() != 0) {
         GameplayConfig gameplayConfig = world.getGameplayConfig();
         Store<EntityStore> entityStore = world.getEntityStore().getStore();
         WorldSpawnData worldSpawnDataResource = entityStore.getResource(this.worldSpawnDataResourceType);
         if (!worldSpawnDataResource.isUnspawnable()
            && world.getChunkStore().getStore().getEntityCount() != 0
            && (gameplayConfig.getMaxEnvironmentalNPCSpawns() <= 0 || worldSpawnDataResource.getActualNPCs() < gameplayConfig.getMaxEnvironmentalNPCSpawns())
            && !(worldSpawnDataResource.getActualNPCs() > worldSpawnDataResource.getExpectedNPCs())) {
            WorldTimeResource worldTimeResource = entityStore.getResource(WorldTimeResource.getResourceType());
            if (worldSpawnDataResource.hasUnprocessedUnspawnableChunks()) {
               while (worldSpawnDataResource.hasUnprocessedUnspawnableChunks()) {
                  WorldSpawnData.UnspawnableEntry entry = worldSpawnDataResource.nextUnspawnableChunk();
                  Ref<ChunkStore> chunkRef = world.getChunkStore().getChunkReference(entry.getChunkIndex());
                  if (chunkRef != null) {
                     int environmentIndex = entry.getEnvironmentIndex();
                     ChunkSpawnData chunkSpawnDataComponent = store.getComponent(chunkRef, this.chunkSpawnDataComponentType);
                     if (chunkSpawnDataComponent != null) {
                        ChunkEnvironmentSpawnData environmentSpawnData = chunkSpawnDataComponent.getEnvironmentSpawnData(environmentIndex);
                        int segmentCount = -environmentSpawnData.getSegmentCount();
                        worldSpawnDataResource.adjustSegmentCount(segmentCount);
                        WorldEnvironmentSpawnData worldEnvironmentSpawnData = worldSpawnDataResource.getWorldEnvironmentSpawnData(environmentIndex);
                        double expectedNPCs = worldEnvironmentSpawnData.getExpectedNPCs();
                        worldEnvironmentSpawnData.adjustSegmentCount(segmentCount);
                        worldEnvironmentSpawnData.updateExpectedNPCs(worldTimeResource.getMoonPhase());
                        environmentSpawnData.markProcessedAsUnspawnable();
                        HytaleLogger.Api context = LOGGER.at(Level.FINEST);
                        if (context.isEnabled()) {
                           Environment environmentAsset = Environment.getAssetMap().getAsset(environmentIndex);
                           if (environmentAsset != null) {
                              String environment = environmentAsset.getId();
                              context.log(
                                 "Reducing expected NPC count for %s due to un-spawnable chunk. Was %s, now %s",
                                 environment,
                                 expectedNPCs,
                                 worldEnvironmentSpawnData.getExpectedNPCs()
                              );
                           }
                        }
                     }
                  }
               }

               worldSpawnDataResource.recalculateWorldCount();
            }

            int activeJobs = worldSpawnDataResource.getActiveSpawnJobs();
            int maxActiveJobs = SpawningPlugin.get().getMaxActiveJobs();

            while (
               activeJobs < maxActiveJobs
                  && worldSpawnDataResource.getActualNPCs() < MathUtil.floor(worldSpawnDataResource.getExpectedNPCs())
                  && this.createRandomSpawnJob(worldSpawnDataResource, store, entityStore)
            ) {
               activeJobs = worldSpawnDataResource.getActiveSpawnJobs();
            }
         }
      }
   }

   private boolean createRandomSpawnJob(
      @Nonnull WorldSpawnData worldData, @Nonnull Store<ChunkStore> chunkStore, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      int[] environmentDataKeySet = worldData.getWorldEnvironmentSpawnDataIndexes();

      WorldNPCSpawnStat npcSpawnStat;
      WorldEnvironmentSpawnData worldEnvironmentSpawnData;
      int environmentIndex;
      do {
         environmentIndex = getAndConsumeNextEnvironmentIndex(worldData, environmentDataKeySet);
         if (environmentIndex == Integer.MIN_VALUE) {
            return false;
         }

         worldEnvironmentSpawnData = worldData.getWorldEnvironmentSpawnData(environmentIndex);
         npcSpawnStat = worldEnvironmentSpawnData.pickRandomSpawnNPCStat(componentAccessor);
      } while (npcSpawnStat == null);

      int availableSlots = npcSpawnStat.getAvailableSlots();
      if (availableSlots == 0) {
         return false;
      } else {
         Ref<ChunkStore> chunkRef = this.pickRandomChunk(worldEnvironmentSpawnData, npcSpawnStat, worldData, chunkStore);
         if (chunkRef == null) {
            return false;
         } else {
            Environment environment = Environment.getAssetMap().getAsset(environmentIndex);
            HytaleLogger.Api context = LOGGER.at(Level.FINER);
            if (context.isEnabled()) {
               WorldChunk worldChunkComponent = chunkStore.getComponent(chunkRef, this.worldChunkComponentType);

               assert worldChunkComponent != null;

               String roleName = NPCPlugin.get().getName(npcSpawnStat.getRoleIndex());
               context.log(
                  "Trying SpawnJob env=%s role=%s chunk=[%s/%s] env(exp/act)=%s/%s npc(exp/act)=%s/%s",
                  environment.getId(),
                  roleName,
                  worldChunkComponent.getX(),
                  worldChunkComponent.getZ(),
                  (int)worldEnvironmentSpawnData.getExpectedNPCs(),
                  worldEnvironmentSpawnData.getActualNPCs(),
                  (int)npcSpawnStat.getExpected(),
                  npcSpawnStat.getActual()
               );
            }

            SpawnJobData spawnJobDataComponent = chunkStore.addComponent(chunkRef, this.spawnJobDataComponentType);
            FlockAsset flockDefinition = npcSpawnStat.getSpawnParams().getFlockDefinition();
            int flockSize = flockDefinition != null ? flockDefinition.pickFlockSize() : 1;
            int roleIndex = npcSpawnStat.getRoleIndex();
            if (flockSize > availableSlots) {
               flockSize = availableSlots;
            }

            spawnJobDataComponent.init(roleIndex, environment, environmentIndex, npcSpawnStat.getSpawnWrapper(), flockDefinition, flockSize);
            if (worldEnvironmentSpawnData.isFullyPopulated()) {
               spawnJobDataComponent.setIgnoreFullyPopulated(true);
            }

            ChunkSpawnData chunkSpawnDataComponent = chunkStore.getComponent(chunkRef, this.chunkSpawnDataComponentType);

            assert chunkSpawnDataComponent != null;

            chunkSpawnDataComponent.getEnvironmentSpawnData(environmentIndex).getRandomChunkColumnIterator().saveIteratorPosition();
            World world = chunkStore.getExternalData().getWorld();
            WorldSpawnData worldSpawnData = world.getEntityStore().getStore().getResource(WorldSpawnData.getResourceType());
            worldSpawnData.trackNPC(environmentIndex, roleIndex, flockSize, world, componentAccessor);
            HytaleLogger.Api finestContext = LOGGER.at(Level.FINEST);
            if (finestContext.isEnabled()) {
               WorldChunk worldChunkComponent = chunkStore.getComponent(chunkRef, this.worldChunkComponentType);

               assert worldChunkComponent != null;

               finestContext.log(
                  "Start Spawnjob id=%s env=%s role=%s chunk=[%s/%s]",
                  spawnJobDataComponent.getJobId(),
                  environment.getId(),
                  NPCPlugin.get().getName(roleIndex),
                  worldChunkComponent.getX(),
                  worldChunkComponent.getZ()
               );
            }

            worldData.adjustActiveSpawnJobs(1, flockSize);
            return true;
         }
      }
   }

   private static int getAndConsumeNextEnvironmentIndex(@Nonnull WorldSpawnData worldSpawnData, @Nonnull int[] environmentKeySet) {
      double weightSum = 0.0;

      for (int keyReference : environmentKeySet) {
         if (keyReference != Integer.MIN_VALUE) {
            weightSum += worldSpawnData.getWorldEnvironmentSpawnData(keyReference).spawnWeight();
         }
      }

      if (weightSum == 0.0) {
         return Integer.MIN_VALUE;
      } else {
         weightSum *= ThreadLocalRandom.current().nextDouble();

         for (int i = 0; i < environmentKeySet.length; i++) {
            int keyReferencex = environmentKeySet[i];
            if (keyReferencex != Integer.MIN_VALUE) {
               weightSum -= worldSpawnData.getWorldEnvironmentSpawnData(keyReferencex).spawnWeight();
               if (weightSum <= 0.0) {
                  environmentKeySet[i] = Integer.MIN_VALUE;
                  return keyReferencex;
               }
            }
         }

         return Integer.MIN_VALUE;
      }
   }

   @Nullable
   private Ref<ChunkStore> pickRandomChunk(
      @Nonnull WorldEnvironmentSpawnData spawnData, @Nonnull WorldNPCSpawnStat stat, @Nonnull WorldSpawnData worldSpawnData, @Nonnull Store<ChunkStore> store
   ) {
      int roleIndex = stat.getRoleIndex();
      boolean wasFullyPopulated = spawnData.isFullyPopulated();
      Set<Ref<ChunkStore>> chunkRefSet = spawnData.getChunkRefSet();
      int environmentIndex = spawnData.getEnvironmentIndex();
      double weight = 0.0;
      boolean spawnable = false;
      boolean fullyPopulated = true;
      if (wasFullyPopulated) {
         for (Ref<ChunkStore> chunkRef : chunkRefSet) {
            ChunkSpawnData chunkSpawnDataComponent = store.getComponent(chunkRef, this.chunkSpawnDataComponentType);

            assert chunkSpawnDataComponent != null;

            ChunkSpawnedNPCData chunkSpawnedNPCDataComponent = store.getComponent(chunkRef, this.chunkSpawnedNPCDataComponentType);

            assert chunkSpawnedNPCDataComponent != null;

            ChunkEnvironmentSpawnData chunkEnvironmentSpawnData = chunkSpawnDataComponent.getEnvironmentSpawnData(environmentIndex);
            fullyPopulated = fullyPopulated
               && chunkEnvironmentSpawnData.isFullyPopulated(chunkSpawnedNPCDataComponent.getEnvironmentSpawnCount(environmentIndex));
            if (chunkEnvironmentSpawnData.isRoleSpawnable(roleIndex)) {
               spawnable = true;
               weight += store.getComponent(chunkRef, this.spawnJobDataComponentType) == null && !getAndUpdateSpawnCooldown(chunkSpawnDataComponent)
                  ? 1.0
                  : 0.0;
            }
         }
      } else {
         for (Ref<ChunkStore> chunkRef : chunkRefSet) {
            ChunkSpawnData chunkSpawnDataComponentx = store.getComponent(chunkRef, this.chunkSpawnDataComponentType);

            assert chunkSpawnDataComponentx != null;

            ChunkSpawnedNPCData chunkSpawnedNPCDataComponentx = store.getComponent(chunkRef, this.chunkSpawnedNPCDataComponentType);

            assert chunkSpawnedNPCDataComponentx != null;

            ChunkEnvironmentSpawnData chunkEnvironmentSpawnData = chunkSpawnDataComponentx.getEnvironmentSpawnData(environmentIndex);
            double spawnCount = chunkSpawnedNPCDataComponentx.getEnvironmentSpawnCount(environmentIndex);
            fullyPopulated = fullyPopulated && chunkEnvironmentSpawnData.isFullyPopulated(spawnCount);
            if (chunkEnvironmentSpawnData.isRoleSpawnable(roleIndex)) {
               spawnable = true;
               weight += store.getComponent(chunkRef, this.spawnJobDataComponentType) == null && !getAndUpdateSpawnCooldown(chunkSpawnDataComponentx)
                  ? chunkEnvironmentSpawnData.getWeight(spawnCount)
                  : 0.0;
            }
         }
      }

      spawnData.setFullyPopulated(fullyPopulated);
      if (spawnable) {
         return RandomExtra.randomWeightedElement(
            chunkRefSet,
            (chunkRefx, index) -> {
               ChunkSpawnData chunkSpawnDataComponent = store.getComponent(chunkRefx, this.chunkSpawnDataComponentType);

               assert chunkSpawnDataComponent != null;

               ChunkEnvironmentSpawnData chunkEnvironmentSpawnDatax = chunkSpawnDataComponent.getEnvironmentSpawnData(environmentIndex);
               return chunkEnvironmentSpawnDatax.isRoleSpawnable(index);
            },
            wasFullyPopulated
               ? (chunkRefx, index) -> {
                  ChunkSpawnData spawnChunkDataComponent = store.getComponent(chunkRefx, this.chunkSpawnDataComponentType);

                  assert spawnChunkDataComponent != null;

                  return store.getComponent(chunkRefx, this.spawnJobDataComponentType) == null && !spawnChunkDataComponent.isOnSpawnCooldown() ? 1.0 : 0.0;
               }
               : (chunkRefx, index) -> {
                  ChunkSpawnData chunkSpawnDataComponent = store.getComponent(chunkRefx, this.chunkSpawnDataComponentType);

                  assert chunkSpawnDataComponent != null;

                  ChunkSpawnedNPCData chunkSpawnedNPCDataComponentx = store.getComponent(chunkRefx, this.chunkSpawnedNPCDataComponentType);

                  assert chunkSpawnedNPCDataComponentx != null;

                  ChunkEnvironmentSpawnData chunkEnvironmentSpawnDatax = chunkSpawnDataComponent.getEnvironmentSpawnData(environmentIndex);
                  return store.getComponent(chunkRefx, this.spawnJobDataComponentType) == null && !chunkSpawnDataComponent.isOnSpawnCooldown()
                     ? chunkEnvironmentSpawnDatax.getWeight(chunkSpawnedNPCDataComponentx.getEnvironmentSpawnCount(environmentIndex))
                     : 0.0;
               },
            weight,
            roleIndex
         );
      } else {
         stat.setUnspawnable(true);
         boolean unspawnable = true;
         ObjectIterator var23 = spawnData.getNpcStatMap().values().iterator();

         while (var23.hasNext()) {
            WorldNPCSpawnStat npcStat = (WorldNPCSpawnStat)var23.next();
            if (!npcStat.isUnspawnable()) {
               unspawnable = false;
               break;
            }
         }

         spawnData.setUnspawnable(unspawnable);
         worldSpawnData.updateSpawnability();
         return null;
      }
   }

   private static boolean getAndUpdateSpawnCooldown(@Nonnull ChunkSpawnData chunkSpawnData) {
      boolean onCooldown = chunkSpawnData.isOnSpawnCooldown();
      if (onCooldown && System.nanoTime() - chunkSpawnData.getLastSpawn() > SPAWN_COOLDOWN_NANOS) {
         chunkSpawnData.setLastSpawn(0L);
         onCooldown = false;
      }

      return onCooldown;
   }
}
