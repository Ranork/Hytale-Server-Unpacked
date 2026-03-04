package com.hypixel.hytale.server.spawning.beacons;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.math.random.RandomExtra;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.spawning.SpawningPlugin;
import javax.annotation.Nonnull;

public class InitialBeaconDelay implements Component<EntityStore> {
   private double loadTimeSpawnDelay;

   public static ComponentType<EntityStore, InitialBeaconDelay> getComponentType() {
      return SpawningPlugin.get().getInitialBeaconDelayComponentType();
   }

   public void setLoadTimeSpawnDelay(double loadTimeSpawnDelay) {
      this.loadTimeSpawnDelay = loadTimeSpawnDelay;
   }

   public boolean tickLoadTimeSpawnDelay(float dt) {
      return this.loadTimeSpawnDelay <= 0.0 ? true : (this.loadTimeSpawnDelay -= dt) <= 0.0;
   }

   public void setupInitialSpawnDelay(@Nonnull double[] initialSpawnDelay) {
      this.loadTimeSpawnDelay = RandomExtra.randomRange(initialSpawnDelay[0], initialSpawnDelay[1]);
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      InitialBeaconDelay delay = new InitialBeaconDelay();
      delay.setLoadTimeSpawnDelay(this.loadTimeSpawnDelay);
      return delay;
   }
}
