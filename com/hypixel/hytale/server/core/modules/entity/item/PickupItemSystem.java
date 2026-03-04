package com.hypixel.hytale.server.core.modules.entity.item;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class PickupItemSystem extends EntityTickingSystem<EntityStore> {
   private static final float EYE_HEIGHT_SCALE = 5.0F;
   @Nonnull
   private final ComponentType<EntityStore, PickupItemComponent> pickupItemComponentType;
   @Nonnull
   private final ComponentType<EntityStore, TransformComponent> transformComponentType;
   @Nonnull
   private final Query<EntityStore> query;

   public PickupItemSystem(
      @Nonnull ComponentType<EntityStore, PickupItemComponent> pickupItemComponentType,
      @Nonnull ComponentType<EntityStore, TransformComponent> transformComponentType
   ) {
      this.pickupItemComponentType = pickupItemComponentType;
      this.transformComponentType = transformComponentType;
      this.query = Query.and(pickupItemComponentType, transformComponentType);
   }

   @Override
   public void tick(
      float dt,
      int index,
      @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
      @Nonnull Store<EntityStore> store,
      @Nonnull CommandBuffer<EntityStore> commandBuffer
   ) {
      PickupItemComponent pickupItemComponent = archetypeChunk.getComponent(index, this.pickupItemComponentType);

      assert pickupItemComponent != null;

      if (pickupItemComponent.hasFinished()) {
         commandBuffer.removeEntity(archetypeChunk.getReferenceTo(index), RemoveReason.REMOVE);
      } else {
         Ref<EntityStore> targetRef = pickupItemComponent.getTargetRef();
         if (targetRef != null && targetRef.isValid()) {
            TransformComponent transformComponent = archetypeChunk.getComponent(index, this.transformComponentType);

            assert transformComponent != null;

            Vector3d position = transformComponent.getPosition();
            TransformComponent targetTransformComponent = commandBuffer.getComponent(targetRef, this.transformComponentType);
            if (targetTransformComponent != null) {
               Vector3d targetPosition = targetTransformComponent.getPosition().clone();
               ModelComponent targetModelComponent = commandBuffer.getComponent(targetRef, ModelComponent.getComponentType());
               if (targetModelComponent != null) {
                  float targetModelEyeHeight = targetModelComponent.getModel().getEyeHeight(targetRef, commandBuffer);
                  targetPosition.add(0.0, targetModelEyeHeight / 5.0F, 0.0);
               }

               if (updateMovement(pickupItemComponent, position, targetPosition, dt)) {
                  pickupItemComponent.setFinished(true);
               }
            }
         } else {
            commandBuffer.removeEntity(archetypeChunk.getReferenceTo(index), RemoveReason.REMOVE);
         }
      }
   }

   private static boolean updateMovement(@Nonnull PickupItemComponent pickupItemComponent, @Nonnull Vector3d current, @Nonnull Vector3d target, float dt) {
      float remainingTime = pickupItemComponent.getLifeTime();
      float originalLifeTime = pickupItemComponent.getOriginalLifeTime();
      float progress = 1.0F - remainingTime / originalLifeTime;
      if (progress >= 1.0F) {
         current.assign(target);
         return true;
      } else {
         current.assign(Vector3d.lerp(pickupItemComponent.getStartPosition(), target, progress));
         pickupItemComponent.decreaseLifetime(dt);
         return false;
      }
   }

   @Nonnull
   @Override
   public Query<EntityStore> getQuery() {
      return this.query;
   }
}
