package com.hypixel.hytale.server.worldgen.loader.climate;

import com.google.gson.JsonArray;
import com.hypixel.hytale.procedurallib.json.JsonLoader;
import com.hypixel.hytale.procedurallib.json.SeedResource;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.server.worldgen.climate.UniqueClimateGenerator;
import java.nio.file.Path;
import javax.annotation.Nonnull;

public class UniqueClimateGeneratorJsonLoader<K extends SeedResource> extends JsonLoader<K, UniqueClimateGenerator> {
   @Nonnull
   private final JsonArray array;

   public UniqueClimateGeneratorJsonLoader(SeedString<K> seed, Path dataFolder, @Nonnull JsonArray json) {
      super(seed, dataFolder, json);
      this.array = json;
   }

   @Nonnull
   public UniqueClimateGenerator load() {
      return this.array.isEmpty() ? UniqueClimateGenerator.EMPTY : new UniqueClimateGenerator(this.loadEntries());
   }

   protected UniqueClimateGenerator.Entry[] loadEntries() {
      UniqueClimateGenerator.Entry[] entries = new UniqueClimateGenerator.Entry[this.array.size()];

      for (int i = 0; i < this.array.size(); i++) {
         entries[i] = new UniqueClimateJsonLoader<>(this.seed, this.dataFolder, this.array.get(i)).load();
      }

      return entries;
   }
}
