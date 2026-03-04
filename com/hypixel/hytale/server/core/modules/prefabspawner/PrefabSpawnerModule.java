package com.hypixel.hytale.server.core.modules.prefabspawner;

import com.hypixel.hytale.common.plugin.PluginManifest;
import com.hypixel.hytale.server.core.modules.prefabspawner.commands.PrefabSpawnerCommand;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.meta.BlockStateModule;
import javax.annotation.Nonnull;

public class PrefabSpawnerModule extends JavaPlugin {
   @Nonnull
   public static final PluginManifest MANIFEST = PluginManifest.corePlugin(PrefabSpawnerModule.class).depends(BlockStateModule.class).build();

   public PrefabSpawnerModule(@Nonnull JavaPluginInit init) {
      super(init);
   }

   @Override
   protected void setup() {
      this.getBlockStateRegistry().registerBlockState(PrefabSpawnerState.class, "prefabspawner", PrefabSpawnerState.CODEC);
      this.getCommandRegistry().registerCommand(new PrefabSpawnerCommand());
   }
}
