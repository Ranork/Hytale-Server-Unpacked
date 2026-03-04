package com.hypixel.hytale.server.core.modules.entity;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.DisableProcessingAssert;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.HolderSystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.protocol.BlockUpdate;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.entities.BlockEntity;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.EntityScaleComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.EntityTrackerSystems;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.modules.physics.SimplePhysicsProvider;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Map;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockEntitySystems {
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

   public static class BlockEntitySetupSystem extends HolderSystem<EntityStore> {
      private final ComponentType<EntityStore, BlockEntity> blockEntityComponentType;

      public BlockEntitySetupSystem(ComponentType<EntityStore, BlockEntity> blockEntityComponentType) {
         this.blockEntityComponentType = blockEntityComponentType;
      }

      @Override
      public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
         if (!holder.getArchetype().contains(NetworkId.getComponentType())) {
            holder.addComponent(NetworkId.getComponentType(), new NetworkId(store.getExternalData().takeNextNetworkId()));
         }

         BlockEntity blockEntityComponent = holder.getComponent(this.blockEntityComponentType);
         BoundingBox boundingBoxComponent = blockEntityComponent.createBoundingBoxComponent();
         if (boundingBoxComponent == null) {
            BlockEntitySystems.LOGGER
               .at(Level.SEVERE)
               .log("Bounding box could not be initialized properly, defaulting to 1x1x1 dimensions for Block Entity bounding box");
            boundingBoxComponent = new BoundingBox(Box.horizontallyCentered(1.0, 1.0, 1.0));
         }

         holder.putComponent(BoundingBox.getComponentType(), boundingBoxComponent);
         SimplePhysicsProvider simplePhysicsProvider = blockEntityComponent.initPhysics(boundingBoxComponent);
         simplePhysicsProvider.setMoveOutOfSolid(false);
      }

      @Override
      public void onEntityRemoved(@Nonnull Holder<EntityStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store) {
      }

      @Override
      public Query<EntityStore> getQuery() {
         return this.blockEntityComponentType;
      }
   }

   public static class BlockEntityTrackerSystem extends EntityTickingSystem<EntityStore> {
      private final ComponentType<EntityStore, EntityTrackerSystems.Visible> visibleComponentType;
      private final ComponentType<EntityStore, BlockEntity> blockEntityComponentType;
      @Nonnull
      private final Query<EntityStore> query;

      public BlockEntityTrackerSystem(
         ComponentType<EntityStore, EntityTrackerSystems.Visible> visibleComponentType, ComponentType<EntityStore, BlockEntity> blockEntityComponentType
      ) {
         this.visibleComponentType = visibleComponentType;
         this.blockEntityComponentType = blockEntityComponentType;
         this.query = Query.and(visibleComponentType, blockEntityComponentType);
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
         BlockEntity blockEntity = archetypeChunk.getComponent(index, this.blockEntityComponentType);

         assert blockEntity != null;

         float entityScale = 2.0F;
         boolean scaleOutdated = false;
         EntityScaleComponent entityScaleComponent = archetypeChunk.getComponent(index, EntityScaleComponent.getComponentType());
         if (entityScaleComponent != null) {
            entityScale = entityScaleComponent.getScale();
            scaleOutdated = entityScaleComponent.consumeNetworkOutdated();
         }

         boolean blockIdOutdated = blockEntity.consumeBlockIdNetworkOutdated();
         if (blockIdOutdated || scaleOutdated) {
            queueUpdatesFor(archetypeChunk.getReferenceTo(index), blockEntity, visible.visibleTo, entityScale);
         } else if (!visible.newlyVisibleTo.isEmpty()) {
            queueUpdatesFor(archetypeChunk.getReferenceTo(index), blockEntity, visible.newlyVisibleTo, entityScale);
         }
      }

      private static void queueUpdatesFor(
         Ref<EntityStore> ref, @Nonnull BlockEntity entity, @Nonnull Map<Ref<EntityStore>, EntityTrackerSystems.EntityViewer> visibleTo, float entityScale
      ) {
         String key = entity.getBlockTypeKey();
         int index = BlockType.getAssetMap().getIndex(key);
         if (index == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unknown key! " + key);
         } else {
            BlockUpdate update = new BlockUpdate(index, entityScale);

            for (EntityTrackerSystems.EntityViewer viewer : visibleTo.values()) {
               viewer.queueUpdate(ref, update);
            }
         }
      }
   }

   public static class Ticking extends EntityTickingSystem<EntityStore> implements DisableProcessingAssert {
      private final ComponentType<EntityStore, TransformComponent> transformComponentType = TransformComponent.getComponentType();
      private final ComponentType<EntityStore, BlockEntity> blockEntityComponentType;
      private final Archetype<EntityStore> archetype;

      public Ticking(@Nonnull ComponentType<EntityStore, BlockEntity> blockEntityComponentType) {
         this.blockEntityComponentType = blockEntityComponentType;
         this.archetype = Archetype.of(this.transformComponentType, blockEntityComponentType, Velocity.getComponentType());
      }

      @Override
      public Query<EntityStore> getQuery() {
         return this.archetype;
      }

      @Override
      public void tick(
         float dt,
         int index,
         @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         TransformComponent transformComponent = archetypeChunk.getComponent(index, this.transformComponentType);

         assert transformComponent != null;

         BlockEntity blockEntityComponent = archetypeChunk.getComponent(index, this.blockEntityComponentType);

         assert blockEntityComponent != null;

         Velocity velocityComponent = archetypeChunk.getComponent(index, Velocity.getComponentType());

         assert velocityComponent != null;

         try {
            blockEntityComponent.getSimplePhysicsProvider()
               .tick(dt, velocityComponent, store.getExternalData().getWorld(), transformComponent, archetypeChunk.getReferenceTo(index), commandBuffer);
         } catch (Throwable var10) {
            ((HytaleLogger.Api)BlockEntitySystems.LOGGER.at(Level.SEVERE).withCause(var10))
               .log("Exception while ticking entity. Removing entity %s", blockEntityComponent);
            commandBuffer.removeEntity(archetypeChunk.getReferenceTo(index), RemoveReason.REMOVE);
         }
      }
   }
}
