package com.hypixel.hytale.server.spawning.spawnmarkers;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.data.unknown.UnknownComponents;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.OrderPriority;
import com.hypixel.hytale.component.dependency.RootDependency;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.component.system.HolderSystem;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.AnimationSlot;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.entity.reference.InvalidatablePersistentRef;
import com.hypixel.hytale.server.core.entity.reference.PersistentRefCount;
import com.hypixel.hytale.server.core.modules.entity.AllLegacyEntityTypesQuery;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.FromPrefab;
import com.hypixel.hytale.server.core.modules.entity.component.FromWorldGen;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.HiddenFromAdventurePlayers;
import com.hypixel.hytale.server.core.modules.entity.component.Intangible;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.PersistentModel;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.component.WorldGenId;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.system.PlayerSpatialSystem;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.prefab.PrefabCopyableComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.flock.StoredFlock;
import com.hypixel.hytale.server.npc.components.SpawnMarkerReference;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.spawning.assets.spawnmarker.config.SpawnMarker;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bson.BsonDocument;

public class SpawnMarkerSystems {
   @Nonnull
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

   public static class AddedFromWorldGen extends HolderSystem<EntityStore> {
      @Nonnull
      private final ComponentType<EntityStore, SpawnMarkerEntity> componentType = SpawnMarkerEntity.getComponentType();
      @Nonnull
      private final ComponentType<EntityStore, WorldGenId> worldGenIdComponentType = WorldGenId.getComponentType();
      @Nonnull
      private final ComponentType<EntityStore, FromWorldGen> fromWorldGenComponentType = FromWorldGen.getComponentType();
      @Nonnull
      private final Query<EntityStore> query = Query.and(this.componentType, this.fromWorldGenComponentType);

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
      public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
         FromWorldGen fromWorldGenComponent = holder.getComponent(this.fromWorldGenComponentType);

         assert fromWorldGenComponent != null;

         holder.putComponent(this.worldGenIdComponentType, new WorldGenId(fromWorldGenComponent.getWorldGenId()));
      }

      @Override
      public void onEntityRemoved(@Nonnull Holder<EntityStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store) {
      }
   }

   public static class CacheMarker extends RefSystem<EntityStore> {
      private final ComponentType<EntityStore, SpawnMarkerEntity> spawnMarkerComponentType;

      public CacheMarker(@Nonnull ComponentType<EntityStore, SpawnMarkerEntity> spawnMarkerComponentType) {
         this.spawnMarkerComponentType = spawnMarkerComponentType;
      }

      @Override
      public Query<EntityStore> getQuery() {
         return this.spawnMarkerComponentType;
      }

      @Override
      public void onEntityAdded(
         @Nonnull Ref<EntityStore> ref, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         SpawnMarkerEntity spawnMarkerEntityComponent = store.getComponent(ref, this.spawnMarkerComponentType);

         assert spawnMarkerEntityComponent != null;

         SpawnMarker spawnMarker = SpawnMarker.getAssetMap().getAsset(spawnMarkerEntityComponent.getSpawnMarkerId());
         if (spawnMarker == null) {
            SpawnMarkerSystems.LOGGER
               .at(Level.SEVERE)
               .log("Marker %s removed due to missing spawn marker type: %s", ref, spawnMarkerEntityComponent.getSpawnMarkerId());
            commandBuffer.removeEntity(ref, RemoveReason.REMOVE);
         } else {
            spawnMarkerEntityComponent.setCachedMarker(spawnMarker);
         }
      }

      @Override
      public void onEntityRemove(
         @Nonnull Ref<EntityStore> ref, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
      }
   }

   public static class EnsureNetworkSendable extends HolderSystem<EntityStore> {
      @Nonnull
      private final Query<EntityStore> query = SpawnMarkerEntity.getComponentType();

      @Override
      public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
         Archetype<EntityStore> archetype = holder.getArchetype();

         assert archetype != null;

         ComponentType<EntityStore, NetworkId> networkIdComponentType = NetworkId.getComponentType();
         if (!archetype.contains(networkIdComponentType)) {
            holder.addComponent(networkIdComponentType, new NetworkId(store.getExternalData().takeNextNetworkId()));
         }

         holder.ensureComponent(Intangible.getComponentType());
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
      private final ComponentType<EntityStore, SpawnMarkerEntity> spawnMarkerEntityComponentType;
      @Nonnull
      private final ComponentType<EntityStore, UUIDComponent> uuidComponentType;
      @Nonnull
      private final Set<Dependency<EntityStore>> dependencies;
      @Nonnull
      private final Query<EntityStore> query;

      public EntityAdded(@Nonnull ComponentType<EntityStore, SpawnMarkerEntity> spawnMarkerEntityComponentType) {
         this.spawnMarkerEntityComponentType = spawnMarkerEntityComponentType;
         this.uuidComponentType = UUIDComponent.getComponentType();
         this.dependencies = Set.of(new SystemDependency<>(Order.AFTER, SpawnMarkerSystems.CacheMarker.class));
         this.query = Query.and(spawnMarkerEntityComponentType, this.uuidComponentType);
      }

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return this.query;
      }

      @Nonnull
      @Override
      public Set<Dependency<EntityStore>> getDependencies() {
         return this.dependencies;
      }

      @Override
      public void onEntityAdded(
         @Nonnull Ref<EntityStore> ref, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         SpawnMarkerEntity spawnMarkerEntityComponent = store.getComponent(ref, this.spawnMarkerEntityComponentType);

         assert spawnMarkerEntityComponent != null;

         HytaleLogger.Api context = SpawnMarkerSystems.LOGGER.at(Level.FINE);
         if (context.isEnabled()) {
            context.log("Loaded marker %s", store.getComponent(ref, this.uuidComponentType));
         }

         if (spawnMarkerEntityComponent.getStoredFlock() != null) {
            spawnMarkerEntityComponent.setTempStorageList(new ObjectArrayList());
         }

         if (spawnMarkerEntityComponent.getSpawnCount() != 0) {
            spawnMarkerEntityComponent.refreshTimeout();
         }

         commandBuffer.ensureComponent(ref, PrefabCopyableComponent.getComponentType());
      }

      @Override
      public void onEntityRemove(
         @Nonnull Ref<EntityStore> ref, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
      }
   }

   public static class EntityAddedFromExternal extends RefSystem<EntityStore> {
      @Nonnull
      private final Query<EntityStore> query;
      @Nonnull
      private final ComponentType<EntityStore, SpawnMarkerEntity> spawnMarkerEntityComponentType;
      @Nonnull
      private final Set<Dependency<EntityStore>> dependencies;

      public EntityAddedFromExternal(@Nonnull ComponentType<EntityStore, SpawnMarkerEntity> spawnMarkerEntityComponentType) {
         this.query = Query.and(spawnMarkerEntityComponentType, Query.or(FromPrefab.getComponentType(), FromWorldGen.getComponentType()));
         this.spawnMarkerEntityComponentType = spawnMarkerEntityComponentType;
         this.dependencies = Set.of(
            new SystemDependency<>(Order.BEFORE, SpawnMarkerSystems.EntityAdded.class),
            new SystemDependency<>(Order.AFTER, SpawnMarkerSystems.CacheMarker.class)
         );
      }

      @Override
      public void onEntityAdded(
         @Nonnull Ref<EntityStore> ref, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         SpawnMarkerEntity spawnMarkerEntityComponent = store.getComponent(ref, this.spawnMarkerEntityComponentType);

         assert spawnMarkerEntityComponent != null;

         spawnMarkerEntityComponent.setSpawnCount(0);
         spawnMarkerEntityComponent.setRespawnCounter(0.0);
         spawnMarkerEntityComponent.setSpawnAfter(null);
         spawnMarkerEntityComponent.setGameTimeRespawn(null);
         if (spawnMarkerEntityComponent.getCachedMarker().getDeactivationDistance() > 0.0) {
            spawnMarkerEntityComponent.setStoredFlock(new StoredFlock());
         }
      }

      @Override
      public void onEntityRemove(
         @Nonnull Ref<EntityStore> ref, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
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

      @Nullable
      @Override
      public SystemGroup<EntityStore> getGroup() {
         return EntityModule.get().getPreClearMarkersGroup();
      }
   }

   @Deprecated(forRemoval = true)
   public static class LegacyEntityMigration extends EntityModule.MigrationSystem {
      @Nonnull
      private final ComponentType<EntityStore, PersistentModel> persistentModelComponentType = PersistentModel.getComponentType();
      @Nonnull
      private final ComponentType<EntityStore, Nameplate> nameplateComponentType = Nameplate.getComponentType();
      @Nonnull
      private final ComponentType<EntityStore, UUIDComponent> uuidComponentType = UUIDComponent.getComponentType();
      @Nonnull
      private final ComponentType<EntityStore, UnknownComponents<EntityStore>> unknownComponentsComponentType = EntityStore.REGISTRY.getUnknownComponentType();
      @Nonnull
      private final Query<EntityStore> query = Query.and(this.unknownComponentsComponentType, Query.not(AllLegacyEntityTypesQuery.INSTANCE));

      @Override
      public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
         UnknownComponents<EntityStore> unknownComponentsComponent = holder.getComponent(this.unknownComponentsComponentType);

         assert unknownComponentsComponent != null;

         Map<String, BsonDocument> unknownComponents = unknownComponentsComponent.getUnknownComponents();
         BsonDocument spawnMarker = unknownComponents.remove("SpawnMarker");
         if (spawnMarker != null) {
            Archetype<EntityStore> archetype = holder.getArchetype();

            assert archetype != null;

            if (!archetype.contains(this.persistentModelComponentType)) {
               Model.ModelReference modelReference = Entity.MODEL.get(spawnMarker).get();
               holder.addComponent(this.persistentModelComponentType, new PersistentModel(modelReference));
            }

            if (!archetype.contains(this.nameplateComponentType)) {
               holder.addComponent(this.nameplateComponentType, new Nameplate(Entity.DISPLAY_NAME.get(spawnMarker).get()));
            }

            if (!archetype.contains(this.uuidComponentType)) {
               holder.addComponent(this.uuidComponentType, new UUIDComponent(Entity.UUID.get(spawnMarker).get()));
            }

            holder.ensureComponent(HiddenFromAdventurePlayers.getComponentType());
            int worldGenId = Codec.INTEGER.decode(spawnMarker.get("WorldgenId"));
            if (worldGenId != 0) {
               holder.addComponent(WorldGenId.getComponentType(), new WorldGenId(worldGenId));
            }

            SpawnMarkerEntity marker = SpawnMarkerEntity.CODEC.decode(spawnMarker, new ExtraInfo(5));
            holder.addComponent(SpawnMarkerEntity.getComponentType(), marker);
         }
      }

      @Override
      public void onEntityRemoved(@Nonnull Holder<EntityStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store) {
      }

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return this.query;
      }

      @Nonnull
      @Override
      public Set<Dependency<EntityStore>> getDependencies() {
         return RootDependency.firstSet();
      }
   }

   public static class Ticking extends EntityTickingSystem<EntityStore> {
      @Nonnull
      private final ComponentType<EntityStore, SpawnMarkerEntity> spawnMarkerEntityComponentType;
      @Nullable
      private final ComponentType<EntityStore, NPCEntity> npcComponentType;
      @Nonnull
      private final ComponentType<EntityStore, PersistentRefCount> referenceIdComponentType;
      @Nonnull
      private final ComponentType<EntityStore, TransformComponent> transformComponentType = TransformComponent.getComponentType();
      @Nonnull
      private final ComponentType<EntityStore, HeadRotation> headRotationComponentType = HeadRotation.getComponentType();
      @Nonnull
      private final ComponentType<EntityStore, ModelComponent> modelComponentType = ModelComponent.getComponentType();
      @Nonnull
      private final ResourceType<EntityStore, SpatialResource<Ref<EntityStore>, EntityStore>> playerSpatialComponent;
      @Nonnull
      private final Set<Dependency<EntityStore>> dependencies;
      @Nonnull
      private final Query<EntityStore> query;

      public Ticking(
         @Nonnull ComponentType<EntityStore, SpawnMarkerEntity> spawnMarkerEntityComponentType,
         @Nonnull ResourceType<EntityStore, SpatialResource<Ref<EntityStore>, EntityStore>> playerSpatialComponent
      ) {
         this.spawnMarkerEntityComponentType = spawnMarkerEntityComponentType;
         this.npcComponentType = NPCEntity.getComponentType();
         this.referenceIdComponentType = PersistentRefCount.getComponentType();
         this.playerSpatialComponent = playerSpatialComponent;
         this.dependencies = Set.of(new SystemDependency<>(Order.AFTER, PlayerSpatialSystem.class, OrderPriority.CLOSEST));
         this.query = Archetype.of(spawnMarkerEntityComponentType, this.transformComponentType);
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
         SpawnMarkerEntity spawnMarkerEntityComponent = archetypeChunk.getComponent(index, this.spawnMarkerEntityComponentType);

         assert spawnMarkerEntityComponent != null;

         TransformComponent transformComponent = archetypeChunk.getComponent(index, this.transformComponentType);

         assert transformComponent != null;

         World world = store.getExternalData().getWorld();
         SpawnMarker cachedMarker = spawnMarkerEntityComponent.getCachedMarker();
         if (spawnMarkerEntityComponent.getSpawnCount() > 0) {
            StoredFlock storedFlock = spawnMarkerEntityComponent.getStoredFlock();
            if (storedFlock != null) {
               SpatialResource<Ref<EntityStore>, EntityStore> spatialResource = store.getResource(this.playerSpatialComponent);
               ObjectList<Ref<EntityStore>> results = SpatialResource.getThreadLocalReferenceList();
               spatialResource.getSpatialStructure().collect(transformComponent.getPosition(), cachedMarker.getDeactivationDistance(), results);
               boolean hasPlayersInRange = !results.isEmpty();
               if (!hasPlayersInRange) {
                  if (!storedFlock.hasStoredNPCs() && spawnMarkerEntityComponent.tickTimeToDeactivation(dt)) {
                     InvalidatablePersistentRef[] npcReferences = spawnMarkerEntityComponent.getNpcReferences();
                     if (npcReferences == null) {
                        return;
                     }

                     if (!spawnMarkerEntityComponent.isDespawnStarted()) {
                        List<Pair<Ref<EntityStore>, NPCEntity>> tempStorageList = spawnMarkerEntityComponent.getTempStorageList();

                        for (InvalidatablePersistentRef reference : npcReferences) {
                           Ref<EntityStore> npcRef = reference.getEntity(commandBuffer);
                           if (npcRef != null) {
                              NPCEntity npcComponent = commandBuffer.getComponent(npcRef, this.npcComponentType);

                              assert npcComponent != null;

                              tempStorageList.add(Pair.of(npcRef, npcComponent));
                              boolean isDead = commandBuffer.getArchetype(npcRef).contains(DeathComponent.getComponentType());
                              if (isDead || npcComponent.getRole().getStateSupport().isInBusyState()) {
                                 spawnMarkerEntityComponent.setTimeToDeactivation(cachedMarker.getDeactivationTime());
                                 tempStorageList.clear();
                                 return;
                              }
                           }
                        }

                        for (int i = 0; i < tempStorageList.size(); i++) {
                           Pair<Ref<EntityStore>, NPCEntity> npcPair = tempStorageList.get(i);
                           Ref<EntityStore> npcRef = (Ref<EntityStore>)npcPair.first();
                           NPCEntity npcComponentx = (NPCEntity)npcPair.second();
                           ModelComponent modelComponent = commandBuffer.getComponent(npcRef, this.modelComponentType);
                           if (modelComponent != null && modelComponent.getModel().getAnimationSetMap().containsKey("Despawn")) {
                              Role role = npcComponentx.getRole();

                              assert role != null;

                              double despawnAnimationTime = role.getDespawnAnimationTime();
                              if (despawnAnimationTime > spawnMarkerEntityComponent.getTimeToDeactivation()) {
                                 spawnMarkerEntityComponent.setTimeToDeactivation(despawnAnimationTime);
                              }

                              npcComponentx.playAnimation(npcRef, AnimationSlot.Status, "Despawn", commandBuffer);
                           }
                        }

                        spawnMarkerEntityComponent.setDespawnStarted(true);
                        tempStorageList.clear();
                        return;
                     }

                     PersistentRefCount refId = archetypeChunk.getComponent(index, this.referenceIdComponentType);
                     if (refId != null) {
                        refId.increment();
                     }

                     Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
                     commandBuffer.run(
                        _store -> {
                           ObjectList<Ref<EntityStore>> tempStorageList = SpatialResource.getThreadLocalReferenceList();

                           for (InvalidatablePersistentRef referencex : npcReferences) {
                              Ref<EntityStore> npcRef = referencex.getEntity(_store);
                              if (npcRef != null && npcRef.isValid()) {
                                 SpawnMarkerReference spawnMarkerReference = _store.ensureAndGetComponent(npcRef, SpawnMarkerReference.getComponentType());
                                 spawnMarkerReference.getReference().setEntity(ref, store);
                                 tempStorageList.add(npcRef);
                              } else {
                                 ((HytaleLogger.Api)SpawnMarkerSystems.LOGGER.atWarning())
                                    .log("Connection with NPC from marker at %s lost due to being invalid/already unloaded", transformComponent.getPosition());
                              }
                           }

                           storedFlock.storeNPCs(tempStorageList, _store);
                           spawnMarkerEntityComponent.setNpcReferences(null);
                        }
                     );
                  }

                  return;
               }

               if (storedFlock.hasStoredNPCs()) {
                  commandBuffer.run(_store -> {
                     ObjectList<Ref<EntityStore>> tempStorageList = SpatialResource.getThreadLocalReferenceList();
                     storedFlock.restoreNPCs(tempStorageList, _store);
                     spawnMarkerEntityComponent.setSpawnCount(tempStorageList.size());
                     Vector3d position = spawnMarkerEntityComponent.getSpawnPosition();
                     Vector3f rotation = transformComponent.getRotation();
                     InvalidatablePersistentRef[] npcReferencesx = new InvalidatablePersistentRef[tempStorageList.size()];
                     int ix = 0;

                     for (int bound = tempStorageList.size(); ix < bound; ix++) {
                        Ref<EntityStore> refx = (Ref<EntityStore>)tempStorageList.get(ix);
                        NPCEntity npcComponentx = _store.getComponent(refx, this.npcComponentType);

                        assert npcComponentx != null;

                        TransformComponent npcTransform = _store.getComponent(refx, this.transformComponentType);

                        assert npcTransform != null;

                        HeadRotation npcHeadRotation = _store.getComponent(refx, this.headRotationComponentType);

                        assert npcHeadRotation != null;

                        InvalidatablePersistentRef referencex = new InvalidatablePersistentRef();
                        referencex.setEntity(refx, _store);
                        npcReferencesx[ix] = referencex;
                        npcTransform.getPosition().assign(position);
                        npcTransform.getRotation().assign(rotation);
                        npcHeadRotation.setRotation(rotation);
                        npcComponentx.playAnimation(refx, AnimationSlot.Status, null, commandBuffer);
                     }

                     spawnMarkerEntityComponent.setNpcReferences(npcReferencesx);
                     spawnMarkerEntityComponent.setDespawnStarted(false);
                     spawnMarkerEntityComponent.setTimeToDeactivation(cachedMarker.getDeactivationTime());
                  });
               }
            }

            if (spawnMarkerEntityComponent.tickSpawnLostTimeout(dt)) {
               PersistentRefCount refId = archetypeChunk.getComponent(index, this.referenceIdComponentType);
               if (refId != null) {
                  refId.increment();
                  SpawnMarkerSystems.LOGGER.at(Level.FINE).log("Marker lost spawned NPC and changed reference ID to %s", refId.get());
               }

               Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
               commandBuffer.run(_store -> spawnMarkerEntityComponent.spawnNPC(ref, cachedMarker, _store));
            }
         } else if (world.getWorldConfig().isSpawnMarkersEnabled()
            && !cachedMarker.isManualTrigger()
            && (spawnMarkerEntityComponent.getSuppressedBy() == null || spawnMarkerEntityComponent.getSuppressedBy().isEmpty())) {
            Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
            WorldTimeResource worldTimeResource = commandBuffer.getResource(WorldTimeResource.getResourceType());
            if (cachedMarker.isRealtimeRespawn()) {
               if (spawnMarkerEntityComponent.tickRespawnTimer(dt)) {
                  commandBuffer.run(_store -> spawnMarkerEntityComponent.spawnNPC(ref, cachedMarker, _store));
               }
            } else if (spawnMarkerEntityComponent.getSpawnAfter() == null
               || worldTimeResource.getGameTime().isAfter(spawnMarkerEntityComponent.getSpawnAfter())) {
               commandBuffer.run(_store -> spawnMarkerEntityComponent.spawnNPC(ref, cachedMarker, _store));
            }
         }
      }
   }
}
