package com.hypixel.hytale.server.worldgen.loader.climate;

import com.google.gson.JsonElement;
import com.hypixel.hytale.procedurallib.json.JsonLoader;
import com.hypixel.hytale.procedurallib.json.SeedResource;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.server.worldgen.climate.ClimateColor;
import com.hypixel.hytale.server.worldgen.loader.util.ColorUtil;
import java.nio.file.Path;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ClimateColorJsonLoader<K extends SeedResource> extends JsonLoader<K, ClimateColor> {
   @Nullable
   private final ClimateColor parent;

   public ClimateColorJsonLoader(SeedString<K> seed, Path dataFolder, @Nullable JsonElement json, @Nullable ClimateColor parent) {
      super(seed, dataFolder, json);
      this.parent = parent;
   }

   @Nonnull
   public ClimateColor load() {
      return new ClimateColor(
         this.loadColor("Color", -1),
         this.loadColor("Shore", this.parent != null ? this.parent.shore : -1),
         this.loadColor("Ocean", this.parent != null ? this.parent.ocean : -1),
         this.loadColor("ShallowOcean", this.parent != null ? this.parent.shallowOcean : -1)
      );
   }

   protected int loadColor(@Nonnull String key, int defaultColor) {
      if (this.has(key)) {
         String color = this.mustGetString(key, null);
         return ColorUtil.hexString(color);
      } else {
         return defaultColor;
      }
   }

   public interface Constants {
      String KEY_COLOR = "Color";
      String KEY_SHORE = "Shore";
      String KEY_OCEAN = "Ocean";
      String KEY_SHALLOW_OCEAN = "ShallowOcean";
   }
}
