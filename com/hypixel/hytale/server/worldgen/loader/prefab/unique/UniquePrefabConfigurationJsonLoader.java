package com.hypixel.hytale.server.worldgen.loader.prefab.unique;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hypixel.hytale.math.vector.Vector2d;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.procedurallib.condition.ConstantBlockFluidCondition;
import com.hypixel.hytale.procedurallib.condition.ConstantIntCondition;
import com.hypixel.hytale.procedurallib.condition.HeightCondition;
import com.hypixel.hytale.procedurallib.condition.IBlockFluidCondition;
import com.hypixel.hytale.procedurallib.condition.ICoordinateCondition;
import com.hypixel.hytale.procedurallib.condition.ICoordinateRndCondition;
import com.hypixel.hytale.procedurallib.condition.IIntCondition;
import com.hypixel.hytale.procedurallib.json.HeightThresholdInterpreterJsonLoader;
import com.hypixel.hytale.procedurallib.json.JsonLoader;
import com.hypixel.hytale.procedurallib.json.NoiseMaskConditionJsonLoader;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.server.core.asset.type.environment.config.Environment;
import com.hypixel.hytale.server.core.prefab.PrefabRotation;
import com.hypixel.hytale.server.worldgen.SeedStringResource;
import com.hypixel.hytale.server.worldgen.loader.biome.BiomeMaskJsonLoader;
import com.hypixel.hytale.server.worldgen.loader.context.ZoneFileContext;
import com.hypixel.hytale.server.worldgen.loader.prefab.BlockPlacementMaskJsonLoader;
import com.hypixel.hytale.server.worldgen.loader.prefab.PrefabPatternGeneratorJsonLoader;
import com.hypixel.hytale.server.worldgen.loader.util.ResolvedBlockArrayJsonLoader;
import com.hypixel.hytale.server.worldgen.loader.util.Vector2dJsonLoader;
import com.hypixel.hytale.server.worldgen.loader.util.Vector3dJsonLoader;
import com.hypixel.hytale.server.worldgen.prefab.unique.UniquePrefabConfiguration;
import com.hypixel.hytale.server.worldgen.util.ResolvedBlockArray;
import com.hypixel.hytale.server.worldgen.util.condition.BlockMaskCondition;
import com.hypixel.hytale.server.worldgen.util.condition.HashSetBlockFluidCondition;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.nio.file.Path;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class UniquePrefabConfigurationJsonLoader extends JsonLoader<SeedStringResource, UniquePrefabConfiguration> {
   protected final ZoneFileContext zoneContext;

   public UniquePrefabConfigurationJsonLoader(SeedString<SeedStringResource> seed, Path dataFolder, JsonElement json, ZoneFileContext zoneContext) {
      super(seed, dataFolder, json);
      this.zoneContext = zoneContext;
   }

   @Nonnull
   public UniquePrefabConfiguration load() {
      return new UniquePrefabConfiguration(
         this.loadHeightThresholds(),
         this.loadMask(),
         this.loadRotations(),
         this.loadBiomeMask(),
         this.loadMapCondition(),
         this.loadParent(),
         this.loadAnchor(),
         this.loadSpawnOffset(),
         this.loadMaxDistance(),
         this.loadFitHeightmap(),
         this.loadSubmerge(),
         this.loadOnWater(),
         this.loadEnvironment(),
         this.loadMaxAttempts(),
         this.loadExclusionRadius(),
         this.loadIsSpawn(),
         this.loadZoneBorderExclusion(),
         this.loadShowOnMap()
      );
   }

   @Nonnull
   protected IBlockFluidCondition loadParent() {
      IBlockFluidCondition parentMask = ConstantBlockFluidCondition.DEFAULT_TRUE;
      if (this.has("Parent")) {
         ResolvedBlockArray blockArray = new ResolvedBlockArrayJsonLoader(this.seed, this.dataFolder, this.get("Parent")).load();
         LongSet biomeSet = blockArray.getEntrySet();
         parentMask = new HashSetBlockFluidCondition(biomeSet);
      }

      return parentMask;
   }

   @Nullable
   protected ICoordinateRndCondition loadHeightThresholds() {
      ICoordinateRndCondition heightCondition = null;
      if (this.has("HeightThreshold")) {
         JsonObject heightThresholdObject = this.get("HeightThreshold").getAsJsonObject();
         heightCondition = new HeightCondition(new HeightThresholdInterpreterJsonLoader<>(this.seed, this.dataFolder, heightThresholdObject, 320).load());
      }

      return heightCondition;
   }

   @Nullable
   protected IIntCondition loadBiomeMask() {
      IIntCondition biomeMask = ConstantIntCondition.DEFAULT_TRUE;
      if (this.has("BiomeMask")) {
         biomeMask = new BiomeMaskJsonLoader(this.seed, this.dataFolder, this.get("BiomeMask"), "UniquePrefab", this.zoneContext).load();
      }

      return biomeMask;
   }

   @Nullable
   protected PrefabRotation[] loadRotations() {
      PrefabRotation[] prefabRotations = null;
      if (this.has("Rotations")) {
         prefabRotations = PrefabPatternGeneratorJsonLoader.loadRotations(this.get("Rotations"));
      }

      return prefabRotations;
   }

   @Nonnull
   protected ICoordinateCondition loadMapCondition() {
      return new NoiseMaskConditionJsonLoader<>(this.seed, this.dataFolder, this.get("NoiseMask")).load();
   }

   @Nullable
   protected BlockMaskCondition loadMask() {
      BlockMaskCondition configuration = BlockMaskCondition.DEFAULT_TRUE;
      if (this.has("Mask")) {
         configuration = new BlockPlacementMaskJsonLoader(this.seed, this.dataFolder, this.getRaw("Mask")).load();
      }

      return configuration;
   }

   @Nonnull
   protected Vector2d loadAnchor() {
      if (!this.has("Anchor")) {
         throw new IllegalArgumentException("Could not find anchor for Unique prefab generator");
      } else {
         return new Vector2dJsonLoader(this.seed, this.dataFolder, this.get("Anchor")).load();
      }
   }

   @Nonnull
   protected Vector3d loadSpawnOffset() {
      Vector3d offset = new Vector3d(0.0, -5000.0, 0.0);
      if (this.has("SpawnOffset")) {
         offset = new Vector3dJsonLoader(this.seed, this.dataFolder, this.get("SpawnOffset")).load();
      }

      return offset;
   }

   protected int loadEnvironment() {
      int environment = Integer.MIN_VALUE;
      if (this.has("Environment")) {
         String environmentId = this.get("Environment").getAsString();
         environment = Environment.getAssetMap().getIndex(environmentId);
         if (environment == Integer.MIN_VALUE) {
            throw new Error(String.format("Error while looking up environment \"%s\"!", environmentId));
         }
      }

      return environment;
   }

   protected boolean loadFitHeightmap() {
      return this.has("FitHeightmap") && this.get("FitHeightmap").getAsBoolean();
   }

   protected boolean loadSubmerge() {
      return this.mustGetBool("Submerge", UniquePrefabConfigurationJsonLoader.Constants.DEFAULT_SUBMERGE);
   }

   protected boolean loadOnWater() {
      return this.has("OnWater") && this.get("OnWater").getAsBoolean();
   }

   protected double loadMaxDistance() {
      return this.has("MaxDistance") ? this.get("MaxDistance").getAsDouble() : 100.0;
   }

   protected int loadMaxAttempts() {
      return this.has("MaxAttempts") ? this.get("MaxAttempts").getAsInt() : 5000;
   }

   protected double loadExclusionRadius() {
      return this.has("ExclusionRadius") ? this.get("ExclusionRadius").getAsDouble() : 50.0;
   }

   protected boolean loadIsSpawn() {
      return this.has("IsSpawn") && this.get("IsSpawn").getAsBoolean();
   }

   protected double loadZoneBorderExclusion() {
      return this.has("BorderExclusion") ? this.get("BorderExclusion").getAsDouble() : 25.0;
   }

   protected boolean loadShowOnMap() {
      return this.has("ShowOnMap") && this.get("ShowOnMap").getAsBoolean();
   }

   public interface Constants {
      String KEY_PARENT = "Parent";
      String KEY_HEIGHT_THRESHOLD = "HeightThreshold";
      String KEY_BIOME_MASK = "BiomeMask";
      String KEY_NOISE_MASK = "NoiseMask";
      String KEY_MASK = "Mask";
      String KEY_ANCHOR = "Anchor";
      String KEY_FIT_HEIGHTMAP = "FitHeightmap";
      String KEY_SUBMERGE = "Submerge";
      String KEY_ENVIRONMENT = "Environment";
      String KEY_ON_WATER = "OnWater";
      String KEY_MAX_DISTANCE = "MaxDistance";
      String KEY_MAX_ATTEMPTS = "MaxAttempts";
      String KEY_EXCLUSION_RADIUS = "ExclusionRadius";
      String KEY_IS_SPAWN = "IsSpawn";
      String KEY_SPAWN_OFFSET = "SpawnOffset";
      String KEY_BORDER_EXCLUSION = "BorderExclusion";
      String KEY_SHOW_ON_MAP = "ShowOnMap";
      String SEED_STRING_BIOME_MASK_TYPE = "UniquePrefab";
      String ERROR_BIOME_ERROR_MASK = "Could not find tile / custom biome \"%s\" for biome mask. Typo or disabled biome?";
      String ERROR_NO_ANCHOR = "Could not find anchor for Unique prefab generator";
      String ERROR_LOADING_ENVIRONMENT = "Error while looking up environment \"%s\"!";
      double DEFAULT_MAX_DISTANCE = 100.0;
      int DEFAULT_MAX_ATTEMPTS = 5000;
      double DEFAULT_EXCLUSION_RADIUS = 50.0;
      double DEFAULT_ZONE_BORDER_EXCLUSION = 25.0;
      Boolean DEFAULT_SUBMERGE = Boolean.FALSE;
   }
}
