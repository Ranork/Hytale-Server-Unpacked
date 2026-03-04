package com.hypixel.hytale.server.core.modules.debug;

import com.hypixel.hytale.common.plugin.PluginManifest;
import com.hypixel.hytale.server.core.modules.debug.commands.DebugCommand;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DebugPlugin extends JavaPlugin {
   @Nonnull
   public static final PluginManifest MANIFEST = PluginManifest.corePlugin(DebugPlugin.class).build();
   @Nullable
   private static DebugPlugin instance;

   @Nullable
   public static DebugPlugin get() {
      return instance;
   }

   public DebugPlugin(@Nonnull JavaPluginInit init) {
      super(init);
      instance = this;
   }

   @Override
   protected void setup() {
      this.getCommandRegistry().registerCommand(new DebugCommand());
   }
}
