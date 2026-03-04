package com.hypixel.hytale.server.spawning.suppression.system;

import com.hypixel.fastutil.longs.Long2ObjectConcurrentHashMap;
import com.hypixel.hytale.assetstore.event.LoadedAssetsEvent;
import com.hypixel.hytale.assetstore.event.RemovedAssetsEvent;
import com.hypixel.hytale.assetstore.map.IndexedAssetMap;
import com.hypixel.hytale.builtin.tagset.TagSetPlugin;
import com.hypixel.hytale.builtin.tagset.config.NPCGroup;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.component.system.HolderSystem;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.component.system.StoreSystem;
import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.FromPrefab;
import com.hypixel.hytale.server.core.modules.entity.component.FromWorldGen;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.prefab.PrefabCopyableComponent;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.spawning.SpawningPlugin;
import com.hypixel.hytale.server.spawning.assets.spawnsuppression.SpawnSuppression;
import com.hypixel.hytale.server.spawning.spawnmarkers.SpawnMarkerEntity;
import com.hypixel.hytale.server.spawning.suppression.SpawnSuppressorEntry;
import com.hypixel.hytale.server.spawning.suppression.component.ChunkSuppressionEntry;
import com.hypixel.hytale.server.spawning.suppression.component.ChunkSuppressionQueue;
import com.hypixel.hytale.server.spawning.suppression.component.SpawnSuppressionComponent;
import com.hypixel.hytale.server.spawning.suppression.component.SpawnSuppressionController;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SpawnSuppressionSystems {
   private static void suppressSpawns(
      @Nonnull ResourceType<ChunkStore, ChunkSuppressionQueue> chunkSuppressionQueueResourceType,
      @Nonnull ComponentType<EntityStore, SpawnMarkerEntity> spawnMarkerEntityComponentType,
      UUID uuid,
      @Nonnull SpawnSuppressorEntry entry,
      @Nonnull SpawnSuppressionController suppressionController,
      @Nonnull Store<EntityStore> store,
      @Nonnull ChunkStore chunkComponentStore
   ) {
      String suppressionId = entry.getSuppressionId();
      SpawningPlugin.get().getLogger().at(Level.FINEST).log("Suppressing spawns with id '%s' from suppressor %s", suppressionId, uuid);
      SpawnSuppression suppression = SpawnSuppression.getAssetMap().getAsset(suppressionId);
      if (suppression == null) {
         SpawningPlugin.get().getLogger().at(Level.WARNING).log("Spawn suppression config '%s' does not exist", suppressionId);
      } else {
         int[] suppressedGroups = suppression.getSuppressedGroupIds();
         IntSet suppressedRoles;
         if (suppressedGroups != null && suppressedGroups.length > 0) {
            suppressedRoles = new IntOpenHashSet();

            for (int suppressedGroup : suppressedGroups) {
               IntSet set = TagSetPlugin.get(NPCGroup.class).getSet(suppressedGroup);
               if (set != null) {
                  suppressedRoles.addAll(set);
               }
            }
         } else {
            suppressedRoles = null;
         }

         double radius = suppression.getRadius();
         Vector3d position = entry.getPosition();
         int minChunkX = MathUtil.floor(position.x - radius) >> 5;
         int minChunkZ = MathUtil.floor(position.z - radius) >> 5;
         int maxChunkX = MathUtil.floor(position.x + radius) >> 5;
         int maxChunkZ = MathUtil.floor(position.z + radius) >> 5;
         int minY = MathUtil.floor(position.y - radius);
         int maxY = MathUtil.floor(position.y + radius);
         Long2ObjectConcurrentHashMap<ChunkSuppressionEntry> chunkSuppressionMap = suppressionController.getChunkSuppressionMap();

         for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
               long chunkIndex = ChunkUtil.indexChunk(chunkX, chunkZ);
               SpawningPlugin.get().getLogger().at(Level.FINEST).log("Suppressing chunk index %s with id '%s'", chunkIndex, suppressionId);
               ChunkSuppressionEntry oldEntry = chunkSuppressionMap.get(chunkIndex);
               List<ChunkSuppressionEntry.SuppressionSpan> suppressionSpanList;
               if (oldEntry != null) {
                  suppressionSpanList = new ObjectArrayList(oldEntry.getSuppressionSpans());
               } else {
                  suppressionSpanList = new ObjectArrayList();
               }

               suppressionSpanList.add(new ChunkSuppressionEntry.SuppressionSpan(uuid, minY, maxY, suppressedRoles));
               suppressionSpanList.sort(Comparator.comparingInt(ChunkSuppressionEntry.SuppressionSpan::getMinY));
               ChunkSuppressionEntry chunkEntry = new ChunkSuppressionEntry(suppressionSpanList);
               chunkSuppressionMap.put(chunkIndex, chunkEntry);
               Ref<ChunkStore> chunkReference = chunkComponentStore.getChunkReference(chunkIndex);
               if (chunkReference != null) {
                  ChunkSuppressionQueue chunkSuppressionQueue = chunkComponentStore.getStore().getResource(chunkSuppressionQueueResourceType);
                  chunkSuppressionQueue.queueForAdd(chunkReference, chunkEntry);
                  SpawningPlugin.get().getLogger().at(Level.FINEST).log("Queueing annotation of chunk index %s with id '%s'", chunkIndex, suppressionId);
               }
            }
         }

         if (suppression.isSuppressSpawnMarkers()) {
            ObjectList<Ref<EntityStore>> results = SpatialResource.getThreadLocalReferenceList();
            SpatialResource<Ref<EntityStore>, EntityStore> spatialResource = store.getResource(SpawningPlugin.get().getSpawnMarkerSpatialResource());
            spatialResource.getSpatialStructure().collect(position, radius, results);

            for (int i = 0; i < results.size(); i++) {
               Ref<EntityStore> markerRef = (Ref<EntityStore>)results.get(i);
               SpawnMarkerEntity marker = store.getComponent(markerRef, spawnMarkerEntityComponentType);
               marker.suppress(uuid);
               HytaleLogger.Api context = SpawningPlugin.get().getLogger().at(Level.FINEST);
               if (context.isEnabled()) {
                  context.log("Suppressing spawn marker %s", store.getComponent(markerRef, UUIDComponent.getComponentType()).getUuid());
               }
            }
         }
      }
   }

   public static class EnsureNetworkSendable extends HolderSystem<EntityStore> {
      private final Query<EntityStore> query = Query.and(SpawnSuppressionComponent.getComponentType(), Query.not(NetworkId.getComponentType()));

      @Override
      public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
         holder.addComponent(NetworkId.getComponentType(), new NetworkId(store.getExternalData().takeNextNetworkId()));
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

   public static class Load extends StoreSystem<EntityStore> {
      private final ResourceType<EntityStore, SpawnSuppressionController> spawnSuppressionControllerResourceType;
      private final ComponentType<EntityStore, SpawnMarkerEntity> spawnMarkerEntityComponentType;
      private final ResourceType<ChunkStore, ChunkSuppressionQueue> chunkSuppressionQueueResourceType;
      private final ComponentType<ChunkStore, ChunkSuppressionEntry> chunkSuppressionEntryComponentType;
      @Nonnull
      private final EventRegistry eventRegistry;

      public Load(
         ResourceType<EntityStore, SpawnSuppressionController> spawnSuppressionControllerResourceType,
         ComponentType<EntityStore, SpawnMarkerEntity> spawnMarkerEntityComponentType,
         ResourceType<ChunkStore, ChunkSuppressionQueue> chunkSuppressionQueueResourceType,
         ComponentType<ChunkStore, ChunkSuppressionEntry> chunkSuppressionEntryComponentType
      ) {
         this.spawnSuppressionControllerResourceType = spawnSuppressionControllerResourceType;
         this.spawnMarkerEntityComponentType = spawnMarkerEntityComponentType;
         this.chunkSuppressionQueueResourceType = chunkSuppressionQueueResourceType;
         this.chunkSuppressionEntryComponentType = chunkSuppressionEntryComponentType;
         this.eventRegistry = new EventRegistry(new CopyOnWriteArrayList<>(), () -> true, null, SpawningPlugin.get().getEventRegistry());
         this.eventRegistry.register(LoadedAssetsEvent.class, SpawnSuppression.class, this::onSpawnSuppressionsLoaded);
         this.eventRegistry.register(RemovedAssetsEvent.class, SpawnSuppression.class, this::onSpawnSuppressionsRemoved);
      }

      @Override
      public void onSystemAddedToStore(@Nonnull Store<EntityStore> store) {
         SpawnSuppressionController resource = store.getResource(this.spawnSuppressionControllerResourceType);
         Map<UUID, SpawnSuppressorEntry> spawnSuppressorMap = resource.getSpawnSuppressorMap();
         spawnSuppressorMap.forEach(
            (id, entry) -> SpawnSuppressionSystems.suppressSpawns(
               this.chunkSuppressionQueueResourceType,
               this.spawnMarkerEntityComponentType,
               id,
               entry,
               resource,
               store,
               store.getExternalData().getWorld().getChunkStore()
            )
         );
      }

      @Override
      public void onSystemRemovedFromStore(@Nonnull Store<EntityStore> store) {
      }

      @Override
      public void onSystemUnregistered() {
         this.eventRegistry.shutdownAndCleanup(true);
      }

      private void onSpawnSuppressionsLoaded(@Nonnull LoadedAssetsEvent<String, SpawnSuppression, IndexedAssetMap<String, SpawnSuppression>> event) {
         Map<String, SpawnSuppression> loadedAssets = event.getLoadedAssets();
         Universe.get().getWorlds().forEach((name, world) -> world.execute(() -> {
            boolean hasChanges = false;
            Store<EntityStore> store = world.getEntityStore().getStore();
            SpawnSuppressionController suppressionController = store.getResource(this.spawnSuppressionControllerResourceType);
            Map<UUID, SpawnSuppressorEntry> spawnSuppressorMap = suppressionController.getSpawnSuppressorMap();

            for (SpawnSuppressorEntry entry : spawnSuppressorMap.values()) {
               if (loadedAssets.containsKey(entry.getSuppressionId())) {
                  hasChanges = true;
                  break;
               }
            }

            if (hasChanges) {
               this.rebuildSuppressionMap(world, store, suppressionController);
            }
         }));
      }

      private void onSpawnSuppressionsRemoved(@Nonnull RemovedAssetsEvent<String, SpawnSuppression, IndexedAssetMap<String, SpawnSuppression>> event) {
         Set<String> removedAssets = event.getRemovedAssets();
         Universe.get().getWorlds().forEach((name, world) -> world.execute(() -> {
            boolean hasChanges = false;
            Store<EntityStore> store = world.getEntityStore().getStore();
            SpawnSuppressionController suppressionController = store.getResource(this.spawnSuppressionControllerResourceType);
            Map<UUID, SpawnSuppressorEntry> spawnSuppressorMap = suppressionController.getSpawnSuppressorMap();

            for (SpawnSuppressorEntry entry : spawnSuppressorMap.values()) {
               if (removedAssets.contains(entry.getSuppressionId())) {
                  hasChanges = true;
                  break;
               }
            }

            if (hasChanges) {
               this.rebuildSuppressionMap(world, store, suppressionController);
            }
         }));
      }

      private void rebuildSuppressionMap(@Nonnull World world, @Nonnull Store<EntityStore> store, @Nonnull SpawnSuppressionController suppressionController) {
         SpawningPlugin.get().getLogger().at(Level.INFO).log("Rebuilding spawn suppression map for world %s", world.getName());
         ChunkStore chunkComponentStore = world.getChunkStore();
         Store<ChunkStore> chunkStore = chunkComponentStore.getStore();
         Long2ObjectConcurrentHashMap<ChunkSuppressionEntry> chunkSuppressionMap = suppressionController.getChunkSuppressionMap();
         LongIterator spawnSuppressorMap = chunkSuppressionMap.keySet().iterator();

         while (spawnSuppressorMap.hasNext()) {
            long key = (Long)spawnSuppressorMap.next();
            Ref<ChunkStore> chunkReference = chunkComponentStore.getChunkReference(key);
            if (chunkReference != null) {
               chunkStore.tryRemoveComponent(chunkReference, this.chunkSuppressionEntryComponentType);
            }
         }

         chunkSuppressionMap.clear();
         store.forEachEntityParallel(
            SpawnMarkerEntity.getComponentType(),
            (index, archetypeChunk, commandBuffer) -> archetypeChunk.getComponent(index, SpawnMarkerEntity.getComponentType()).clearAllSuppressions()
         );
         Map<UUID, SpawnSuppressorEntry> spawnSuppressorMapx = suppressionController.getSpawnSuppressorMap();
         spawnSuppressorMapx.forEach(
            (id, entry) -> SpawnSuppressionSystems.suppressSpawns(
               this.chunkSuppressionQueueResourceType,
               this.spawnMarkerEntityComponentType,
               id,
               entry,
               suppressionController,
               store,
               store.getExternalData().getWorld().getChunkStore()
            )
         );
      }
   }

   public static class Suppressor extends RefSystem<EntityStore> {
      private final ComponentType<EntityStore, SpawnSuppressionComponent> spawnSuppressorComponentType;
      private final ResourceType<EntityStore, SpawnSuppressionController> spawnSuppressionControllerResourceType;
      private final ComponentType<EntityStore, SpawnMarkerEntity> spawnMarkerEntityComponentType;
      private final ResourceType<ChunkStore, ChunkSuppressionQueue> chunkSuppressionQueueResourceType;
      private final ResourceType<EntityStore, SpatialResource<Ref<EntityStore>, EntityStore>> spawnMarkerSpatialResourceType;
      private final ComponentType<EntityStore, TransformComponent> transformComponentType = TransformComponent.getComponentType();
      private final ComponentType<EntityStore, UUIDComponent> uuidComponentType = UUIDComponent.getComponentType();
      @Nonnull
      private final Query<EntityStore> query;

      public Suppressor(
         ComponentType<EntityStore, SpawnSuppressionComponent> spawnSuppressorComponentType,
         ResourceType<EntityStore, SpawnSuppressionController> spawnSuppressionControllerResourceType,
         ComponentType<EntityStore, SpawnMarkerEntity> spawnMarkerEntityComponentType,
         ResourceType<ChunkStore, ChunkSuppressionQueue> chunkSuppressionQueueResourceType,
         ResourceType<EntityStore, SpatialResource<Ref<EntityStore>, EntityStore>> spawnMarkerSpatialResourceType
      ) {
         this.spawnSuppressorComponentType = spawnSuppressorComponentType;
         this.spawnSuppressionControllerResourceType = spawnSuppressionControllerResourceType;
         this.spawnMarkerEntityComponentType = spawnMarkerEntityComponentType;
         this.chunkSuppressionQueueResourceType = chunkSuppressionQueueResourceType;
         this.spawnMarkerSpatialResourceType = spawnMarkerSpatialResourceType;
         this.query = Query.and(spawnSuppressorComponentType, this.transformComponentType, this.uuidComponentType);
      }

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return this.query;
      }

      @Nullable
      @Override
      public SystemGroup<EntityStore> getGroup() {
         return EntityModule.get().getPreClearMarkersGroup();
      }

      @Override
      public void onEntityAdded(
         @Nonnull Ref<EntityStore> reference, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         Archetype<EntityStore> archetype = store.getArchetype(reference);
         SpawnSuppressionComponent suppressor = store.getComponent(reference, this.spawnSuppressorComponentType);
         TransformComponent transform = commandBuffer.getComponent(reference, this.transformComponentType);
         UUIDComponent uuidComponent = commandBuffer.getComponent(reference, this.uuidComponentType);
         boolean fromExternal = archetype.contains(FromPrefab.getComponentType()) || archetype.contains(FromWorldGen.getComponentType());
         if (reason == AddReason.SPAWN || fromExternal) {
            SpawnSuppressionController suppressionController = store.getResource(this.spawnSuppressionControllerResourceType);
            SpawnSuppressorEntry entry = new SpawnSuppressorEntry(suppressor.getSpawnSuppression(), transform.getPosition().clone());
            UUID uuid = uuidComponent.getUuid();
            SpawnSuppressorEntry prev = suppressionController.getSpawnSuppressorMap().put(uuid, entry);
            if (prev != null) {
               throw new IllegalStateException(String.format("A spawn suppressor with the ID %s is already registered.", uuid));
            } else {
               SpawnSuppressionSystems.suppressSpawns(
                  this.chunkSuppressionQueueResourceType,
                  this.spawnMarkerEntityComponentType,
                  uuid,
                  entry,
                  suppressionController,
                  store,
                  store.getExternalData().getWorld().getChunkStore()
               );
               commandBuffer.ensureComponent(reference, PrefabCopyableComponent.getComponentType());
            }
         }
      }

      @Override
      public void onEntityRemove(
         @Nonnull Ref<EntityStore> reference,
         @Nonnull RemoveReason reason,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         if (reason == RemoveReason.REMOVE) {
            SpawnSuppressionController suppressionController = store.getResource(this.spawnSuppressionControllerResourceType);
            UUIDComponent uuidComponent = commandBuffer.getComponent(reference, this.uuidComponentType);
            Map<UUID, SpawnSuppressorEntry> spawnSuppressorMap = suppressionController.getSpawnSuppressorMap();
            UUID uuid = uuidComponent.getUuid();
            SpawnSuppressorEntry entry = spawnSuppressorMap.remove(uuid);
            String suppressionId = entry.getSuppressionId();
            SpawnSuppression suppression = SpawnSuppression.getAssetMap().getAsset(suppressionId);
            if (suppression == null) {
               SpawningPlugin.get().getLogger().at(Level.WARNING).log("Spawn suppression config '%s' does not exist", suppressionId);
            } else {
               double radius = suppression.getRadius();
               Vector3d position = entry.getPosition();
               int minChunkX = MathUtil.floor(position.x - radius) >> 5;
               int minChunkZ = MathUtil.floor(position.z - radius) >> 5;
               int maxChunkX = MathUtil.floor(position.x + radius) >> 5;
               int maxChunkZ = MathUtil.floor(position.z + radius) >> 5;
               ChunkStore chunkComponentStore = store.getExternalData().getWorld().getChunkStore();
               Store<ChunkStore> chunkStore = chunkComponentStore.getStore();
               Long2ObjectConcurrentHashMap<ChunkSuppressionEntry> chunkSuppressionMap = suppressionController.getChunkSuppressionMap();

               for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
                  for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                     long chunkIndex = ChunkUtil.indexChunk(chunkX, chunkZ);
                     ChunkSuppressionEntry chunkEntry = null;
                     ChunkSuppressionEntry oldEntry = chunkSuppressionMap.get(chunkIndex);
                     if (oldEntry.containsOnly(uuid)) {
                        chunkSuppressionMap.remove(chunkIndex);
                     } else {
                        List<ChunkSuppressionEntry.SuppressionSpan> oldSpans = oldEntry.getSuppressionSpans();
                        ObjectArrayList<ChunkSuppressionEntry.SuppressionSpan> suppressedSpans = new ObjectArrayList();

                        for (ChunkSuppressionEntry.SuppressionSpan span : oldSpans) {
                           if (!span.getSuppressorId().equals(uuid)) {
                              suppressedSpans.add(span);
                           }
                        }

                        chunkEntry = new ChunkSuppressionEntry(suppressedSpans);
                        chunkSuppressionMap.put(chunkIndex, chunkEntry);
                     }

                     Ref<ChunkStore> chunkReference = chunkComponentStore.getChunkReference(chunkIndex);
                     if (chunkReference != null) {
                        ChunkSuppressionQueue chunkSuppressionQueue = chunkStore.getResource(this.chunkSuppressionQueueResourceType);
                        if (chunkEntry == null) {
                           chunkSuppressionQueue.queueForRemove(chunkReference);
                        } else {
                           chunkSuppressionQueue.queueForAdd(chunkReference, chunkEntry);
                        }

                        SpawningPlugin.get()
                           .getLogger()
                           .at(Level.FINEST)
                           .log("Queuing removal of suppression from chunk index %s, %s", chunkIndex, suppressionId);
                     }
                  }
               }

               if (suppression.isSuppressSpawnMarkers()) {
                  ObjectList<Ref<EntityStore>> results = SpatialResource.getThreadLocalReferenceList();
                  SpatialResource<Ref<EntityStore>, EntityStore> spatialResource = store.getResource(this.spawnMarkerSpatialResourceType);
                  spatialResource.getSpatialStructure().collect(position, radius, results);

                  for (int i = 0; i < results.size(); i++) {
                     Ref<EntityStore> markerRef = (Ref<EntityStore>)results.get(i);
                     SpawnMarkerEntity marker = commandBuffer.getComponent(markerRef, this.spawnMarkerEntityComponentType);
                     marker.releaseSuppression(uuid);
                     HytaleLogger.Api context = SpawningPlugin.get().getLogger().at(Level.FINEST);
                     if (context.isEnabled()) {
                        context.log("Releasing suppression of spawn marker %s", commandBuffer.getComponent(markerRef, this.uuidComponentType).getUuid());
                     }
                  }
               }
            }
         }
      }
   }
}
