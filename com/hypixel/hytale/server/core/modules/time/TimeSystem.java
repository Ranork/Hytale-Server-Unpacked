package com.hypixel.hytale.server.core.modules.time;

import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.tick.TickingSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.time.temporal.ChronoUnit;
import javax.annotation.Nonnull;

public class TimeSystem extends TickingSystem<EntityStore> {
   @Nonnull
   private final ResourceType<EntityStore, TimeResource> timeResourceType;

   public TimeSystem(@Nonnull ResourceType<EntityStore, TimeResource> timeResourceType) {
      this.timeResourceType = timeResourceType;
   }

   @Override
   public void tick(float dt, int systemIndex, @Nonnull Store<EntityStore> store) {
      long nanos = (long)(1.0E9F * dt);
      store.getResource(this.timeResourceType).add(nanos, ChronoUnit.NANOS);
   }
}
