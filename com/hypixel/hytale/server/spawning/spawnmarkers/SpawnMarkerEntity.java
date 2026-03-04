package com.hypixel.hytale.server.spawning.spawnmarkers;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.common.map.IWeightedMap;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.function.consumer.TriConsumer;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.group.EntityGroup;
import com.hypixel.hytale.server.core.entity.reference.InvalidatablePersistentRef;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.component.WorldGenId;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.flock.FlockPlugin;
import com.hypixel.hytale.server.flock.StoredFlock;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderInfo;
import com.hypixel.hytale.server.npc.components.SpawnMarkerReference;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.spawning.ISpawnableWithModel;
import com.hypixel.hytale.server.spawning.SpawnTestResult;
import com.hypixel.hytale.server.spawning.SpawningContext;
import com.hypixel.hytale.server.spawning.SpawningPlugin;
import com.hypixel.hytale.server.spawning.assets.spawnmarker.config.SpawnMarker;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SpawnMarkerEntity implements Component<EntityStore> {
   private static final double SPAWN_LOST_TIMEOUT = 35.0;
   @Nonnull
   private static final InvalidatablePersistentRef[] EMPTY_REFERENCES = new InvalidatablePersistentRef[0];
   public static final ArrayCodec<InvalidatablePersistentRef> NPC_REFERENCES_CODEC = new ArrayCodec<>(
      InvalidatablePersistentRef.CODEC, InvalidatablePersistentRef[]::new
   );
   @Nonnull
   public static final BuilderCodec<SpawnMarkerEntity> CODEC = BuilderCodec.builder(SpawnMarkerEntity.class, SpawnMarkerEntity::new)
      .addField(
         new KeyedCodec<>("SpawnMarker", Codec.STRING),
         (spawnMarkerEntity, s) -> spawnMarkerEntity.spawnMarkerId = s,
         spawnMarkerEntity -> spawnMarkerEntity.spawnMarkerId
      )
      .addField(
         new KeyedCodec<>("RespawnTime", Codec.DOUBLE),
         (spawnMarkerEntity, d) -> spawnMarkerEntity.respawnCounter = d,
         spawnMarkerEntity -> spawnMarkerEntity.respawnCounter
      )
      .addField(
         new KeyedCodec<>("SpawnCount", Codec.INTEGER),
         (spawnMarkerEntity, i) -> spawnMarkerEntity.spawnCount = i,
         spawnMarkerEntity -> spawnMarkerEntity.spawnCount
      )
      .addField(
         new KeyedCodec<>("GameTimeRespawn", Codec.DURATION),
         (spawnMarkerEntity, duration) -> spawnMarkerEntity.gameTimeRespawn = duration,
         spawnMarkerEntity -> spawnMarkerEntity.gameTimeRespawn
      )
      .addField(
         new KeyedCodec<>("SpawnAfter", Codec.INSTANT),
         (spawnMarkerEntity, instant) -> spawnMarkerEntity.spawnAfter = instant,
         spawnMarkerEntity -> spawnMarkerEntity.spawnAfter
      )
      .addField(
         new KeyedCodec<>("NPCReferences", NPC_REFERENCES_CODEC),
         (spawnMarkerEntity, array) -> spawnMarkerEntity.npcReferences = array,
         spawnMarkerEntity -> spawnMarkerEntity.npcReferences
      )
      .addField(
         new KeyedCodec<>("PersistedFlock", StoredFlock.CODEC),
         (spawnMarkerEntity, o) -> spawnMarkerEntity.storedFlock = o,
         spawnMarkerEntity -> spawnMarkerEntity.storedFlock
      )
      .addField(
         new KeyedCodec<>("SpawnPosition", Vector3d.CODEC),
         (spawnMarkerEntity, v) -> spawnMarkerEntity.spawnPosition.assign(v),
         spawnMarkerEntity -> spawnMarkerEntity.storedFlock == null ? null : spawnMarkerEntity.spawnPosition
      )
      .build();
   private static final int MAX_FAILED_SPAWNS = 5;
   private String spawnMarkerId;
   private SpawnMarker cachedMarker;
   private double respawnCounter;
   @Nullable
   private Duration gameTimeRespawn;
   @Nullable
   private Instant spawnAfter;
   private int spawnCount;
   @Nullable
   private Set<UUID> suppressedBy;
   private int failedSpawns;
   @Nullable
   private final SpawningContext context;
   private final Vector3d spawnPosition = new Vector3d();
   private InvalidatablePersistentRef[] npcReferences;
   @Nullable
   private StoredFlock storedFlock;
   @Nullable
   private List<Pair<Ref<EntityStore>, NPCEntity>> tempStorageList;
   private double timeToDeactivation;
   private boolean despawnStarted;
   private double spawnLostTimeoutCounter;

   public static ComponentType<EntityStore, SpawnMarkerEntity> getComponentType() {
      return SpawningPlugin.get().getSpawnMarkerComponentType();
   }

   public SpawnMarkerEntity() {
      this(new SpawningContext());
   }

   private SpawnMarkerEntity(@Nullable SpawningContext context) {
      this.context = context;
      this.npcReferences = EMPTY_REFERENCES;
   }

   public SpawnMarker getCachedMarker() {
      return this.cachedMarker;
   }

   public void setCachedMarker(@Nonnull SpawnMarker marker) {
      this.cachedMarker = marker;
   }

   public int getSpawnCount() {
      return this.spawnCount;
   }

   public void setSpawnCount(int spawnCount) {
      this.spawnCount = spawnCount;
   }

   public void setRespawnCounter(double respawnCounter) {
      this.respawnCounter = respawnCounter;
   }

   public void setSpawnAfter(@Nullable Instant spawnAfter) {
      this.spawnAfter = spawnAfter;
   }

   @Nullable
   public Instant getSpawnAfter() {
      return this.spawnAfter;
   }

   public void setGameTimeRespawn(@Nullable Duration gameTimeRespawn) {
      this.gameTimeRespawn = gameTimeRespawn;
   }

   @Nullable
   public Duration pollGameTimeRespawn() {
      Duration ret = this.gameTimeRespawn;
      this.gameTimeRespawn = null;
      return ret;
   }

   public boolean tickRespawnTimer(float dt) {
      return (this.respawnCounter -= dt) <= 0.0;
   }

   @Nullable
   public Set<UUID> getSuppressedBy() {
      return this.suppressedBy;
   }

   public void setStoredFlock(@Nonnull StoredFlock storedFlock) {
      this.storedFlock = storedFlock;
   }

   @Nullable
   public StoredFlock getStoredFlock() {
      return this.storedFlock;
   }

   public double getTimeToDeactivation() {
      return this.timeToDeactivation;
   }

   public void setTimeToDeactivation(double timeToDeactivation) {
      this.timeToDeactivation = timeToDeactivation;
   }

   public boolean tickTimeToDeactivation(float dt) {
      return (this.timeToDeactivation -= dt) <= 0.0;
   }

   public boolean tickSpawnLostTimeout(float dt) {
      return (this.spawnLostTimeoutCounter -= dt) <= 0.0;
   }

   @Nonnull
   public Vector3d getSpawnPosition() {
      return this.spawnPosition;
   }

   public InvalidatablePersistentRef[] getNpcReferences() {
      return this.npcReferences;
   }

   public void setNpcReferences(@Nullable InvalidatablePersistentRef[] npcReferences) {
      this.npcReferences = npcReferences != null ? npcReferences : EMPTY_REFERENCES;
   }

   @Nullable
   public List<Pair<Ref<EntityStore>, NPCEntity>> getTempStorageList() {
      return this.tempStorageList;
   }

   public void setTempStorageList(@Nonnull List<Pair<Ref<EntityStore>, NPCEntity>> tempStorageList) {
      this.tempStorageList = tempStorageList;
   }

   public boolean isDespawnStarted() {
      return this.despawnStarted;
   }

   public void setDespawnStarted(boolean despawnStarted) {
      this.despawnStarted = despawnStarted;
   }

   public void refreshTimeout() {
      this.spawnLostTimeoutCounter = 35.0;
   }

   public boolean spawnNPC(@Nonnull Ref<EntityStore> ref, @Nonnull SpawnMarker marker, @Nonnull Store<EntityStore> store) {
      IWeightedMap<SpawnMarker.SpawnConfiguration> configs = marker.getWeightedConfigurations();
      if (configs == null) {
         SpawningPlugin.get().getLogger().at(Level.SEVERE).log("Marker %s has no spawn configurations to spawn", ref);
         this.refreshTimeout();
         return false;
      } else {
         SpawnMarker.SpawnConfiguration spawn = configs.get(ThreadLocalRandom.current());
         if (spawn == null) {
            SpawningPlugin.get().getLogger().at(Level.SEVERE).log("Marker %s has no spawn configuration to spawn", ref);
            this.refreshTimeout();
            return false;
         } else {
            boolean realtime = marker.isRealtimeRespawn();
            if (realtime) {
               this.respawnCounter = spawn.getRealtimeRespawnTime();
            } else {
               this.spawnAfter = null;
               this.gameTimeRespawn = spawn.getSpawnAfterGameTime();
            }

            UUIDComponent uuidComponent = store.getComponent(ref, UUIDComponent.getComponentType());

            assert uuidComponent != null;

            UUID uuid = uuidComponent.getUuid();
            String roleName = spawn.getNpc();
            if (roleName != null && !roleName.isEmpty()) {
               NPCPlugin npcModule = NPCPlugin.get();
               int roleIndex = npcModule.getIndex(roleName);
               TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());

               assert transformComponent != null;

               Vector3d position = transformComponent.getPosition();
               BuilderInfo builderInfo = npcModule.getRoleBuilderInfo(roleIndex);
               if (builderInfo == null) {
                  SpawningPlugin.get().getLogger().at(Level.SEVERE).log("Marker %s attempted to spawn non-existent NPC role '%s'", uuid, roleName);
                  this.fail(ref, uuid, roleName, position, store, SpawnMarkerEntity.FailReason.NONEXISTENT_ROLE);
                  return false;
               } else {
                  Builder<?> role = builderInfo.isValid() ? builderInfo.getBuilder() : null;
                  if (role == null) {
                     SpawningPlugin.get().getLogger().at(Level.SEVERE).log("Marker %s attempted to spawn invalid NPC role '%s'", uuid, roleName);
                     this.fail(ref, uuid, roleName, position, store, SpawnMarkerEntity.FailReason.INVALID_ROLE);
                     return false;
                  } else if (!role.isSpawnable()) {
                     SpawningPlugin.get().getLogger().at(Level.SEVERE).log("Marker %s attempted to spawn a non-spawnable (abstract) role '%s'", uuid, roleName);
                     this.fail(ref, uuid, roleName, position, store, SpawnMarkerEntity.FailReason.INVALID_ROLE);
                     return false;
                  } else if (!this.context.setSpawnable((ISpawnableWithModel)role)) {
                     SpawningPlugin.get()
                        .getLogger()
                        .at(Level.SEVERE)
                        .log("Marker %s failed to spawn NPC role '%s' due to failed role validation", uuid, roleName);
                     this.fail(ref, uuid, roleName, position, store, SpawnMarkerEntity.FailReason.FAILED_ROLE_VALIDATION);
                     return false;
                  } else {
                     ObjectList<Ref<EntityStore>> results = SpatialResource.getThreadLocalReferenceList();
                     SpatialResource<Ref<EntityStore>, EntityStore> spatialResource = store.getResource(EntityModule.get().getPlayerSpatialResourceType());
                     spatialResource.getSpatialStructure().collect(position, marker.getExclusionRadius(), results);
                     boolean hasPlayersInRange = !results.isEmpty();
                     if (hasPlayersInRange) {
                        this.refreshTimeout();
                        return false;
                     } else {
                        World world = store.getExternalData().getWorld();
                        if (!this.context.set(world, position.x, position.y, position.z)) {
                           SpawningPlugin.get()
                              .getLogger()
                              .at(Level.FINE)
                              .log("Marker %s attempted to spawn NPC '%s' at %s but could not fit", uuid, roleName, position);
                           this.fail(ref, uuid, roleName, position, store, SpawnMarkerEntity.FailReason.NO_ROOM);
                           return false;
                        } else {
                           SpawnTestResult testResult = this.context.canSpawn(true, false);
                           if (testResult != SpawnTestResult.TEST_OK) {
                              SpawningPlugin.get()
                                 .getLogger()
                                 .at(Level.FINE)
                                 .log("Marker %s attempted to spawn NPC '%s' at %s but could not fit: %s", uuid, roleName, position, testResult);
                              this.fail(ref, uuid, roleName, position, store, SpawnMarkerEntity.FailReason.NO_ROOM);
                              return false;
                           } else {
                              this.spawnPosition.assign(this.context.xSpawn, this.context.ySpawn, this.context.zSpawn);
                              if (this.spawnPosition.distanceSquaredTo(position) > marker.getMaxDropHeightSquared()) {
                                 SpawningPlugin.get()
                                    .getLogger()
                                    .at(Level.FINE)
                                    .log("Marker %s attempted to spawn NPC '%s' but was offset too far from the ground at %s", uuid, roleName, position);
                                 this.fail(ref, uuid, roleName, position, store, SpawnMarkerEntity.FailReason.TOO_HIGH);
                                 return false;
                              } else {
                                 TriConsumer<NPCEntity, Ref<EntityStore>, Store<EntityStore>> postSpawn = (_entity, _ref, _store) -> {
                                    SpawnMarkerReference spawnMarkerReference = _store.ensureAndGetComponent(_ref, SpawnMarkerReference.getComponentType());
                                    spawnMarkerReference.getReference().setEntity(ref, _store);
                                    spawnMarkerReference.refreshTimeoutCounter();
                                    WorldGenId worldGenIdComponent = _store.getComponent(ref, WorldGenId.getComponentType());
                                    int worldGenId = worldGenIdComponent != null ? worldGenIdComponent.getWorldGenId() : 0;
                                    _store.putComponent(_ref, WorldGenId.getComponentType(), new WorldGenId(worldGenId));
                                 };
                                 Vector3f rotation = transformComponent.getRotation();
                                 Pair<Ref<EntityStore>, NPCEntity> npcPair = npcModule.spawnEntity(
                                    store, roleIndex, this.spawnPosition, rotation, null, postSpawn
                                 );
                                 if (npcPair == null) {
                                    SpawningPlugin.get()
                                       .getLogger()
                                       .at(Level.SEVERE)
                                       .log("Marker %s failed to spawn NPC role '%s' due to an internal error", uuid, roleName);
                                    this.fail(ref, uuid, roleName, position, store, SpawnMarkerEntity.FailReason.INVALID_ROLE);
                                    return false;
                                 } else {
                                    Ref<EntityStore> npcRef = (Ref<EntityStore>)npcPair.first();
                                    NPCEntity npcComponent = (NPCEntity)npcPair.second();
                                    Ref<EntityStore> flockReference = FlockPlugin.trySpawnFlock(
                                       npcRef, npcComponent, store, roleIndex, this.spawnPosition, rotation, spawn.getFlockDefinition(), postSpawn
                                    );
                                    EntityGroup group = flockReference == null ? null : store.getComponent(flockReference, EntityGroup.getComponentType());
                                    this.spawnCount = group != null ? group.size() : 1;
                                    if (this.storedFlock != null) {
                                       this.despawnStarted = false;
                                       this.npcReferences = new InvalidatablePersistentRef[this.spawnCount];
                                       if (group != null) {
                                          group.forEachMember((index, member, referenceArray) -> {
                                             InvalidatablePersistentRef referencex = new InvalidatablePersistentRef();
                                             referencex.setEntity(member, store);
                                             referenceArray[index] = referencex;
                                          }, this.npcReferences);
                                       } else {
                                          InvalidatablePersistentRef reference = new InvalidatablePersistentRef();
                                          reference.setEntity(npcRef, store);
                                          this.npcReferences[0] = reference;
                                       }

                                       this.storedFlock.clear();
                                    }

                                    SpawningPlugin.get()
                                       .getLogger()
                                       .at(Level.FINE)
                                       .log(
                                          "Marker %s spawned %s and set respawn to %s",
                                          uuid,
                                          npcComponent.getRoleName(),
                                          realtime ? this.respawnCounter : this.gameTimeRespawn
                                       );
                                    this.refreshTimeout();
                                    return true;
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            } else {
               SpawningPlugin.get()
                  .getLogger()
                  .at(Level.FINE)
                  .log("Marker %s performed noop spawn and set repawn to %s", uuid, realtime ? this.respawnCounter : this.gameTimeRespawn);
               this.refreshTimeout();
               return true;
            }
         }
      }
   }

   private void fail(
      @Nonnull Ref<EntityStore> self,
      @Nonnull UUID uuid,
      @Nonnull String role,
      @Nonnull Vector3d position,
      @Nonnull Store<EntityStore> store,
      @Nonnull SpawnMarkerEntity.FailReason reason
   ) {
      if (++this.failedSpawns >= 5) {
         SpawningPlugin.get()
            .getLogger()
            .at(Level.WARNING)
            .log("Marker %s at %s removed due to repeated spawning fails of %s with reason: %s", uuid, position, role, reason);
         store.removeEntity(self, RemoveReason.REMOVE);
      } else {
         this.refreshTimeout();
      }
   }

   public void setSpawnMarker(@Nonnull SpawnMarker marker) {
      this.spawnMarkerId = marker.getId();
      this.cachedMarker = marker;
      if (this.cachedMarker.getDeactivationDistance() > 0.0) {
         this.storedFlock = new StoredFlock();
         this.tempStorageList = new ObjectArrayList();
      } else {
         this.storedFlock = null;
         this.tempStorageList = null;
      }
   }

   public int decrementAndGetSpawnCount() {
      return --this.spawnCount;
   }

   public String getSpawnMarkerId() {
      return this.spawnMarkerId;
   }

   public boolean isManualTrigger() {
      return this.cachedMarker.isManualTrigger();
   }

   public boolean trigger(@Nonnull Ref<EntityStore> markerRef, @Nonnull Store<EntityStore> store) {
      return this.cachedMarker.isManualTrigger() && this.spawnCount <= 0 ? this.spawnNPC(markerRef, this.cachedMarker, store) : false;
   }

   public void suppress(@Nonnull UUID suppressor) {
      if (this.suppressedBy == null) {
         this.suppressedBy = new HashSet<>();
      }

      this.suppressedBy.add(suppressor);
   }

   public void releaseSuppression(@Nonnull UUID suppressor) {
      if (this.suppressedBy != null) {
         this.suppressedBy.remove(suppressor);
      }
   }

   public void clearAllSuppressions() {
      if (this.suppressedBy != null) {
         this.suppressedBy.clear();
      }
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      SpawnMarkerEntity spawnMarker = new SpawnMarkerEntity();
      spawnMarker.spawnMarkerId = this.spawnMarkerId;
      spawnMarker.cachedMarker = this.cachedMarker;
      spawnMarker.respawnCounter = this.respawnCounter;
      spawnMarker.gameTimeRespawn = this.gameTimeRespawn;
      spawnMarker.spawnAfter = this.spawnAfter;
      spawnMarker.spawnCount = this.spawnCount;
      spawnMarker.suppressedBy = this.suppressedBy != null ? new HashSet<>(this.suppressedBy) : null;
      spawnMarker.failedSpawns = this.failedSpawns;
      spawnMarker.spawnPosition.assign(this.spawnPosition);
      spawnMarker.npcReferences = this.npcReferences;
      spawnMarker.storedFlock = this.storedFlock != null ? this.storedFlock.clone() : null;
      spawnMarker.timeToDeactivation = this.timeToDeactivation;
      spawnMarker.despawnStarted = this.despawnStarted;
      spawnMarker.spawnLostTimeoutCounter = this.spawnLostTimeoutCounter;
      return spawnMarker;
   }

   @Nullable
   @Override
   public Component<EntityStore> cloneSerializable() {
      SpawnMarkerEntity spawnMarker = new SpawnMarkerEntity(null);
      spawnMarker.spawnMarkerId = this.spawnMarkerId;
      spawnMarker.respawnCounter = this.respawnCounter;
      spawnMarker.spawnCount = this.spawnCount;
      spawnMarker.gameTimeRespawn = this.gameTimeRespawn;
      spawnMarker.spawnAfter = this.spawnAfter;
      spawnMarker.npcReferences = this.npcReferences;
      spawnMarker.storedFlock = this.storedFlock != null ? this.storedFlock.cloneSerializable() : null;
      spawnMarker.spawnPosition.assign(this.spawnPosition);
      return spawnMarker;
   }

   @Nonnull
   @Override
   public String toString() {
      return "SpawnMarkerEntity{spawnMarkerId='"
         + this.spawnMarkerId
         + "', cachedMarker="
         + this.cachedMarker
         + ", respawnCounter="
         + this.respawnCounter
         + ", gameTimeRespawn="
         + this.gameTimeRespawn
         + ", spawnAfter="
         + this.spawnAfter
         + ", spawnCount="
         + this.spawnCount
         + ", spawnLostTimeoutCounter="
         + this.spawnLostTimeoutCounter
         + ", failedSpawns="
         + this.failedSpawns
         + ", context="
         + this.context
         + ", spawnPosition="
         + this.spawnPosition
         + ", storedFlock="
         + this.storedFlock
         + "} "
         + super.toString();
   }

   public static Model getModel(@Nonnull SpawnMarker marker) {
      String modelName = marker.getModel();
      ModelAsset modelAsset = null;
      if (modelName != null && !modelName.isEmpty()) {
         modelAsset = ModelAsset.getAssetMap().getAsset(modelName);
      }

      Model model;
      if (modelAsset == null) {
         model = SpawningPlugin.get().getSpawnMarkerModel();
      } else {
         model = Model.createUnitScaleModel(modelAsset);
      }

      return model;
   }

   private static enum FailReason {
      INVALID_ROLE,
      NONEXISTENT_ROLE,
      FAILED_ROLE_VALIDATION,
      NO_ROOM,
      TOO_HIGH;
   }
}
