package com.hypixel.hytale.server.core.modules.entity.tracker;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.dependency.SystemGroupDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.component.spatial.SpatialStructure;
import com.hypixel.hytale.component.system.HolderSystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.ComponentUpdate;
import com.hypixel.hytale.protocol.ComponentUpdateType;
import com.hypixel.hytale.protocol.EntityEffectsUpdate;
import com.hypixel.hytale.protocol.packets.entities.EntityUpdates;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.system.NetworkSendableSpatialSystem;
import com.hypixel.hytale.server.core.receiver.IPacketReceiver;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.StampedLock;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EntityTrackerSystems {
   @Nonnull
   public static final SystemGroup<EntityStore> FIND_VISIBLE_ENTITIES_GROUP = EntityStore.REGISTRY.registerSystemGroup();
   @Nonnull
   public static final SystemGroup<EntityStore> QUEUE_UPDATE_GROUP = EntityStore.REGISTRY.registerSystemGroup();

   public static boolean despawnAll(@Nonnull Ref<EntityStore> viewerRef, @Nonnull Store<EntityStore> store) {
      if (!viewerRef.isValid()) {
         return false;
      } else {
         EntityTrackerSystems.EntityViewer entityViewerComponent = store.getComponent(viewerRef, EntityTrackerSystems.EntityViewer.getComponentType());
         if (entityViewerComponent == null) {
            return false;
         } else {
            int networkId = entityViewerComponent.sent.removeInt(viewerRef);
            EntityUpdates packet = new EntityUpdates();
            packet.removed = entityViewerComponent.sent.values().toIntArray();
            entityViewerComponent.packetReceiver.writeNoCache(packet);
            clear(viewerRef, store);
            entityViewerComponent.sent.put(viewerRef, networkId);
            return true;
         }
      }
   }

   public static boolean clear(@Nonnull Ref<EntityStore> viewerRef, @Nonnull Store<EntityStore> store) {
      if (!viewerRef.isValid()) {
         return false;
      } else {
         EntityTrackerSystems.EntityViewer entityViewerComponent = store.getComponent(viewerRef, EntityTrackerSystems.EntityViewer.getComponentType());
         if (entityViewerComponent == null) {
            return false;
         } else {
            ObjectIterator var3 = entityViewerComponent.sent.keySet().iterator();

            while (var3.hasNext()) {
               Ref<EntityStore> ref = (Ref<EntityStore>)var3.next();
               if (ref != null && ref.isValid()) {
                  EntityTrackerSystems.Visible visibleComponent = store.getComponent(ref, EntityTrackerSystems.Visible.getComponentType());
                  if (visibleComponent != null) {
                     visibleComponent.visibleTo.remove(viewerRef);
                  }
               }
            }

            entityViewerComponent.sent.clear();
            return true;
         }
      }
   }

   public static class AddToVisible extends EntityTickingSystem<EntityStore> {
      @Nonnull
      public static final Set<Dependency<EntityStore>> DEPENDENCIES = Collections.singleton(
         new SystemDependency<>(Order.AFTER, EntityTrackerSystems.EnsureVisibleComponent.class)
      );
      @Nonnull
      private final ComponentType<EntityStore, EntityTrackerSystems.EntityViewer> entityViewerComponentType;
      @Nonnull
      private final ComponentType<EntityStore, EntityTrackerSystems.Visible> visibleComponentType;

      public AddToVisible(
         @Nonnull ComponentType<EntityStore, EntityTrackerSystems.EntityViewer> entityViewerComponentType,
         @Nonnull ComponentType<EntityStore, EntityTrackerSystems.Visible> visibleComponentType
      ) {
         this.entityViewerComponentType = entityViewerComponentType;
         this.visibleComponentType = visibleComponentType;
      }

      @Nonnull
      @Override
      public Set<Dependency<EntityStore>> getDependencies() {
         return DEPENDENCIES;
      }

      @Override
      public Query<EntityStore> getQuery() {
         return this.entityViewerComponentType;
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
         Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
         EntityTrackerSystems.EntityViewer entityViewerComponent = archetypeChunk.getComponent(index, this.entityViewerComponentType);

         assert entityViewerComponent != null;

         for (Ref<EntityStore> vislbleRef : entityViewerComponent.visible) {
            if (vislbleRef != null && vislbleRef.isValid()) {
               EntityTrackerSystems.Visible visibleComponent = commandBuffer.getComponent(vislbleRef, this.visibleComponentType);
               if (visibleComponent != null) {
                  visibleComponent.addViewerParallel(ref, entityViewerComponent);
               }
            }
         }
      }
   }

   public static class ClearEntityViewers extends EntityTickingSystem<EntityStore> {
      @Nonnull
      public static final Set<Dependency<EntityStore>> DEPENDENCIES = Collections.singleton(
         new SystemGroupDependency<>(Order.BEFORE, EntityTrackerSystems.FIND_VISIBLE_ENTITIES_GROUP)
      );
      @Nonnull
      private final ComponentType<EntityStore, EntityTrackerSystems.EntityViewer> entityViewerComponentType;

      public ClearEntityViewers(@Nonnull ComponentType<EntityStore, EntityTrackerSystems.EntityViewer> entityViewerComponentType) {
         this.entityViewerComponentType = entityViewerComponentType;
      }

      @Nonnull
      @Override
      public Set<Dependency<EntityStore>> getDependencies() {
         return DEPENDENCIES;
      }

      @Override
      public Query<EntityStore> getQuery() {
         return this.entityViewerComponentType;
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
         EntityTrackerSystems.EntityViewer entityViewerComponent = archetypeChunk.getComponent(index, this.entityViewerComponentType);

         assert entityViewerComponent != null;

         entityViewerComponent.visible.clear();
         entityViewerComponent.lodExcludedCount = 0;
         entityViewerComponent.hiddenCount = 0;
      }
   }

   public static class ClearPreviouslyVisible extends EntityTickingSystem<EntityStore> {
      @Nonnull
      public static final Set<Dependency<EntityStore>> DEPENDENCIES = Set.of(
         new SystemDependency<>(Order.AFTER, EntityTrackerSystems.ClearEntityViewers.class),
         new SystemGroupDependency<EntityStore>(Order.AFTER, EntityTrackerSystems.FIND_VISIBLE_ENTITIES_GROUP)
      );
      @Nonnull
      private final ComponentType<EntityStore, EntityTrackerSystems.Visible> visibleComponentType;

      public ClearPreviouslyVisible(@Nonnull ComponentType<EntityStore, EntityTrackerSystems.Visible> visibleComponentType) {
         this.visibleComponentType = visibleComponentType;
      }

      @Nonnull
      @Override
      public Set<Dependency<EntityStore>> getDependencies() {
         return DEPENDENCIES;
      }

      @Override
      public Query<EntityStore> getQuery() {
         return this.visibleComponentType;
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
         EntityTrackerSystems.Visible visibleComponent = archetypeChunk.getComponent(index, this.visibleComponentType);

         assert visibleComponent != null;

         Map<Ref<EntityStore>, EntityTrackerSystems.EntityViewer> oldVisibleTo = visibleComponent.previousVisibleTo;
         visibleComponent.previousVisibleTo = visibleComponent.visibleTo;
         visibleComponent.visibleTo = oldVisibleTo;
         visibleComponent.visibleTo.clear();
         visibleComponent.newlyVisibleTo.clear();
      }
   }

   public static class CollectVisible extends EntityTickingSystem<EntityStore> {
      @Nonnull
      private final ComponentType<EntityStore, EntityTrackerSystems.EntityViewer> entityViewerComponentType;
      @Nonnull
      private final Query<EntityStore> query;
      @Nonnull
      private final Set<Dependency<EntityStore>> dependencies;

      public CollectVisible(@Nonnull ComponentType<EntityStore, EntityTrackerSystems.EntityViewer> entityViewerComponentType) {
         this.entityViewerComponentType = entityViewerComponentType;
         this.query = Archetype.of(entityViewerComponentType, TransformComponent.getComponentType());
         this.dependencies = Collections.singleton(new SystemDependency<>(Order.AFTER, NetworkSendableSpatialSystem.class));
      }

      @Nullable
      @Override
      public SystemGroup<EntityStore> getGroup() {
         return EntityTrackerSystems.FIND_VISIBLE_ENTITIES_GROUP;
      }

      @Nonnull
      @Override
      public Set<Dependency<EntityStore>> getDependencies() {
         return this.dependencies;
      }

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
         TransformComponent transformComponent = archetypeChunk.getComponent(index, TransformComponent.getComponentType());

         assert transformComponent != null;

         Vector3d position = transformComponent.getPosition();
         EntityTrackerSystems.EntityViewer entityViewerComponent = archetypeChunk.getComponent(index, this.entityViewerComponentType);

         assert entityViewerComponent != null;

         SpatialStructure<Ref<EntityStore>> spatialStructure = store.getResource(EntityModule.get().getNetworkSendableSpatialResourceType())
            .getSpatialStructure();
         ObjectList<Ref<EntityStore>> results = SpatialResource.getThreadLocalReferenceList();
         spatialStructure.collect(position, entityViewerComponent.viewRadiusBlocks, results);
         entityViewerComponent.visible.addAll(results);
      }
   }

   public static class EffectControllerSystem extends EntityTickingSystem<EntityStore> {
      @Nonnull
      private final ComponentType<EntityStore, EntityTrackerSystems.Visible> visibleComponentType;
      @Nonnull
      private final ComponentType<EntityStore, EffectControllerComponent> effectControllerComponentType;
      @Nonnull
      private final Query<EntityStore> query;

      public EffectControllerSystem(
         @Nonnull ComponentType<EntityStore, EntityTrackerSystems.Visible> visibleComponentType,
         @Nonnull ComponentType<EntityStore, EffectControllerComponent> effectControllerComponentType
      ) {
         this.visibleComponentType = visibleComponentType;
         this.effectControllerComponentType = effectControllerComponentType;
         this.query = Query.and(visibleComponentType, effectControllerComponentType);
      }

      @Nullable
      @Override
      public SystemGroup<EntityStore> getGroup() {
         return EntityTrackerSystems.QUEUE_UPDATE_GROUP;
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
         EntityTrackerSystems.Visible visibleComponent = archetypeChunk.getComponent(index, this.visibleComponentType);

         assert visibleComponent != null;

         EffectControllerComponent effectControllerComponent = archetypeChunk.getComponent(index, this.effectControllerComponentType);

         assert effectControllerComponent != null;

         Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
         if (!visibleComponent.newlyVisibleTo.isEmpty()) {
            queueFullUpdate(ref, effectControllerComponent, visibleComponent.newlyVisibleTo);
         }

         if (effectControllerComponent.consumeNetworkOutdated()) {
            queueUpdatesFor(ref, effectControllerComponent, visibleComponent.visibleTo, visibleComponent.newlyVisibleTo);
         }
      }

      private static void queueFullUpdate(
         @Nonnull Ref<EntityStore> ref,
         @Nonnull EffectControllerComponent effectControllerComponent,
         @Nonnull Map<Ref<EntityStore>, EntityTrackerSystems.EntityViewer> visibleTo
      ) {
         EntityEffectsUpdate update = new EntityEffectsUpdate();
         update.entityEffectUpdates = effectControllerComponent.createInitUpdates();

         for (EntityTrackerSystems.EntityViewer viewer : visibleTo.values()) {
            viewer.queueUpdate(ref, update);
         }
      }

      private static void queueUpdatesFor(
         @Nonnull Ref<EntityStore> ref,
         @Nonnull EffectControllerComponent effectControllerComponent,
         @Nonnull Map<Ref<EntityStore>, EntityTrackerSystems.EntityViewer> visibleTo,
         @Nonnull Map<Ref<EntityStore>, EntityTrackerSystems.EntityViewer> exclude
      ) {
         EntityEffectsUpdate update = new EntityEffectsUpdate();
         update.entityEffectUpdates = effectControllerComponent.consumeChanges();
         if (!exclude.isEmpty()) {
            for (Entry<Ref<EntityStore>, EntityTrackerSystems.EntityViewer> entry : visibleTo.entrySet()) {
               if (!exclude.containsKey(entry.getKey())) {
                  entry.getValue().queueUpdate(ref, update);
               }
            }
         } else {
            for (EntityTrackerSystems.EntityViewer viewer : visibleTo.values()) {
               viewer.queueUpdate(ref, update);
            }
         }
      }
   }

   public static class EnsureVisibleComponent extends EntityTickingSystem<EntityStore> {
      @Nonnull
      public static final Set<Dependency<EntityStore>> DEPENDENCIES = Collections.singleton(
         new SystemDependency<>(Order.AFTER, EntityTrackerSystems.ClearPreviouslyVisible.class)
      );
      @Nonnull
      private final ComponentType<EntityStore, EntityTrackerSystems.EntityViewer> entityViewerComponentType;
      @Nonnull
      private final ComponentType<EntityStore, EntityTrackerSystems.Visible> visibleComponentType;

      public EnsureVisibleComponent(
         @Nonnull ComponentType<EntityStore, EntityTrackerSystems.EntityViewer> entityViewerComponentType,
         @Nonnull ComponentType<EntityStore, EntityTrackerSystems.Visible> visibleComponentType
      ) {
         this.entityViewerComponentType = entityViewerComponentType;
         this.visibleComponentType = visibleComponentType;
      }

      @Nonnull
      @Override
      public Set<Dependency<EntityStore>> getDependencies() {
         return DEPENDENCIES;
      }

      @Override
      public Query<EntityStore> getQuery() {
         return this.entityViewerComponentType;
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
         EntityTrackerSystems.EntityViewer entityViewerComponent = archetypeChunk.getComponent(index, this.entityViewerComponentType);

         assert entityViewerComponent != null;

         for (Ref<EntityStore> visibleRef : entityViewerComponent.visible) {
            if (visibleRef != null && visibleRef.isValid() && !commandBuffer.getArchetype(visibleRef).contains(this.visibleComponentType)) {
               commandBuffer.ensureComponent(visibleRef, this.visibleComponentType);
            }
         }
      }
   }

   public static class EntityUpdate {
      @Nonnull
      private final StampedLock removeLock = new StampedLock();
      @Nonnull
      private final EnumSet<ComponentUpdateType> removed;
      @Nonnull
      private final StampedLock updatesLock = new StampedLock();
      @Nonnull
      private final List<ComponentUpdate> updates;

      public EntityUpdate() {
         this.removed = EnumSet.noneOf(ComponentUpdateType.class);
         this.updates = new ObjectArrayList();
      }

      public EntityUpdate(@Nonnull EntityTrackerSystems.EntityUpdate other) {
         this.removed = EnumSet.copyOf(other.removed);
         this.updates = new ObjectArrayList(other.updates);
      }

      @Nonnull
      public EntityTrackerSystems.EntityUpdate clone() {
         return new EntityTrackerSystems.EntityUpdate(this);
      }

      public void queueRemove(@Nonnull ComponentUpdateType type) {
         long stamp = this.removeLock.writeLock();

         try {
            this.removed.add(type);
         } finally {
            this.removeLock.unlockWrite(stamp);
         }
      }

      public void queueUpdate(@Nonnull ComponentUpdate update) {
         long stamp = this.updatesLock.writeLock();

         try {
            this.updates.add(update);
         } finally {
            this.updatesLock.unlockWrite(stamp);
         }
      }

      @Nullable
      public ComponentUpdateType[] toRemovedArray() {
         return this.removed.isEmpty() ? null : this.removed.toArray(ComponentUpdateType[]::new);
      }

      @Nullable
      public ComponentUpdate[] toUpdatesArray() {
         return this.updates.isEmpty() ? null : this.updates.toArray(ComponentUpdate[]::new);
      }
   }

   public static class EntityViewer implements Component<EntityStore> {
      public int viewRadiusBlocks;
      @Nonnull
      public IPacketReceiver packetReceiver;
      @Nonnull
      public Set<Ref<EntityStore>> visible;
      @Nonnull
      public Map<Ref<EntityStore>, EntityTrackerSystems.EntityUpdate> updates;
      @Nonnull
      public Object2IntMap<Ref<EntityStore>> sent;
      public int lodExcludedCount;
      public int hiddenCount;

      public static ComponentType<EntityStore, EntityTrackerSystems.EntityViewer> getComponentType() {
         return EntityModule.get().getEntityViewerComponentType();
      }

      public EntityViewer(int viewRadiusBlocks, @Nonnull IPacketReceiver packetReceiver) {
         this.viewRadiusBlocks = viewRadiusBlocks;
         this.packetReceiver = packetReceiver;
         this.visible = new ObjectOpenHashSet();
         this.updates = new ConcurrentHashMap<>();
         this.sent = new Object2IntOpenHashMap();
         this.sent.defaultReturnValue(-1);
      }

      public EntityViewer(@Nonnull EntityTrackerSystems.EntityViewer other) {
         this.viewRadiusBlocks = other.viewRadiusBlocks;
         this.packetReceiver = other.packetReceiver;
         this.visible = new HashSet<>(other.visible);
         this.updates = new ConcurrentHashMap<>(other.updates.size());

         for (Entry<Ref<EntityStore>, EntityTrackerSystems.EntityUpdate> entry : other.updates.entrySet()) {
            this.updates.put(entry.getKey(), entry.getValue().clone());
         }

         this.sent = new Object2IntOpenHashMap(other.sent);
         this.sent.defaultReturnValue(-1);
      }

      @Nonnull
      @Override
      public Component<EntityStore> clone() {
         return new EntityTrackerSystems.EntityViewer(this);
      }

      public void queueRemove(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentUpdateType type) {
         if (!this.visible.contains(ref)) {
            throw new IllegalArgumentException("Entity is not visible!");
         } else {
            this.updates.computeIfAbsent(ref, k -> new EntityTrackerSystems.EntityUpdate()).queueRemove(type);
         }
      }

      public void queueUpdate(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentUpdate update) {
         if (!this.visible.contains(ref)) {
            throw new IllegalArgumentException("Entity is not visible!");
         } else {
            this.updates.computeIfAbsent(ref, k -> new EntityTrackerSystems.EntityUpdate()).queueUpdate(update);
         }
      }
   }

   public static class RemoveEmptyVisibleComponent extends EntityTickingSystem<EntityStore> {
      @Nonnull
      public static final Set<Dependency<EntityStore>> DEPENDENCIES = Set.of(
         new SystemDependency<>(Order.AFTER, EntityTrackerSystems.AddToVisible.class),
         new SystemGroupDependency<EntityStore>(Order.BEFORE, EntityTrackerSystems.QUEUE_UPDATE_GROUP)
      );
      @Nonnull
      private final ComponentType<EntityStore, EntityTrackerSystems.Visible> visibleComponentType;

      public RemoveEmptyVisibleComponent(@Nonnull ComponentType<EntityStore, EntityTrackerSystems.Visible> visibleComponentType) {
         this.visibleComponentType = visibleComponentType;
      }

      @Nonnull
      @Override
      public Set<Dependency<EntityStore>> getDependencies() {
         return DEPENDENCIES;
      }

      @Override
      public Query<EntityStore> getQuery() {
         return this.visibleComponentType;
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
         EntityTrackerSystems.Visible visibleComponent = archetypeChunk.getComponent(index, this.visibleComponentType);

         assert visibleComponent != null;

         if (visibleComponent.visibleTo.isEmpty()) {
            commandBuffer.removeComponent(archetypeChunk.getReferenceTo(index), this.visibleComponentType);
         }
      }
   }

   public static class RemoveVisibleComponent extends HolderSystem<EntityStore> {
      @Nonnull
      private final ComponentType<EntityStore, EntityTrackerSystems.Visible> visibleComponentType;

      public RemoveVisibleComponent(@Nonnull ComponentType<EntityStore, EntityTrackerSystems.Visible> visibleComponentType) {
         this.visibleComponentType = visibleComponentType;
      }

      @Override
      public Query<EntityStore> getQuery() {
         return this.visibleComponentType;
      }

      @Override
      public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
      }

      @Override
      public void onEntityRemoved(@Nonnull Holder<EntityStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store) {
         holder.removeComponent(this.visibleComponentType);
      }
   }

   public static class SendPackets extends EntityTickingSystem<EntityStore> {
      @Nonnull
      public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
      @Nonnull
      public static final ThreadLocal<IntList> INT_LIST_THREAD_LOCAL = ThreadLocal.withInitial(IntArrayList::new);
      @Nonnull
      public static final Set<Dependency<EntityStore>> DEPENDENCIES = Set.of(new SystemGroupDependency<>(Order.AFTER, EntityTrackerSystems.QUEUE_UPDATE_GROUP));
      @Nonnull
      private final ComponentType<EntityStore, EntityTrackerSystems.EntityViewer> entityViewerComponentType;

      public SendPackets(@Nonnull ComponentType<EntityStore, EntityTrackerSystems.EntityViewer> entityViewerComponentType) {
         this.entityViewerComponentType = entityViewerComponentType;
      }

      @Nullable
      @Override
      public SystemGroup<EntityStore> getGroup() {
         return EntityStore.SEND_PACKET_GROUP;
      }

      @Nonnull
      @Override
      public Set<Dependency<EntityStore>> getDependencies() {
         return DEPENDENCIES;
      }

      @Override
      public Query<EntityStore> getQuery() {
         return this.entityViewerComponentType;
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
         EntityTrackerSystems.EntityViewer entityViewerComponent = archetypeChunk.getComponent(index, this.entityViewerComponentType);

         assert entityViewerComponent != null;

         IntList removedEntities = INT_LIST_THREAD_LOCAL.get();
         removedEntities.clear();
         int before = entityViewerComponent.updates.size();
         entityViewerComponent.updates.entrySet().removeIf(v -> !v.getKey().isValid());
         if (before != entityViewerComponent.updates.size()) {
            ((HytaleLogger.Api)LOGGER.atWarning()).log("Removed %d invalid updates for removed entities.", before - entityViewerComponent.updates.size());
         }

         ObjectIterator<it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<Ref<EntityStore>>> iterator = entityViewerComponent.sent
            .object2IntEntrySet()
            .iterator();

         while (iterator.hasNext()) {
            it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<Ref<EntityStore>> entry = (it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<Ref<EntityStore>>)iterator.next();
            Ref<EntityStore> ref = (Ref<EntityStore>)entry.getKey();
            if (ref == null || !ref.isValid() || !entityViewerComponent.visible.contains(ref)) {
               removedEntities.add(entry.getIntValue());
               iterator.remove();
               if (entityViewerComponent.updates.remove(ref) != null) {
                  ((HytaleLogger.Api)LOGGER.atSevere()).log("Entity can't be removed and also receive an update! " + ref);
               }
            }
         }

         if (!removedEntities.isEmpty() || !entityViewerComponent.updates.isEmpty()) {
            Iterator<Ref<EntityStore>> iteratorx = entityViewerComponent.updates.keySet().iterator();

            while (iteratorx.hasNext()) {
               Ref<EntityStore> ref = iteratorx.next();
               if (ref == null || !ref.isValid() || ref.getStore() != store) {
                  iteratorx.remove();
               } else if (!entityViewerComponent.sent.containsKey(ref)) {
                  NetworkId networkIdComponent = commandBuffer.getComponent(ref, NetworkId.getComponentType());

                  assert networkIdComponent != null;

                  int networkId = networkIdComponent.getId();
                  if (networkId == -1) {
                     throw new IllegalArgumentException("Invalid entity network id: " + ref);
                  }

                  entityViewerComponent.sent.put(ref, networkId);
               }
            }

            EntityUpdates packet = new EntityUpdates();
            packet.removed = !removedEntities.isEmpty() ? removedEntities.toIntArray() : null;
            packet.updates = new com.hypixel.hytale.protocol.EntityUpdate[entityViewerComponent.updates.size()];
            int i = 0;

            for (Entry<Ref<EntityStore>, EntityTrackerSystems.EntityUpdate> entry : entityViewerComponent.updates.entrySet()) {
               com.hypixel.hytale.protocol.EntityUpdate entityUpdate = packet.updates[i++] = new com.hypixel.hytale.protocol.EntityUpdate();
               entityUpdate.networkId = entityViewerComponent.sent.getInt(entry.getKey());
               EntityTrackerSystems.EntityUpdate update = entry.getValue();
               entityUpdate.removed = update.toRemovedArray();
               entityUpdate.updates = update.toUpdatesArray();
            }

            entityViewerComponent.updates.clear();
            entityViewerComponent.packetReceiver.writeNoCache(packet);
         }
      }
   }

   public static class Visible implements Component<EntityStore> {
      @Nonnull
      private final StampedLock lock = new StampedLock();
      @Nonnull
      public Map<Ref<EntityStore>, EntityTrackerSystems.EntityViewer> previousVisibleTo = new Object2ObjectOpenHashMap();
      @Nonnull
      public Map<Ref<EntityStore>, EntityTrackerSystems.EntityViewer> visibleTo = new Object2ObjectOpenHashMap();
      @Nonnull
      public Map<Ref<EntityStore>, EntityTrackerSystems.EntityViewer> newlyVisibleTo = new Object2ObjectOpenHashMap();

      @Nonnull
      public static ComponentType<EntityStore, EntityTrackerSystems.Visible> getComponentType() {
         return EntityModule.get().getVisibleComponentType();
      }

      @Nonnull
      @Override
      public Component<EntityStore> clone() {
         return new EntityTrackerSystems.Visible();
      }

      public void addViewerParallel(@Nonnull Ref<EntityStore> ref, @Nonnull EntityTrackerSystems.EntityViewer entityViewerComponent) {
         long stamp = this.lock.writeLock();

         try {
            this.visibleTo.put(ref, entityViewerComponent);
            if (!this.previousVisibleTo.containsKey(ref)) {
               this.newlyVisibleTo.put(ref, entityViewerComponent);
            }
         } finally {
            this.lock.unlockWrite(stamp);
         }
      }
   }
}
