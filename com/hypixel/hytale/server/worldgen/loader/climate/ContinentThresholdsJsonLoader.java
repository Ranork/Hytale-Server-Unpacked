package com.hypixel.hytale.server.worldgen.loader.climate;

import com.google.gson.JsonElement;
import com.hypixel.hytale.procedurallib.json.JsonLoader;
import com.hypixel.hytale.procedurallib.json.SeedResource;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.server.worldgen.climate.ClimateNoise;
import java.nio.file.Path;
import javax.annotation.Nonnull;

public class ContinentThresholdsJsonLoader<K extends SeedResource> extends JsonLoader<K, ClimateNoise.Thresholds> {
   public ContinentThresholdsJsonLoader(SeedString<K> seed, Path dataFolder, JsonElement json) {
      super(seed, dataFolder, json);
   }

   @Nonnull
   public ClimateNoise.Thresholds load() {
      return new ClimateNoise.Thresholds(this.loadLandThreshold(), this.loadIslandThreshold(), this.loadBeachSize(), this.loadShallowOceanSize());
   }

   protected double loadLandThreshold() {
      return this.mustGetNumber("Land", ContinentThresholdsJsonLoader.Constants.DEFAULT_LAND).doubleValue();
   }

   protected double loadIslandThreshold() {
      return this.mustGetNumber("Island", ContinentThresholdsJsonLoader.Constants.DEFAULT_ISLAND).doubleValue();
   }

   protected double loadBeachSize() {
      return this.mustGetNumber("BeachSize", ContinentThresholdsJsonLoader.Constants.DEFAULT_BEACH_SIZE).doubleValue();
   }

   protected double loadShallowOceanSize() {
      return this.mustGetNumber("ShallowOceanSize", ContinentThresholdsJsonLoader.Constants.DEFAULT_SHALLOW_OCEAN_SIZE).doubleValue();
   }

   public interface Constants {
      String KEY_LAND = "Land";
      String KEY_ISLAND = "Island";
      String KEY_BEACH_SIZE = "BeachSize";
      String KEY_SHALLOW_OCEAN_SIZE = "ShallowOceanSize";
      Double DEFAULT_LAND = 0.5;
      Double DEFAULT_ISLAND = 0.8;
      Double DEFAULT_BEACH_SIZE = 0.05;
      Double DEFAULT_SHALLOW_OCEAN_SIZE = 0.15;
   }
}
