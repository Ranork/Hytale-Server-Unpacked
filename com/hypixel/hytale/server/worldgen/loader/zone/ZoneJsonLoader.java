package com.hypixel.hytale.server.worldgen.loader.zone;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hypixel.hytale.common.map.IWeightedMap;
import com.hypixel.hytale.procedurallib.json.JsonLoader;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.server.worldgen.SeedStringResource;
import com.hypixel.hytale.server.worldgen.biome.Biome;
import com.hypixel.hytale.server.worldgen.biome.BiomePatternGenerator;
import com.hypixel.hytale.server.worldgen.biome.CustomBiome;
import com.hypixel.hytale.server.worldgen.biome.TileBiome;
import com.hypixel.hytale.server.worldgen.cave.CaveGenerator;
import com.hypixel.hytale.server.worldgen.container.UniquePrefabContainer;
import com.hypixel.hytale.server.worldgen.loader.biome.BiomePatternGeneratorJsonLoader;
import com.hypixel.hytale.server.worldgen.loader.cave.CaveGeneratorJsonLoader;
import com.hypixel.hytale.server.worldgen.loader.container.UniquePrefabContainerJsonLoader;
import com.hypixel.hytale.server.worldgen.loader.context.ZoneFileContext;
import com.hypixel.hytale.server.worldgen.zone.Zone;
import com.hypixel.hytale.server.worldgen.zone.ZoneDiscoveryConfig;
import java.nio.file.Path;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ZoneJsonLoader extends JsonLoader<SeedStringResource, Zone> {
   @Nonnull
   protected final ZoneFileContext zoneContext;

   public ZoneJsonLoader(
      @Nonnull SeedString<SeedStringResource> seed, @Nonnull Path dataFolder, @Nonnull JsonElement json, @Nonnull ZoneFileContext zoneContext
   ) {
      super(seed.append(String.format(".Zone-%s", zoneContext.getName())), dataFolder, json);
      this.zoneContext = zoneContext;
   }

   @Nonnull
   public Zone load() {
      return new Zone(
         this.zoneContext.getId(),
         this.zoneContext.getName(),
         this.loadDiscoveryConfig(),
         this.loadCaveGenerator(),
         this.loadBiomePatternGenerator(),
         this.loadUniquePrefabContainer()
      );
   }

   @Nonnull
   protected ZoneDiscoveryConfig loadDiscoveryConfig() {
      JsonElement discoveryElement = this.get("Discovery");
      if (discoveryElement != null && discoveryElement.isJsonObject()) {
         JsonObject discoveryObject = discoveryElement.getAsJsonObject();
         Boolean display = null;
         JsonElement displayElement = discoveryObject.get("Display");
         if (displayElement != null && !displayElement.isJsonNull()) {
            display = displayElement.getAsBoolean();
         }

         String zoneName = null;
         JsonElement zoneNameElement = discoveryObject.get("ZoneName");
         if (zoneNameElement != null && !zoneNameElement.isJsonNull()) {
            zoneName = zoneNameElement.getAsString();
         }

         String soundEventId = null;
         JsonElement soundElement = discoveryObject.get("SoundEventId");
         if (soundElement != null && !soundElement.isJsonNull()) {
            soundEventId = soundElement.getAsString();
         }

         String icon = null;
         JsonElement iconElement = discoveryObject.get("Icon");
         if (iconElement != null && !iconElement.isJsonNull()) {
            icon = iconElement.getAsString();
         }

         Boolean major = null;
         JsonElement majorElement = discoveryObject.get("Major");
         if (majorElement != null && !majorElement.isJsonNull()) {
            major = majorElement.getAsBoolean();
         }

         Float duration = null;
         JsonElement durationElement = discoveryObject.get("Duration");
         if (durationElement != null && !durationElement.isJsonNull()) {
            duration = durationElement.getAsFloat();
         }

         Float fadeInDuration = null;
         JsonElement fadeInElement = discoveryObject.get("FadeInDuration");
         if (fadeInElement != null && !fadeInElement.isJsonNull()) {
            fadeInDuration = fadeInElement.getAsFloat();
         }

         Float fadeOutDuration = null;
         JsonElement fadeOutElement = discoveryObject.get("FadeOutDuration");
         if (fadeOutElement != null && !fadeOutElement.isJsonNull()) {
            fadeOutDuration = fadeOutElement.getAsFloat();
         }

         return ZoneDiscoveryConfig.of(display, zoneName, soundEventId, icon, major, duration, fadeInDuration, fadeOutDuration);
      } else {
         return ZoneDiscoveryConfig.DEFAULT;
      }
   }

   @Nonnull
   protected BiomePatternGenerator loadBiomePatternGenerator() {
      IWeightedMap<TileBiome> tileBiomes = this.loadBiomes();
      TileBiome[] biomes = tileBiomes.toArray();
      CustomBiome[] customBiomes = this.loadCustomBiomes(biomes);

      try {
         return new BiomePatternGeneratorJsonLoader(this.seed, this.dataFolder, this.get("BiomeGenerator"), tileBiomes, customBiomes).load();
      } catch (Throwable var5) {
         throw new Error("Error while loading biome generator.", var5);
      }
   }

   @Nullable
   protected IWeightedMap<TileBiome> loadBiomes() {
      try {
         return new ZoneBiomesJsonLoader(this.seed, this.dataFolder, this.get("BiomeGenerator"), this.zoneContext).load();
      } catch (Throwable var2) {
         throw new Error("Error while loading tile biomes.", var2);
      }
   }

   @Nonnull
   protected CustomBiome[] loadCustomBiomes(@Nonnull Biome[] tileBiomes) {
      try {
         return new ZoneCustomBiomesJsonLoader(this.seed, this.dataFolder, this.get("BiomeGenerator"), this.zoneContext, tileBiomes).load();
      } catch (Throwable var3) {
         throw new Error("Error while loading custom biomes.", var3);
      }
   }

   @Nullable
   protected CaveGenerator loadCaveGenerator() {
      try {
         return new CaveGeneratorJsonLoader(this.seed, this.dataFolder, this.json, this.zoneContext.getPath().resolve("Cave"), this.zoneContext).load();
      } catch (Throwable var2) {
         throw new Error("Error while loading cave generator.", var2);
      }
   }

   @Nonnull
   protected UniquePrefabContainer loadUniquePrefabContainer() {
      try {
         return new UniquePrefabContainerJsonLoader(this.seed, this.dataFolder, this.get("UniquePrefabs"), this.zoneContext).load();
      } catch (Throwable var2) {
         throw new Error("Error while loading unique prefabs.", var2);
      }
   }

   public interface Constants {
      String KEY_BIOME_GENERATOR = "BiomeGenerator";
      String KEY_UNIQUE_PREFABS = "UniquePrefabs";
      String KEY_DISCOVERY = "Discovery";
      String KEY_DISCOVERY_DISPLAY = "Display";
      String KEY_DISCOVERY_ZONE = "ZoneName";
      String KEY_DISCOVERY_SOUND_EVENT_ID = "SoundEventId";
      String KEY_DISCOVERY_ICON = "Icon";
      String KEY_DISCOVERY_MAJOR = "Major";
      String KEY_DISCOVERY_DURATION = "Duration";
      String KEY_DISCOVERY_FADE_IN_DURATION = "FadeInDuration";
      String KEY_DISCOVERY_FADE_OUT_DURATION = "FadeOutDuration";
      String PATH_CAVE = "Cave";
      String SEED_ZONE_SUFFIX = ".Zone-%s";
      String ERROR_BIOME_GENERATOR = "Error while loading biome generator.";
      String ERROR_TILE_BIOMES = "Error while loading tile biomes.";
      String ERROR_CUSTOM_BIOMES = "Error while loading custom biomes.";
      String ERROR_CAVE_GENERATOR = "Error while loading cave generator.";
      String ERROR_UNIQUE_PREFABS = "Error while loading unique prefabs.";
   }
}
