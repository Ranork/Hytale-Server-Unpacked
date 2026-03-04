package com.hypixel.hytale.procedurallib.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.hypixel.hytale.procedurallib.condition.DefaultDoubleThresholdCondition;
import com.hypixel.hytale.procedurallib.condition.DoubleThreshold;
import com.hypixel.hytale.procedurallib.condition.IDoubleThreshold;
import java.nio.file.Path;
import javax.annotation.Nonnull;

public class DoubleThresholdJsonLoader<K extends SeedResource> extends JsonLoader<K, IDoubleThreshold> {
   protected final boolean defaultValue;

   public DoubleThresholdJsonLoader(@Nonnull SeedString<K> seed, Path dataFolder, JsonElement json) {
      this(seed, dataFolder, json, true);
   }

   public DoubleThresholdJsonLoader(@Nonnull SeedString<K> seed, Path dataFolder, JsonElement json, boolean defaultValue) {
      super(seed.append(".DoubleThreshold"), dataFolder, json);
      this.defaultValue = defaultValue;
   }

   @Nonnull
   public IDoubleThreshold load() {
      if (this.json == null || this.json.isJsonNull()) {
         return this.defaultValue ? DefaultDoubleThresholdCondition.DEFAULT_TRUE : DefaultDoubleThresholdCondition.DEFAULT_FALSE;
      } else if (this.json.isJsonPrimitive()) {
         double value = this.json.getAsDouble();
         return new DoubleThreshold.Single(0.0, value);
      } else {
         JsonArray jsonArray = this.json.getAsJsonArray();
         if (jsonArray.size() <= 0) {
            throw new IllegalArgumentException("Threshold array must contain at least one entry!");
         } else if (jsonArray.get(0).isJsonArray()) {
            DoubleThreshold.Single[] entries = new DoubleThreshold.Single[jsonArray.size()];

            for (int i = 0; i < entries.length; i++) {
               JsonArray jsonArrayEntry = jsonArray.get(i).getAsJsonArray();
               if (jsonArrayEntry.size() != 2) {
                  throw new IllegalArgumentException("Threshold array entries must have 2 numbers for lower/upper limit!");
               }

               entries[i] = new DoubleThreshold.Single(jsonArrayEntry.get(0).getAsDouble(), jsonArrayEntry.get(1).getAsDouble());
            }

            return new DoubleThreshold.Multiple(entries);
         } else if (jsonArray.size() != 2) {
            throw new IllegalArgumentException("Threshold array entries must have 2 numbers for lower/upper limit!");
         } else {
            return new DoubleThreshold.Single(jsonArray.get(0).getAsDouble(), jsonArray.get(1).getAsDouble());
         }
      }
   }

   public interface Constants {
      String ERROR_NO_ENTRY = "Threshold array must contain at least one entry!";
      String ERROR_THRESHOLD_SIZE = "Threshold array entries must have 2 numbers for lower/upper limit!";
   }
}
