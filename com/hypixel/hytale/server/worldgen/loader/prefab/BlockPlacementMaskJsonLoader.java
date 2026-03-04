package com.hypixel.hytale.server.worldgen.loader.prefab;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hypixel.hytale.assetstore.map.BlockTypeAssetMap;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.procedurallib.json.JsonLoader;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.worldgen.SeedStringResource;
import com.hypixel.hytale.server.worldgen.loader.util.ResolvedBlockArrayJsonLoader;
import com.hypixel.hytale.server.worldgen.loader.util.ResolvedVariantsBlockArrayLoader;
import com.hypixel.hytale.server.worldgen.util.BlockFluidEntry;
import com.hypixel.hytale.server.worldgen.util.ResolvedBlockArray;
import com.hypixel.hytale.server.worldgen.util.condition.BlockMaskCondition;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.nio.file.Path;
import javax.annotation.Nonnull;

public class BlockPlacementMaskJsonLoader extends JsonLoader<SeedStringResource, BlockMaskCondition> {
   private String fileName;

   public BlockPlacementMaskJsonLoader(@Nonnull SeedString<SeedStringResource> seed, Path dataFolder, JsonElement json) {
      super(seed.append(".BlockMaskCondition"), dataFolder, json);
   }

   public BlockMaskCondition load() {
      BlockPlacementMaskRegistry registry = this.seed.get().getBlockMaskRegistry();
      if (this.fileName != null) {
         BlockMaskCondition mask = registry.getIfPresentFileMask(this.fileName);
         if (mask != null) {
            return mask;
         }
      }

      Long2ObjectMap<BlockMaskCondition.Mask> specificMasks = Long2ObjectMaps.emptyMap();
      BlockMaskCondition.Mask defaultMask;
      if (this.json != null && !this.json.isJsonNull()) {
         if (this.has("Default")) {
            defaultMask = new BlockMaskCondition.Mask(this.loadEntries(this.get("Default").getAsJsonArray()));
         } else {
            defaultMask = BlockMaskCondition.DEFAULT_MASK;
         }

         if (this.has("Specific")) {
            BlockTypeAssetMap<String, BlockType> assetMap = BlockType.getAssetMap();
            specificMasks = new Long2ObjectOpenHashMap();
            JsonArray array = this.get("Specific").getAsJsonArray();

            for (int i = 0; i < array.size(); i++) {
               try {
                  JsonObject specificObject = array.get(i).getAsJsonObject();
                  JsonElement blocksElement = specificObject.get("Block");
                  ResolvedBlockArray blocks = new ResolvedBlockArrayJsonLoader(this.seed, this.dataFolder, blocksElement).load();

                  for (BlockFluidEntry blockEntry : blocks.getEntries()) {
                     String key = assetMap.getAsset(blockEntry.blockId()).getId();
                     ObjectIterator var15 = assetMap.getSubKeys(key).iterator();

                     while (var15.hasNext()) {
                        String variant = (String)var15.next();
                        int index = assetMap.getIndex(variant);
                        if (index == Integer.MIN_VALUE) {
                           throw new IllegalArgumentException("Unknown key! " + variant);
                        }

                        JsonArray rule = specificObject.getAsJsonArray("Rule");
                        specificMasks.put(MathUtil.packLong(index, blockEntry.fluidId()), new BlockMaskCondition.Mask(this.loadEntries(rule)));
                     }
                  }
               } catch (Throwable var19) {
                  throw new Error(String.format("Error while reading specific block mask #%s!", i), var19);
               }
            }
         }
      } else {
         defaultMask = BlockMaskCondition.DEFAULT_MASK;
      }

      BlockMaskCondition mask = registry.retainOrAllocateMask(defaultMask, specificMasks);
      if (this.fileName != null) {
         registry.putFileMask(this.fileName, mask);
      }

      return mask;
   }

   @Nonnull
   protected BlockMaskCondition.MaskEntry[] loadEntries(@Nonnull JsonArray jsonArray) {
      BlockMaskCondition.MaskEntry[] entries = new BlockMaskCondition.MaskEntry[jsonArray.size()];
      int head = 0;
      int tail = entries.length;

      for (JsonElement element : jsonArray) {
         if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();
            boolean replace = true;
            if (obj.has("Replace")) {
               replace = obj.get("Replace").getAsBoolean();
            }

            ResolvedBlockArray blocks = ResolvedVariantsBlockArrayLoader.loadSingleBlock(obj);
            entries[head++] = this.seed.get().getBlockMaskRegistry().retainOrAllocateEntry(blocks, replace);
         } else {
            String string = element.getAsString();
            boolean replace = true;
            int beginIndex = 0;
            if (string.charAt(0) == '!') {
               replace = false;
               beginIndex = 1;
            }

            if (string.length() == beginIndex + 1 && string.charAt(beginIndex) == '*') {
               if (tail < entries.length) {
                  System.arraycopy(entries, tail, entries, tail - 1, entries.length - tail);
               }

               entries[entries.length - 1] = replace ? BlockMaskCondition.MaskEntry.WILDCARD_TRUE : BlockMaskCondition.MaskEntry.WILDCARD_FALSE;
               tail--;
            } else {
               string = string.substring(beginIndex);
               ResolvedBlockArray blocks = ResolvedVariantsBlockArrayLoader.loadSingleBlock(string);
               entries[head++] = this.seed.get().getBlockMaskRegistry().retainOrAllocateEntry(blocks, replace);
            }
         }
      }

      return entries;
   }

   @Override
   protected JsonElement loadFileConstructor(String filePath) {
      this.fileName = filePath;
      return this.seed.get().getBlockMaskRegistry().cachedFile(filePath, file -> super.loadFileConstructor(file));
   }

   public interface Constants {
      String KEY_DEFAULT = "Default";
      String KEY_SPECIFIC = "Specific";
      String KEY_BLOCK = "Block";
      String KEY_RULE = "Rule";
      String ERROR_FAIL_SPECIFIC = "Error while reading specific block mask #%s!";
      String ERROR_BLOCK_INVALID = "Failed to resolve block \"%s\"";
   }
}
