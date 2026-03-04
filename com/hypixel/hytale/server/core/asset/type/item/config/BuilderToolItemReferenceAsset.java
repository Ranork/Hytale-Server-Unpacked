package com.hypixel.hytale.server.core.asset.type.item.config;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.codec.AssetCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import javax.annotation.Nonnull;

public class BuilderToolItemReferenceAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, BuilderToolItemReferenceAsset>> {
   private static AssetStore<String, BuilderToolItemReferenceAsset, DefaultAssetMap<String, BuilderToolItemReferenceAsset>> ASSET_STORE;
   @Nonnull
   public static final AssetCodec<String, BuilderToolItemReferenceAsset> CODEC = AssetBuilderCodec.builder(
         BuilderToolItemReferenceAsset.class,
         BuilderToolItemReferenceAsset::new,
         Codec.STRING,
         (t, k) -> t.id = k,
         t -> t.id,
         (asset, data) -> asset.data = data,
         asset -> asset.data
      )
      .append(new KeyedCodec<>("BuilderToolItems", new ArrayCodec<>(Codec.STRING, String[]::new)), (i, itemIds) -> i.itemIds = itemIds, i -> i.itemIds)
      .add()
      .build();
   private String id;
   protected String[] itemIds;
   private AssetExtraInfo.Data data;

   public static DefaultAssetMap<String, BuilderToolItemReferenceAsset> getAssetMap() {
      return (DefaultAssetMap<String, BuilderToolItemReferenceAsset>)getAssetStore().getAssetMap();
   }

   public static AssetStore<String, BuilderToolItemReferenceAsset, DefaultAssetMap<String, BuilderToolItemReferenceAsset>> getAssetStore() {
      if (ASSET_STORE == null) {
         ASSET_STORE = AssetRegistry.getAssetStore(BuilderToolItemReferenceAsset.class);
      }

      return ASSET_STORE;
   }

   public String[] getItems() {
      return this.itemIds;
   }

   public String getId() {
      return this.id;
   }
}
