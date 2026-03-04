package com.hypixel.hytale.server.spawning.assets.spawns.config;

import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.codec.validation.validator.DoubleArrayValidator;
import com.hypixel.hytale.server.core.asset.type.environment.config.Environment;
import java.util.Arrays;
import javax.annotation.Nonnull;

public class WorldNPCSpawn extends NPCSpawn implements JsonAssetWithMap<String, IndexedLookupTableAssetMap<String, WorldNPCSpawn>> {
   public static final AssetBuilderCodec<String, WorldNPCSpawn> CODEC = AssetBuilderCodec.builder(
         WorldNPCSpawn.class,
         WorldNPCSpawn::new,
         NPCSpawn.BASE_CODEC,
         Codec.STRING,
         (t, k) -> t.id = k,
         t -> t.id,
         (asset, data) -> asset.data = data,
         asset -> asset.data
      )
      .documentation("A spawning configuration used to spawn NPCs around the world.")
      .<String[]>appendInherited(
         new KeyedCodec<>("Environments", Codec.STRING_ARRAY),
         (spawn, o) -> spawn.environments = o,
         spawn -> spawn.environments,
         (spawn, parent) -> spawn.environments = parent.environments
      )
      .documentation(
         "A required list of environments that this configuration covers. Each combination of environment and NPC in this configuration should be unique."
      )
      .addValidator(Validators.nonEmptyArray())
      .addValidator(Validators.uniqueInArray())
      .addValidator(Environment.VALIDATOR_CACHE.getArrayValidator())
      .add()
      .<double[]>appendInherited(
         new KeyedCodec<>("MoonPhaseWeightModifiers", Codec.DOUBLE_ARRAY),
         (spawn, o) -> spawn.moonPhaseWeightModifiers = o,
         spawn -> spawn.moonPhaseWeightModifiers,
         (spawn, parent) -> spawn.moonPhaseWeightModifiers = parent.moonPhaseWeightModifiers
      )
      .documentation(
         "An array of modifiers which will be multiplied into the weights for each NPC in this config dependent on the current moon phase.\n\nAt present, Hytale has **5** moon phases, with day 0 being a full moon which gradually wanes up to day 4 which is a new moon. These are represented in the array indices.\n\nEach modifier can only be between 0 and 2, where 0 means the NPC will not spawn at all, 1 will use the standard weight, and 2 will result in twice the spawns\n\ne.g. `[ 0, 0, 0.5, 1, 2 ]` would result in NPCs that do not spawn during day 0 and day 1 (full moon and three quarters), would spawn half as much on day 2, the defined amount on day 3, and twice as many on day 4 (new moon)."
      )
      .addValidator(new DoubleArrayValidator(Validators.range(0.0, 2.0)))
      .add()
      .build();
   private static IndexedLookupTableAssetMap<String, WorldNPCSpawn> ASSET_MAP;
   protected double[] moonPhaseWeightModifiers;

   public static IndexedLookupTableAssetMap<String, WorldNPCSpawn> getAssetMap() {
      if (ASSET_MAP == null) {
         ASSET_MAP = (IndexedLookupTableAssetMap<String, WorldNPCSpawn>)AssetRegistry.getAssetStore(WorldNPCSpawn.class).getAssetMap();
      }

      return ASSET_MAP;
   }

   public WorldNPCSpawn(String id) {
      super(id);
   }

   protected WorldNPCSpawn() {
   }

   @Override
   public String getId() {
      return this.id;
   }

   public double[] getMoonPhaseWeightModifiers() {
      return this.moonPhaseWeightModifiers;
   }

   @Nonnull
   @Override
   public String toString() {
      return "WorldNPCSpawn{moonPhaseWeightModifiers=" + Arrays.toString(this.moonPhaseWeightModifiers) + "} " + super.toString();
   }
}
