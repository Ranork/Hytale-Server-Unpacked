package com.hypixel.hytale.procedurallib.json;

import com.google.gson.JsonElement;
import com.hypixel.hytale.procedurallib.logic.PointNoise;
import java.nio.file.Path;
import javax.annotation.Nullable;

public class PointNoiseJsonLoader<K extends SeedResource> extends JsonLoader<K, PointNoise> {
   public PointNoiseJsonLoader(SeedString<K> seed, Path dataFolder, @Nullable JsonElement json) {
      super(seed, dataFolder, json);
   }

   @Nullable
   public PointNoise load() {
      return new PointNoise(
         this.mustGetNumber("X", PointNoiseJsonLoader.Constants.DEFAULT_COORD).doubleValue(),
         this.mustGetNumber("Y", PointNoiseJsonLoader.Constants.DEFAULT_COORD).doubleValue(),
         this.mustGetNumber("Z", PointNoiseJsonLoader.Constants.DEFAULT_COORD).doubleValue(),
         this.mustGetNumber("InnerRadius", PointNoiseJsonLoader.Constants.DEFAULT_RADIUS).doubleValue(),
         this.mustGetNumber("OuterRadius", PointNoiseJsonLoader.Constants.DEFAULT_RADIUS).doubleValue()
      );
   }

   public interface Constants {
      String KEY_X = "X";
      String KEY_Y = "Y";
      String KEY_Z = "Z";
      String KEY_INNER_RADIUS = "InnerRadius";
      String KEY_OUTER_RADIUS = "OuterRadius";
      Double DEFAULT_COORD = 0.0;
      Double DEFAULT_RADIUS = 0.0;
   }
}
