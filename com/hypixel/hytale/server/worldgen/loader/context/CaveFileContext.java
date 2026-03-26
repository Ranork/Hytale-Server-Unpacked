package com.hypixel.hytale.server.worldgen.loader.context;

import java.nio.file.Path;
import javax.annotation.Nonnull;

public class CaveFileContext extends FileContext<ZoneFileContext> {
   public CaveFileContext(@Nonnull String name, @Nonnull ZoneFileContext parentContext) {
      super(0, name, resolvePath(parentContext, name), parentContext);
   }

   public CaveFileContext(@Nonnull String name, @Nonnull Path relativePath, @Nonnull ZoneFileContext parentContext) {
      super(0, name, relativePath, parentContext);
   }

   private static Path resolvePath(@Nonnull ZoneFileContext context, @Nonnull String name) {
      String filepath = name.replace(".", context.getPath().getFileSystem().getSeparator());
      return context.getPath().resolve("Cave").resolve(filepath);
   }
}
