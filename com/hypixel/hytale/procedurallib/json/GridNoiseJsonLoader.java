package com.hypixel.hytale.procedurallib.json;

import com.google.gson.JsonElement;
import com.hypixel.hytale.procedurallib.NoiseFunction;
import com.hypixel.hytale.procedurallib.logic.GridNoise;
import java.nio.file.Path;
import javax.annotation.Nonnull;

public class GridNoiseJsonLoader<K extends SeedResource> extends JsonLoader<K, NoiseFunction> {
   public GridNoiseJsonLoader(@Nonnull SeedString<K> seed, Path dataFolder, JsonElement json) {
      super(seed.append(".GridNoise"), dataFolder, json);
   }

   @Nonnull
   public NoiseFunction load() {
      double defaultThickness = this.loadDefaultThickness();
      return new GridNoise(this.loadThicknessX(defaultThickness), this.loadThicknessY(defaultThickness), this.loadThicknessZ(defaultThickness));
   }

   protected double loadDefaultThickness() {
      return !this.has("Thickness") ? Double.NaN : this.get("Thickness").getAsDouble();
   }

   protected double loadThicknessX(double defaultThickness) {
      return this.loadThickness("ThicknessX", defaultThickness);
   }

   protected double loadThicknessY(double defaultThickness) {
      return this.loadThickness("ThicknessY", defaultThickness);
   }

   protected double loadThicknessZ(double defaultThickness) {
      if (Double.isNaN(defaultThickness)) {
         defaultThickness = 0.0;
      }

      return this.loadThickness("ThicknessZ", defaultThickness);
   }

   protected double loadThickness(String key, double defaultThickness) {
      double value = defaultThickness;
      if (this.has(key)) {
         value = this.get(key).getAsDouble();
      }

      if (Double.isNaN(value)) {
         throw new Error(String.format("Could not find thickness '%s' and no default 'Thickness' value defined!", key));
      } else {
         return value;
      }
   }

   public interface Constants {
      double DEFAULT_NO_THICKNESS = Double.NaN;
      double DEFAULT_THICKNESS_Z = 0.0;
      String KEY_THICKNESS = "Thickness";
      String KEY_THICKNESS_X = "ThicknessX";
      String KEY_THICKNESS_Y = "ThicknessY";
      String KEY_THICKNESS_Z = "ThicknessZ";
      String ERROR_NO_THICKNESS = "Could not find thickness '%s' and no default 'Thickness' value defined!";
   }
}
