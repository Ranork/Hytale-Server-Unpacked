package com.hypixel.hytale.procedurallib.json;

import com.google.gson.JsonElement;
import com.hypixel.hytale.procedurallib.NoiseFunction;
import com.hypixel.hytale.procedurallib.logic.OldSimplexNoise;
import java.nio.file.Path;
import javax.annotation.Nonnull;

public class OldSimplexNoiseJsonLoader<K extends SeedResource> extends JsonLoader<K, NoiseFunction> {
   public OldSimplexNoiseJsonLoader(@Nonnull SeedString<K> seed, Path dataFolder, JsonElement json) {
      super(seed.append(".OldSimplexNoise"), dataFolder, json);
   }

   @Nonnull
   public NoiseFunction load() {
      return OldSimplexNoise.INSTANCE;
   }
}
