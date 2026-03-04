package com.hypixel.hytale.procedurallib.json;

import com.google.gson.JsonElement;
import com.hypixel.hytale.procedurallib.property.CurveNoiseProperty;
import com.hypixel.hytale.procedurallib.property.NoiseProperty;
import java.nio.file.Path;
import java.util.function.DoubleUnaryOperator;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CurveNoisePropertyJsonLoader<K extends SeedResource> extends JsonLoader<K, CurveNoiseProperty> {
   @Nullable
   protected final NoiseProperty noise;

   public CurveNoisePropertyJsonLoader(SeedString<K> seed, Path dataFolder, JsonElement json, @Nullable NoiseProperty noise) {
      super(seed, dataFolder, json);
      this.noise = noise;
   }

   @Nonnull
   public CurveNoiseProperty load() {
      return new CurveNoiseProperty(this.loadNoise(), this.loadDCurve());
   }

   @Nullable
   protected NoiseProperty loadNoise() {
      NoiseProperty noise = this.noise;
      if (noise == null) {
         if (this.has("Noise")) {
            return new NoisePropertyJsonLoader<>(this.seed, this.dataFolder, this.get("Noise")).load();
         } else {
            throw new Error("Missing Noise entry!");
         }
      } else {
         return noise;
      }
   }

   @Nonnull
   protected DoubleUnaryOperator loadDCurve() {
      double a = this.loadValue("A", 2.0);
      double b = this.loadValue("B", -2.0);
      return new CurveNoiseProperty.PowerCurve(a, b);
   }

   protected double loadValue(String key, double def) {
      double value = def;
      if (this.has(key)) {
         value = this.get(key).getAsDouble();
      }

      return value;
   }

   public interface Constants {
      String KEY_NOISE = "Noise";
      String KEY_CONST_A = "A";
      String KEY_CONST_B = "B";
      double DEFAULT_A = 2.0;
      double DEFAULT_B = -2.0;
   }
}
