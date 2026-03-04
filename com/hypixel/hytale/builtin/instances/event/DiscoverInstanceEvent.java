package com.hypixel.hytale.builtin.instances.event;

import com.hypixel.hytale.builtin.instances.config.InstanceDiscoveryConfig;
import com.hypixel.hytale.component.system.EcsEvent;
import com.hypixel.hytale.component.system.ICancellableEcsEvent;
import java.util.UUID;
import javax.annotation.Nonnull;

public abstract class DiscoverInstanceEvent extends EcsEvent {
   @Nonnull
   private final UUID instanceWorldUuid;
   @Nonnull
   private final InstanceDiscoveryConfig discoveryConfig;

   public DiscoverInstanceEvent(@Nonnull UUID instanceWorldUuid, @Nonnull InstanceDiscoveryConfig discoveryConfig) {
      this.instanceWorldUuid = instanceWorldUuid;
      this.discoveryConfig = discoveryConfig;
   }

   @Nonnull
   public UUID getInstanceWorldUuid() {
      return this.instanceWorldUuid;
   }

   @Nonnull
   public InstanceDiscoveryConfig getDiscoveryConfig() {
      return this.discoveryConfig;
   }

   public static class Display extends DiscoverInstanceEvent implements ICancellableEcsEvent {
      private boolean cancelled = false;
      private boolean display;

      public Display(@Nonnull UUID instanceWorldUuid, @Nonnull InstanceDiscoveryConfig discoveryConfig) {
         super(instanceWorldUuid, discoveryConfig);
         this.display = discoveryConfig.isDisplay();
      }

      @Override
      public boolean isCancelled() {
         return this.cancelled;
      }

      @Override
      public void setCancelled(boolean cancelled) {
         this.cancelled = cancelled;
      }

      public boolean shouldDisplay() {
         return this.display;
      }

      public void setDisplay(boolean display) {
         this.display = display;
      }
   }
}
