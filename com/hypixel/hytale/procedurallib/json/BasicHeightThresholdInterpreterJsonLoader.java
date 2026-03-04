package com.hypixel.hytale.procedurallib.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.hypixel.hytale.procedurallib.condition.BasicHeightThresholdInterpreter;
import java.nio.file.Path;
import javax.annotation.Nonnull;

public class BasicHeightThresholdInterpreterJsonLoader<K extends SeedResource> extends JsonLoader<K, BasicHeightThresholdInterpreter> {
   protected final int length;

   public BasicHeightThresholdInterpreterJsonLoader(@Nonnull SeedString<K> seed, Path dataFolder, JsonElement json, int length) {
      super(seed.append(".BasicHeightThresholdInterpreter"), dataFolder, json);
      this.length = length;
   }

   @Nonnull
   public BasicHeightThresholdInterpreter load() {
      return new BasicHeightThresholdInterpreter(this.loadPositions(), this.loadValues(), this.length);
   }

   protected int[] loadPositions() {
      if (!this.has("Positions")) {
         throw new IllegalStateException("Could not find position data in HeightThresholdInterpreter. Keyword: Positions");
      } else {
         JsonArray terrainNoiseKeyPositionsJson = this.get("Positions").getAsJsonArray();
         int[] positions = new int[terrainNoiseKeyPositionsJson.size()];

         for (int i = 0; i < positions.length; i++) {
            positions[i] = terrainNoiseKeyPositionsJson.get(i).getAsInt();
         }

         return positions;
      }
   }

   protected float[] loadValues() {
      if (!this.has("Values")) {
         throw new IllegalStateException("Could not find value data in HeightThresholdInterpreter. Keyword: Values");
      } else {
         JsonArray terrainNoiseKeyThresholdsJson = this.get("Values").getAsJsonArray();
         float[] thresholds = new float[terrainNoiseKeyThresholdsJson.size()];

         for (int i = 0; i < thresholds.length; i++) {
            thresholds[i] = terrainNoiseKeyThresholdsJson.get(i).getAsFloat();
         }

         return thresholds;
      }
   }

   public interface Constants {
      String KEY_POSITIONS = "Positions";
      String KEY_VALUES = "Values";
      String ERROR_NO_POSITIONS = "Could not find position data in HeightThresholdInterpreter. Keyword: Positions";
      String ERROR_NO_VALUES = "Could not find value data in HeightThresholdInterpreter. Keyword: Values";
   }
}
