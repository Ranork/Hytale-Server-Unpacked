package com.hypixel.hytale.server.worldgen.loader.zone;

import com.google.gson.JsonElement;
import com.hypixel.hytale.common.map.IWeightedMap;
import com.hypixel.hytale.common.map.WeightedMap;
import com.hypixel.hytale.procedurallib.file.FileIO;
import com.hypixel.hytale.procedurallib.json.JsonLoader;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.server.worldgen.SeedStringResource;
import com.hypixel.hytale.server.worldgen.biome.TileBiome;
import com.hypixel.hytale.server.worldgen.loader.biome.TileBiomeJsonLoader;
import com.hypixel.hytale.server.worldgen.loader.context.BiomeFileContext;
import com.hypixel.hytale.server.worldgen.loader.context.ZoneFileContext;
import java.nio.file.Path;
import java.util.Map.Entry;
import javax.annotation.Nonnull;

public class ZoneBiomesJsonLoader extends JsonLoader<SeedStringResource, IWeightedMap<TileBiome>> {
   protected final ZoneFileContext zoneContext;

   public ZoneBiomesJsonLoader(SeedString<SeedStringResource> seed, Path dataFolder, JsonElement json, ZoneFileContext zone) {
      super(seed, dataFolder, json);
      this.zoneContext = zone;
   }

   public IWeightedMap<TileBiome> load() {
      WeightedMap.Builder<TileBiome> builder = WeightedMap.builder(TileBiome.EMPTY_ARRAY);

      for (Entry<String, BiomeFileContext> biomeEntry : this.zoneContext.getTileBiomes()) {
         TileBiome biome = this.loadBiome(biomeEntry.getValue());
         builder.put(biome, biome.getWeight());
      }

      if (builder.size() <= 0) {
         throw new IllegalArgumentException("Could not find any tile biomes for this zone!");
      } else {
         return builder.build();
      }
   }

   @Nonnull
   protected TileBiome loadBiome(@Nonnull BiomeFileContext biomeContext) {
      try {
         JsonElement biomeJson = (JsonElement)FileIO.load(biomeContext.getPath(), JsonLoader.JSON_OBJ_LOADER);
         return new TileBiomeJsonLoader(this.seed, this.dataFolder, biomeJson, biomeContext).load();
      } catch (Throwable var3) {
         throw new Error(String.format("Error while loading tile biome \"%s\" from \"%s\"", biomeContext.getName(), biomeContext.getPath().toString()), var3);
      }
   }

   public interface Constants {
      String ERROR_BIOME_FILES_NULL = "Biome files error occured.";
      String ERROR_BIOME_FAILED = "Error while loading tile biome \"%s\" from \"%s\"";
      String ERROR_NO_TILE_BIOMES = "Could not find any tile biomes for this zone!";
      String FILE_TILE_PREFIX = "Tile.";
      String FILE_TILE_SUFFIX = ".json";
   }
}
