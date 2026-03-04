package com.hypixel.hytale.server.core.plugin;

import com.hypixel.hytale.common.plugin.PluginIdentifier;
import com.hypixel.hytale.server.core.asset.AssetModule;
import java.nio.file.Path;
import javax.annotation.Nonnull;

public abstract class JavaPlugin extends PluginBase {
   @Nonnull
   private final Path file;
   @Nonnull
   private final PluginClassLoader classLoader;

   public JavaPlugin(@Nonnull JavaPluginInit init) {
      super(init);
      this.file = init.getFile();
      this.classLoader = init.getClassLoader();
      this.classLoader.setPlugin(this);
   }

   @Nonnull
   public Path getFile() {
      return this.file;
   }

   @Override
   protected void setup0() {
      super.setup0();
      if (this.getManifest().includesAssetPack()) {
         AssetModule assetModule = AssetModule.get();
         String id = new PluginIdentifier(this.getManifest()).toString();
         assetModule.registerPack(id, this.file, this.getManifest(), true);
      }
   }

   @Nonnull
   public PluginClassLoader getClassLoader() {
      return this.classLoader;
   }

   @Nonnull
   @Override
   public final PluginType getType() {
      return PluginType.PLUGIN;
   }
}
