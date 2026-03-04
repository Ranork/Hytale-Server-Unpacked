package com.hypixel.hytale.server.worldgen.loader.biome;

import com.google.gson.JsonElement;
import com.hypixel.hytale.procedurallib.condition.IHeightThresholdInterpreter;
import com.hypixel.hytale.procedurallib.json.HeightThresholdInterpreterJsonLoader;
import com.hypixel.hytale.procedurallib.json.JsonLoader;
import com.hypixel.hytale.procedurallib.json.NoisePropertyJsonLoader;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.procedurallib.property.NoiseProperty;
import com.hypixel.hytale.server.worldgen.SeedStringResource;
import com.hypixel.hytale.server.worldgen.biome.Biome;
import com.hypixel.hytale.server.worldgen.biome.BiomeInterpolation;
import com.hypixel.hytale.server.worldgen.container.CoverContainer;
import com.hypixel.hytale.server.worldgen.container.EnvironmentContainer;
import com.hypixel.hytale.server.worldgen.container.FadeContainer;
import com.hypixel.hytale.server.worldgen.container.LayerContainer;
import com.hypixel.hytale.server.worldgen.container.PrefabContainer;
import com.hypixel.hytale.server.worldgen.container.TintContainer;
import com.hypixel.hytale.server.worldgen.container.WaterContainer;
import com.hypixel.hytale.server.worldgen.loader.container.CoverContainerJsonLoader;
import com.hypixel.hytale.server.worldgen.loader.container.EnvironmentContainerJsonLoader;
import com.hypixel.hytale.server.worldgen.loader.container.FadeContainerJsonLoader;
import com.hypixel.hytale.server.worldgen.loader.container.LayerContainerJsonLoader;
import com.hypixel.hytale.server.worldgen.loader.container.PrefabContainerJsonLoader;
import com.hypixel.hytale.server.worldgen.loader.container.TintContainerJsonLoader;
import com.hypixel.hytale.server.worldgen.loader.container.WaterContainerJsonLoader;
import com.hypixel.hytale.server.worldgen.loader.context.BiomeFileContext;
import com.hypixel.hytale.server.worldgen.loader.context.FileLoadingContext;
import java.nio.file.Path;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class BiomeJsonLoader extends JsonLoader<SeedStringResource, Biome> {
   private static final Pattern COLOR_PREFIX_PATTERN = Pattern.compile("0x|#");
   protected final BiomeFileContext biomeContext;
   protected final FileLoadingContext fileContext;

   public BiomeJsonLoader(SeedString<SeedStringResource> seed, Path dataFolder, JsonElement json, BiomeFileContext biomeContext) {
      super(seed, dataFolder, json);
      this.biomeContext = biomeContext;
      this.fileContext = biomeContext.getParentContext().getParentContext();
   }

   @Nonnull
   protected IHeightThresholdInterpreter loadTerrainHeightThreshold() {
      try {
         return new HeightThresholdInterpreterJsonLoader<>(this.seed, this.dataFolder, this.get("TerrainHeightThreshold"), 320).load();
      } catch (Throwable var2) {
         throw new Error("Failed to load height threshold container", var2);
      }
   }

   @Nonnull
   protected CoverContainer loadCoverContainer() {
      try {
         return new CoverContainerJsonLoader(this.seed, this.dataFolder, this.get("Covers")).load();
      } catch (Throwable var2) {
         throw new Error("Failed to load cover container", var2);
      }
   }

   @Nonnull
   protected FadeContainer loadFadeContainer() {
      try {
         return new FadeContainerJsonLoader(this.seed, this.dataFolder, this.get("Fade")).load();
      } catch (Throwable var2) {
         throw new Error("Failed to load fade container", var2);
      }
   }

   @Nonnull
   protected LayerContainer loadLayerContainers() {
      try {
         if (!this.has("Layers")) {
            throw new IllegalArgumentException("LayerContainer is not defined in Biome!");
         } else {
            return new LayerContainerJsonLoader(this.seed, this.dataFolder, this.get("Layers")).load();
         }
      } catch (Throwable var2) {
         throw new Error("Failed to load layer container", var2);
      }
   }

   @Nullable
   protected PrefabContainer loadPrefabContainer() {
      try {
         PrefabContainer prefabContainer = null;
         if (this.has("Prefabs")) {
            prefabContainer = new PrefabContainerJsonLoader(this.seed, this.dataFolder, this.get("Prefabs"), this.fileContext).load();
         }

         return prefabContainer;
      } catch (Throwable var2) {
         throw new Error("Failed to load prefab container", var2);
      }
   }

   @Nonnull
   protected TintContainer loadTintContainer() {
      try {
         return new TintContainerJsonLoader(this.seed, this.dataFolder, this.get("Tint")).load();
      } catch (Throwable var2) {
         throw new Error("Failed to load tint container", var2);
      }
   }

   @Nonnull
   protected EnvironmentContainer loadEnvironmentContainer() {
      try {
         return new EnvironmentContainerJsonLoader(this.seed, this.dataFolder, this.get("Environment")).load();
      } catch (Throwable var2) {
         throw new Error("Failed to load environment container", var2);
      }
   }

   @Nonnull
   protected WaterContainer loadWaterContainer() {
      try {
         return new WaterContainerJsonLoader(this.seed, this.dataFolder, this.get("Water")).load();
      } catch (Throwable var2) {
         throw new Error("Failed to load water container", var2);
      }
   }

   @Nullable
   protected NoiseProperty loadHeightmapNoise() {
      NoiseProperty heightmapNoise = null;
      if (this.has("HeightmapNoise")) {
         heightmapNoise = new NoisePropertyJsonLoader<>(this.seed, this.dataFolder, this.get("HeightmapNoise")).load();
      }

      return heightmapNoise;
   }

   protected int loadColor() {
      int rgb = 16711680;
      if (this.has("MapColor")) {
         rgb = getColor(this.get("MapColor").getAsString());
      }

      return rgb;
   }

   @Nullable
   protected BiomeInterpolation loadInterpolation() {
      BiomeInterpolation interpolation = BiomeInterpolation.DEFAULT;
      if (this.has("Interpolation")) {
         interpolation = new BiomeInterpolationJsonLoader(this.seed, this.dataFolder, this.get("Interpolation"), this.biomeContext.getParentContext()).load();
      }

      return interpolation;
   }

   protected static int getColor(@Nonnull String string) {
      String tintString = COLOR_PREFIX_PATTERN.matcher(string).replaceFirst("");
      return Integer.parseInt(tintString, 16);
   }

   public interface Constants {
      String KEY_TERRAIN_HEIGHT_THRESHOLD = "TerrainHeightThreshold";
      String KEY_COVERS = "Covers";
      String KEY_LAYERS = "Layers";
      String KEY_PREFABS = "Prefabs";
      String KEY_FADE = "Fade";
      String KEY_TINT = "Tint";
      String KEY_ENVIRONMENT = "Environment";
      String KEY_WATER = "Water";
      String KEY_HEIGHTMAP_NOISE = "HeightmapNoise";
      String KEY_INTERPOLATION = "Interpolation";
      String KEY_MAP_COLOR = "MapColor";
      String ERROR_NO_LAYER_CONTAINER = "LayerContainer is not defined in Biome!";
      String ERROR_COVER_CONTAINER = "Failed to load cover container";
      String ERROR_HEIGHT_CONTAINER = "Failed to load height threshold container";
      String ERROR_LAYER_CONTAINER = "Failed to load layer container";
      String ERROR_WATER_CONTAINER = "Failed to load water container";
      String ERROR_TINT_CONTAINER = "Failed to load tint container";
      String ERROR_FADE_CONTAINER = "Failed to load fade container";
      String ERROR_ENVIRONMENT_CONTAINER = "Failed to load environment container";
      String ERROR_PREFAB_CONTAINER = "Failed to load prefab container";
   }
}
