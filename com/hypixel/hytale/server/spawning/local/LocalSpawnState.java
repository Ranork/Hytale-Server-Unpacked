package com.hypixel.hytale.server.spawning.local;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Resource;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.spawning.SpawningPlugin;
import com.hypixel.hytale.server.spawning.beacons.LegacySpawnBeaconEntity;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import javax.annotation.Nonnull;

public class LocalSpawnState implements Resource<EntityStore> {
   private final List<Ref<EntityStore>> localControllerList = new ObjectArrayList();
   private final List<LegacySpawnBeaconEntity> localPendingSpawns = new ObjectArrayList();
   private boolean forceTriggerControllers;

   public static ResourceType<EntityStore, LocalSpawnState> getResourceType() {
      return SpawningPlugin.get().getLocalSpawnStateResourceType();
   }

   @Nonnull
   public List<Ref<EntityStore>> getLocalControllerList() {
      return this.localControllerList;
   }

   @Nonnull
   public List<LegacySpawnBeaconEntity> getLocalPendingSpawns() {
      return this.localPendingSpawns;
   }

   public boolean pollForceTriggerControllers() {
      boolean result = this.forceTriggerControllers;
      this.forceTriggerControllers = false;
      return result;
   }

   public void forceTriggerControllers() {
      this.forceTriggerControllers = true;
   }

   @Nonnull
   @Override
   public Resource<EntityStore> clone() {
      LocalSpawnState state = new LocalSpawnState();
      state.localControllerList.addAll(this.localControllerList);
      state.localPendingSpawns.addAll(this.localPendingSpawns);
      return state;
   }
}
