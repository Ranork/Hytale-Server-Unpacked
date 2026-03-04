package com.hypixel.hytale.server.core.modules.entity.repulsion;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.OrderPriority;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.component.system.HolderSystem;
import com.hypixel.hytale.component.system.RefChangeSystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Vector2d;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.ChangeVelocityType;
import com.hypixel.hytale.protocol.ComponentUpdateType;
import com.hypixel.hytale.protocol.RepulsionUpdate;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.system.PlayerSpatialSystem;
import com.hypixel.hytale.server.core.modules.entity.tracker.EntityTrackerSystems;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.modules.physics.systems.IVelocityModifyingSystem;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RepulsionSystems {
   public static class EntityTrackerRemove extends RefChangeSystem<EntityStore, Repulsion> {
      private final ComponentType<EntityStore, Repulsion> componentType;
      private final ComponentType<EntityStore, EntityTrackerSystems.Visible> visibleComponentType;
      @Nonnull
      private final Query<EntityStore> query;

      public EntityTrackerRemove(
         ComponentType<EntityStore, EntityTrackerSystems.Visible> visibleComponentType, ComponentType<EntityStore, Repulsion> componentType
      ) {
         this.visibleComponentType = visibleComponentType;
         this.componentType = componentType;
         this.query = Query.and(visibleComponentType, componentType);
      }

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return this.query;
      }

      @Nonnull
      @Override
      public ComponentType<EntityStore, Repulsion> componentType() {
         return this.componentType;
      }

      public void onComponentAdded(
         @Nonnull Ref<EntityStore> ref, @Nonnull Repulsion component, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
      }

      public void onComponentSet(
         @Nonnull Ref<EntityStore> ref,
         Repulsion oldComponent,
         @Nonnull Repulsion newComponent,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
      }

      public void onComponentRemoved(
         @Nonnull Ref<EntityStore> ref, @Nonnull Repulsion component, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         for (EntityTrackerSystems.EntityViewer viewer : store.getComponent(ref, this.visibleComponentType).visibleTo.values()) {
            viewer.queueRemove(ref, ComponentUpdateType.Repulsion);
         }
      }
   }

   public static class EntityTrackerUpdate extends EntityTickingSystem<EntityStore> {
      private final ComponentType<EntityStore, EntityTrackerSystems.Visible> visibleComponentType;
      private final ComponentType<EntityStore, Repulsion> componentType;
      @Nonnull
      private final Query<EntityStore> query;

      public EntityTrackerUpdate(
         ComponentType<EntityStore, EntityTrackerSystems.Visible> visibleComponentType, ComponentType<EntityStore, Repulsion> componentType
      ) {
         this.visibleComponentType = visibleComponentType;
         this.componentType = componentType;
         this.query = Query.and(visibleComponentType, componentType);
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
         EntityTrackerSystems.Visible visible = archetypeChunk.getComponent(index, this.visibleComponentType);
         Repulsion repulsion = archetypeChunk.getComponent(index, this.componentType);
         if (repulsion.consumeNetworkOutdated()) {
            queueUpdatesFor(archetypeChunk.getReferenceTo(index), repulsion, visible.visibleTo);
         } else if (!visible.newlyVisibleTo.isEmpty()) {
            queueUpdatesFor(archetypeChunk.getReferenceTo(index), repulsion, visible.newlyVisibleTo);
         }
      }

      private static void queueUpdatesFor(
         Ref<EntityStore> ref, @Nonnull Repulsion repulsion, @Nonnull Map<Ref<EntityStore>, EntityTrackerSystems.EntityViewer> visibleTo
      ) {
         RepulsionUpdate update = new RepulsionUpdate(repulsion.getRepulsionConfigIndex());

         for (EntityTrackerSystems.EntityViewer viewer : visibleTo.values()) {
            viewer.queueUpdate(ref, update);
         }
      }
   }

   public static class PlayerSetup extends HolderSystem<EntityStore> {
      private final ComponentType<EntityStore, Repulsion> componentType;
      @Nonnull
      private final Query<EntityStore> query;

      public PlayerSetup(ComponentType<EntityStore, Repulsion> componentType, ComponentType<EntityStore, Player> playerComponentType) {
         this.componentType = componentType;
         this.query = Query.and(playerComponentType, Query.not(componentType));
      }

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return this.query;
      }

      @Override
      public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
         World world = store.getExternalData().getWorld();
         int repulsionConfigIndex = world.getGameplayConfig().getPlayerConfig().getRepulsionConfigIndex();
         if (repulsionConfigIndex == -1) {
            if (holder.getComponent(this.componentType) != null) {
               holder.removeComponent(this.componentType);
            }
         } else {
            RepulsionConfig repulsion = RepulsionConfig.getAssetMap().getAsset(repulsionConfigIndex);
            if (holder.getComponent(this.componentType) != null) {
               holder.removeComponent(this.componentType);
            }

            holder.addComponent(this.componentType, new Repulsion(repulsion));
         }
      }

      @Override
      public void onEntityRemoved(@Nonnull Holder<EntityStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store) {
      }
   }

   public static class RepulsionTicker extends EntityTickingSystem<EntityStore> implements IVelocityModifyingSystem {
      private final ComponentType<EntityStore, Repulsion> repulsionComponentType;
      private final ComponentType<EntityStore, TransformComponent> transformComponentComponentType;
      @Nonnull
      private final Query<EntityStore> query;
      @Nonnull
      private final Set<Dependency<EntityStore>> dependencies;
      private final ResourceType<EntityStore, SpatialResource<Ref<EntityStore>, EntityStore>> spatialComponent;

      public RepulsionTicker(
         ComponentType<EntityStore, Repulsion> repulsionComponentType,
         ComponentType<EntityStore, TransformComponent> transformComponentComponentType,
         ResourceType<EntityStore, SpatialResource<Ref<EntityStore>, EntityStore>> spatialComponent
      ) {
         this.repulsionComponentType = repulsionComponentType;
         this.transformComponentComponentType = transformComponentComponentType;
         this.query = Query.and(repulsionComponentType, transformComponentComponentType);
         this.spatialComponent = spatialComponent;
         this.dependencies = Set.of(new SystemDependency<>(Order.AFTER, PlayerSpatialSystem.class, OrderPriority.CLOSEST));
      }

      @Nonnull
      @Override
      public Set<Dependency<EntityStore>> getDependencies() {
         return this.dependencies;
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
      public void tick(
         float dt,
         int index,
         @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         Repulsion repulsionComponent = archetypeChunk.getComponent(index, this.repulsionComponentType);

         assert repulsionComponent != null;

         int repulsionConfigIndex = repulsionComponent.getRepulsionConfigIndex();
         if (repulsionConfigIndex != -1) {
            RepulsionConfig repulsion = RepulsionConfig.getAssetMap().getAsset(repulsionConfigIndex);
            float radius = repulsion.radius;
            TransformComponent transformComponent = archetypeChunk.getComponent(index, this.transformComponentComponentType);

            assert transformComponent != null;

            Vector2d position = new Vector2d(transformComponent.getPosition().x, transformComponent.getPosition().z);
            SpatialResource<Ref<EntityStore>, EntityStore> spatialResource = store.getResource(this.spatialComponent);
            List<Ref<EntityStore>> results = new ObjectArrayList();
            spatialResource.getSpatialStructure().ordered(transformComponent.getPosition(), radius, results);

            for (Ref<EntityStore> entityRef : results) {
               TransformComponent entityTransformComponent = commandBuffer.getComponent(entityRef, this.transformComponentComponentType);
               if (entityTransformComponent != null) {
                  Vector2d entityPosition = new Vector2d(entityTransformComponent.getPosition().x, entityTransformComponent.getPosition().z);
                  if (!entityPosition.equals(position)) {
                     double distance = position.distanceTo(entityPosition);
                     if (!(distance < 0.1)) {
                        double fraction = (radius - distance) / radius;
                        float maxForce = repulsion.maxForce;
                        int flip = 1;
                        if (maxForce < 0.0F) {
                           flip = -1;
                           maxForce *= flip;
                        }

                        double force = Math.max((double)repulsion.minForce, maxForce * fraction);
                        force *= flip;
                        Vector2d push = entityPosition.subtract(position);
                        push.normalize();
                        push.scale(force);
                        Velocity entityVelocityComponent = commandBuffer.getComponent(entityRef, Velocity.getComponentType());
                        if (entityVelocityComponent != null) {
                           Vector3d addedVelocity = new Vector3d((float)push.x, 0.0, (float)push.y);
                           entityVelocityComponent.addInstruction(addedVelocity, null, ChangeVelocityType.Add);
                        }
                     }
                  }
               }
            }
         }
      }
   }
}
