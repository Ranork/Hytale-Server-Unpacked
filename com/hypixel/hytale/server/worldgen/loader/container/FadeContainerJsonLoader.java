package com.hypixel.hytale.server.worldgen.loader.container;

import com.google.gson.JsonElement;
import com.hypixel.hytale.procedurallib.json.JsonLoader;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.server.worldgen.SeedStringResource;
import com.hypixel.hytale.server.worldgen.container.FadeContainer;
import java.nio.file.Path;
import javax.annotation.Nonnull;

public class FadeContainerJsonLoader extends JsonLoader<SeedStringResource, FadeContainer> {
   public FadeContainerJsonLoader(@Nonnull SeedString<SeedStringResource> seed, Path dataFolder, JsonElement json) {
      super(seed.append(".FadeContainer"), dataFolder, json);
   }

   @Nonnull
   public FadeContainer load() {
      return new FadeContainer(this.loadFadeStart(), this.loadFadeLength(), this.loadTerrainStart(), this.loadTerrainLength(), this.loadFadeHeightmap());
   }

   protected double loadFadeStart() {
      return this.has("FadeStart") ? this.get("FadeStart").getAsDouble() : 0.0;
   }

   protected double loadFadeLength() {
      return this.has("FadeLength") ? this.get("FadeLength").getAsDouble() : 0.0;
   }

   protected double loadTerrainStart() {
      return this.has("TerrainStart") ? this.get("TerrainStart").getAsDouble() : 0.0;
   }

   protected double loadTerrainLength() {
      return this.has("TerrainLength") ? this.get("TerrainLength").getAsDouble() : 0.0;
   }

   protected double loadFadeHeightmap() {
      double mod = Double.NEGATIVE_INFINITY;
      if (this.has("FadeHeightmap")) {
         mod = this.get("FadeHeightmap").getAsDouble();
      }

      return mod;
   }

   public interface Constants {
      String KEY_FADE_START = "FadeStart";
      String KEY_FADE_LENGTH = "FadeLength";
      String KEY_TERRAIN_START = "TerrainStart";
      String KEY_TERRAIN_LENGTH = "TerrainLength";
      String KEY_FADE_HEIGHTMAP = "FadeHeightmap";
   }
}
