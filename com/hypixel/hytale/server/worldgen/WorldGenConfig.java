package com.hypixel.hytale.server.worldgen;

import com.hypixel.hytale.common.semver.Semver;
import com.hypixel.hytale.procedurallib.file.FileIO;
import java.nio.file.Path;
import javax.annotation.Nonnull;

public record WorldGenConfig(@Nonnull Path path, @Nonnull String name, @Nonnull Semver version) {
   public WorldGenConfig withOverride(@Nonnull Path path) {
      return FileIO.equals(this.path, path) ? this : new WorldGenConfig(path, this.name, this.version);
   }
}
