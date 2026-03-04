package com.hypixel.hytale.server.worldgen.loader.climate;

import com.google.gson.JsonElement;
import com.hypixel.hytale.procedurallib.json.JsonLoader;
import com.hypixel.hytale.procedurallib.json.SeedResource;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.server.worldgen.climate.ClimatePoint;
import java.nio.file.Path;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ClimatePointJsonLoader<K extends SeedResource> extends JsonLoader<K, ClimatePoint> {
   public ClimatePointJsonLoader(SeedString<K> seed, Path dataFolder, @Nullable JsonElement json) {
      super(seed, dataFolder, json);
   }

   @Nonnull
   public ClimatePoint load() {
      return new ClimatePoint(this.loadTemperature(), this.loadIntensity(), this.loadModifier());
   }

   protected double loadTemperature() {
      return this.mustGetNumber("Temperature", ClimatePointJsonLoader.Constants.DEFAULT_TEMPERATURE).doubleValue();
   }

   protected double loadIntensity() {
      return this.mustGetNumber("Intensity", ClimatePointJsonLoader.Constants.DEFAULT_INTENSITY).doubleValue();
   }

   protected double loadModifier() {
      return this.mustGetNumber("Modifier", ClimatePointJsonLoader.Constants.DEFAULT_MODIFIER).doubleValue();
   }

   public interface Constants {
      String KEY_TEMPERATURE = "Temperature";
      String KEY_INTENSITY = "Intensity";
      String KEY_MODIFIER = "Modifier";
      Double DEFAULT_TEMPERATURE = 0.5;
      Double DEFAULT_INTENSITY = 0.5;
      Double DEFAULT_MODIFIER = 1.0;
   }
}
