package com.hypixel.hytale.server.core.event.events.ecs;

import com.hypixel.hytale.component.system.EcsEvent;
import com.hypixel.hytale.component.system.ICancellableEcsEvent;
import com.hypixel.hytale.server.core.universe.world.WorldMapTracker;
import javax.annotation.Nonnull;

public abstract class DiscoverZoneEvent extends EcsEvent {
   @Nonnull
   private final WorldMapTracker.ZoneDiscoveryInfo discoveryInfo;

   public DiscoverZoneEvent(@Nonnull WorldMapTracker.ZoneDiscoveryInfo discoveryInfo) {
      this.discoveryInfo = discoveryInfo;
   }

   @Nonnull
   public WorldMapTracker.ZoneDiscoveryInfo getDiscoveryInfo() {
      return this.discoveryInfo;
   }

   public static class Display extends DiscoverZoneEvent implements ICancellableEcsEvent {
      private boolean cancelled = false;

      public Display(@Nonnull WorldMapTracker.ZoneDiscoveryInfo discoveryInfo) {
         super(discoveryInfo);
      }

      @Override
      public boolean isCancelled() {
         return this.cancelled;
      }

      @Override
      public void setCancelled(boolean cancelled) {
         this.cancelled = cancelled;
      }
   }
}
