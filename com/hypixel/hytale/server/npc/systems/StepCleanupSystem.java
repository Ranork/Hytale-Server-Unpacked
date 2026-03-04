package com.hypixel.hytale.server.npc.systems;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.RootDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.components.StepComponent;
import java.util.Set;
import javax.annotation.Nonnull;

public class StepCleanupSystem extends EntityTickingSystem<EntityStore> {
   @Nonnull
   private final ComponentType<EntityStore, StepComponent> stepComponentType;
   @Nonnull
   private final Set<Dependency<EntityStore>> dependencies = RootDependency.lastSet();

   public StepCleanupSystem(@Nonnull ComponentType<EntityStore, StepComponent> stepComponentType) {
      this.stepComponentType = stepComponentType;
   }

   @Nonnull
   @Override
   public Set<Dependency<EntityStore>> getDependencies() {
      return this.dependencies;
   }

   @Override
   public void tick(
      float dt,
      int index,
      @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
      @Nonnull Store<EntityStore> store,
      @Nonnull CommandBuffer<EntityStore> commandBuffer
   ) {
      commandBuffer.removeComponent(archetypeChunk.getReferenceTo(index), this.stepComponentType);
   }

   @Nonnull
   @Override
   public Query<EntityStore> getQuery() {
      return this.stepComponentType;
   }
}
