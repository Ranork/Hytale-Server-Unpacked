package com.hypixel.hytale.server.core.event.events;

import com.hypixel.hytale.event.IEvent;
import javax.annotation.Nonnull;

public class BootEvent implements IEvent<Void> {
   @Nonnull
   @Override
   public String toString() {
      return "BootEvent{}";
   }
}
