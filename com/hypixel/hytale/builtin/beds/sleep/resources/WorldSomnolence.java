package com.hypixel.hytale.builtin.beds.sleep.resources;

import com.hypixel.hytale.builtin.beds.BedsPlugin;
import com.hypixel.hytale.component.Resource;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WorldSomnolence implements Resource<EntityStore> {
   @Nonnull
   private WorldSleep state = WorldSleep.Awake.INSTANCE;
   private long lastSleepNotification;

   public static ResourceType<EntityStore, WorldSomnolence> getResourceType() {
      return BedsPlugin.getInstance().getWorldSomnolenceResourceType();
   }

   @Nonnull
   public WorldSleep getState() {
      return this.state;
   }

   public void setState(@Nonnull WorldSleep state) {
      this.state = state;
   }

   public boolean useSleepNotificationCooldown(long now, long cooldownMs) {
      long elapsedMs = now - this.lastSleepNotification;
      boolean ready = elapsedMs >= cooldownMs;
      if (ready) {
         this.lastSleepNotification = now;
         return true;
      } else {
         return false;
      }
   }

   public void resetNotificationCooldown() {
      this.lastSleepNotification = 0L;
   }

   @Nullable
   @Override
   public Resource<EntityStore> clone() {
      WorldSomnolence clone = new WorldSomnolence();
      clone.state = this.state;
      return clone;
   }
}
