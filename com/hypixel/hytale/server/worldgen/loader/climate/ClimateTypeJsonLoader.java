package com.hypixel.hytale.server.worldgen.loader.climate;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hypixel.hytale.procedurallib.json.JsonLoader;
import com.hypixel.hytale.procedurallib.json.SeedResource;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.server.worldgen.climate.ClimateColor;
import com.hypixel.hytale.server.worldgen.climate.ClimatePoint;
import com.hypixel.hytale.server.worldgen.climate.ClimateType;
import java.nio.file.Path;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ClimateTypeJsonLoader<K extends SeedResource> extends JsonLoader<K, ClimateType> {
   @Nullable
   private final ClimateType parent;

   public ClimateTypeJsonLoader(SeedString<K> seed, Path dataFolder, JsonElement json, @Nullable ClimateType parent) {
      super(seed, dataFolder, json);
      this.parent = parent;
   }

   public ClimateType load() {
      String name = this.loadName();
      ClimateColor color = new ClimateColorJsonLoader<>(this.seed, this.dataFolder, this.json, this.parent != null ? this.parent.color : null).load();
      ClimateColor island = this.loadIslandColor(color);
      ClimatePoint[] points = this.loadClimatePoints();
      ClimateType[] children = this.loadChildren(new ClimateType(name, color, island, points, ClimateType.EMPTY_ARRAY));
      return new ClimateType(name, color, island, points, children);
   }

   @Nonnull
   protected String loadName() {
      return this.mustGetString("Name", null);
   }

   @Nonnull
   protected ClimateColor loadIslandColor(@Nonnull ClimateColor color) {
      return this.has("Island")
         ? new ClimateColorJsonLoader<>(this.seed, this.dataFolder, this.get("Island"), this.parent != null ? this.parent.island : null).load()
         : color;
   }

   @Nonnull
   protected ClimatePoint[] loadClimatePoints() {
      JsonArray pointsArr = this.mustGetArray("Points", ClimateTypeJsonLoader.Constants.DEFAULT_POINTS);
      ClimatePoint[] points = new ClimatePoint[pointsArr.size()];

      for (int i = 0; i < pointsArr.size(); i++) {
         points[i] = this.loadPoint(i, pointsArr.get(i));
      }

      return points;
   }

   @Nonnull
   protected ClimateType[] loadChildren(ClimateType parent) {
      JsonArray childrenArr = this.mustGetArray("Children", ClimateTypeJsonLoader.Constants.DEFAULT_CHILDREN);
      ClimateType[] children = new ClimateType[childrenArr.size()];

      for (int i = 0; i < childrenArr.size(); i++) {
         JsonObject childJson = childrenArr.get(i).getAsJsonObject();
         children[i] = new ClimateTypeJsonLoader<>(this.seed, this.dataFolder, childJson, parent).load();
      }

      return children;
   }

   @Nonnull
   protected ClimatePoint loadPoint(int index, JsonElement pointsJson) {
      try {
         return new ClimatePointJsonLoader<>(this.seed, this.dataFolder, pointsJson).load();
      } catch (Throwable var4) {
         throw error(var4, "Invalid climate point at index: %d", index);
      }
   }

   public interface Constants {
      String KEY_NAME = "Name";
      String KEY_POINTS = "Points";
      String KEY_CHILDREN = "Children";
      String KEY_ISLAND = "Island";
      JsonArray DEFAULT_POINTS = new JsonArray();
      JsonArray DEFAULT_CHILDREN = new JsonArray();
   }
}
