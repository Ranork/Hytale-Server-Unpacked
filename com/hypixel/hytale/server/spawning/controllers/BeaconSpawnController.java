package com.hypixel.hytale.server.spawning.controllers;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.spawning.assets.spawns.config.BeaconNPCSpawn;
import com.hypixel.hytale.server.spawning.assets.spawns.config.RoleSpawnParameters;
import com.hypixel.hytale.server.spawning.beacons.LegacySpawnBeaconEntity;
import com.hypixel.hytale.server.spawning.jobs.NPCBeaconSpawnJob;
import com.hypixel.hytale.server.spawning.wrappers.BeaconSpawnWrapper;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BeaconSpawnController extends SpawnController<NPCBeaconSpawnJob> {
   @Nonnull
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   public static final int MAX_ATTEMPTS_PER_TICK = 5;
   public static final double ROUNDING_BREAK_POINT = 0.25;
   @Nonnull
   private final Ref<EntityStore> ownerRef;
   private final List<Ref<EntityStore>> spawnedEntities = new ObjectArrayList();
   private final List<PlayerRef> playersInRegion = new ObjectArrayList();
   private int nextPlayerIndex = 0;
   private final Object2IntMap<UUID> entitiesPerPlayer = new Object2IntOpenHashMap();
   private final Object2DoubleMap<Ref<EntityStore>> entityTimeoutCounter = new Object2DoubleOpenHashMap();
   private final IntSet unspawnableRoles = new IntOpenHashSet();
   private final Comparator<PlayerRef> threatComparator = Comparator.comparingInt(playerRef -> this.entitiesPerPlayer.getOrDefault(playerRef.getUuid(), 0));
   private int baseMaxTotalSpawns;
   private int currentScaledMaxTotalSpawns;
   private int[] baseMaxConcurrentSpawns;
   private int currentScaledMaxConcurrentSpawns;
   private int spawnsThisRound;
   private int remainingSpawns;
   private boolean roundStart = true;
   private double beaconRadiusSquared;
   private double spawnRadiusSquared;
   private double despawnNPCAfterTimeout;
   private Duration despawnBeaconAfterTimeout;
   private boolean despawnNPCsIfIdle;

   public BeaconSpawnController(@Nonnull World world, @Nonnull Ref<EntityStore> ownerRef) {
      super(world);
      this.ownerRef = ownerRef;
   }

   @Override
   public int getMaxActiveJobs() {
      return Math.min(this.remainingSpawns, this.baseMaxActiveJobs);
   }

   @Nullable
   public NPCBeaconSpawnJob createRandomSpawnJob(@Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      LegacySpawnBeaconEntity legacySpawnBeaconComponent = componentAccessor.getComponent(this.ownerRef, LegacySpawnBeaconEntity.getComponentType());
      if (legacySpawnBeaconComponent == null) {
         return null;
      } else {
         BeaconSpawnWrapper wrapper = legacySpawnBeaconComponent.getSpawnWrapper();
         RoleSpawnParameters spawn = wrapper.pickRole(ThreadLocalRandom.current());
         if (spawn == null) {
            return null;
         } else {
            String spawnId = spawn.getId();
            int roleIndex = NPCPlugin.get().getIndex(spawnId);
            if (roleIndex >= 0 && !this.unspawnableRoles.contains(roleIndex)) {
               NPCBeaconSpawnJob job = null;
               int predictedTotal = this.spawnedEntities.size() + this.activeJobs.size();
               if (this.activeJobs.size() < this.getMaxActiveJobs()
                  && this.nextPlayerIndex < this.playersInRegion.size()
                  && predictedTotal < this.currentScaledMaxTotalSpawns) {
                  job = this.idleJobs.isEmpty() ? new NPCBeaconSpawnJob() : this.idleJobs.pop();
                  job.beginProbing(
                     this.playersInRegion.get(this.nextPlayerIndex++), this.currentScaledMaxConcurrentSpawns, roleIndex, spawn.getFlockDefinition()
                  );
                  this.activeJobs.add(job);
                  if (this.nextPlayerIndex >= this.playersInRegion.size()) {
                     this.nextPlayerIndex = 0;
                  }
               }

               return job;
            } else {
               return null;
            }
         }
      }
   }

   public void initialise(@Nonnull BeaconSpawnWrapper spawnWrapper) {
      BeaconNPCSpawn spawn = spawnWrapper.getSpawn();
      this.baseMaxTotalSpawns = spawn.getMaxSpawnedNpcs();
      this.baseMaxConcurrentSpawns = spawn.getConcurrentSpawnsRange();
      double beaconRadius = spawn.getBeaconRadius();
      this.beaconRadiusSquared = beaconRadius * beaconRadius;
      double spawnRadius = spawn.getSpawnRadius();
      this.spawnRadiusSquared = spawnRadius * spawnRadius;
      this.despawnNPCAfterTimeout = spawn.getNpcIdleDespawnTimeSeconds();
      this.despawnBeaconAfterTimeout = spawn.getBeaconVacantDespawnTime();
      this.despawnNPCsIfIdle = spawn.getNpcSpawnState() != null;
   }

   public int getSpawnsThisRound() {
      return this.spawnsThisRound;
   }

   public void setRemainingSpawns(int remainingSpawns) {
      this.remainingSpawns = remainingSpawns;
   }

   public void addRoundSpawn() {
      this.spawnsThisRound++;
      this.remainingSpawns--;
   }

   public boolean isRoundStart() {
      return this.roundStart;
   }

   public void setRoundStart(boolean roundStart) {
      this.roundStart = roundStart;
   }

   @Nonnull
   public Ref<EntityStore> getOwnerRef() {
      return this.ownerRef;
   }

   public int[] getBaseMaxConcurrentSpawns() {
      return this.baseMaxConcurrentSpawns;
   }

   public List<PlayerRef> getPlayersInRegion() {
      return this.playersInRegion;
   }

   public int getCurrentScaledMaxConcurrentSpawns() {
      return this.currentScaledMaxConcurrentSpawns;
   }

   public void setCurrentScaledMaxConcurrentSpawns(int currentScaledMaxConcurrentSpawns) {
      this.currentScaledMaxConcurrentSpawns = currentScaledMaxConcurrentSpawns;
   }

   public Duration getDespawnBeaconAfterTimeout() {
      return this.despawnBeaconAfterTimeout;
   }

   public double getSpawnRadiusSquared() {
      return this.spawnRadiusSquared;
   }

   public double getBeaconRadiusSquared() {
      return this.beaconRadiusSquared;
   }

   public int getBaseMaxTotalSpawns() {
      return this.baseMaxTotalSpawns;
   }

   public void setCurrentScaledMaxTotalSpawns(int currentScaledMaxTotalSpawns) {
      this.currentScaledMaxTotalSpawns = currentScaledMaxTotalSpawns;
   }

   public List<Ref<EntityStore>> getSpawnedEntities() {
      return this.spawnedEntities;
   }

   public void setNextPlayerIndex(int nextPlayerIndex) {
      this.nextPlayerIndex = nextPlayerIndex;
   }

   public Object2DoubleMap<Ref<EntityStore>> getEntityTimeoutCounter() {
      return this.entityTimeoutCounter;
   }

   public Object2IntMap<UUID> getEntitiesPerPlayer() {
      return this.entitiesPerPlayer;
   }

   public boolean isDespawnNPCsIfIdle() {
      return this.despawnNPCsIfIdle;
   }

   public double getDespawnNPCAfterTimeout() {
      return this.despawnNPCAfterTimeout;
   }

   public Comparator<PlayerRef> getThreatComparator() {
      return this.threatComparator;
   }

   public void notifySpawnedEntityExists(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      this.spawnedEntities.add(ref);
      HytaleLogger.Api context = LOGGER.at(Level.FINE);
      if (context.isEnabled()) {
         UUIDComponent ownerUuidComponent = componentAccessor.getComponent(this.ownerRef, UUIDComponent.getComponentType());

         assert ownerUuidComponent != null;

         context.log("Registering NPC with reference %s with Spawn Beacon %s", ref, ownerUuidComponent.getUuid());
      }
   }

   public void onJobFinished(@Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      if (++this.spawnsThisRound >= this.currentScaledMaxConcurrentSpawns) {
         this.onAllConcurrentSpawned(componentAccessor);
      }
   }

   public void notifyNPCRemoval(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      this.spawnedEntities.remove(ref);
      this.entityTimeoutCounter.removeDouble(ref);
      if (this.spawnedEntities.size() == this.currentScaledMaxTotalSpawns - 1) {
         LegacySpawnBeaconEntity.prepareNextSpawnTimer(this.ownerRef, componentAccessor);
      }

      HytaleLogger.Api context = LOGGER.at(Level.FINE);
      if (context.isEnabled()) {
         UUIDComponent ownerUuidComponent = componentAccessor.getComponent(this.ownerRef, UUIDComponent.getComponentType());

         assert ownerUuidComponent != null;

         context.log("Removing NPC with reference %s from Spawn Beacon %s", ref, ownerUuidComponent.getUuid());
      }
   }

   public boolean hasSlots() {
      return this.spawnedEntities.size() < this.currentScaledMaxTotalSpawns;
   }

   public void markNPCUnspawnable(int roleIndex) {
      this.unspawnableRoles.add(roleIndex);
   }

   public void clearUnspawnableNPCs() {
      this.unspawnableRoles.clear();
   }

   public void onAllConcurrentSpawned(@Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      this.spawnsThisRound = 0;
      this.remainingSpawns = 0;
      LegacySpawnBeaconEntity.prepareNextSpawnTimer(this.ownerRef, componentAccessor);
      this.roundStart = true;
   }
}
