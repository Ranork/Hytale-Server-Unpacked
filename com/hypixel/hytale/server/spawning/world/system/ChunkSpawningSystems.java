package com.hypixel.hytale.server.spawning.world.system;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.NonTicking;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefChangeSystem;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.asset.type.environment.config.Environment;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.environment.EnvironmentColumn;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.spawning.SpawningPlugin;
import com.hypixel.hytale.server.spawning.world.ChunkEnvironmentSpawnData;
import com.hypixel.hytale.server.spawning.world.WorldEnvironmentSpawnData;
import com.hypixel.hytale.server.spawning.world.component.ChunkSpawnData;
import com.hypixel.hytale.server.spawning.world.component.ChunkSpawnedNPCData;
import com.hypixel.hytale.server.spawning.world.component.WorldSpawnData;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class ChunkSpawningSystems {
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   private static final Query<ChunkStore> QUERY = Archetype.of(WorldChunk.getComponentType(), ChunkSpawnData.getComponentType());
   private static final Query<ChunkStore> TICKING_QUERY = Query.and(Query.not(ChunkStore.REGISTRY.getNonTickingComponentType()), QUERY);

   protected static boolean processStoppedChunk(
      @Nonnull Ref<ChunkStore> ref,
      @Nonnull Store<ChunkStore> store,
      @Nonnull WorldSpawnData worldSpawnData,
      @Nonnull ComponentType<ChunkStore, ChunkSpawnData> chunkSpawnDataComponentType,
      @Nonnull CommandBuffer<ChunkStore> commandBuffer
   ) {
      ChunkSpawnData chunkSpawnData = store.getComponent(ref, chunkSpawnDataComponentType);
      if (chunkSpawnData == null) {
         return false;
      } else {
         commandBuffer.removeComponent(ref, chunkSpawnDataComponentType);
         if (!chunkSpawnData.isStarted()) {
            return false;
         } else {
            World world = store.getExternalData().getWorld();
            Store<EntityStore> entityStore = world.getEntityStore().getStore();
            ObjectIterator<Entry<ChunkEnvironmentSpawnData>> iterator = Int2ObjectMaps.fastIterator(chunkSpawnData.getChunkEnvironmentSpawnDataMap());

            while (iterator.hasNext()) {
               Entry<ChunkEnvironmentSpawnData> entry = (Entry<ChunkEnvironmentSpawnData>)iterator.next();
               WorldEnvironmentSpawnData worldEnvironmentSpawnData = worldSpawnData.getWorldEnvironmentSpawnData(entry.getIntKey());
               if (worldEnvironmentSpawnData != null) {
                  ChunkEnvironmentSpawnData chunkEnvironmentSpawnData = (ChunkEnvironmentSpawnData)entry.getValue();
                  if (!chunkEnvironmentSpawnData.wasProcessedAsUnspawnable()) {
                     int segmentCount = -chunkEnvironmentSpawnData.getSegmentCount();
                     worldEnvironmentSpawnData.adjustSegmentCount(segmentCount);
                     if (worldEnvironmentSpawnData.getSegmentCount() < 0) {
                        Environment environment = Environment.getAssetMap().getAsset(worldEnvironmentSpawnData.getEnvironmentIndex());
                        String environmentName = environment != null ? environment.getId() : null;
                        LOGGER.at(Level.SEVERE)
                           .log("Block count for environment %s dropped below 0 to %s", environmentName, worldEnvironmentSpawnData.getSegmentCount());
                     }

                     worldSpawnData.adjustSegmentCount(segmentCount);
                  }

                  worldEnvironmentSpawnData.removeChunk(ref, entityStore);
               }
            }

            return true;
         }
      }
   }

   protected static boolean processStartedChunk(
      @Nonnull Ref<ChunkStore> ref,
      @Nonnull Store<ChunkStore> store,
      @Nonnull WorldChunk worldChunk,
      @Nonnull WorldSpawnData worldSpawnData,
      @Nonnull ComponentType<ChunkStore, ChunkSpawnData> chunkSpawnDataComponentType,
      @Nonnull ComponentType<ChunkStore, ChunkSpawnedNPCData> chunkSpawnedNPCDataComponentType,
      @Nonnull CommandBuffer<ChunkStore> commandBuffer
   ) {
      ChunkSpawnData chunkSpawnData = store.getComponent(ref, chunkSpawnDataComponentType);
      if (chunkSpawnData == null) {
         chunkSpawnData = new ChunkSpawnData();
         commandBuffer.putComponent(ref, chunkSpawnDataComponentType, chunkSpawnData);
      }

      commandBuffer.ensureComponent(ref, chunkSpawnedNPCDataComponentType);
      if (chunkSpawnData.isStarted()) {
         return false;
      } else {
         ObjectIterator<Entry<ChunkEnvironmentSpawnData>> iterator = Int2ObjectMaps.fastIterator(chunkSpawnData.getChunkEnvironmentSpawnDataMap());

         while (iterator.hasNext()) {
            Entry<ChunkEnvironmentSpawnData> entry = (Entry<ChunkEnvironmentSpawnData>)iterator.next();
            ((ChunkEnvironmentSpawnData)entry.getValue()).init(entry.getIntKey(), worldChunk);
         }

         preprocessChunk(chunkSpawnData, worldChunk);
         chunkSpawnData.setStarted(true);
         World world = store.getExternalData().getWorld();
         Store<EntityStore> entityStore = world.getEntityStore().getStore();
         iterator = Int2ObjectMaps.fastIterator(chunkSpawnData.getChunkEnvironmentSpawnDataMap());

         while (iterator.hasNext()) {
            Entry<ChunkEnvironmentSpawnData> entry = (Entry<ChunkEnvironmentSpawnData>)iterator.next();
            int environmentIndex = entry.getIntKey();
            ChunkEnvironmentSpawnData chunkEnvironmentSpawnData = (ChunkEnvironmentSpawnData)entry.getValue();
            chunkEnvironmentSpawnData.updateDensity(SpawningPlugin.get().getEnvironmentDensity(environmentIndex));
            WorldEnvironmentSpawnData worldEnvironmentSpawnData = worldSpawnData.getOrCreateWorldEnvironmentSpawnData(environmentIndex, world, entityStore);
            int segmentCount = chunkEnvironmentSpawnData.getSegmentCount();
            worldEnvironmentSpawnData.adjustSegmentCount(segmentCount);
            worldSpawnData.adjustSegmentCount(segmentCount);
            worldEnvironmentSpawnData.addChunk(ref, entityStore);
            HytaleLogger.Api context = LOGGER.at(Level.FINER);
            if (context.isEnabled()) {
               Environment environment = Environment.getAssetMap().getAsset(environmentIndex);
               context.log(
                  "   Add chunk [%s/%s] to env=%s, exp=%s, act=%s, blks=%s",
                  worldChunk.getX(),
                  worldChunk.getZ(),
                  environment == null ? null : environment.getId(),
                  worldEnvironmentSpawnData.getExpectedNPCs(),
                  worldEnvironmentSpawnData.getActualNPCs(),
                  worldEnvironmentSpawnData.getSegmentCount()
               );
            }
         }

         return true;
      }
   }

   private static void preprocessChunk(@Nonnull ChunkSpawnData chunkSpawnData, @Nonnull WorldChunk worldChunk) {
      for (int x = 0; x < 32; x++) {
         for (int z = 0; z < 32; z++) {
            preprocessColumn(chunkSpawnData, worldChunk, x, z);
         }
      }
   }

   private static void preprocessColumn(@Nonnull ChunkSpawnData chunkSpawnData, @Nonnull WorldChunk worldChunk, int x, int z) {
      EnvironmentColumn column = worldChunk.getBlockChunk().getEnvironmentColumn(x, z);
      Int2ObjectMap<ChunkEnvironmentSpawnData> environmentSpawnDataMap = chunkSpawnData.getChunkEnvironmentSpawnDataMap();

      for (int i = column.indexOf(0); i < column.size() && column.getValueMin(i) <= 320; i++) {
         int environmentIndex = column.getValue(i);
         ChunkEnvironmentSpawnData data = (ChunkEnvironmentSpawnData)environmentSpawnDataMap.get(environmentIndex);
         if (data == null) {
            data = new ChunkEnvironmentSpawnData();
            data.init(environmentIndex, worldChunk);
            environmentSpawnDataMap.put(environmentIndex, data);
         }

         data.registerSegment(x, z);
      }
   }

   protected static void updateChunkCount(int newChunks, @Nonnull WorldSpawnData worldSpawnData) {
      if (newChunks > 0) {
         worldSpawnData.setUnspawnable(false);
      }

      worldSpawnData.adjustChunkCount(newChunks);
      worldSpawnData.recalculateWorldCount();
   }

   public static class ChunkRefAdded extends RefSystem<ChunkStore> {
      private final ResourceType<EntityStore, WorldSpawnData> worldSpawnDataResourceType;
      private final ComponentType<ChunkStore, ChunkSpawnData> chunkSpawnDataComponentType;
      private final ComponentType<ChunkStore, ChunkSpawnedNPCData> chunkSpawnedNPCDataComponentType;
      private final ComponentType<ChunkStore, WorldChunk> worldChunkComponentType;

      public ChunkRefAdded(
         ResourceType<EntityStore, WorldSpawnData> worldSpawnDataResourceType,
         ComponentType<ChunkStore, ChunkSpawnData> chunkSpawnDataComponentType,
         ComponentType<ChunkStore, ChunkSpawnedNPCData> chunkSpawnedNPCDataComponentType
      ) {
         this.worldSpawnDataResourceType = worldSpawnDataResourceType;
         this.chunkSpawnDataComponentType = chunkSpawnDataComponentType;
         this.chunkSpawnedNPCDataComponentType = chunkSpawnedNPCDataComponentType;
         this.worldChunkComponentType = WorldChunk.getComponentType();
      }

      @Nonnull
      @Override
      public Query<ChunkStore> getQuery() {
         return ChunkSpawningSystems.TICKING_QUERY;
      }

      @Override
      public void onEntityAdded(
         @Nonnull Ref<ChunkStore> ref, @Nonnull AddReason reason, @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer
      ) {
         WorldSpawnData worldSpawnData = store.getExternalData().getWorld().getEntityStore().getStore().getResource(this.worldSpawnDataResourceType);
         WorldChunk worldChunk = store.getComponent(ref, this.worldChunkComponentType);
         if (ChunkSpawningSystems.processStartedChunk(
            ref, store, worldChunk, worldSpawnData, this.chunkSpawnDataComponentType, this.chunkSpawnedNPCDataComponentType, commandBuffer
         )) {
            ChunkSpawningSystems.LOGGER.at(Level.FINE).log("Adding chunk [%s/%s]", worldChunk.getX(), worldChunk.getZ());
            ChunkSpawningSystems.updateChunkCount(1, worldSpawnData);
         }
      }

      @Override
      public void onEntityRemove(
         @Nonnull Ref<ChunkStore> ref, @Nonnull RemoveReason reason, @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer
      ) {
      }
   }

   public static class TickingState extends RefChangeSystem<ChunkStore, NonTicking<ChunkStore>> {
      private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
      private final ResourceType<EntityStore, WorldSpawnData> worldSpawnDataResourceType;
      private final ComponentType<ChunkStore, ChunkSpawnData> chunkSpawnDataComponentType;
      private final ComponentType<ChunkStore, ChunkSpawnedNPCData> chunkSpawnedNPCDataComponentType;
      private final ComponentType<ChunkStore, WorldChunk> worldChunkComponentType;

      public TickingState(
         ResourceType<EntityStore, WorldSpawnData> worldSpawnDataResourceType,
         ComponentType<ChunkStore, ChunkSpawnData> chunkSpawnDataComponentType,
         ComponentType<ChunkStore, ChunkSpawnedNPCData> chunkSpawnedNPCDataComponentType
      ) {
         this.worldSpawnDataResourceType = worldSpawnDataResourceType;
         this.chunkSpawnDataComponentType = chunkSpawnDataComponentType;
         this.chunkSpawnedNPCDataComponentType = chunkSpawnedNPCDataComponentType;
         this.worldChunkComponentType = WorldChunk.getComponentType();
      }

      @Override
      public Query<ChunkStore> getQuery() {
         return this.worldChunkComponentType;
      }

      @Nonnull
      @Override
      public ComponentType<ChunkStore, NonTicking<ChunkStore>> componentType() {
         return ChunkStore.REGISTRY.getNonTickingComponentType();
      }

      public void onComponentAdded(
         @Nonnull Ref<ChunkStore> ref,
         @Nonnull NonTicking<ChunkStore> component,
         @Nonnull Store<ChunkStore> store,
         @Nonnull CommandBuffer<ChunkStore> commandBuffer
      ) {
         WorldSpawnData worldSpawnData = store.getExternalData().getWorld().getEntityStore().getStore().getResource(this.worldSpawnDataResourceType);
         WorldChunk worldChunk = store.getComponent(ref, this.worldChunkComponentType);
         if (ChunkSpawningSystems.processStoppedChunk(ref, store, worldSpawnData, this.chunkSpawnDataComponentType, commandBuffer)) {
            LOGGER.at(Level.FINE).log("Removing chunk [%s/%s]", worldChunk.getX(), worldChunk.getZ());
            ChunkSpawningSystems.updateChunkCount(-1, worldSpawnData);
         }
      }

      public void onComponentSet(
         @Nonnull Ref<ChunkStore> ref,
         NonTicking<ChunkStore> oldComponent,
         @Nonnull NonTicking<ChunkStore> newComponent,
         @Nonnull Store<ChunkStore> store,
         @Nonnull CommandBuffer<ChunkStore> commandBuffer
      ) {
      }

      public void onComponentRemoved(
         @Nonnull Ref<ChunkStore> ref,
         @Nonnull NonTicking<ChunkStore> component,
         @Nonnull Store<ChunkStore> store,
         @Nonnull CommandBuffer<ChunkStore> commandBuffer
      ) {
         WorldSpawnData worldSpawnData = store.getExternalData().getWorld().getEntityStore().getStore().getResource(this.worldSpawnDataResourceType);
         WorldChunk worldChunk = store.getComponent(ref, this.worldChunkComponentType);
         if (ChunkSpawningSystems.processStartedChunk(
            ref, store, worldChunk, worldSpawnData, this.chunkSpawnDataComponentType, this.chunkSpawnedNPCDataComponentType, commandBuffer
         )) {
            LOGGER.at(Level.FINE).log("Adding chunk [%s/%s]", worldChunk.getX(), worldChunk.getZ());
            ChunkSpawningSystems.updateChunkCount(1, worldSpawnData);
         }
      }
   }
}
