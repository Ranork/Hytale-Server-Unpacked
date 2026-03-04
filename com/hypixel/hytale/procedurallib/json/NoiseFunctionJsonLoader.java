package com.hypixel.hytale.procedurallib.json;

import com.google.gson.JsonElement;
import com.hypixel.hytale.procedurallib.NoiseFunction;
import java.nio.file.Path;
import javax.annotation.Nonnull;

public class NoiseFunctionJsonLoader<K extends SeedResource> extends JsonLoader<K, NoiseFunction> {
   public NoiseFunctionJsonLoader(@Nonnull SeedString<K> seed, Path dataFolder, JsonElement json) {
      super(seed.append(".NoiseFunction"), dataFolder, json);
   }

   public NoiseFunction load() {
      if (!this.has("NoiseType")) {
         throw new IllegalStateException(String.format("Could not find noise type for noise map! Keyword: %s", "NoiseType"));
      } else {
         NoiseTypeJson noiseTypeJson = NoiseTypeJson.valueOf(this.get("NoiseType").getAsString());
         return this.newLoader(noiseTypeJson).load();
      }
   }

   @Nonnull
   protected JsonLoader<K, NoiseFunction> newLoader(@Nonnull NoiseTypeJson noiseTypeJson) {
      return noiseTypeJson.newLoader(this.seed, this.dataFolder, this.json);
   }

   public interface Constants {
      String KEY_NOISE_TYPE = "NoiseType";
      String ERROR_NO_NOISE_TYPE = "Could not find noise type for noise map! Keyword: %s";
   }
}
