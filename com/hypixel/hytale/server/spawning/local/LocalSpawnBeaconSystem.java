package com.hypixel.hytale.server.spawning.local;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.spawning.SpawningPlugin;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class LocalSpawnBeaconSystem extends RefSystem<EntityStore> {
   private final ComponentType<EntityStore, LocalSpawnBeacon> componentType;
   private final ResourceType<EntityStore, LocalSpawnState> localSpawnStateResourceType;

   public LocalSpawnBeaconSystem(
      ComponentType<EntityStore, LocalSpawnBeacon> componentType, ResourceType<EntityStore, LocalSpawnState> localSpawnStateResourceType
   ) {
      this.componentType = componentType;
      this.localSpawnStateResourceType = localSpawnStateResourceType;
   }

   @Override
   public void onEntityAdded(
      @Nonnull Ref<EntityStore> reference, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
   ) {
   }

   @Override
   public void onEntityRemove(
      @Nonnull Ref<EntityStore> reference, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
   ) {
      SpawningPlugin.get().getLogger().at(Level.FINE).log("Triggering forced rerun of local spawn controllers");
      store.getResource(this.localSpawnStateResourceType).forceTriggerControllers();
   }

   @Override
   public Query<EntityStore> getQuery() {
      return this.componentType;
   }
}
