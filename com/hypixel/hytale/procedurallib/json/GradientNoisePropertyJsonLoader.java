package com.hypixel.hytale.procedurallib.json;

import com.google.gson.JsonElement;
import com.hypixel.hytale.procedurallib.property.GradientNoiseProperty;
import com.hypixel.hytale.procedurallib.property.NoiseProperty;
import java.nio.file.Path;
import javax.annotation.Nonnull;

public class GradientNoisePropertyJsonLoader<K extends SeedResource> extends JsonLoader<K, GradientNoiseProperty> {
   protected final NoiseProperty noise;

   public GradientNoisePropertyJsonLoader(SeedString<K> seed, Path dataFolder, JsonElement json, NoiseProperty noise) {
      super(seed, dataFolder, json);
      this.noise = noise;
   }

   @Nonnull
   public GradientNoiseProperty load() {
      return new GradientNoiseProperty(this.noise, this.loadMode(), this.loadDistance(), this.loadNormalization());
   }

   @Nonnull
   protected GradientNoiseProperty.GradientMode loadMode() {
      GradientNoiseProperty.GradientMode mode = GradientNoisePropertyJsonLoader.Constants.DEFAULT_MODE;
      if (this.has("Mode")) {
         mode = GradientNoiseProperty.GradientMode.valueOf(this.get("Mode").getAsString());
      }

      return mode;
   }

   protected double loadDistance() {
      double distance = 5.0;
      if (this.has("Distance")) {
         distance = this.get("Distance").getAsDouble();
      }

      return distance;
   }

   protected double loadNormalization() {
      double distance = 0.1;
      if (this.has("Normalize")) {
         distance = this.get("Normalize").getAsDouble();
      }

      return distance;
   }

   public interface Constants {
      String KEY_MODE = "Mode";
      String KEY_DISTANCE = "Distance";
      String KEY_NORMALIZE = "Normalize";
      GradientNoiseProperty.GradientMode DEFAULT_MODE = GradientNoiseProperty.GradientMode.MAGNITUDE;
      double DEFAULT_DISTANCE = 5.0;
      double DEFAULT_NORMALIZATION = 0.1;
   }
}
