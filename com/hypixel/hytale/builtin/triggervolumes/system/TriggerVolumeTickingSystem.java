package com.hypixel.hytale.builtin.triggervolumes.system;

import com.hypixel.hytale.builtin.triggervolumes.EntityTargetType;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerCondition;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEffect;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEventType;
import com.hypixel.hytale.builtin.triggervolumes.event.TriggerVolumeEvent;
import com.hypixel.hytale.builtin.triggervolumes.manager.ConditionTiming;
import com.hypixel.hytale.builtin.triggervolumes.manager.GroupEntry;
import com.hypixel.hytale.builtin.triggervolumes.manager.RejectionDelayMode;
import com.hypixel.hytale.builtin.triggervolumes.manager.TriggerVolumeManager;
import com.hypixel.hytale.builtin.triggervolumes.manager.VolumeEntry;
import com.hypixel.hytale.builtin.triggervolumes.shape.TriggerVolumeShape;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.OrderPriority;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.spatial.SpatialData;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.component.system.tick.TickingSystem;
import com.hypixel.hytale.event.IEventDispatcher;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.ProjectileComponent;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.modules.entity.system.PlayerSpatialSystem;
import com.hypixel.hytale.server.core.modules.projectile.component.Projectile;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.ChunkFlag;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3d;

public class TriggerVolumeTickingSystem extends TickingSystem<EntityStore> {
   @Nonnull
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   private static final double DEFAULT_ENTITY_HEIGHT = 1.8;
   private static final int MAX_PENDING_EVENTS_PER_TICK = 64;
   @Nonnull
   private static final ThreadLocal<Set<UUID>> THREAD_LOCAL_PREVIOUS = ThreadLocal.withInitial(HashSet::new);
   @Nonnull
   private static final ThreadLocal<Map<UUID, Ref<EntityStore>>> THREAD_LOCAL_PREVIOUS_REFS = ThreadLocal.withInitial(HashMap::new);
   @Nonnull
   private static final ThreadLocal<Vector3d> THREAD_LOCAL_TEST_POINT = ThreadLocal.withInitial(Vector3d::new);
   @Nonnull
   private static final ThreadLocal<List<VolumeEntry>> THREAD_LOCAL_CANDIDATES = ThreadLocal.withInitial(ArrayList::new);
   @Nonnull
   private static final ThreadLocal<List<Ref<EntityStore>>> THREAD_LOCAL_ENTITY_REFS = ThreadLocal.withInitial(ArrayList::new);
   @Nonnull
   private final ResourceType<EntityStore, TriggerVolumeManager> managerResourceType;
   @Nonnull
   private final ResourceType<EntityStore, SpatialResource<Ref<EntityStore>, EntityStore>> playerSpatialResourceType;
   @Nonnull
   private final ResourceType<EntityStore, SpatialResource<Ref<EntityStore>, EntityStore>> entitySpatialResourceType;
   @Nonnull
   private final Set<Dependency<EntityStore>> dependencies;
   @Nonnull
   private final TriggerVolumeTickingSystem.EventDispatcher eventDispatcher;

   public TriggerVolumeTickingSystem(
      @Nonnull ResourceType<EntityStore, TriggerVolumeManager> managerResourceType,
      @Nonnull ResourceType<EntityStore, SpatialResource<Ref<EntityStore>, EntityStore>> playerSpatialResourceType,
      @Nonnull ResourceType<EntityStore, SpatialResource<Ref<EntityStore>, EntityStore>> entitySpatialResourceType
   ) {
      this(managerResourceType, playerSpatialResourceType, entitySpatialResourceType, TriggerVolumeTickingSystem::dispatchToServerEventBus);
   }

   TriggerVolumeTickingSystem(
      @Nonnull ResourceType<EntityStore, TriggerVolumeManager> managerResourceType,
      @Nonnull ResourceType<EntityStore, SpatialResource<Ref<EntityStore>, EntityStore>> playerSpatialResourceType,
      @Nonnull ResourceType<EntityStore, SpatialResource<Ref<EntityStore>, EntityStore>> entitySpatialResourceType,
      @Nonnull TriggerVolumeTickingSystem.EventDispatcher eventDispatcher
   ) {
      this.managerResourceType = managerResourceType;
      this.playerSpatialResourceType = playerSpatialResourceType;
      this.entitySpatialResourceType = entitySpatialResourceType;
      this.eventDispatcher = eventDispatcher;
      this.dependencies = Set.of(new SystemDependency<>(Order.AFTER, PlayerSpatialSystem.class, OrderPriority.CLOSEST));
   }

   @Nonnull
   @Override
   public Set<Dependency<EntityStore>> getDependencies() {
      return this.dependencies;
   }

   @Override
   public void tick(float deltaSeconds, int systemIndex, @Nonnull Store<EntityStore> store) {
      TriggerVolumeManager manager = store.getResource(this.managerResourceType);
      if (manager != null) {
         VolumeSpatialIndex spatialIndex = manager.getSpatialIndex();
         spatialIndex.rebuildIfDirty(manager.getVolumes());
         Set<VolumeEntry> toTick = Collections.newSetFromMap(new IdentityHashMap<>());
         List<VolumeEntry> candidates = THREAD_LOCAL_CANDIDATES.get();
         this.collectCandidatesFromEntityPositions(store, spatialIndex, toTick, candidates);
         World world = manager.getWorld();

         for (VolumeEntry entry : manager.getVolumes()) {
            if (!entry.isEnabled()) {
               if (!entry.getTrackedEntities().isEmpty()) {
                  this.processTrackedEntityExits(entry, store, manager);
               }
            } else {
               boolean chunkLoaded = entry.isKeepLoaded() || world == null || isChunkLoaded(world, entry.getPosition());
               if (!chunkLoaded) {
                  toTick.remove(entry);
                  if (!entry.getTrackedEntities().isEmpty()) {
                     this.processTrackedEntityExits(entry, store, manager);
                  }
               } else if (!entry.getTrackedEntities().isEmpty()) {
                  toTick.add(entry);
               }
            }
         }

         for (VolumeEntry entryx : toTick) {
            try {
               this.tickVolume(entryx, deltaSeconds, store, manager);
            } catch (Exception var12) {
               ((HytaleLogger.Api)LOGGER.at(Level.WARNING).withCause(var12)).log("Error ticking trigger volume '%s'", entryx.getId());
            }
         }

         DelayedEffectScheduler delayedEffectScheduler = manager.getDelayedEffectScheduler();
         if (!delayedEffectScheduler.isEmpty()) {
            delayedEffectScheduler.tick(System.nanoTime(), store);
         }

         this.processPendingEvents(manager, store);
         this.processPendingDestroys(manager);
      }
   }

   private void processPendingEvents(@Nonnull TriggerVolumeManager manager, @Nonnull Store<EntityStore> store) {
      int processed = 0;

      TriggerVolumeManager.PendingTriggerEvent event;
      while ((event = manager.pollPendingEvent()) != null) {
         if (++processed > 64) {
            LOGGER.at(Level.WARNING).log("Stopped trigger volume event cascade after %d events", processed - 1);
            break;
         }

         this.processPendingEvent(event, manager, store, System.nanoTime());
      }
   }

   private void processPendingEvent(
      @Nonnull TriggerVolumeManager.PendingTriggerEvent event, @Nonnull TriggerVolumeManager manager, @Nonnull Store<EntityStore> store, long nowNanos
   ) {
      if (event.actorRef().isValid()) {
         if (event.volumeId() != null) {
            VolumeEntry entry = manager.getVolume(event.volumeId());
            if (entry != null && entry.isEnabled()) {
               this.firePendingEvent(entry, event, manager, store, nowNanos);
            }
         } else if (event.blockPosition() != null) {
            VolumeSpatialIndex spatialIndex = manager.getSpatialIndex();
            spatialIndex.rebuildIfDirty(manager.getVolumes());
            List<VolumeEntry> candidates = THREAD_LOCAL_CANDIDATES.get();
            candidates.clear();
            spatialIndex.collectCandidates(event.blockPosition(), candidates);

            for (VolumeEntry entry : candidates) {
               if (entry.isEnabled() && entry.getShape().contains(entry.getPosition(), event.blockPosition())) {
                  this.firePendingEvent(entry, event, manager, store, nowNanos);
               }
            }
         }
      }
   }

   private void firePendingEvent(
      @Nonnull VolumeEntry entry,
      @Nonnull TriggerVolumeManager.PendingTriggerEvent event,
      @Nonnull TriggerVolumeManager manager,
      @Nonnull Store<EntityStore> store,
      long nowNanos
   ) {
      this.firePendingVolumeEvent(entry, event, store, nowNanos);
      this.firePendingGroupEvent(entry, event, manager, store, nowNanos);
   }

   private void firePendingVolumeEvent(
      @Nonnull VolumeEntry entry, @Nonnull TriggerVolumeManager.PendingTriggerEvent event, @Nonnull Store<EntityStore> store, long nowNanos
   ) {
      if (hasMatchingEventEffect(event.eventType(), entry.getEffects())
         || hasMatchingEventEffect(event.eventType(), entry.getRejectionEffects())
         || hasMatchingEventCondition(event.eventType(), entry.getConditions())) {
         if (!entry.isOnCooldown(event.actorUuid(), nowNanos)) {
            this.fireGatedEffects(
               event.eventType(),
               event.actorRef(),
               entry,
               store,
               nowNanos,
               event.actorUuid(),
               null,
               entry.getConditions(),
               entry.getEffects(),
               entry.getRejectionEffects(),
               entry.getConditionTiming() == ConditionTiming.BEFORE_VOLUME_DELAY ? entry.getActivationDelay() : 0.0F,
               rejectionDelay(entry.getRejectionDelayMode(), entry.getActivationDelay()),
               VolumeEntry.EffectBucket.VOLUME,
               VolumeEntry.EffectBucket.VOLUME_REJECTION,
               "volume '" + entry.getId() + "'",
               event
            );
         }
      }
   }

   private void firePendingGroupEvent(
      @Nonnull VolumeEntry entry,
      @Nonnull TriggerVolumeManager.PendingTriggerEvent event,
      @Nonnull TriggerVolumeManager manager,
      @Nonnull Store<EntityStore> store,
      long nowNanos
   ) {
      if (entry.getGroupId() != null) {
         GroupEntry group = manager.getGroup(entry.getGroupId());
         if (group != null && group.isEnabled()) {
            if (hasMatchingEventEffect(event.eventType(), group.getEffects())
               || hasMatchingEventEffect(event.eventType(), group.getRejectionEffects())
               || hasMatchingEventCondition(event.eventType(), group.getConditions())) {
               if (!entry.isOnCooldown(event.actorUuid(), nowNanos)) {
                  List<VolumeEntry> spatialVolumes = getGroupSpatialVolumes(entry, manager, group);
                  this.fireGatedEffects(
                     event.eventType(),
                     event.actorRef(),
                     entry,
                     store,
                     nowNanos,
                     event.actorUuid(),
                     spatialVolumes,
                     group.getConditions(),
                     group.getEffects(),
                     group.getRejectionEffects(),
                     group.getConditionTiming() == ConditionTiming.BEFORE_VOLUME_DELAY ? entry.getActivationDelay() : 0.0F,
                     rejectionDelay(group.getRejectionDelayMode(), entry.getActivationDelay()),
                     VolumeEntry.EffectBucket.GROUP,
                     VolumeEntry.EffectBucket.GROUP_REJECTION,
                     "group '" + group.getId() + "' via volume '" + entry.getId() + "'",
                     event
                  );
               }
            }
         }
      }
   }

   private void processPendingDestroys(@Nonnull TriggerVolumeManager manager) {
      for (VolumeEntry entry : manager.getVolumes()) {
         if (entry.isPendingDestroy()) {
            String volumeId = entry.getId();
            manager.getDelayedEffectScheduler().cancelForVolume(entry);
            manager.unregister(volumeId);
            manager.notifyViewersRemove(volumeId);
         }
      }
   }

   private void collectCandidatesFromEntityPositions(
      @Nonnull Store<EntityStore> store, @Nonnull VolumeSpatialIndex spatialIndex, @Nonnull Set<VolumeEntry> toTick, @Nonnull List<VolumeEntry> candidates
   ) {
      SpatialResource<Ref<EntityStore>, EntityStore> playerSpatial = store.getResource(this.playerSpatialResourceType);
      if (playerSpatial != null) {
         SpatialData<Ref<EntityStore>> data = playerSpatial.getSpatialData();

         for (int i = 0; i < data.size(); i++) {
            candidates.clear();
            spatialIndex.collectCandidates(data.getVector(i), candidates);
            toTick.addAll(candidates);
         }
      }

      SpatialResource<Ref<EntityStore>, EntityStore> entitySpatial = store.getResource(this.entitySpatialResourceType);
      if (entitySpatial != null) {
         SpatialData<Ref<EntityStore>> data = entitySpatial.getSpatialData();

         for (int i = 0; i < data.size(); i++) {
            candidates.clear();
            spatialIndex.collectCandidates(data.getVector(i), candidates);
            toTick.addAll(candidates);
         }
      }
   }

   private void tickVolume(@Nonnull VolumeEntry entry, float deltaSeconds, @Nonnull Store<EntityStore> store, @Nonnull TriggerVolumeManager manager) {
      TriggerVolumeShape shape = entry.getShape();
      Vector3d origin = entry.getPosition();
      Set<UUID> previousUuids = THREAD_LOCAL_PREVIOUS.get();
      Map<UUID, Ref<EntityStore>> previousRefs = THREAD_LOCAL_PREVIOUS_REFS.get();
      previousUuids.clear();
      previousRefs.clear();
      previousUuids.addAll(entry.getTrackedEntities().keySet());
      previousRefs.putAll(entry.getTrackedEntities());
      entry.getTrackedEntities().clear();
      List<Ref<EntityStore>> results = SpatialResource.getThreadLocalReferenceList();
      if (entry.getTargetTypes().contains(EntityTargetType.PLAYER)) {
         SpatialResource<Ref<EntityStore>, EntityStore> playerSpatial = store.getResource(this.playerSpatialResourceType);
         if (playerSpatial != null) {
            playerSpatial.getSpatialStructure().collect(origin, shape.getMaxDistanceFromOrigin(), results);
         }
      }

      if (usesEntitySpatial(entry.getTargetTypes())) {
         SpatialResource<Ref<EntityStore>, EntityStore> entitySpatial = store.getResource(this.entitySpatialResourceType);
         if (entitySpatial != null) {
            entitySpatial.getSpatialStructure().collect(origin, shape.getMaxDistanceFromOrigin(), results);
         }
      }

      List<Ref<EntityStore>> entitiesToProcess = THREAD_LOCAL_ENTITY_REFS.get();
      entitiesToProcess.clear();
      entitiesToProcess.addAll(results);
      long nowNanos = System.nanoTime();

      for (int i = 0; i < entitiesToProcess.size(); i++) {
         Ref<EntityStore> entityRef = entitiesToProcess.get(i);
         if (entityRef.isValid() && matchesTargetTypes(entry.getTargetTypes(), entityRef, store)) {
            TransformComponent transform = store.getComponent(entityRef, TransformComponent.getComponentType());
            if (transform != null && containsEntity(shape, origin, transform, store.getComponent(entityRef, BoundingBox.getComponentType()))) {
               UUIDComponent uuidComponent = store.getComponent(entityRef, UUIDComponent.getComponentType());
               if (uuidComponent != null) {
                  UUID uuid = uuidComponent.getUuid();
                  entry.getTrackedEntities().put(uuid, entityRef);
                  boolean wasInside = previousUuids.remove(uuid);
                  if (!wasInside) {
                     if (entry.isOnCooldown(uuid, nowNanos)) {
                        continue;
                     }

                     this.clearIntervalTimers(entry, uuid);
                     if (!hasTickActivationGate(entry.getConditions())) {
                        this.activateVolumeEntry(entityRef, entry, store, nowNanos, uuid);
                     }

                     if (!hasGroupTickActivationGate(entry, manager)) {
                        this.activateGroupEntry(entityRef, entry, manager, store, nowNanos, uuid);
                     }
                  }

                  if (entry.isVolumeActivated(uuid)) {
                     this.fireEffects(TriggerEventType.TICK, entityRef, entry, store, nowNanos, uuid);
                  } else if (hasTickActivationGate(entry.getConditions())) {
                     this.processVolumeTickActivationGate(entityRef, entry, store, nowNanos, uuid);
                  }

                  if (entry.getGroupId() != null && entry.isGroupActivated(entry.getGroupId(), uuid)) {
                     this.fireGroupEffects(TriggerEventType.TICK, entityRef, entry, manager, store, nowNanos, uuid);
                  } else {
                     this.processGroupTickActivationGate(entityRef, entry, manager, store, nowNanos, uuid);
                  }
               }
            }
         }
      }

      for (UUID exitedUuid : previousUuids) {
         Ref<EntityStore> exitedRef = previousRefs.get(exitedUuid);
         if (exitedRef != null && exitedRef.isValid()) {
            this.dispatchEvent(TriggerEventType.EXIT, entry, exitedRef, exitedUuid);
            this.fireEffects(TriggerEventType.EXIT, exitedRef, entry, store, nowNanos, exitedUuid);
            if (entry.getGroupId() != null) {
               this.fireGroupEffects(TriggerEventType.EXIT, exitedRef, entry, manager, store, nowNanos, exitedUuid);
            }
         }

         notifyVolumeEntityExit(entry, exitedUuid);
         this.fireGroupOnEntityExit(entry, manager, exitedUuid);
         if (entry.isCancelDelayedEffectsOnExit()) {
            manager.getDelayedEffectScheduler().cancelNonExitForEntityInVolume(exitedUuid, entry);
         }

         entry.clearEntityRuntimeState(exitedUuid);
      }
   }

   private boolean activateVolumeEntry(
      @Nonnull Ref<EntityStore> entityRef, @Nonnull VolumeEntry entry, @Nonnull Store<EntityStore> store, long nowNanos, @Nonnull UUID entityUuid
   ) {
      TriggerVolumeTickingSystem.ActivationResult result = this.fireEffects(TriggerEventType.ENTER, entityRef, entry, store, nowNanos, entityUuid);
      if (result != TriggerVolumeTickingSystem.ActivationResult.ACCEPTED) {
         return false;
      } else {
         this.completeVolumeActivation(entityRef, entry, nowNanos, entityUuid);
         return true;
      }
   }

   private void completeVolumeActivation(@Nonnull Ref<EntityStore> entityRef, @Nonnull VolumeEntry entry, long nowNanos, @Nonnull UUID entityUuid) {
      if (entry.getTrackedEntities().containsKey(entityUuid)) {
         entry.markVolumeActivated(entityUuid);
         entry.recordActivation(entityUuid, nowNanos);
         this.dispatchEvent(TriggerEventType.ENTER, entry, entityRef, entityUuid);
      }
   }

   private void processVolumeTickActivationGate(
      @Nonnull Ref<EntityStore> entityRef, @Nonnull VolumeEntry entry, @Nonnull Store<EntityStore> store, long nowNanos, @Nonnull UUID entityUuid
   ) {
      if (entry.isOnCooldown(entityUuid, nowNanos)) {
         entry.markVolumeActivated(entityUuid);
      } else {
         TriggerContext context = new TriggerContext(entityRef, store, TriggerEventType.TICK, entry);
         if (this.conditionsPass(entry.getConditions(), TriggerEventType.TICK, context, "volume '" + entry.getId() + "'")) {
            this.activateVolumeEntry(entityRef, entry, store, nowNanos, entityUuid);
         } else if (entry.markVolumeTickRejectionFired(entityUuid)) {
            this.fireEffectList(
               TriggerEventType.TICK,
               entityRef,
               entry,
               store,
               nowNanos,
               entityUuid,
               null,
               entry.getRejectionEffects(),
               0.0F,
               VolumeEntry.EffectBucket.VOLUME_REJECTION,
               "volume '" + entry.getId() + "'"
            );
         }
      }
   }

   private boolean activateGroupEntry(
      @Nonnull Ref<EntityStore> entityRef,
      @Nonnull VolumeEntry entry,
      @Nonnull TriggerVolumeManager manager,
      @Nonnull Store<EntityStore> store,
      long nowNanos,
      @Nonnull UUID entityUuid
   ) {
      String groupId = entry.getGroupId();
      if (groupId == null) {
         return false;
      } else {
         TriggerVolumeTickingSystem.ActivationResult result = this.fireGroupEffects(
            TriggerEventType.ENTER, entityRef, entry, manager, store, nowNanos, entityUuid
         );
         if (result != TriggerVolumeTickingSystem.ActivationResult.ACCEPTED) {
            return false;
         } else {
            this.completeGroupActivation(entry, groupId, entityUuid);
            return true;
         }
      }
   }

   private void completeGroupActivation(@Nonnull VolumeEntry entry, @Nonnull String groupId, @Nonnull UUID entityUuid) {
      if (entry.getTrackedEntities().containsKey(entityUuid)) {
         entry.markGroupActivated(groupId, entityUuid);
      }
   }

   private void processGroupTickActivationGate(
      @Nonnull Ref<EntityStore> entityRef,
      @Nonnull VolumeEntry entry,
      @Nonnull TriggerVolumeManager manager,
      @Nonnull Store<EntityStore> store,
      long nowNanos,
      @Nonnull UUID entityUuid
   ) {
      String groupId = entry.getGroupId();
      if (groupId != null) {
         GroupEntry group = manager.getGroup(groupId);
         if (group != null && group.isEnabled() && hasTickActivationGate(group.getConditions())) {
            if (entry.isOnCooldown(entityUuid, nowNanos)) {
               entry.markGroupActivated(groupId, entityUuid);
            } else {
               List<VolumeEntry> spatialVolumes = getGroupSpatialVolumes(entry, manager, group);
               if (!spatialVolumes.isEmpty()) {
                  TriggerContext context = new TriggerContext(entityRef, store, TriggerEventType.TICK, entry, spatialVolumes);
                  String sourceLabel = "group '" + group.getId() + "' via volume '" + entry.getId() + "'";
                  if (this.conditionsPass(group.getConditions(), TriggerEventType.TICK, context, sourceLabel)) {
                     this.activateGroupEntry(entityRef, entry, manager, store, nowNanos, entityUuid);
                  } else if (entry.markGroupTickRejectionFired(groupId, entityUuid)) {
                     this.fireEffectList(
                        TriggerEventType.TICK,
                        entityRef,
                        entry,
                        store,
                        nowNanos,
                        entityUuid,
                        spatialVolumes,
                        group.getRejectionEffects(),
                        0.0F,
                        VolumeEntry.EffectBucket.GROUP_REJECTION,
                        sourceLabel
                     );
                  }
               }
            }
         }
      }
   }

   @Nonnull
   private static List<VolumeEntry> getGroupSpatialVolumes(@Nonnull VolumeEntry entry, @Nonnull TriggerVolumeManager manager, @Nonnull GroupEntry group) {
      ArrayList<VolumeEntry> spatialVolumes = new ArrayList<>();

      for (String memberId : group.getMemberVolumeIds()) {
         VolumeEntry member = manager.getVolume(memberId);
         if (member != null) {
            spatialVolumes.add(member);
         }
      }

      if (spatialVolumes.isEmpty()) {
         spatialVolumes.add(entry);
      }

      return spatialVolumes;
   }

   private TriggerVolumeTickingSystem.ActivationResult fireEffects(
      @Nonnull TriggerEventType eventType,
      @Nonnull Ref<EntityStore> entityRef,
      @Nonnull VolumeEntry entry,
      @Nonnull Store<EntityStore> store,
      long nowNanos,
      @Nonnull UUID entityUuid
   ) {
      if (hasMatchingEventEffect(eventType, entry.getEffects()) || hasMatchingEventEffect(eventType, entry.getRejectionEffects())) {
         return entry.getConditionTiming() == ConditionTiming.AFTER_VOLUME_DELAY && entry.getActivationDelay() > 0.0F
            ? this.scheduleDelayedVolumeGate(eventType, entityRef, entry, store, nowNanos, entityUuid)
            : this.fireGatedEffects(
               eventType,
               entityRef,
               entry,
               store,
               nowNanos,
               entityUuid,
               null,
               entry.getConditions(),
               entry.getEffects(),
               entry.getRejectionEffects(),
               entry.getConditionTiming() == ConditionTiming.BEFORE_VOLUME_DELAY ? entry.getActivationDelay() : 0.0F,
               rejectionDelay(entry.getRejectionDelayMode(), entry.getActivationDelay()),
               VolumeEntry.EffectBucket.VOLUME,
               VolumeEntry.EffectBucket.VOLUME_REJECTION,
               "volume '" + entry.getId() + "'"
            );
      } else if (!hasMatchingEventCondition(eventType, entry.getConditions())) {
         return TriggerVolumeTickingSystem.ActivationResult.ACCEPTED;
      } else if (entry.getConditionTiming() == ConditionTiming.AFTER_VOLUME_DELAY && entry.getActivationDelay() > 0.0F) {
         return this.scheduleDelayedVolumeGate(eventType, entityRef, entry, store, nowNanos, entityUuid);
      } else {
         TriggerContext context = new TriggerContext(entityRef, store, eventType, entry);
         return this.conditionsPass(entry.getConditions(), eventType, context, "volume '" + entry.getId() + "'")
            ? TriggerVolumeTickingSystem.ActivationResult.ACCEPTED
            : TriggerVolumeTickingSystem.ActivationResult.REJECTED;
      }
   }

   @Nonnull
   private TriggerVolumeTickingSystem.ActivationResult scheduleDelayedVolumeGate(
      @Nonnull TriggerEventType eventType,
      @Nonnull Ref<EntityStore> entityRef,
      @Nonnull VolumeEntry entry,
      @Nonnull Store<EntityStore> store,
      long nowNanos,
      @Nonnull UUID entityUuid
   ) {
      if (eventType == TriggerEventType.ENTER && !entry.markVolumeActivationPending(entityUuid)) {
         return TriggerVolumeTickingSystem.ActivationResult.PENDING;
      } else {
         this.getScheduler(store)
            .scheduleGate(this::fireDelayedVolumeEffects, entityRef, entityUuid, eventType, entry, nowNanos, entry.getActivationDelay(), null);
         return TriggerVolumeTickingSystem.ActivationResult.PENDING;
      }
   }

   private void fireDelayedVolumeEffects(
      @Nonnull Ref<EntityStore> entityRef,
      @Nonnull UUID entityUuid,
      @Nonnull TriggerEventType eventType,
      @Nonnull VolumeEntry entry,
      @Nullable List<VolumeEntry> spatialVolumes,
      @Nonnull Store<EntityStore> store,
      long nowNanos
   ) {
      try {
         TriggerVolumeTickingSystem.ActivationResult result = this.fireGatedEffects(
            eventType,
            entityRef,
            entry,
            store,
            nowNanos,
            entityUuid,
            null,
            entry.getConditions(),
            entry.getEffects(),
            entry.getRejectionEffects(),
            0.0F,
            rejectionDelay(entry.getRejectionDelayMode(), entry.getActivationDelay()),
            VolumeEntry.EffectBucket.VOLUME,
            VolumeEntry.EffectBucket.VOLUME_REJECTION,
            "volume '" + entry.getId() + "'"
         );
         if (eventType == TriggerEventType.ENTER && result == TriggerVolumeTickingSystem.ActivationResult.ACCEPTED) {
            this.completeVolumeActivation(entityRef, entry, nowNanos, entityUuid);
         }
      } finally {
         if (eventType == TriggerEventType.ENTER) {
            entry.clearVolumeActivationPending(entityUuid);
         }
      }
   }

   private TriggerVolumeTickingSystem.ActivationResult fireGatedEffects(
      @Nonnull TriggerEventType eventType,
      @Nonnull Ref<EntityStore> entityRef,
      @Nonnull VolumeEntry entry,
      @Nonnull Store<EntityStore> store,
      long nowNanos,
      @Nonnull UUID entityUuid,
      @Nullable List<VolumeEntry> spatialVolumes,
      @Nonnull List<TriggerCondition> conditions,
      @Nonnull List<TriggerEffect> effects,
      @Nonnull List<TriggerEffect> rejectionEffects,
      float successDelay,
      float rejectionDelay,
      @Nonnull VolumeEntry.EffectBucket effectsBucket,
      @Nonnull VolumeEntry.EffectBucket rejectionBucket,
      @Nonnull String sourceLabel
   ) {
      return this.fireGatedEffects(
         eventType,
         entityRef,
         entry,
         store,
         nowNanos,
         entityUuid,
         spatialVolumes,
         conditions,
         effects,
         rejectionEffects,
         successDelay,
         rejectionDelay,
         effectsBucket,
         rejectionBucket,
         sourceLabel,
         null
      );
   }

   private TriggerVolumeTickingSystem.ActivationResult fireGatedEffects(
      @Nonnull TriggerEventType eventType,
      @Nonnull Ref<EntityStore> entityRef,
      @Nonnull VolumeEntry entry,
      @Nonnull Store<EntityStore> store,
      long nowNanos,
      @Nonnull UUID entityUuid,
      @Nullable List<VolumeEntry> spatialVolumes,
      @Nonnull List<TriggerCondition> conditions,
      @Nonnull List<TriggerEffect> effects,
      @Nonnull List<TriggerEffect> rejectionEffects,
      float successDelay,
      float rejectionDelay,
      @Nonnull VolumeEntry.EffectBucket effectsBucket,
      @Nonnull VolumeEntry.EffectBucket rejectionBucket,
      @Nonnull String sourceLabel,
      @Nullable TriggerVolumeManager.PendingTriggerEvent eventData
   ) {
      TriggerContext context = spatialVolumes != null
         ? createContext(entityRef, store, eventType, entry, spatialVolumes, eventData)
         : createContext(entityRef, store, eventType, entry, List.of(entry), eventData);
      boolean accepted = this.conditionsPass(conditions, eventType, context, sourceLabel);
      List<TriggerEffect> effectsToFire = accepted ? effects : rejectionEffects;
      VolumeEntry.EffectBucket bucket = accepted ? effectsBucket : rejectionBucket;
      float activationDelay = accepted ? successDelay : rejectionDelay;
      this.fireEffectList(eventType, entityRef, entry, store, nowNanos, entityUuid, spatialVolumes, effectsToFire, activationDelay, bucket, sourceLabel);
      return accepted ? TriggerVolumeTickingSystem.ActivationResult.ACCEPTED : TriggerVolumeTickingSystem.ActivationResult.REJECTED;
   }

   @Nonnull
   private static TriggerContext createContext(
      @Nonnull Ref<EntityStore> entityRef,
      @Nonnull Store<EntityStore> store,
      @Nonnull TriggerEventType eventType,
      @Nonnull VolumeEntry entry,
      @Nonnull List<VolumeEntry> spatialVolumes,
      @Nullable TriggerVolumeManager.PendingTriggerEvent eventData
   ) {
      return eventData == null
         ? new TriggerContext(entityRef, store, eventType, entry, spatialVolumes)
         : new TriggerContext(
            entityRef, store, eventType, entry, spatialVolumes, eventData.tagKey(), eventData.tagValue(), eventData.blockPosition(), eventData.blockId()
         );
   }

   private boolean conditionsPass(
      @Nonnull List<TriggerCondition> conditions, @Nonnull TriggerEventType eventType, @Nonnull TriggerContext context, @Nonnull String sourceLabel
   ) {
      ArrayList<TriggerCondition> acceptedConditions = new ArrayList<>();

      for (TriggerCondition condition : conditions) {
         if (condition.getEventType() == eventType) {
            try {
               if (!condition.test(context)) {
                  return false;
               }

               acceptedConditions.add(condition);
            } catch (Exception var10) {
               ((HytaleLogger.Api)LOGGER.at(Level.WARNING).withCause(var10))
                  .log("Error evaluating condition %s on %s", condition.getClass().getSimpleName(), sourceLabel);
               return false;
            }
         }
      }

      for (TriggerCondition conditionx : acceptedConditions) {
         try {
            conditionx.applyOnAccept(context);
         } catch (Exception var9) {
            ((HytaleLogger.Api)LOGGER.at(Level.WARNING).withCause(var9))
               .log("Error applying accepted condition %s on %s", conditionx.getClass().getSimpleName(), sourceLabel);
            return false;
         }
      }

      return true;
   }

   static boolean hasTickActivationGate(@Nonnull List<TriggerCondition> conditions) {
      for (TriggerCondition condition : conditions) {
         if (condition.getEventType() == TriggerEventType.TICK) {
            return true;
         }
      }

      return false;
   }

   static boolean hasMatchingEventEffect(@Nonnull TriggerEventType eventType, @Nonnull List<TriggerEffect> effects) {
      for (TriggerEffect effect : effects) {
         if (effect.getEventType() == eventType) {
            return true;
         }
      }

      return false;
   }

   static boolean hasMatchingEventCondition(@Nonnull TriggerEventType eventType, @Nonnull List<TriggerCondition> conditions) {
      for (TriggerCondition condition : conditions) {
         if (condition.getEventType() == eventType) {
            return true;
         }
      }

      return false;
   }

   private static boolean hasGroupTickActivationGate(@Nonnull VolumeEntry entry, @Nonnull TriggerVolumeManager manager) {
      String groupId = entry.getGroupId();
      if (groupId == null) {
         return false;
      } else {
         GroupEntry group = manager.getGroup(groupId);
         return group != null && group.isEnabled() && hasTickActivationGate(group.getConditions());
      }
   }

   private void fireEffectList(
      @Nonnull TriggerEventType eventType,
      @Nonnull Ref<EntityStore> entityRef,
      @Nonnull VolumeEntry entry,
      @Nonnull Store<EntityStore> store,
      long nowNanos,
      @Nonnull UUID entityUuid,
      @Nullable List<VolumeEntry> spatialVolumes,
      @Nonnull List<TriggerEffect> effects,
      float activationDelay,
      @Nonnull VolumeEntry.EffectBucket bucket,
      @Nonnull String sourceLabel
   ) {
      for (int i = 0; i < effects.size(); i++) {
         TriggerEffect effect = effects.get(i);
         if (effect.getEventType() == eventType) {
            VolumeEntry.EffectEntityKey intervalKey = null;
            if (eventType == TriggerEventType.TICK && effect.getInterval() > 0.0F) {
               intervalKey = new VolumeEntry.EffectEntityKey(bucket, i, entityUuid);
               Long lastFire = entry.getLastFireTimes().get(intervalKey);
               if (lastFire != null) {
                  double elapsedSeconds = (nowNanos - lastFire) / 1.0E9;
                  if (elapsedSeconds < effect.getInterval()) {
                     continue;
                  }
               }
            }

            float totalDelay = activationDelay + effectDelay(eventType, effect, intervalKey != null && entry.getLastFireTimes().containsKey(intervalKey));
            if (totalDelay > 0.0F) {
               DelayedEffectScheduler scheduler = this.getScheduler(store);
               if (intervalKey != null) {
                  VolumeEntry.EffectEntityKey pendingIntervalKey = intervalKey;
                  if (entry.markDelayedEffectPending(pendingIntervalKey)) {
                     scheduler.schedule(effect, entityRef, entityUuid, eventType, entry, nowNanos, totalDelay, spatialVolumes, (executed, executedAtNanos) -> {
                        entry.clearDelayedEffectPending(pendingIntervalKey);
                        if (executed) {
                           entry.getLastFireTimes().put(pendingIntervalKey, executedAtNanos);
                        }
                     });
                  }
               } else {
                  scheduler.schedule(effect, entityRef, entityUuid, eventType, entry, nowNanos, totalDelay, spatialVolumes);
               }
            } else {
               try {
                  Ref<EntityStore> actorRef = entry.getProjectileSource().resolveActorRef(entityRef, store);
                  TriggerContext context = spatialVolumes != null
                     ? new TriggerContext(actorRef, store, eventType, entry, spatialVolumes)
                     : new TriggerContext(actorRef, store, eventType, entry);
                  effect.execute(context);
                  if (intervalKey != null) {
                     entry.getLastFireTimes().put(intervalKey, nowNanos);
                  }
               } catch (Exception var19) {
                  ((HytaleLogger.Api)LOGGER.at(Level.WARNING).withCause(var19))
                     .log("Error executing effect %s on %s", effect.getClass().getSimpleName(), sourceLabel);
               }
            }
         }
      }
   }

   static float effectDelay(@Nonnull TriggerEventType eventType, @Nonnull TriggerEffect effect, boolean hasFiredBefore) {
      return eventType == TriggerEventType.TICK && hasFiredBefore ? 0.0F : effect.getDelay();
   }

   private TriggerVolumeTickingSystem.ActivationResult fireGroupEffects(
      @Nonnull TriggerEventType eventType,
      @Nonnull Ref<EntityStore> entityRef,
      @Nonnull VolumeEntry entry,
      @Nonnull TriggerVolumeManager manager,
      @Nonnull Store<EntityStore> store,
      long nowNanos,
      @Nonnull UUID entityUuid
   ) {
      if (entry.getGroupId() == null) {
         return TriggerVolumeTickingSystem.ActivationResult.REJECTED;
      } else {
         GroupEntry group = manager.getGroup(entry.getGroupId());
         if (group == null || !group.isEnabled()) {
            return TriggerVolumeTickingSystem.ActivationResult.REJECTED;
         } else if (group.getConditions().isEmpty() && group.getEffects().isEmpty() && group.getRejectionEffects().isEmpty()) {
            return TriggerVolumeTickingSystem.ActivationResult.ACCEPTED;
         } else if (hasMatchingEventEffect(eventType, group.getEffects()) || hasMatchingEventEffect(eventType, group.getRejectionEffects())) {
            List<VolumeEntry> spatialVolumes = getGroupSpatialVolumes(entry, manager, group);
            if (spatialVolumes.isEmpty()) {
               return TriggerVolumeTickingSystem.ActivationResult.REJECTED;
            } else {
               if (!group.getTargetTypes().isEmpty()) {
                  EntityTargetType required = resolveTargetType(entityRef, store);
                  if (required == null) {
                     return TriggerVolumeTickingSystem.ActivationResult.REJECTED;
                  }

                  if (!group.getTargetTypes().contains(required)) {
                     return TriggerVolumeTickingSystem.ActivationResult.REJECTED;
                  }
               }

               return group.getConditionTiming() == ConditionTiming.AFTER_VOLUME_DELAY && entry.getActivationDelay() > 0.0F
                  ? this.scheduleDelayedGroupGate(eventType, entityRef, entry, group, store, nowNanos, entityUuid, spatialVolumes)
                  : this.fireGroupEffectLists(
                     eventType,
                     entityRef,
                     entry,
                     group,
                     store,
                     nowNanos,
                     entityUuid,
                     spatialVolumes,
                     group.getConditionTiming() == ConditionTiming.BEFORE_VOLUME_DELAY ? entry.getActivationDelay() : 0.0F,
                     rejectionDelay(group.getRejectionDelayMode(), entry.getActivationDelay())
                  );
            }
         } else if (!hasMatchingEventCondition(eventType, group.getConditions())) {
            return TriggerVolumeTickingSystem.ActivationResult.ACCEPTED;
         } else if (group.getConditionTiming() == ConditionTiming.AFTER_VOLUME_DELAY && entry.getActivationDelay() > 0.0F) {
            return this.scheduleDelayedGroupGate(eventType, entityRef, entry, group, store, nowNanos, entityUuid, getGroupSpatialVolumes(entry, manager, group));
         } else {
            TriggerContext context = new TriggerContext(entityRef, store, eventType, entry, getGroupSpatialVolumes(entry, manager, group));
            return this.conditionsPass(group.getConditions(), eventType, context, "group '" + group.getId() + "' via volume '" + entry.getId() + "'")
               ? TriggerVolumeTickingSystem.ActivationResult.ACCEPTED
               : TriggerVolumeTickingSystem.ActivationResult.REJECTED;
         }
      }
   }

   @Nonnull
   private TriggerVolumeTickingSystem.ActivationResult scheduleDelayedGroupGate(
      @Nonnull TriggerEventType eventType,
      @Nonnull Ref<EntityStore> entityRef,
      @Nonnull VolumeEntry entry,
      @Nonnull GroupEntry group,
      @Nonnull Store<EntityStore> store,
      long nowNanos,
      @Nonnull UUID entityUuid,
      @Nonnull List<VolumeEntry> spatialVolumes
   ) {
      if (eventType == TriggerEventType.ENTER && !entry.markGroupActivationPending(group.getId(), entityUuid)) {
         return TriggerVolumeTickingSystem.ActivationResult.PENDING;
      } else {
         this.getScheduler(store)
            .scheduleGate(
               (delayedEntityRef, delayedEntityUuid, delayedEventType, delayedVolume, delayedVolumes, delayedStore, delayedNow) -> this.fireDelayedGroupEffects(
                  delayedEventType, delayedEntityRef, delayedVolume, group, delayedStore, delayedNow, delayedEntityUuid, delayedVolumes
               ),
               entityRef,
               entityUuid,
               eventType,
               entry,
               nowNanos,
               entry.getActivationDelay(),
               spatialVolumes
            );
         return TriggerVolumeTickingSystem.ActivationResult.PENDING;
      }
   }

   private void fireDelayedGroupEffects(
      @Nonnull TriggerEventType eventType,
      @Nonnull Ref<EntityStore> entityRef,
      @Nonnull VolumeEntry entry,
      @Nonnull GroupEntry group,
      @Nonnull Store<EntityStore> store,
      long nowNanos,
      @Nonnull UUID entityUuid,
      @Nullable List<VolumeEntry> spatialVolumes
   ) {
      try {
         if (!group.isEnabled()) {
            return;
         }

         TriggerVolumeTickingSystem.ActivationResult result = this.fireGroupEffectLists(
            eventType,
            entityRef,
            entry,
            group,
            store,
            nowNanos,
            entityUuid,
            spatialVolumes,
            0.0F,
            rejectionDelay(group.getRejectionDelayMode(), entry.getActivationDelay())
         );
         if (eventType == TriggerEventType.ENTER && result == TriggerVolumeTickingSystem.ActivationResult.ACCEPTED) {
            this.completeGroupActivation(entry, group.getId(), entityUuid);
         }
      } finally {
         if (eventType == TriggerEventType.ENTER) {
            entry.clearGroupActivationPending(group.getId(), entityUuid);
         }
      }
   }

   private TriggerVolumeTickingSystem.ActivationResult fireGroupEffectLists(
      @Nonnull TriggerEventType eventType,
      @Nonnull Ref<EntityStore> entityRef,
      @Nonnull VolumeEntry entry,
      @Nonnull GroupEntry group,
      @Nonnull Store<EntityStore> store,
      long nowNanos,
      @Nonnull UUID entityUuid,
      @Nullable List<VolumeEntry> spatialVolumes,
      float successDelay,
      float rejectionDelay
   ) {
      List<VolumeEntry> volumes = spatialVolumes != null ? spatialVolumes : List.of(entry);
      return this.fireGatedEffects(
         eventType,
         entityRef,
         entry,
         store,
         nowNanos,
         entityUuid,
         volumes,
         group.getConditions(),
         group.getEffects(),
         group.getRejectionEffects(),
         successDelay,
         rejectionDelay,
         VolumeEntry.EffectBucket.GROUP,
         VolumeEntry.EffectBucket.GROUP_REJECTION,
         "group '" + group.getId() + "' via volume '" + entry.getId() + "'"
      );
   }

   private static float rejectionDelay(@Nonnull RejectionDelayMode delayMode, float activationDelay) {
      return delayMode == RejectionDelayMode.USE_VOLUME_DELAY ? activationDelay : 0.0F;
   }

   @Nonnull
   private DelayedEffectScheduler getScheduler(@Nonnull Store<EntityStore> store) {
      TriggerVolumeManager manager = store.getResource(this.managerResourceType);
      if (manager == null) {
         throw new IllegalStateException("TriggerVolumeManager missing on store");
      } else {
         return manager.getDelayedEffectScheduler();
      }
   }

   private static boolean usesEntitySpatial(@Nonnull Set<EntityTargetType> targetTypes) {
      return targetTypes.contains(EntityTargetType.NPC)
         || targetTypes.contains(EntityTargetType.ITEM_DROP)
         || targetTypes.contains(EntityTargetType.PROJECTILE);
   }

   private static boolean matchesTargetTypes(@Nonnull Set<EntityTargetType> targetTypes, @Nonnull Ref<EntityStore> entityRef, @Nonnull Store<EntityStore> store) {
      EntityTargetType targetType = resolveTargetType(entityRef, store);
      return targetType != null && targetTypes.contains(targetType);
   }

   @Nullable
   private static EntityTargetType resolveTargetType(@Nonnull Ref<EntityStore> entityRef, @Nonnull Store<EntityStore> store) {
      if (store.getComponent(entityRef, PlayerRef.getComponentType()) != null) {
         return EntityTargetType.PLAYER;
      } else if (store.getComponent(entityRef, ItemComponent.getComponentType()) != null) {
         return EntityTargetType.ITEM_DROP;
      } else {
         return store.getComponent(entityRef, Projectile.getComponentType()) == null
               && store.getComponent(entityRef, ProjectileComponent.getComponentType()) == null
            ? EntityTargetType.NPC
            : EntityTargetType.PROJECTILE;
      }
   }

   private void fireGroupOnEntityExit(@Nonnull VolumeEntry entry, @Nonnull TriggerVolumeManager manager, @Nonnull UUID entityUuid) {
      if (entry.getGroupId() != null) {
         GroupEntry group = manager.getGroup(entry.getGroupId());
         if (group != null) {
            for (TriggerCondition condition : group.getConditions()) {
               condition.onEntityExit(entityUuid);
            }

            for (TriggerEffect effect : group.getEffects()) {
               effect.onEntityExit(entityUuid);
            }

            for (TriggerEffect effect : group.getRejectionEffects()) {
               effect.onEntityExit(entityUuid);
            }
         }
      }
   }

   private static void notifyVolumeEntityExit(@Nonnull VolumeEntry entry, @Nonnull UUID entityUuid) {
      for (TriggerCondition condition : entry.getConditions()) {
         condition.onEntityExit(entityUuid);
      }

      for (TriggerEffect effect : entry.getEffects()) {
         effect.onEntityExit(entityUuid);
      }

      for (TriggerEffect effect : entry.getRejectionEffects()) {
         effect.onEntityExit(entityUuid);
      }
   }

   private void dispatchEvent(@Nonnull TriggerEventType eventType, @Nonnull VolumeEntry entry, @Nonnull Ref<EntityStore> entityRef, @Nonnull UUID entityUuid) {
      this.eventDispatcher.dispatch(eventType, entry, entityRef, entityUuid);
   }

   private static void dispatchToServerEventBus(
      @Nonnull TriggerEventType eventType, @Nonnull VolumeEntry entry, @Nonnull Ref<EntityStore> entityRef, @Nonnull UUID entityUuid
   ) {
      HytaleServer server = HytaleServer.get();
      if (server != null) {
         IEventDispatcher<TriggerVolumeEvent, TriggerVolumeEvent> dispatcher = server.getEventBus().dispatchFor(TriggerVolumeEvent.class, entry.getWorldName());
         if (dispatcher.hasListener()) {
            dispatcher.dispatch(new TriggerVolumeEvent(entry.getWorldName(), eventType, entry, entityRef, entityUuid));
         }
      }
   }

   private void clearIntervalTimers(@Nonnull VolumeEntry entry, @Nonnull UUID entityUuid) {
      entry.getLastFireTimes().entrySet().removeIf(lastFireEntry -> lastFireEntry.getKey().entityId().equals(entityUuid));
   }

   private void processTrackedEntityExits(@Nonnull VolumeEntry entry, @Nonnull Store<EntityStore> store, @Nonnull TriggerVolumeManager manager) {
      long nowNanos = System.nanoTime();

      for (Entry<UUID, Ref<EntityStore>> tracked : entry.getTrackedEntities().entrySet()) {
         UUID uuid = tracked.getKey();
         Ref<EntityStore> entityRef = tracked.getValue();
         if (entityRef != null && entityRef.isValid()) {
            this.dispatchEvent(TriggerEventType.EXIT, entry, entityRef, uuid);
            this.fireEffects(TriggerEventType.EXIT, entityRef, entry, store, nowNanos, uuid);
            if (entry.getGroupId() != null) {
               this.fireGroupEffects(TriggerEventType.EXIT, entityRef, entry, manager, store, nowNanos, uuid);
            }
         }

         notifyVolumeEntityExit(entry, uuid);
         this.fireGroupOnEntityExit(entry, manager, uuid);
         entry.clearEntityRuntimeState(uuid);
      }

      entry.getTrackedEntities().clear();
   }

   private static boolean isChunkLoaded(@Nonnull World world, @Nonnull Vector3d position) {
      long idx = ChunkUtil.indexChunkFromBlock(position.x(), position.z());
      ChunkStore chunkStore = world.getChunkStore();
      Ref<ChunkStore> chunkRef = chunkStore.getChunkReference(idx);
      if (chunkRef != null && chunkRef.isValid()) {
         WorldChunk worldChunkComponent = chunkStore.getStore().getComponent(chunkRef, WorldChunk.getComponentType());
         return worldChunkComponent != null && worldChunkComponent.is(ChunkFlag.TICKING);
      } else {
         return false;
      }
   }

   private static boolean containsEntity(
      @Nonnull TriggerVolumeShape shape, @Nonnull Vector3d origin, @Nonnull TransformComponent transform, @Nullable BoundingBox boundingBox
   ) {
      Vector3d pos = transform.getPosition();
      if (shape.contains(origin, pos)) {
         return true;
      } else {
         double entityHeight = boundingBox != null ? boundingBox.getBoundingBox().height() : 1.8;
         if (entityHeight <= 0.0) {
            return false;
         } else {
            Vector3d testPoint = THREAD_LOCAL_TEST_POINT.get();
            testPoint.set(pos.x(), pos.y() + entityHeight * 0.5, pos.z());
            if (shape.contains(origin, testPoint)) {
               return true;
            } else {
               testPoint.set(pos.x(), pos.y() + entityHeight, pos.z());
               return shape.contains(origin, testPoint);
            }
         }
      }
   }

   private static enum ActivationResult {
      ACCEPTED,
      REJECTED,
      PENDING;
   }

   @FunctionalInterface
   interface EventDispatcher {
      void dispatch(@Nonnull TriggerEventType var1, @Nonnull VolumeEntry var2, @Nonnull Ref<EntityStore> var3, @Nonnull UUID var4);
   }
}
