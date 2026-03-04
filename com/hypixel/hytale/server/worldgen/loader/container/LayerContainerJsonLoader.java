package com.hypixel.hytale.server.worldgen.loader.container;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.hypixel.hytale.procedurallib.condition.ICoordinateCondition;
import com.hypixel.hytale.procedurallib.json.DoubleRangeJsonLoader;
import com.hypixel.hytale.procedurallib.json.JsonLoader;
import com.hypixel.hytale.procedurallib.json.NoiseMaskConditionJsonLoader;
import com.hypixel.hytale.procedurallib.json.NoisePropertyJsonLoader;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.procedurallib.property.NoiseProperty;
import com.hypixel.hytale.procedurallib.supplier.DoubleRange;
import com.hypixel.hytale.procedurallib.supplier.DoubleRangeNoiseSupplier;
import com.hypixel.hytale.procedurallib.supplier.IDoubleCoordinateSupplier;
import com.hypixel.hytale.procedurallib.supplier.IDoubleRange;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.environment.config.Environment;
import com.hypixel.hytale.server.worldgen.SeedStringResource;
import com.hypixel.hytale.server.worldgen.container.LayerContainer;
import com.hypixel.hytale.server.worldgen.loader.util.NoiseBlockArrayJsonLoader;
import com.hypixel.hytale.server.worldgen.util.ConstantNoiseProperty;
import com.hypixel.hytale.server.worldgen.util.NoiseBlockArray;
import java.nio.file.Path;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LayerContainerJsonLoader extends JsonLoader<SeedStringResource, LayerContainer> {
   public LayerContainerJsonLoader(@Nonnull SeedString<SeedStringResource> seed, Path dataFolder, JsonElement json) {
      super(seed.append(".LayerContainer"), dataFolder, json);
   }

   @Nonnull
   public LayerContainer load() {
      return new LayerContainer(this.loadDefault(), this.loadDefaultEnvironment(), this.loadStaticLayers(), this.loadDynamicLayers());
   }

   protected int loadDefault() {
      if (!this.has("Default")) {
         throw new IllegalArgumentException("Could not find default material. Keyword: Default");
      } else {
         String blockName = this.get("Default").getAsString();
         int index = BlockType.getAssetMap().getIndex(blockName);
         if (index == Integer.MIN_VALUE) {
            throw new Error(String.format("Default block for LayerContainer could not be found! BlockType: %s", blockName));
         } else {
            return index;
         }
      }
   }

   protected int loadDefaultEnvironment() {
      int environment = Integer.MIN_VALUE;
      if (this.has("Environment")) {
         String environmentId = this.get("Environment").getAsString();
         environment = Environment.getAssetMap().getIndex(environmentId);
         if (environment == Integer.MIN_VALUE) {
            throw new Error(String.format("Error while looking up environment \"%s\"!", environmentId));
         }
      }

      return environment;
   }

   @Nonnull
   protected LayerContainer.StaticLayer[] loadStaticLayers() {
      if (!this.has("Static")) {
         return new LayerContainer.StaticLayer[0];
      } else {
         JsonArray array = this.get("Static").getAsJsonArray();
         LayerContainer.StaticLayer[] layers = new LayerContainer.StaticLayer[array.size()];

         for (int i = 0; i < layers.length; i++) {
            try {
               layers[i] = new LayerContainerJsonLoader.StaticLayerJsonLoader(this.seed.append("-" + i), this.dataFolder, array.get(i)).load();
            } catch (Throwable var5) {
               throw new Error(String.format("Error while loading StaticLayer #%s", i), var5);
            }
         }

         return layers;
      }
   }

   @Nonnull
   protected LayerContainer.DynamicLayer[] loadDynamicLayers() {
      if (!this.has("Dynamic")) {
         return new LayerContainer.DynamicLayer[0];
      } else {
         JsonArray array = this.get("Dynamic").getAsJsonArray();
         LayerContainer.DynamicLayer[] layers = new LayerContainer.DynamicLayer[array.size()];

         for (int i = 0; i < layers.length; i++) {
            try {
               layers[i] = new LayerContainerJsonLoader.DynamicLayerJsonLoader(this.seed.append("-" + i), this.dataFolder, array.get(i)).load();
            } catch (Throwable var5) {
               throw new Error(String.format("Error while loading DynamicLayer #%s", i), var5);
            }
         }

         return layers;
      }
   }

   public interface Constants {
      String KEY_DEFAULT = "Default";
      String KEY_DYNAMIC = "Dynamic";
      String KEY_STATIC = "Static";
      String KEY_ENTRY_ENTRIES = "Entries";
      String KEY_ENTRY_BLOCKS = "Blocks";
      String KEY_ENTRY_NOISE_MASK = "NoiseMask";
      String KEY_ENTRY_DYNAMIC_OFFSET = "Offset";
      String KEY_ENTRY_DYNAMIC_OFFSET_NOISE = "OffsetNoise";
      String KEY_ENTRY_STATIC_MIN = "Min";
      String KEY_ENTRY_STATIC_MIN_NOISE = "MinNoise";
      String KEY_ENTRY_STATIC_MAX = "Max";
      String KEY_ENTRY_STATIC_MAX_NOISE = "MaxNoise";
      String KEY_ENVIRONMENT = "Environment";
      String ERROR_NO_DEFAULT = "Could not find default material. Keyword: Default";
      String ERROR_DEFAULT_INVALID = "Default block for LayerContainer could not be found! BlockType: %s";
      String ERROR_FAIL_DYNAMIC_LAYER = "Error while loading DynamicLayer #%s";
      String ERROR_FAIL_STATIC_LAYER = "Error while loading StaticLayer #%s";
      String ERROR_NO_BLOCKS = "Could not find block data for layer entry. Keyword: Blocks";
      String ERROR_UNKOWN_STATIC = "Unknown type for static Layer";
      String ERROR_UNKOWN_DYNAMIC = "Unknown type for dynamic Layer";
      String ERROR_FAIL_DYNAMIC_ENTRY = "Error while loading DynamicLayerEntry #%s";
      String ERROR_FAIL_STATIC_ENTRY = "Error while loading StaticLayerEntry #%s";
      String ERROR_STATIC_NO_MIN = "Could not find minimum of static layer entry.";
      String ERROR_STATIC_NO_MAX = "Could not find maximum of static layer entry.";
   }

   protected static class DynamicLayerJsonLoader extends JsonLoader<SeedStringResource, LayerContainer.DynamicLayer> {
      public DynamicLayerJsonLoader(@Nonnull SeedString<SeedStringResource> seed, Path dataFolder, JsonElement json) {
         super(seed.append(".DynamicLayer"), dataFolder, json);
      }

      @Nonnull
      public LayerContainer.DynamicLayer load() {
         return new LayerContainer.DynamicLayer(this.loadEntries(), this.loadMapCondition(), this.loadEnvironment(), this.loadOffset());
      }

      @Nonnull
      protected ICoordinateCondition loadMapCondition() {
         return new NoiseMaskConditionJsonLoader<>(this.seed, this.dataFolder, this.get("NoiseMask")).load();
      }

      @Nonnull
      protected IDoubleCoordinateSupplier loadOffset() {
         IDoubleRange offset = DoubleRange.ZERO;
         NoiseProperty offsetNoise = ConstantNoiseProperty.DEFAULT_ZERO;
         if (this.has("Offset")) {
            offset = new DoubleRangeJsonLoader<>(this.seed, this.dataFolder, this.get("Offset")).load();
            if (this.has("OffsetNoise")) {
               offsetNoise = new NoisePropertyJsonLoader<>(this.seed, this.dataFolder, this.get("OffsetNoise")).load();
            }
         }

         return new DoubleRangeNoiseSupplier(offset, offsetNoise);
      }

      @Nonnull
      protected LayerContainer.DynamicLayerEntry[] loadEntries() {
         if (this.json == null || this.json.isJsonNull()) {
            return new LayerContainer.DynamicLayerEntry[0];
         } else if (!this.json.isJsonObject()) {
            throw new Error("Unknown type for dynamic Layer");
         } else if (!this.has("Entries")) {
            try {
               return new LayerContainer.DynamicLayerEntry[]{
                  new LayerContainerJsonLoader.DynamicLayerJsonLoader.DynamicLayerEntryJsonLoader(this.seed, this.dataFolder, this.json).load()
               };
            } catch (Throwable var6) {
               throw new Error(String.format("Error while loading DynamicLayerEntry #%s", 0), var6);
            }
         } else {
            JsonArray array = this.get("Entries").getAsJsonArray();
            LayerContainer.DynamicLayerEntry[] entries = new LayerContainer.DynamicLayerEntry[array.size()];

            for (int i = 0; i < entries.length; i++) {
               try {
                  entries[i] = new LayerContainerJsonLoader.DynamicLayerJsonLoader.DynamicLayerEntryJsonLoader(
                        this.seed.append("-" + i), this.dataFolder, array.get(i)
                     )
                     .load();
               } catch (Throwable var5) {
                  throw new Error(String.format("Error while loading DynamicLayerEntry #%s", i), var5);
               }
            }

            return entries;
         }
      }

      protected int loadEnvironment() {
         int environment = Integer.MIN_VALUE;
         if (this.has("Environment")) {
            String environmentId = this.get("Environment").getAsString();
            environment = Environment.getAssetMap().getIndex(environmentId);
            if (environment == Integer.MIN_VALUE) {
               throw new Error(String.format("Error while looking up environment \"%s\"!", environmentId));
            }
         }

         return environment;
      }

      protected static class DynamicLayerEntryJsonLoader extends LayerContainerJsonLoader.LayerEntryJsonLoader<LayerContainer.DynamicLayerEntry> {
         public DynamicLayerEntryJsonLoader(@Nonnull SeedString<SeedStringResource> seed, Path dataFolder, JsonElement json) {
            super(seed, dataFolder, json);
         }

         @Nonnull
         public LayerContainer.DynamicLayerEntry load() {
            return new LayerContainer.DynamicLayerEntry(this.loadBlocks(), this.loadMapCondition());
         }
      }
   }

   protected abstract static class LayerEntryJsonLoader<T extends LayerContainer.LayerEntry> extends JsonLoader<SeedStringResource, T> {
      public LayerEntryJsonLoader(@Nonnull SeedString<SeedStringResource> seed, Path dataFolder, JsonElement json) {
         super(seed.append(".LayerEntry"), dataFolder, json);
      }

      @Nonnull
      protected NoiseBlockArray loadBlocks() {
         return !this.has("Blocks") ? NoiseBlockArray.EMPTY : new NoiseBlockArrayJsonLoader(this.seed, this.dataFolder, this.get("Blocks")).load();
      }

      @Nonnull
      protected ICoordinateCondition loadMapCondition() {
         return new NoiseMaskConditionJsonLoader<>(this.seed, this.dataFolder, this.get("NoiseMask")).load();
      }
   }

   protected static class StaticLayerJsonLoader extends JsonLoader<SeedStringResource, LayerContainer.StaticLayer> {
      public StaticLayerJsonLoader(@Nonnull SeedString<SeedStringResource> seed, Path dataFolder, JsonElement json) {
         super(seed.append(".StaticLayer"), dataFolder, json);
      }

      @Nonnull
      public LayerContainer.StaticLayer load() {
         return new LayerContainer.StaticLayer(this.loadEntries(), this.loadMapCondition(), this.loadEnvironment());
      }

      @Nonnull
      protected ICoordinateCondition loadMapCondition() {
         return new NoiseMaskConditionJsonLoader<>(this.seed, this.dataFolder, this.get("NoiseMask")).load();
      }

      @Nonnull
      protected LayerContainer.StaticLayerEntry[] loadEntries() {
         if (this.json == null || this.json.isJsonNull()) {
            return new LayerContainer.StaticLayerEntry[0];
         } else if (!this.json.isJsonObject()) {
            throw new Error("Unknown type for static Layer");
         } else if (!this.has("Entries")) {
            try {
               return new LayerContainer.StaticLayerEntry[]{
                  new LayerContainerJsonLoader.StaticLayerJsonLoader.StaticLayerEntryJsonLoader(this.seed, this.dataFolder, this.json).load()
               };
            } catch (Throwable var6) {
               throw new Error(String.format("Error while loading StaticLayerEntry #%s", 0), var6);
            }
         } else {
            JsonArray array = this.get("Entries").getAsJsonArray();
            LayerContainer.StaticLayerEntry[] entries = new LayerContainer.StaticLayerEntry[array.size()];

            for (int i = 0; i < entries.length; i++) {
               try {
                  entries[i] = new LayerContainerJsonLoader.StaticLayerJsonLoader.StaticLayerEntryJsonLoader(
                        this.seed.append("-" + i), this.dataFolder, array.get(i)
                     )
                     .load();
               } catch (Throwable var5) {
                  throw new Error(String.format("Error while loading StaticLayerEntry #%s", i), var5);
               }
            }

            return entries;
         }
      }

      protected int loadEnvironment() {
         int environment = Integer.MIN_VALUE;
         if (this.has("Environment")) {
            String environmentId = this.get("Environment").getAsString();
            environment = Environment.getAssetMap().getIndex(environmentId);
            if (environment == Integer.MIN_VALUE) {
               throw new Error(String.format("Error while looking up environment \"%s\"!", environmentId));
            }
         }

         return environment;
      }

      protected static class StaticLayerEntryJsonLoader extends LayerContainerJsonLoader.LayerEntryJsonLoader<LayerContainer.StaticLayerEntry> {
         public StaticLayerEntryJsonLoader(@Nonnull SeedString<SeedStringResource> seed, Path dataFolder, JsonElement json) {
            super(seed.append(".StaticLayerEntry"), dataFolder, json);
         }

         @Nonnull
         public LayerContainer.StaticLayerEntry load() {
            return new LayerContainer.StaticLayerEntry(this.loadBlocks(), this.loadMapCondition(), this.loadMin(), this.loadMax());
         }

         @Nonnull
         protected IDoubleCoordinateSupplier loadMin() {
            if (!this.has("Min")) {
               throw new IllegalArgumentException("Could not find minimum of static layer entry.");
            } else {
               IDoubleRange array = new DoubleRangeJsonLoader<>(this.seed, this.dataFolder, this.get("Min"), 0.0).load();
               NoiseProperty minNoise = this.loadMinNoise();
               return new DoubleRangeNoiseSupplier(array, minNoise);
            }
         }

         @Nonnull
         protected IDoubleCoordinateSupplier loadMax() {
            if (!this.has("Max")) {
               throw new IllegalArgumentException("Could not find maximum of static layer entry.");
            } else {
               IDoubleRange array = new DoubleRangeJsonLoader<>(this.seed, this.dataFolder, this.get("Max"), 0.0).load();
               NoiseProperty maxNoise = this.loadMaxNoise();
               return new DoubleRangeNoiseSupplier(array, maxNoise);
            }
         }

         @Nullable
         protected NoiseProperty loadMinNoise() {
            NoiseProperty minNoise = ConstantNoiseProperty.DEFAULT_ZERO;
            if (this.has("MinNoise")) {
               minNoise = new NoisePropertyJsonLoader<>(this.seed, this.dataFolder, this.get("MinNoise")).load();
            }

            return minNoise;
         }

         @Nullable
         protected NoiseProperty loadMaxNoise() {
            NoiseProperty maxNoise = ConstantNoiseProperty.DEFAULT_ZERO;
            if (this.has("MaxNoise")) {
               maxNoise = new NoisePropertyJsonLoader<>(this.seed, this.dataFolder, this.get("MaxNoise")).load();
            }

            return maxNoise;
         }
      }
   }
}
