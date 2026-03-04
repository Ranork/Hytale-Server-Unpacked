package com.hypixel.hytale.procedurallib.json;

import com.google.gson.JsonElement;
import com.hypixel.hytale.procedurallib.NoiseFunction;
import com.hypixel.hytale.procedurallib.logic.SimplexNoise;
import java.nio.file.Path;
import javax.annotation.Nonnull;

public class SimplexNoiseJsonLoader<K extends SeedResource> extends JsonLoader<K, NoiseFunction> {
   public SimplexNoiseJsonLoader(@Nonnull SeedString<K> seed, Path dataFolder, JsonElement json) {
      super(seed.append(".SimplexNoise"), dataFolder, json);
   }

   @Nonnull
   public NoiseFunction load() {
      return SimplexNoise.INSTANCE;
   }
}
