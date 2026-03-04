package com.hypixel.hytale.server.core.asset.type.item.config;

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
import com.hypixel.hytale.server.core.asset.common.CommonAssetValidator;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import java.lang.ref.SoftReference;
import javax.annotation.Nonnull;

public class ResourceType
   implements JsonAssetWithMap<String, DefaultAssetMap<String, ResourceType>>,
   NetworkSerializable<com.hypixel.hytale.protocol.ResourceType> {
   public static final AssetBuilderCodec<String, ResourceType> CODEC = AssetBuilderCodec.builder(
         ResourceType.class,
         ResourceType::new,
         Codec.STRING,
         (resourceType, k) -> resourceType.id = k,
         resourceType -> resourceType.id,
         (asset, data) -> asset.data = data,
         asset -> asset.data
      )
      .appendInherited(
         new KeyedCodec<>("Name", Codec.STRING),
         (resourceType, s) -> resourceType.name = s,
         resourceType -> resourceType.name,
         (resourceType, parent) -> resourceType.name = parent.name
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("Description", Codec.STRING),
         (resourceType, s) -> resourceType.description = s,
         resourceType -> resourceType.description,
         (resourceType, parent) -> resourceType.description = parent.description
      )
      .add()
      .<String>appendInherited(
         new KeyedCodec<>("Icon", Codec.STRING),
         (resourceType, s) -> resourceType.icon = s,
         resourceType -> resourceType.icon,
         (resourceType, parent) -> resourceType.icon = parent.icon
      )
      .addValidator(CommonAssetValidator.ICON_RESOURCE)
      .add()
      .build();
   public static final ValidatorCache<String> VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(ResourceType::getAssetStore));
   private static AssetStore<String, ResourceType, DefaultAssetMap<String, ResourceType>> ASSET_STORE;
   protected AssetExtraInfo.Data data;
   protected String id;
   protected String name;
   protected String description;
   protected String icon;
   private SoftReference<com.hypixel.hytale.protocol.ResourceType> cachedPacket;

   public static AssetStore<String, ResourceType, DefaultAssetMap<String, ResourceType>> getAssetStore() {
      if (ASSET_STORE == null) {
         ASSET_STORE = AssetRegistry.getAssetStore(ResourceType.class);
      }

      return ASSET_STORE;
   }

   public static DefaultAssetMap<String, ResourceType> getAssetMap() {
      return (DefaultAssetMap<String, ResourceType>)getAssetStore().getAssetMap();
   }

   public ResourceType(String id, String name, String description, String icon) {
      this.id = id;
      this.name = name;
      this.description = description;
      this.icon = icon;
   }

   protected ResourceType() {
   }

   public String getId() {
      return this.id;
   }

   public String getName() {
      return this.name;
   }

   public String getDescription() {
      return this.description;
   }

   public String getIcon() {
      return this.icon;
   }

   @Nonnull
   public com.hypixel.hytale.protocol.ResourceType toPacket() {
      com.hypixel.hytale.protocol.ResourceType cached = this.cachedPacket == null ? null : this.cachedPacket.get();
      if (cached != null) {
         return cached;
      } else {
         com.hypixel.hytale.protocol.ResourceType packet = new com.hypixel.hytale.protocol.ResourceType(this.id, this.icon);
         this.cachedPacket = new SoftReference<>(packet);
         return packet;
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "ResourceType{id='" + this.id + "', name='" + this.name + "', description='" + this.description + "', icon='" + this.icon + "'}";
   }
}
