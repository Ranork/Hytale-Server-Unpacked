package com.hypixel.hytale.server.worldgen.loader.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hypixel.hytale.math.vector.Vector2d;
import com.hypixel.hytale.procedurallib.json.JsonLoader;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.server.worldgen.SeedStringResource;
import java.nio.file.Path;
import javax.annotation.Nonnull;

public class Vector2dJsonLoader extends JsonLoader<SeedStringResource, Vector2d> {
   public Vector2dJsonLoader(SeedString<SeedStringResource> seed, Path dataFolder, JsonElement json) {
      super(seed, dataFolder, json);
   }

   @Nonnull
   public Vector2d load() {
      if (this.json == null || this.json.isJsonNull()) {
         return new Vector2d();
      } else if (this.json.isJsonArray()) {
         JsonArray array = this.json.getAsJsonArray();
         if (array.isEmpty()) {
            return new Vector2d();
         } else if (array.size() == 1) {
            double value = array.get(0).getAsDouble();
            return new Vector2d(value, value);
         } else {
            double x = array.get(0).getAsDouble();
            double y = array.get(1).getAsDouble();
            return new Vector2d(x, y);
         }
      } else if (this.json.isJsonObject()) {
         JsonObject object = this.json.getAsJsonObject();
         double x = object.get("X").getAsDouble();
         double y = object.get("Y").getAsDouble();
         return new Vector2d(x, y);
      } else {
         return new Vector2d();
      }
   }

   public interface Constants {
      String KEY_X = "X";
      String KEY_Y = "Y";
   }
}
