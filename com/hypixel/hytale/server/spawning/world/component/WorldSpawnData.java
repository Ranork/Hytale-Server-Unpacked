package com.hypixel.hytale.server.spawning.world.component;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Resource;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.iterator.SpiralIterator;
import com.hypixel.hytale.server.core.asset.type.environment.config.Environment;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.spawning.SpawningPlugin;
import com.hypixel.hytale.server.spawning.world.WorldEnvironmentSpawnData;
import com.hypixel.hytale.server.spawning.world.manager.EnvironmentSpawnParameters;
import com.hypixel.hytale.server.spawning.world.manager.WorldSpawnWrapper;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.ArrayDeque;
import java.util.function.Consumer;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WorldSpawnData implements Resource<EntityStore> {
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   private final Int2ObjectMap<WorldEnvironmentSpawnData> worldEnvironmentSpawnData = new Int2ObjectOpenHashMap();
   private final ArrayDeque<WorldSpawnData.UnspawnableEntry> unspawnableProcessingQueue = new ArrayDeque<>();
   private int actualNPCs;
   private double expectedNPCs;
   private double expectedNPCsInEmptyEnvironments;
   private boolean unspawnable;
   private int chunkCount;
   private int segmentCount;
   private int activeSpawnJobs;
   private int trackedCountFromJobs;
   private int totalSpawnJobBudgetUsed;
   private int totalSpawnJobsCompleted;
   private final SpiralIterator spiralIterator = new SpiralIterator();

   public static ResourceType<EntityStore, WorldSpawnData> getResourceType() {
      return SpawningPlugin.get().getWorldSpawnDataResourceType();
   }

   public int getActualNPCs() {
      return this.actualNPCs;
   }

   public double getExpectedNPCs() {
      return this.expectedNPCs;
   }

   public double getExpectedNPCsInEmptyEnvironments() {
      return this.expectedNPCsInEmptyEnvironments;
   }

   public boolean isUnspawnable() {
      return this.unspawnable;
   }

   public void setUnspawnable(boolean unspawnable) {
      this.unspawnable = unspawnable;
   }

   public int getChunkCount() {
      return this.chunkCount;
   }

   public void adjustChunkCount(int amount) {
      this.chunkCount += amount;
   }

   public void adjustSegmentCount(int amount) {
      this.segmentCount += amount;
   }

   @Nonnull
   public SpiralIterator getSpiralIterator() {
      return this.spiralIterator;
   }

   public double averageSegmentCount() {
      return this.chunkCount == 0 ? 0.0 : (double)this.segmentCount / this.chunkCount;
   }

   public int getActiveSpawnJobs() {
      return this.activeSpawnJobs;
   }

   public void adjustActiveSpawnJobs(int amount, int trackedCount) {
      this.activeSpawnJobs += amount;
      this.trackedCountFromJobs += trackedCount;
   }

   public int getTrackedCountFromJobs() {
      return this.trackedCountFromJobs;
   }

   public int getTotalSpawnJobBudgetUsed() {
      return this.totalSpawnJobBudgetUsed;
   }

   public int getTotalSpawnJobsCompleted() {
      return this.totalSpawnJobsCompleted;
   }

   public void addCompletedSpawnJob(int budgetUsed) {
      this.totalSpawnJobBudgetUsed += budgetUsed;
      this.totalSpawnJobsCompleted++;
   }

   public WorldEnvironmentSpawnData getWorldEnvironmentSpawnData(int environmentIndex) {
      return (WorldEnvironmentSpawnData)this.worldEnvironmentSpawnData.get(environmentIndex);
   }

   @Nonnull
   public WorldEnvironmentSpawnData getOrCreateWorldEnvironmentSpawnData(
      int environmentIndex, @Nonnull World world, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      WorldTimeResource worldTimeResource = componentAccessor.getResource(WorldTimeResource.getResourceType());
      return (WorldEnvironmentSpawnData)this.worldEnvironmentSpawnData.computeIfAbsent(environmentIndex, envIndex -> {
         WorldEnvironmentSpawnData newWorldEnvironmentSpawnData = new WorldEnvironmentSpawnData(envIndex);
         EnvironmentSpawnParameters envSpawnParameters = SpawningPlugin.get().getWorldEnvironmentSpawnParameters(envIndex);
         if (envSpawnParameters == null) {
            Environment env = Environment.getAssetMap().getAsset(envIndex);
            LOGGER.at(Level.WARNING).log("No environment data found for '%s' [%s] but used in chunk", env == null ? null : env.getId(), envIndex);
            return newWorldEnvironmentSpawnData;
         } else {
            for (WorldSpawnWrapper config : envSpawnParameters.getSpawnWrappers()) {
               newWorldEnvironmentSpawnData.updateNPCs(config, world);
            }

            int moonPhase = worldTimeResource.getMoonPhase();
            newWorldEnvironmentSpawnData.recalculateWeight(moonPhase);
            newWorldEnvironmentSpawnData.resetUnspawnable();
            return newWorldEnvironmentSpawnData;
         }
      });
   }

   public int[] getWorldEnvironmentSpawnDataIndexes() {
      return this.worldEnvironmentSpawnData.keySet().toIntArray();
   }

   public void updateSpawnability() {
      this.unspawnable = true;
      ObjectIterator var1 = this.worldEnvironmentSpawnData.values().iterator();

      while (var1.hasNext()) {
         WorldEnvironmentSpawnData stats = (WorldEnvironmentSpawnData)var1.next();
         if (!stats.isUnspawnable()) {
            this.unspawnable = false;
            break;
         }
      }
   }

   public void forEachEnvironmentSpawnData(Consumer<WorldEnvironmentSpawnData> consumer) {
      this.worldEnvironmentSpawnData.values().forEach(consumer);
   }

   public boolean trackNPC(int environmentIndex, int roleIndex, int npcCount, @Nonnull World world, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      if (roleIndex >= 0 && environmentIndex >= 0) {
         WorldEnvironmentSpawnData stats = this.getOrCreateWorldEnvironmentSpawnData(environmentIndex, world, componentAccessor);
         stats.trackSpawn(roleIndex, npcCount);
         this.actualNPCs += npcCount;
         return true;
      } else {
         return false;
      }
   }

   public boolean untrackNPC(int environmentIndex, int roleIndex, int npcCount) {
      if (environmentIndex >= 0 && roleIndex >= 0) {
         WorldEnvironmentSpawnData stats = (WorldEnvironmentSpawnData)this.worldEnvironmentSpawnData.get(environmentIndex);
         if (stats == null) {
            LOGGER.at(Level.WARNING)
               .log(
                  "Removing NPC %s from environment %s which is not contained in the world environment spawn data",
                  NPCPlugin.get().getName(roleIndex),
                  Environment.getAssetMap().getAsset(environmentIndex).getId()
               );
            return false;
         } else {
            stats.trackDespawn(roleIndex, npcCount);
            this.actualNPCs -= npcCount;
            return true;
         }
      } else {
         return false;
      }
   }

   public void recalculateWorldCount() {
      this.actualNPCs = 0;
      this.expectedNPCs = 0.0;
      this.expectedNPCsInEmptyEnvironments = 0.0;
      ObjectIterator var1 = this.worldEnvironmentSpawnData.values().iterator();

      while (var1.hasNext()) {
         WorldEnvironmentSpawnData stats = (WorldEnvironmentSpawnData)var1.next();
         this.actualNPCs = this.actualNPCs + stats.getActualNPCs();
         if (stats.hasNPCs()) {
            this.expectedNPCs = this.expectedNPCs + stats.getExpectedNPCs();
         } else {
            this.expectedNPCsInEmptyEnvironments = this.expectedNPCsInEmptyEnvironments + stats.getExpectedNPCs();
         }
      }
   }

   public void queueUnspawnableChunk(int environmentIndex, long chunkIndex) {
      this.unspawnableProcessingQueue.add(new WorldSpawnData.UnspawnableEntry(environmentIndex, chunkIndex));
   }

   public boolean hasUnprocessedUnspawnableChunks() {
      return !this.unspawnableProcessingQueue.isEmpty();
   }

   @Nullable
   public WorldSpawnData.UnspawnableEntry nextUnspawnableChunk() {
      return this.unspawnableProcessingQueue.isEmpty() ? null : this.unspawnableProcessingQueue.poll();
   }

   @Override
   public Resource<EntityStore> clone() {
      throw new UnsupportedOperationException("Not implemented!");
   }

   public static class UnspawnableEntry {
      private final int environmentIndex;
      private final long chunkIndex;

      public UnspawnableEntry(int environmentIndex, long chunkIndex) {
         this.environmentIndex = environmentIndex;
         this.chunkIndex = chunkIndex;
      }

      public int getEnvironmentIndex() {
         return this.environmentIndex;
      }

      public long getChunkIndex() {
         return this.chunkIndex;
      }
   }
}
