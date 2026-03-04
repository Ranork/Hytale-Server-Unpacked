package com.hypixel.hytale.server.spawning.assets.spawnmarker.config;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.schema.metadata.ui.UIPropertyTitle;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.common.map.IWeightedElement;
import com.hypixel.hytale.common.map.IWeightedMap;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.codec.WeightedMapCodec;
import com.hypixel.hytale.server.flock.config.FlockAsset;
import com.hypixel.hytale.server.npc.validators.NPCRoleValidator;
import java.time.Duration;
import javax.annotation.Nullable;

public class SpawnMarker implements JsonAssetWithMap<String, DefaultAssetMap<String, SpawnMarker>> {
   public static final AssetBuilderCodec<String, SpawnMarker> CODEC = AssetBuilderCodec.builder(
         SpawnMarker.class, SpawnMarker::new, Codec.STRING, (t, k) -> t.id = k, t -> t.id, (asset, data) -> asset.data = data, asset -> asset.data
      )
      .documentation(
         "A marker designed to spawn NPCs with its current position and rotation. When the NPC dies, a new NPC will spawn after a defined cooldown. This cooldown can be specified either in terms of game time or real time and begins to count down from the moment the mob dies."
      )
      .<String>appendInherited(
         new KeyedCodec<>("Model", Codec.STRING),
         (spawnMarker, s) -> spawnMarker.model = s,
         spawnMarker -> spawnMarker.model,
         (spawnMarker, parent) -> spawnMarker.model = parent.model
      )
      .documentation(
         "The optional visual representation to use in the world when in creative mode (this has a default value which can be specified using **DefaultSpawnMarkerModel** in the server config for the spawning plugin)."
      )
      .addValidator(ModelAsset.VALIDATOR_CACHE.getValidator())
      .add()
      .<IWeightedMap<SpawnMarker.SpawnConfiguration>>appendInherited(
         new KeyedCodec<>("NPCs", new WeightedMapCodec<>(SpawnMarker.SpawnConfiguration.CODEC, SpawnMarker.SpawnConfiguration.EMPTY_ARRAY)),
         (spawnMarker, s) -> spawnMarker.weightedConfigurations = s,
         spawnMarker -> spawnMarker.weightedConfigurations,
         (spawnMarker, parent) -> spawnMarker.weightedConfigurations = parent.weightedConfigurations
      )
      .documentation("A weighted list of NPCs and their configurations.")
      .metadata(new UIPropertyTitle("NPCs"))
      .addValidator(Validators.nonNull())
      .add()
      .<Double>appendInherited(
         new KeyedCodec<>("ExclusionRadius", Codec.DOUBLE),
         (spawnMarker, s) -> spawnMarker.exclusionRadius = s,
         spawnMarker -> spawnMarker.exclusionRadius,
         (spawnMarker, parent) -> spawnMarker.exclusionRadius = parent.exclusionRadius
      )
      .documentation("A radius used to prevent a marker from spawning new NPCs if a player in adventure mode is within range.")
      .addValidator(Validators.greaterThanOrEqual(0.0))
      .add()
      .<Double>appendInherited(
         new KeyedCodec<>("MaxDropHeight", Codec.DOUBLE),
         (spawnMarker, s) -> spawnMarker.maxDropHeightSquared = s * s,
         spawnMarker -> spawnMarker.maxDropHeightSquared,
         (spawnMarker, parent) -> spawnMarker.maxDropHeightSquared = parent.maxDropHeightSquared
      )
      .documentation(
         "A maximum offset from the marker's position at which the mob can be spawned. Ground mobs are spawned directly on the ground, so if the marker is high in the air due to the building having been destroyed, if the distance between the marker and the ground is greater than this, the marker will not spawn mobs."
      )
      .addValidator(Validators.greaterThan(0.0))
      .add()
      .<Boolean>appendInherited(
         new KeyedCodec<>("RealtimeRespawn", Codec.BOOLEAN),
         (spawnMarker, s) -> spawnMarker.realtimeRespawn = s,
         spawnMarker -> spawnMarker.realtimeRespawn,
         (spawnMarker, parent) -> spawnMarker.realtimeRespawn = parent.realtimeRespawn
      )
      .documentation("Whether to use real time or game time for the respawn timer.")
      .add()
      .<Boolean>appendInherited(
         new KeyedCodec<>("ManualTrigger", Codec.BOOLEAN),
         (spawnMarker, s) -> spawnMarker.manualTrigger = s,
         spawnMarker -> spawnMarker.manualTrigger,
         (spawnMarker, parent) -> spawnMarker.manualTrigger = parent.manualTrigger
      )
      .documentation("Indicates if the spawn marker has to be triggered manually or not.")
      .add()
      .<Double>appendInherited(
         new KeyedCodec<>("DeactivationDistance", Codec.DOUBLE),
         (spawnMarker, d) -> spawnMarker.deactivationDistance = d,
         spawnMarker -> spawnMarker.deactivationDistance,
         (spawnMarker, parent) -> spawnMarker.deactivationDistance = parent.deactivationDistance
      )
      .documentation("If no players are inside this range, the spawn marker will deactivate and store its NPCs once this distance is exceeded.")
      .addValidator(Validators.greaterThan(0.0))
      .add()
      .<Double>appendInherited(
         new KeyedCodec<>("DeactivationTime", Codec.DOUBLE),
         (spawnMarker, d) -> spawnMarker.deactivationTime = d,
         spawnMarker -> spawnMarker.deactivationTime,
         (spawnMarker, parent) -> spawnMarker.deactivationTime = parent.deactivationTime
      )
      .documentation("The delay before deactivation happens when no players are in range.")
      .addValidator(Validators.greaterThan(0.0))
      .add()
      .validator(
         (asset, results) -> {
            boolean isRealtime = asset.isRealtimeRespawn();
            IWeightedMap<SpawnMarker.SpawnConfiguration> configs = asset.getWeightedConfigurations();
            if (configs != null && configs.size() != 0) {
               configs.forEach(
                  config -> {
                     if (isRealtime && config.getRealtimeRespawnTime() <= 0.0) {
                        results.fail(
                           String.format(
                              "Value for RealtimeRespawn in %s:%s must be greater than zero if using realtime spawning", asset.getId(), config.getNpc()
                           )
                        );
                     } else if (!isRealtime && config.getSpawnAfterGameTime() == null) {
                        results.fail(
                           String.format("Value for SpawnAfterGameTime in %s:%s must be provided if using game time spawning", asset.getId(), config.getNpc())
                        );
                     } else {
                        if (config.getSpawnAfterGameTime() != null && config.getRealtimeRespawnTime() > 0.0) {
                           results.warn(
                              String.format(
                                 "%s:%s defines both RealtimeRespawn and SpawnAfterGameTime despite being set to %s spawning",
                                 asset.getId(),
                                 config.getNpc(),
                                 isRealtime ? "realtime" : "game time"
                              )
                           );
                        }
                     }
                  }
               );
            } else {
               results.fail(String.format("Spawn marker %s must define at least one NPC configuration to spawn", asset.getId()));
            }
         }
      )
      .build();
   public static final ValidatorCache<String> VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(SpawnMarker::getAssetStore));
   private static AssetStore<String, SpawnMarker, DefaultAssetMap<String, SpawnMarker>> ASSET_STORE;
   private AssetExtraInfo.Data data;
   protected String id;
   protected String model;
   @Nullable
   protected IWeightedMap<SpawnMarker.SpawnConfiguration> weightedConfigurations;
   protected double exclusionRadius;
   protected double maxDropHeightSquared = 4.0;
   protected boolean realtimeRespawn;
   protected boolean manualTrigger;
   protected double deactivationDistance = 40.0;
   protected double deactivationTime = 5.0;

   public static AssetStore<String, SpawnMarker, DefaultAssetMap<String, SpawnMarker>> getAssetStore() {
      if (ASSET_STORE == null) {
         ASSET_STORE = AssetRegistry.getAssetStore(SpawnMarker.class);
      }

      return ASSET_STORE;
   }

   public static DefaultAssetMap<String, SpawnMarker> getAssetMap() {
      return (DefaultAssetMap<String, SpawnMarker>)getAssetStore().getAssetMap();
   }

   protected SpawnMarker() {
   }

   @Nullable
   public IWeightedMap<SpawnMarker.SpawnConfiguration> getWeightedConfigurations() {
      return this.weightedConfigurations;
   }

   public String getId() {
      return this.id;
   }

   public String getModel() {
      return this.model;
   }

   public double getExclusionRadius() {
      return this.exclusionRadius;
   }

   public double getMaxDropHeightSquared() {
      return this.maxDropHeightSquared;
   }

   public boolean isRealtimeRespawn() {
      return this.realtimeRespawn;
   }

   public boolean isManualTrigger() {
      return this.manualTrigger;
   }

   public double getDeactivationDistance() {
      return this.deactivationDistance;
   }

   public double getDeactivationTime() {
      return this.deactivationTime;
   }

   public static class SpawnConfiguration implements IWeightedElement {
      public static final BuilderCodec<SpawnMarker.SpawnConfiguration> CODEC = BuilderCodec.builder(
            SpawnMarker.SpawnConfiguration.class, SpawnMarker.SpawnConfiguration::new
         )
         .documentation(
            "A configuration for an individual weighted NPC to spawn. **Note:** At least one of **RealtimeRespawnTime** and **SpawnAfterGameTime** must be set, matching the **RealtimeRespawn** flag on the marker."
         )
         .<String>append(new KeyedCodec<>("Name", Codec.STRING), (spawn, s) -> spawn.npc = s, spawn -> spawn.npc)
         .documentation("The role name of the NPC to spawn (omitting this results in a no-op spawn, i.e. a weighted chance of spawning nothing).")
         .addValidator(Validators.nonEmptyString())
         .addValidator(NPCRoleValidator.INSTANCE)
         .add()
         .<Double>append(new KeyedCodec<>("Weight", Codec.DOUBLE, true), (spawn, s) -> spawn.weight = s, spawn -> spawn.weight)
         .documentation("The spawn chance, relative to the total sum of all weights in this pool.")
         .addValidator(Validators.greaterThan(0.0))
         .add()
         .<Double>append(new KeyedCodec<>("RealtimeRespawnTime", Codec.DOUBLE), (spawn, s) -> spawn.realtimeRespawnTime = s, spawn -> spawn.realtimeRespawnTime)
         .documentation("A value in seconds that specifies how long after the death of this mob a new mob will be spawned.")
         .add()
         .<Duration>append(
            new KeyedCodec<>("SpawnAfterGameTime", Codec.DURATION), (spawn, s) -> spawn.spawnAfterGameTime = s, spawn -> spawn.spawnAfterGameTime
         )
         .documentation(
            "A Duration string e.g. of form P2DT3H4M (2 days, 3 hours, and 4 minutes) that specifies how long after the death of this mob a new mob will be spawned based on in-game time."
         )
         .add()
         .<String>append(new KeyedCodec<>("Flock", FlockAsset.CHILD_ASSET_CODEC), (spawn, o) -> spawn.flockDefinitionId = o, spawn -> spawn.flockDefinitionId)
         .documentation("The optional flock definition to spawn around this NPC.")
         .addValidator(FlockAsset.VALIDATOR_CACHE.getValidator())
         .add()
         .build();
      public static final SpawnMarker.SpawnConfiguration[] EMPTY_ARRAY = new SpawnMarker.SpawnConfiguration[0];
      protected String npc;
      protected double weight;
      protected double realtimeRespawnTime;
      protected Duration spawnAfterGameTime;
      protected String flockDefinitionId;
      protected int flockDefinitionIndex = Integer.MIN_VALUE;

      public SpawnConfiguration(String npc, double weight, double realtimeRespawnTime, Duration spawnAfterGameTime, String flockDefinitionId) {
         this.npc = npc;
         this.weight = weight;
         this.realtimeRespawnTime = realtimeRespawnTime;
         this.spawnAfterGameTime = spawnAfterGameTime;
         this.flockDefinitionId = flockDefinitionId;
      }

      protected SpawnConfiguration() {
      }

      public String getNpc() {
         return this.npc;
      }

      public double getRealtimeRespawnTime() {
         return this.realtimeRespawnTime;
      }

      public Duration getSpawnAfterGameTime() {
         return this.spawnAfterGameTime;
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

      @Override
      public double getWeight() {
         return this.weight;
      }
   }
}
