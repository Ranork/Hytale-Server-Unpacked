package com.hypixel.hytale.server.core.modules.entity.system;

import com.hypixel.hytale.component.AddReason;
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
import com.hypixel.hytale.component.system.HolderSystem;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.protocol.ColorLight;
import com.hypixel.hytale.protocol.ComponentUpdateType;
import com.hypixel.hytale.protocol.DynamicLightUpdate;
import com.hypixel.hytale.protocol.NewSpawnUpdate;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.DynamicLight;
import com.hypixel.hytale.server.core.modules.entity.component.FromPrefab;
import com.hypixel.hytale.server.core.modules.entity.component.FromWorldGen;
import com.hypixel.hytale.server.core.modules.entity.component.NewSpawnComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.EntityTrackerSystems;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.EntityChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EntitySystems {
   public static class ClearFromPrefabMarker extends EntitySystems.ClearMarker<FromPrefab> {
      public ClearFromPrefabMarker(@Nonnull ComponentType<EntityStore, FromPrefab> componentType, @Nonnull SystemGroup<EntityStore> preGroup) {
         super(componentType, preGroup);
      }
   }

   public static class ClearFromWorldGenMarker extends EntitySystems.ClearMarker<FromWorldGen> {
      public ClearFromWorldGenMarker(@Nonnull ComponentType<EntityStore, FromWorldGen> componentType, @Nonnull SystemGroup<EntityStore> preGroup) {
         super(componentType, preGroup);
      }
   }

   public abstract static class ClearMarker<T extends Component<EntityStore>> extends RefSystem<EntityStore> {
      @Nonnull
      private final ComponentType<EntityStore, T> componentType;
      @Nonnull
      private final Set<Dependency<EntityStore>> dependencies;

      public ClearMarker(@Nonnull ComponentType<EntityStore, T> componentType, @Nonnull SystemGroup<EntityStore> preGroup) {
         this.componentType = componentType;
         this.dependencies = Set.of(new SystemGroupDependency<>(Order.AFTER, preGroup));
      }

      @Override
      public void onEntityAdded(
         @Nonnull Ref<EntityStore> ref, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         commandBuffer.removeComponent(ref, this.componentType);
      }

      @Override
      public void onEntityRemove(
         @Nonnull Ref<EntityStore> ref, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
      }

      @Override
      public Query<EntityStore> getQuery() {
         return this.componentType;
      }

      @Nonnull
      @Override
      public Set<Dependency<EntityStore>> getDependencies() {
         return this.dependencies;
      }
   }

   public static class DynamicLightTracker extends EntityTickingSystem<EntityStore> {
      @Nonnull
      private final ComponentType<EntityStore, EntityTrackerSystems.Visible> componentType;
      @Nonnull
      private final ComponentType<EntityStore, DynamicLight> dynamicLightType;
      @Nonnull
      private final Query<EntityStore> query;

      public DynamicLightTracker(@Nonnull ComponentType<EntityStore, EntityTrackerSystems.Visible> componentType) {
         this.componentType = componentType;
         this.dynamicLightType = DynamicLight.getComponentType();
         this.query = Query.and(componentType, this.dynamicLightType);
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
         EntityTrackerSystems.Visible visibleComponent = archetypeChunk.getComponent(index, this.componentType);

         assert visibleComponent != null;

         DynamicLight dynamicLightComponent = archetypeChunk.getComponent(index, this.dynamicLightType);

         assert dynamicLightComponent != null;

         ColorLight dynamicLight = dynamicLightComponent.getColorLight();
         if (dynamicLightComponent.consumeNetworkOutdated()) {
            if (dynamicLight != null) {
               queueUpdatesFor(archetypeChunk.getReferenceTo(index), dynamicLight, visibleComponent.visibleTo);
            } else {
               queueRemoveFor(archetypeChunk.getReferenceTo(index), visibleComponent.visibleTo);
            }
         } else if (!visibleComponent.newlyVisibleTo.isEmpty() && dynamicLight != null) {
            queueUpdatesFor(archetypeChunk.getReferenceTo(index), dynamicLight, visibleComponent.newlyVisibleTo);
         }
      }

      private static void queueUpdatesFor(
         @Nonnull Ref<EntityStore> ref, @Nonnull ColorLight dynamicLight, @Nonnull Map<Ref<EntityStore>, EntityTrackerSystems.EntityViewer> visibleTo
      ) {
         DynamicLightUpdate update = new DynamicLightUpdate(dynamicLight);

         for (EntityTrackerSystems.EntityViewer viewer : visibleTo.values()) {
            viewer.queueUpdate(ref, update);
         }
      }

      private static void queueRemoveFor(@Nonnull Ref<EntityStore> ref, @Nonnull Map<Ref<EntityStore>, EntityTrackerSystems.EntityViewer> visibleTo) {
         for (EntityTrackerSystems.EntityViewer viewer : visibleTo.values()) {
            viewer.queueRemove(ref, ComponentUpdateType.DynamicLight);
         }
      }
   }

   public static class NewSpawnEntityTrackerUpdate extends EntityTickingSystem<EntityStore> {
      @Nonnull
      private final ComponentType<EntityStore, EntityTrackerSystems.Visible> visibleComponentType = EntityTrackerSystems.Visible.getComponentType();
      @Nonnull
      private final ComponentType<EntityStore, NewSpawnComponent> newSpawnComponentType = NewSpawnComponent.getComponentType();
      @Nonnull
      private final Query<EntityStore> query = Query.and(this.visibleComponentType, this.newSpawnComponentType);

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

         NewSpawnComponent newSpawnComponent = archetypeChunk.getComponent(index, this.newSpawnComponentType);

         assert newSpawnComponent != null;

         Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
         if (!visibleComponent.newlyVisibleTo.isEmpty()) {
            NewSpawnUpdate update = new NewSpawnUpdate();

            for (Entry<Ref<EntityStore>, EntityTrackerSystems.EntityViewer> entry : visibleComponent.newlyVisibleTo.entrySet()) {
               entry.getValue().queueUpdate(ref, update);
            }
         }
      }
   }

   public static class NewSpawnTick extends EntityTickingSystem<EntityStore> {
      @Nonnull
      private final ComponentType<EntityStore, NewSpawnComponent> newSpawnComponentType = NewSpawnComponent.getComponentType();

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return this.newSpawnComponentType;
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
         NewSpawnComponent newSpawnComponent = archetypeChunk.getComponent(index, this.newSpawnComponentType);

         assert newSpawnComponent != null;

         if (newSpawnComponent.newSpawnWindowPassed(dt)) {
            commandBuffer.removeComponent(archetypeChunk.getReferenceTo(index), this.newSpawnComponentType);
         }
      }
   }

   public static class OnLoadFromExternal extends HolderSystem<EntityStore> {
      @Nonnull
      private final Query<EntityStore> query;
      @Nonnull
      private final SystemGroup<EntityStore> group;
      @Nonnull
      private final Set<Dependency<EntityStore>> dependencies;

      public OnLoadFromExternal(
         @Nonnull ComponentType<EntityStore, FromPrefab> fromPrefab,
         @Nonnull ComponentType<EntityStore, FromWorldGen> fromWorldGen,
         @Nonnull SystemGroup<EntityStore> group
      ) {
         this.query = Query.and(Query.or(fromPrefab, fromWorldGen), UUIDComponent.getComponentType());
         this.group = group;
         this.dependencies = Set.of(
            new SystemDependency<>(Order.BEFORE, EntityStore.UUIDSystem.class), new SystemDependency<>(Order.AFTER, EntityModule.LegacyUUIDSystem.class)
         );
      }

      @Override
      public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
         holder.putComponent(UUIDComponent.getComponentType(), UUIDComponent.generateVersion3UUID());
      }

      @Override
      public void onEntityRemoved(@Nonnull Holder<EntityStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store) {
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

      @Nonnull
      @Override
      public SystemGroup<EntityStore> getGroup() {
         return this.group;
      }
   }

   public static class UnloadEntityFromChunk extends RefSystem<EntityStore> {
      @Override
      public Query<EntityStore> getQuery() {
         return TransformComponent.getComponentType();
      }

      @Override
      public void onEntityAdded(
         @Nonnull Ref<EntityStore> ref, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
      }

      @Override
      public void onEntityRemove(
         @Nonnull Ref<EntityStore> ref, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());

         assert transformComponent != null;

         Ref<ChunkStore> chunkRef = transformComponent.getChunkRef();
         if (chunkRef != null && chunkRef.isValid()) {
            World world = commandBuffer.getExternalData().getWorld();
            ChunkStore chunkStore = world.getChunkStore();
            Store<ChunkStore> chunkComponentStore = chunkStore.getStore();
            EntityChunk entityChunkComponent = chunkComponentStore.getComponent(chunkRef, EntityChunk.getComponentType());

            assert entityChunkComponent != null;

            switch (reason) {
               case REMOVE:
                  entityChunkComponent.removeEntityReference(ref);
                  break;
               case UNLOAD:
                  entityChunkComponent.unloadEntityReference(ref);
            }
         }
      }
   }
}
