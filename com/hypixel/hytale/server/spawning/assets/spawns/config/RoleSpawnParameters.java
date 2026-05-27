package com.hypixel.hytale.server.spawning.assets.spawns.config;

import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.EnumMapCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.common.map.IWeightedElement;
import com.hypixel.hytale.server.core.asset.type.blockset.config.BlockSet;
import com.hypixel.hytale.server.flock.config.FlockAsset;
import com.hypixel.hytale.server.npc.movement.MovementMode;
import com.hypixel.hytale.server.npc.validators.NPCRoleValidator;
import com.hypixel.hytale.server.spawning.ISpawnable;
import com.hypixel.hytale.server.spawning.SpawningContext;
import com.hypixel.hytale.server.spawning.SpawningPlugin;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.logging.Level;
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
      .<Map<MovementMode, Double>>append(
         new KeyedCodec<>(
            "MovementModes",
            new EnumMapCodec<>(MovementMode.class, Codec.DOUBLE)
               .documentKey(MovementMode.WALK, "NPC will spawn on dry solid ground if permitted")
               .documentKey(MovementMode.WADE, "NPC will spawn in shallow water if permitted")
               .documentKey(MovementMode.UNDERWATER_WALK, "NPC will spawn on the ground underwater if permitted")
               .documentKey(MovementMode.DIVE, "NPC will spawn fully submerged underwater if permitted")
               .documentKey(MovementMode.FLY, "NPC will spawn in the air flying")
         ),
         (spawn, m) -> spawn.configMovementModeWeights = m,
         spawn -> spawn.configMovementModeWeights
      )
      .documentation(
         "An optional map of movement modes with weights >0.0 that this NPC will use to spawn. If not set then the NPC will use backwards compatible defaults."
      )
      .add()
      .<Boolean>append(new KeyedCodec<>("EnableSafeSpawning", Codec.BOOLEAN), (spawn, b) -> spawn.enableSafeSpawning = b, spawn -> spawn.enableSafeSpawning)
      .documentation(
         "When true (default), movement modes that are unsafe for this NPC's configuration are excluded from spawning. Set to false to allow spawning in all supported movement modes."
      )
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
      .validator((asset, results) -> {
         if (asset.configMovementModeWeights != null) {
            if (asset.configMovementModeWeights.isEmpty()) {
               asset.configMovementModeWeights = Collections.emptyMap();
            } else {
               double[] weightsSum = new double[]{0.0};
               asset.configMovementModeWeights.forEach((key, value) -> {
                  if (value < 0.0) {
                     results.fail(String.format("Movement mode weights must be greater or equal 0! Invalid weight for %s", key));
                  }

                  weightsSum[0] += value;
               });
               if (weightsSum[0] == 0.0) {
                  results.fail("At least one movement mode weight must be greater than 0!");
               }

               asset.configMovementModeWeights = Collections.unmodifiableMap(asset.configMovementModeWeights);
            }
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
   @Nullable
   protected Map<MovementMode, Double> configMovementModeWeights;
   protected boolean enableSafeSpawning = true;
   @Nullable
   private double[] cachedMovementModeWeights;
   private boolean noValidMovementModes;
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

   public boolean getEnableSafeSpawning() {
      return this.enableSafeSpawning;
   }

   @Nullable
   public Map<MovementMode, Double> getConfigMovementModeWeights() {
      return Collections.unmodifiableMap(this.configMovementModeWeights);
   }

   @Nullable
   public double[] getOrComputeMovementModeWeights(@Nonnull ISpawnable spawnable, @Nonnull SpawningContext context) {
      if (this.noValidMovementModes) {
         return null;
      } else if (this.cachedMovementModeWeights != null) {
         return this.cachedMovementModeWeights;
      } else {
         EnumSet<MovementMode> supported = EnumSet.noneOf(MovementMode.class);
         EnumSet<MovementMode> defaults = EnumSet.noneOf(MovementMode.class);
         EnumSet<MovementMode> safe = EnumSet.noneOf(MovementMode.class);
         spawnable.getMovementModes(context, supported, defaults, safe);
         this.cachedMovementModeWeights = SpawningContext.buildMovementModeWeights(
            this.configMovementModeWeights, supported, defaults, this.enableSafeSpawning ? safe : null
         );
         if (this.cachedMovementModeWeights == null) {
            this.noValidMovementModes = true;
            SpawningPlugin.get()
               .getLogger()
               .at(Level.WARNING)
               .log(
                  "Role '%s' cannot be spawned: no valid movement mode exists (supported=%s, defaults=%s, safe=%s, enableSafeSpawning=%s)",
                  this.id,
                  supported,
                  defaults,
                  safe,
                  this.enableSafeSpawning
               );
         }

         return this.cachedMovementModeWeights;
      }
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
         + ", movementModeWeights="
         + (
            this.configMovementModeWeights != null
               ? this.configMovementModeWeights.entrySet().stream().<String>map(entry -> entry.getKey() + "=" + entry.getValue())
               : "Null"
         )
         + ", enableSafeSpawning="
         + this.enableSafeSpawning
         + ", flockDefinitionId="
         + this.flockDefinitionId
         + "}";
   }
}
