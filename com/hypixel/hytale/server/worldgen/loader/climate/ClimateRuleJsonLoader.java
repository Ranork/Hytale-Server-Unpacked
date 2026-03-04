package com.hypixel.hytale.server.worldgen.loader.climate;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hypixel.hytale.procedurallib.json.JsonLoader;
import com.hypixel.hytale.procedurallib.json.SeedResource;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.server.worldgen.climate.ClimateSearch;
import java.nio.file.Path;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ClimateRuleJsonLoader<K extends SeedResource> extends JsonLoader<K, ClimateSearch.Rule> {
   public ClimateRuleJsonLoader(SeedString<K> seed, Path dataFolder, @Nullable JsonElement json) {
      super(seed, dataFolder, json);
   }

   @Nullable
   public ClimateSearch.Rule load() {
      return new ClimateSearch.Rule(this.loadRange("Continent"), this.loadRange("Temperature"), this.loadRange("Intensity"), this.loadRange("Fade"));
   }

   protected ClimateSearch.Range loadRange(String key) {
      if (this.has(key)) {
         JsonObject json = this.mustGetObject(key, null);
         double target = loadTarget(json);
         double radius = loadRadius(json);
         double weight = loadWeight(json);
         return new ClimateSearch.Range(target, radius, weight);
      } else {
         return ClimateSearch.Range.DEFAULT;
      }
   }

   protected static double loadTarget(@Nonnull JsonObject json) {
      return mustGet("Target", json.get("Target"), null, Double.class, x$0 -> JsonLoader.isNumber(x$0), JsonElement::getAsDouble);
   }

   protected static double loadRadius(@Nonnull JsonObject json) {
      return mustGet("Radius", json.get("Radius"), null, Double.class, x$0 -> JsonLoader.isNumber(x$0), JsonElement::getAsDouble);
   }

   protected static double loadWeight(@Nonnull JsonObject json) {
      return mustGet("Weight", json.get("Weight"), null, Double.class, x$0 -> JsonLoader.isNumber(x$0), JsonElement::getAsDouble);
   }

   protected interface Constants {
      String KEY_TARGET = "Target";
      String KEY_RADIUS = "Radius";
      String KEY_WEIGHT = "Weight";
   }
}
