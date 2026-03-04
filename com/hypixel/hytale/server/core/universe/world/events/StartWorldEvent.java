package com.hypixel.hytale.server.core.universe.world.events;

import com.hypixel.hytale.server.core.universe.world.World;
import javax.annotation.Nonnull;

public class StartWorldEvent extends WorldEvent {
   public StartWorldEvent(@Nonnull World world) {
      super(world);
   }

   @Nonnull
   @Override
   public String toString() {
      return "StartWorldEvent{} " + super.toString();
   }
}
