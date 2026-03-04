package com.hypixel.hytale.server.worldgen.loader.cave.shape;

import com.google.gson.JsonElement;
import com.hypixel.hytale.procedurallib.json.JsonLoader;
import com.hypixel.hytale.procedurallib.json.NoisePropertyJsonLoader;
import com.hypixel.hytale.procedurallib.json.SeedResource;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.procedurallib.property.NoiseProperty;
import com.hypixel.hytale.server.worldgen.cave.shape.distorted.ShapeDistortion;
import java.nio.file.Path;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ShapeDistortionJsonLoader<K extends SeedResource> extends JsonLoader<K, ShapeDistortion> {
   public ShapeDistortionJsonLoader(@Nonnull SeedString<K> seed, Path dataFolder, JsonElement json) {
      super(seed.append(".ShapeDistortion"), dataFolder, json);
   }

   public ShapeDistortion load() {
      return ShapeDistortion.of(this.loadWidth(), this.loadFloor(), this.loadCeiling());
   }

   @Nullable
   private NoiseProperty loadWidth() {
      return this.has("Width") ? new NoisePropertyJsonLoader<>(this.seed, this.dataFolder, this.get("Width")).load() : null;
   }

   @Nullable
   private NoiseProperty loadFloor() {
      return this.has("Floor") ? new NoisePropertyJsonLoader<>(this.seed, this.dataFolder, this.get("Floor")).load() : null;
   }

   @Nullable
   private NoiseProperty loadCeiling() {
      return this.has("Ceiling") ? new NoisePropertyJsonLoader<>(this.seed, this.dataFolder, this.get("Ceiling")).load() : null;
   }

   public interface Constants {
      String KEY_WIDTH = "Width";
      String KEY_FLOOR = "Floor";
      String KEY_CEILING = "Ceiling";
   }
}
