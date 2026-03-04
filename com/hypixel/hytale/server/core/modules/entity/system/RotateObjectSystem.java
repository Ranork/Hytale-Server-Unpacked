package com.hypixel.hytale.server.core.modules.entity.system;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.modules.entity.component.RotateObjectComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class RotateObjectSystem extends EntityTickingSystem<EntityStore> {
   @Nonnull
   private final ComponentType<EntityStore, TransformComponent> transformComponentType;
   @Nonnull
   private final ComponentType<EntityStore, RotateObjectComponent> rotateObjectComponentType;

   public RotateObjectSystem(
      @Nonnull ComponentType<EntityStore, TransformComponent> transformComponentType,
      @Nonnull ComponentType<EntityStore, RotateObjectComponent> rotateObjectComponentType
   ) {
      this.transformComponentType = transformComponentType;
      this.rotateObjectComponentType = rotateObjectComponentType;
   }

   @Override
   public Query<EntityStore> getQuery() {
      return Query.and(this.rotateObjectComponentType, this.transformComponentType);
   }

   @Override
   public void tick(
      float dt,
      int index,
      @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
      @Nonnull Store<EntityStore> store,
      @Nonnull CommandBuffer<EntityStore> commandBuffer
   ) {
      RotateObjectComponent rotateObjectComponent = archetypeChunk.getComponent(index, this.rotateObjectComponentType);

      assert rotateObjectComponent != null;

      TransformComponent transformComponent = archetypeChunk.getComponent(index, this.transformComponentType);

      assert transformComponent != null;

      Vector3f rotation = transformComponent.getRotation();
      rotation.y = rotation.y + rotateObjectComponent.getRotationSpeed() * dt;
      if (rotation.y >= 360.0F) {
         rotation.y %= 360.0F;
      }

      transformComponent.setRotation(rotation);
   }
}
