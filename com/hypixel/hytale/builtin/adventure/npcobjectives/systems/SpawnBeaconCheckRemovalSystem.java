package com.hypixel.hytale.builtin.adventure.npcobjectives.systems;

import com.hypixel.hytale.builtin.adventure.objectives.ObjectivePlugin;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.HolderSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.spawning.beacons.LegacySpawnBeaconEntity;
import java.util.UUID;
import javax.annotation.Nonnull;

public class SpawnBeaconCheckRemovalSystem extends HolderSystem<EntityStore> {
   @Nonnull
   private final ComponentType<EntityStore, LegacySpawnBeaconEntity> legacySpawnBeaconEntityComponentType;

   public SpawnBeaconCheckRemovalSystem(@Nonnull ComponentType<EntityStore, LegacySpawnBeaconEntity> legacySpawnBeaconEntityComponentType) {
      this.legacySpawnBeaconEntityComponentType = legacySpawnBeaconEntityComponentType;
   }

   @Nonnull
   @Override
   public Query<EntityStore> getQuery() {
      return this.legacySpawnBeaconEntityComponentType;
   }

   @Override
   public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
      LegacySpawnBeaconEntity spawnBeaconComponent = holder.getComponent(this.legacySpawnBeaconEntityComponentType);

      assert spawnBeaconComponent != null;

      UUID objectiveUUID = spawnBeaconComponent.getObjectiveUUID();
      if (objectiveUUID != null && ObjectivePlugin.get().getObjectiveDataStore().getObjective(objectiveUUID) == null) {
         spawnBeaconComponent.remove();
      }
   }

   @Override
   public void onEntityRemoved(@Nonnull Holder<EntityStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store) {
   }
}
