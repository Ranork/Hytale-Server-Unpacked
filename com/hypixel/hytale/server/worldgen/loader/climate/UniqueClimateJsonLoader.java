package com.hypixel.hytale.server.worldgen.loader.climate;

import com.google.gson.JsonElement;
import com.hypixel.hytale.math.vector.Vector2i;
import com.hypixel.hytale.procedurallib.json.JsonLoader;
import com.hypixel.hytale.procedurallib.json.SeedResource;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.server.worldgen.climate.ClimateSearch;
import com.hypixel.hytale.server.worldgen.climate.UniqueClimateGenerator;
import com.hypixel.hytale.server.worldgen.loader.util.ColorUtil;
import java.nio.file.Path;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class UniqueClimateJsonLoader<K extends SeedResource> extends JsonLoader<K, UniqueClimateGenerator.Entry> {
   public UniqueClimateJsonLoader(SeedString<K> seed, Path dataFolder, @Nullable JsonElement json) {
      super(seed, dataFolder, json);
   }

   @Nonnull
   public UniqueClimateGenerator.Entry load() {
      return new UniqueClimateGenerator.Entry(
         this.loadName(),
         this.loadParent(),
         this.loadColor(),
         this.loadRadius(),
         this.loadOrigin(),
         this.loadMinDistance(),
         this.loadDistance(),
         this.loadRule()
      );
   }

   protected String loadName() {
      return this.mustGetString("Name", null);
   }

   protected String loadParent() {
      return this.mustGetString("Parent", "");
   }

   protected int loadColor() {
      return ColorUtil.hexString(this.mustGetString("Color", null));
   }

   protected int loadRadius() {
      return this.mustGetNumber("Radius", UniqueClimateJsonLoader.Constants.DEFAULT_RADIUS).intValue();
   }

   @Nonnull
   protected Vector2i loadOrigin() {
      int x = this.mustGetNumber("OriginX", UniqueClimateJsonLoader.Constants.DEFAULT_OFFSET).intValue();
      int y = this.mustGetNumber("OriginY", UniqueClimateJsonLoader.Constants.DEFAULT_OFFSET).intValue();
      return new Vector2i(x, y);
   }

   protected int loadDistance() {
      return this.mustGetNumber("Distance", UniqueClimateJsonLoader.Constants.DEFAULT_SEARCH_RADIUS).intValue();
   }

   protected int loadMinDistance() {
      return this.mustGetNumber("MinDistance", UniqueClimateJsonLoader.Constants.DEFAULT_SEARCH_MIN_RADIUS).intValue();
   }

   protected ClimateSearch.Rule loadRule() {
      return new ClimateRuleJsonLoader<>(this.seed, this.dataFolder, this.mustGetObject("Rule", null)).load();
   }

   protected interface Constants {
      String KEY_ZONE = "Name";
      String KEY_PARENT = "Parent";
      String KEY_COLOR = "Color";
      String KEY_RADIUS = "Radius";
      String KEY_ORIGIN_X = "OriginX";
      String KEY_ORIGIN_Y = "OriginY";
      String KEY_DISTANCE = "Distance";
      String KEY_MIN_DISTANCE = "MinDistance";
      String KEY_RULE = "Rule";
      Integer DEFAULT_RADIUS = 8;
      Integer DEFAULT_OFFSET = 8;
      Integer DEFAULT_SEARCH_RADIUS = 5000;
      Integer DEFAULT_SEARCH_MIN_RADIUS = 100;
   }
}
