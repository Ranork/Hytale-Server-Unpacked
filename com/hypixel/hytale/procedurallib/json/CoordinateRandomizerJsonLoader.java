package com.hypixel.hytale.procedurallib.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hypixel.hytale.procedurallib.property.NoiseProperty;
import com.hypixel.hytale.procedurallib.random.CoordinateRandomizer;
import com.hypixel.hytale.procedurallib.random.CoordinateRotator;
import com.hypixel.hytale.procedurallib.random.ICoordinateRandomizer;
import com.hypixel.hytale.procedurallib.random.RotatedCoordinateRandomizer;
import java.nio.file.Path;
import javax.annotation.Nonnull;

public class CoordinateRandomizerJsonLoader<K extends SeedResource> extends JsonLoader<K, ICoordinateRandomizer> {
   public CoordinateRandomizerJsonLoader(SeedString<K> seed, Path dataFolder, JsonElement json) {
      super(seed, dataFolder, json);
   }

   @Nonnull
   public ICoordinateRandomizer load() {
      return this.json != null && !this.json.isJsonNull() ? this.loadRandomizer() : CoordinateRandomizer.EMPTY_RANDOMIZER;
   }

   @Nonnull
   protected ICoordinateRandomizer loadRandomizer() {
      ICoordinateRandomizer randomizer = new CoordinateRandomizer(
         this.loadGenerators(".X-Noise#%s"), this.loadGenerators(".Y-Noise#%s"), this.loadGenerators(".Z-Noise#%s")
      );
      if (this.has("Rotate")) {
         CoordinateRotator rotation = new CoordinateRotatorJsonLoader<>(this.seed, this.dataFolder, this.get("Rotate")).load();
         if (rotation != CoordinateRotator.NONE) {
            randomizer = new RotatedCoordinateRandomizer(randomizer, rotation);
         }
      }

      return randomizer;
   }

   @Nonnull
   protected CoordinateRandomizer.AmplitudeNoiseProperty[] loadGenerators(@Nonnull String seedSuffix) {
      JsonArray array = this.get("Generators").getAsJsonArray();
      CoordinateRandomizer.AmplitudeNoiseProperty[] generators = new CoordinateRandomizer.AmplitudeNoiseProperty[array.size()];

      for (int i = 0; i < array.size(); i++) {
         JsonObject object = array.get(i).getAsJsonObject();
         NoiseProperty property = new NoisePropertyJsonLoader<>(this.seed.alternateOriginal(String.format(seedSuffix, i)), this.dataFolder, object).load();
         double amplitude = object.get("Amplitude").getAsDouble();
         generators[i] = new CoordinateRandomizer.AmplitudeNoiseProperty(property, amplitude);
      }

      return generators;
   }

   public interface Constants {
      String KEY_GENERATORS = "Generators";
      String KEY_GENERATORS_AMPLITUDE = "Amplitude";
      String SEED_X_NOISE_SUFFIX = ".X-Noise#%s";
      String SEED_Y_NOISE_SUFFIX = ".Y-Noise#%s";
      String SEED_Z_NOISE_SUFFIX = ".Z-Noise#%s";
   }
}
