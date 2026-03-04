package com.hypixel.hytale.server.core.universe.world.events;

import com.hypixel.hytale.event.IEvent;
import javax.annotation.Nonnull;

public class AllWorldsLoadedEvent implements IEvent<Void> {
   @Nonnull
   @Override
   public String toString() {
      return "AllWorldsLoadedEvent{}";
   }
}
