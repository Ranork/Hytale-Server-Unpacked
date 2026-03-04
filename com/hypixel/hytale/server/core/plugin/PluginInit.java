package com.hypixel.hytale.server.core.plugin;

import com.hypixel.hytale.common.plugin.PluginManifest;
import java.nio.file.Path;
import javax.annotation.Nonnull;

public class PluginInit {
   @Nonnull
   private final PluginManifest pluginManifest;
   @Nonnull
   private final Path dataDirectory;

   public PluginInit(@Nonnull PluginManifest pluginManifest, @Nonnull Path dataDirectory) {
      this.pluginManifest = pluginManifest;
      this.dataDirectory = dataDirectory;
   }

   @Nonnull
   public PluginManifest getPluginManifest() {
      return this.pluginManifest;
   }

   @Nonnull
   public Path getDataDirectory() {
      return this.dataDirectory;
   }

   public boolean isInServerClassPath() {
      return true;
   }
}
