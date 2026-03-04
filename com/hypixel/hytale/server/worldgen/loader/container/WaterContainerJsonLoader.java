package com.hypixel.hytale.server.worldgen.loader.container;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.hypixel.hytale.procedurallib.condition.DefaultCoordinateCondition;
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
import com.hypixel.hytale.server.core.asset.type.fluid.Fluid;
import com.hypixel.hytale.server.worldgen.SeedStringResource;
import com.hypixel.hytale.server.worldgen.container.WaterContainer;
import com.hypixel.hytale.server.worldgen.util.ConstantNoiseProperty;
import java.nio.file.Path;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WaterContainerJsonLoader extends JsonLoader<SeedStringResource, WaterContainer> {
   public WaterContainerJsonLoader(@Nonnull SeedString<SeedStringResource> seed, Path dataFolder, JsonElement json) {
      super(seed.append(".WaterContainer"), dataFolder, json);
   }

   @Nonnull
   public WaterContainer load() {
      if (this.has("Block")) {
         String blockString = this.get("Block").getAsString();
         int index = BlockType.getAssetMap().getIndex(blockString);
         if (index == Integer.MIN_VALUE) {
            throw new Error(String.format("Could not find Fluid for fluid: %s", blockString.toString()));
         } else {
            IDoubleRange array = new DoubleRangeJsonLoader<>(this.seed, this.dataFolder, this.get("Height"), 0.0).load();
            NoiseProperty heightmapNoise = ConstantNoiseProperty.DEFAULT_ZERO;
            if (this.has("Heightmap")) {
               heightmapNoise = new NoisePropertyJsonLoader<>(this.seed, this.dataFolder, this.get("Heightmap")).load();
            }

            DoubleRangeNoiseSupplier height = new DoubleRangeNoiseSupplier(array, heightmapNoise);
            return new WaterContainer(
               new WaterContainer.Entry[]{
                  new WaterContainer.Entry(
                     index,
                     0,
                     new DoubleRangeNoiseSupplier(DoubleRange.ZERO, ConstantNoiseProperty.DEFAULT_ZERO),
                     height,
                     DefaultCoordinateCondition.DEFAULT_TRUE
                  )
               }
            );
         }
      } else if (this.has("Fluid")) {
         String fluidString = this.get("Fluid").getAsString();
         int index = Fluid.getAssetMap().getIndex(fluidString);
         if (index == Integer.MIN_VALUE) {
            throw new Error(String.format("Could not find Fluid for fluid: %s", fluidString));
         } else {
            IDoubleRange array = new DoubleRangeJsonLoader<>(this.seed, this.dataFolder, this.get("Height"), 0.0).load();
            NoiseProperty heightmapNoise = ConstantNoiseProperty.DEFAULT_ZERO;
            if (this.has("Heightmap")) {
               heightmapNoise = new NoisePropertyJsonLoader<>(this.seed, this.dataFolder, this.get("Heightmap")).load();
            }

            DoubleRangeNoiseSupplier height = new DoubleRangeNoiseSupplier(array, heightmapNoise);
            return new WaterContainer(
               new WaterContainer.Entry[]{
                  new WaterContainer.Entry(
                     0,
                     index,
                     new DoubleRangeNoiseSupplier(DoubleRange.ZERO, ConstantNoiseProperty.DEFAULT_ZERO),
                     height,
                     DefaultCoordinateCondition.DEFAULT_TRUE
                  )
               }
            );
         }
      } else {
         return new WaterContainer(this.loadEntries());
      }
   }

   @Nonnull
   private WaterContainer.Entry[] loadEntries() {
      if (!this.has("Entries")) {
         return WaterContainer.Entry.EMPTY_ARRAY;
      } else {
         JsonArray arr = this.get("Entries").getAsJsonArray();
         if (arr.isEmpty()) {
            return WaterContainer.Entry.EMPTY_ARRAY;
         } else {
            WaterContainer.Entry[] entries = new WaterContainer.Entry[arr.size()];

            for (int i = 0; i < arr.size(); i++) {
               try {
                  entries[i] = new WaterContainerJsonLoader.WaterContainerEntryJsonLoader(
                        this.seed.append(String.format("-%s", i)), this.dataFolder, arr.get(i)
                     )
                     .load();
               } catch (Throwable var5) {
                  throw new Error(String.format("Failed to load TintContainerEntry #%s", i), var5);
               }
            }

            return entries;
         }
      }
   }

   public interface Constants {
      String KEY_ENTRIES = "Entries";
      String KEY_ENTRY_BLOCK = "Block";
      String ERROR_ENTRY_NO_BLOCK = "Could not find block information. Keyword: Block";
      String ERROR_ENTRY_FLUID_BLOCK = "Could not find BlockType for block: %s";
      String KEY_ENTRY_FLUID = "Fluid";
      String ERROR_ENTRY_NO_FLUID = "Could not find fluid information. Keyword: Fluid";
      String ERROR_ENTRY_FLUID_TYPE = "Could not find Fluid for fluid: %s";
      String KEY_ENTRY_MIN = "Min";
      String KEY_ENTRY_MIN_NOISE = "MinNoise";
      String KEY_ENTRY_MAX = "Max";
      String KEY_ENTRY_MAX_NOISE = "MaxNoise";
      String KEY_ENTRY_NOISE_MASK = "NoiseMask";
      String ERROR_ENTRY_NO_MAX = "Could not find maximum of water container entry.";
   }

   private static class WaterContainerEntryJsonLoader extends JsonLoader<SeedStringResource, WaterContainer.Entry> {
      public WaterContainerEntryJsonLoader(@Nonnull SeedString<SeedStringResource> seed, Path dataFolder, JsonElement json) {
         super(seed.append(".Entry"), dataFolder, json);
      }

      @Nonnull
      public WaterContainer.Entry load() {
         try {
            if (this.has("Fluid")) {
               String fluidString = this.get("Fluid").getAsString();
               int index = Fluid.getAssetMap().getIndex(fluidString);
               if (index == Integer.MIN_VALUE) {
                  throw new Error(String.format("Could not find Fluid for fluid: %s", fluidString));
               } else {
                  return new WaterContainer.Entry(0, index, this.loadMin(), this.loadMax(), this.loadNoiseMask());
               }
            } else if (this.has("Block")) {
               String blockString = this.get("Block").getAsString();
               int index = BlockType.getAssetMap().getIndex(blockString);
               if (index == Integer.MIN_VALUE) {
                  throw new Error(String.format("Could not find Fluid for fluid: %s", blockString.toString()));
               } else {
                  return new WaterContainer.Entry(index, 0, this.loadMin(), this.loadMax(), this.loadNoiseMask());
               }
            } else {
               throw new IllegalArgumentException("Could not find fluid information. Keyword: Fluid");
            }
         } catch (Error var4) {
            throw new Error("Failed to load water container.", var4);
         }
      }

      @Nonnull
      private IDoubleCoordinateSupplier loadMin() {
         IDoubleRange array = new DoubleRangeJsonLoader<>(this.seed, this.dataFolder, this.get("Min"), 0.0).load();
         NoiseProperty minNoise = this.loadNoise("MinNoise");
         return new DoubleRangeNoiseSupplier(array, minNoise);
      }

      @Nonnull
      private IDoubleCoordinateSupplier loadMax() {
         if (!this.has("Max")) {
            throw new IllegalArgumentException("Could not find maximum of water container entry.");
         } else {
            IDoubleRange array = new DoubleRangeJsonLoader<>(this.seed, this.dataFolder, this.get("Max"), 0.0).load();
            NoiseProperty maxNoise = this.loadNoise("MaxNoise");
            return new DoubleRangeNoiseSupplier(array, maxNoise);
         }
      }

      @Nullable
      private NoiseProperty loadNoise(String key) {
         NoiseProperty maxNoise = ConstantNoiseProperty.DEFAULT_ZERO;
         if (this.has(key)) {
            maxNoise = new NoisePropertyJsonLoader<>(this.seed, this.dataFolder, this.get(key)).load();
         }

         return maxNoise;
      }

      @Nonnull
      private ICoordinateCondition loadNoiseMask() {
         ICoordinateCondition mask = DefaultCoordinateCondition.DEFAULT_TRUE;
         if (this.has("NoiseMask")) {
            mask = new NoiseMaskConditionJsonLoader<>(this.seed, this.dataFolder, this.get("NoiseMask")).load();
         }

         return mask;
      }
   }
}
