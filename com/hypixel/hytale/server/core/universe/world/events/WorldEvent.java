package com.hypixel.hytale.server.core.universe.world.events;

import com.hypixel.hytale.event.IEvent;
import com.hypixel.hytale.server.core.universe.world.World;
import javax.annotation.Nonnull;

public abstract class WorldEvent implements IEvent<String> {
   @Nonnull
   private final World world;

   public WorldEvent(@Nonnull World world) {
      this.world = world;
   }

   @Nonnull
   public World getWorld() {
      return this.world;
   }

   @Nonnull
   @Override
   public String toString() {
      return "WorldEvent{world=" + this.world + "}";
   }
}
