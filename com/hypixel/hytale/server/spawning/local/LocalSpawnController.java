package com.hypixel.hytale.server.spawning.local;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.spawning.SpawningPlugin;
import javax.annotation.Nonnull;

public class LocalSpawnController implements Component<EntityStore> {
   private double timeToNextRunSeconds = SpawningPlugin.get().getLocalSpawnControllerJoinDelay();

   public static ComponentType<EntityStore, LocalSpawnController> getComponentType() {
      return SpawningPlugin.get().getLocalSpawnControllerComponentType();
   }

   public void setTimeToNextRunSeconds(double seconds) {
      this.timeToNextRunSeconds = seconds;
   }

   public boolean tickTimeToNextRunSeconds(float dt) {
      return (this.timeToNextRunSeconds -= dt) <= 0.0;
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      LocalSpawnController controller = new LocalSpawnController();
      controller.timeToNextRunSeconds = this.timeToNextRunSeconds;
      return controller;
   }

   @Nonnull
   @Override
   public String toString() {
      return "LocalSpawnController{timeToNextRunSeconds=" + this.timeToNextRunSeconds + "}";
   }
}
