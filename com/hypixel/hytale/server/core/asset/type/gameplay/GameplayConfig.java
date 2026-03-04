package com.hypixel.hytale.server.core.asset.type.gameplay;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.lookup.MapKeyMapCodec;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemEntityConfig;
import com.hypixel.hytale.server.core.asset.type.soundset.config.SoundSet;
import javax.annotation.Nonnull;

public class GameplayConfig implements JsonAssetWithMap<String, DefaultAssetMap<String, GameplayConfig>> {
   public static final String DEFAULT_ID = "Default";
   public static final GameplayConfig DEFAULT = new GameplayConfig();
   @Nonnull
   public static final MapKeyMapCodec<Object> PLUGIN_CODEC = new MapKeyMapCodec<>(true);
   @Nonnull
   public static final AssetBuilderCodec<String, GameplayConfig> CODEC = AssetBuilderCodec.builder(
         GameplayConfig.class,
         GameplayConfig::new,
         Codec.STRING,
         (gameplayConfig, s) -> gameplayConfig.id = s,
         GameplayConfig::getId,
         (asset, data) -> asset.data = data,
         asset -> asset.data
      )
      .appendInherited(
         new KeyedCodec<>("Gathering", GatheringConfig.CODEC),
         (gameplayConfig, o) -> gameplayConfig.gatheringConfig = o,
         gameplayConfig -> gameplayConfig.gatheringConfig,
         (gameplayConfig, parent) -> gameplayConfig.gatheringConfig = parent.gatheringConfig
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("World", WorldConfig.CODEC),
         (gameplayConfig, o) -> gameplayConfig.worldConfig = o,
         gameplayConfig -> gameplayConfig.worldConfig,
         (gameplayConfig, parent) -> gameplayConfig.worldConfig = parent.worldConfig
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("WorldMap", WorldMapConfig.CODEC),
         (gameplayConfig, o) -> gameplayConfig.worldMapConfig = o,
         gameplayConfig -> gameplayConfig.worldMapConfig,
         (gameplayConfig, parent) -> gameplayConfig.worldMapConfig = parent.worldMapConfig
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("Death", DeathConfig.CODEC),
         (gameplayConfig, o) -> gameplayConfig.deathConfig = o,
         gameplayConfig -> gameplayConfig.deathConfig,
         (gameplayConfig, parent) -> gameplayConfig.deathConfig = parent.deathConfig
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("Respawn", RespawnConfig.CODEC),
         (gameplayConfig, o) -> gameplayConfig.respawnConfig = o,
         gameplayConfig -> gameplayConfig.respawnConfig,
         (gameplayConfig, parent) -> gameplayConfig.respawnConfig = parent.respawnConfig
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("ShowItemPickupNotifications", Codec.BOOLEAN),
         (gameplayConfig, showItemPickupNotifications) -> gameplayConfig.showItemPickupNotifications = showItemPickupNotifications,
         gameplayConfig -> gameplayConfig.showItemPickupNotifications,
         (gameplayConfig, parent) -> gameplayConfig.showItemPickupNotifications = parent.showItemPickupNotifications
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("ItemDurability", ItemDurabilityConfig.CODEC),
         (gameplayConfig, o) -> gameplayConfig.itemDurabilityConfig = o,
         gameplayConfig -> gameplayConfig.itemDurabilityConfig,
         (gameplayConfig, parent) -> gameplayConfig.itemDurabilityConfig = parent.itemDurabilityConfig
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("ItemEntity", ItemEntityConfig.CODEC),
         (gameplayConfig, itemEntityConfig) -> gameplayConfig.itemEntityConfig = itemEntityConfig,
         gameplayConfig -> gameplayConfig.itemEntityConfig,
         (gameplayConfig, parent) -> gameplayConfig.itemEntityConfig = parent.itemEntityConfig
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("Combat", CombatConfig.CODEC),
         (gameplayConfig, combatConfig) -> gameplayConfig.combatConfig = combatConfig,
         gameplayConfig -> gameplayConfig.combatConfig,
         (gameplayConfig, parent) -> gameplayConfig.combatConfig = parent.combatConfig
      )
      .add()
      .<MapKeyMapCodec.TypeMap<Object>>appendInherited(new KeyedCodec<>("Plugin", PLUGIN_CODEC), (o, i) -> {
         if (o.pluginConfig.isEmpty()) {
            o.pluginConfig = i;
         } else {
            MapKeyMapCodec.TypeMap<Object> temp = o.pluginConfig;
            o.pluginConfig = new MapKeyMapCodec.TypeMap<>(PLUGIN_CODEC);
            o.pluginConfig.putAll(temp);
            o.pluginConfig.putAll(i);
         }
      }, o -> o.pluginConfig, (o, p) -> o.pluginConfig = p.pluginConfig)
      .addValidator(Validators.nonNull())
      .add()
      .appendInherited(
         new KeyedCodec<>("Player", PlayerConfig.CODEC),
         (gameplayConfig, playerConfig) -> gameplayConfig.playerConfig = playerConfig,
         gameplayConfig -> gameplayConfig.playerConfig,
         (gameplayConfig, parent) -> gameplayConfig.playerConfig = parent.playerConfig
      )
      .add()
      .<CameraEffectsConfig>appendInherited(
         new KeyedCodec<>("CameraEffects", CameraEffectsConfig.CODEC),
         (o, i) -> o.cameraEffectsConfig = i,
         o -> o.cameraEffectsConfig,
         (o, p) -> o.cameraEffectsConfig = p.cameraEffectsConfig
      )
      .addValidator(Validators.nonNull())
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("CreativePlaySoundSet", SoundSet.CHILD_ASSET_CODEC),
         (o, i) -> o.creativePlaySoundSet = i,
         o -> o.creativePlaySoundSet,
         (o, p) -> o.creativePlaySoundSet = p.creativePlaySoundSet
      )
      .addValidator(SoundSet.VALIDATOR_CACHE.getValidator())
      .add()
      .appendInherited(
         new KeyedCodec<>("Crafting", CraftingConfig.CODEC),
         (gameplayConfig, o) -> gameplayConfig.craftingConfig = o,
         gameplayConfig -> gameplayConfig.craftingConfig,
         (gameplayConfig, parent) -> gameplayConfig.craftingConfig = parent.craftingConfig
      )
      .add()
      .appendInherited(new KeyedCodec<>("Spawn", SpawnConfig.CODEC), (o, v) -> o.spawnConfig = v, o -> o.spawnConfig, (o, p) -> o.spawnConfig = p.spawnConfig)
      .add()
      .<Integer>appendInherited(
         new KeyedCodec<>("MaxEnvironmentalNPCSpawns", Codec.INTEGER),
         (o, v) -> o.maxEnvironmentalNPCSpawns = v,
         o -> o.maxEnvironmentalNPCSpawns,
         (o, p) -> o.maxEnvironmentalNPCSpawns = p.maxEnvironmentalNPCSpawns
      )
      .documentation("The absolute maximum number of environmental NPC spawns. < 0 for infinite.")
      .add()
      .afterDecode(GameplayConfig::processConfig)
      .build();
   private static AssetStore<String, GameplayConfig, DefaultAssetMap<String, GameplayConfig>> ASSET_STORE;
   @Nonnull
   public static final ValidatorCache<String> VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(GameplayConfig::getAssetStore));
   protected AssetExtraInfo.Data data;
   protected String id;
   protected GatheringConfig gatheringConfig = new GatheringConfig();
   protected WorldConfig worldConfig = new WorldConfig();
   protected WorldMapConfig worldMapConfig = new WorldMapConfig();
   protected DeathConfig deathConfig = new DeathConfig();
   protected ItemDurabilityConfig itemDurabilityConfig = new ItemDurabilityConfig();
   protected ItemEntityConfig itemEntityConfig = new ItemEntityConfig();
   protected RespawnConfig respawnConfig = new RespawnConfig();
   protected CombatConfig combatConfig = new CombatConfig();
   protected MapKeyMapCodec.TypeMap<Object> pluginConfig = MapKeyMapCodec.TypeMap.empty();
   protected PlayerConfig playerConfig = new PlayerConfig();
   protected CameraEffectsConfig cameraEffectsConfig = new CameraEffectsConfig();
   protected CraftingConfig craftingConfig = new CraftingConfig();
   protected SpawnConfig spawnConfig = new SpawnConfig();
   protected String creativePlaySoundSet;
   protected boolean showItemPickupNotifications = true;
   protected transient int creativePlaySoundSetIndex;
   protected int maxEnvironmentalNPCSpawns = 500;

   public static AssetStore<String, GameplayConfig, DefaultAssetMap<String, GameplayConfig>> getAssetStore() {
      if (ASSET_STORE == null) {
         ASSET_STORE = AssetRegistry.getAssetStore(GameplayConfig.class);
      }

      return ASSET_STORE;
   }

   public static DefaultAssetMap<String, GameplayConfig> getAssetMap() {
      return (DefaultAssetMap<String, GameplayConfig>)getAssetStore().getAssetMap();
   }

   public GatheringConfig getGatheringConfig() {
      return this.gatheringConfig;
   }

   public WorldConfig getWorldConfig() {
      return this.worldConfig;
   }

   public WorldMapConfig getWorldMapConfig() {
      return this.worldMapConfig;
   }

   public DeathConfig getDeathConfig() {
      return this.deathConfig;
   }

   public boolean getShowItemPickupNotifications() {
      return this.showItemPickupNotifications;
   }

   public ItemDurabilityConfig getItemDurabilityConfig() {
      return this.itemDurabilityConfig;
   }

   public ItemEntityConfig getItemEntityConfig() {
      return this.itemEntityConfig;
   }

   public RespawnConfig getRespawnConfig() {
      return this.respawnConfig;
   }

   public CombatConfig getCombatConfig() {
      return this.combatConfig;
   }

   public MapKeyMapCodec.TypeMap<Object> getPluginConfig() {
      return this.pluginConfig;
   }

   public PlayerConfig getPlayerConfig() {
      return this.playerConfig;
   }

   public CameraEffectsConfig getCameraEffectsConfig() {
      return this.cameraEffectsConfig;
   }

   public String getCreativePlaySoundSet() {
      return this.creativePlaySoundSet;
   }

   public int getCreativePlaySoundSetIndex() {
      return this.creativePlaySoundSetIndex;
   }

   public CraftingConfig getCraftingConfig() {
      return this.craftingConfig;
   }

   public int getMaxEnvironmentalNPCSpawns() {
      return this.maxEnvironmentalNPCSpawns;
   }

   @Nonnull
   public SpawnConfig getSpawnConfig() {
      return this.spawnConfig;
   }

   protected void processConfig() {
      if (this.creativePlaySoundSet != null) {
         this.creativePlaySoundSetIndex = SoundSet.getAssetMap().getIndex(this.creativePlaySoundSet);
      }
   }

   public String getId() {
      return this.id;
   }
}
