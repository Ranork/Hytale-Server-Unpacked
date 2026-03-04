package com.hypixel.hytale.server.spawning;

import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.event.LoadedAssetsEvent;
import com.hypixel.hytale.assetstore.event.RemovedAssetsEvent;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.IndexedAssetMap;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.builtin.tagset.config.NPCGroup;
import com.hypixel.hytale.builtin.weather.components.WeatherTracker;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.common.map.IWeightedMap;
import com.hypixel.hytale.common.util.FormatUtil;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.data.unknown.UnknownComponents;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.RootDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.spatial.KDTree;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.server.core.Options;
import com.hypixel.hytale.server.core.asset.HytaleAssetStore;
import com.hypixel.hytale.server.core.asset.LoadAssetEvent;
import com.hypixel.hytale.server.core.asset.type.blockset.config.BlockSet;
import com.hypixel.hytale.server.core.asset.type.environment.config.Environment;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.asset.type.responsecurve.config.ResponseCurve;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.modules.entity.AllLegacyEntityTypesQuery;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.HiddenFromAdventurePlayers;
import com.hypixel.hytale.server.core.modules.entity.component.PersistentModel;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.meta.BlockStateModule;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.Config;
import com.hypixel.hytale.server.flock.config.FlockAsset;
import com.hypixel.hytale.server.npc.AllNPCsLoadedEvent;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.asset.builder.BuilderInfo;
import com.hypixel.hytale.server.npc.components.SpawnBeaconReference;
import com.hypixel.hytale.server.npc.components.SpawnMarkerReference;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.systems.SpawnReferenceSystems;
import com.hypixel.hytale.server.spawning.assets.spawnmarker.config.SpawnMarker;
import com.hypixel.hytale.server.spawning.assets.spawns.config.BeaconNPCSpawn;
import com.hypixel.hytale.server.spawning.assets.spawns.config.NPCSpawn;
import com.hypixel.hytale.server.spawning.assets.spawns.config.RoleSpawnParameters;
import com.hypixel.hytale.server.spawning.assets.spawns.config.WorldNPCSpawn;
import com.hypixel.hytale.server.spawning.assets.spawnsuppression.SpawnSuppression;
import com.hypixel.hytale.server.spawning.beacons.InitialBeaconDelay;
import com.hypixel.hytale.server.spawning.beacons.LegacySpawnBeaconEntity;
import com.hypixel.hytale.server.spawning.beacons.SpawnBeacon;
import com.hypixel.hytale.server.spawning.beacons.SpawnBeaconSystems;
import com.hypixel.hytale.server.spawning.blockstates.SpawnMarkerBlockReference;
import com.hypixel.hytale.server.spawning.blockstates.SpawnMarkerBlockState;
import com.hypixel.hytale.server.spawning.blockstates.SpawnMarkerBlockStateSystems;
import com.hypixel.hytale.server.spawning.commands.SpawnCommand;
import com.hypixel.hytale.server.spawning.corecomponents.builders.BuilderActionTriggerSpawnBeacon;
import com.hypixel.hytale.server.spawning.interactions.TriggerSpawnMarkersInteraction;
import com.hypixel.hytale.server.spawning.local.LocalSpawnBeacon;
import com.hypixel.hytale.server.spawning.local.LocalSpawnBeaconSystem;
import com.hypixel.hytale.server.spawning.local.LocalSpawnController;
import com.hypixel.hytale.server.spawning.local.LocalSpawnControllerSystem;
import com.hypixel.hytale.server.spawning.local.LocalSpawnForceTriggerSystem;
import com.hypixel.hytale.server.spawning.local.LocalSpawnSetupSystem;
import com.hypixel.hytale.server.spawning.local.LocalSpawnState;
import com.hypixel.hytale.server.spawning.managers.BeaconSpawnManager;
import com.hypixel.hytale.server.spawning.managers.SpawnManager;
import com.hypixel.hytale.server.spawning.spawnmarkers.SpawnMarkerEntity;
import com.hypixel.hytale.server.spawning.spawnmarkers.SpawnMarkerSystems;
import com.hypixel.hytale.server.spawning.suppression.component.ChunkSuppressionEntry;
import com.hypixel.hytale.server.spawning.suppression.component.ChunkSuppressionQueue;
import com.hypixel.hytale.server.spawning.suppression.component.SpawnSuppressionComponent;
import com.hypixel.hytale.server.spawning.suppression.component.SpawnSuppressionController;
import com.hypixel.hytale.server.spawning.suppression.system.ChunkSuppressionSystems;
import com.hypixel.hytale.server.spawning.suppression.system.SpawnMarkerSuppressionSystem;
import com.hypixel.hytale.server.spawning.suppression.system.SpawnSuppressionSystems;
import com.hypixel.hytale.server.spawning.systems.BeaconSpatialSystem;
import com.hypixel.hytale.server.spawning.systems.LegacyBeaconSpatialSystem;
import com.hypixel.hytale.server.spawning.systems.SpawnMarkerSpatialSystem;
import com.hypixel.hytale.server.spawning.util.FloodFillEntryPoolProviderSimple;
import com.hypixel.hytale.server.spawning.util.FloodFillPositionSelector;
import com.hypixel.hytale.server.spawning.world.WorldEnvironmentSpawnData;
import com.hypixel.hytale.server.spawning.world.WorldNPCSpawnStat;
import com.hypixel.hytale.server.spawning.world.component.ChunkSpawnData;
import com.hypixel.hytale.server.spawning.world.component.ChunkSpawnedNPCData;
import com.hypixel.hytale.server.spawning.world.component.SpawnJobData;
import com.hypixel.hytale.server.spawning.world.component.WorldSpawnData;
import com.hypixel.hytale.server.spawning.world.manager.EnvironmentSpawnParameters;
import com.hypixel.hytale.server.spawning.world.manager.WorldSpawnManager;
import com.hypixel.hytale.server.spawning.world.manager.WorldSpawnWrapper;
import com.hypixel.hytale.server.spawning.world.system.ChunkSpawningSystems;
import com.hypixel.hytale.server.spawning.world.system.MoonPhaseChangeEventSystem;
import com.hypixel.hytale.server.spawning.world.system.WorldSpawnJobSystems;
import com.hypixel.hytale.server.spawning.world.system.WorldSpawnTrackingSystem;
import com.hypixel.hytale.server.spawning.world.system.WorldSpawningSystem;
import com.hypixel.hytale.server.spawning.wrappers.BeaconSpawnWrapper;
import com.hypixel.hytale.server.spawning.wrappers.SpawnWrapper;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bson.BsonDocument;

public class SpawningPlugin extends JavaPlugin {
   private static final String DEFAULT_SPAWN_MARKER_MODEL = "NPC_Spawn_Marker";
   private static final int TICK_COLUMN_BUDGET = 20480;
   private static final float OVERPOPULATION_RATIO = 0.25F;
   private static final int OVERPOPULATION_GROUP_BUFFER = 4;
   private static SpawningPlugin instance;
   private Model spawnMarkerModel;
   private double localSpawnControllerJoinDelay;
   private int tickColumnBudget;
   private final WorldSpawnManager worldSpawnManager = new WorldSpawnManager();
   private final BeaconSpawnManager beaconSpawnManager = new BeaconSpawnManager();
   private final Config<SpawningPlugin.NPCSpawningConfig> config = this.withConfig("SpawningModule", SpawningPlugin.NPCSpawningConfig.CODEC);
   private ResourceType<EntityStore, SpatialResource<Ref<EntityStore>, EntityStore>> legacyBeaconSpatialResource;
   private ResourceType<EntityStore, SpatialResource<Ref<EntityStore>, EntityStore>> spawnMarkerSpatialResource;
   private ResourceType<EntityStore, SpatialResource<Ref<EntityStore>, EntityStore>> manualSpawnBeaconSpatialResource;
   private ComponentType<EntityStore, SpawnMarkerEntity> spawnMarkerComponentType;
   private ComponentType<EntityStore, LocalSpawnController> localSpawnControllerComponentType;
   private ResourceType<EntityStore, WorldSpawnData> worldSpawnDataResourceType;
   private ComponentType<EntityStore, SpawnSuppressionComponent> spawnSuppressorComponentType;
   private ResourceType<EntityStore, SpawnSuppressionController> spawnSuppressionControllerResourceType;
   private ComponentType<EntityStore, LocalSpawnBeacon> localSpawnBeaconComponentType;
   private ResourceType<EntityStore, LocalSpawnState> localSpawnStateResourceType;
   private ComponentType<ChunkStore, SpawnJobData> spawnJobDataComponentType;
   private ComponentType<ChunkStore, ChunkSpawnData> chunkSpawnDataComponentType;
   private ComponentType<ChunkStore, ChunkSpawnedNPCData> chunkSpawnedNPCDataComponentType;
   private ResourceType<ChunkStore, ChunkSuppressionQueue> chunkSuppressionQueueResourceType;
   private ComponentType<ChunkStore, ChunkSuppressionEntry> chunkSuppressionEntryComponentType;
   private ComponentType<EntityStore, InitialBeaconDelay> initialBeaconDelayComponentType;
   private ComponentType<EntityStore, SpawnMarkerReference> spawnMarkerReferenceComponentType;
   private ComponentType<EntityStore, SpawnBeaconReference> spawnBeaconReferenceComponentType;
   private ComponentType<EntityStore, FloodFillPositionSelector> floodFillPositionSelectorComponentType;
   private ResourceType<EntityStore, FloodFillEntryPoolProviderSimple> floodFillEntryPoolProviderSimpleResourceType;
   private ComponentType<EntityStore, SpawnMarkerBlockReference> spawnMarkerBlockReferenceComponentType;

   public static SpawningPlugin get() {
      return instance;
   }

   public SpawningPlugin(@Nonnull JavaPluginInit init) {
      super(init);
   }

   @Override
   public void setup() {
      instance = this;
      this.getCommandRegistry().registerCommand(new SpawnCommand());
      this.getEventRegistry().register(LoadedAssetsEvent.class, Environment.class, this::onEnvironmentChange);
      this.getEventRegistry().register(AllNPCsLoadedEvent.class, this::onLoadedNPCEvent);
      this.getEventRegistry().register(LoadedAssetsEvent.class, SpawnMarker.class, this::onSpawnMarkersChange);
      this.getEventRegistry().register(RemovedAssetsEvent.class, SpawnMarker.class, SpawningPlugin::onSpawnMarkersRemove);
      this.getEventRegistry().register(LoadedAssetsEvent.class, WorldNPCSpawn.class, this::onWorldNPCSpawnsLoaded);
      this.getEventRegistry().register(LoadedAssetsEvent.class, BeaconNPCSpawn.class, this::onBeaconNPCSpawnsLoaded);
      this.getEventRegistry().register(RemovedAssetsEvent.class, WorldNPCSpawn.class, this::onWorldNPCSpawnsRemoved);
      this.getEventRegistry().register(RemovedAssetsEvent.class, BeaconNPCSpawn.class, this::onBeaconNPCSpawnsRemoved);
      this.getEventRegistry().register(LoadedAssetsEvent.class, ModelAsset.class, this::onModelAssetChange);
      this.getEventRegistry().register((short)-7, LoadAssetEvent.class, this::onLoadAsset);
      this.getEntityRegistry().registerEntity("LegacySpawnBeacon", LegacySpawnBeaconEntity.class, LegacySpawnBeaconEntity::new, LegacySpawnBeaconEntity.CODEC);
      this.getEntityRegistry().registerEntity("SpawnBeacon", SpawnBeacon.class, SpawnBeacon::new, SpawnBeacon.CODEC);
      AssetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(
                              SpawnMarker.class, new DefaultAssetMap()
                           )
                           .setPath("NPC/Spawn/Markers"))
                        .setCodec(SpawnMarker.CODEC))
                     .setKeyFunction(SpawnMarker::getId))
                  .loadsAfter(FlockAsset.class, ModelAsset.class))
               .loadsBefore(Interaction.class))
            .build()
      );
      AssetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(
                              WorldNPCSpawn.class, new IndexedLookupTableAssetMap<>(WorldNPCSpawn[]::new)
                           )
                           .setPath("NPC/Spawn/World"))
                        .setCodec(WorldNPCSpawn.CODEC))
                     .setKeyFunction(WorldNPCSpawn::getId))
                  .setReplaceOnRemove(WorldNPCSpawn::new))
               .loadsAfter(Environment.class, BlockSet.class, FlockAsset.class))
            .build()
      );
      AssetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(
                              BeaconNPCSpawn.class, new IndexedLookupTableAssetMap<>(BeaconNPCSpawn[]::new)
                           )
                           .setPath("NPC/Spawn/Beacons"))
                        .setCodec(BeaconNPCSpawn.CODEC))
                     .setKeyFunction(BeaconNPCSpawn::getId))
                  .setReplaceOnRemove(BeaconNPCSpawn::new))
               .loadsAfter(Environment.class, BlockSet.class, SpawnSuppression.class, FlockAsset.class, ModelAsset.class, ResponseCurve.class))
            .build()
      );
      AssetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(
                              SpawnSuppression.class, new IndexedAssetMap()
                           )
                           .setPath("NPC/Spawn/Suppression"))
                        .setCodec(SpawnSuppression.CODEC))
                     .setKeyFunction(SpawnSuppression::getId))
                  .setReplaceOnRemove(SpawnSuppression::new))
               .loadsAfter(NPCGroup.class))
            .build()
      );
      NPCPlugin.get().registerCoreComponentType("TriggerSpawnBeacon", BuilderActionTriggerSpawnBeacon::new);
      BlockStateModule.get()
         .registerBlockState(
            SpawnMarkerBlockState.class, "SpawnMarkerBlock", SpawnMarkerBlockState.CODEC, SpawnMarkerBlockState.Data.class, SpawnMarkerBlockState.Data.CODEC
         );
      this.spawnMarkerComponentType = this.getEntityStoreRegistry().registerComponent(SpawnMarkerEntity.class, "SpawnMarkerComponent", SpawnMarkerEntity.CODEC);
      this.localSpawnControllerComponentType = this.getEntityStoreRegistry().registerComponent(LocalSpawnController.class, LocalSpawnController::new);
      this.worldSpawnDataResourceType = this.getEntityStoreRegistry().registerResource(WorldSpawnData.class, WorldSpawnData::new);
      this.localSpawnBeaconComponentType = this.getEntityStoreRegistry().registerComponent(LocalSpawnBeacon.class, "LocalSpawnBeacon", LocalSpawnBeacon.CODEC);
      this.localSpawnStateResourceType = this.getEntityStoreRegistry().registerResource(LocalSpawnState.class, LocalSpawnState::new);
      this.legacyBeaconSpatialResource = this.getEntityStoreRegistry().registerSpatialResource(() -> new KDTree<>(Ref::isValid));
      this.spawnMarkerSpatialResource = this.getEntityStoreRegistry().registerSpatialResource(() -> new KDTree<>(Ref::isValid));
      this.manualSpawnBeaconSpatialResource = this.getEntityStoreRegistry().registerSpatialResource(() -> new KDTree<>(Ref::isValid));
      this.spawnSuppressorComponentType = this.getEntityStoreRegistry()
         .registerComponent(SpawnSuppressionComponent.class, "SpawnSuppression", SpawnSuppressionComponent.CODEC);
      this.spawnSuppressionControllerResourceType = this.getEntityStoreRegistry()
         .registerResource(SpawnSuppressionController.class, "SpawnSuppressionController", SpawnSuppressionController.CODEC);
      this.initialBeaconDelayComponentType = this.getEntityStoreRegistry().registerComponent(InitialBeaconDelay.class, InitialBeaconDelay::new);
      this.spawnMarkerReferenceComponentType = this.getEntityStoreRegistry()
         .registerComponent(SpawnMarkerReference.class, "SpawnMarkerReference", SpawnMarkerReference.CODEC);
      this.spawnBeaconReferenceComponentType = this.getEntityStoreRegistry()
         .registerComponent(SpawnBeaconReference.class, "SpawnBeaconReference", SpawnBeaconReference.CODEC);
      this.floodFillPositionSelectorComponentType = this.getEntityStoreRegistry().registerComponent(FloodFillPositionSelector.class, () -> {
         throw new UnsupportedOperationException("Not implemented");
      });
      this.floodFillEntryPoolProviderSimpleResourceType = this.getEntityStoreRegistry()
         .registerResource(FloodFillEntryPoolProviderSimple.class, FloodFillEntryPoolProviderSimple::new);
      this.spawnMarkerBlockReferenceComponentType = this.getEntityStoreRegistry()
         .registerComponent(SpawnMarkerBlockReference.class, "SpawnMarkerBlockReference", SpawnMarkerBlockReference.CODEC);
      this.spawnJobDataComponentType = this.getChunkStoreRegistry().registerComponent(SpawnJobData.class, SpawnJobData::new);
      this.chunkSpawnDataComponentType = this.getChunkStoreRegistry().registerComponent(ChunkSpawnData.class, ChunkSpawnData::new);
      this.chunkSpawnedNPCDataComponentType = this.getChunkStoreRegistry()
         .registerComponent(ChunkSpawnedNPCData.class, "ChunkSpawnedNPCData", ChunkSpawnedNPCData.CODEC);
      this.chunkSuppressionQueueResourceType = this.getChunkStoreRegistry().registerResource(ChunkSuppressionQueue.class, ChunkSuppressionQueue::new);
      this.chunkSuppressionEntryComponentType = this.getChunkStoreRegistry().registerComponent(ChunkSuppressionEntry.class, () -> {
         throw new UnsupportedOperationException("Not implemented");
      });
      EntityModule entityModule = EntityModule.get();
      ComponentType<EntityStore, Player> playerComponentType = entityModule.getPlayerComponentType();
      ComponentType<EntityStore, TransformComponent> transformComponentType = entityModule.getTransformComponentType();
      ComponentType<EntityStore, LegacySpawnBeaconEntity> legacySpawnBeaconComponentType = entityModule.getComponentType(LegacySpawnBeaconEntity.class);
      ComponentType<EntityStore, SpawnBeacon> spawnBeaconComponentType = entityModule.getComponentType(SpawnBeacon.class);
      ResourceType<EntityStore, SpatialResource<Ref<EntityStore>, EntityStore>> playerSpatialComponent = entityModule.getPlayerSpatialResourceType();
      this.getEntityStoreRegistry().registerSystem(new SpawnSuppressionSystems.EnsureNetworkSendable());
      this.getEntityStoreRegistry()
         .registerSystem(
            new SpawnSuppressionSystems.Load(
               this.spawnSuppressionControllerResourceType,
               this.spawnMarkerComponentType,
               this.chunkSuppressionQueueResourceType,
               this.chunkSuppressionEntryComponentType
            )
         );
      this.getEntityStoreRegistry()
         .registerSystem(
            new SpawnSuppressionSystems.Suppressor(
               this.spawnSuppressorComponentType,
               this.spawnSuppressionControllerResourceType,
               this.spawnMarkerComponentType,
               this.chunkSuppressionQueueResourceType,
               this.spawnMarkerSpatialResource
            )
         );
      this.getEntityStoreRegistry().registerSystem(new LegacyBeaconSpatialSystem(this.legacyBeaconSpatialResource));
      this.getEntityStoreRegistry().registerSystem(new SpawnMarkerSpatialSystem(this.spawnMarkerSpatialResource));
      this.getEntityStoreRegistry().registerSystem(new BeaconSpatialSystem(this.manualSpawnBeaconSpatialResource));
      this.getEntityStoreRegistry()
         .registerSystem(new SpawnMarkerSuppressionSystem(this.spawnMarkerComponentType, this.spawnSuppressionControllerResourceType));
      this.getChunkStoreRegistry()
         .registerSystem(new ChunkSuppressionSystems.ChunkAdded(this.chunkSuppressionEntryComponentType, this.spawnSuppressionControllerResourceType));
      this.getChunkStoreRegistry()
         .registerSystem(new ChunkSuppressionSystems.Ticking(this.chunkSuppressionEntryComponentType, this.chunkSuppressionQueueResourceType));
      this.getEntityStoreRegistry().registerSystem(new LocalSpawnSetupSystem(playerComponentType));
      this.getEntityStoreRegistry()
         .registerSystem(
            new LocalSpawnControllerSystem(
               this.localSpawnControllerComponentType,
               transformComponentType,
               WeatherTracker.getComponentType(),
               this.localSpawnBeaconComponentType,
               legacySpawnBeaconComponentType,
               this.localSpawnStateResourceType,
               this.legacyBeaconSpatialResource
            )
         );
      this.getEntityStoreRegistry().registerSystem(new LocalSpawnBeaconSystem(this.localSpawnBeaconComponentType, this.localSpawnStateResourceType));
      this.getEntityStoreRegistry().registerSystem(new LocalSpawnForceTriggerSystem(this.localSpawnControllerComponentType, this.localSpawnStateResourceType));
      this.getEntityStoreRegistry().registerSystem(new SpawnBeaconSystems.LegacyEntityAdded(legacySpawnBeaconComponentType));
      this.getEntityStoreRegistry().registerSystem(new SpawnBeaconSystems.EntityAdded(spawnBeaconComponentType));
      this.getEntityStoreRegistry().registerSystem(new SpawnBeaconSystems.CheckDespawn(legacySpawnBeaconComponentType, this.initialBeaconDelayComponentType));
      this.getEntityStoreRegistry()
         .registerSystem(
            new SpawnBeaconSystems.PositionSelectorUpdate(this.floodFillPositionSelectorComponentType, this.floodFillEntryPoolProviderSimpleResourceType)
         );
      this.getEntityStoreRegistry()
         .registerSystem(
            new SpawnBeaconSystems.ControllerTick(
               legacySpawnBeaconComponentType, this.floodFillPositionSelectorComponentType, this.initialBeaconDelayComponentType
            )
         );
      this.getEntityStoreRegistry().registerSystem(new SpawnBeaconSystems.SpawnJobTick(legacySpawnBeaconComponentType, this.initialBeaconDelayComponentType));
      this.getEntityStoreRegistry().registerSystem(new SpawnBeaconSystems.LoadTimeDelay(this.initialBeaconDelayComponentType));
      this.getEntityStoreRegistry().registerSystem(new SpawnMarkerSystems.LegacyEntityMigration());
      this.getEntityStoreRegistry().registerSystem(new SpawnMarkerSystems.EnsureNetworkSendable());
      this.getEntityStoreRegistry().registerSystem(new SpawnMarkerSystems.CacheMarker(this.spawnMarkerComponentType));
      this.getEntityStoreRegistry().registerSystem(new SpawnMarkerSystems.EntityAdded(this.spawnMarkerComponentType));
      this.getEntityStoreRegistry().registerSystem(new SpawnMarkerSystems.EntityAddedFromExternal(this.spawnMarkerComponentType));
      this.getEntityStoreRegistry().registerSystem(new SpawnMarkerSystems.AddedFromWorldGen());
      this.getEntityStoreRegistry().registerSystem(new SpawnMarkerSystems.Ticking(this.spawnMarkerComponentType, playerSpatialComponent));
      this.getEntityStoreRegistry()
         .registerSystem(new SpawnReferenceSystems.MarkerAddRemoveSystem(SpawnMarkerReference.getComponentType(), this.spawnMarkerComponentType));
      this.getEntityStoreRegistry()
         .registerSystem(new SpawnReferenceSystems.BeaconAddRemoveSystem(SpawnBeaconReference.getComponentType(), legacySpawnBeaconComponentType));
      this.getEntityStoreRegistry()
         .registerSystem(new WorldSpawnTrackingSystem(this.worldSpawnDataResourceType, this.chunkSpawnDataComponentType, this.chunkSpawnedNPCDataComponentType));
      this.getEntityStoreRegistry().registerSystem(new MoonPhaseChangeEventSystem());
      this.getEntityStoreRegistry()
         .registerSystem(new SpawnReferenceSystems.TickingSpawnMarkerSystem(this.spawnMarkerReferenceComponentType, this.spawnMarkerComponentType));
      this.getEntityStoreRegistry().registerSystem(new SpawnReferenceSystems.TickingSpawnBeaconSystem(this.spawnBeaconReferenceComponentType));
      this.getChunkStoreRegistry()
         .registerSystem(
            new WorldSpawningSystem(
               this.worldSpawnDataResourceType, this.chunkSpawnDataComponentType, this.chunkSpawnedNPCDataComponentType, this.spawnJobDataComponentType
            )
         );
      this.getChunkStoreRegistry().registerSystem(new WorldSpawnJobSystems.EntityRemoved(this.worldSpawnDataResourceType, this.spawnJobDataComponentType));
      this.getChunkStoreRegistry()
         .registerSystem(
            new WorldSpawnJobSystems.Ticking(
               this.worldSpawnDataResourceType,
               this.spawnSuppressionControllerResourceType,
               this.spawnJobDataComponentType,
               this.chunkSpawnDataComponentType,
               this.chunkSpawnedNPCDataComponentType
            )
         );
      this.getChunkStoreRegistry().registerSystem(new WorldSpawnJobSystems.TickingState(this.worldSpawnDataResourceType, this.spawnJobDataComponentType));
      this.getChunkStoreRegistry()
         .registerSystem(
            new ChunkSpawningSystems.ChunkRefAdded(this.worldSpawnDataResourceType, this.chunkSpawnDataComponentType, this.chunkSpawnedNPCDataComponentType)
         );
      this.getChunkStoreRegistry()
         .registerSystem(
            new ChunkSpawningSystems.TickingState(this.worldSpawnDataResourceType, this.chunkSpawnDataComponentType, this.chunkSpawnedNPCDataComponentType)
         );
      ComponentType<ChunkStore, SpawnMarkerBlockState> spawnMarkerBlockStateComponentType = BlockStateModule.get()
         .getComponentType(SpawnMarkerBlockState.class);
      this.getChunkStoreRegistry().registerSystem(new SpawnMarkerBlockStateSystems.AddOrRemove(spawnMarkerBlockStateComponentType));
      this.getChunkStoreRegistry().registerSystem(new SpawnMarkerBlockStateSystems.TickHeartbeat(spawnMarkerBlockStateComponentType));
      this.getEntityStoreRegistry().registerSystem(new SpawnMarkerBlockStateSystems.SpawnMarkerAddedFromExternal(this.spawnMarkerBlockReferenceComponentType));
      this.getEntityStoreRegistry().registerSystem(new SpawnMarkerBlockStateSystems.SpawnMarkerTickHeartbeat(this.spawnMarkerBlockReferenceComponentType));
      this.getEntityStoreRegistry().registerSystem(new EntityModule.HiddenFromPlayerMigrationSystem(this.spawnSuppressorComponentType), true);
      this.getEntityStoreRegistry().registerSystem(new SpawningPlugin.LegacySpawnSuppressorEntityMigration());
      Interaction.CODEC.register("TriggerSpawnMarkers", TriggerSpawnMarkersInteraction.class, TriggerSpawnMarkersInteraction.CODEC);
   }

   @Override
   public void start() {
      SpawningPlugin.NPCSpawningConfig config = this.config.get();
      String spawnMarkerModelId = config.defaultMarkerModel;
      this.localSpawnControllerJoinDelay = config.localSpawnControllerJoinDelay;
      this.tickColumnBudget = MathUtil.floor(config.spawnBudgetFactor * 20480.0);
      DefaultAssetMap<String, ModelAsset> modelAssetMap = ModelAsset.getAssetMap();
      ModelAsset modelAsset = modelAssetMap.getAsset(spawnMarkerModelId);
      if (modelAsset == null) {
         this.getLogger().at(Level.SEVERE).log("Spawn marker model %s does not exist");
         modelAsset = modelAssetMap.getAsset("NPC_Spawn_Marker");
         if (modelAsset == null) {
            throw new IllegalStateException(String.format("Default spawn marker '%s' not found", "NPC_Spawn_Marker"));
         }
      }

      this.spawnMarkerModel = Model.createUnitScaleModel(modelAsset);
      this.setUpWithAllRoles();
   }

   @Override
   public void shutdown() {
   }

   public ResourceType<EntityStore, SpatialResource<Ref<EntityStore>, EntityStore>> getSpawnMarkerSpatialResource() {
      return this.spawnMarkerSpatialResource;
   }

   public ResourceType<EntityStore, SpatialResource<Ref<EntityStore>, EntityStore>> getManualSpawnBeaconSpatialResource() {
      return this.manualSpawnBeaconSpatialResource;
   }

   public ComponentType<EntityStore, SpawnMarkerEntity> getSpawnMarkerComponentType() {
      return this.spawnMarkerComponentType;
   }

   public ComponentType<EntityStore, LocalSpawnController> getLocalSpawnControllerComponentType() {
      return this.localSpawnControllerComponentType;
   }

   public ResourceType<EntityStore, WorldSpawnData> getWorldSpawnDataResourceType() {
      return this.worldSpawnDataResourceType;
   }

   public ComponentType<EntityStore, SpawnSuppressionComponent> getSpawnSuppressorComponentType() {
      return this.spawnSuppressorComponentType;
   }

   public ResourceType<EntityStore, SpawnSuppressionController> getSpawnSuppressionControllerResourceType() {
      return this.spawnSuppressionControllerResourceType;
   }

   public ComponentType<EntityStore, LocalSpawnBeacon> getLocalSpawnBeaconComponentType() {
      return this.localSpawnBeaconComponentType;
   }

   public ResourceType<EntityStore, LocalSpawnState> getLocalSpawnStateResourceType() {
      return this.localSpawnStateResourceType;
   }

   public ComponentType<EntityStore, InitialBeaconDelay> getInitialBeaconDelayComponentType() {
      return this.initialBeaconDelayComponentType;
   }

   public ComponentType<ChunkStore, SpawnJobData> getSpawnJobDataComponentType() {
      return this.spawnJobDataComponentType;
   }

   public ComponentType<ChunkStore, ChunkSpawnData> getChunkSpawnDataComponentType() {
      return this.chunkSpawnDataComponentType;
   }

   public ComponentType<ChunkStore, ChunkSpawnedNPCData> getChunkSpawnedNPCDataComponentType() {
      return this.chunkSpawnedNPCDataComponentType;
   }

   public ResourceType<ChunkStore, ChunkSuppressionQueue> getChunkSuppressionQueueResourceType() {
      return this.chunkSuppressionQueueResourceType;
   }

   public ResourceType<EntityStore, FloodFillEntryPoolProviderSimple> getFloodFillEntryPoolProviderSimpleResourceType() {
      return this.floodFillEntryPoolProviderSimpleResourceType;
   }

   public ComponentType<ChunkStore, ChunkSuppressionEntry> getChunkSuppressionEntryComponentType() {
      return this.chunkSuppressionEntryComponentType;
   }

   public BeaconSpawnWrapper getBeaconSpawnWrapper(int configId) {
      return this.beaconSpawnManager.getSpawnWrapper(configId);
   }

   public ComponentType<EntityStore, SpawnMarkerReference> getSpawnMarkerReferenceComponentType() {
      return this.spawnMarkerReferenceComponentType;
   }

   public ComponentType<EntityStore, SpawnBeaconReference> getSpawnBeaconReferenceComponentType() {
      return this.spawnBeaconReferenceComponentType;
   }

   public ComponentType<EntityStore, FloodFillPositionSelector> getFloodFillPositionSelectorComponentType() {
      return this.floodFillPositionSelectorComponentType;
   }

   public ComponentType<EntityStore, SpawnMarkerBlockReference> getSpawnMarkerBlockReferenceComponentType() {
      return this.spawnMarkerBlockReferenceComponentType;
   }

   public boolean shouldNPCDespawn(
      @Nonnull Store<EntityStore> store, @Nonnull NPCEntity npcComponent, @Nonnull WorldTimeResource timeManager, int configuration, boolean beaconSpawn
   ) {
      if (configuration == Integer.MIN_VALUE) {
         return false;
      } else {
         SpawnManager manager = (SpawnManager)(beaconSpawn ? this.beaconSpawnManager : this.worldSpawnManager);
         SpawnWrapper wrapper = manager.getSpawnWrapper(configuration);
         if (wrapper == null) {
            return false;
         } else {
            if (!beaconSpawn) {
               int environment = npcComponent.getEnvironment();
               if (environment != Integer.MIN_VALUE) {
                  WorldSpawnData worldSpawnData = store.getResource(WorldSpawnData.getResourceType());
                  WorldEnvironmentSpawnData environmentSpawnData = worldSpawnData.getWorldEnvironmentSpawnData(environment);
                  if (environmentSpawnData != null) {
                     WorldNPCSpawnStat npcSpawnData = (WorldNPCSpawnStat)environmentSpawnData.getNpcStatMap().get(npcComponent.getRoleIndex());
                     if (npcSpawnData != null && npcSpawnData.getActual() > npcSpawnData.getExpected() * 1.25 + 4.0) {
                        get()
                           .getLogger()
                           .at(Level.WARNING)
                           .log(
                              "Removing NPC of type %s due to overpopulation (expected: %f, actual: %d)",
                              npcComponent.getRoleName(),
                              npcSpawnData.getExpected(),
                              npcSpawnData.getActual()
                           );
                        return true;
                     }
                  }
               }
            }

            World world = store.getExternalData().getWorld();
            return wrapper.shouldDespawn(world, timeManager);
         }
      }
   }

   public Model getSpawnMarkerModel() {
      return this.spawnMarkerModel;
   }

   public EnvironmentSpawnParameters getWorldEnvironmentSpawnParameters(int environmentIndex) {
      return this.worldSpawnManager.getEnvironmentSpawnParameters(environmentIndex);
   }

   public List<BeaconSpawnWrapper> getBeaconSpawnsForEnvironment(int environmentIndex) {
      return this.beaconSpawnManager.getBeaconSpawns(environmentIndex);
   }

   public IntSet getRolesForEnvironment(int environment) {
      return this.worldSpawnManager.getRolesForEnvironment(environment);
   }

   public int getTickColumnBudget() {
      return this.tickColumnBudget;
   }

   public int getMaxActiveJobs() {
      return this.config.get().maxActiveJobs;
   }

   public double getLocalSpawnControllerJoinDelay() {
      return this.localSpawnControllerJoinDelay;
   }

   public static <T extends NPCSpawn> void validateSpawnsConfigurations(@Nonnull String type, @Nonnull Map<String, T> spawns, @Nonnull List<String> errors) {
      for (Entry<String, T> spawn : spawns.entrySet()) {
         RoleSpawnParameters[] spawnParameters = spawn.getValue().getNPCs();

         for (RoleSpawnParameters spawnParameter : spawnParameters) {
            try {
               NPCPlugin.get().validateSpawnableRole(spawnParameter.getId());
            } catch (IllegalArgumentException var11) {
               errors.add(type + " " + spawn.getKey() + ": " + var11.getMessage());
            }
         }
      }
   }

   public static void validateSpawnMarkers(@Nonnull Map<String, SpawnMarker> markers, @Nonnull List<String> errors) {
      for (Entry<String, SpawnMarker> marker : markers.entrySet()) {
         IWeightedMap<SpawnMarker.SpawnConfiguration> configs = marker.getValue().getWeightedConfigurations();
         if (configs == null) {
            errors.add("Spawn marker " + marker.getKey() + ": No configurations defined");
         } else {
            configs.forEach(config -> {
               try {
                  String npcConfig = config.getNpc();
                  if (npcConfig != null) {
                     NPCPlugin.get().validateSpawnableRole(npcConfig);
                  }
               } catch (IllegalArgumentException var4x) {
                  errors.add("Spawn marker " + marker.getKey() + ": " + var4x.getMessage());
               }
            });
         }
      }
   }

   public double getEnvironmentDensity(int environmentIndex) {
      EnvironmentSpawnParameters environment = this.getWorldEnvironmentSpawnParameters(environmentIndex);
      return environment != null ? environment.getSpawnDensity() : 0.0;
   }

   protected void onSpawnMarkersChange(@Nonnull LoadedAssetsEvent<String, SpawnMarker, DefaultAssetMap<String, SpawnMarker>> event) {
      Map<String, SpawnMarker> loadedAssets = event.getLoadedAssets();
      Universe.get()
         .getWorlds()
         .forEach(
            (name, world) -> world.execute(
               () -> world.getEntityStore()
                  .getStore()
                  .forEachChunk(
                     SpawnMarkerEntity.getComponentType(),
                     (archetypeChunk, commandBuffer) -> {
                        for (int index = 0; index < archetypeChunk.size(); index++) {
                           SpawnMarkerEntity spawnMarkerEntity = archetypeChunk.getComponent(index, SpawnMarkerEntity.getComponentType());
                           if (loadedAssets.containsKey(spawnMarkerEntity.getSpawnMarkerId())) {
                              Holder<EntityStore> holder = commandBuffer.removeEntity(
                                 archetypeChunk.getReferenceTo(index), EntityStore.REGISTRY.newHolder(), RemoveReason.UNLOAD
                              );
                              commandBuffer.addEntity(holder, AddReason.LOAD);
                           }
                        }
                     }
                  )
            )
         );
      if (!NPCPlugin.get().getBuilderManager().isEmpty()) {
         ObjectArrayList<String> errors = new ObjectArrayList();
         validateSpawnMarkers(event.getLoadedAssets(), errors);
         ObjectListIterator var4 = errors.iterator();

         while (var4.hasNext()) {
            String error = (String)var4.next();
            this.getLogger().at(Level.SEVERE).log(error);
         }
      }
   }

   protected static void onSpawnMarkersRemove(@Nonnull RemovedAssetsEvent<String, SpawnMarker, DefaultAssetMap<String, SpawnMarker>> event) {
      Set<String> removedAssets = event.getRemovedAssets();
      Universe.get()
         .getWorlds()
         .forEach(
            (name, world) -> world.execute(
               () -> world.getEntityStore().getStore().forEachChunk(SpawnMarkerEntity.getComponentType(), (archetypeChunk, commandBuffer) -> {
                  for (int index = 0; index < archetypeChunk.size(); index++) {
                     SpawnMarkerEntity spawnMarkerEntityComponent = archetypeChunk.getComponent(index, SpawnMarkerEntity.getComponentType());
                     if (spawnMarkerEntityComponent != null && removedAssets.contains(spawnMarkerEntityComponent.getSpawnMarkerId())) {
                        commandBuffer.removeEntity(archetypeChunk.getReferenceTo(index), RemoveReason.REMOVE);
                     }
                  }
               })
            )
         );
   }

   private void onEnvironmentChange(@Nonnull LoadedAssetsEvent<String, Environment, IndexedLookupTableAssetMap<String, Environment>> event) {
      IndexedLookupTableAssetMap<String, Environment> environmentAssetMap = Environment.getAssetMap();

      for (Entry<String, Environment> entry : event.getLoadedAssets().entrySet()) {
         String environment = entry.getKey();
         int index = environmentAssetMap.getIndex(environment);
         if (index == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unknown key! " + environment);
         }

         this.worldSpawnManager.updateSpawnParameters(index, entry.getValue());
      }

      WorldSpawnManager.onEnvironmentChanged();
   }

   private void onWorldNPCSpawnsLoaded(@Nonnull LoadedAssetsEvent<String, WorldNPCSpawn, IndexedLookupTableAssetMap<String, WorldNPCSpawn>> event) {
      if (!NPCPlugin.get().getBuilderManager().isEmpty()) {
         IntOpenHashSet changeSet = new IntOpenHashSet();

         for (String config : event.getLoadedAssets().keySet()) {
            int index = ((IndexedLookupTableAssetMap)event.getAssetMap()).getIndex(config);
            if (index == Integer.MIN_VALUE) {
               throw new IllegalArgumentException("Unknown key! " + config);
            }

            changeSet.add(index);
         }

         this.worldSpawnManager.rebuildConfigurations(changeSet);
         ObjectArrayList<String> errors = new ObjectArrayList();
         validateSpawnsConfigurations("World spawn", event.getLoadedAssets(), errors);
         ObjectListIterator var7 = errors.iterator();

         while (var7.hasNext()) {
            String error = (String)var7.next();
            this.getLogger().at(Level.SEVERE).log(error);
         }
      }
   }

   private void onBeaconNPCSpawnsLoaded(@Nonnull LoadedAssetsEvent<String, BeaconNPCSpawn, IndexedLookupTableAssetMap<String, BeaconNPCSpawn>> event) {
      if (!NPCPlugin.get().getBuilderManager().isEmpty()) {
         IntOpenHashSet changeSet = new IntOpenHashSet();

         for (String config : event.getLoadedAssets().keySet()) {
            int index = ((IndexedLookupTableAssetMap)event.getAssetMap()).getIndex(config);
            if (index == Integer.MIN_VALUE) {
               throw new IllegalArgumentException("Unknown key! " + config);
            }

            changeSet.add(index);
         }

         this.rebuildBeaconSpawnConfigurations(changeSet);
         Map<String, BeaconNPCSpawn> loadedAssets = event.getLoadedAssets();
         Universe.get().getWorlds().forEach((name, world) -> world.execute(() -> {
            Store<EntityStore> store = world.getEntityStore().getStore();
            store.forEachChunk(LegacySpawnBeaconEntity.getComponentType(), (archetypeChunk, commandBuffer) -> {
               for (int indexx = 0; indexx < archetypeChunk.size(); indexx++) {
                  LegacySpawnBeaconEntity legacySpawnBeaconComponent = archetypeChunk.getComponent(indexx, LegacySpawnBeaconEntity.getComponentType());

                  assert legacySpawnBeaconComponent != null;

                  if (loadedAssets.containsKey(legacySpawnBeaconComponent.getSpawnConfigId())) {
                     Ref<EntityStore> spawnBeaconRef = archetypeChunk.getReferenceTo(indexx);
                     Holder<EntityStore> holder = commandBuffer.removeEntity(spawnBeaconRef, EntityStore.REGISTRY.newHolder(), RemoveReason.UNLOAD);
                     commandBuffer.addEntity(holder, AddReason.LOAD);
                  }
               }
            });
            store.forEachChunk(SpawnBeacon.getComponentType(), (archetypeChunk, commandBuffer) -> {
               for (int indexx = 0; indexx < archetypeChunk.size(); indexx++) {
                  SpawnBeacon legacySpawnBeaconComponent = archetypeChunk.getComponent(indexx, SpawnBeacon.getComponentType());

                  assert legacySpawnBeaconComponent != null;

                  if (loadedAssets.containsKey(legacySpawnBeaconComponent.getSpawnConfigId())) {
                     Ref<EntityStore> spawnBeaconRef = archetypeChunk.getReferenceTo(indexx);
                     Holder<EntityStore> holder = commandBuffer.removeEntity(spawnBeaconRef, EntityStore.REGISTRY.newHolder(), RemoveReason.UNLOAD);
                     commandBuffer.addEntity(holder, AddReason.LOAD);
                  }
               }
            });
         }));
         ObjectArrayList<String> errors = new ObjectArrayList();
         validateSpawnsConfigurations("Beacon spawn", event.getLoadedAssets(), errors);
         ObjectListIterator var9 = errors.iterator();

         while (var9.hasNext()) {
            String error = (String)var9.next();
            this.getLogger().at(Level.SEVERE).log(error);
         }
      }
   }

   private void onWorldNPCSpawnsRemoved(@Nonnull RemovedAssetsEvent<String, WorldNPCSpawn, IndexedLookupTableAssetMap<String, WorldNPCSpawn>> event) {
      for (String removed : event.getRemovedAssets()) {
         this.worldSpawnManager.onNPCSpawnRemoved(removed);
      }

      WorldSpawnManager.onEnvironmentChanged();
   }

   private void onBeaconNPCSpawnsRemoved(@Nonnull RemovedAssetsEvent<String, BeaconNPCSpawn, IndexedLookupTableAssetMap<String, BeaconNPCSpawn>> event) {
      for (String removed : event.getRemovedAssets()) {
         this.beaconSpawnManager.onNPCSpawnRemoved(removed);
      }

      Set<String> removedAssets = event.getRemovedAssets();
      Universe.get().getWorlds().forEach((name, world) -> world.execute(() -> {
         Store<EntityStore> store = world.getEntityStore().getStore();
         store.forEachChunk(LegacySpawnBeaconEntity.getComponentType(), (archetypeChunk, commandBuffer) -> {
            for (int index = 0; index < archetypeChunk.size(); index++) {
               LegacySpawnBeaconEntity spawnBeaconComponent = archetypeChunk.getComponent(index, LegacySpawnBeaconEntity.getComponentType());

               assert spawnBeaconComponent != null;

               if (removedAssets.contains(spawnBeaconComponent.getSpawnConfigId())) {
                  Ref<EntityStore> spawnBeaconRef = archetypeChunk.getReferenceTo(index);
                  commandBuffer.removeEntity(spawnBeaconRef, RemoveReason.REMOVE);
               }
            }
         });
         store.forEachChunk(SpawnBeacon.getComponentType(), (archetypeChunk, commandBuffer) -> {
            for (int index = 0; index < archetypeChunk.size(); index++) {
               SpawnBeacon spawnBeaconComponent = archetypeChunk.getComponent(index, SpawnBeacon.getComponentType());

               assert spawnBeaconComponent != null;

               if (removedAssets.contains(spawnBeaconComponent.getSpawnConfigId())) {
                  Ref<EntityStore> spawnBeaconRef = archetypeChunk.getReferenceTo(index);
                  commandBuffer.removeEntity(spawnBeaconRef, RemoveReason.REMOVE);
               }
            }
         });
      }));
   }

   private void onLoadedNPCEvent(@Nonnull AllNPCsLoadedEvent loadedNPCEvent) {
      IntOpenHashSet changeSet = new IntOpenHashSet();
      Int2ObjectMap<BuilderInfo> loadedNPCs = loadedNPCEvent.getLoadedNPCs();
      ObjectIterator var4 = loadedNPCs.values().iterator();

      while (var4.hasNext()) {
         BuilderInfo builder = (BuilderInfo)var4.next();
         String key = builder.getKeyName();
         this.worldSpawnManager.onNPCLoaded(key, changeSet);
         this.beaconSpawnManager.onNPCLoaded(key, changeSet);
      }

      this.worldSpawnManager.rebuildConfigurations(changeSet);
      this.rebuildBeaconSpawnConfigurations(changeSet);
   }

   private void setUpWithAllRoles() {
      IntOpenHashSet changeSet = new IntOpenHashSet();
      IndexedLookupTableAssetMap<String, WorldNPCSpawn> npcWorldSpawnMap = WorldNPCSpawn.getAssetMap();
      Map<String, WorldNPCSpawn> assetMap = npcWorldSpawnMap.getAssetMap();
      int worldSetupCount = 0;

      for (Entry<String, WorldNPCSpawn> entry : assetMap.entrySet()) {
         WorldNPCSpawn value = entry.getValue();
         if (this.worldSpawnManager.addSpawnWrapper(new WorldSpawnWrapper(value))) {
            worldSetupCount++;
         }

         String key = entry.getKey();
         int index = npcWorldSpawnMap.getIndex(key);
         if (index == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unknown key! " + key);
         }

         changeSet.add(index);
      }

      IndexedLookupTableAssetMap<String, BeaconNPCSpawn> beaconSpawnMap = BeaconNPCSpawn.getAssetMap();
      Map<String, BeaconNPCSpawn> beaconSpawnAssetMap = beaconSpawnMap.getAssetMap();
      int beaconSetupCount = 0;

      for (Entry<String, BeaconNPCSpawn> entry : beaconSpawnAssetMap.entrySet()) {
         BeaconNPCSpawn valuex = entry.getValue();
         if (this.beaconSpawnManager.addSpawnWrapper(new BeaconSpawnWrapper(valuex))) {
            beaconSetupCount++;
         }

         String key = entry.getKey();
         int index = beaconSpawnMap.getIndex(key);
         if (index == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unknown key! " + key);
         }

         changeSet.add(index);
      }

      WorldSpawnManager.trackNPCs(changeSet);
      this.getLogger().at(Level.INFO).log("Successfully set up %s world spawn configurations", worldSetupCount);
      this.getLogger().at(Level.INFO).log("Successfully set up %s beacon spawn configurations", beaconSetupCount);
   }

   private void rebuildBeaconSpawnConfigurations(@Nullable IntSet changeSet) {
      if (changeSet != null && !changeSet.isEmpty()) {
         int setupCount = 0;
         IntIterator var3 = changeSet.iterator();

         while (var3.hasNext()) {
            int configIndex = (Integer)var3.next();
            this.beaconSpawnManager.removeSpawnWrapper(configIndex);
            BeaconNPCSpawn spawn = BeaconNPCSpawn.getAssetMap().getAssetOrDefault(configIndex, null);
            if (spawn != null && this.beaconSpawnManager.addSpawnWrapper(new BeaconSpawnWrapper(spawn))) {
               setupCount++;
            }
         }

         this.getLogger().at(Level.INFO).log("Successfully rebuilt %s beacon spawn configurations", setupCount);
      }
   }

   private void onModelAssetChange(@Nonnull LoadedAssetsEvent<String, ModelAsset, DefaultAssetMap<String, ModelAsset>> event) {
      if (this.spawnMarkerModel != null) {
         Map<String, ModelAsset> modelMap = event.getLoadedAssets();
         ModelAsset modelAsset = modelMap.get(this.spawnMarkerModel.getModelAssetId());
         if (modelAsset != null) {
            this.spawnMarkerModel = Model.createUnitScaleModel(modelAsset);
         }
      }
   }

   private void onLoadAsset(@Nonnull LoadAssetEvent event) {
      HytaleLogger.getLogger().at(Level.INFO).log("Validating Spawn assets phase...");
      long start = System.nanoTime();
      ObjectArrayList<String> errors = new ObjectArrayList();
      validateSpawnsConfigurations("World spawn", WorldNPCSpawn.getAssetMap().getAssetMap(), errors);
      validateSpawnsConfigurations("Beacon spawn", BeaconNPCSpawn.getAssetMap().getAssetMap(), errors);
      validateSpawnMarkers(SpawnMarker.getAssetMap().getAssetMap(), errors);
      ObjectListIterator var5 = errors.iterator();

      while (var5.hasNext()) {
         String error = (String)var5.next();
         this.getLogger().at(Level.SEVERE).log(error);
      }

      if (!errors.isEmpty()) {
         event.failed(Options.getOptionSet().has(Options.VALIDATE_ASSETS), "failed to validate spawning assets");
      }

      HytaleLogger.getLogger()
         .at(Level.INFO)
         .log(
            "Spawn assets validation phase completed! Boot time %s, Took %s",
            FormatUtil.nanosToString(System.nanoTime() - event.getBootStart()),
            FormatUtil.nanosToString(System.nanoTime() - start)
         );
   }

   @Deprecated(forRemoval = true)
   public static class LegacySpawnSuppressorEntityMigration extends EntityModule.MigrationSystem {
      private final ComponentType<EntityStore, PersistentModel> persistentModelComponentType = PersistentModel.getComponentType();
      private final ComponentType<EntityStore, Nameplate> nameplateComponentType = Nameplate.getComponentType();
      private final ComponentType<EntityStore, UUIDComponent> uuidComponentType = UUIDComponent.getComponentType();
      private final ComponentType<EntityStore, UnknownComponents<EntityStore>> unknownComponentsComponentType = EntityStore.REGISTRY.getUnknownComponentType();
      private final Query<EntityStore> query = Query.and(this.unknownComponentsComponentType, Query.not(AllLegacyEntityTypesQuery.INSTANCE));

      @Override
      public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
         UnknownComponents<EntityStore> unknownComponent = holder.getComponent(this.unknownComponentsComponentType);

         assert unknownComponent != null;

         Map<String, BsonDocument> unknownComponents = unknownComponent.getUnknownComponents();
         BsonDocument spawnSuppressor = unknownComponents.remove("SpawnSuppressor");
         if (spawnSuppressor != null) {
            Archetype<EntityStore> archetype = holder.getArchetype();
            if (!archetype.contains(this.persistentModelComponentType)) {
               Model.ModelReference modelReference = Entity.MODEL.get(spawnSuppressor).get();
               holder.addComponent(this.persistentModelComponentType, new PersistentModel(modelReference));
            }

            if (!archetype.contains(this.nameplateComponentType)) {
               holder.addComponent(this.nameplateComponentType, new Nameplate(Entity.DISPLAY_NAME.get(spawnSuppressor).get()));
            }

            if (!archetype.contains(this.uuidComponentType)) {
               holder.addComponent(this.uuidComponentType, new UUIDComponent(Entity.UUID.get(spawnSuppressor).get()));
            }

            holder.ensureComponent(HiddenFromAdventurePlayers.getComponentType());
         }
      }

      @Override
      public void onEntityRemoved(@Nonnull Holder<EntityStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store) {
      }

      @Nonnull
      @Override
      public Query<EntityStore> getQuery() {
         return this.query;
      }

      @Nonnull
      @Override
      public Set<Dependency<EntityStore>> getDependencies() {
         return RootDependency.firstSet();
      }
   }

   public static class NPCSpawningConfig {
      @Nonnull
      public static final BuilderCodec<SpawningPlugin.NPCSpawningConfig> CODEC = BuilderCodec.builder(
            SpawningPlugin.NPCSpawningConfig.class, SpawningPlugin.NPCSpawningConfig::new
         )
         .append(new KeyedCodec<>("SpawnBudgetFactor", Codec.DOUBLE), (o, i) -> o.spawnBudgetFactor = i, o -> o.spawnBudgetFactor)
         .add()
         .append(new KeyedCodec<>("MaxActiveJobs", Codec.INTEGER), (o, i) -> o.maxActiveJobs = i, o -> o.maxActiveJobs)
         .add()
         .append(new KeyedCodec<>("DefaultSpawnMarkerModel", Codec.STRING), (o, i) -> o.defaultMarkerModel = i, o -> o.defaultMarkerModel)
         .add()
         .append(
            new KeyedCodec<>("LocalSpawnControllerJoinDelay", Codec.DOUBLE),
            (o, i) -> o.localSpawnControllerJoinDelay = i,
            o -> o.localSpawnControllerJoinDelay
         )
         .add()
         .build();
      private double spawnBudgetFactor = 1.0;
      private int maxActiveJobs = 20;
      private String defaultMarkerModel = "NPC_Spawn_Marker";
      private double localSpawnControllerJoinDelay = 15.0;
   }
}
