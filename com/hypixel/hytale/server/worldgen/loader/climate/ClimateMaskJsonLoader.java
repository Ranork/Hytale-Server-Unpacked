package com.hypixel.hytale.server.worldgen.loader.climate;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hypixel.hytale.procedurallib.file.FileIO;
import com.hypixel.hytale.procedurallib.json.CoordinateRandomizerJsonLoader;
import com.hypixel.hytale.procedurallib.json.JsonLoader;
import com.hypixel.hytale.procedurallib.json.SeedResource;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.procedurallib.random.CoordinateRandomizer;
import com.hypixel.hytale.procedurallib.random.ICoordinateRandomizer;
import com.hypixel.hytale.server.worldgen.climate.ClimateGraph;
import com.hypixel.hytale.server.worldgen.climate.ClimateMaskProvider;
import com.hypixel.hytale.server.worldgen.climate.ClimateNoise;
import com.hypixel.hytale.server.worldgen.climate.UniqueClimateGenerator;
import java.io.IOException;
import java.nio.file.Path;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ClimateMaskJsonLoader<K extends SeedResource> extends JsonLoader<K, ClimateMaskProvider> {
   public ClimateMaskJsonLoader(SeedString<K> seed, Path dataFolder, Path maskFile) {
      super(seed, dataFolder, loadMaskFileJson(maskFile));
   }

   @Nullable
   public ClimateMaskProvider load() {
      return new ClimateMaskProvider(this.loadRandomizer(), this.loadClimateNoise(), this.loadClimateGraph(), this.loadUniqueClimateGenerator());
   }

   @Nonnull
   protected ICoordinateRandomizer loadRandomizer() {
      return this.has("Randomizer")
         ? new CoordinateRandomizerJsonLoader<>(this.seed, this.dataFolder, this.get("Randomizer")).load()
         : CoordinateRandomizer.EMPTY_RANDOMIZER;
   }

   @Nonnull
   protected ClimateNoise loadClimateNoise() {
      return new ClimateNoiseJsonLoader<>(this.seed, this.dataFolder, this.mustGetObject("Noise", null)).load();
   }

   @Nonnull
   protected ClimateGraph loadClimateGraph() {
      return new ClimateGraphJsonLoader<>(this.seed, this.dataFolder, this.mustGetObject("Climate", null)).load();
   }

   @Nonnull
   protected UniqueClimateGenerator loadUniqueClimateGenerator() {
      return new UniqueClimateGeneratorJsonLoader<>(
            this.seed, this.dataFolder, this.mustGetArray("UniqueZones", ClimateMaskJsonLoader.Constants.DEFAULT_UNIQUE)
         )
         .load();
   }

   protected static JsonObject loadMaskFileJson(Path file) {
      try {
         return FileIO.load(file, JsonLoader.JSON_OBJ_LOADER);
      } catch (IOException var2) {
         throw new Error("Failed to load Mask.json", var2);
      }
   }

   public interface Constants {
      String KEY_RANDOMIZER = "Randomizer";
      String KEY_UNIQUE_ZONES = "UniqueZones";
      JsonArray DEFAULT_UNIQUE = new JsonArray();
   }
}
