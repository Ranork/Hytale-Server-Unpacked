package com.hypixel.hytale.procedurallib.json;

import java.nio.file.Path;
import javax.annotation.Nullable;

public abstract class Loader<K extends SeedResource, T> {
   protected SeedString<K> seed;
   protected final Path dataFolder;

   public Loader(SeedString<K> seed, Path dataFolder) {
      this.seed = seed;
      this.dataFolder = dataFolder;
   }

   public SeedString<K> getSeed() {
      return this.seed;
   }

   public Path getDataFolder() {
      return this.dataFolder;
   }

   @Nullable
   public abstract T load();
}
