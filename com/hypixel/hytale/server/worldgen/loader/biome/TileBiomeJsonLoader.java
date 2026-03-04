package com.hypixel.hytale.server.worldgen.loader.biome;

import com.google.gson.JsonElement;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.server.worldgen.SeedStringResource;
import com.hypixel.hytale.server.worldgen.biome.TileBiome;
import com.hypixel.hytale.server.worldgen.loader.context.BiomeFileContext;
import java.nio.file.Path;
import javax.annotation.Nonnull;

public class TileBiomeJsonLoader extends BiomeJsonLoader {
   public TileBiomeJsonLoader(@Nonnull SeedString<SeedStringResource> seed, Path dataFolder, JsonElement json, @Nonnull BiomeFileContext biomeContext) {
      super(seed.append(String.format(".TileBiome-%s", biomeContext.getName())), dataFolder, json, biomeContext);
   }

   @Nonnull
   public TileBiome load() {
      return new TileBiome(
         this.biomeContext.getId(),
         this.biomeContext.getName(),
         this.loadInterpolation(),
         this.loadTerrainHeightThreshold(),
         this.loadCoverContainer(),
         this.loadLayerContainers(),
         this.loadPrefabContainer(),
         this.loadTintContainer(),
         this.loadEnvironmentContainer(),
         this.loadWaterContainer(),
         this.loadFadeContainer(),
         this.loadHeightmapNoise(),
         this.loadWeight(),
         this.loadSizeModifier(),
         this.loadColor()
      );
   }

   protected double loadWeight() {
      double weight = 1.0;
      if (this.has("Weight")) {
         weight = this.get("Weight").getAsDouble();
      }

      return weight;
   }

   protected double loadSizeModifier() {
      double sizeModifier = 1.0;
      if (this.has("SizeModifier")) {
         sizeModifier = this.get("SizeModifier").getAsDouble();
      }

      return sizeModifier * sizeModifier;
   }

   public interface Constants {
      String KEY_WEIGHT = "Weight";
      String KEY_SIZE_MODIFIER = "SizeModifier";
      String SEED_PREFIX = ".TileBiome-%s";
   }
}
