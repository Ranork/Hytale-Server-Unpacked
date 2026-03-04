package com.hypixel.hytale.server.core.asset.type.portalworld;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.gameplay.GameplayConfig;
import java.util.Collections;
import java.util.Set;
import javax.annotation.Nullable;

public class PortalType implements JsonAssetWithMap<String, DefaultAssetMap<String, PortalType>> {
   public static final AssetBuilderCodec<String, PortalType> CODEC = AssetBuilderCodec.builder(
         PortalType.class,
         PortalType::new,
         Codec.STRING,
         (portalType, s) -> portalType.id = s,
         portalType -> portalType.id,
         (asset, data) -> asset.data = data,
         asset -> asset.data
      )
      .append(new KeyedCodec<>("InstanceId", Codec.STRING), (portalType, o) -> portalType.instanceId = o, portalType -> portalType.instanceId)
      .documentation("The instance id (folder name in Assets/Server/Instances) that this instance loads.")
      .add()
      .<PortalDescription>append(
         new KeyedCodec<>("Description", PortalDescription.CODEC), (portalType, o) -> portalType.description = o, portalType -> portalType.description
      )
      .documentation("The qualitative description of the portal.")
      .add()
      .<String[]>append(
         new KeyedCodec<>("CursedItems", Codec.STRING_ARRAY),
         (portalType, o) -> portalType.cursedItems = o == null ? Collections.emptySet() : Set.of(o),
         portalType -> portalType.cursedItems.toArray(String[]::new)
      )
      .documentation("Which item drops are cursed when spawned in this portal.")
      .add()
      .<Boolean>append(
         new KeyedCodec<>("VoidInvasionEnabled", Codec.BOOLEAN),
         (portalType, o) -> portalType.voidInvasionEnabled = o,
         portalType -> portalType.voidInvasionEnabled
      )
      .documentation("Whether the void invasion is enabled for this portal.")
      .add()
      .<String>append(new KeyedCodec<>("GameplayConfig", Codec.STRING), (config, o) -> config.gameplayConfig = o, config -> config.gameplayConfig)
      .documentation("The ID of the GameplayConfig asset for worlds spawned with this portal type.")
      .add()
      .build();
   private static AssetStore<String, PortalType, DefaultAssetMap<String, PortalType>> ASSET_STORE;
   public static final ValidatorCache<String> VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(PortalType::getAssetStore));
   private AssetExtraInfo.Data data;
   private String id;
   private String instanceId;
   private PortalDescription description;
   private String gameplayConfig = "Portal";
   private boolean voidInvasionEnabled = false;
   private Set<String> cursedItems = Collections.emptySet();

   public static AssetStore<String, PortalType, DefaultAssetMap<String, PortalType>> getAssetStore() {
      if (ASSET_STORE == null) {
         ASSET_STORE = AssetRegistry.getAssetStore(PortalType.class);
      }

      return ASSET_STORE;
   }

   public static DefaultAssetMap<String, PortalType> getAssetMap() {
      return (DefaultAssetMap<String, PortalType>)getAssetStore().getAssetMap();
   }

   public String getId() {
      return this.id;
   }

   public String getInstanceId() {
      return this.instanceId;
   }

   public Message getDisplayName() {
      return this.description.getDisplayName();
   }

   public PortalDescription getDescription() {
      return this.description;
   }

   public Set<String> getCursedItems() {
      return this.cursedItems;
   }

   public String getGameplayConfigId() {
      return this.gameplayConfig;
   }

   public boolean isVoidInvasionEnabled() {
      return this.voidInvasionEnabled;
   }

   @Nullable
   public GameplayConfig getGameplayConfig() {
      return GameplayConfig.getAssetMap().getAsset(this.gameplayConfig);
   }
}
