package com.hypixel.hytale.server.worldgen.loader.container;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.hypixel.hytale.common.map.IWeightedMap;
import com.hypixel.hytale.common.map.WeightedMap;
import com.hypixel.hytale.common.util.ArrayUtil;
import com.hypixel.hytale.procedurallib.condition.ICoordinateCondition;
import com.hypixel.hytale.procedurallib.json.JsonLoader;
import com.hypixel.hytale.procedurallib.json.NoiseMaskConditionJsonLoader;
import com.hypixel.hytale.procedurallib.json.NoisePropertyJsonLoader;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.procedurallib.property.NoiseProperty;
import com.hypixel.hytale.server.worldgen.SeedStringResource;
import com.hypixel.hytale.server.worldgen.container.TintContainer;
import com.hypixel.hytale.server.worldgen.loader.util.ColorUtil;
import com.hypixel.hytale.server.worldgen.util.ConstantNoiseProperty;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TintContainerJsonLoader extends JsonLoader<SeedStringResource, TintContainer> {
   public TintContainerJsonLoader(@Nonnull SeedString<SeedStringResource> seed, Path dataFolder, JsonElement json) {
      super(seed.append(".TintContainer"), dataFolder, json);
   }

   @Nonnull
   public TintContainer load() {
      return new TintContainer(this.loadDefault(), this.loadEntries());
   }

   @Nonnull
   protected TintContainer.DefaultTintContainerEntry loadDefault() {
      JsonElement element;
      if (this.json == null || this.json.isJsonNull() || this.json.isJsonArray()) {
         element = null;
      } else if (this.json.isJsonObject() && this.has("Default")) {
         element = this.get("Default");
      } else if (!this.json.isJsonPrimitive() && !this.json.isJsonObject()) {
         element = null;
      } else {
         element = this.json;
      }

      return new TintContainerJsonLoader.DefaultTintContainerEntryJsonLoader(this.seed, this.dataFolder, element).load();
   }

   @Nonnull
   protected List<TintContainer.TintContainerEntry> loadEntries() {
      if (!this.has("Entries")) {
         return Collections.emptyList();
      } else {
         JsonArray arr = this.get("Entries").getAsJsonArray();
         List<TintContainer.TintContainerEntry> entries = new ArrayList<>(arr.size());

         for (int i = 0; i < arr.size(); i++) {
            try {
               entries.add(
                  new TintContainerJsonLoader.TintContainerEntryJsonLoader(this.seed.append(String.format("-%s", i)), this.dataFolder, arr.get(i)).load()
               );
            } catch (Throwable var5) {
               throw new Error(String.format("Failed to load TintContainerEntry #%s", i), var5);
            }
         }

         return entries;
      }
   }

   public interface Constants {
      String KEY_DEFAULT = "Default";
      String KEY_ENTRIES = "Entries";
      String KEY_COLORS = "Colors";
      String KEY_WEIGHTS = "Weights";
      String KEY_ENTRY_NOISE = "Noise";
      String KEY_NOISE_MASK = "NoiseMask";
      String ERROR_COLORS_NOT_FOUND = "Could not find colors. Keyword: Colors";
      String ERROR_WEIGHT_SIZE = "Tint weights array size does not fit color array size.";
      String ERROR_NO_VALUE_NOISE = "Could not find value noise. Keyword: Noise";
      String ERROR_LOADING_ENTRY = "Failed to load TintContainerEntry #%s";
      String SEED_INDEX_SUFFIX = "-%s";
      int DEFAULT_TINT_COLOR = 16711680;
   }

   protected static class DefaultTintContainerEntryJsonLoader extends TintContainerJsonLoader.TintContainerEntryJsonLoader {
      public DefaultTintContainerEntryJsonLoader(@Nonnull SeedString<SeedStringResource> seed, Path dataFolder, JsonElement json) {
         super(seed.append(".DefaultTintContainerEntry"), dataFolder, json);
      }

      @Nonnull
      public TintContainer.DefaultTintContainerEntry load() {
         IWeightedMap<Integer> colorMapping;
         if (this.json == null || this.json.isJsonNull()) {
            WeightedMap.Builder<Integer> builder = WeightedMap.builder(ArrayUtil.EMPTY_INTEGER_ARRAY);
            builder.put(16711680, 1.0);
            colorMapping = builder.build();
         } else if (this.json.isJsonPrimitive()) {
            WeightedMap.Builder<Integer> builder = WeightedMap.builder(ArrayUtil.EMPTY_INTEGER_ARRAY);
            builder.put(ColorUtil.hexString(this.json.getAsString()), 1.0);
            colorMapping = builder.build();
         } else {
            colorMapping = this.loadColorMapping();
         }

         return new TintContainer.DefaultTintContainerEntry(colorMapping, colorMapping.size() > 1 ? this.loadValueNoise() : ConstantNoiseProperty.DEFAULT_ZERO);
      }
   }

   protected static class TintContainerEntryJsonLoader extends JsonLoader<SeedStringResource, TintContainer.TintContainerEntry> {
      public TintContainerEntryJsonLoader(@Nonnull SeedString<SeedStringResource> seed, Path dataFolder, JsonElement json) {
         super(seed.append(".TintContainerEntry"), dataFolder, json);
      }

      @Nonnull
      public TintContainer.TintContainerEntry load() {
         IWeightedMap<Integer> colorMapping = this.loadColorMapping();
         return new TintContainer.TintContainerEntry(
            colorMapping, colorMapping.size() > 1 ? this.loadValueNoise() : ConstantNoiseProperty.DEFAULT_ZERO, this.loadMapCondition()
         );
      }

      @Nonnull
      protected IWeightedMap<Integer> loadColorMapping() {
         WeightedMap.Builder<Integer> builder = WeightedMap.builder(ArrayUtil.EMPTY_INTEGER_ARRAY);
         if (this.json == null || this.json.isJsonNull()) {
            builder.put(16711680, 1.0);
         } else if (this.json.isJsonObject()) {
            if (!this.has("Colors")) {
               throw new IllegalArgumentException("Could not find colors. Keyword: Colors");
            }

            JsonElement colorsElement = this.get("Colors");
            if (colorsElement.isJsonArray()) {
               JsonArray colors = colorsElement.getAsJsonArray();
               JsonArray weights = this.has("Weights") ? this.get("Weights").getAsJsonArray() : null;
               if (weights != null && weights.size() != colors.size()) {
                  throw new IllegalArgumentException("Tint weights array size does not fit color array size.");
               }

               for (int i = 0; i < colors.size(); i++) {
                  int color = ColorUtil.hexString(colors.get(i).getAsString());
                  double weight = weights == null ? 1.0 : weights.get(i).getAsDouble();
                  builder.put(color, weight);
               }
            } else {
               int color = ColorUtil.hexString(colorsElement.getAsString());
               builder.put(color, 1.0);
            }
         } else {
            builder.put(ColorUtil.hexString(this.json.getAsString()), 1.0);
         }

         return builder.build();
      }

      @Nullable
      protected NoiseProperty loadValueNoise() {
         if (!this.has("Noise")) {
            throw new IllegalArgumentException("Could not find value noise. Keyword: Noise");
         } else {
            return new NoisePropertyJsonLoader<>(this.seed, this.dataFolder, this.get("Noise")).load();
         }
      }

      @Nonnull
      protected ICoordinateCondition loadMapCondition() {
         return new NoiseMaskConditionJsonLoader<>(this.seed, this.dataFolder, this.get("NoiseMask")).load();
      }
   }
}
