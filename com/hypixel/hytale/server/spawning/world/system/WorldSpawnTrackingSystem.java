package com.hypixel.hytale.server.spawning.world.system;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.iterator.SpiralIterator;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.asset.type.environment.config.Environment;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.spawning.world.ChunkEnvironmentSpawnData;
import com.hypixel.hytale.server.spawning.world.WorldEnvironmentSpawnData;
import com.hypixel.hytale.server.spawning.world.WorldNPCSpawnStat;
import com.hypixel.hytale.server.spawning.world.component.ChunkSpawnData;
import com.hypixel.hytale.server.spawning.world.component.ChunkSpawnedNPCData;
import com.hypixel.hytale.server.spawning.world.component.WorldSpawnData;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WorldSpawnTrackingSystem extends RefSystem<EntityStore> {
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   private static final int COUNT_SPREAD_RADIUS = 3;
   @Nullable
   private final ComponentType<EntityStore, NPCEntity> npcComponentType = NPCEntity.getComponentType();
   private final ComponentType<EntityStore, TransformComponent> transformComponentType = TransformComponent.getComponentType();
   private final ResourceType<EntityStore, WorldSpawnData> worldSpawnDataResourceType;
   private final ComponentType<ChunkStore, ChunkSpawnData> chunkSpawnDataComponentType;
   private final ComponentType<ChunkStore, ChunkSpawnedNPCData> chunkSpawnedNPCDataComponentType;
   @Nonnull
   private final Query<EntityStore> query;

   public WorldSpawnTrackingSystem(
      @Nonnull ResourceType<EntityStore, WorldSpawnData> worldSpawnDataResourceType,
      @Nonnull ComponentType<ChunkStore, ChunkSpawnData> chunkSpawnDataComponentType,
      @Nonnull ComponentType<ChunkStore, ChunkSpawnedNPCData> chunkSpawnedNPCDataComponentType
   ) {
      this.worldSpawnDataResourceType = worldSpawnDataResourceType;
      this.chunkSpawnDataComponentType = chunkSpawnDataComponentType;
      this.chunkSpawnedNPCDataComponentType = chunkSpawnedNPCDataComponentType;
      this.query = Query.and(this.npcComponentType, this.transformComponentType);
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
      NPCEntity npcComponent = store.getComponent(ref, this.npcComponentType);

      assert npcComponent != null;

      boolean isTracked = npcComponent.updateSpawnTrackingState(true);
      if (!isTracked) {
         World world = store.getExternalData().getWorld();
         WorldSpawnData worldSpawnData = store.getResource(this.worldSpawnDataResourceType);
         switch (reason) {
            case SPAWN:
               int environmentIndex = npcComponent.getEnvironment();
               if (!trackNPC(environmentIndex, npcComponent.getSpawnRoleIndex(), worldSpawnData, world, commandBuffer)) {
                  return;
               }

               ChunkStore chunkStore = world.getChunkStore();
               Store<ChunkStore> chunkComponentStore = chunkStore.getStore();
               Vector3d position = store.getComponent(ref, this.transformComponentType).getPosition();
               int originX = ChunkUtil.chunkCoordinate(position.getX());
               int originZ = ChunkUtil.chunkCoordinate(position.getZ());
               Ref<ChunkStore> chunkRef = chunkStore.getChunkReference(ChunkUtil.indexChunk(originX, originZ));
               double count = trackNewNPC(
                  chunkRef, environmentIndex, 1.0, this.chunkSpawnDataComponentType, this.chunkSpawnedNPCDataComponentType, chunkComponentStore
               );
               if (count <= 0.0) {
                  return;
               }

               SpiralIterator spiralIterator = worldSpawnData.getSpiralIterator();
               spiralIterator.init(originX, originZ, 3);
               if (!spiralIterator.hasNext()) {
                  return;
               }

               spiralIterator.next();
               int checkedCount = 0;
               int unloadedCount = 0;

               while (spiralIterator.hasNext() && count > 0.0) {
                  checkedCount++;
                  long chunkIndex = spiralIterator.next();
                  chunkRef = chunkStore.getChunkReference(chunkIndex);
                  if (chunkRef == null) {
                     unloadedCount++;
                  } else {
                     count = trackNewNPC(
                        chunkRef, environmentIndex, count, this.chunkSpawnDataComponentType, this.chunkSpawnedNPCDataComponentType, chunkComponentStore
                     );
                  }
               }

               if (count > 0.0) {
                  HytaleLogger.Api context = LOGGER.at(Level.FINE);
                  if (context.isEnabled()) {
                     context.log(
                        "Failed to spread %s of an NPC spawn to neighbouring chunks. Checked %s chunks, %s not in memory. Centered on chunk (%s, %s), spreading to other chunks with matching environment",
                        count,
                        checkedCount,
                        unloadedCount,
                        originX,
                        originZ
                     );
                  }

                  Set<Ref<ChunkStore>> chunkOptions = worldSpawnData.getWorldEnvironmentSpawnData(environmentIndex).getChunkRefSet();
                  Iterator<Ref<ChunkStore>> iterator = chunkOptions.iterator();

                  while (iterator.hasNext() && count > 0.0) {
                     count = trackNewNPC(
                        iterator.next(), environmentIndex, count, this.chunkSpawnDataComponentType, this.chunkSpawnedNPCDataComponentType, chunkComponentStore
                     );
                  }

                  if (count > 0.0 && context.isEnabled()) {
                     WorldEnvironmentSpawnData worldEnvironmentSpawnData = worldSpawnData.getWorldEnvironmentSpawnData(environmentIndex);
                     WorldNPCSpawnStat npcSpawnStat = (WorldNPCSpawnStat)worldEnvironmentSpawnData.getNpcStatMap().get(npcComponent.getRoleIndex());
                     context.log(
                        "Failed to spread %s of an NPC spawn across random chunks with matching environments (%s). NPC Type: %s. World environment exp: %s act: %s. Stat exp: %s act: %s",
                        count,
                        Environment.getAssetMap().getAsset(environmentIndex).getId(),
                        NPCPlugin.get().getName(npcComponent.getRoleIndex()),
                        worldEnvironmentSpawnData.getExpectedNPCs(),
                        worldEnvironmentSpawnData.getActualNPCs(),
                        npcSpawnStat.getExpected(),
                        npcSpawnStat.getActual()
                     );
                  }
               }

               spiralIterator.reset();
               break;
            case LOAD:
               trackNPC(npcComponent.getEnvironment(), npcComponent.getSpawnRoleIndex(), worldSpawnData, world, commandBuffer);
         }
      }
   }

   @Override
   public void onEntityRemove(
      @Nonnull Ref<EntityStore> ref, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
   ) {
      NPCEntity npcComponent = store.getComponent(ref, this.npcComponentType);

      assert npcComponent != null;

      boolean isTracked = npcComponent.updateSpawnTrackingState(false);
      if (isTracked) {
         WorldSpawnData worldSpawnData = store.getResource(this.worldSpawnDataResourceType);
         switch (reason) {
            case REMOVE:
               int environmentIndex = npcComponent.getEnvironment();
               if (!untrackNPC(environmentIndex, npcComponent.getSpawnRoleIndex(), worldSpawnData)) {
                  return;
               }

               World world = store.getExternalData().getWorld();
               ChunkStore chunkStore = world.getChunkStore();
               Store<ChunkStore> chunkComponentStore = chunkStore.getStore();
               TransformComponent transformComponent = store.getComponent(ref, this.transformComponentType);

               assert transformComponent != null;

               Vector3d position = transformComponent.getPosition();
               int originX = ChunkUtil.chunkCoordinate(position.getX());
               int originZ = ChunkUtil.chunkCoordinate(position.getZ());
               Ref<ChunkStore> chunkRef = chunkStore.getChunkReference(ChunkUtil.indexChunk(originX, originZ));
               double count = untrackRemovedNPC(
                  chunkRef, environmentIndex, 1.0, this.chunkSpawnDataComponentType, this.chunkSpawnedNPCDataComponentType, chunkComponentStore
               );
               if (count <= 0.0) {
                  return;
               }

               SpiralIterator spiralIterator = worldSpawnData.getSpiralIterator();
               spiralIterator.init(originX, originZ, 3);
               if (!spiralIterator.hasNext()) {
                  return;
               }

               spiralIterator.next();

               while (spiralIterator.hasNext() && count > 0.0) {
                  long chunkIndex = spiralIterator.next();
                  chunkRef = chunkStore.getChunkReference(chunkIndex);
                  if (chunkRef != null) {
                     count = untrackRemovedNPC(
                        chunkRef, environmentIndex, count, this.chunkSpawnDataComponentType, this.chunkSpawnedNPCDataComponentType, chunkComponentStore
                     );
                  }
               }

               if (count > 0.0) {
                  HytaleLogger.Api context = LOGGER.at(Level.FINE);
                  if (context.isEnabled()) {
                     context.log(
                        "Failed to remove %s of a spread NPC spawn from neighbouring chunks, spreading to other chunks with matching environment", count
                     );
                  }

                  Set<Ref<ChunkStore>> chunkOptions = worldSpawnData.getWorldEnvironmentSpawnData(environmentIndex).getChunkRefSet();
                  Iterator<Ref<ChunkStore>> iterator = chunkOptions.iterator();

                  while (iterator.hasNext() && count > 0.0) {
                     count = untrackRemovedNPC(
                        iterator.next(), environmentIndex, count, this.chunkSpawnDataComponentType, this.chunkSpawnedNPCDataComponentType, chunkComponentStore
                     );
                  }

                  if (count > 0.0 && context.isEnabled()) {
                     context.log("Failed to remove %s of an NPC spawn from random chunks with matching environments", count);
                  }
               }

               spiralIterator.reset();
               break;
            case UNLOAD:
               untrackNPC(npcComponent.getEnvironment(), npcComponent.getSpawnRoleIndex(), worldSpawnData);
         }
      }
   }

   private static boolean trackNPC(
      int environmentIndex,
      int roleIndex,
      @Nonnull WorldSpawnData worldSpawnData,
      @Nonnull World world,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      if (!worldSpawnData.trackNPC(environmentIndex, roleIndex, 1, world, componentAccessor)) {
         return false;
      } else {
         HytaleLogger.Api context = LOGGER.at(Level.FINER);
         if (context.isEnabled()) {
            context.log("Track Spawn env=%s role=%s", getEnvironmentName(environmentIndex), NPCPlugin.get().getName(roleIndex));
         }

         return true;
      }
   }

   private static boolean untrackNPC(int environmentIndex, int roleIndex, @Nonnull WorldSpawnData worldSpawnData) {
      if (!worldSpawnData.untrackNPC(environmentIndex, roleIndex, 1)) {
         return false;
      } else {
         HytaleLogger.Api context = LOGGER.at(Level.FINER);
         if (context.isEnabled()) {
            context.log("Despawn env=%s role=%s", getEnvironmentName(environmentIndex), NPCPlugin.get().getName(roleIndex));
         }

         return true;
      }
   }

   private static double trackNewNPC(
      @Nonnull Ref<ChunkStore> ref,
      int environmentIndex,
      double count,
      @Nonnull ComponentType<ChunkStore, ChunkSpawnData> chunkSpawnDataComponentType,
      @Nonnull ComponentType<ChunkStore, ChunkSpawnedNPCData> chunkSpawnedNPCDataComponentType,
      @Nonnull Store<ChunkStore> store
   ) {
      ChunkSpawnData chunkSpawnData = store.getComponent(ref, chunkSpawnDataComponentType);
      if (chunkSpawnData == null) {
         return count;
      } else {
         ChunkEnvironmentSpawnData spawnData = (ChunkEnvironmentSpawnData)chunkSpawnData.getChunkEnvironmentSpawnDataMap().get(environmentIndex);
         if (spawnData == null) {
            return count;
         } else {
            ChunkSpawnedNPCData chunkSpawnedNPCDataComponent = store.getComponent(ref, chunkSpawnedNPCDataComponentType);

            assert chunkSpawnedNPCDataComponent != null;

            double spawnedNPCs = chunkSpawnedNPCDataComponent.getEnvironmentSpawnCount(environmentIndex);
            if (spawnData.isFullyPopulated(spawnedNPCs)) {
               return count;
            } else {
               WorldChunk worldChunkComponent = store.getComponent(ref, WorldChunk.getComponentType());

               assert worldChunkComponent != null;

               worldChunkComponent.markNeedsSaving();
               double expectedNPCs = spawnData.getExpectedNPCs();
               double remainingSpace = expectedNPCs - spawnedNPCs;
               if (count > remainingSpace) {
                  HytaleLogger.Api context = LOGGER.at(Level.FINEST);
                  if (context.isEnabled()) {
                     context.log("Spreading " + remainingSpace + " to chunk " + worldChunkComponent.getIndex() + " with total capacity " + expectedNPCs);
                  }

                  count -= remainingSpace;
                  chunkSpawnedNPCDataComponent.setEnvironmentSpawnCount(environmentIndex, expectedNPCs);
                  return count;
               } else {
                  HytaleLogger.Api context = LOGGER.at(Level.FINEST);
                  if (context.isEnabled()) {
                     context.log("Spreading " + count + " to chunk " + worldChunkComponent.getIndex() + " with total capacity " + expectedNPCs);
                  }

                  chunkSpawnedNPCDataComponent.setEnvironmentSpawnCount(environmentIndex, spawnedNPCs + count);
                  return 0.0;
               }
            }
         }
      }
   }

   private static double untrackRemovedNPC(
      @Nonnull Ref<ChunkStore> ref,
      int environmentIndex,
      double count,
      @Nonnull ComponentType<ChunkStore, ChunkSpawnData> chunkSpawnDataComponentType,
      @Nonnull ComponentType<ChunkStore, ChunkSpawnedNPCData> chunkSpawnedNPCDataComponentType,
      @Nonnull Store<ChunkStore> store
   ) {
      ChunkSpawnData chunkSpawnData = store.getComponent(ref, chunkSpawnDataComponentType);
      if (chunkSpawnData == null) {
         return count;
      } else {
         ChunkEnvironmentSpawnData spawnData = (ChunkEnvironmentSpawnData)chunkSpawnData.getChunkEnvironmentSpawnDataMap().get(environmentIndex);
         if (spawnData == null) {
            return count;
         } else {
            ChunkSpawnedNPCData chunkSpawnedNPCDataComponent = store.getComponent(ref, chunkSpawnedNPCDataComponentType);

            assert chunkSpawnedNPCDataComponent != null;

            double spawnedNPCs = chunkSpawnedNPCDataComponent.getEnvironmentSpawnCount(environmentIndex);
            if (spawnedNPCs <= 0.0) {
               return count;
            } else {
               WorldChunk worldChunkComponent = store.getComponent(ref, WorldChunk.getComponentType());

               assert worldChunkComponent != null;

               worldChunkComponent.markNeedsSaving();
               double expectedNPCs = spawnData.getExpectedNPCs();
               if (spawnedNPCs < count) {
                  HytaleLogger.Api context = LOGGER.at(Level.FINEST);
                  if (context.isEnabled()) {
                     context.log("Spreading removal of " + spawnedNPCs + " to chunk " + worldChunkComponent.getIndex() + " with total capacity " + expectedNPCs);
                  }

                  count -= spawnedNPCs;
                  chunkSpawnedNPCDataComponent.setEnvironmentSpawnCount(environmentIndex, 0.0);
                  return count;
               } else {
                  HytaleLogger.Api context = LOGGER.at(Level.FINEST);
                  if (context.isEnabled()) {
                     context.log("Spreading removal of " + count + " to chunk " + worldChunkComponent.getIndex() + " with total capacity " + expectedNPCs);
                  }

                  chunkSpawnedNPCDataComponent.setEnvironmentSpawnCount(environmentIndex, spawnedNPCs - count);
                  return 0.0;
               }
            }
         }
      }
   }

   private static String getEnvironmentName(int id) {
      Environment env = Environment.getAssetMap().getAsset(id);
      return env != null ? env.getId() : "<" + id + ">";
   }
}
