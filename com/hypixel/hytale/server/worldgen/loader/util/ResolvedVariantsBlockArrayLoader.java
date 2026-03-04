package com.hypixel.hytale.server.worldgen.loader.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hypixel.hytale.assetstore.map.BlockTypeAssetMap;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.procedurallib.json.JsonLoader;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.fluid.Fluid;
import com.hypixel.hytale.server.worldgen.SeedStringResource;
import com.hypixel.hytale.server.worldgen.util.BlockFluidEntry;
import com.hypixel.hytale.server.worldgen.util.ResolvedBlockArray;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

public class ResolvedVariantsBlockArrayLoader extends JsonLoader<SeedStringResource, ResolvedBlockArray> {
   public ResolvedVariantsBlockArrayLoader(SeedString<SeedStringResource> seed, Path dataFolder, JsonElement json) {
      super(seed, dataFolder, json);
   }

   @Nonnull
   public ResolvedBlockArray load() {
      if (this.json != null && !this.json.isJsonNull()) {
         if (!this.json.isJsonArray()) {
            return loadSingleBlock(this.json.getAsString());
         } else {
            JsonArray jsonArray = this.json.getAsJsonArray();
            if (jsonArray.size() == 1) {
               return loadSingleBlock(jsonArray.get(0).getAsString());
            } else {
               BlockTypeAssetMap<String, BlockType> assetMap = BlockType.getAssetMap();
               List<BlockFluidEntry[]> resolvedBlocksList = new ArrayList<>();
               int size = 0;

               for (int k = 0; k < jsonArray.size(); k++) {
                  String blockName = jsonArray.get(k).getAsString();

                  try {
                     if (assetMap.getAsset(blockName) == null) {
                        throw new IllegalArgumentException(String.valueOf(blockName));
                     }

                     int index = assetMap.getIndex(blockName);
                     if (index == Integer.MIN_VALUE) {
                        throw new IllegalArgumentException("Unknown key! " + blockName);
                     }

                     ResolvedBlockArray cachedResolvedBlockArray = (ResolvedBlockArray)ResolvedBlockArray.RESOLVED_BLOCKS_WITH_VARIANTS.get(index);
                     BlockFluidEntry[] blockVariantArray;
                     if (cachedResolvedBlockArray != null) {
                        blockVariantArray = cachedResolvedBlockArray.getEntries();
                     } else {
                        blockVariantArray = resolveBlockArrayWithVariants(blockName, assetMap, 0);
                     }

                     resolvedBlocksList.add(blockVariantArray);
                     size += blockVariantArray.length;
                  } catch (IllegalArgumentException var12) {
                     throw new IllegalArgumentException("BlockLayer does not exist in BlockTypes", var12);
                  }
               }

               BlockFluidEntry[] blocks = new BlockFluidEntry[size];

               for (BlockFluidEntry[] blockArray : resolvedBlocksList) {
                  for (BlockFluidEntry block : blockArray) {
                     blocks[--size] = block;
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
   public static ResolvedBlockArray loadSingleBlock(@Nonnull String blockName) {
      BlockTypeAssetMap<String, BlockType> assetMap = BlockType.getAssetMap();

      try {
         if (assetMap.getAsset(blockName) == null) {
            throw new IllegalArgumentException(String.valueOf(blockName));
         } else {
            int blockId = assetMap.getIndex(blockName);
            if (blockId == Integer.MIN_VALUE) {
               throw new IllegalArgumentException("Unknown block! " + blockName);
            } else {
               long mapIndex = MathUtil.packLong(blockId, 0);
               ResolvedBlockArray cachedResolvedBlockArray = (ResolvedBlockArray)ResolvedBlockArray.RESOLVED_BLOCKS_WITH_VARIANTS.get(mapIndex);
               if (cachedResolvedBlockArray != null) {
                  return cachedResolvedBlockArray;
               } else {
                  BlockFluidEntry[] blocks = resolveBlockArrayWithVariants(blockName, assetMap, 0);
                  ResolvedBlockArray resolvedBlockArray = new ResolvedBlockArray(blocks);
                  ResolvedBlockArray.RESOLVED_BLOCKS_WITH_VARIANTS.put(mapIndex, resolvedBlockArray);
                  return resolvedBlockArray;
               }
            }
         }
      } catch (IllegalArgumentException var8) {
         throw new IllegalArgumentException("BlockLayer does not exist in BlockTypes", var8);
      }
   }

   @Nonnull
   public static ResolvedBlockArray loadSingleBlock(@Nonnull JsonObject object) {
      BlockTypeAssetMap<String, BlockType> assetMap = BlockType.getAssetMap();

      try {
         if (object.has("Block")) {
            String blockName = object.get("Block").getAsString();
            if (assetMap.getAsset(blockName) == null) {
               throw new IllegalArgumentException(String.valueOf(blockName));
            } else {
               int blockId = assetMap.getIndex(blockName);
               if (blockId == Integer.MIN_VALUE) {
                  throw new IllegalArgumentException("Unknown block! " + blockName);
               } else {
                  int fluidId = 0;
                  if (object.has("Fluid")) {
                     String fluidName = object.get("Fluid").getAsString();
                     fluidId = Fluid.getAssetMap().getIndex(fluidName);
                     if (fluidId == Integer.MIN_VALUE) {
                        throw new IllegalArgumentException("Unknown fluid! " + fluidName);
                     }
                  }

                  long mapIndex = MathUtil.packLong(blockId, fluidId);
                  ResolvedBlockArray cachedResolvedBlockArray = (ResolvedBlockArray)ResolvedBlockArray.RESOLVED_BLOCKS_WITH_VARIANTS.get(mapIndex);
                  if (cachedResolvedBlockArray != null) {
                     return cachedResolvedBlockArray;
                  } else {
                     BlockFluidEntry[] blocks = resolveBlockArrayWithVariants(blockName, assetMap, fluidId);
                     ResolvedBlockArray resolvedBlockArray = new ResolvedBlockArray(blocks);
                     ResolvedBlockArray.RESOLVED_BLOCKS_WITH_VARIANTS.put(mapIndex, resolvedBlockArray);
                     return resolvedBlockArray;
                  }
               }
            }
         } else if (object.has("Fluid")) {
            return ResolvedBlockArrayJsonLoader.loadSingleBlock(object);
         } else {
            throw new IllegalArgumentException("Required either Block or Fluid key");
         }
      } catch (IllegalArgumentException var10) {
         throw new IllegalArgumentException("BlockLayer does not exist in BlockTypes", var10);
      }
   }

   @Nonnull
   public static BlockFluidEntry[] resolveBlockArrayWithVariants(String baseKey, @Nonnull BlockTypeAssetMap<String, BlockType> assetMap, int fluidId) {
      List<String> variants = new ArrayList<>(assetMap.getSubKeys(baseKey));
      BlockFluidEntry[] blocks = new BlockFluidEntry[variants.size()];

      for (int i = 0; i < variants.size(); i++) {
         String key = variants.get(i);
         int index = assetMap.getIndex(key);
         if (index == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unknown key! " + key);
         }

         blocks[i] = new BlockFluidEntry(index, 0, fluidId);
      }

      return blocks;
   }
}
