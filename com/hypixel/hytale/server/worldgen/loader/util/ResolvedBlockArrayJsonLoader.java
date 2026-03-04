package com.hypixel.hytale.server.worldgen.loader.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hypixel.hytale.assetstore.map.BlockTypeAssetMap;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.procedurallib.json.JsonLoader;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.fluid.Fluid;
import com.hypixel.hytale.server.core.prefab.selection.mask.BlockPattern;
import com.hypixel.hytale.server.worldgen.SeedStringResource;
import com.hypixel.hytale.server.worldgen.util.BlockFluidEntry;
import com.hypixel.hytale.server.worldgen.util.ResolvedBlockArray;
import java.nio.file.Path;
import javax.annotation.Nonnull;

public class ResolvedBlockArrayJsonLoader extends JsonLoader<SeedStringResource, ResolvedBlockArray> {
   public ResolvedBlockArrayJsonLoader(@Nonnull SeedString<SeedStringResource> seed, Path dataFolder, JsonElement json) {
      super(seed.append("ResolvedBlockArray"), dataFolder, json);
   }

   public ResolvedBlockArray load() {
      if (this.json != null && !this.json.isJsonNull()) {
         if (!this.json.isJsonArray()) {
            return this.json.isJsonObject() ? loadSingleBlock(this.json.getAsJsonObject()) : this.loadSingleBlock(this.json.getAsString());
         } else {
            JsonArray jsonArray = this.json.getAsJsonArray();
            if (jsonArray.size() == 1) {
               return jsonArray.get(0).isJsonObject()
                  ? loadSingleBlock(jsonArray.get(0).getAsJsonObject())
                  : this.loadSingleBlock(jsonArray.get(0).getAsString());
            } else {
               BlockTypeAssetMap<String, BlockType> assetMap = BlockType.getAssetMap();
               IndexedLookupTableAssetMap<String, Fluid> fluidMap = Fluid.getAssetMap();
               BlockFluidEntry[] blocks = new BlockFluidEntry[jsonArray.size()];

               for (int k = 0; k < blocks.length; k++) {
                  JsonElement elm = jsonArray.get(k);
                  if (elm.isJsonObject()) {
                     JsonObject obj = elm.getAsJsonObject();
                     int blockIndex = 0;
                     int rotation = 0;
                     int fluidIndex = 0;
                     if (obj.has("Block")) {
                        BlockPattern.BlockEntry key = BlockPattern.BlockEntry.decode(obj.get("Block").getAsString());
                        int index = BlockType.getBlockIdOrUnknown(key.blockTypeKey(), "Failed to find block '%s' in resolved block array!", key.blockTypeKey());
                        if (index == Integer.MIN_VALUE) {
                           throw new IllegalArgumentException("Unknown key! " + key);
                        }

                        blockIndex = index;
                        rotation = key.rotation();
                     }

                     if (obj.has("Fluid")) {
                        String key = obj.get("Fluid").getAsString();
                        int index = Fluid.getFluidIdOrUnknown(key, "Failed to find fluid '%s' in resolved block array!", key);
                        if (index == Integer.MIN_VALUE) {
                           throw new IllegalArgumentException("Unknown key! " + key);
                        }

                        fluidIndex = index;
                     }

                     blocks[k] = new BlockFluidEntry(blockIndex, rotation, fluidIndex);
                  } else {
                     String blockName = elm.getAsString();

                     try {
                        BlockPattern.BlockEntry key = BlockPattern.BlockEntry.decode(blockName);
                        int index = BlockType.getBlockIdOrUnknown(key.blockTypeKey(), "Failed to find block '%s' in resolved block array!", key.blockTypeKey());
                        if (index == Integer.MIN_VALUE) {
                           throw new IllegalArgumentException("Unknown key! " + key);
                        }

                        blocks[k] = new BlockFluidEntry(index, key.rotation(), 0);
                     } catch (IllegalArgumentException var13) {
                        throw new IllegalArgumentException("BlockLayer " + blockName + " does not exist in BlockTypes", var13);
                     }
                  }
               }

               return new ResolvedBlockArray(blocks);
            }
         }
      } else {
         return ResolvedBlockArray.EMPTY;
      }
   }

   @Nonnull
   public ResolvedBlockArray loadSingleBlock(@Nonnull String blockName) {
      BlockTypeAssetMap<String, BlockType> assetMap = BlockType.getAssetMap();

      try {
         BlockPattern.BlockEntry key = BlockPattern.BlockEntry.decode(blockName);
         int index = assetMap.getIndex(key.blockTypeKey());
         if (index == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unknown key! " + key);
         } else {
            long mapIndex = MathUtil.packLong(index, 0);
            if (key.rotation() == 0) {
               ResolvedBlockArray cachedResolvedBlockArray = (ResolvedBlockArray)ResolvedBlockArray.RESOLVED_BLOCKS.get(mapIndex);
               if (cachedResolvedBlockArray != null) {
                  return cachedResolvedBlockArray;
               }
            }

            ResolvedBlockArray resolvedBlockArray = new ResolvedBlockArray(new BlockFluidEntry[]{new BlockFluidEntry(index, key.rotation(), 0)});
            if (key.rotation() == 0) {
               ResolvedBlockArray.RESOLVED_BLOCKS.put(mapIndex, resolvedBlockArray);
            }

            return resolvedBlockArray;
         }
      } catch (IllegalArgumentException var8) {
         throw new IllegalArgumentException("BlockLayer does not exist in BlockTypes", var8);
      }
   }

   @Nonnull
   public static ResolvedBlockArray loadSingleBlock(@Nonnull JsonObject obj) {
      BlockTypeAssetMap<String, BlockType> assetMap = BlockType.getAssetMap();
      IndexedLookupTableAssetMap<String, Fluid> fluidMap = Fluid.getAssetMap();

      try {
         int blockIndex = 0;
         int rotation = 0;
         int fluidIndex = 0;
         if (obj.has("Block")) {
            BlockPattern.BlockEntry key = BlockPattern.BlockEntry.decode(obj.get("Block").getAsString());
            int index = assetMap.getIndex(key.blockTypeKey());
            if (index == Integer.MIN_VALUE) {
               throw new IllegalArgumentException("Unknown key! " + key);
            }

            blockIndex = index;
            rotation = key.rotation();
         }

         if (obj.has("Fluid")) {
            String key = obj.get("Fluid").getAsString();
            int index = fluidMap.getIndex(key);
            if (index == Integer.MIN_VALUE) {
               throw new IllegalArgumentException("Unknown key! " + key);
            }

            fluidIndex = index;
         }

         long mapIndex = MathUtil.packLong(blockIndex, fluidIndex);
         if (rotation == 0) {
            ResolvedBlockArray cachedResolvedBlockArray = (ResolvedBlockArray)ResolvedBlockArray.RESOLVED_BLOCKS.get(mapIndex);
            if (cachedResolvedBlockArray != null) {
               return cachedResolvedBlockArray;
            }
         }

         ResolvedBlockArray resolvedBlockArray = new ResolvedBlockArray(new BlockFluidEntry[]{new BlockFluidEntry(blockIndex, rotation, fluidIndex)});
         if (rotation == 0) {
            ResolvedBlockArray.RESOLVED_BLOCKS.put(mapIndex, resolvedBlockArray);
         }

         return resolvedBlockArray;
      } catch (IllegalArgumentException var9) {
         throw new IllegalArgumentException("BlockLayer does not exist in BlockTypes", var9);
      }
   }
}
