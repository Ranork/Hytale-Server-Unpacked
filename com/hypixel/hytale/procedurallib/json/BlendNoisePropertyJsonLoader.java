package com.hypixel.hytale.procedurallib.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.hypixel.hytale.procedurallib.property.BlendNoiseProperty;
import com.hypixel.hytale.procedurallib.property.NoiseProperty;
import java.nio.file.Path;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlendNoisePropertyJsonLoader<K extends SeedResource> extends JsonLoader<K, BlendNoiseProperty> {
   public BlendNoisePropertyJsonLoader(SeedString<K> seed, Path dataFolder, @Nullable JsonElement json) {
      super(seed, dataFolder, json);
   }

   @Nonnull
   public BlendNoiseProperty load() {
      NoiseProperty alpha = this.loadAlpha();
      NoiseProperty[] noise = this.loadNoise();
      double[] thresholds = this.loadThresholds();
      validate(noise, thresholds);
      return new BlendNoiseProperty(alpha, noise, thresholds);
   }

   protected NoiseProperty loadAlpha() {
      return new NoisePropertyJsonLoader<>(this.seed, this.dataFolder, this.mustGetObject("Alpha", null)).load();
   }

   protected NoiseProperty[] loadNoise() {
      JsonArray noise = this.mustGetArray("Noise", BlendNoisePropertyJsonLoader.Constants.EMPTY_ARRAY);
      NoiseProperty[] noises = new NoiseProperty[noise.size()];

      for (int i = 0; i < noise.size(); i++) {
         noises[i] = new NoisePropertyJsonLoader<>(this.seed, this.dataFolder, noise.get(i)).load();
      }

      return noises;
   }

   protected double[] loadThresholds() {
      JsonArray thresholds = this.mustGetArray("Thresholds", BlendNoisePropertyJsonLoader.Constants.EMPTY_ARRAY);
      double[] values = new double[thresholds.size()];

      for (int i = 0; i < thresholds.size(); i++) {
         values[i] = mustGet("$" + i, thresholds.get(i), null, Number.class, JsonLoader::isNumber, JsonElement::getAsNumber).doubleValue();
      }

      return values;
   }

   protected static void validate(NoiseProperty[] noises, double[] thresholds) {
      if (noises.length != thresholds.length) {
         throw new IllegalStateException("Number of noises must match number of thresholds");
      } else {
         double previous = Double.NEGATIVE_INFINITY;

         for (int i = 0; i < thresholds.length; i++) {
            if (thresholds[i] <= previous) {
               throw new IllegalStateException("Thresholds must be in ascending order and cannot be equal");
            }

            previous = thresholds[i];
         }
      }
   }

   public interface Constants {
      String KEY_ALPHA = "Alpha";
      String KEY_NOISE = "Noise";
      String KEY_THRESHOLDS = "Thresholds";
      JsonArray EMPTY_ARRAY = new JsonArray();
   }
}
