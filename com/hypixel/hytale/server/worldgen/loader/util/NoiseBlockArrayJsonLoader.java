package com.hypixel.hytale.server.worldgen.loader.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.hypixel.hytale.procedurallib.json.DoubleRangeJsonLoader;
import com.hypixel.hytale.procedurallib.json.JsonLoader;
import com.hypixel.hytale.procedurallib.json.NoisePropertyJsonLoader;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.procedurallib.property.NoiseProperty;
import com.hypixel.hytale.procedurallib.supplier.DoubleRange;
import com.hypixel.hytale.procedurallib.supplier.IDoubleRange;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.fluid.Fluid;
import com.hypixel.hytale.server.core.prefab.selection.mask.BlockPattern;
import com.hypixel.hytale.server.worldgen.SeedStringResource;
import com.hypixel.hytale.server.worldgen.util.BlockFluidEntry;
import com.hypixel.hytale.server.worldgen.util.ConstantNoiseProperty;
import com.hypixel.hytale.server.worldgen.util.NoiseBlockArray;
import java.nio.file.Path;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NoiseBlockArrayJsonLoader extends JsonLoader<SeedStringResource, NoiseBlockArray> {
   public NoiseBlockArrayJsonLoader(@Nonnull SeedString<SeedStringResource> seed, Path dataFolder, JsonElement json) {
      super(seed.append(".NoiseBlockArray"), dataFolder, json);
   }

   @Nonnull
   public NoiseBlockArray load() {
      if (this.json.isJsonPrimitive()) {
         return new NoiseBlockArray(new NoiseBlockArray.Entry[]{new NoiseBlockArrayJsonLoader.EntryJsonLoader(this.seed, this.dataFolder, this.json).load()});
      } else if (this.json.isJsonObject() && !this.has("Entries")) {
         return new NoiseBlockArray(new NoiseBlockArray.Entry[]{this.loadEntry(this.json, 0)});
      } else {
         JsonElement entriesElement;
         if (this.json.isJsonArray()) {
            entriesElement = this.json;
         } else {
            entriesElement = this.get("Entries");
         }

         if (entriesElement != null && !entriesElement.isJsonNull()) {
            JsonArray entriesArray = entriesElement.getAsJsonArray();
            NoiseBlockArray.Entry[] entries = new NoiseBlockArray.Entry[entriesArray.size()];

            for (int i = 0; i < entriesArray.size(); i++) {
               try {
                  entries[i] = this.loadEntry(entriesArray.get(i), i);
               } catch (Throwable var6) {
                  throw new Error(String.format("Failed to load block array entry #%s", i), var6);
               }
            }

            return new NoiseBlockArray(entries);
         } else {
            throw new IllegalArgumentException("Could not find entries in block array. Keyword: Entries");
         }
      }
   }

   @Nonnull
   protected NoiseBlockArray.Entry loadEntry(JsonElement element, int i) {
      return new NoiseBlockArrayJsonLoader.EntryJsonLoader(this.seed.append("-" + i), this.dataFolder, element).load();
   }

   public interface Constants {
      String KEY_ENTRIES = "Entries";
      String KEY_ENTRY_TYPE = "Type";
      String KEY_ENTRY_REPEAT = "Repeat";
      String KEY_ENTRY_REPEAT_NOISE = "RepeatNoise";
      String ERROR_NO_ENTRIES = "Could not find entries in block array. Keyword: Entries";
      String ERROR_ENTRY_FAIL = "Failed to load block array entry #%s";
   }

   protected static class EntryJsonLoader extends JsonLoader<SeedStringResource, NoiseBlockArray.Entry> {
      public EntryJsonLoader(@Nonnull SeedString<SeedStringResource> seed, Path dataFolder, JsonElement json) {
         super(seed.append(".Entry"), dataFolder, json);
      }

      @Nonnull
      public NoiseBlockArray.Entry load() {
         if (this.json.isJsonPrimitive()) {
            String blockName = this.json.getAsString();
            int repetitions = 1;
            if (blockName.contains(":")) {
               String[] parts = blockName.split(":");
               blockName = parts[0];
               repetitions = Integer.parseInt(parts[1]);
            }

            BlockFluidEntry blockEntry = this.resolveBlockId(blockName);
            return new NoiseBlockArray.Entry(blockName, blockEntry, new DoubleRange.Constant(repetitions), ConstantNoiseProperty.DEFAULT_ZERO);
         } else if (this.json.isJsonObject()) {
            String blockName = this.get("Type").getAsString();
            BlockFluidEntry blockEntry = this.resolveBlockId(blockName);
            IDoubleRange repetitions = this.loadRepetitions();
            NoiseProperty noise = this.loadNoise();
            return new NoiseBlockArray.Entry(blockName, blockEntry, repetitions, noise);
         } else {
            throw new IllegalArgumentException("Unsupported Json Entry: " + this.json);
         }
      }

      @Nullable
      protected IDoubleRange loadRepetitions() {
         return new DoubleRangeJsonLoader<>(this.seed, this.dataFolder, this.get("Repeat"), 1.0).load();
      }

      @Nullable
      protected NoiseProperty loadNoise() {
         NoiseProperty noise = ConstantNoiseProperty.DEFAULT_ZERO;
         if (this.has("RepeatNoise")) {
            noise = new NoisePropertyJsonLoader<>(this.seed, this.dataFolder, this.get("RepeatNoise")).load();
         }

         return noise;
      }

      @Nonnull
      protected BlockFluidEntry resolveBlockId(@Nonnull String name) {
         try {
            if (name.startsWith("fluid#")) {
               String key = name.substring("fluid#".length());
               int index = Fluid.getAssetMap().getIndex(key);
               if (index == Integer.MIN_VALUE) {
                  throw new IllegalArgumentException("Unknown key! " + key);
               } else {
                  return new BlockFluidEntry(0, 0, index);
               }
            } else {
               BlockPattern.BlockEntry key = BlockPattern.BlockEntry.decode(name);
               int index = BlockType.getAssetMap().getIndex(key.blockTypeKey());
               if (index == Integer.MIN_VALUE) {
                  throw new IllegalArgumentException("Unknown key! " + key);
               } else {
                  return new BlockFluidEntry(index, key.rotation(), 0);
               }
            }
         } catch (IllegalArgumentException var4) {
            throw new Error("BlockLayer does not exist in BlockTypes", var4);
         }
      }
   }
}
