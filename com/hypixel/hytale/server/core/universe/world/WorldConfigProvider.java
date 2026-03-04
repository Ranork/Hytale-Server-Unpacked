package com.hypixel.hytale.server.core.universe.world;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;

public interface WorldConfigProvider {
   @Nonnull
   default CompletableFuture<WorldConfig> load(@Nonnull Path savePath, String name) {
      Path oldPath = savePath.resolve("config.bson");
      Path path = savePath.resolve("config.json");
      if (Files.exists(oldPath) && !Files.exists(path)) {
         try {
            Files.move(oldPath, path);
         } catch (IOException var6) {
         }
      }

      return WorldConfig.load(path);
   }

   @Nonnull
   default CompletableFuture<Void> save(@Nonnull Path savePath, WorldConfig config, World world) {
      return WorldConfig.save(savePath.resolve("config.json"), config);
   }

   public static class Default implements WorldConfigProvider {
   }
}
