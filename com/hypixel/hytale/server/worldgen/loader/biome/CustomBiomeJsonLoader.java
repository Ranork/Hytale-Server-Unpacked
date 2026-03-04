package com.hypixel.hytale.server.worldgen.loader.biome;

import com.google.gson.JsonElement;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.server.worldgen.SeedStringResource;
import com.hypixel.hytale.server.worldgen.biome.Biome;
import com.hypixel.hytale.server.worldgen.biome.CustomBiome;
import com.hypixel.hytale.server.worldgen.biome.CustomBiomeGenerator;
import com.hypixel.hytale.server.worldgen.loader.context.BiomeFileContext;
import java.nio.file.Path;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CustomBiomeJsonLoader extends BiomeJsonLoader {
   protected final Biome[] tileBiomes;

   public CustomBiomeJsonLoader(
      @Nonnull SeedString<SeedStringResource> seed, Path dataFolder, JsonElement json, @Nonnull BiomeFileContext biomeContext, Biome[] tileBiomes
   ) {
      super(seed.append(String.format("TileBiome-%s", biomeContext.getName())), dataFolder, json, biomeContext);
      this.tileBiomes = tileBiomes;
   }

   @Nonnull
   public CustomBiome load() {
      return new CustomBiome(
         this.biomeContext.getId(),
         this.biomeContext.getName(),
         this.loadInterpolation(),
         this.loadCustomBiomeGenerator(),
         this.loadTerrainHeightThreshold(),
         this.loadCoverContainer(),
         this.loadLayerContainers(),
         this.loadPrefabContainer(),
         this.loadTintContainer(),
         this.loadEnvironmentContainer(),
         this.loadWaterContainer(),
         this.loadFadeContainer(),
         this.loadHeightmapNoise(),
         this.loadColor()
      );
   }

   @Nullable
   protected CustomBiomeGenerator loadCustomBiomeGenerator() {
      CustomBiomeGenerator customBiomeGenerator = null;
      if (this.has("CustomBiomeGenerator")) {
         customBiomeGenerator = new CustomBiomeGeneratorJsonLoader(
               this.seed, this.dataFolder, this.get("CustomBiomeGenerator"), this.biomeContext, this.tileBiomes
            )
            .load();
      }

      return customBiomeGenerator;
   }

   public interface Constants {
      String KEY_CUSTOM_BIOME_GENERATOR = "CustomBiomeGenerator";
      String SEED_PREFIX = "TileBiome-%s";
   }
}
