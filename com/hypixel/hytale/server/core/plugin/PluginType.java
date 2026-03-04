package com.hypixel.hytale.server.core.plugin;

import javax.annotation.Nonnull;

public enum PluginType {
   PLUGIN("Plugin");

   @Nonnull
   private final String displayName;

   private PluginType(@Nonnull final String displayName) {
      this.displayName = displayName;
   }

   @Nonnull
   public String getDisplayName() {
      return this.displayName;
   }
}
