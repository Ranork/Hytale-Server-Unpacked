package com.hypixel.hytale.procedurallib.json;

import com.google.gson.JsonElement;
import com.hypixel.hytale.procedurallib.condition.IHeightThresholdInterpreter;
import java.nio.file.Path;
import javax.annotation.Nonnull;

public class HeightThresholdInterpreterJsonLoader<K extends SeedResource> extends JsonLoader<K, IHeightThresholdInterpreter> {
   protected final int length;

   public HeightThresholdInterpreterJsonLoader(@Nonnull SeedString<K> seed, Path dataFolder, JsonElement json, int length) {
      super(seed.append(".HeightThresholdInterpreter"), dataFolder, json);
      this.length = length;
   }

   @Nonnull
   public IHeightThresholdInterpreter load() {
      return (IHeightThresholdInterpreter)(NoiseHeightThresholdInterpreterJsonLoader.shouldHandle(this.json.getAsJsonObject())
         ? new NoiseHeightThresholdInterpreterJsonLoader<>(this.seed, this.dataFolder, this.json, this.length).load()
         : new BasicHeightThresholdInterpreterJsonLoader<>(this.seed, this.dataFolder, this.json, this.length).load());
   }
}
