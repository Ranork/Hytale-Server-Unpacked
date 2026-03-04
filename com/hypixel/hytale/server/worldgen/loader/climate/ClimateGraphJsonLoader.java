package com.hypixel.hytale.server.worldgen.loader.climate;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.hypixel.hytale.procedurallib.json.JsonLoader;
import com.hypixel.hytale.procedurallib.json.SeedResource;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.server.worldgen.climate.ClimateGraph;
import com.hypixel.hytale.server.worldgen.climate.ClimateType;
import java.nio.file.Path;
import javax.annotation.Nonnull;

public class ClimateGraphJsonLoader<K extends SeedResource> extends JsonLoader<K, ClimateGraph> {
   public ClimateGraphJsonLoader(SeedString<K> seed, Path dataFolder, JsonElement json) {
      super(seed, dataFolder, json);
   }

   @Nonnull
   public ClimateGraph load() {
      ClimateType[] climates = this.loadClimates();
      ClimateGraph.FadeMode fadeMode = this.loadFadeMode();
      double fadeRadius = this.loadFadeRadius();
      double fadeDistance = this.loadFadeDistance();
      return new ClimateGraph(512, climates, fadeMode, fadeRadius, fadeDistance);
   }

   protected ClimateGraph.FadeMode loadFadeMode() {
      String fadeMode = this.mustGetString("FadeMode", ClimateGraphJsonLoader.Constants.DEFAULT_FADE_MODE);
      return ClimateGraph.FadeMode.valueOf(fadeMode);
   }

   protected double loadFadeRadius() {
      return this.mustGetNumber("FadeRadius", ClimateGraphJsonLoader.Constants.DEFAULT_FADE_RADIUS).doubleValue();
   }

   protected double loadFadeDistance() {
      return this.mustGetNumber("FadeDistance", ClimateGraphJsonLoader.Constants.DEFAULT_FADE_DISTANCE).doubleValue();
   }

   @Nonnull
   protected ClimateType[] loadClimates() {
      JsonArray climatesArr = this.mustGetArray("Climates", ClimateGraphJsonLoader.Constants.DEFAULT_CLIMATES);
      ClimateType[] climates = new ClimateType[climatesArr.size()];

      for (int i = 0; i < climatesArr.size(); i++) {
         JsonElement climateJson = climatesArr.get(i);
         climates[i] = new ClimateTypeJsonLoader<>(this.seed, this.dataFolder, climateJson, null).load();
      }

      return climates;
   }

   public interface Constants {
      String KEY_CLIMATE = "Climate";
      String KEY_FADE_MODE = "FadeMode";
      String KEY_FADE_RADIUS = "FadeRadius";
      String KEY_FADE_DISTANCE = "FadeDistance";
      String KEY_CLIMATES = "Climates";
      JsonArray DEFAULT_CLIMATES = new JsonArray();
      Double DEFAULT_FADE_RADIUS = 50.0;
      Double DEFAULT_FADE_DISTANCE = 100.0;
      String DEFAULT_FADE_MODE = ClimateGraph.FadeMode.CHILDREN.name();
   }
}
