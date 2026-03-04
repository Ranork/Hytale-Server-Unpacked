package com.hypixel.hytale.server.worldgen.loader.container;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hypixel.hytale.common.map.IWeightedMap;
import com.hypixel.hytale.common.map.WeightedMap;
import com.hypixel.hytale.procedurallib.condition.ConstantBlockFluidCondition;
import com.hypixel.hytale.procedurallib.condition.DefaultCoordinateRndCondition;
import com.hypixel.hytale.procedurallib.condition.HeightCondition;
import com.hypixel.hytale.procedurallib.condition.IBlockFluidCondition;
import com.hypixel.hytale.procedurallib.condition.ICoordinateCondition;
import com.hypixel.hytale.procedurallib.condition.ICoordinateRndCondition;
import com.hypixel.hytale.procedurallib.json.HeightThresholdInterpreterJsonLoader;
import com.hypixel.hytale.procedurallib.json.JsonLoader;
import com.hypixel.hytale.procedurallib.json.NoiseMaskConditionJsonLoader;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.server.worldgen.SeedStringResource;
import com.hypixel.hytale.server.worldgen.container.CoverContainer;
import com.hypixel.hytale.server.worldgen.loader.util.ResolvedBlockArrayJsonLoader;
import com.hypixel.hytale.server.worldgen.util.BlockFluidEntry;
import com.hypixel.hytale.server.worldgen.util.ResolvedBlockArray;
import com.hypixel.hytale.server.worldgen.util.condition.HashSetBlockFluidCondition;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.nio.file.Path;
import java.util.Arrays;
import javax.annotation.Nonnull;

public class CoverContainerJsonLoader extends JsonLoader<SeedStringResource, CoverContainer> {
   public CoverContainerJsonLoader(SeedString<SeedStringResource> seed, Path dataFolder, JsonElement json) {
      super(seed, dataFolder, json);
   }

   @Nonnull
   public CoverContainer load() {
      CoverContainer.CoverContainerEntry[] coverContainerEntries;
      if (this.json == null || this.json.isJsonNull()) {
         coverContainerEntries = new CoverContainer.CoverContainerEntry[0];
      } else if (this.json.isJsonArray()) {
         JsonArray coversArray = this.json.getAsJsonArray();
         coverContainerEntries = new CoverContainer.CoverContainerEntry[coversArray.size()];

         for (int i = 0; i < coverContainerEntries.length; i++) {
            JsonObject coversObject = coversArray.get(i).getAsJsonObject();
            coverContainerEntries[i] = new CoverContainerJsonLoader.CoverContainerEntryJsonLoader(this.seed.append("-" + i), this.dataFolder, coversObject)
               .load();
         }
      } else {
         JsonObject coversObject = this.json.getAsJsonObject();
         coverContainerEntries = new CoverContainer.CoverContainerEntry[]{
            new CoverContainerJsonLoader.CoverContainerEntryJsonLoader(this.seed.append("-0"), this.dataFolder, coversObject).load()
         };
      }

      return new CoverContainer(coverContainerEntries);
   }

   public interface Constants {
      String KEY_ENTRY_TYPE = "Type";
      String KEY_ENTRY_WEIGHT = "Weight";
      String KEY_ENTRY_DENSITY = "Density";
      String KEY_ENTRY_NOISE_MASK = "NoiseMask";
      String KEY_ENTRY_HEIGHT_THRESHOLD = "HeightThreshold";
      String KEY_ENTRY_OFFSET = "Offset";
      String KEY_ENTRY_PARENT = "Parent";
      String KEY_ENTRY_ON_WATER = "OnWater";
      String ERROR_NO_TYPE = "Could not find type array for cover container! Keyword: Type";
      String ERROR_NO_ENTRIES = "There are no blocks in this cover container!";
      String ERROR_WEIGHTS_ARRAY_SIZE = "Weight array size does not equal size of types array";
      String ERROR_OFFSETS_ARRAY_SIZE = "Offset array size does not equal size of types array";
   }

   public static class CoverContainerEntryJsonLoader extends JsonLoader<SeedStringResource, CoverContainer.CoverContainerEntry> {
      public CoverContainerEntryJsonLoader(@Nonnull SeedString<SeedStringResource> seed, Path dataFolder, JsonElement json) {
         super(seed.append(".CoverContainerEntry"), dataFolder, json);
      }

      @Nonnull
      public CoverContainer.CoverContainerEntry load() {
         return new CoverContainer.CoverContainerEntry(
            this.loadEntries(), this.loadMapCondition(), this.loadHeightCondition(), this.loadParents(), this.loadDensity(), this.loadOnWater()
         );
      }

      @Nonnull
      protected IWeightedMap<CoverContainer.CoverContainerEntry.CoverContainerEntryPart> loadEntries() {
         if (!this.has("Type")) {
            throw new IllegalArgumentException("Could not find type array for cover container! Keyword: Type");
         } else {
            ResolvedBlockArray types = new ResolvedBlockArrayJsonLoader(this.seed, this.dataFolder, this.get("Type")).load();
            int[] offsets = this.loadOffsetArray(types.size());
            JsonArray weights = this.has("Weight") ? this.get("Weight").getAsJsonArray() : null;
            if (weights != null && weights.size() != types.size()) {
               throw new IllegalArgumentException("Weight array size does not equal size of types array");
            } else {
               WeightedMap.Builder<CoverContainer.CoverContainerEntry.CoverContainerEntryPart> builder = WeightedMap.builder(
                  CoverContainer.CoverContainerEntry.CoverContainerEntryPart.EMPTY_ARRAY
               );

               for (int i = 0; i < types.size(); i++) {
                  BlockFluidEntry blockEntry = types.getEntries()[i];
                  int offset = offsets[i];
                  double weight = weights == null ? 1.0 : weights.get(i).getAsDouble();
                  CoverContainer.CoverContainerEntry.CoverContainerEntryPart entry = new CoverContainer.CoverContainerEntry.CoverContainerEntryPart(
                     blockEntry, offset
                  );
                  builder.put(entry, weight);
               }

               if (builder.size() <= 0) {
                  throw new IllegalArgumentException("There are no blocks in this cover container!");
               } else {
                  return builder.build();
               }
            }
         }
      }

      protected int[] loadOffsetArray(int length) {
         JsonElement offsetElement = this.get("Offset");
         int[] offsets = new int[length];
         if (offsetElement == null || offsetElement.isJsonNull()) {
            Arrays.fill(offsets, 0);
         } else if (offsetElement.isJsonArray()) {
            JsonArray offsetArray = offsetElement.getAsJsonArray();
            if (offsetArray.size() != length) {
               throw new IllegalArgumentException("Offset array size does not equal size of types array");
            }

            for (int i = 0; i < length; i++) {
               offsets[i] = offsetArray.get(i).getAsInt();
            }
         } else {
            int offset = offsetElement.getAsInt();
            Arrays.fill(offsets, offset);
         }

         return offsets;
      }

      protected double loadDensity() {
         double density = 1.0;
         if (this.has("Density")) {
            density = this.get("Density").getAsDouble();
         }

         return density;
      }

      @Nonnull
      protected ICoordinateCondition loadMapCondition() {
         return new NoiseMaskConditionJsonLoader<>(this.seed, this.dataFolder, this.get("NoiseMask")).load();
      }

      @Nonnull
      protected ICoordinateRndCondition loadHeightCondition() {
         ICoordinateRndCondition heightThreshold = DefaultCoordinateRndCondition.DEFAULT_TRUE;
         if (this.has("HeightThreshold")) {
            heightThreshold = new HeightCondition(
               new HeightThresholdInterpreterJsonLoader<>(this.seed, this.dataFolder, this.get("HeightThreshold"), 320).load()
            );
         }

         return heightThreshold;
      }

      @Nonnull
      protected IBlockFluidCondition loadParents() {
         IBlockFluidCondition parentMask = ConstantBlockFluidCondition.DEFAULT_TRUE;
         if (this.has("Parent")) {
            ResolvedBlockArray blockArray = new ResolvedBlockArrayJsonLoader(this.seed, this.dataFolder, this.get("Parent")).load();
            LongSet biomeSet = blockArray.getEntrySet();
            parentMask = new HashSetBlockFluidCondition(biomeSet);
         }

         return parentMask;
      }

      protected boolean loadOnWater() {
         return this.has("OnWater") && this.get("OnWater").getAsBoolean();
      }
   }
}
