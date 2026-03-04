package com.hypixel.hytale.procedurallib.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.hypixel.hytale.procedurallib.supplier.FloatRange;
import com.hypixel.hytale.procedurallib.supplier.IFloatRange;
import java.nio.file.Path;
import java.util.Objects;
import javax.annotation.Nonnull;

public class FloatRangeJsonLoader<K extends SeedResource> extends JsonLoader<K, IFloatRange> {
   protected final float default1;
   protected final float default2;
   @Nonnull
   protected final FloatRangeJsonLoader.FloatToFloatFunction function;

   public FloatRangeJsonLoader(@Nonnull SeedString<K> seed, Path dataFolder, JsonElement json) {
      this(seed, dataFolder, json, 0.0F, d -> d);
   }

   public FloatRangeJsonLoader(@Nonnull SeedString<K> seed, Path dataFolder, JsonElement json, FloatRangeJsonLoader.FloatToFloatFunction function) {
      this(seed, dataFolder, json, 0.0F, function);
   }

   public FloatRangeJsonLoader(@Nonnull SeedString<K> seed, Path dataFolder, JsonElement json, float default1) {
      this(seed, dataFolder, json, default1, default1, d -> d);
   }

   public FloatRangeJsonLoader(
      @Nonnull SeedString<K> seed, Path dataFolder, JsonElement json, float default1, FloatRangeJsonLoader.FloatToFloatFunction function
   ) {
      this(seed, dataFolder, json, default1, default1, function);
   }

   public FloatRangeJsonLoader(@Nonnull SeedString<K> seed, Path dataFolder, JsonElement json, float default1, float default2) {
      this(seed, dataFolder, json, default1, default2, d -> d);
   }

   public FloatRangeJsonLoader(
      @Nonnull SeedString<K> seed, Path dataFolder, JsonElement json, float default1, float default2, FloatRangeJsonLoader.FloatToFloatFunction function
   ) {
      super(seed.append(".DoubleRange"), dataFolder, json);
      this.default1 = default1;
      this.default2 = default2;
      this.function = Objects.requireNonNull(function);
   }

   @Nonnull
   public IFloatRange load() {
      if (this.json != null && !this.json.isJsonNull()) {
         if (this.json.isJsonArray()) {
            JsonArray array = this.json.getAsJsonArray();
            if (array.size() != 1 && array.size() != 2) {
               throw new IllegalStateException(String.format("Range array contains %s values. Only 1 or 2 entries are allowed.", array.size()));
            } else {
               return (IFloatRange)(array.size() == 1
                  ? new FloatRange.Constant(this.function.get(array.get(0).getAsFloat()))
                  : new FloatRange.Normal(this.function.get(array.get(0).getAsFloat()), this.function.get(array.get(1).getAsFloat())));
            }
         } else if (!this.json.isJsonObject()) {
            return new FloatRange.Constant(this.function.get(this.json.getAsFloat()));
         } else if (!this.has("Min")) {
            throw new IllegalStateException("Minimum value of range is not defined. Keyword: Min");
         } else if (!this.has("Max")) {
            throw new IllegalStateException("Maximum value of range is not defined. Keyword: Max");
         } else {
            float min = this.get("Min").getAsFloat();
            float max = this.get("Max").getAsFloat();
            return new FloatRange.Normal(this.function.get(min), this.function.get(max));
         }
      } else {
         return (IFloatRange)(this.default1 == this.default2
            ? new FloatRange.Constant(this.function.get(this.default1))
            : new FloatRange.Normal(this.function.get(this.default1), this.function.get(this.default2)));
      }
   }

   public interface Constants {
      String KEY_MIN = "Min";
      String KEY_MAX = "Max";
      String ERROR_ARRAY_SIZE = "Range array contains %s values. Only 1 or 2 entries are allowed.";
      String ERROR_NO_MIN = "Minimum value of range is not defined. Keyword: Min";
      String ERROR_NO_MAX = "Maximum value of range is not defined. Keyword: Max";
   }

   @FunctionalInterface
   public interface FloatToFloatFunction {
      float get(float var1);
   }
}
