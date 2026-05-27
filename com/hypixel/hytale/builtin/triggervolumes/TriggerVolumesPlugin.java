package com.hypixel.hytale.builtin.triggervolumes;

import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.builtin.triggervolumes.asset.TriggerEffectAsset;
import com.hypixel.hytale.builtin.triggervolumes.command.TriggerVolumeCommand;
import com.hypixel.hytale.builtin.triggervolumes.component.TriggerVolume;
import com.hypixel.hytale.builtin.triggervolumes.component.TriggerVolumeGroup;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerCondition;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEffect;
import com.hypixel.hytale.builtin.triggervolumes.effect.builtin.ControlDoorsEffect;
import com.hypixel.hytale.builtin.triggervolumes.effect.builtin.DamageEntityEffect;
import com.hypixel.hytale.builtin.triggervolumes.effect.builtin.DeleteVolumeEffect;
import com.hypixel.hytale.builtin.triggervolumes.effect.builtin.DisableVolumeEffect;
import com.hypixel.hytale.builtin.triggervolumes.effect.builtin.EnableVolumeEffect;
import com.hypixel.hytale.builtin.triggervolumes.effect.builtin.EntityEffectEffect;
import com.hypixel.hytale.builtin.triggervolumes.effect.builtin.GiveItemEffect;
import com.hypixel.hytale.builtin.triggervolumes.effect.builtin.ItemCondition;
import com.hypixel.hytale.builtin.triggervolumes.effect.builtin.ModifyTagsEffect;
import com.hypixel.hytale.builtin.triggervolumes.effect.builtin.PastePrefabEffect;
import com.hypixel.hytale.builtin.triggervolumes.effect.builtin.PlaceBlockEffect;
import com.hypixel.hytale.builtin.triggervolumes.effect.builtin.PlaySoundEffect;
import com.hypixel.hytale.builtin.triggervolumes.effect.builtin.PlayVfxEffect;
import com.hypixel.hytale.builtin.triggervolumes.effect.builtin.ReplaceBlockTypeEffect;
import com.hypixel.hytale.builtin.triggervolumes.effect.builtin.RunRootInteractionEffect;
import com.hypixel.hytale.builtin.triggervolumes.effect.builtin.SendMessageEffect;
import com.hypixel.hytale.builtin.triggervolumes.effect.builtin.SetGameModeEffect;
import com.hypixel.hytale.builtin.triggervolumes.effect.builtin.SetMusicEffect;
import com.hypixel.hytale.builtin.triggervolumes.effect.builtin.SetVelocityEffect;
import com.hypixel.hytale.builtin.triggervolumes.effect.builtin.SetWeatherEffect;
import com.hypixel.hytale.builtin.triggervolumes.effect.builtin.ShowEventTitleEffect;
import com.hypixel.hytale.builtin.triggervolumes.effect.builtin.TeleportEffect;
import com.hypixel.hytale.builtin.triggervolumes.effect.builtin.TriggerNpcMarkersEffect;
import com.hypixel.hytale.builtin.triggervolumes.effect.builtin.conditions.BlockTypeCondition;
import com.hypixel.hytale.builtin.triggervolumes.effect.builtin.conditions.CooldownCondition;
import com.hypixel.hytale.builtin.triggervolumes.effect.builtin.conditions.GameModeCondition;
import com.hypixel.hytale.builtin.triggervolumes.effect.builtin.conditions.PermissionCondition;
import com.hypixel.hytale.builtin.triggervolumes.effect.builtin.conditions.PlayerCountCondition;
import com.hypixel.hytale.builtin.triggervolumes.effect.builtin.conditions.RandomChanceCondition;
import com.hypixel.hytale.builtin.triggervolumes.effect.builtin.conditions.TagCondition;
import com.hypixel.hytale.builtin.triggervolumes.interaction.DestroyTaggedVolumesInteraction;
import com.hypixel.hytale.builtin.triggervolumes.manager.GroupEntry;
import com.hypixel.hytale.builtin.triggervolumes.manager.TriggerVolumeManager;
import com.hypixel.hytale.builtin.triggervolumes.manager.VolumeEntry;
import com.hypixel.hytale.builtin.triggervolumes.prefab.TriggerVolumeChunkRegenSystem;
import com.hypixel.hytale.builtin.triggervolumes.prefab.TriggerVolumeGroupWorldGenHandler;
import com.hypixel.hytale.builtin.triggervolumes.prefab.TriggerVolumePasteHandler;
import com.hypixel.hytale.builtin.triggervolumes.prefab.TriggerVolumePrefabContributor;
import com.hypixel.hytale.builtin.triggervolumes.prefab.TriggerVolumePrefabPasteRemapSystem;
import com.hypixel.hytale.builtin.triggervolumes.prefab.TriggerVolumeWorldGenHandler;
import com.hypixel.hytale.builtin.triggervolumes.system.TriggerVolumeBlockEventSystems;
import com.hypixel.hytale.builtin.triggervolumes.system.TriggerVolumeTickingSystem;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentRegistry;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.packets.player.UpdateTriggerVolumeDisplay;
import com.hypixel.hytale.server.core.asset.HytaleAssetStore;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.buildertool.config.PrefabListAsset;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.asset.type.musiccontainer.config.MusicContainer;
import com.hypixel.hytale.server.core.asset.type.particle.config.ParticleSystem;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.asset.type.weather.config.Weather;
import com.hypixel.hytale.server.core.event.events.player.AddPlayerToWorldEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.event.events.player.RemovedPlayerFromWorldEvent;
import com.hypixel.hytale.server.core.io.ServerManager;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.prefab.PrefabStore;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.events.RemoveWorldEvent;
import com.hypixel.hytale.server.core.universe.world.events.StartWorldEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.spawning.assets.spawnmarker.config.SpawnMarker;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3d;

public class TriggerVolumesPlugin extends JavaPlugin {
   @Nonnull
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   private static TriggerVolumesPlugin instance;
   private ResourceType<EntityStore, TriggerVolumeManager> managerResourceType;
   private ComponentType<EntityStore, TriggerVolume> triggerVolumeComponentType;
   private ComponentType<EntityStore, TriggerVolumeGroup> triggerVolumeGroupComponentType;
   private final Map<String, AssetSourceProvider> assetSources = new LinkedHashMap<>();
   private final Map<TriggerVolumesPlugin.AssetFieldKey, String> assetFieldMappings = new HashMap<>();
   private final Map<UUID, TriggerVolumesPlugin.PastePrefabPreviewState> pastePrefabPreviewStates = new ConcurrentHashMap<>();
   private final Map<UUID, EnumSet<TriggerVolumeManager.ViewSource>> pendingTransferSources = new ConcurrentHashMap<>();

   @Nonnull
   public static TriggerVolumesPlugin get() {
      return instance;
   }

   public TriggerVolumesPlugin(@Nonnull JavaPluginInit init) {
      super(init);
   }

   public <T extends TriggerEffect> void registerEffectType(@Nonnull String typeId, @Nonnull Class<T> clazz, @Nonnull BuilderCodec<T> codec) {
      Objects.requireNonNull(typeId, "typeId");
      Objects.requireNonNull(clazz, "clazz");
      Objects.requireNonNull(codec, "codec");
      if (TriggerEffect.CODEC.getCodecFor(typeId) != null) {
         throw new IllegalArgumentException("Trigger effect type '" + typeId + "' is already registered");
      } else {
         TriggerEffect.CODEC.register(typeId, clazz, codec);
         LOGGER.at(Level.INFO).log("Registered trigger effect type '%s' (%s)", typeId, clazz.getSimpleName());
      }
   }

   public <T extends TriggerCondition> void registerConditionType(@Nonnull String typeId, @Nonnull Class<T> clazz, @Nonnull BuilderCodec<T> codec) {
      Objects.requireNonNull(typeId, "typeId");
      Objects.requireNonNull(clazz, "clazz");
      Objects.requireNonNull(codec, "codec");
      if (TriggerCondition.CODEC.getCodecFor(typeId) != null) {
         throw new IllegalArgumentException("Trigger condition type '" + typeId + "' is already registered");
      } else {
         TriggerCondition.CODEC.register(typeId, clazz, codec);
         LOGGER.at(Level.INFO).log("Registered trigger condition type '%s' (%s)", typeId, clazz.getSimpleName());
      }
   }

   public void registerAssetSource(@Nonnull String sourceId, @Nonnull AssetSourceProvider provider) {
      Objects.requireNonNull(sourceId, "sourceId");
      Objects.requireNonNull(provider, "provider");
      this.assetSources.put(sourceId, provider);
   }

   public void registerAssetField(@Nonnull String effectTypeId, @Nonnull String fieldKey, @Nonnull String sourceId) {
      Objects.requireNonNull(effectTypeId, "effectTypeId");
      Objects.requireNonNull(fieldKey, "fieldKey");
      Objects.requireNonNull(sourceId, "sourceId");
      this.assetFieldMappings.put(new TriggerVolumesPlugin.AssetFieldKey(effectTypeId, fieldKey), sourceId);
   }

   @Nullable
   public TriggerVolumesPlugin.PastePrefabPreviewState getPastePrefabPreviewState(@Nonnull UUID playerUuid) {
      return this.pastePrefabPreviewStates.get(playerUuid);
   }

   public void setPastePrefabPreviewState(@Nonnull UUID playerUuid, @Nonnull TriggerVolumesPlugin.PastePrefabPreviewState state) {
      this.pastePrefabPreviewStates.put(playerUuid, state);
   }

   public void clearPastePrefabPreviewState(@Nonnull UUID playerUuid) {
      this.pastePrefabPreviewStates.remove(playerUuid);
   }

   @Nullable
   public String getAssetSourceForField(@Nonnull String effectTypeId, @Nonnull String fieldKey) {
      return this.assetFieldMappings.get(new TriggerVolumesPlugin.AssetFieldKey(effectTypeId, fieldKey));
   }

   @Nonnull
   public Collection<String> getAssetIds(@Nonnull String sourceId) {
      AssetSourceProvider provider = this.assetSources.get(sourceId);
      return provider == null ? List.of() : provider.getAssetIds().stream().filter(assetId -> !assetId.startsWith("*")).toList();
   }

   @Nonnull
   public ResourceType<EntityStore, TriggerVolumeManager> getManagerResourceType() {
      return this.managerResourceType;
   }

   @Nonnull
   public ComponentType<EntityStore, TriggerVolume> getTriggerVolumeComponentType() {
      return this.triggerVolumeComponentType;
   }

   @Nonnull
   public ComponentType<EntityStore, TriggerVolumeGroup> getTriggerVolumeGroupComponentType() {
      return this.triggerVolumeGroupComponentType;
   }

   @Override
   protected void setup() {
      instance = this;
      this.registerEffectTypes();
      AssetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(
                        TriggerEffectAsset.class, new DefaultAssetMap()
                     )
                     .setPath("TriggerVolumes/Effects"))
                  .setCodec(TriggerEffectAsset.CODEC))
               .setKeyFunction(TriggerEffectAsset::getId))
            .build()
      );
      ComponentRegistry<EntityStore> entityStoreRegistry = EntityStore.REGISTRY;
      this.managerResourceType = entityStoreRegistry.registerResource(TriggerVolumeManager.class, "TriggerVolumeData", TriggerVolumeManager.CODEC);
      this.triggerVolumeComponentType = entityStoreRegistry.registerComponent(TriggerVolume.class, "TriggerVolume", TriggerVolume.CODEC);
      this.triggerVolumeGroupComponentType = entityStoreRegistry.registerComponent(TriggerVolumeGroup.class, "TriggerVolumeGroup", TriggerVolumeGroup.CODEC);
      EntityModule entityModule = EntityModule.get();
      entityStoreRegistry.registerSystem(
         new TriggerVolumeTickingSystem(this.managerResourceType, entityModule.getPlayerSpatialResourceType(), entityModule.getEntitySpatialResourceType())
      );
      entityStoreRegistry.registerSystem(new TriggerVolumeBlockEventSystems.BlockPlaced(this.managerResourceType));
      entityStoreRegistry.registerSystem(new TriggerVolumeBlockEventSystems.BlockBroken(this.managerResourceType));
      entityStoreRegistry.registerSystem(
         new TriggerVolumePasteHandler(this.managerResourceType, this.triggerVolumeComponentType, this.triggerVolumeGroupComponentType)
      );
      entityStoreRegistry.registerSystem(new TriggerVolumePrefabPasteRemapSystem(this.managerResourceType));
      entityStoreRegistry.registerSystem(new TriggerVolumeWorldGenHandler(this.managerResourceType, this.triggerVolumeComponentType));
      entityStoreRegistry.registerSystem(new TriggerVolumeGroupWorldGenHandler(this.managerResourceType, this.triggerVolumeGroupComponentType));
      this.getChunkStoreRegistry().registerSystem(new TriggerVolumeChunkRegenSystem(this.managerResourceType));
      this.getCodecRegistry(Interaction.CODEC).register("DestroyTaggedVolumes", DestroyTaggedVolumesInteraction.class, DestroyTaggedVolumesInteraction.CODEC);
      this.getCommandRegistry().registerCommand(new TriggerVolumeCommand());
      ServerManager.get().registerSubPacketHandlers(TriggerVolumeToolPacketHandler::new);
      this.getEventRegistry().registerGlobal(StartWorldEvent.class, event -> this.initManagerForWorld(event.getWorld()));
      this.getEventRegistry().registerGlobal(RemoveWorldEvent.class, this::onWorldRemoved);
      this.getEventRegistry().registerGlobal(RemovedPlayerFromWorldEvent.class, this::onPlayerRemovedFromWorld);
      this.getEventRegistry().registerGlobal(AddPlayerToWorldEvent.class, this::onPlayerAddedToWorld);
      this.getEventRegistry().registerGlobal(PlayerDisconnectEvent.class, this::onPlayerDisconnect);
   }

   @Override
   protected void start() {
      BuilderToolsPlugin builderTools = BuilderToolsPlugin.get();
      builderTools.registerPrefabSaveContributor(new TriggerVolumePrefabContributor());
      builderTools.registerClipboardContributor(new TriggerVolumePrefabContributor());
      builderTools.setBuilderToolModeDeactivatedCallback((playerRef, store) -> {
         World world = store.getExternalData().getWorld();
         if (world != null) {
            TriggerVolumeManager mgr = world.getEntityStore().getStore().getResource(this.managerResourceType);
            if (mgr != null) {
               mgr.removeViewer(playerRef.getUuid(), TriggerVolumeManager.ViewSource.SELECTION_TOOL);
               if (!mgr.isViewing(playerRef.getUuid())) {
                  playerRef.getPacketHandler().write(new UpdateTriggerVolumeDisplay());
               } else {
                  mgr.sendVolumeDisplay(playerRef);
               }
            }
         }
      });
   }

   @Override
   protected void shutdown() {
   }

   private void onWorldRemoved(@Nonnull RemoveWorldEvent event) {
      TriggerVolumeManager manager = this.getManager(event.getWorld());
      if (manager != null) {
         for (UUID viewerUuid : manager.getViewerUuids()) {
            this.pendingTransferSources.remove(viewerUuid);
            PlayerRef playerRef = Universe.get().getPlayer(viewerUuid);
            if (playerRef != null) {
               playerRef.getPacketHandler().write(new UpdateTriggerVolumeDisplay());
            }
         }

         manager.clearViewers();
      }
   }

   private void onPlayerRemovedFromWorld(@Nonnull RemovedPlayerFromWorldEvent event) {
      PlayerRef playerRef = event.getHolder().getComponent(PlayerRef.getComponentType());
      if (playerRef != null) {
         TriggerVolumeManager manager = this.getManager(event.getWorld());
         if (manager != null) {
            UUID playerUuid = playerRef.getUuid();
            EnumSet<TriggerVolumeManager.ViewSource> sources = manager.peekViewerSources(playerUuid);
            if (sources != null && !sources.isEmpty()) {
               this.pendingTransferSources.put(playerUuid, sources);
               playerRef.getPacketHandler().write(new UpdateTriggerVolumeDisplay());
            } else {
               this.pendingTransferSources.remove(playerUuid);
            }

            clearViewerState(manager, playerUuid);
         }
      }
   }

   private void onPlayerAddedToWorld(@Nonnull AddPlayerToWorldEvent event) {
      PlayerRef playerRef = event.getHolder().getComponent(PlayerRef.getComponentType());
      if (playerRef != null) {
         EnumSet<TriggerVolumeManager.ViewSource> sources = this.pendingTransferSources.remove(playerRef.getUuid());
         if (sources != null && !sources.isEmpty()) {
            TriggerVolumeManager manager = this.getManager(event.getWorld());
            if (manager != null) {
               for (TriggerVolumeManager.ViewSource source : sources) {
                  manager.addViewer(playerRef.getUuid(), source);
               }

               manager.sendVolumeDisplay(playerRef);
            }
         }
      }
   }

   private void onPlayerDisconnect(@Nonnull PlayerDisconnectEvent event) {
      this.pendingTransferSources.remove(event.getPlayerRef().getUuid());
   }

   private static void clearViewerState(@Nonnull TriggerVolumeManager manager, @Nonnull UUID playerUuid) {
      for (TriggerVolumeManager.ViewSource source : TriggerVolumeManager.ViewSource.values()) {
         manager.removeViewer(playerUuid, source);
      }

      manager.setPlayerSelection(playerUuid, null);
   }

   @Nullable
   private TriggerVolumeManager getManager(@Nonnull World world) {
      return world.getEntityStore().getStore().getResource(this.managerResourceType);
   }

   private void registerEffectTypes() {
      TriggerCondition.CODEC.register("PermissionCondition", PermissionCondition.class, PermissionCondition.CODEC);
      TriggerCondition.CODEC.register("CooldownCondition", CooldownCondition.class, CooldownCondition.CODEC);
      TriggerCondition.CODEC.register("GameModeCondition", GameModeCondition.class, GameModeCondition.CODEC);
      TriggerCondition.CODEC.register("ItemCondition", ItemCondition.class, ItemCondition.CODEC);
      TriggerCondition.CODEC.register("RandomChanceCondition", RandomChanceCondition.class, RandomChanceCondition.CODEC);
      TriggerCondition.CODEC.register("PlayerCountCondition", PlayerCountCondition.class, PlayerCountCondition.CODEC);
      TriggerCondition.CODEC.register("TagCondition", TagCondition.class, TagCondition.CODEC);
      TriggerCondition.CODEC.register("BlockTypeCondition", BlockTypeCondition.class, BlockTypeCondition.CODEC);
      TriggerEffect.CODEC.register("Teleport", TeleportEffect.class, TeleportEffect.CODEC);
      TriggerEffect.CODEC.register("SendMessage", SendMessageEffect.class, SendMessageEffect.CODEC);
      TriggerEffect.CODEC.register("PlaySound", PlaySoundEffect.class, PlaySoundEffect.CODEC);
      TriggerEffect.CODEC.register("SetVelocity", SetVelocityEffect.class, SetVelocityEffect.CODEC);
      TriggerEffect.CODEC.register("EntityEffect", EntityEffectEffect.class, EntityEffectEffect.CODEC);
      TriggerEffect.CODEC.register("TriggerNpcMarkers", TriggerNpcMarkersEffect.class, TriggerNpcMarkersEffect.CODEC);
      TriggerEffect.CODEC.register("PlayVfx", PlayVfxEffect.class, PlayVfxEffect.CODEC);
      TriggerEffect.CODEC.register("SetWeather", SetWeatherEffect.class, SetWeatherEffect.CODEC);
      TriggerEffect.CODEC.register("ShowEventTitle", ShowEventTitleEffect.class, ShowEventTitleEffect.CODEC);
      TriggerEffect.CODEC.register("PastePrefab", PastePrefabEffect.class, PastePrefabEffect.CODEC);
      TriggerEffect.CODEC.register("ControlDoors", ControlDoorsEffect.class, ControlDoorsEffect.CODEC);
      TriggerEffect.CODEC.register("EnableVolume", EnableVolumeEffect.class, EnableVolumeEffect.CODEC);
      TriggerEffect.CODEC.register("DisableVolume", DisableVolumeEffect.class, DisableVolumeEffect.CODEC);
      TriggerEffect.CODEC.register("DeleteVolume", DeleteVolumeEffect.class, DeleteVolumeEffect.CODEC);
      TriggerEffect.CODEC.register("DamageEntity", DamageEntityEffect.class, DamageEntityEffect.CODEC);
      TriggerEffect.CODEC.register("RunRootInteraction", RunRootInteractionEffect.class, RunRootInteractionEffect.CODEC);
      TriggerEffect.CODEC.register("SetMusic", SetMusicEffect.class, SetMusicEffect.CODEC);
      TriggerEffect.CODEC.register("GiveItem", GiveItemEffect.class, GiveItemEffect.CODEC);
      TriggerEffect.CODEC.register("SetGameMode", SetGameModeEffect.class, SetGameModeEffect.CODEC);
      TriggerEffect.CODEC.register("ModifyTags", ModifyTagsEffect.class, ModifyTagsEffect.CODEC);
      TriggerEffect.CODEC.register("PlaceBlock", PlaceBlockEffect.class, PlaceBlockEffect.CODEC);
      TriggerEffect.CODEC.register("ReplaceBlockType", ReplaceBlockTypeEffect.class, ReplaceBlockTypeEffect.CODEC);
      this.registerAssetSource("BlockType", () -> BlockType.getAssetMap().getAssetMap().keySet());
      this.registerAssetSource("EntityEffect", () -> EntityEffect.getAssetMap().getAssetMap().keySet());
      this.registerAssetSource("Item", () -> Item.getAssetMap().getAssetMap().keySet());
      this.registerAssetSource("SoundEvent", () -> SoundEvent.getAssetMap().getAssetMap().keySet());
      this.registerAssetSource("EffectAsset", () -> {
         AssetStore<String, TriggerEffectAsset, DefaultAssetMap<String, TriggerEffectAsset>> store = AssetRegistry.getAssetStore(TriggerEffectAsset.class);
         return store != null ? ((DefaultAssetMap)store.getAssetMap()).getAssetMap().keySet() : Set.of();
      });
      this.registerAssetSource("Weather", () -> Weather.getAssetMap().getAssetMap().keySet());
      this.registerAssetSource("PrefabList", () -> PrefabListAsset.getAssetMap().getAssetMap().keySet());
      this.registerAssetSource("Prefab", TriggerVolumesPlugin::collectPrefabRelPaths);
      this.registerAssetSource("ParticleSystem", () -> ParticleSystem.getAssetMap().getAssetMap().keySet());
      this.registerAssetSource("ManualSpawnMarker", TriggerVolumesPlugin::collectManualSpawnMarkerIds);
      this.registerAssetSource("RootInteraction", () -> RootInteraction.getAssetMap().getAssetMap().keySet());
      this.registerAssetSource("MusicContainer", () -> MusicContainer.getAssetMap().getAssetMap().keySet());
      this.registerAssetField("EntityEffect", "Effect", "EntityEffect");
      this.registerAssetField("PlaySound", "SoundEvent", "SoundEvent");
      this.registerAssetField("SetWeather", "Weather", "Weather");
      this.registerAssetField("PastePrefab", "PrefabList", "PrefabList");
      this.registerAssetField("PastePrefab", "Prefab", "Prefab");
      this.registerAssetField("PlayVfx", "ParticleSystem", "ParticleSystem");
      this.registerAssetField("TriggerNpcMarkers", "MarkerType", "ManualSpawnMarker");
      this.registerAssetField("RunRootInteraction", "RootInteraction", "RootInteraction");
      this.registerAssetField("SetMusic", "MusicContainer", "MusicContainer");
      this.registerAssetField("GiveItem", "Item", "Item");
      this.registerAssetField("PlaceBlock", "BlockType", "BlockType");
      this.registerAssetField("ReplaceBlockType", "FromBlockTypes", "BlockType");
      this.registerAssetField("ReplaceBlockType", "ToBlockType", "BlockType");
      this.registerAssetField("ItemCondition", "Item", "Item");
      this.registerAssetField("BlockTypeCondition", "BlockType", "BlockType");
   }

   @Nonnull
   private static Collection<String> collectPrefabRelPaths() {
      TreeSet<String> ids = new TreeSet<>();
      PrefabStore store = PrefabStore.get();

      for (PrefabStore.AssetPackPrefabPath entry : store.getAllAssetPrefabPaths()) {
         Path root = entry.prefabsPath();

         try (Stream<Path> stream = Files.walk(root)) {
            stream.filter(PrefabStore.PREFAB_FILTER).forEach(prefabPath -> {
               Path relativePath = root.relativize(prefabPath);
               String prefabId = relativePath.toString().replace('\\', '/');
               if (prefabId.endsWith(".prefab.json")) {
                  prefabId = prefabId.substring(0, prefabId.length() - ".prefab.json".length());
               }

               ids.add(prefabId);
            });
         } catch (Exception var10) {
            ((HytaleLogger.Api)LOGGER.at(Level.WARNING).withCause(var10)).log("Failed to enumerate prefabs under %s", root);
         }
      }

      return ids;
   }

   @Nonnull
   private static Collection<String> collectManualSpawnMarkerIds() {
      ArrayList<String> ids = new ArrayList<>();

      for (Entry<String, SpawnMarker> entry : SpawnMarker.getAssetMap().getAssetMap().entrySet()) {
         if (entry.getValue().isManualTrigger()) {
            ids.add(entry.getKey());
         }
      }

      Collections.sort(ids);
      return ids;
   }

   private void initManagerForWorld(@Nonnull World world) {
      String worldName = world.getName().toLowerCase(Locale.ROOT);
      Store<EntityStore> store = world.getEntityStore().getStore();
      TriggerVolumeManager manager = store.getResource(this.managerResourceType);
      if (manager != null) {
         manager.setWorld(world);
         AssetStore<String, TriggerEffectAsset, DefaultAssetMap<String, TriggerEffectAsset>> effectAssetStore = AssetRegistry.getAssetStore(
            TriggerEffectAsset.class
         );

         for (VolumeEntry vol : manager.getVolumesMap().values()) {
            vol.setWorldName(worldName);
            if (vol.getEffectAssetRef() != null && effectAssetStore != null) {
               TriggerEffectAsset effectAsset = (TriggerEffectAsset)((DefaultAssetMap)effectAssetStore.getAssetMap()).getAsset(vol.getEffectAssetRef());
               if (effectAsset == null) {
                  LOGGER.at(Level.WARNING).log("Volume '%s' references missing effect asset '%s'", vol.getId(), vol.getEffectAssetRef());
               } else {
                  vol.getConditions().clear();
                  vol.getConditions().addAll(Arrays.asList(effectAsset.getConditions()));
                  vol.getEffects().clear();
                  vol.getEffects().addAll(Arrays.asList(effectAsset.getEffects()));
                  vol.getRejectionEffects().clear();
                  vol.getRejectionEffects().addAll(Arrays.asList(effectAsset.getRejectionEffects()));
               }
            }
         }

         for (GroupEntry group : manager.getGroupsMap().values()) {
            group.setWorldName(worldName);
         }

         LOGGER.at(Level.INFO)
            .log("Loaded %d trigger volumes and %d groups for world '%s'", manager.getVolumesMap().size(), manager.getGroupsMap().size(), worldName);
      }
   }

   private record AssetFieldKey(@Nonnull String typeId, @Nonnull String fieldKey) {
   }

   public static final class PastePrefabPreviewState {
      @Nonnull
      private final String worldName;
      @Nullable
      private final String selectedId;
      private final boolean selectedIsGroup;
      @Nonnull
      private final String effectListKind;
      private final int effectIndex;
      @Nullable
      private Vector3d lastSentPosition;

      public PastePrefabPreviewState(
         @Nonnull String worldName,
         @Nullable String selectedId,
         boolean selectedIsGroup,
         @Nonnull String effectListKind,
         int effectIndex,
         @Nullable Vector3d lastSentPosition
      ) {
         this.worldName = worldName;
         this.selectedId = selectedId;
         this.selectedIsGroup = selectedIsGroup;
         this.effectListKind = effectListKind;
         this.effectIndex = effectIndex;
         this.lastSentPosition = lastSentPosition != null ? new Vector3d(lastSentPosition) : null;
      }

      public boolean matches(@Nonnull String worldName, @Nullable String selectedId, boolean selectedIsGroup, @Nonnull String effectListKind, int effectIndex) {
         return this.worldName.equals(worldName)
            && Objects.equals(this.selectedId, selectedId)
            && this.selectedIsGroup == selectedIsGroup
            && this.effectListKind.equals(effectListKind)
            && this.effectIndex == effectIndex;
      }

      @Nonnull
      public String worldName() {
         return this.worldName;
      }

      @Nullable
      public String selectedId() {
         return this.selectedId;
      }

      public boolean selectedIsGroup() {
         return this.selectedIsGroup;
      }

      @Nonnull
      public String effectListKind() {
         return this.effectListKind;
      }

      public int effectIndex() {
         return this.effectIndex;
      }

      @Nullable
      public Vector3d lastSentPosition() {
         return this.lastSentPosition != null ? new Vector3d(this.lastSentPosition) : null;
      }

      public void setLastSentPosition(@Nonnull Vector3d lastSentPosition) {
         this.lastSentPosition = new Vector3d(lastSentPosition);
      }
   }
}
