package com.hypixel.hytale.server.core.plugin.event;

import com.hypixel.hytale.server.core.plugin.PluginBase;
import javax.annotation.Nonnull;

public class PluginSetupEvent extends PluginEvent {
   public PluginSetupEvent(@Nonnull PluginBase plugin) {
      super(plugin);
   }
}
