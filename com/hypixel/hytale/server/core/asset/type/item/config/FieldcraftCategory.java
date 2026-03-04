package com.hypixel.hytale.server.core.asset.type.item.config;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.server.core.asset.common.CommonAssetValidator;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import java.lang.ref.SoftReference;
import javax.annotation.Nonnull;

public class FieldcraftCategory
   implements JsonAssetWithMap<String, DefaultAssetMap<String, FieldcraftCategory>>,
   NetworkSerializable<com.hypixel.hytale.protocol.ItemCategory> {
   public static final AssetBuilderCodec<String, FieldcraftCategory> CODEC = AssetBuilderCodec.builder(
         FieldcraftCategory.class,
         FieldcraftCategory::new,
         Codec.STRING,
         (itemCategory, k) -> itemCategory.id = k,
         itemCategory -> itemCategory.id,
         (asset, data) -> asset.data = data,
         asset -> asset.data
      )
      .addField(new KeyedCodec<>("Name", Codec.STRING), (itemCategory, s) -> itemCategory.name = s, itemCategory -> itemCategory.name)
      .<String>append(new KeyedCodec<>("Icon", Codec.STRING), (itemCategory, s) -> itemCategory.icon = s, itemCategory -> itemCategory.icon)
      .addValidator(CommonAssetValidator.ICON_CRAFTING)
      .add()
      .addField(new KeyedCodec<>("Order", Codec.INTEGER), (itemCategory, s) -> itemCategory.order = s, itemCategory -> itemCategory.order)
      .build();
   private static DefaultAssetMap<String, FieldcraftCategory> ASSET_MAP;
   protected AssetExtraInfo.Data data;
   protected String id;
   protected String name;
   protected String icon;
   protected int order;
   private SoftReference<com.hypixel.hytale.protocol.ItemCategory> cachedPacket;

   public static DefaultAssetMap<String, FieldcraftCategory> getAssetMap() {
      if (ASSET_MAP == null) {
         ASSET_MAP = (DefaultAssetMap<String, FieldcraftCategory>)AssetRegistry.getAssetStore(FieldcraftCategory.class).getAssetMap();
      }

      return ASSET_MAP;
   }

   protected FieldcraftCategory() {
   }

   @Nonnull
   public com.hypixel.hytale.protocol.ItemCategory toPacket() {
      com.hypixel.hytale.protocol.ItemCategory cached = this.cachedPacket == null ? null : this.cachedPacket.get();
      if (cached != null) {
         return cached;
      } else {
         com.hypixel.hytale.protocol.ItemCategory packet = new com.hypixel.hytale.protocol.ItemCategory();
         packet.id = this.id;
         packet.icon = this.icon;
         packet.name = this.name;
         packet.order = this.order;
         this.cachedPacket = new SoftReference<>(packet);
         return packet;
      }
   }

   public String getId() {
      return this.id;
   }

   public String getName() {
      return this.name;
   }

   public String getIcon() {
      return this.icon;
   }

   @Nonnull
   @Override
   public String toString() {
      return "FieldcraftCategory{id='" + this.id + "', name='" + this.name + "', icon='" + this.icon + "', order=" + this.order + "}";
   }
}
