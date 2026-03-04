package com.hypixel.hytale.server.spawning.assets.spawns.config;

import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.common.map.IWeightedElement;
import com.hypixel.hytale.server.core.asset.type.blockset.config.BlockSet;
import com.hypixel.hytale.server.flock.config.FlockAsset;
import com.hypixel.hytale.server.npc.validators.NPCRoleValidator;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RoleSpawnParameters implements IWeightedElement {
   public static final BuilderCodec<RoleSpawnParameters> CODEC = BuilderCodec.builder(RoleSpawnParameters.class, RoleSpawnParameters::new)
      .documentation("A set of parameters that configure spawning for a single NPC type.")
      .<String>append(new KeyedCodec<>("Id", Codec.STRING), (parameters, s) -> parameters.id = s, parameters -> parameters.id)
      .documentation("The Role ID of the NPC to spawn.")
      .addValidator(Validators.nonNull())
      .addValidator(NPCRoleValidator.INSTANCE)
      .add()
      .<Double>append(new KeyedCodec<>("Weight", Codec.DOUBLE, true), (parameter, d) -> parameter.weight = d, parameters -> parameters.weight)
      .documentation("The relative weight of this NPC (chance of being spawned is this value relative to the sum of all weights).")
      .addValidator(Validators.nonNull())
      .addValidator(Validators.greaterThan(0.0))
      .add()
      .<String>append(new KeyedCodec<>("SpawnBlockSet", Codec.STRING), (parameter, s) -> parameter.spawnBlockSet = s, parameters -> parameters.spawnBlockSet)
      .addValidator(BlockSet.VALIDATOR_CACHE.getValidator())
      .documentation("An optional BlockSet reference that defines which blocks this NPC can spawn on.")
      .add()
      .<String>append(new KeyedCodec<>("SpawnFluidTag", Codec.STRING), (parameter, s) -> parameter.spawnFluidTag = s, parameters -> parameters.spawnFluidTag)
      .documentation("An optional tag reference that defines which fluids this NPC can spawn on.")
      .add()
      .<String>append(new KeyedCodec<>("Flock", FlockAsset.CHILD_ASSET_CODEC), (spawn, o) -> spawn.flockDefinitionId = o, spawn -> spawn.flockDefinitionId)
      .documentation("The optional flock definition to spawn around this NPC.")
      .addValidator(FlockAsset.VALIDATOR_CACHE.getValidator())
      .add()
      .afterDecode(parameters -> {
         if (parameters.spawnBlockSet != null) {
            int index = BlockSet.getAssetMap().getIndex(parameters.spawnBlockSet);
            if (index == Integer.MIN_VALUE) {
               throw new IllegalArgumentException("Unknown key! " + parameters.spawnBlockSet);
            }

            parameters.spawnBlockSetIndex = index;
         }

         if (parameters.spawnFluidTag != null) {
            parameters.spawnFluidTagIndex = AssetRegistry.getOrCreateTagIndex(parameters.spawnFluidTag);
         }
      })
      .build();
   public static final RoleSpawnParameters[] EMPTY_ARRAY = new RoleSpawnParameters[0];
   protected String id;
   protected double weight;
   protected String spawnBlockSet;
   protected int spawnBlockSetIndex = Integer.MIN_VALUE;
   protected String spawnFluidTag;
   protected int spawnFluidTagIndex = Integer.MIN_VALUE;
   protected String flockDefinitionId;
   protected int flockDefinitionIndex = Integer.MIN_VALUE;

   public RoleSpawnParameters(String id, double weight, String spawnBlockSet, String flockDefinitionId) {
      this.id = id;
      this.weight = weight;
      this.spawnBlockSet = spawnBlockSet;
      this.flockDefinitionId = flockDefinitionId;
   }

   protected RoleSpawnParameters() {
   }

   public String getId() {
      return this.id;
   }

   @Override
   public double getWeight() {
      return this.weight;
   }

   public String getSpawnBlockSet() {
      return this.spawnBlockSet;
   }

   public int getSpawnBlockSetIndex() {
      return this.spawnBlockSetIndex;
   }

   public int getSpawnFluidTagIndex() {
      return this.spawnFluidTagIndex;
   }

   public String getFlockDefinitionId() {
      return this.flockDefinitionId;
   }

   public int getFlockDefinitionIndex() {
      if (this.flockDefinitionIndex == Integer.MIN_VALUE && this.flockDefinitionId != null) {
         int index = FlockAsset.getAssetMap().getIndex(this.flockDefinitionId);
         if (index == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unknown key! " + this.flockDefinitionId);
         }

         this.flockDefinitionIndex = index;
      }

      return this.flockDefinitionIndex;
   }

   @Nullable
   public FlockAsset getFlockDefinition() {
      int index = this.getFlockDefinitionIndex();
      return index != Integer.MIN_VALUE ? FlockAsset.getAssetMap().getAsset(index) : null;
   }

   @Nonnull
   @Override
   public String toString() {
      return "RoleSpawnParameters{id='"
         + this.id
         + "', weight="
         + this.weight
         + ", spawnBlockSet="
         + this.spawnBlockSet
         + ", flockDefinitionId="
         + this.flockDefinitionId
         + "}";
   }
}
