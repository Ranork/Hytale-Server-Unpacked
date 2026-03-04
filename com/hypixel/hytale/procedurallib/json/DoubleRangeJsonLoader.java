package com.hypixel.hytale.procedurallib.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.hypixel.hytale.procedurallib.supplier.DoubleRange;
import com.hypixel.hytale.procedurallib.supplier.IDoubleRange;
import java.nio.file.Path;
import java.util.Objects;
import javax.annotation.Nonnull;

public class DoubleRangeJsonLoader<K extends SeedResource> extends JsonLoader<K, IDoubleRange> {
   protected final double default1;
   protected final double default2;
   @Nonnull
   protected final DoubleRangeJsonLoader.DoubleToDoubleFunction function;

   public DoubleRangeJsonLoader(@Nonnull SeedString<K> seed, Path dataFolder, JsonElement json) {
      this(seed, dataFolder, json, 0.0, d -> d);
   }

   public DoubleRangeJsonLoader(@Nonnull SeedString<K> seed, Path dataFolder, JsonElement json, DoubleRangeJsonLoader.DoubleToDoubleFunction function) {
      this(seed, dataFolder, json, 0.0, function);
   }

   public DoubleRangeJsonLoader(@Nonnull SeedString<K> seed, Path dataFolder, JsonElement json, double default1) {
      this(seed, dataFolder, json, default1, default1, d -> d);
   }

   public DoubleRangeJsonLoader(
      @Nonnull SeedString<K> seed, Path dataFolder, JsonElement json, double default1, DoubleRangeJsonLoader.DoubleToDoubleFunction function
   ) {
      this(seed, dataFolder, json, default1, default1, function);
   }

   public DoubleRangeJsonLoader(@Nonnull SeedString<K> seed, Path dataFolder, JsonElement json, double default1, double default2) {
      this(seed, dataFolder, json, default1, default2, d -> d);
   }

   public DoubleRangeJsonLoader(
      @Nonnull SeedString<K> seed, Path dataFolder, JsonElement json, double default1, double default2, DoubleRangeJsonLoader.DoubleToDoubleFunction function
   ) {
      super(seed.append(".DoubleRange"), dataFolder, json);
      this.default1 = default1;
      this.default2 = default2;
      this.function = Objects.requireNonNull(function);
   }

   public IDoubleRange load() {
      if (this.json != null && !this.json.isJsonNull()) {
         if (this.json.isJsonArray()) {
            JsonArray array = this.json.getAsJsonArray();
            if (array.size() != 1 && array.size() != 2) {
               throw new IllegalStateException(String.format("Range array contains %s values. Only 1 or 2 entries are allowed.", array.size()));
            } else {
               return (IDoubleRange)(array.size() == 1
                  ? new DoubleRange.Constant(this.function.get(array.get(0).getAsDouble()))
                  : new DoubleRange.Normal(this.function.get(array.get(0).getAsDouble()), this.function.get(array.get(1).getAsDouble())));
            }
         } else if (!this.json.isJsonObject()) {
            return new DoubleRange.Constant(this.function.get(this.json.getAsDouble()));
         } else if (this.has("Thresholds") && this.has("Values")) {
            return this.loadThreshold();
         } else if (!this.has("Min")) {
            throw new IllegalStateException("Minimum value of range is not defined. Keyword: Min");
         } else if (!this.has("Max")) {
            throw new IllegalStateException("Maximum value of range is not defined. Keyword: Max");
         } else {
            double min = this.get("Min").getAsDouble();
            double max = this.get("Max").getAsDouble();
            return new DoubleRange.Normal(this.function.get(min), this.function.get(max));
         }
      } else {
         return (IDoubleRange)(this.default1 == this.default2
            ? new DoubleRange.Constant(this.function.get(this.default1))
            : new DoubleRange.Normal(this.function.get(this.default1), this.function.get(this.default2)));
      }
   }

   @Nonnull
   protected IDoubleRange loadThreshold() {
      JsonArray thresholdsJson = this.get("Thresholds").getAsJsonArray();
      JsonArray valuesJson = this.get("Values").getAsJsonArray();
      double[] thresholds = new double[thresholdsJson.size()];
      double[] values = new double[thresholdsJson.size()];

      for (int i = 0; i < thresholds.length; i++) {
         thresholds[i] = thresholdsJson.get(i).getAsDouble();
         values[i] = valuesJson.get(i).getAsDouble();
      }

      return new DoubleRange.Multiple(thresholds, values);
   }

   public interface Constants {
      String KEY_MIN = "Min";
      String KEY_MAX = "Max";
      String KEY_THRESHOLDS = "Thresholds";
      String KEY_VALUES = "Values";
      String ERROR_ARRAY_SIZE = "Range array contains %s values. Only 1 or 2 entries are allowed.";
      String ERROR_NO_MIN = "Minimum value of range is not defined. Keyword: Min";
      String ERROR_NO_MAX = "Maximum value of range is not defined. Keyword: Max";
   }

   @FunctionalInterface
   public interface DoubleToDoubleFunction {
      double get(double var1);
   }
}
