package com.hypixel.hytale.server.core.modules.entity.item;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.modules.collision.BlockCollisionData;
import com.hypixel.hytale.server.core.modules.collision.CollisionModule;
import com.hypixel.hytale.server.core.modules.collision.CollisionResult;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class ItemPhysicsSystem extends EntityTickingSystem<EntityStore> {
   @Nonnull
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   @Nonnull
   private final ComponentType<EntityStore, ItemPhysicsComponent> itemPhysicsComponentType;
   @Nonnull
   private final ComponentType<EntityStore, BoundingBox> boundingBoxComponentType;
   @Nonnull
   private final ComponentType<EntityStore, Velocity> velocityComponentType;
   @Nonnull
   private final ComponentType<EntityStore, TransformComponent> transformComponentType;
   @Nonnull
   private final Query<EntityStore> query;

   public ItemPhysicsSystem(
      @Nonnull ComponentType<EntityStore, ItemPhysicsComponent> itemPhysicsComponentType,
      @Nonnull ComponentType<EntityStore, Velocity> velocityComponentType,
      @Nonnull ComponentType<EntityStore, BoundingBox> boundingBoxComponentType
   ) {
      this.itemPhysicsComponentType = itemPhysicsComponentType;
      this.velocityComponentType = velocityComponentType;
      this.boundingBoxComponentType = boundingBoxComponentType;
      this.transformComponentType = TransformComponent.getComponentType();
      this.query = Query.and(itemPhysicsComponentType, boundingBoxComponentType, velocityComponentType, this.transformComponentType);
   }

   @Nonnull
   @Override
   public Query<EntityStore> getQuery() {
      return this.query;
   }

   @Override
   public boolean isParallel(int archetypeChunkSize, int taskCount) {
      return false;
   }

   @Override
   public void tick(
      float dt,
      int index,
      @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
      @Nonnull Store<EntityStore> store,
      @Nonnull CommandBuffer<EntityStore> commandBuffer
   ) {
      World world = store.getExternalData().getWorld();
      ItemPhysicsComponent itemPhysicsComponent = archetypeChunk.getComponent(index, this.itemPhysicsComponentType);

      assert itemPhysicsComponent != null;

      Velocity velocityComponent = archetypeChunk.getComponent(index, this.velocityComponentType);

      assert velocityComponent != null;

      TransformComponent transformComponent = archetypeChunk.getComponent(index, this.transformComponentType);

      assert transformComponent != null;

      Vector3d position = transformComponent.getPosition();
      Vector3d scaledVelocity = itemPhysicsComponent.scaledVelocity;
      CollisionResult collisionResult = itemPhysicsComponent.collisionResult;
      velocityComponent.assignVelocityTo(scaledVelocity).scale(dt);
      BoundingBox boundingBoxComponent = archetypeChunk.getComponent(index, this.boundingBoxComponentType);

      assert boundingBoxComponent != null;

      Box boundingBox = boundingBoxComponent.getBoundingBox();
      if (CollisionModule.isBelowMovementThreshold(scaledVelocity)) {
         CollisionModule.findBlockCollisionsShortDistance(world, boundingBox, position, scaledVelocity, collisionResult);
      } else {
         CollisionModule.findBlockCollisionsIterative(world, boundingBox, position, scaledVelocity, true, collisionResult);
      }

      BlockCollisionData blockCollisionData = collisionResult.getFirstBlockCollision();
      if (blockCollisionData != null) {
         if (blockCollisionData.collisionNormal.equals(Vector3d.UP)) {
            velocityComponent.setZero();
            position.assign(blockCollisionData.collisionPoint);
         } else {
            Vector3d velocity = velocityComponent.getVelocity();
            double dot = velocity.dot(blockCollisionData.collisionNormal);
            Vector3d velocityToCancel = blockCollisionData.collisionNormal.clone().scale(dot);
            velocity.subtract(velocityToCancel);
         }
      } else {
         velocityComponent.assignVelocityTo(scaledVelocity).scale(dt);
         position.add(scaledVelocity);
      }

      collisionResult.reset();
      if (position.getY() < -32.0) {
         LOGGER.at(Level.WARNING).log("Item fell out of the world %s", archetypeChunk.getReferenceTo(index));
         commandBuffer.removeEntity(archetypeChunk.getReferenceTo(index), RemoveReason.REMOVE);
      }
   }
}
