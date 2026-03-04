package com.hypixel.hytale.procedurallib.json;

import com.google.gson.JsonElement;
import com.hypixel.hytale.procedurallib.NoiseFunction;
import com.hypixel.hytale.procedurallib.logic.GeneralNoise;
import com.hypixel.hytale.procedurallib.logic.PerlinNoise;
import java.nio.file.Path;
import javax.annotation.Nonnull;

public class PerlinNoiseJsonLoader<K extends SeedResource> extends JsonLoader<K, NoiseFunction> {
   public PerlinNoiseJsonLoader(@Nonnull SeedString<K> seed, Path dataFolder, JsonElement json) {
      super(seed.append(".PerlinNoise"), dataFolder, json);
   }

   @Nonnull
   public NoiseFunction load() {
      return new PerlinNoise(this.loadInterpolationFunction());
   }

   protected GeneralNoise.InterpolationFunction loadInterpolationFunction() {
      GeneralNoise.InterpolationMode interpolationMode = PerlinNoiseJsonLoader.Constants.DEFAULT_INTERPOLATION_MODE;
      if (this.has("InterpolationMode")) {
         interpolationMode = GeneralNoise.InterpolationMode.valueOf(this.get("InterpolationMode").getAsString());
      }

      return interpolationMode.getFunction();
   }

   public interface Constants {
      String KEY_INTERPOLATION_MODE = "InterpolationMode";
      GeneralNoise.InterpolationMode DEFAULT_INTERPOLATION_MODE = GeneralNoise.InterpolationMode.QUINTIC;
   }
}
