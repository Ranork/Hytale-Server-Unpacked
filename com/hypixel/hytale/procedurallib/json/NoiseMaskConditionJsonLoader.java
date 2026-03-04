package com.hypixel.hytale.procedurallib.json;

import com.google.gson.JsonElement;
import com.hypixel.hytale.procedurallib.condition.DefaultCoordinateCondition;
import com.hypixel.hytale.procedurallib.condition.ICoordinateCondition;
import com.hypixel.hytale.procedurallib.condition.IDoubleCondition;
import com.hypixel.hytale.procedurallib.condition.NoiseMaskCondition;
import com.hypixel.hytale.procedurallib.property.NoiseProperty;
import java.nio.file.Path;
import javax.annotation.Nonnull;

public class NoiseMaskConditionJsonLoader<K extends SeedResource> extends JsonLoader<K, ICoordinateCondition> {
   protected final boolean defaultValue;

   public NoiseMaskConditionJsonLoader(@Nonnull SeedString<K> seed, Path dataFolder, JsonElement json) {
      this(seed, dataFolder, json, true);
   }

   public NoiseMaskConditionJsonLoader(@Nonnull SeedString<K> seed, Path dataFolder, JsonElement json, boolean defaultValue) {
      super(seed.append(".NoiseMaskCondition"), dataFolder, json);
      this.defaultValue = defaultValue;
   }

   @Nonnull
   public ICoordinateCondition load() {
      ICoordinateCondition mapCondition = this.defaultValue ? DefaultCoordinateCondition.DEFAULT_TRUE : DefaultCoordinateCondition.DEFAULT_FALSE;
      if (this.json != null && !this.json.isJsonNull()) {
         if (!this.has("Threshold")) {
            throw new IllegalStateException("Could not find threshold data in noise mask. Keyword: Threshold");
         }

         NoiseProperty noise = new NoisePropertyJsonLoader<>(this.seed, this.dataFolder, this.json).load();
         IDoubleCondition threshold = new DoubleConditionJsonLoader<>(this.seed, this.dataFolder, this.get("Threshold")).load();
         mapCondition = new NoiseMaskCondition(noise, threshold);
      }

      return mapCondition;
   }

   public interface Constants {
      String KEY_THRESHOLD = "Threshold";
      String ERROR_THRESHOLD = "Could not find threshold data in noise mask. Keyword: Threshold";
   }
}
