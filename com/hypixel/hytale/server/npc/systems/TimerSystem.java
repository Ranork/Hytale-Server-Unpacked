package com.hypixel.hytale.server.npc.systems;

import com.hypixel.hytale.common.thread.ticking.Tickable;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.components.Timers;
import java.util.Set;
import javax.annotation.Nonnull;

public class TimerSystem extends SteppableTickingSystem {
   @Nonnull
   private final ComponentType<EntityStore, Timers> timersComponentType;
   @Nonnull
   private final Set<Dependency<EntityStore>> dependencies;

   public TimerSystem(@Nonnull ComponentType<EntityStore, Timers> timersComponentType, @Nonnull Set<Dependency<EntityStore>> dependencies) {
      this.timersComponentType = timersComponentType;
      this.dependencies = dependencies;
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

   @Nonnull
   @Override
   public Query<EntityStore> getQuery() {
      return this.timersComponentType;
   }

   @Override
   public void steppedTick(
      float dt,
      int index,
      @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
      @Nonnull Store<EntityStore> store,
      @Nonnull CommandBuffer<EntityStore> commandBuffer
   ) {
      Timers timersComponent = archetypeChunk.getComponent(index, this.timersComponentType);

      assert timersComponent != null;

      for (Tickable timer : timersComponent.getTimers()) {
         timer.tick(dt);
      }
   }
}
