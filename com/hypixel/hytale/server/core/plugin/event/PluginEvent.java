package com.hypixel.hytale.server.core.plugin.event;

import com.hypixel.hytale.event.IEvent;
import com.hypixel.hytale.server.core.plugin.PluginBase;
import javax.annotation.Nonnull;

public abstract class PluginEvent implements IEvent<Class<? extends PluginBase>> {
   @Nonnull
   private final PluginBase plugin;

   public PluginEvent(@Nonnull PluginBase plugin) {
      this.plugin = plugin;
   }

   @Nonnull
   public PluginBase getPlugin() {
      return this.plugin;
   }

   @Nonnull
   @Override
   public String toString() {
      return "PluginEvent{}";
   }
}
