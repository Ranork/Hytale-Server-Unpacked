package com.hypixel.hytale.server.worldgen.loader.climate;

import com.google.gson.JsonElement;
import com.hypixel.hytale.procedurallib.json.JsonLoader;
import com.hypixel.hytale.procedurallib.json.NoisePropertyJsonLoader;
import com.hypixel.hytale.procedurallib.json.SeedResource;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.procedurallib.property.NoiseProperty;
import com.hypixel.hytale.server.worldgen.climate.ClimateNoise;
import java.nio.file.Path;
import javax.annotation.Nonnull;

public class ClimateNoiseJsonLoader<K extends SeedResource> extends JsonLoader<K, ClimateNoise> {
   public ClimateNoiseJsonLoader(SeedString<K> seed, Path dataFolder, JsonElement json) {
      super(seed, dataFolder, json);
   }

   @Nonnull
   public ClimateNoise load() {
      return new ClimateNoise(this.loadGrid(), this.loadContinentNoise(), this.loadTemperatureNoise(), this.loadIntensityNoise(), this.loadThresholds());
   }

   @Nonnull
   protected ClimateNoise.Grid loadGrid() {
      return new ClimateGridJsonLoader<>(this.seed, this.dataFolder, this.get("Grid")).load();
   }

   @Nonnull
   protected NoiseProperty loadContinentNoise() {
      return new NoisePropertyJsonLoader<>(this.seed, this.dataFolder, this.mustGetObject("Continent", null)).load();
   }

   @Nonnull
   protected NoiseProperty loadTemperatureNoise() {
      return new NoisePropertyJsonLoader<>(this.seed, this.dataFolder, this.mustGetObject("Temperature", null)).load();
   }

   @Nonnull
   protected NoiseProperty loadIntensityNoise() {
      return new NoisePropertyJsonLoader<>(this.seed, this.dataFolder, this.mustGetObject("Intensity", null)).load();
   }

   @Nonnull
   protected ClimateNoise.Thresholds loadThresholds() {
      return new ContinentThresholdsJsonLoader<>(this.seed, this.dataFolder, this.mustGetObject("Thresholds", null)).load();
   }

   public interface Constants {
      String KEY_NOISE = "Noise";
      String KEY_GRID = "Grid";
      String KEY_THRESHOLDS = "Thresholds";
      String KEY_CONTINENT = "Continent";
   }
}
