package com.hypixel.hytale.builtin.asseteditor;

import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.AssetUpdateQuery;
import com.hypixel.hytale.assetstore.event.LoadedAssetsEvent;
import com.hypixel.hytale.assetstore.map.BlockTypeAssetMap;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.builtin.asseteditor.assettypehandler.AssetStoreTypeHandler;
import com.hypixel.hytale.builtin.asseteditor.assettypehandler.AssetTypeHandler;
import com.hypixel.hytale.builtin.asseteditor.event.AssetEditorActivateButtonEvent;
import com.hypixel.hytale.builtin.asseteditor.event.AssetEditorAssetCreatedEvent;
import com.hypixel.hytale.builtin.asseteditor.event.AssetEditorClientDisconnectEvent;
import com.hypixel.hytale.builtin.asseteditor.event.AssetEditorFetchAutoCompleteDataEvent;
import com.hypixel.hytale.builtin.asseteditor.event.AssetEditorRequestDataSetEvent;
import com.hypixel.hytale.builtin.asseteditor.event.AssetEditorSelectAssetEvent;
import com.hypixel.hytale.builtin.asseteditor.event.AssetEditorUpdateWeatherPreviewLockEvent;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.InstantData;
import com.hypixel.hytale.protocol.Model;
import com.hypixel.hytale.protocol.Vector2f;
import com.hypixel.hytale.protocol.Vector3f;
import com.hypixel.hytale.protocol.packets.asseteditor.AssetEditorPopupNotificationType;
import com.hypixel.hytale.protocol.packets.asseteditor.AssetEditorPreviewCameraSettings;
import com.hypixel.hytale.protocol.packets.asseteditor.AssetEditorUpdateModelPreview;
import com.hypixel.hytale.protocol.packets.asseteditor.AssetEditorUpdateSecondsPerGameDay;
import com.hypixel.hytale.protocol.packets.world.ClearEditorTimeOverride;
import com.hypixel.hytale.protocol.packets.world.UpdateEditorTimeOverride;
import com.hypixel.hytale.protocol.packets.world.UpdateEditorWeatherOverride;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.environment.config.Environment;
import com.hypixel.hytale.server.core.asset.type.item.config.AssetIconProperties;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemArmor;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.asset.type.weather.config.Weather;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSkinComponent;
import com.hypixel.hytale.server.core.modules.i18n.I18nModule;
import com.hypixel.hytale.server.core.modules.item.ItemModule;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.PlayerUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Deprecated
public class AssetSpecificFunctionality {
   private static final Message NO_GAME_CLIENT_MESSAGE = Message.translation("server.assetEditor.messages.noGameClient");
   private static final ClearEditorTimeOverride CLEAR_EDITOR_TIME_OVERRIDE_PACKET = new ClearEditorTimeOverride();
   private static final UpdateEditorWeatherOverride CLEAR_WEATHER_OVERRIDE_PACKET = new UpdateEditorWeatherOverride(0);
   private static final String MODEL_ASSET_ID = ModelAsset.class.getSimpleName();
   private static final String ITEM_ASSET_ID = Item.class.getSimpleName();
   private static final String WEATHER_ASSET_ID = Weather.class.getSimpleName();
   private static final String ENVIRONMENT_ASSET_ID = Environment.class.getSimpleName();
   private static final Map<UUID, AssetSpecificFunctionality.PlayerPreviewData> activeWeatherPreviewMapping = new ConcurrentHashMap<>();
   private static final AssetEditorPreviewCameraSettings DEFAULT_PREVIEW_CAMERA_SETTINGS = new AssetEditorPreviewCameraSettings(
      0.25F, new Vector3f(0.0F, 75.0F, 0.0F), new Vector3f(0.0F, (float)Math.toRadians(45.0), 0.0F)
   );

   public static void setup() {
      getEventRegistry().register(LoadedAssetsEvent.class, ModelAsset.class, AssetSpecificFunctionality::onModelAssetLoaded);
      getEventRegistry().register(LoadedAssetsEvent.class, Item.class, AssetSpecificFunctionality::onItemAssetLoaded);
      getEventRegistry().register(AssetEditorActivateButtonEvent.class, "EquipItem", AssetSpecificFunctionality::onEquipItem);
      getEventRegistry().register(AssetEditorActivateButtonEvent.class, "UseModel", AssetSpecificFunctionality::onUseModel);
      getEventRegistry().register(AssetEditorActivateButtonEvent.class, "ResetModel", AssetSpecificFunctionality::onResetModel);
      getEventRegistry().register(AssetEditorUpdateWeatherPreviewLockEvent.class, AssetSpecificFunctionality::onUpdateWeatherPreviewLockEvent);
      getEventRegistry().register(AssetEditorAssetCreatedEvent.class, ITEM_ASSET_ID, AssetSpecificFunctionality::onItemAssetCreated);
      getEventRegistry().register(AssetEditorAssetCreatedEvent.class, MODEL_ASSET_ID, AssetSpecificFunctionality::onModelAssetCreated);
      getEventRegistry().register(AssetEditorFetchAutoCompleteDataEvent.class, "BlockGroups", AssetSpecificFunctionality::onRequestBlockGroupsDataSet);
      getEventRegistry().register(AssetEditorFetchAutoCompleteDataEvent.class, "LocalizationKeys", AssetSpecificFunctionality::onRequestLocalizationKeyDataSet);
      getEventRegistry().register(AssetEditorRequestDataSetEvent.class, "ItemCategories", AssetSpecificFunctionality::onRequestItemCategoriesDataSet);
      getEventRegistry().registerGlobal(AssetEditorSelectAssetEvent.class, AssetSpecificFunctionality::onSelectAsset);
      getEventRegistry().registerGlobal(AssetEditorClientDisconnectEvent.class, AssetSpecificFunctionality::onClientDisconnected);
   }

   @Nullable
   private static PlayerRef tryGetPlayer(@Nonnull EditorClient editorClient) {
      PlayerRef playerRef = editorClient.tryGetPlayer();
      if (playerRef == null) {
         editorClient.sendPopupNotification(AssetEditorPopupNotificationType.Warning, NO_GAME_CLIENT_MESSAGE);
         return null;
      } else {
         return playerRef;
      }
   }

   private static void onModelAssetLoaded(@Nonnull LoadedAssetsEvent<String, ModelAsset, ?> event) {
      if (!event.isInitial()) {
         Map<EditorClient, AssetPath> clientOpenAssetPathMapping = AssetEditorPlugin.get().getClientOpenAssetPathMapping();
         if (!clientOpenAssetPathMapping.isEmpty()) {
            for (ModelAsset modelAsset : event.getLoadedAssets().values()) {
               for (Entry<EditorClient, AssetPath> editor : clientOpenAssetPathMapping.entrySet()) {
                  Path path = editor.getValue().path();
                  if (!path.toString().isEmpty()) {
                     AssetTypeHandler assetType = AssetEditorPlugin.get().getAssetTypeRegistry().getAssetTypeHandlerForPath(path);
                     if (assetType instanceof AssetStoreTypeHandler
                        && ((AssetStoreTypeHandler)assetType).getAssetStore().getAssetClass().equals(ModelAsset.class)) {
                        String id = ModelAsset.getAssetStore().decodeFilePathKey(path);
                        if (modelAsset.getId().equals(id)) {
                           Model modelPacket = com.hypixel.hytale.server.core.asset.type.model.config.Model.createUnitScaleModel(modelAsset).toPacket();
                           AssetEditorUpdateModelPreview packet = new AssetEditorUpdateModelPreview(
                              editor.getValue().toPacket(), modelPacket, null, DEFAULT_PREVIEW_CAMERA_SETTINGS
                           );
                           editor.getKey().getPacketHandler().write(packet);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static void onItemAssetLoaded(@Nonnull LoadedAssetsEvent<String, Item, ?> event) {
      if (!event.isInitial()) {
         Map<EditorClient, AssetPath> clientOpenAssetPathMapping = AssetEditorPlugin.get().getClientOpenAssetPathMapping();
         if (!clientOpenAssetPathMapping.isEmpty()) {
            AssetUpdateQuery.RebuildCache rebuildCache = event.getQuery().getRebuildCache();
            if (rebuildCache.isBlockTextures() || rebuildCache.isModelTextures() || rebuildCache.isItemIcons() || rebuildCache.isModels()) {
               for (Item item : event.getLoadedAssets().values()) {
                  for (Entry<EditorClient, AssetPath> editor : clientOpenAssetPathMapping.entrySet()) {
                     Path path = editor.getValue().path();
                     if (!path.toString().isEmpty()) {
                        AssetTypeHandler assetType = AssetEditorPlugin.get().getAssetTypeRegistry().getAssetTypeHandlerForPath(path);
                        if (assetType instanceof AssetStoreTypeHandler && ((AssetStoreTypeHandler)assetType).getAssetStore().getAssetClass().equals(Item.class)
                           )
                         {
                           String id = Item.getAssetStore().decodeFilePathKey(path);
                           if (item.getId().equals(id)) {
                              AssetEditorUpdateModelPreview packet = getModelPreviewPacketForItem(editor.getValue(), item);
                              if (packet != null) {
                                 editor.getKey().getPacketHandler().write(packet);
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static void onItemAssetCreated(@Nonnull AssetEditorAssetCreatedEvent event) {
      if ("EquipItem".equals(event.getButtonId())) {
         equipItem(event.getAssetPath(), event.getEditorClient());
      }
   }

   private static void onModelAssetCreated(@Nonnull AssetEditorAssetCreatedEvent event) {
      if ("UseModel".equals(event.getButtonId())) {
         useModel(event.getAssetPath(), event.getEditorClient());
      }
   }

   private static void onEquipItem(@Nonnull AssetEditorActivateButtonEvent event) {
      AssetPath currentAssetPath = AssetEditorPlugin.get().getOpenAssetPath(event.getEditorClient());
      if (currentAssetPath != null && !currentAssetPath.path().toString().isEmpty()) {
         equipItem(currentAssetPath.path(), event.getEditorClient());
      }
   }

   private static void onUseModel(@Nonnull AssetEditorActivateButtonEvent event) {
      AssetPath currentAssetPath = AssetEditorPlugin.get().getOpenAssetPath(event.getEditorClient());
      if (currentAssetPath != null && !currentAssetPath.path().toString().isEmpty()) {
         useModel(currentAssetPath.path(), event.getEditorClient());
      }
   }

   private static void onUpdateWeatherPreviewLockEvent(@Nonnull AssetEditorUpdateWeatherPreviewLockEvent event) {
      AssetSpecificFunctionality.PlayerPreviewData currentPreviewSettings = activeWeatherPreviewMapping.computeIfAbsent(
         event.getEditorClient().getUuid(), k -> new AssetSpecificFunctionality.PlayerPreviewData()
      );
      currentPreviewSettings.keepPreview = event.isLocked();
   }

   private static void onResetModel(@Nonnull AssetEditorActivateButtonEvent event) {
      EditorClient editorClient = event.getEditorClient();
      PlayerRef playerRef = tryGetPlayer(editorClient);
      if (playerRef != null) {
         Ref<EntityStore> ref = playerRef.getReference();
         if (ref != null && ref.isValid()) {
            Store<EntityStore> store = ref.getStore();
            World world = store.getExternalData().getWorld();
            world.execute(() -> {
               if (!store.getArchetype(ref).contains(PlayerSkinComponent.getComponentType())) {
                  Message message = Message.translation("server.assetEditor.messages.model.noAuthSkinForPlayer").param("model", "Player");
                  editorClient.sendPopupNotification(AssetEditorPopupNotificationType.Error, message);
               } else {
                  PlayerUtil.resetPlayerModel(ref, store);
               }
            });
         }
      }
   }

   private static void equipItem(@Nonnull Path assetPath, @Nonnull EditorClient editorClient) {
      PlayerRef playerRef = tryGetPlayer(editorClient);
      if (playerRef != null) {
         Player player = playerRef.getComponent(Player.getComponentType());
         String key = Item.getAssetStore().decodeFilePathKey(assetPath);
         Item item = Item.getAssetMap().getAsset(key);
         if (item == null) {
            editorClient.sendPopupNotification(
               AssetEditorPopupNotificationType.Error, Message.translation("server.assetEditor.messages.unknownItem").param("id", key.toString())
            );
         } else {
            ItemArmor itemArmor = item.getArmor();
            if (itemArmor != null) {
               player.getInventory().getArmor().setItemStackForSlot((short)itemArmor.getArmorSlot().ordinal(), new ItemStack(key));
            } else {
               player.getInventory().getCombinedHotbarFirst().addItemStack(new ItemStack(key));
            }
         }
      }
   }

   private static void useModel(@Nonnull Path assetPath, @Nonnull EditorClient editorClient) {
      PlayerRef playerRef = tryGetPlayer(editorClient);
      if (playerRef != null) {
         Ref<EntityStore> ref = playerRef.getReference();
         if (ref != null && ref.isValid()) {
            Store<EntityStore> store = ref.getStore();
            World world = store.getExternalData().getWorld();
            world.execute(
               () -> {
                  String key = ModelAsset.getAssetStore().decodeFilePathKey(assetPath);
                  ModelAsset modelAsset = ModelAsset.getAssetMap().getAsset(key);
                  if (modelAsset == null) {
                     Message unknownModelMessage = Message.translation("server.assetEditor.messages.unknownModel").param("id", key);
                     editorClient.sendPopupNotification(AssetEditorPopupNotificationType.Error, unknownModelMessage);
                  } else {
                     com.hypixel.hytale.server.core.asset.type.model.config.Model model = com.hypixel.hytale.server.core.asset.type.model.config.Model.createRandomScaleModel(
                        modelAsset
                     );
                     store.putComponent(ref, ModelComponent.getComponentType(), new ModelComponent(model));
                  }
               }
            );
         }
      }
   }

   private static void onRequestLocalizationKeyDataSet(@Nonnull AssetEditorFetchAutoCompleteDataEvent event) {
      ObjectArrayList<String> results = new ObjectArrayList();
      String query = event.getQuery().toLowerCase();

      for (String key : I18nModule.get().getMessages("en-US").keySet()) {
         if (key.toLowerCase().startsWith(query)) {
            results.add(key);
         }

         if (results.size() >= 25) {
            break;
         }
      }

      event.setResults((String[])results.toArray(String[]::new));
   }

   private static void onRequestBlockGroupsDataSet(@Nonnull AssetEditorFetchAutoCompleteDataEvent event) {
      ObjectArrayList<String> results = new ObjectArrayList();
      String query = event.getQuery().toLowerCase();

      for (String group : BlockType.getAssetMap().getGroups()) {
         if (group != null && !group.trim().isEmpty() && (query.isEmpty() || group.toLowerCase().contains(query))) {
            results.add(group);
         }
      }

      event.setResults((String[])results.toArray(String[]::new));
   }

   private static void onRequestItemCategoriesDataSet(@Nonnull AssetEditorRequestDataSetEvent event) {
      ItemModule itemModule = ItemModule.get();
      if (itemModule.isDisabled()) {
         HytaleLogger.getLogger().at(Level.WARNING).log("Received ItemCategories dataset request but ItemModule is disabled!");
      } else {
         event.setResults(itemModule.getFlatItemCategoryList().toArray(String[]::new));
      }
   }

   private static void onClientDisconnected(@Nonnull AssetEditorClientDisconnectEvent event) {
      AssetEditorPlugin plugin = AssetEditorPlugin.get();
      EditorClient editorClient = event.getEditorClient();
      PlayerRef player = editorClient.tryGetPlayer();
      UUID uuid = editorClient.getUuid();
      Set<EditorClient> editorClients = plugin.getEditorClients(uuid);
      if (editorClients != null && editorClients.size() != 1) {
         AssetPath openAssetPath = plugin.getOpenAssetPath(editorClient);
         if (openAssetPath != null && !openAssetPath.equals(AssetPath.EMPTY_PATH)) {
            AssetTypeHandler assetType = plugin.getAssetTypeRegistry().getAssetTypeHandlerForPath(openAssetPath.path());
            if (assetType != null && Weather.class.getSimpleName().equals(assetType.getConfig().id)) {
               activeWeatherPreviewMapping.remove(uuid);
               if (player != null) {
                  player.getPacketHandler().write(new UpdateEditorWeatherOverride(0));
               }
            }
         }
      } else {
         if (player != null) {
            player.getPacketHandler().write(CLEAR_EDITOR_TIME_OVERRIDE_PACKET);
            player.getPacketHandler().write(CLEAR_WEATHER_OVERRIDE_PACKET);
         }

         activeWeatherPreviewMapping.remove(uuid);
      }
   }

   static void resetTimeSettings(@Nonnull EditorClient editorClient, @Nonnull PlayerRef playerRef) {
      Ref<EntityStore> ref = playerRef.getReference();
      if (ref != null && ref.isValid()) {
         Store<EntityStore> store = ref.getStore();
         World world = store.getExternalData().getWorld();
         Player playerComponent = playerRef.getComponent(Player.getComponentType());

         assert playerComponent != null;

         WorldTimeResource worldTimeResource = store.getResource(WorldTimeResource.getResourceType());
         PacketHandler packetHandler = editorClient.getPacketHandler();
         AssetEditorUpdateSecondsPerGameDay settingsPacket = new AssetEditorUpdateSecondsPerGameDay(
            world.getDaytimeDurationSeconds(), world.getNighttimeDurationSeconds()
         );
         packetHandler.write(settingsPacket);
         Instant gameTime = worldTimeResource.getGameTime();
         UpdateEditorTimeOverride packet = new UpdateEditorTimeOverride(
            new InstantData(gameTime.getEpochSecond(), gameTime.getNano()), world.getWorldConfig().isGameTimePaused()
         );
         packetHandler.write(packet);
         playerRef.getPacketHandler().write(CLEAR_EDITOR_TIME_OVERRIDE_PACKET);
      }
   }

   static void handleWeatherOrEnvironmentUnselected(@Nonnull EditorClient editorClient, @Nonnull Path assetPath, boolean wasWeather) {
      PlayerRef player = editorClient.tryGetPlayer();
      if (player != null) {
         AssetSpecificFunctionality.PlayerPreviewData currentPreviewSettings = activeWeatherPreviewMapping.computeIfAbsent(
            editorClient.getUuid(), k -> new AssetSpecificFunctionality.PlayerPreviewData()
         );
         if (!currentPreviewSettings.keepPreview) {
            resetTimeSettings(editorClient, player);
            if (wasWeather) {
               if (!assetPath.equals(currentPreviewSettings.weatherAssetPath)) {
                  return;
               }

               currentPreviewSettings.weatherAssetPath = null;
               player.getPacketHandler().write(CLEAR_WEATHER_OVERRIDE_PACKET);
            }
         }
      }
   }

   static void handleWeatherOrEnvironmentSelected(@Nonnull EditorClient editorClient, @Nonnull Path assetPath, boolean isWeather) {
      PlayerRef player = editorClient.tryGetPlayer();
      if (player != null) {
         AssetSpecificFunctionality.PlayerPreviewData currentPreviewSettings = activeWeatherPreviewMapping.computeIfAbsent(
            editorClient.getUuid(), k -> new AssetSpecificFunctionality.PlayerPreviewData()
         );
         if (!currentPreviewSettings.keepPreview) {
            resetTimeSettings(editorClient, player);
         }

         if (isWeather) {
            AssetStore<String, Weather, IndexedLookupTableAssetMap<String, Weather>> assetStore = Weather.getAssetStore();
            String key = assetStore.decodeFilePathKey(assetPath);
            int weatherIndex = ((IndexedLookupTableAssetMap)assetStore.getAssetMap()).getIndex(key);
            currentPreviewSettings.weatherAssetPath = assetPath;
            player.getPacketHandler().write(new UpdateEditorWeatherOverride(weatherIndex));
         }
      }
   }

   private static void onSelectAsset(@Nonnull AssetEditorSelectAssetEvent event) {
      String assetType = event.getAssetType();
      if (MODEL_ASSET_ID.equals(assetType)) {
         String key = ModelAsset.getAssetStore().decodeFilePathKey(event.getAssetFilePath().path());
         ModelAsset modelAsset = ModelAsset.getAssetMap().getAsset(key);
         if (modelAsset != null) {
            Model modelPacket = com.hypixel.hytale.server.core.asset.type.model.config.Model.createUnitScaleModel(modelAsset).toPacket();
            event.getEditorClient()
               .getPacketHandler()
               .write(new AssetEditorUpdateModelPreview(event.getAssetFilePath().toPacket(), modelPacket, null, DEFAULT_PREVIEW_CAMERA_SETTINGS));
         }
      }

      if (ITEM_ASSET_ID.equals(assetType)) {
         AssetPath assetPath = event.getAssetFilePath();
         String key = Item.getAssetStore().decodeFilePathKey(assetPath.path());
         Item item = Item.getAssetMap().getAsset(key);
         if (item != null) {
            AssetEditorUpdateModelPreview packet = getModelPreviewPacketForItem(assetPath, item);
            if (packet != null) {
               event.getEditorClient().getPacketHandler().write(packet);
            }
         }
      }

      String previousAssetType = event.getPreviousAssetType();
      boolean wasWeather = WEATHER_ASSET_ID.equals(previousAssetType);
      if (wasWeather || ENVIRONMENT_ASSET_ID.equals(previousAssetType)) {
         handleWeatherOrEnvironmentUnselected(event.getEditorClient(), event.getPreviousAssetFilePath().path(), wasWeather);
      }

      boolean isWeather = WEATHER_ASSET_ID.equals(assetType);
      if (isWeather || ENVIRONMENT_ASSET_ID.equals(assetType)) {
         handleWeatherOrEnvironmentSelected(event.getEditorClient(), event.getAssetFilePath().path(), isWeather);
      }
   }

   public static AssetEditorUpdateModelPreview getModelPreviewPacketForItem(@Nonnull AssetPath assetPath, @Nullable Item item) {
      if (item == null) {
         return null;
      } else {
         AssetIconProperties iconProperties = item.getIconProperties();
         AssetIconProperties defaultIconProperties = getDefaultItemIconProperties(item);
         if (iconProperties == null) {
            iconProperties = defaultIconProperties;
         }

         AssetEditorPreviewCameraSettings camera = new AssetEditorPreviewCameraSettings();
         camera.modelScale = iconProperties.getScale() * item.getScale();
         Vector2f translation = iconProperties.getTranslation() != null ? iconProperties.getTranslation() : defaultIconProperties.getTranslation();
         camera.cameraPosition = new Vector3f(-translation.x, -translation.y, 0.0F);
         Vector3f rotation = iconProperties.getRotation() != null ? iconProperties.getRotation() : defaultIconProperties.getRotation();
         camera.cameraOrientation = new Vector3f(
            (float)(-Math.toRadians(rotation.x)), (float)(-Math.toRadians(rotation.y)), (float)(-Math.toRadians(rotation.z))
         );
         if (item.getBlockId() != null) {
            BlockType blockType = (BlockType)((BlockTypeAssetMap)BlockType.getAssetStore().getAssetMap()).getAsset(item.getBlockId());
            if (blockType != null) {
               camera.modelScale = camera.modelScale * blockType.getCustomModelScale();
               return new AssetEditorUpdateModelPreview(assetPath.toPacket(), null, blockType.toPacket(), camera);
            }
         }

         Model modelPacket = convertToModelPacket(item);
         return new AssetEditorUpdateModelPreview(assetPath.toPacket(), modelPacket, null, camera);
      }
   }

   @Nonnull
   public static AssetIconProperties getDefaultItemIconProperties(@Nonnull Item item) {
      if (item.getWeapon() != null) {
         return new AssetIconProperties(0.37F, new Vector2f(-24.6F, -24.6F), new Vector3f(45.0F, 90.0F, 0.0F));
      } else if (item.getTool() != null) {
         return new AssetIconProperties(0.5F, new Vector2f(-17.4F, -12.0F), new Vector3f(45.0F, 270.0F, 0.0F));
      } else if (item.getArmor() != null) {
         return switch (item.getArmor().getArmorSlot()) {
            case Chest -> new AssetIconProperties(0.5F, new Vector2f(0.0F, -5.0F), new Vector3f(22.5F, 45.0F, 22.5F));
            case Head -> new AssetIconProperties(0.5F, new Vector2f(0.0F, -3.0F), new Vector3f(22.5F, 45.0F, 22.5F));
            case Legs -> new AssetIconProperties(0.5F, new Vector2f(0.0F, -25.8F), new Vector3f(22.5F, 45.0F, 22.5F));
            case Hands -> new AssetIconProperties(0.92F, new Vector2f(0.0F, -10.8F), new Vector3f(22.5F, 45.0F, 22.5F));
         };
      } else {
         return new AssetIconProperties(0.58823F, new Vector2f(0.0F, -13.5F), new Vector3f(22.5F, 45.0F, 22.5F));
      }
   }

   @Nonnull
   public static Model convertToModelPacket(@Nonnull Item item) {
      Model packet = new Model();
      packet.path = item.getModel();
      packet.texture = item.getTexture();
      return packet;
   }

   @Nonnull
   private static EventRegistry getEventRegistry() {
      return AssetEditorPlugin.get().getEventRegistry();
   }

   public static class PlayerPreviewData {
      @Nullable
      private Path weatherAssetPath;
      private boolean keepPreview;
   }
}
