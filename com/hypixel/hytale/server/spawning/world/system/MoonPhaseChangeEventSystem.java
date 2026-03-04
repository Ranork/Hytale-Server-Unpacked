package com.hypixel.hytale.server.spawning.world.system;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.WorldEventSystem;
import com.hypixel.hytale.server.core.universe.world.events.ecs.MoonPhaseChangeEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.spawning.world.component.WorldSpawnData;
import javax.annotation.Nonnull;

public class MoonPhaseChangeEventSystem extends WorldEventSystem<EntityStore, MoonPhaseChangeEvent> {
   public MoonPhaseChangeEventSystem() {
      super(MoonPhaseChangeEvent.class);
   }

   public void handle(@Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull MoonPhaseChangeEvent event) {
      WorldSpawnData worldSpawnDataResource = commandBuffer.getResource(WorldSpawnData.getResourceType());
      worldSpawnDataResource.forEachEnvironmentSpawnData(worldEnvironmentSpawnData -> worldEnvironmentSpawnData.recalculateWeight(event.getNewMoonPhase()));
   }
}
