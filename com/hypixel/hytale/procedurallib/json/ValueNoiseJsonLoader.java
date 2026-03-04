package com.hypixel.hytale.procedurallib.json;

import com.google.gson.JsonElement;
import com.hypixel.hytale.procedurallib.logic.GeneralNoise;
import com.hypixel.hytale.procedurallib.logic.ValueNoise;
import java.nio.file.Path;
import javax.annotation.Nonnull;

public class ValueNoiseJsonLoader<K extends SeedResource> extends JsonLoader<K, ValueNoise> {
   public ValueNoiseJsonLoader(@Nonnull SeedString<K> seed, Path dataFolder, JsonElement json) {
      super(seed.append(".ValueNoise"), dataFolder, json);
   }

   @Nonnull
   public ValueNoise load() {
      return new ValueNoise(this.loadInterpolationFunction());
   }

   protected GeneralNoise.InterpolationFunction loadInterpolationFunction() {
      GeneralNoise.InterpolationMode interpolationMode = ValueNoiseJsonLoader.Constants.DEFAULT_INTERPOLATION_MODE;
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
