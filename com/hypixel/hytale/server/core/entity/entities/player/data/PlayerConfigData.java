package com.hypixel.hytale.server.core.entity.entities.player.data;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.codec.codecs.map.Object2IntMapCodec;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockMigration;
import com.hypixel.hytale.server.core.universe.Universe;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import javax.annotation.Nonnull;

public final class PlayerConfigData {
   @Nonnull
   public static final BuilderCodec<PlayerConfigData> CODEC = BuilderCodec.builder(PlayerConfigData.class, PlayerConfigData::new)
      .addField(
         new KeyedCodec<>("BlockIdVersion", Codec.INTEGER),
         (playerConfigData, s) -> playerConfigData.blockIdVersion = s,
         playerConfigData -> playerConfigData.blockIdVersion
      )
      .addField(new KeyedCodec<>("World", Codec.STRING), (playerConfigData, s) -> playerConfigData.world = s, playerConfigData -> playerConfigData.world)
      .addField(new KeyedCodec<>("Preset", Codec.STRING), (playerConfigData, s) -> playerConfigData.preset = s, playerConfigData -> playerConfigData.preset)
      .addField(new KeyedCodec<>("KnownRecipes", new ArrayCodec<>(Codec.STRING, String[]::new)), (playerConfigData, knownRecipes) -> {
         playerConfigData.knownRecipes = Set.of(knownRecipes);
         playerConfigData.unmodifiableKnownRecipes = Collections.unmodifiableSet(playerConfigData.knownRecipes);
      }, playerConfigData -> playerConfigData.knownRecipes.toArray(String[]::new))
      .addField(new KeyedCodec<>("PerWorldData", new MapCodec<>(PlayerWorldData.CODEC, ConcurrentHashMap::new, false)), (playerConfigData, perWorldData) -> {
         playerConfigData.perWorldData = perWorldData;
         playerConfigData.unmodifiablePerWorldData = Collections.unmodifiableMap(perWorldData);
      }, playerConfigData -> playerConfigData.perWorldData)
      .addField(new KeyedCodec<>("DiscoveredZones", Codec.STRING_ARRAY), (playerConfigData, discoveredZones) -> {
         playerConfigData.discoveredZones = Set.of(discoveredZones);
         playerConfigData.unmodifiableDiscoveredZones = Collections.unmodifiableSet(playerConfigData.discoveredZones);
      }, playerConfigData -> playerConfigData.discoveredZones.toArray(String[]::new))
      .addField(new KeyedCodec<>("DiscoveredInstances", new ArrayCodec<>(Codec.UUID_BINARY, UUID[]::new)), (playerConfigData, discoveredInstances) -> {
         playerConfigData.discoveredInstances = Set.of(discoveredInstances);
         playerConfigData.unmodifiableDiscoveredInstances = Collections.unmodifiableSet(playerConfigData.discoveredInstances);
      }, playerConfigData -> playerConfigData.discoveredInstances.toArray(UUID[]::new))
      .addField(
         new KeyedCodec<>("ReputationData", new Object2IntMapCodec<>(Codec.STRING, Object2IntOpenHashMap::new, false)), (playerConfigData, reputationData) -> {
            playerConfigData.reputationData = reputationData;
            playerConfigData.unmodifiableReputationData = Object2IntMaps.unmodifiable(reputationData);
         }, playerConfigData -> playerConfigData.reputationData
      )
      .addField(
         new KeyedCodec<>("ActiveObjectiveUUIDs", new ArrayCodec<>(Codec.UUID_BINARY, UUID[]::new)),
         (playerConfigData, objectives) -> Collections.addAll(playerConfigData.activeObjectiveUUIDs, objectives),
         playerConfigData -> playerConfigData.activeObjectiveUUIDs.toArray(UUID[]::new)
      )
      .afterDecode(data -> {
         for (PlayerWorldData worldData : data.perWorldData.values()) {
            worldData.setPlayerConfigData(data);
         }

         int v = data.getBlockIdVersion();
         Map<Integer, BlockMigration> blockMigrationMap = BlockMigration.getAssetMap().getAssetMap();
         BlockMigration migration = blockMigrationMap.get(v);

         Function<String, String> blockMigration;
         for (blockMigration = null; migration != null; migration = blockMigrationMap.get(++v)) {
            if (blockMigration == null) {
               blockMigration = migration::getMigration;
            } else {
               blockMigration = blockMigration.andThen(migration::getMigration);
            }
         }

         data.setBlockIdVersion(v);
         if (blockMigration != null) {
            Set<String> oldKnownRecipes = data.getKnownRecipes();
            if (!oldKnownRecipes.isEmpty()) {
               Set<String> knownRecipes = new HashSet<>();

               for (String blockTypeKey : oldKnownRecipes) {
                  knownRecipes.add(blockMigration.apply(blockTypeKey));
               }

               data.setKnownRecipes(knownRecipes);
            }
         }
      })
      .build();
   @Nonnull
   private final transient AtomicBoolean hasChanged = new AtomicBoolean();
   private int blockIdVersion = 1;
   private String world;
   private String preset;
   @Nonnull
   private Set<String> knownRecipes = new HashSet<>();
   @Nonnull
   private Set<String> unmodifiableKnownRecipes = Collections.unmodifiableSet(this.knownRecipes);
   private Map<String, PlayerWorldData> perWorldData = new ConcurrentHashMap<>();
   @Nonnull
   private Map<String, PlayerWorldData> unmodifiablePerWorldData = Collections.unmodifiableMap(this.perWorldData);
   @Nonnull
   private Set<String> discoveredZones = new HashSet<>();
   @Nonnull
   private Set<String> unmodifiableDiscoveredZones = Collections.unmodifiableSet(this.discoveredZones);
   @Nonnull
   private Set<UUID> discoveredInstances = new HashSet<>();
   @Nonnull
   private Set<UUID> unmodifiableDiscoveredInstances = Collections.unmodifiableSet(this.discoveredInstances);
   private Object2IntMap<String> reputationData = new Object2IntOpenHashMap();
   @Nonnull
   private Object2IntMap<String> unmodifiableReputationData = Object2IntMaps.unmodifiable(this.reputationData);
   @Nonnull
   private Set<UUID> activeObjectiveUUIDs = ConcurrentHashMap.newKeySet();
   @Nonnull
   private Set<UUID> unmodifiableActiveObjectiveUUIDs = Collections.unmodifiableSet(this.activeObjectiveUUIDs);
   public final Vector3d lastSavedPosition = new Vector3d();
   public final Vector3f lastSavedRotation = new Vector3f();

   public int getBlockIdVersion() {
      return this.blockIdVersion;
   }

   public void setBlockIdVersion(int blockIdVersion) {
      this.blockIdVersion = blockIdVersion;
   }

   public String getWorld() {
      return this.world;
   }

   public void setWorld(@Nonnull String world) {
      this.world = world;
      this.markChanged();
   }

   public String getPreset() {
      return this.preset;
   }

   public void setPreset(@Nonnull String preset) {
      this.preset = preset;
      this.markChanged();
   }

   @Nonnull
   public Set<String> getKnownRecipes() {
      return this.unmodifiableKnownRecipes;
   }

   public void setKnownRecipes(@Nonnull Set<String> knownRecipes) {
      this.knownRecipes = knownRecipes;
      this.unmodifiableKnownRecipes = Collections.unmodifiableSet(knownRecipes);
      this.markChanged();
   }

   @Nonnull
   public Map<String, PlayerWorldData> getPerWorldData() {
      return this.unmodifiablePerWorldData;
   }

   @Nonnull
   public PlayerWorldData getPerWorldData(@Nonnull String worldName) {
      return this.perWorldData.computeIfAbsent(worldName, s -> new PlayerWorldData(this));
   }

   public void setPerWorldData(@Nonnull Map<String, PlayerWorldData> perWorldData) {
      this.perWorldData = perWorldData;
      this.unmodifiablePerWorldData = Collections.unmodifiableMap(perWorldData);
      this.markChanged();
   }

   @Nonnull
   public Set<String> getDiscoveredZones() {
      return this.unmodifiableDiscoveredZones;
   }

   public void setDiscoveredZones(@Nonnull Set<String> discoveredZones) {
      this.discoveredZones = discoveredZones;
      this.unmodifiableDiscoveredZones = Collections.unmodifiableSet(discoveredZones);
      this.markChanged();
   }

   @Nonnull
   public Set<UUID> getDiscoveredInstances() {
      return this.unmodifiableDiscoveredInstances;
   }

   public void setDiscoveredInstances(@Nonnull Set<UUID> discoveredInstances) {
      this.discoveredInstances = discoveredInstances;
      this.unmodifiableDiscoveredInstances = Collections.unmodifiableSet(discoveredInstances);
      this.markChanged();
   }

   @Nonnull
   public Object2IntMap<String> getReputationData() {
      return this.unmodifiableReputationData;
   }

   public void setReputationData(@Nonnull Object2IntMap<String> reputationData) {
      this.reputationData = reputationData;
      this.unmodifiableReputationData = Object2IntMaps.unmodifiable(reputationData);
      this.markChanged();
   }

   @Nonnull
   public Set<UUID> getActiveObjectiveUUIDs() {
      return this.unmodifiableActiveObjectiveUUIDs;
   }

   public void setActiveObjectiveUUIDs(@Nonnull Set<UUID> activeObjectiveUUIDs) {
      this.activeObjectiveUUIDs.clear();
      this.activeObjectiveUUIDs.addAll(activeObjectiveUUIDs);
      this.markChanged();
   }

   public void markChanged() {
      this.hasChanged.set(true);
   }

   public boolean consumeHasChanged() {
      return this.hasChanged.getAndSet(false);
   }

   public void cleanup(@Nonnull Universe universe) {
      Set<String> keySet = this.perWorldData.keySet();
      Iterator<String> iterator = keySet.iterator();

      while (iterator.hasNext()) {
         String worldName = iterator.next();
         if (worldName.startsWith("instance-") && universe.getWorld(worldName) == null) {
            iterator.remove();
         }
      }
   }
}
