package com.hypixel.hytale.server.core.plugin;

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
