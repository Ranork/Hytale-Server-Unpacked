package com.hypixel.hytale.procedurallib.json;

import com.google.gson.JsonElement;
import com.hypixel.hytale.procedurallib.condition.DefaultDoubleCondition;
import com.hypixel.hytale.procedurallib.condition.DoubleThresholdCondition;
import com.hypixel.hytale.procedurallib.condition.IDoubleCondition;
import com.hypixel.hytale.procedurallib.condition.SingleDoubleCondition;
import java.nio.file.Path;
import java.util.Objects;
import javax.annotation.Nonnull;

public class DoubleConditionJsonLoader<K extends SeedResource> extends JsonLoader<K, IDoubleCondition> {
   protected final Boolean defaultValue;

   public DoubleConditionJsonLoader(@Nonnull SeedString<K> seed, Path dataFolder, JsonElement json) {
      this(seed.append(".DoubleCondition"), dataFolder, json, true);
   }

   public DoubleConditionJsonLoader(SeedString<K> seed, Path dataFolder, JsonElement json, Boolean defaultValue) {
      super(seed, dataFolder, json);
      this.defaultValue = defaultValue;
   }

   @Nonnull
   public IDoubleCondition load() {
      if (this.json == null || this.json.isJsonNull()) {
         Objects.requireNonNull(this.defaultValue, "Default value is not set and condition is not defined.");
         return this.defaultValue ? DefaultDoubleCondition.DEFAULT_TRUE : DefaultDoubleCondition.DEFAULT_FALSE;
      } else if (!this.json.isJsonPrimitive()
         && (!this.json.isJsonArray() || this.json.getAsJsonArray().size() != 1 || !this.json.getAsJsonArray().get(0).isJsonPrimitive())) {
         if (this.json.isJsonArray()) {
            return new DoubleThresholdCondition(new DoubleThresholdJsonLoader<>(this.seed, this.dataFolder, this.json).load());
         } else {
            throw new Error(String.format("Failed to load \"%s\" as DoubleCondition", this.json));
         }
      } else {
         double limit = this.json.getAsDouble();
         return new SingleDoubleCondition(limit);
      }
   }

   public interface Constants {
      String ERROR_NO_DEFAULT = "Default value is not set and condition is not defined.";
      String ERROR_FAILED = "Failed to load \"%s\" as DoubleCondition";
   }
}
