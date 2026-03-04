package com.hypixel.hytale.server.npc.systems;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import java.util.Set;
import javax.annotation.Nonnull;

public class ComputeVelocitySystem extends SteppableTickingSystem {
   @Nonnull
   private final ComponentType<EntityStore, NPCEntity> npcEntityComponentType;
   @Nonnull
   private final ComponentType<EntityStore, Velocity> velocityComponentType;
   @Nonnull
   private final ComponentType<EntityStore, TransformComponent> transformComponentType = TransformComponent.getComponentType();
   @Nonnull
   private final Set<Dependency<EntityStore>> dependencies;
   @Nonnull
   private final Query<EntityStore> query;

   public ComputeVelocitySystem(
      @Nonnull ComponentType<EntityStore, NPCEntity> npcEntityComponentType,
      @Nonnull ComponentType<EntityStore, Velocity> velocityComponentType,
      @Nonnull Set<Dependency<EntityStore>> dependencies
   ) {
      this.npcEntityComponentType = npcEntityComponentType;
      this.velocityComponentType = velocityComponentType;
      this.dependencies = dependencies;
      this.query = Query.and(npcEntityComponentType, this.transformComponentType, velocityComponentType);
   }

   @Nonnull
   @Override
   public Set<Dependency<EntityStore>> getDependencies() {
      return this.dependencies;
   }

   @Override
   public boolean isParallel(int archetypeChunkSize, int taskCount) {
      return EntityTickingSystem.maybeUseParallel(archetypeChunkSize, taskCount);
   }

   @Override
   public void steppedTick(
      float dt,
      int index,
      @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
      @Nonnull Store<EntityStore> store,
      @Nonnull CommandBuffer<EntityStore> commandBuffer
   ) {
      NPCEntity npcComponent = archetypeChunk.getComponent(index, this.npcEntityComponentType);

      assert npcComponent != null;

      TransformComponent transformComponent = archetypeChunk.getComponent(index, this.transformComponentType);

      assert transformComponent != null;

      Velocity velocityComponent = archetypeChunk.getComponent(index, this.velocityComponentType);

      assert velocityComponent != null;

      Vector3d position = transformComponent.getPosition();
      Vector3d oldPosition = npcComponent.getOldPosition();
      double x = (position.getX() - oldPosition.getX()) / dt;
      double y = (position.getY() - oldPosition.getY()) / dt;
      double z = (position.getZ() - oldPosition.getZ()) / dt;
      velocityComponent.set(x, y, z);
   }

   @Nonnull
   @Override
   public Query<EntityStore> getQuery() {
      return this.query;
   }
}
