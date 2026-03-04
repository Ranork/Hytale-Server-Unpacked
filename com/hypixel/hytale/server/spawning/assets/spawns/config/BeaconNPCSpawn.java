package com.hypixel.hytale.server.spawning.assets.spawns.config;

import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.server.core.asset.type.environment.config.Environment;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.asset.type.responsecurve.ScaledXYResponseCurve;
import com.hypixel.hytale.server.spawning.assets.spawnsuppression.SpawnSuppression;
import com.hypixel.hytale.server.spawning.util.FloodFillPositionSelector;
import java.time.Duration;
import java.util.Arrays;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

public class BeaconNPCSpawn extends NPCSpawn implements JsonAssetWithMap<String, IndexedLookupTableAssetMap<String, BeaconNPCSpawn>> {
   public static final AssetBuilderCodec<String, BeaconNPCSpawn> CODEC = AssetBuilderCodec.builder(
         BeaconNPCSpawn.class,
         BeaconNPCSpawn::new,
         NPCSpawn.BASE_CODEC,
         Codec.STRING,
         (t, k) -> t.id = k,
         t -> t.id,
         (asset, data) -> asset.data = data,
         asset -> asset.data
      )
      .documentation(
         "A spawning configuration used to spawn NPCs around the player when the player is within a specific radius from the beacon. When **Environments** are defined for the beacon, beacons of that type will be dynamically created while the player is in one of the specified environments."
      )
      .<String>appendInherited(
         new KeyedCodec<>("Model", Codec.STRING), (spawn, s) -> spawn.model = s, spawn -> spawn.model, (spawn, parent) -> spawn.model = parent.model
      )
      .documentation("An optional model to represent the beacon in the world.")
      .addValidator(ModelAsset.VALIDATOR_CACHE.getValidator())
      .add()
      .<String[]>appendInherited(
         new KeyedCodec<>("Environments", Codec.STRING_ARRAY),
         (spawn, o) -> spawn.environments = o,
         spawn -> spawn.environments,
         (spawn, parent) -> spawn.environments = parent.environments
      )
      .documentation(
         "A required list of environments that this configuration covers. Each combination of environment and NPC in this configuration should be unique.\n\nFor Beacon NPC Spawn configurations, this can be left empty. In this case, these define the environments this beacon can be dynamically spawned in. If left empty, the beacon will not be dynamically spawned (e.g. if it should only be spawned by an objective)."
      )
      .addValidator(Validators.nonNull())
      .addValidator(Validators.uniqueInArray())
      .addValidator(Environment.VALIDATOR_CACHE.getArrayValidator())
      .add()
      .<Double>appendInherited(
         new KeyedCodec<>("TargetDistanceFromPlayer", Codec.DOUBLE),
         (spawn, d) -> spawn.targetDistanceFromPlayer = d,
         spawn -> spawn.targetDistanceFromPlayer,
         (spawn, parent) -> spawn.targetDistanceFromPlayer = parent.targetDistanceFromPlayer
      )
      .documentation("Roughly how far the NPC should be spawned away from the player (this is a guideline and not an absolute).")
      .addValidator(Validators.greaterThan(0.0))
      .add()
      .<Double>appendInherited(
         new KeyedCodec<>("MinDistanceFromPlayer", Codec.DOUBLE),
         (spawn, d) -> spawn.minDistanceFromPlayer = d,
         spawn -> spawn.minDistanceFromPlayer,
         (spawn, parent) -> spawn.minDistanceFromPlayer = parent.minDistanceFromPlayer
      )
      .documentation(
         "A hard cutoff for how close an NPC can be spawned to the player to prevent the guideline distance above resulting in an NPC spawning too close to them."
      )
      .addValidator(Validators.greaterThan(0.0))
      .add()
      .<int[]>appendInherited(
         new KeyedCodec<>("YRange", Codec.INT_ARRAY), (spawn, o) -> spawn.yRange = o, spawn -> spawn.yRange, (spawn, parent) -> spawn.yRange = parent.yRange
      )
      .documentation(
         "The acceptable y range within which NPCs can be spawned from the beacon. This is defined as offsets from the beacon. With [ -5, 5 ], NPCs can be spawned from five blocks below the beacon up to five blocks above."
      )
      .addValidator(Validators.intArraySize(2))
      .add()
      .<Integer>appendInherited(
         new KeyedCodec<>("MaxSpawnedNPCs", Codec.INTEGER),
         (spawn, i) -> spawn.maxSpawnedNpcs = i,
         spawn -> spawn.maxSpawnedNpcs,
         (spawn, parent) -> spawn.maxSpawnedNpcs = parent.maxSpawnedNpcs
      )
      .documentation("The maximum number of NPCs this beacon can have spawned at once.")
      .addValidator(Validators.greaterThan(0))
      .add()
      .<int[]>appendInherited(
         new KeyedCodec<>("ConcurrentSpawnsRange", Codec.INT_ARRAY),
         (spawn, i) -> spawn.concurrentSpawnsRange = i,
         spawn -> spawn.concurrentSpawnsRange,
         (spawn, parent) -> spawn.concurrentSpawnsRange = parent.concurrentSpawnsRange
      )
      .documentation(
         "The range from which a random number will be chosen that will represent the number of NPCs to be spawned in the next round between cooldowns."
      )
      .addValidator(Validators.intArraySize(2))
      .add()
      .<Duration[]>appendInherited(
         new KeyedCodec<>("SpawnAfterGameTimeRange", new ArrayCodec<>(Codec.DURATION, Duration[]::new)),
         (spawn, s) -> spawn.spawnAfterGameTime = s,
         spawn -> spawn.spawnAfterGameTime,
         (spawn, parent) -> spawn.spawnAfterGameTime = parent.spawnAfterGameTime
      )
      .documentation(
         "The random range from which to pick the next game-time based cooldown between spawns. This should be a duration string, e.g. [ \"PT5M\", \"PT10M\" ] which will spawn between 5 and 10 in-game minutes after the last spawn."
      )
      .addValidator(Validators.arraySize(2))
      .add()
      .<Duration[]>appendInherited(
         new KeyedCodec<>("SpawnAfterRealTimeRange", new ArrayCodec<>(Codec.DURATION, Duration[]::new)),
         (spawn, s) -> spawn.spawnAfterRealTime = s,
         spawn -> spawn.spawnAfterRealTime,
         (spawn, parent) -> spawn.spawnAfterRealTime = parent.spawnAfterRealTime
      )
      .documentation(
         "The random range from which to pick the next real-time based cooldown between spawns. This should be a duration string, e.g. [ \"PT30S\", \"PT80S\" ] which will spawn between 30 and 80 seconds IRL after the last spawn."
      )
      .addValidator(Validators.arraySize(2))
      .add()
      .<double[]>appendInherited(
         new KeyedCodec<>("InitialSpawnDelayRange", Codec.DOUBLE_ARRAY),
         (spawn, s) -> spawn.initialSpawnDelay = s,
         spawn -> spawn.initialSpawnDelay,
         (spawn, parent) -> spawn.initialSpawnDelay = parent.initialSpawnDelay
      )
      .documentation(
         "An optional range from which to pick an initial delay in real time seconds before which the first round of NPCs will be spawned after a beacon is created."
      )
      .addValidator(Validators.doubleArraySize(2))
      .add()
      .<Double>appendInherited(
         new KeyedCodec<>("NPCIdleDespawnTime", Codec.DOUBLE),
         (spawn, d) -> spawn.npcIdleDespawnTimeSeconds = d,
         spawn -> spawn.npcIdleDespawnTimeSeconds,
         (spawn, parent) -> spawn.npcIdleDespawnTimeSeconds = parent.npcIdleDespawnTimeSeconds
      )
      .documentation(
         "The number of seconds an NPC spawned by this beacon needs to spend idle before it will be despawned due to having no target. If **NPCSpawnState** is omitted, this will be ignored."
      )
      .addValidator(Validators.greaterThan(0.0))
      .add()
      .<Duration>appendInherited(
         new KeyedCodec<>("BeaconVacantDespawnGameTime", Codec.DURATION),
         (spawn, d) -> spawn.beaconVacantDespawnTime = d,
         spawn -> spawn.beaconVacantDespawnTime,
         (spawn, parent) -> spawn.beaconVacantDespawnTime = parent.beaconVacantDespawnTime
      )
      .documentation(
         "The amount of game time that needs to pass with no players present within the **SpawnRadius** before this beacon will remove itself. This should be a duration string."
      )
      .add()
      .<Double>appendInherited(
         new KeyedCodec<>("BeaconRadius", Codec.DOUBLE),
         (spawn, d) -> spawn.beaconRadius = d,
         spawn -> spawn.beaconRadius,
         (spawn, parent) -> spawn.beaconRadius = parent.beaconRadius
      )
      .documentation(
         "The radius within which a spawned NPC is considered to be under the influence of the beacon and NPCs will be spawned for a player. If an NPC spawned by the beacon moves outside this radius and is not in a busy state, it will begin to tick down the **NPCIdleDespawnTime** (if being considered). It is recommended that this be ~25% larger than the **SpawnRadius**."
      )
      .addValidator(Validators.greaterThan(0.0))
      .add()
      .<Double>appendInherited(
         new KeyedCodec<>("SpawnRadius", Codec.DOUBLE),
         (spawn, d) -> spawn.spawnRadius = d,
         spawn -> spawn.spawnRadius,
         (spawn, parent) -> spawn.spawnRadius = parent.spawnRadius
      )
      .documentation("The radius within which NPCs spawns can physically happen (i.e. where their spawn points will be).")
      .addValidator(Validators.greaterThan(0.0))
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("NPCSpawnState", Codec.STRING),
         (spawn, s) -> spawn.npcSpawnState = s,
         spawn -> spawn.npcSpawnState,
         (spawn, parent) -> spawn.npcSpawnState = parent.npcSpawnState
      )
      .documentation(
         "An optional state to force the NPC into upon spawn. If this state exists on the NPC, it will immediately enter the state upon spawn. For example, setting this to **Chase** will result in most NPCs immediately going for the player they were spawned around. If omitted, this beacon will allow idle NPCs."
      )
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("NPCSpawnSubState", Codec.STRING),
         (spawn, s) -> spawn.npcSpawnSubState = s,
         spawn -> spawn.npcSpawnSubState,
         (spawn, parent) -> spawn.npcSpawnSubState = parent.npcSpawnSubState
      )
      .documentation("As with **NPCSpawnStat**, but acts as an additional qualifier to define the desired substate.")
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("TargetSlot", Codec.STRING),
         (spawn, s) -> spawn.targetSlot = s,
         spawn -> spawn.targetSlot,
         (spawn, parent) -> spawn.targetSlot = parent.targetSlot
      )
      .documentation("The locked target slot to set the player to in the NPC.")
      .addValidator(Validators.nonNull())
      .addValidator(Validators.nonEmptyString())
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("SpawnSuppression", Codec.STRING),
         (spawn, s) -> spawn.spawnSuppression = s,
         spawn -> spawn.spawnSuppression,
         (spawn, parent) -> spawn.spawnSuppression = parent.spawnSuppression
      )
      .documentation("An optional spawn suppression that will be tied to this beacon.")
      .addValidator(SpawnSuppression.VALIDATOR_CACHE.getValidator())
      .add()
      .<Boolean>appendInherited(
         new KeyedCodec<>("OverrideSpawnSuppressors", Codec.BOOLEAN),
         (spawn, b) -> spawn.overrideSpawnSuppressors = b,
         spawn -> spawn.overrideSpawnSuppressors,
         (spawn, parent) -> spawn.overrideSpawnSuppressors = parent.overrideSpawnSuppressors
      )
      .documentation("Whether this beacon should ignore any spawn suppressions.")
      .add()
      .<ScaledXYResponseCurve>appendInherited(
         new KeyedCodec<>("MaxSpawnsScalingCurve", ScaledXYResponseCurve.CODEC),
         (spawn, s) -> spawn.maxSpawnsScalingCurve = s,
         spawn -> spawn.maxSpawnsScalingCurve,
         (spawn, parent) -> spawn.maxSpawnsScalingCurve = parent.maxSpawnsScalingCurve
      )
      .documentation(
         "A scaled response curve that represents the number of additional mobs to be added to the total **MaxSpawnedNPCs** based on the number of players within the beacon's max **DistanceRange**."
      )
      .add()
      .<ScaledXYResponseCurve>appendInherited(
         new KeyedCodec<>("ConcurrentSpawnsScalingCurve", ScaledXYResponseCurve.CODEC),
         (spawn, s) -> spawn.concurrentSpawnsScalingCurve = s,
         spawn -> spawn.concurrentSpawnsScalingCurve,
         (spawn, parent) -> spawn.concurrentSpawnsScalingCurve = parent.concurrentSpawnsScalingCurve
      )
      .documentation(
         "A scaled response curve that represents the number of additional mobs to be added to the total **MaxConcurrentSpawns** based on the number of players within the beacon's max **DistanceRange**."
      )
      .add()
      .<FloodFillPositionSelector.Debug>appendInherited(
         new KeyedCodec<>(
            "Debug",
            new EnumCodec<>(FloodFillPositionSelector.Debug.class)
               .documentKey(FloodFillPositionSelector.Debug.ALL, "Print all maps.")
               .documentKey(FloodFillPositionSelector.Debug.IRREGULARITIES, "Print only irregular maps.")
               .documentKey(FloodFillPositionSelector.Debug.DISABLED, "Disable map printing.")
         ),
         (spawn, b) -> spawn.debug = b,
         spawn -> spawn.debug,
         (spawn, parent) -> spawn.debug = parent.debug
      )
      .documentation("The debug mode. Can be used to print 2d maps of evaluated spawn regions.")
      .add()
      .build();
   public static final int[] DEFAULT_Y_RANGE = new int[]{-5, 5};
   public static final int[] DEFAULT_CONCURRENT_SPAWNS_RANGE = new int[]{1, 1};
   private static final Duration[] DEFAULT_RESPAWN_TIME_RANGE = new Duration[]{Duration.ofSeconds(5L), Duration.ofSeconds(10L)};
   protected String model;
   protected double targetDistanceFromPlayer = 15.0;
   protected double minDistanceFromPlayer = 5.0;
   protected int[] yRange = DEFAULT_Y_RANGE;
   protected int maxSpawnedNpcs = 1;
   protected int[] concurrentSpawnsRange = DEFAULT_CONCURRENT_SPAWNS_RANGE;
   protected Duration[] spawnAfterGameTime;
   protected Duration[] spawnAfterRealTime;
   protected double[] initialSpawnDelay;
   protected double npcIdleDespawnTimeSeconds = 10.0;
   protected double beaconRadius = 20.0;
   protected double spawnRadius = 15.0;
   protected Duration beaconVacantDespawnTime;
   protected String npcSpawnState;
   protected String npcSpawnSubState;
   protected String targetSlot = "LockedTarget";
   protected String spawnSuppression;
   protected ScaledXYResponseCurve maxSpawnsScalingCurve;
   protected ScaledXYResponseCurve concurrentSpawnsScalingCurve;
   protected boolean overrideSpawnSuppressors;
   protected FloodFillPositionSelector.Debug debug = FloodFillPositionSelector.Debug.DISABLED;
   public static final ValidatorCache<String> VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(BeaconNPCSpawn::getAssetStore));
   private static AssetStore<String, BeaconNPCSpawn, IndexedLookupTableAssetMap<String, BeaconNPCSpawn>> ASSET_STORE;

   public BeaconNPCSpawn(String id) {
      super(id);
   }

   protected BeaconNPCSpawn() {
   }

   public static AssetStore<String, BeaconNPCSpawn, IndexedLookupTableAssetMap<String, BeaconNPCSpawn>> getAssetStore() {
      if (ASSET_STORE == null) {
         ASSET_STORE = AssetRegistry.getAssetStore(BeaconNPCSpawn.class);
      }

      return ASSET_STORE;
   }

   public static IndexedLookupTableAssetMap<String, BeaconNPCSpawn> getAssetMap() {
      return (IndexedLookupTableAssetMap<String, BeaconNPCSpawn>)getAssetStore().getAssetMap();
   }

   @Override
   public String getId() {
      return this.id;
   }

   public String getModel() {
      return this.model;
   }

   public double getTargetDistanceFromPlayer() {
      return this.targetDistanceFromPlayer;
   }

   public double getMinDistanceFromPlayer() {
      return this.minDistanceFromPlayer;
   }

   public int[] getYRange() {
      return this.yRange;
   }

   public int getMaxSpawnedNpcs() {
      return this.maxSpawnedNpcs;
   }

   public int[] getConcurrentSpawnsRange() {
      return this.concurrentSpawnsRange;
   }

   public Duration[] getSpawnAfterGameTimeRange() {
      return this.spawnAfterGameTime == null ? DEFAULT_RESPAWN_TIME_RANGE : this.spawnAfterGameTime;
   }

   public Duration[] getSpawnAfterRealTimeRange() {
      return this.spawnAfterRealTime;
   }

   public boolean isRespawnRealtime() {
      return this.spawnAfterRealTime != null && this.spawnAfterGameTime == null;
   }

   public double[] getInitialSpawnDelay() {
      return this.initialSpawnDelay;
   }

   public double getNpcIdleDespawnTimeSeconds() {
      return this.npcIdleDespawnTimeSeconds;
   }

   public Duration getBeaconVacantDespawnTime() {
      return this.beaconVacantDespawnTime;
   }

   public double getBeaconRadius() {
      return this.beaconRadius;
   }

   public double getSpawnRadius() {
      return this.spawnRadius;
   }

   public String getNpcSpawnState() {
      return this.npcSpawnState;
   }

   public String getNpcSpawnSubState() {
      return this.npcSpawnSubState;
   }

   public String getSpawnSuppression() {
      return this.spawnSuppression;
   }

   public boolean isOverrideSpawnSuppressors() {
      return this.overrideSpawnSuppressors;
   }

   public String getTargetSlot() {
      return this.targetSlot;
   }

   public ScaledXYResponseCurve getMaxSpawnsScalingCurve() {
      return this.maxSpawnsScalingCurve;
   }

   public ScaledXYResponseCurve getConcurrentSpawnsScalingCurve() {
      return this.concurrentSpawnsScalingCurve;
   }

   public FloodFillPositionSelector.Debug getDebug() {
      return this.debug;
   }

   @Nonnull
   @Override
   public String toString() {
      return "BeaconNPCSpawn{id='"
         + this.id
         + "', model="
         + this.model
         + ", npcs="
         + Arrays.deepToString(this.npcs)
         + ", despawnParameters="
         + (this.despawnParameters != null ? this.despawnParameters.toString() : "Null")
         + ", environments="
         + Arrays.toString((Object[])this.environments)
         + ", dayTimeRange="
         + Arrays.toString(this.dayTimeRange)
         + ", moonPhaseRange="
         + Arrays.toString(this.moonPhaseRange)
         + ", lightTypeMap="
         + (
            this.lightTypeMap != null
               ? this.lightTypeMap
                  .entrySet()
                  .stream()
                  .map(entry -> entry.getKey() + "=" + Arrays.toString(entry.getValue()))
                  .collect(Collectors.joining(", ", "{", "}"))
               : "Null"
         )
         + ", targetDistanceFromPlayer="
         + this.targetDistanceFromPlayer
         + ", minDistanceFromPlayer="
         + this.minDistanceFromPlayer
         + ", yRange="
         + Arrays.toString(this.yRange)
         + ", maxSpawnedNpcs="
         + this.maxSpawnedNpcs
         + ", concurrentSpawnsRange="
         + Arrays.toString(this.concurrentSpawnsRange)
         + ", spawnAfterGameTimeRange="
         + Arrays.toString((Object[])this.spawnAfterGameTime)
         + ", spawnAfterRealTime="
         + Arrays.toString((Object[])this.spawnAfterRealTime)
         + ", initialSpawnDelay="
         + Arrays.toString(this.initialSpawnDelay)
         + ", npcIdleDespawnTimeSeconds="
         + this.npcIdleDespawnTimeSeconds
         + ", beaconVacantDespawnTime="
         + this.beaconVacantDespawnTime
         + ", beaconRadius="
         + this.beaconRadius
         + ", spawnRadius="
         + this.spawnRadius
         + ", npcSpawnState="
         + this.npcSpawnState
         + ", npcSpawnSubState="
         + this.npcSpawnSubState
         + ", spawnSuppression="
         + this.spawnSuppression
         + ", overrideSpawnSuppressors="
         + this.overrideSpawnSuppressors
         + ", targetSlot="
         + this.targetSlot
         + ", scaleDayTimeRange="
         + this.scaleDayTimeRange
         + ", maxSpawnsScalingCurve="
         + this.maxSpawnsScalingCurve
         + ", concurrentSpawnsScalingCurve="
         + this.concurrentSpawnsScalingCurve
         + ", debug="
         + this.debug
         + "}";
   }
}
